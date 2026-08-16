package com.example.engine

import android.content.Context
import android.provider.CalendarContract
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkInfo
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
import com.example.security.PolicyEnforcementPoint
import com.example.worker.RuleEvaluatorWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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

        if (!PolicyEnforcementPoint.enforceAutonomy(context, "High")) {
            return TaskExecutionResult(
                outcome = ExecutionOutcome.Blocked("System autonomy policy blocked this execution."),
                status = "BLOCKED",
                logMessage = "Execution denied by PolicyEnforcementPoint (Autonomy)."
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
                    if (!PolicyEnforcementPoint.enforceDataAccess(context)) {
                        return TaskExecutionResult(
                            outcome = ExecutionOutcome.Blocked("Data access denied by policy."),
                            status = "BLOCKED",
                            logMessage = "Data access denied."
                        )
                    }
                    var eventCount = 0
                    try {
                        val cursor = context.contentResolver.query(
                            CalendarContract.Events.CONTENT_URI,
                            arrayOf(CalendarContract.Events._ID, CalendarContract.Events.TITLE),
                            null, null, null
                        )
                        eventCount = cursor?.count ?: 0
                        cursor?.close()
                    } catch (e: Exception) {
                        return TaskExecutionResult(
                            outcome = ExecutionOutcome.Failed(e.message ?: "Failed to read calendar"),
                            status = "FAILED",
                            logMessage = "Calendar access exception: ${e.message}"
                        )
                    }

                    val evidence = ExecutionEvidence(
                        actionType = task.actionType,
                        toolProvider = "CalendarProvider",
                        startTime = System.currentTimeMillis(),
                        endTime = System.currentTimeMillis(),
                        requestId = UUID.randomUUID().toString(),
                        effectId = "SYNC_CALENDAR_EVENTS",
                        verifier = "LocalProvider",
                        evidenceHash = "CAL_${System.currentTimeMillis()}_$eventCount"
                    )
                    TaskExecutionResult(
                        outcome = ExecutionOutcome.Executed(evidence),
                        status = "EXECUTED",
                        logMessage = "Synchronized $eventCount calendar events with evidence hash: ${evidence.evidenceHash}"
                    )
                }
                "RULE_EVALUATION" -> {
                    if (!PolicyEnforcementPoint.enforceBackgroundExecution(context)) {
                        return TaskExecutionResult(
                            outcome = ExecutionOutcome.Blocked("Background execution denied by policy."),
                            status = "BLOCKED",
                            logMessage = "Background execution denied."
                        )
                    }
                    val request = OneTimeWorkRequestBuilder<RuleEvaluatorWorker>().build()
                    val workManager = WorkManager.getInstance(context)
                    workManager.enqueue(request)
                    
                    // Verify WorkManager status instead of returning early
                    var finalStatus = "ENQUEUED"
                    var workState = WorkInfo.State.ENQUEUED
                    withContext(Dispatchers.IO) {
                        var attempt = 0
                        while (attempt < 15) {
                            val info = workManager.getWorkInfoById(request.id).get()
                            workState = info?.state ?: WorkInfo.State.FAILED
                            if (workState.isFinished) {
                                break
                            }
                            kotlinx.coroutines.delay(1000)
                            attempt++
                        }
                    }
                    
                    finalStatus = if (workState == WorkInfo.State.SUCCEEDED) "EXECUTED" else "FAILED"

                    val evidence = ExecutionEvidence(
                        actionType = "RULE_EVALUATION",
                        toolProvider = "WorkManager (RuleEvaluatorWorker)",
                        startTime = System.currentTimeMillis(),
                        endTime = System.currentTimeMillis(),
                        requestId = request.id.toString(),
                        effectId = "WORKER_COMPLETED",
                        verifier = "WorkManager",
                        evidenceHash = "WM_${request.id}_$workState"
                    )
                    TaskExecutionResult(
                        outcome = if (finalStatus == "EXECUTED") ExecutionOutcome.Executed(evidence) else ExecutionOutcome.Failed("Worker ended with state $workState"),
                        status = finalStatus,
                        logMessage = "RuleEvaluatorWorker execution finished with status $finalStatus."
                    )
                }
                else -> {
                    val prefs = com.example.data.AgentPreferencesRepository(context).agentPreferencesFlow.first()
                    val isConfigured = if (prefs.aiProvider == "openrouter") {
                        prefs.openRouterApiKey.isNotBlank()
                    } else {
                        prefs.geminiApiKey.isNotBlank() || (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")
                    }

                    if (!isConfigured) {
                        TaskExecutionResult(
                            outcome = ExecutionOutcome.Blocked("Brak skonfigurowanego klucza API dla wybranego dostawcy AI."),
                            status = "BLOCKED",
                            logMessage = "AI API key is missing."
                        )
                    } else {
                        val prompt = """
                            You are ${agent.name}, an AI Agent with role: ${agent.role}.
                            You have been assigned a task: "${task.description}".
                            Please execute this task. Provide the actual output or result of this task based on your expertise. (2-3 sentences max).
                        """.trimIndent()

                        val reply = com.example.network.AILlmClient.generateContent(context, prompt)
                        val evidence = ExecutionEvidence(
                            actionType = "LLM_GENERATION",
                            toolProvider = "AILlmClient",
                            startTime = System.currentTimeMillis(),
                            endTime = System.currentTimeMillis(),
                            requestId = "LLM_${System.currentTimeMillis()}",
                            effectId = "TEXT_GENERATED",
                            verifier = "LLM_Engine",
                            evidenceHash = "LLM_${reply.hashCode()}"
                        )

                        TaskExecutionResult(
                            outcome = ExecutionOutcome.Executed(evidence),
                            status = "EXECUTED",
                            logMessage = "Executed Task Response: $reply"
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
