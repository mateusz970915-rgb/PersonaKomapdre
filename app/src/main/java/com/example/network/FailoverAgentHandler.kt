package com.example.network

import android.content.Context
import android.util.Log
import com.example.data.Agent
import com.example.data.AppDatabase
import com.example.data.DataAccessRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Failover Agent Handler: Automatic task redirection to a secondary agent upon timeout
 * or high latency exceeding user-configured thresholds.
 */
class FailoverAgentHandler(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val colonyDao = database.colonyDao()

    suspend fun executeWithFailover(
        primaryAgent: Agent,
        prompt: String,
        systemInstruction: String? = null,
        onFailoverTriggered: (secondaryAgent: Agent, reason: String) -> Unit = { _, _ -> }
    ): String = withContext(Dispatchers.IO) {
        val maxLatencyMs = if (primaryAgent.maxLatencyThresholdMs > 0) primaryAgent.maxLatencyThresholdMs else 5000L
        val startTime = System.currentTimeMillis()

        Log.d("FailoverAgentHandler", "Executing prompt with primary agent ${primaryAgent.name} (Max Latency: ${maxLatencyMs}ms)...")

        var primaryResponse: String? = null
        var primaryException: Exception? = null

        try {
            primaryResponse = AILlmClient.generateContent(
                context = context,
                prompt = prompt,
                systemInstruction = systemInstruction ?: primaryAgent.systemPrompt
            )
        } catch (e: Exception) {
            primaryException = e
            Log.w("FailoverAgentHandler", "Primary agent ${primaryAgent.name} failed: ${e.message}")
        }

        val latencyMs = System.currentTimeMillis() - startTime

        val isTimeoutOrLatencySpike = latencyMs > maxLatencyMs
        val isError = primaryException != null || primaryResponse.isNullOrBlank()

        if ((isError || isTimeoutOrLatencySpike) && primaryAgent.failoverAgentId != null) {
            val secondaryAgent = colonyDao.getAgentById(primaryAgent.failoverAgentId)
            if (secondaryAgent != null) {
                val reason = if (isTimeoutOrLatencySpike) {
                    "Przekroczono próg opóźnienia (${latencyMs}ms > ${maxLatencyMs}ms)."
                } else {
                    "Błąd agenta głównego: ${primaryException?.message ?: "Brak odpowiedzi"}."
                }

                Log.i("FailoverAgentHandler", "Triggering failover to secondary agent ${secondaryAgent.name}. Reason: $reason")

                // Log failover event in DataAccessRequest / System Log
                colonyDao.insertDataAccessRequest(
                    DataAccessRequest(
                        agentName = primaryAgent.name,
                        dataType = "[Failover Redirection]",
                        isPolicyViolation = true,
                        violationReason = "Przekierowanie do agenta zapasowego ${secondaryAgent.name}. Powód: $reason"
                    )
                )

                onFailoverTriggered(secondaryAgent, reason)

                // Execute prompt with secondary failover agent
                return@withContext try {
                    AILlmClient.generateContent(
                        context = context,
                        prompt = "[FAILOVER DELEGATION FROM ${primaryAgent.name}]\n$prompt",
                        systemInstruction = secondaryAgent.systemPrompt.ifBlank { systemInstruction }
                    )
                } catch (secondaryErr: Exception) {
                    Log.e("FailoverAgentHandler", "Secondary failover agent ${secondaryAgent.name} also failed: ${secondaryErr.message}")
                    primaryResponse ?: "Błąd: Zarówno agent główny, jak i agent zapasowy (${secondaryAgent.name}) nie odpowiedzieli."
                }
            }
        }

        if (primaryResponse != null) {
            return@withContext primaryResponse
        } else {
            throw primaryException ?: Exception("Primary agent ${primaryAgent.name} execution failed.")
        }
    }
}
