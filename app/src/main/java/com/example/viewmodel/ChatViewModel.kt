package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AgentDecision
import com.example.data.CouncilMessage
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.InlineData
import com.example.network.OpenRouterChatMessage
import com.example.network.OpenRouterChatRequest
import com.example.network.OpenRouterClient
import com.example.network.OpenRouterModel
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.network.ThinkingConfig
import com.example.network.Tool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import java.io.ByteArrayOutputStream

class ChatViewModel(application: Application) : BaseAgentViewModel(application) {

    val messages: StateFlow<List<CouncilMessage>> = baseRepository.councilMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _apiErrorState = MutableStateFlow<String?>(null)
    val apiErrorState: StateFlow<String?> = _apiErrorState

    private val _openRouterFreeModels = MutableStateFlow<List<OpenRouterModel>>(OpenRouterClient.FALLBACK_FREE_MODELS)
    val openRouterFreeModels: StateFlow<List<OpenRouterModel>> = _openRouterFreeModels

    private val _isLoadingOpenRouterModels = MutableStateFlow(false)
    val isLoadingOpenRouterModels: StateFlow<Boolean> = _isLoadingOpenRouterModels

    init {
        fetchOpenRouterFreeModels()
    }

    fun clearApiError() {
        _apiErrorState.value = null
    }

    fun clearChat() {
        viewModelScope.launch {
            baseRepository.clearMessages()
        }
    }

    fun fetchOpenRouterFreeModels() {
        viewModelScope.launch {
            _isLoadingOpenRouterModels.value = true
            try {
                val response = withContext(Dispatchers.IO) {
                    OpenRouterClient.service.getModels()
                }
                val fetchedFreeModels = response.data.filter { it.isFree }
                if (fetchedFreeModels.isNotEmpty()) {
                    // Combine with fallback to ensure rich metadata and no duplicates
                    val combinedMap = LinkedHashMap<String, OpenRouterModel>()
                    fetchedFreeModels.forEach { combinedMap[it.id] = it }
                    OpenRouterClient.FALLBACK_FREE_MODELS.forEach {
                        if (!combinedMap.containsKey(it.id)) {
                            combinedMap[it.id] = it
                        }
                    }
                    _openRouterFreeModels.value = combinedMap.values.toList()
                } else {
                    _openRouterFreeModels.value = OpenRouterClient.FALLBACK_FREE_MODELS
                }
            } catch (e: Exception) {
                // If offline or request fails, retain fallback list
                _openRouterFreeModels.value = OpenRouterClient.FALLBACK_FREE_MODELS
            } finally {
                _isLoadingOpenRouterModels.value = false
            }
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun sendMessage(userText: String, bitmap: Bitmap? = null, mode: String = "Fast") {
        val imageBase64 = bitmap?.toBase64()
        sendMessageMultimodal(userText, imageBase64, null, mode)
    }

    fun sendMessageMultimodal(
        userText: String,
        imageBase64: String? = null,
        audioBase64: String? = null,
        mode: String = "Fast"
    ) {
        viewModelScope.launch {
            val history = baseRepository.councilMessages.first()
            var formattedUserMsg = userText
            if (imageBase64 != null) formattedUserMsg += "\n[Załącznik Obrazu]"
            if (audioBase64 != null) formattedUserMsg += "\n[Załącznik Głosu/Audio]"

            baseRepository.insertMessage(CouncilMessage(role = "user", content = formattedUserMsg))

            val currentMemories = baseRepository.allMemories.first()
            val memoryContext = if (currentMemories.isNotEmpty()) {
                "\n\nColony Long-Term Memory:\n" + currentMemories.takeLast(10).joinToString("\n") { "- ${it.content}" }
            } else ""

            val systemInstructionText = """
                You are PersonaMesh, a council of specialized personal agents living in the user's Android phone.
                The agents include Health, Work, Study, Finance, Relationship, Rest, Privacy, and Security.
                Before making a decision or answering the user, you must initiate a brief negotiation or discussion 
                among the relevant agents to consider multiple aspects of the user's life. 
                Present the different points of view, and then provide a unified recommendation from the council.
                Keep it concise but show the multi-agent nature. Use emojis for agents.
            """.trimIndent() + memoryContext

            try {
                val responseText = com.example.network.AILlmClient.generateContent(
                    context = getApplication(),
                    prompt = userText,
                    systemInstruction = systemInstructionText,
                    imageAttachmentBase64 = imageBase64,
                    audioAttachmentBase64 = audioBase64
                )
                baseRepository.insertMessage(CouncilMessage(role = "model", content = responseText))

                // Also record in FTS5 database
                baseRepository.colonyDao.insertFtsContent(
                    com.example.data.AgentInteractionFtsContent(
                        agentName = "Council Assembly",
                        snippet = responseText,
                        modelUsed = mode,
                        tag = "Council Chat"
                    )
                )
            } catch (e: Exception) {
                val errorMsg = "Błąd komunikacji z agentami: ${e.message}"
                _apiErrorState.value = errorMsg
                baseRepository.insertMessage(CouncilMessage(role = "model", content = errorMsg))
            }
        }
    }
}
