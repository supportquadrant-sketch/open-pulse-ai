package com.example.data.repository

import com.example.data.api.OpenPulseApiService
import com.example.data.api.OpenPulseContent
import com.example.data.api.OpenPulseGenerationConfig
import com.example.data.api.OpenPulsePart
import com.example.data.api.OpenPulseRequest
import com.example.data.api.RetrofitClient
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class GenerationEvent {
    data class Chunk(val text: String) : GenerationEvent()
    data class Complete(val fullText: String, val tokens: Int, val latencyMs: Long) : GenerationEvent()
    data class Error(val message: String) : GenerationEvent()
}

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val apiService: OpenPulseApiService = RetrofitClient.openPulseService
) {
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    fun searchConversations(query: String): Flow<List<ConversationEntity>> =
        conversationDao.searchConversations(query)

    fun searchMessages(query: String): Flow<List<MessageEntity>> =
        messageDao.searchMessages(query)

    fun getBookmarkedMessages(): Flow<List<MessageEntity>> =
        messageDao.getBookmarkedMessages()

    suspend fun getConversationById(id: Long): ConversationEntity? =
        conversationDao.getConversationById(id)

    suspend fun createConversation(
        title: String,
        modelId: String = "openpulse-3.5-flash",
        personaId: Long? = null,
        personaName: String? = null
    ): Long {
        val entity = ConversationEntity(
            title = title,
            modelId = modelId,
            personaId = personaId,
            personaName = personaName
        )
        return conversationDao.insertConversation(entity)
    }

    suspend fun updateConversationTitle(id: Long, newTitle: String) {
        conversationDao.updateTitle(id, newTitle)
    }

    suspend fun togglePin(id: Long) {
        conversationDao.togglePin(id)
    }

    suspend fun deleteConversation(id: Long) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearAllConversations() {
        conversationDao.clearAllConversations()
    }

    suspend fun insertUserMessage(
        conversationId: Long,
        text: String,
        inputImageUri: String? = null
    ): Long {
        val estimatedTokens = (text.length / 4).coerceAtLeast(1)
        val message = MessageEntity(
            conversationId = conversationId,
            role = "user",
            content = text,
            tokenUsage = estimatedTokens,
            inputImageUri = inputImageUri
        )
        val msgId = messageDao.insertMessage(message)
        conversationDao.incrementUsage(conversationId, estimatedTokens)
        return msgId
    }

    suspend fun insertAssistantMessage(
        conversationId: Long,
        text: String,
        tokens: Int,
        latencyMs: Long,
        modelUsed: String,
        isError: Boolean = false,
        imageUri: String? = null,
        inputImageUri: String? = null
    ): Long {
        val message = MessageEntity(
            conversationId = conversationId,
            role = "assistant",
            content = text,
            tokenUsage = tokens,
            latencyMs = latencyMs,
            modelUsed = modelUsed,
            isError = isError,
            imageUri = imageUri,
            inputImageUri = inputImageUri
        )
        val msgId = messageDao.insertMessage(message)
        conversationDao.incrementUsage(conversationId, tokens)
        return msgId
    }

    suspend fun toggleBookmark(id: Long) {
        messageDao.toggleBookmark(id)
    }

    suspend fun deleteMessage(id: Long) {
        messageDao.deleteMessageById(id)
    }

    private fun resolveModelCandidates(modelId: String): List<String> {
        return when (modelId.lowercase()) {
            "openpulse-fast-lite", "openpulse-fast", "fast", "flash-lite", "turbo", "lite" -> listOf(
                "gemini-2.5-flash",
                "gemini-flash-latest",
                "gemini-3.5-flash",
                "gemini-3.1-flash-lite-preview"
            )
            "openpulse-3.5-flash", "openpulse-flash", "flash" -> listOf(
                "gemini-2.5-flash",
                "gemini-flash-latest",
                "gemini-3.5-flash"
            )
            "openpulse-3.1-pro", "openpulse-pro", "pro" -> listOf(
                "gemini-2.5-pro",
                "gemini-3.1-pro-preview",
                "gemini-2.5-flash"
            )
            else -> if (modelId.startsWith("gemini-")) {
                listOf(modelId, "gemini-2.5-flash", "gemini-flash-latest")
            } else {
                listOf("gemini-2.5-flash", "gemini-flash-latest", "gemini-3.5-flash")
            }
        }
    }

    suspend fun generateAiResponseStream(
        conversationId: Long,
        userPrompt: String,
        systemPrompt: String?,
        modelId: String,
        temperature: Float,
        apiKey: String,
        isTurboSpeed: Boolean = true,
        onEvent: (GenerationEvent) -> Unit
    ) = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (apiKey.isBlank()) {
            onEvent(
                GenerationEvent.Error(
                    "API Key is missing. Please configure your OpenPulse API Key in Settings or set the GEMINI_API_KEY environment secret."
                )
            )
            return@withContext
        }

        val previousMessages = messageDao.getMessagesListForConversation(conversationId)

        // Latency Optimization: Keep the most recent 10 messages for ultra-fast ingestion (<1s)
        val recentHistory = if (previousMessages.size > 10) {
            previousMessages.takeLast(10)
        } else {
            previousMessages
        }

        // Build contents list for multi-turn conversation
        val contentsList = mutableListOf<OpenPulseContent>()
        for (msg in recentHistory) {
            val role = if (msg.role == "assistant") "model" else "user"
            contentsList.add(
                OpenPulseContent(
                    role = role,
                    parts = listOf(OpenPulsePart(text = msg.content))
                )
            )
        }

        // Include the latest prompt if not already present
        if (contentsList.isEmpty() || contentsList.last().parts.firstOrNull()?.text != userPrompt) {
            contentsList.add(
                OpenPulseContent(
                    role = "user",
                    parts = listOf(OpenPulsePart(text = userPrompt))
                )
            )
        }

        val systemInstruction = if (!systemPrompt.isNullOrBlank()) {
            OpenPulseContent(
                parts = listOf(OpenPulsePart(text = systemPrompt))
            )
        } else null

        val candidateModels = resolveModelCandidates(modelId)
        var lastErrorMessage = "Unknown error"

        for (candidate in candidateModels) {
            val isFastModel = candidate.contains("lite") || candidate.contains("flash")
            val isGemini25 = candidate.contains("2.5") || candidate.contains("flash-latest")

            // Sub-second Latency Optimization: Disable reasoning wait-time to stream immediately (<1-2s TTFT)
            val thinkingConfig = if (isGemini25) {
                if (isTurboSpeed || isFastModel) {
                    com.example.data.api.OpenPulseThinkingConfig(thinkingBudget = 0)
                } else null
            } else {
                if (isTurboSpeed || isFastModel) {
                    com.example.data.api.OpenPulseThinkingConfig(thinkingLevel = "MINIMAL")
                } else {
                    com.example.data.api.OpenPulseThinkingConfig(thinkingLevel = "LOW")
                }
            }

            val request = OpenPulseRequest(
                contents = contentsList,
                systemInstruction = systemInstruction,
                generationConfig = OpenPulseGenerationConfig(
                    temperature = temperature,
                    topP = 0.95f,
                    maxOutputTokens = 2048,
                    thinkingConfig = thinkingConfig
                )
            )

            // Attempt 1: Real-time SSE Streaming
            try {
                val response = apiService.streamGenerateContent(candidate, apiKey, request)
                if (response.isSuccessful && response.body() != null) {
                    val accumulatedText = StringBuilder()
                    val inputStream = response.body()!!.byteStream()
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        val rawLine = line?.trim() ?: continue
                        if (!rawLine.startsWith("data:")) continue
                        val jsonPayload = rawLine.removePrefix("data:").trim()
                        if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

                        try {
                            val jsonObj = JSONObject(jsonPayload)
                            val candidatesArray = jsonObj.optJSONArray("candidates")
                            if (candidatesArray != null && candidatesArray.length() > 0) {
                                val cand = candidatesArray.getJSONObject(0)
                                val content = cand.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    for (p in 0 until parts.length()) {
                                        val partObj = parts.getJSONObject(p)
                                        val isThought = partObj.optBoolean("thought", false)
                                        if (!isThought) {
                                            val textChunk = partObj.optString("text", "")
                                            if (textChunk.isNotEmpty()) {
                                                accumulatedText.append(textChunk)
                                                onEvent(GenerationEvent.Chunk(textChunk))
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }

                    val finalText = accumulatedText.toString()
                    if (finalText.isNotBlank()) {
                        val latency = System.currentTimeMillis() - startTime
                        val estimatedTokens = (finalText.length / 4 + userPrompt.length / 4).coerceAtLeast(1)
                        onEvent(GenerationEvent.Complete(finalText, estimatedTokens, latency))
                        return@withContext
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                    lastErrorMessage = try {
                        val json = JSONObject(errorBody)
                        json.optJSONObject("error")?.optString("message") ?: errorBody
                    } catch (e: Exception) {
                        errorBody
                    }
                    // If high demand or overloaded, continue to next candidate model immediately!
                }
            } catch (e: Exception) {
                lastErrorMessage = e.localizedMessage ?: "Connection error"
            }

            // Attempt 2: Direct generateContent fallback for this candidate
            try {
                val directResponse = apiService.generateContent(candidate, apiKey, request)
                if (directResponse.isSuccessful && directResponse.body() != null) {
                    val candidateObj = directResponse.body()?.candidates?.firstOrNull()
                    val text = candidateObj?.content?.parts?.firstOrNull()?.text
                    if (!text.isNullOrBlank()) {
                        val latency = System.currentTimeMillis() - startTime
                        val estimatedTokens = (text.length / 4 + userPrompt.length / 4).coerceAtLeast(1)
                        onEvent(GenerationEvent.Complete(text, estimatedTokens, latency))
                        return@withContext
                    }
                }
            } catch (_: Exception) {
            }
        }

        // If all candidate models in the pool were exhausted
        onEvent(GenerationEvent.Error("OpenPulse Engine Notice: $lastErrorMessage"))
    }
}
