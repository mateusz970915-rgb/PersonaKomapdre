package com.example.network

import android.content.Context
import com.example.BuildConfig
import com.example.data.AgentPreferencesRepository
import com.example.data.AppDatabase
import com.example.data.LlmCallTelemetry
import com.example.data.DataAccessRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import java.util.concurrent.atomic.AtomicInteger

object AILlmClient {

    private val consecutiveFailures = AtomicInteger(0)

    private fun sanitizeInput(text: String): Pair<String, List<String>> {
        val sanitizedDetails = mutableListOf<String>()
        var result = text

        // 1. Email pattern
        val emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        val emailMatcher = emailPattern.matcher(result)
        var emailFound = false
        while (emailMatcher.find()) {
            emailFound = true
        }
        if (emailFound) {
            result = emailMatcher.replaceAll("[EMAIL_SAFE]")
            sanitizedDetails.add("Adres e-mail")
        }

        // 2. API Key pattern (Gemini)
        val geminiKeyPattern = Pattern.compile("AIzaSy[A-Za-z0-9_-]{35}")
        val geminiMatcher = geminiKeyPattern.matcher(result)
        var geminiFound = false
        while (geminiMatcher.find()) {
            geminiFound = true
        }
        if (geminiFound) {
            result = geminiMatcher.replaceAll("[GEMINI_API_KEY_SAFE]")
            sanitizedDetails.add("Klucz API Gemini")
        }

        // 3. API Key pattern (OpenRouter)
        val openRouterKeyPattern = Pattern.compile("sk-or-v1-[A-Za-z0-9]{64}")
        val orMatcher = openRouterKeyPattern.matcher(result)
        var orFound = false
        while (orMatcher.find()) {
            orFound = true
        }
        if (orFound) {
            result = orMatcher.replaceAll("[OPENROUTER_API_KEY_SAFE]")
            sanitizedDetails.add("Klucz API OpenRouter")
        }

        // 4. Phone Pattern
        val phonePattern = Pattern.compile("(?:\\+?48)?(?:[ -]*\\d){9}")
        val phoneMatcher = phonePattern.matcher(result)
        var phoneFound = false
        while (phoneMatcher.find()) {
            phoneFound = true
        }
        if (phoneFound) {
            result = phoneMatcher.replaceAll("[PHONE_SAFE]")
            sanitizedDetails.add("Numer telefonu")
        }

        // 5. Polish PESEL pattern (11 digits)
        val peselPattern = Pattern.compile("\\b\\d{11}\\b")
        val peselMatcher = peselPattern.matcher(result)
        var peselFound = false
        while (peselMatcher.find()) {
            peselFound = true
        }
        if (peselFound) {
            result = peselMatcher.replaceAll("[PESEL_SAFE]")
            sanitizedDetails.add("Numer PESEL")
        }

        // 6. Polish ID Card pattern (Dowód osobisty - 3 letters + 6 digits)
        val idCardPattern = Pattern.compile("\\b[A-Za-z]{3}\\d{6}\\b")
        val idCardMatcher = idCardPattern.matcher(result)
        var idCardFound = false
        while (idCardMatcher.find()) {
            idCardFound = true
        }
        if (idCardFound) {
            result = idCardMatcher.replaceAll("[ID_CARD_SAFE]")
            sanitizedDetails.add("Dowód osobisty")
        }

        // 7. Polish Postcode (e.g. 00-950)
        val postcodePattern = Pattern.compile("\\b\\d{2}-\\d{3}\\b")
        val postcodeMatcher = postcodePattern.matcher(result)
        var postcodeFound = false
        while (postcodeMatcher.find()) {
            postcodeFound = true
        }
        if (postcodeFound) {
            result = postcodeMatcher.replaceAll("[POSTCODE_SAFE]")
            sanitizedDetails.add("Kod pocztowy")
        }

        // 8. Credit Card Pattern
        val creditCardPattern = Pattern.compile("\\b(?:\\d{4}[ -]?){3}\\d{4}\\b")
        val creditCardMatcher = creditCardPattern.matcher(result)
        var creditCardFound = false
        while (creditCardMatcher.find()) {
            creditCardFound = true
        }
        if (creditCardFound) {
            result = creditCardMatcher.replaceAll("[CREDIT_CARD_SAFE]")
            sanitizedDetails.add("Karta płatnicza")
        }

        return Pair(result, sanitizedDetails)
    }

    private suspend fun performCall(
        context: Context,
        provider: String,
        cleanPrompt: String,
        cleanInstruction: String?,
        prefs: com.example.data.AgentPreferences
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (provider == "openrouter") {
                val apiKey = prefs.openRouterApiKey.trim()
                if (apiKey.isEmpty()) {
                    Result.failure(Exception("OpenRouter API Key is missing."))
                } else {
                    val model = prefs.openRouterSelectedModel.ifBlank { "meta-llama/llama-3.3-70b-instruct:free" }
                    val messages = mutableListOf<OpenRouterChatMessage>()
                    if (!cleanInstruction.isNullOrBlank()) {
                        messages.add(OpenRouterChatMessage(role = "system", content = cleanInstruction))
                    }
                    messages.add(OpenRouterChatMessage(role = "user", content = cleanPrompt))
                    
                    val request = OpenRouterChatRequest(
                        model = model,
                        messages = messages,
                        temperature = 0.7f
                    )
                    val response = OpenRouterClient.service.createChatCompletion("Bearer $apiKey", request = request)
                    if (response.error != null) {
                        Result.failure(Exception("OpenRouter Error (${response.error.code}): ${response.error.message ?: "Unknown error"}"))
                    } else {
                        val content = response.choices?.firstOrNull()?.message?.content
                        if (content != null) {
                            Result.success(content)
                        } else {
                            Result.failure(Exception("No response from OpenRouter."))
                        }
                    }
                }
            } else {
                val userApiKey = prefs.geminiApiKey.trim()
                val apiKey = if (userApiKey.isNotEmpty()) userApiKey else BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    Result.failure(Exception("Gemini API Key is missing."))
                } else {
                    val model = prefs.geminiSelectedModel.ifBlank { "gemini-3.5-flash" }
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = cleanPrompt)))),
                        systemInstruction = if (!cleanInstruction.isNullOrBlank()) Content(parts = listOf(Part(text = cleanInstruction))) else null
                    )
                    val response = RetrofitClient.service.generateContent(model, apiKey, request)
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (text != null) {
                        Result.success(text)
                    } else {
                        Result.failure(Exception("No response from Gemini."))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateContent(
        context: Context,
        prompt: String,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val prefsRepository = AgentPreferencesRepository(context)
        val prefs = prefsRepository.agentPreferencesFlow.first()
        val db = AppDatabase.getDatabase(context)
        val colonyDao = db.colonyDao()

        // Enforce Autonomy / Data Access
        if (!com.example.security.PolicyEnforcementPoint.enforceDataAccess(context)) {
            return@withContext "Error: Data access denied by policy. LLM cannot process data."
        }

        // Apply offline privacy filtering
        val (cleanPrompt, promptSanitizations) = sanitizeInput(prompt)
        val cleanInstruction = systemInstruction?.let { sanitizeInput(it).first }

        if (promptSanitizations.isNotEmpty()) {
            colonyDao.insertDataAccessRequest(
                DataAccessRequest(
                    agentName = "Privacy Sentinel (Offline Filter)",
                    dataType = "[Filtrowanie Wrażliwych Danych]",
                    isPolicyViolation = false,
                    violationReason = "Wykryto i usunięto poufne dane przed wysyłką sieciową: ${promptSanitizations.distinct().joinToString(", ")}."
                )
            )
        }

        val startTime = System.currentTimeMillis()
        val originalProvider = prefs.aiProvider
        var currentProvider = if (originalProvider.isBlank()) "gemini" else originalProvider
        
        var isError = false
        var activeModel = if (currentProvider == "openrouter") prefs.openRouterSelectedModel else prefs.geminiSelectedModel
        var responseText = ""

        // Execute LLM Call with automatic retry and failover
        var executionResult = performCall(context, currentProvider, cleanPrompt, cleanInstruction, prefs)
        
        if (executionResult.isFailure) {
            val count = consecutiveFailures.incrementAndGet()
            if (count >= 3) {
                // Attempt Failover to alternative provider
                val fallbackProvider = if (currentProvider == "openrouter") "gemini" else "openrouter"
                try {
                    colonyDao.insertDataAccessRequest(
                        DataAccessRequest(
                            agentName = "System Failover Coordinator",
                            dataType = "[Automatyczne Przełączenie Dostawcy]",
                            isPolicyViolation = true,
                            violationReason = "Dostawca $currentProvider zgłosił błąd $count razy z rzędu. Następuje automatyczne przełączenie na dostawcę zapasowego: $fallbackProvider."
                        )
                    )
                } catch (e: Exception) {
                    // Ignore database errors during failover logging
                }

                val fallbackResult = performCall(context, fallbackProvider, cleanPrompt, cleanInstruction, prefs)
                if (fallbackResult.isSuccess) {
                    executionResult = fallbackResult
                    currentProvider = fallbackProvider
                    consecutiveFailures.set(0)
                }
            }
        } else {
            consecutiveFailures.set(0)
        }

        val duration = System.currentTimeMillis() - startTime

        if (executionResult.isSuccess) {
            responseText = executionResult.getOrThrow()
            isError = false
            activeModel = if (currentProvider == "openrouter") prefs.openRouterSelectedModel else prefs.geminiSelectedModel
        } else {
            val exception = executionResult.exceptionOrNull()
            responseText = "Error: " + (exception?.localizedMessage ?: exception?.message ?: "Unknown communication error.")
            isError = true
            activeModel = if (currentProvider == "openrouter") prefs.openRouterSelectedModel else prefs.geminiSelectedModel
        }

        // Log telemetry
        try {
            colonyDao.insertLlmTelemetry(
                LlmCallTelemetry(
                    provider = currentProvider,
                    model = activeModel.ifBlank { "unknown" },
                    promptLength = cleanPrompt.length + (cleanInstruction?.length ?: 0),
                    responseLength = if (isError) 0 else responseText.length,
                    durationMs = duration,
                    status = if (isError) "FAILED" else "SUCCESS",
                    errorMessage = if (isError) responseText else null
                )
            )
        } catch (e: Exception) {
            // Silence telemetry insert errors to ensure output always bubbles up
        }

        responseText
    }
}
