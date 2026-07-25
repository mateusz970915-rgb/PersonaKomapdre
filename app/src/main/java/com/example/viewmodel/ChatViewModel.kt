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
        viewModelScope.launch {
            val history = baseRepository.councilMessages.first()
            baseRepository.insertMessage(CouncilMessage(role = "user", content = userText))

            val currentMemories = baseRepository.allMemories.first()
            val memoryContext = if (currentMemories.isNotEmpty()) {
                "\n\nColony Long-Term Memory:\n" + currentMemories.takeLast(10).joinToString("\n") { "- ${it.content}" }
            } else ""

            val systemInstructionText = """
                You are PersonaMesh, a council of specialized personal agents living in the user's Android phone.
                The agents include Health, Work, Study, Finance, Relationship, Rest, Privacy, and Security.
                Before making a decision or answering the user, you must simulate a brief negotiation or discussion 
                among the relevant agents to consider multiple aspects of the user's life. 
                Present the different points of view, and then provide a unified recommendation from the council.
                Keep it concise but show the multi-agent nature. Use emojis for agents.
            """.trimIndent() + memoryContext

            val currentPrefs = preferencesRepository.agentPreferencesFlow.first()
            val provider = currentPrefs.aiProvider

            if (provider == "openrouter") {
                val openRouterKey = currentPrefs.openRouterApiKey.trim()
                if (openRouterKey.isEmpty()) {
                    val errorMsg = "System Halted: OpenRouter API Key missing. Please configure your key in Settings."
                    _apiErrorState.value = errorMsg
                    baseRepository.insertMessage(CouncilMessage(role = "model", content = errorMsg))
                    return@launch
                }

                val selectedModel = currentPrefs.openRouterSelectedModel.ifBlank { "meta-llama/llama-3.3-70b-instruct:free" }

                val openRouterMessages = mutableListOf<OpenRouterChatMessage>()
                openRouterMessages.add(OpenRouterChatMessage(role = "system", content = systemInstructionText))

                history.forEach { msg ->
                    openRouterMessages.add(
                        OpenRouterChatMessage(
                            role = if (msg.role == "user") "user" else "assistant",
                            content = msg.content
                        )
                    )
                }

                openRouterMessages.add(OpenRouterChatMessage(role = "user", content = userText))

                val request = OpenRouterChatRequest(
                    model = selectedModel,
                    messages = openRouterMessages,
                    temperature = 0.7f
                )

                try {
                    val response = withContext(Dispatchers.IO) {
                        OpenRouterClient.service.createChatCompletion("Bearer $openRouterKey", request = request)
                    }

                    if (response.error != null) {
                        val errorMsg = "OpenRouter Error (${response.error.code}): ${response.error.message}"
                        _apiErrorState.value = errorMsg
                        baseRepository.insertMessage(CouncilMessage(role = "model", content = errorMsg))
                        return@launch
                    }

                    val responseText = response.choices?.firstOrNull()?.message?.content
                        ?: "No response content received from OpenRouter."

                    baseRepository.insertMessage(CouncilMessage(role = "model", content = responseText))

                    baseRepository.insertDecision(
                        AgentDecision(
                            agentName = "Council (OpenRouter: $selectedModel)",
                            actionDescription = if (userText.length > 50) "Formulated action plan: \"${userText.take(47)}...\"" else "Formulated action plan: \"$userText\"",
                            dataUsed = "User input via OpenRouter API ($selectedModel)",
                            confidenceLevel = "High (OpenRouter response)",
                            dissentingOpinions = "None"
                        )
                    )
                } catch (e: Exception) {
                    val errorMsg = "OpenRouter Connection Error: ${e.localizedMessage ?: e.message}"
                    _apiErrorState.value = errorMsg
                    baseRepository.insertMessage(CouncilMessage(role = "model", content = errorMsg))
                }
            } else {
                // Gemini API Provider
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    baseRepository.insertMessage(CouncilMessage(role = "model", content = "System Halted: LLM Engine Offline"))
                    return@launch
                }

                val apiContents = history.map { msg ->
                    Content(
                        role = if (msg.role == "user") "user" else "model",
                        parts = listOf(Part(text = msg.content))
                    )
                }.toMutableList()

                val currentParts = mutableListOf<Part>()
                currentParts.add(Part(text = userText))

                if (bitmap != null) {
                    currentParts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = bitmap.toBase64())))
                }

                apiContents.add(Content(role = "user", parts = currentParts))

                val model = "gemini-3.5-flash"

                val generationConfig = if (mode == "Deep Think") {
                    GenerationConfig(thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH"))
                } else {
                    null
                }

                val toolsList = if (mode == "Search") {
                    listOf(Tool(googleSearch = JsonObject(emptyMap())))
                } else {
                    null
                }

                val request = GenerateContentRequest(
                    contents = apiContents,
                    systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
                    generationConfig = generationConfig,
                    tools = toolsList
                )

                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.service.generateContent(model, apiKey, request)
                    }
                    val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "No response from the council."
                    baseRepository.insertMessage(CouncilMessage(role = "model", content = responseText))

                    val consensusPrompt = """
                        You are the Colony Consensus Judge.
                        Read the following council response:
                        "$responseText"
                        
                        Evaluate the degree of agreement among the agents. Return a raw JSON object (without markdown blocks) with two fields:
                        1. "confidenceLevel": A string describing the consensus (e.g. "High (85% consensus)", "Medium (60% consensus)").
                        2. "dissentingOpinions": A brief string summarizing any dissenting opinions. If none, say "None".
                    """.trimIndent()

                    val judgeRequest = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = consensusPrompt))))
                    )

                    val judgeResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, judgeRequest)
                    }

                    var confidence = "Medium (Unknown consensus)"
                    var dissents = "None"

                    val rawJsonText = judgeResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                    try {
                        val cleanJson = rawJsonText.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
                        val jsonObj = org.json.JSONObject(cleanJson)
                        confidence = jsonObj.optString("confidenceLevel", confidence)
                        dissents = jsonObj.optString("dissentingOpinions", dissents)
                    } catch (e: Exception) {
                        // Fallback if parsing fails
                    }

                    baseRepository.insertDecision(
                        AgentDecision(
                            agentName = "Council Consensus",
                            actionDescription = if (userText.length > 50) "Formulated action plan: \"${userText.take(47)}...\"" else "Formulated action plan: \"$userText\"",
                            dataUsed = "User input, attached image (${bitmap != null})",
                            confidenceLevel = confidence,
                            dissentingOpinions = dissents
                        )
                    )
                } catch (e: Exception) {
                    val errorMsg = "Error communicating with the council: ${e.localizedMessage ?: e.message}"
                    _apiErrorState.value = errorMsg
                    baseRepository.insertMessage(CouncilMessage(role = "model", content = errorMsg))
                }
            }
        }
    }
}
