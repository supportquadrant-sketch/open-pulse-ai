package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenPulseRequest(
    @Json(name = "contents") val contents: List<OpenPulseContent>,
    @Json(name = "systemInstruction") val systemInstruction: OpenPulseContent? = null,
    @Json(name = "generationConfig") val generationConfig: OpenPulseGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class OpenPulseContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<OpenPulsePart>
)

@JsonClass(generateAdapter = true)
data class OpenPulsePart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: OpenPulseInlineData? = null
)

@JsonClass(generateAdapter = true)
data class OpenPulseInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class OpenPulseImageConfig(
    @Json(name = "aspectRatio") val aspectRatio: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenPulseGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @Json(name = "thinkingConfig") val thinkingConfig: OpenPulseThinkingConfig? = null,
    @Json(name = "responseModalities") val responseModalities: List<String>? = null,
    @Json(name = "imageConfig") val imageConfig: OpenPulseImageConfig? = null
)

@JsonClass(generateAdapter = true)
data class OpenPulseThinkingConfig(
    @Json(name = "thinkingBudget") val thinkingBudget: Int? = null,
    @Json(name = "thinkingLevel") val thinkingLevel: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenPulseResponse(
    @Json(name = "candidates") val candidates: List<OpenPulseCandidate>? = null,
    @Json(name = "usageMetadata") val usageMetadata: OpenPulseUsageMetadata? = null,
    @Json(name = "error") val error: OpenPulseError? = null
)

@JsonClass(generateAdapter = true)
data class OpenPulseCandidate(
    @Json(name = "content") val content: OpenPulseContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenPulseUsageMetadata(
    @Json(name = "promptTokenCount") val promptTokenCount: Int? = null,
    @Json(name = "candidatesTokenCount") val candidatesTokenCount: Int? = null,
    @Json(name = "totalTokenCount") val totalTokenCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class OpenPulseError(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: String? = null
)
