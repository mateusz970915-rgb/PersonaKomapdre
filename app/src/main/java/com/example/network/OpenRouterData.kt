package com.example.network

import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterModel(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val context_length: Int? = null,
    val pricing: OpenRouterPricing? = null
) {
    val isFree: Boolean
        get() {
            if (id.endsWith(":free", ignoreCase = true)) return true
            val pPrompt = pricing?.prompt?.toDoubleOrNull() ?: -1.0
            val pComp = pricing?.completion?.toDoubleOrNull() ?: -1.0
            return (pPrompt == 0.0 && pComp == 0.0)
        }
}

@Serializable
data class OpenRouterPricing(
    val prompt: String? = null,
    val completion: String? = null,
    val image: String? = null,
    val request: String? = null
)

@Serializable
data class OpenRouterModelsResponse(
    val data: List<OpenRouterModel> = emptyList()
)

@Serializable
data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterChatMessage>,
    val temperature: Float? = 0.7f,
    val max_tokens: Int? = 1500
)

@Serializable
data class OpenRouterChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

@Serializable
data class OpenRouterChatResponse(
    val id: String? = null,
    val choices: List<OpenRouterChoice>? = null,
    val error: OpenRouterError? = null
)

@Serializable
data class OpenRouterChoice(
    val message: OpenRouterChatMessage? = null,
    val finish_reason: String? = null
)

@Serializable
data class OpenRouterError(
    val message: String? = null,
    val code: Int? = null
)
