package com.example.network

import com.example.data.PersonaAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonaMeshService {
    
    suspend fun processUserQuery(apiKey: String, agent: PersonaAgent, query: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val systemInstruction = Content(
                    role = "system",
                    parts = listOf(
                        Part(text = "You are ${agent.name}, a PersonaAgent. Your personality is: ${agent.personalityType}. Your role is: ${agent.role}. You process user queries and manage digital life tasks.")
                    )
                )

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(role = "user", parts = listOf(Part(text = query)))
                    ),
                    systemInstruction = systemInstruction,
                    generationConfig = GenerationConfig(
                        thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
                    )
                )

                val response = RetrofitClient.service.generateContent(
                    model = "gemini-3.1-pro-preview",
                    apiKey = apiKey,
                    request = request
                )
                
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response from PersonaMesh agent."
            } catch (e: Exception) {
                e.printStackTrace()
                "Error processing query: ${e.message}"
            }
        }
    }
}
