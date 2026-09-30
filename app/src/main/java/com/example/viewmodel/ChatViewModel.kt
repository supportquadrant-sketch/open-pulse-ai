package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PersonaEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.GenerationEvent
import com.example.data.repository.PersonaRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ChatUiState(
    val activeConversation: ConversationEntity? = null,
    val isStreaming: Boolean = false,
    val streamingPartialText: String = "",
    val activePersona: PersonaEntity? = null,
    val selectedModel: String = "openpulse-fast-lite",
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val showPersonaPicker: Boolean = false,
    val showPromptPicker: Boolean = false,
    val showNewChatDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val showModelPicker: Boolean = false
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val personaRepository: PersonaRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    val conversations: StateFlow<List<ConversationEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                chatRepository.allConversations
            } else {
                chatRepository.searchConversations(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<Long?>(null)

    val currentMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else chatRepository.getMessagesForConversation(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personas: StateFlow<List<PersonaEntity>> = personaRepository.allPersonas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeGenerationJob: Job? = null

    init {
        viewModelScope.launch {
            // Load default persona and initial conversation if any
            val defaultPersona = personaRepository.getDefaultPersona()
            val settings = settingsRepository.settings.value
            _uiState.value = _uiState.value.copy(
                activePersona = defaultPersona,
                selectedModel = settings.defaultModel
            )
        }
    }

    fun selectConversation(conversation: ConversationEntity) {
        _activeConversationId.value = conversation.id
        _uiState.value = _uiState.value.copy(
            activeConversation = conversation,
            selectedModel = conversation.modelId
        )
        // Set persona if assigned
        viewModelScope.launch {
            if (conversation.personaId != null) {
                val p = personaRepository.getPersonaById(conversation.personaId)
                _uiState.value = _uiState.value.copy(activePersona = p)
            }
        }
    }

    fun startNewConversation(
        title: String = "New Chat",
        persona: PersonaEntity? = _uiState.value.activePersona
    ) {
        viewModelScope.launch {
            val settings = settingsRepository.settings.value
            val convId = chatRepository.createConversation(
                title = title,
                modelId = settings.defaultModel,
                personaId = persona?.id,
                personaName = persona?.name
            )
            val newConv = chatRepository.getConversationById(convId)
            _activeConversationId.value = convId
            _uiState.value = _uiState.value.copy(
                activeConversation = newConv,
                activePersona = persona,
                selectedModel = settings.defaultModel,
                showNewChatDialog = false
            )
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        viewModelScope.launch {
            var conv = _uiState.value.activeConversation
            if (conv == null) {
                // Auto-generate title from first prompt
                val autoTitle = if (userText.length > 28) userText.take(28) + "..." else userText
                val settings = settingsRepository.settings.value
                val convId = chatRepository.createConversation(
                    title = autoTitle,
                    modelId = _uiState.value.selectedModel,
                    personaId = _uiState.value.activePersona?.id,
                    personaName = _uiState.value.activePersona?.name
                )
                conv = chatRepository.getConversationById(convId)
                _activeConversationId.value = convId
                _uiState.value = _uiState.value.copy(activeConversation = conv)
            } else if (conv.title == "New Chat") {
                // Update title if still default
                val autoTitle = if (userText.length > 28) userText.take(28) + "..." else userText
                chatRepository.updateConversationTitle(conv.id, autoTitle)
                _uiState.value = _uiState.value.copy(activeConversation = conv.copy(title = autoTitle))
            }

            val conversationId = conv?.id ?: return@launch

            // Insert user message
            chatRepository.insertUserMessage(conversationId, userText)

            // Trigger AI Generation
            generateAiResponse(conversationId, userText)
        }
    }

    private fun generateAiResponse(conversationId: Long, prompt: String) {
        activeGenerationJob?.cancel()

        _uiState.value = _uiState.value.copy(
            isStreaming = true,
            streamingPartialText = "",
            errorMessage = null
        )

        activeGenerationJob = viewModelScope.launch {
            val settings = settingsRepository.settings.value
            val apiKey = RetrofitClient.getEffectiveApiKey(settings.customApiKey)
            val modelId = _uiState.value.selectedModel
            val systemPrompt = _uiState.value.activePersona?.systemPrompt
            val temperature = _uiState.value.activePersona?.defaultTemperature ?: settings.defaultTemperature
            val isTurbo = settings.isTurboSpeedEnabled
            chatRepository.generateAiResponseStream(
                conversationId = conversationId,
                userPrompt = prompt,
                systemPrompt = systemPrompt,
                modelId = modelId,
                temperature = temperature,
                apiKey = apiKey,
                isTurboSpeed = isTurbo
            ) { event ->
                when (event) {
                    is GenerationEvent.Chunk -> {
                        _uiState.value = _uiState.value.copy(
                            streamingPartialText = _uiState.value.streamingPartialText + event.text
                        )
                    }
                    is GenerationEvent.Complete -> {
                        viewModelScope.launch {
                            chatRepository.insertAssistantMessage(
                                conversationId = conversationId,
                                text = event.fullText,
                                tokens = event.tokens,
                                latencyMs = event.latencyMs,
                                modelUsed = modelId,
                                isError = false
                            )
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                streamingPartialText = ""
                            )
                        }
                    }
                    is GenerationEvent.Error -> {
                        viewModelScope.launch {
                            chatRepository.insertAssistantMessage(
                                conversationId = conversationId,
                                text = event.message,
                                tokens = 0,
                                latencyMs = 0L,
                                modelUsed = modelId,
                                isError = true
                            )
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                streamingPartialText = "",
                                errorMessage = event.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun regenerateLastMessage() {
        val convId = _activeConversationId.value ?: return
        val messages = currentMessages.value
        val lastUserMessage = messages.lastOrNull { it.role == "user" } ?: return
        generateAiResponse(convId, lastUserMessage.content)
    }

    fun toggleBookmark(messageId: Long) {
        viewModelScope.launch {
            chatRepository.toggleBookmark(messageId)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }

    fun togglePinConversation(conversationId: Long) {
        viewModelScope.launch {
            chatRepository.togglePin(conversationId)
        }
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationId)
            if (_activeConversationId.value == conversationId) {
                _activeConversationId.value = null
                _uiState.value = _uiState.value.copy(activeConversation = null)
            }
        }
    }

    fun setPersona(persona: PersonaEntity) {
        _uiState.value = _uiState.value.copy(
            activePersona = persona,
            showPersonaPicker = false
        )
    }

    fun setModel(modelId: String) {
        _uiState.value = _uiState.value.copy(selectedModel = modelId)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun togglePersonaPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPersonaPicker = show)
    }

    fun togglePromptPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPromptPicker = show)
    }

    fun toggleExportDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showExportDialog = show)
    }

    fun toggleModelPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showModelPicker = show)
    }

    fun formatConversationExport(messages: List<MessageEntity>, conv: ConversationEntity?): String {
        return formatConversationExportText(messages, conv)
    }

    fun formatConversationExportText(messages: List<MessageEntity>, conv: ConversationEntity?): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("==================================================\n")
        sb.append("OpenPulse AI Chat Export\n")
        sb.append("Title: ${conv?.title ?: "OpenPulse AI Chat"}\n")
        sb.append("Model: ${conv?.modelId ?: "gemini-2.5-flash"}\n")
        sb.append("Persona: ${conv?.personaName ?: "Default"}\n")
        sb.append("Exported: ${dateFormat.format(Date())}\n")
        sb.append("Total Messages: ${messages.size}\n")
        sb.append("==================================================\n\n")

        for (m in messages) {
            val author = if (m.role == "user") "User" else (conv?.personaName ?: "OpenPulse Assistant")
            val timeStr = dateFormat.format(Date(m.timestamp))
            sb.append("[$author] ($timeStr)\n")
            sb.append("${m.content.trim()}\n\n")
            sb.append("--------------------------------------------------\n\n")
        }
        return sb.toString()
    }

    fun formatConversationExportJson(messages: List<MessageEntity>, conv: ConversationEntity?): String {
        val root = JSONObject()
        root.put("exportVersion", "1.0")
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        root.put("exportedAt", isoFormat.format(Date()))

        val convObj = JSONObject()
        convObj.put("id", conv?.id ?: 0L)
        convObj.put("title", conv?.title ?: "OpenPulse AI Chat")
        convObj.put("modelId", conv?.modelId ?: "gemini-2.5-flash")
        convObj.put("personaName", conv?.personaName ?: "Default")
        convObj.put("createdAt", conv?.createdAt ?: System.currentTimeMillis())
        convObj.put("updatedAt", conv?.updatedAt ?: System.currentTimeMillis())
        convObj.put("totalTokens", conv?.totalTokens ?: 0)
        convObj.put("messageCount", messages.size)
        root.put("conversation", convObj)

        val messagesArray = JSONArray()
        for (m in messages) {
            val msgObj = JSONObject()
            msgObj.put("id", m.id)
            msgObj.put("role", m.role)
            msgObj.put("author", if (m.role == "user") "User" else (conv?.personaName ?: "OpenPulse Assistant"))
            msgObj.put("content", m.content)
            msgObj.put("timestamp", m.timestamp)
            msgObj.put("tokenUsage", m.tokenUsage)
            msgObj.put("latencyMs", m.latencyMs)
            if (m.modelUsed != null) {
                msgObj.put("modelUsed", m.modelUsed)
            }
            msgObj.put("isError", m.isError)
            messagesArray.put(msgObj)
        }
        root.put("messages", messagesArray)

        return root.toString(2)
    }

    fun getExportFileName(conv: ConversationEntity?, extension: String): String {
        val rawTitle = conv?.title ?: "chat"
        val safeTitle = rawTitle.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9_-]"), "_")
            .take(25)
            .trim('_')
            .ifEmpty { "chat" }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "openpulse_${safeTitle}_${timestamp}.$extension"
    }
}

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val personaRepository: PersonaRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(chatRepository, personaRepository, settingsRepository) as T
    }
}
