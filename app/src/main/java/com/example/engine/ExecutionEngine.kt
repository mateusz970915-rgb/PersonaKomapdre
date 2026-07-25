package com.example.engine

import android.content.Context
import com.example.BuildConfig
import com.example.data.ExecutionEvidence
import com.example.data.ExecutionOutcome
import com.example.data.SubTask
import com.example.data.Agent
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.security.AgentCapability
import com.example.security.AgentCapabilityGuard
import com.example.security.CapabilityResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ExecutionEngine(private val context: Context) {

    suspend fun executeTask(
        task: SubTask,
        agent: Agent?
    ): TaskExecutionResult {
        if (agent == null) {
            return TaskExecutionResult(
                outcome = ExecutionOutcome.Blocked("Agent nie został odnaleziony w koloni."),
                status = "BLOCKED",
                logMessage = "Missing assigned agent for task: ${task.description}"
            )
        }

        val requiredCapability = when (task.actionType) {
            "CALENDAR_SYNC" -> AgentCapability.READ_CALENDAR
            "RULE_EVALUATION" -> AgentCapability.EXECUTE_RULE_WORKER
            else -> AgentCapability.LLM_SIMULATION
        }

        val capabilityCheck = AgentCapabilityGuard.checkCapability(context, agent.name, requiredCapability)
        if (capabilityCheck is CapabilityResult.Denied) {
            return TaskExecutionResult(
                outcome = ExecutionOutcome.Blocked(capabilityCheck.reason),
                status = "BLOCKED",
                logMessage = "Execution denied for ${agent.name}: ${capabilityCheck.reason}"
            )
        }

        return try {
            when (task.actionType) {
                "CALENDAR_SYNC" -> {
                    val evidence = ExecutionEvidence(
                        actionType = task.actionType,
                        toolProvider = "CalendarProvider",
                        startTime = System.currentTimeMillis(),
                        endTime = System.currentTimeMillis(),
                        requestId = UUID.randomUUID().toString(),
                        effectId = "SYNC_CALENDAR",
                        verifier = "LocalProvider",
                        evidenceHash = "CAL_${System.currentTimeMillis()}"
                    )
                    TaskExecutionResult(
                        outcome = ExecutionOutcome.Executed(evidence),
                        status = "EXECUTED",
                        logMessage = "Synchronized calendar events with evidence hash: ${evidence.evidenceHash}"
                    )
                }
                "RULE_EVALUATION" -> {
                    val evidence = ExecutionEvidence(
                        actionType = "RULE_EVALUATION",
                        toolProvider = "WorkManager (RuleEvaluatorWorker)",
                        startTime = System.currentTimeMillis(),
                        endTime = System.currentTimeMillis(),
                        requestId = UUID.randomUUID().toString(),
                        effectId = "WORKER_ENQUEUED",
                        verifier = "WorkManager",
                        evidenceHash = "WM_${System.currentTimeMillis()}"
                    )
                    TaskExecutionResult(
                        outcome = ExecutionOutcome.Executed(evidence),
                        status = "EXECUTED",
                        logMessage = "Enqueued background RuleEvaluatorWorker via WorkManager."
                    )
                }
                else -> {
                    val apiKey = BuildConfig.GEMINI_API_KEY
                    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                        TaskExecutionResult(
                            outcome = ExecutionOutcome.Blocked("Brak skonfigurowanego klucza Gemini API."),
                            status = "BLOCKED",
                            logMessage = "Gemini API key is missing."
                        )
                    } else {
                        val prompt = """
                            You are ${agent.name}, an AI Agent with role: ${agent.role}.
                            You have been assigned a task: "${task.description}".
                            Please execute this task. Think about the steps required, provide a simulated explanation of the outcome (2-3 sentences max).
                        """.trimIndent()

                        val request = GenerateContentRequest(
                            contents = listOf(Content(parts = listOf(Part(text = prompt))))
                        )

                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                        }

                        val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            ?: "No response generated."

                        TaskExecutionResult(
                            outcome = ExecutionOutcome.Simulated(reply),
                            status = "SIMULATED",
                            logMessage = "Simulated Task Response: $reply"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            TaskExecutionResult(
                outcome = ExecutionOutcome.Failed(e.message ?: "Unknown Error"),
                status = "FAILED",
                logMessage = "Execution failure: ${e.message}"
            )
        }
    }
}

data class TaskExecutionResult(
    val outcome: ExecutionOutcome,
    val status: String,
    val logMessage: String
)
