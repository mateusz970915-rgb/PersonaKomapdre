package com.example.execution

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Repository for storing and querying execution outcomes.
 *
 * Bridges between ExecutionOutcome types and Room persistence.
 * Handles serialization of outcome data to/from database.
 */
class ExecutionRepository(private val executionLogDao: ExecutionLogDao) {
    /**
     * Record an execution outcome in the audit trail.
     */
    suspend fun logOutcome(outcome: ExecutionOutcome) {
        val entity = serializeOutcome(outcome)
        executionLogDao.insert(entity)
    }

    /**
     * Get all execution logs for a task.
     */
    fun getTaskExecutionHistory(taskId: String): Flow<List<ExecutionOutcome>> =
        executionLogDao.getByTaskIdFlow(taskId).map { entities ->
            entities.mapNotNull { deserializeOutcome(it) }
        }

    /**
     * Get all execution logs for a mission.
     */
    fun getMissionExecutionHistory(missionId: String): Flow<List<ExecutionOutcome>> =
        executionLogDao.getByMissionIdFlow(missionId).map { entities ->
            entities.mapNotNull { deserializeOutcome(it) }
        }

    /**
     * Count how many times an executor has successfully executed actions.
     */
    fun countSuccessfulExecutions(executorId: String): Flow<Long> =
        executionLogDao.countExecutedByExecutor(executorId)

    /**
     * Count failures in the last N ms.
     */
    fun countRecentFailures(executorId: String, windowMs: Long): Flow<Long> =
        executionLogDao.countFailuresSince(executorId, System.currentTimeMillis() - windowMs)

    /**
     * Count blocked executions.
     */
    fun countBlockedExecutions(executorId: String): Flow<Long> =
        executionLogDao.countBlockedByExecutor(executorId)

    /**
     * Count simulated (non-executed) actions in a task.
     */
    fun countSimulatedInTask(taskId: String): Flow<Long> =
        executionLogDao.countSimulatedInTask(taskId)

    private fun serializeOutcome(outcome: ExecutionOutcome): ExecutionLogEntity {
        return when (outcome) {
            is ExecutionOutcome.Executed -> ExecutionLogEntity(
                outcomeId = outcome.id,
                executorId = outcome.executorId,
                actionType = outcome.actionType,
                outcomeType = "Executed",
                timestamp = outcome.timestamp.toEpochMilli(),
                durationMs = outcome.durationMs,
                evidenceType = outcome.evidenceType,
                evidenceId = outcome.evidenceId,
                verifier = outcome.verifier,
                metadata = outcome.metadata.let {
                    if (it.isEmpty()) null else kotlinx.serialization.json.Json.encodeToString(it)
                },
                taskId = null,
                missionId = null,
                errorCode = null,
                errorMessage = null,
                toolError = null,
                blockType = null,
                requestedCapability = null,
                reason = null,
                explanation = null,
                reasoning = null,
                approvalRequestId = null,
                requiredCapability = null,
                approvalExpiresAt = null,
                approvalGrantedAt = null,
                approvalGrantedBy = null
            )

            is ExecutionOutcome.Simulated -> ExecutionLogEntity(
                outcomeId = outcome.id,
                executorId = outcome.executorId,
                actionType = outcome.actionType,
                outcomeType = "Simulated",
                timestamp = outcome.timestamp.toEpochMilli(),
                explanation = outcome.explanation,
                reasoning = outcome.reasoning,
                taskId = null,
                missionId = null,
                durationMs = null,
                evidenceType = null,
                evidenceId = null,
                verifier = null,
                metadata = null,
                errorCode = null,
                errorMessage = null,
                toolError = null,
                blockType = null,
                requestedCapability = null,
                reason = null,
                approvalRequestId = null,
                requiredCapability = null,
                approvalExpiresAt = null,
                approvalGrantedAt = null,
                approvalGrantedBy = null
            )

            is ExecutionOutcome.Blocked -> ExecutionLogEntity(
                outcomeId = outcome.id,
                executorId = outcome.executorId,
                actionType = outcome.actionType,
                outcomeType = "Blocked",
                timestamp = outcome.timestamp.toEpochMilli(),
                blockType = outcome.blockType,
                reason = outcome.reason,
                requestedCapability = outcome.requestedCapability,
                taskId = null,
                missionId = null,
                durationMs = null,
                evidenceType = null,
                evidenceId = null,
                verifier = null,
                metadata = null,
                errorCode = null,
                errorMessage = null,
                toolError = null,
                explanation = null,
                reasoning = null,
                approvalRequestId = null,
                requiredCapability = null,
                approvalExpiresAt = null,
                approvalGrantedAt = null,
                approvalGrantedBy = null
            )

            is ExecutionOutcome.Failed -> ExecutionLogEntity(
                outcomeId = outcome.id,
                executorId = outcome.executorId,
                actionType = outcome.actionType,
                outcomeType = "Failed",
                timestamp = outcome.timestamp.toEpochMilli(),
                errorCode = outcome.errorCode,
                errorMessage = outcome.errorMessage,
                toolError = outcome.toolError,
                retryable = outcome.retryable,
                taskId = null,
                missionId = null,
                durationMs = null,
                evidenceType = null,
                evidenceId = null,
                verifier = null,
                metadata = null,
                blockType = null,
                requestedCapability = null,
                reason = null,
                explanation = null,
                reasoning = null,
                approvalRequestId = null,
                requiredCapability = null,
                approvalExpiresAt = null,
                approvalGrantedAt = null,
                approvalGrantedBy = null
            )

            is ExecutionOutcome.NeedsApproval -> ExecutionLogEntity(
                outcomeId = outcome.id,
                executorId = outcome.executorId,
                actionType = outcome.actionType,
                outcomeType = "NeedsApproval",
                timestamp = outcome.timestamp.toEpochMilli(),
                approvalRequestId = outcome.requestId,
                requiredCapability = outcome.requiredCapability,
                approvalExpiresAt = outcome.expiresAt?.toEpochMilli(),
                taskId = null,
                missionId = null,
                durationMs = null,
                evidenceType = null,
                evidenceId = null,
                verifier = null,
                metadata = null,
                errorCode = null,
                errorMessage = null,
                toolError = null,
                blockType = null,
                requestedCapability = null,
                reason = null,
                explanation = null,
                reasoning = null,
                approvalGrantedAt = null,
                approvalGrantedBy = null
            )
        }
    }

    private fun deserializeOutcome(entity: ExecutionLogEntity): ExecutionOutcome? {
        val timestamp = Instant.ofEpochMilli(entity.timestamp)
        return when (entity.outcomeType) {
            "Executed" -> if (entity.evidenceId != null && entity.verifier != null) {
                ExecutionOutcome.Executed(
                    id = entity.outcomeId,
                    timestamp = timestamp,
                    executorId = entity.executorId,
                    actionType = entity.actionType,
                    evidenceType = entity.evidenceType ?: "",
                    evidenceId = entity.evidenceId,
                    verifier = entity.verifier,
                    durationMs = entity.durationMs ?: 0,
                    metadata = entity.metadata?.let { kotlinx.serialization.json.Json.decodeFromString(it) } ?: emptyMap()
                )
            } else null

            "Simulated" -> ExecutionOutcome.Simulated(
                id = entity.outcomeId,
                timestamp = timestamp,
                executorId = entity.executorId,
                actionType = entity.actionType,
                explanation = entity.explanation ?: "",
                reasoning = entity.reasoning ?: ""
            )

            "Blocked" -> ExecutionOutcome.Blocked(
                id = entity.outcomeId,
                timestamp = timestamp,
                executorId = entity.executorId,
                actionType = entity.actionType,
                reason = entity.reason ?: "",
                blockType = entity.blockType ?: "",
                requestedCapability = entity.requestedCapability
            )

            "Failed" -> ExecutionOutcome.Failed(
                id = entity.outcomeId,
                timestamp = timestamp,
                executorId = entity.executorId,
                actionType = entity.actionType,
                errorCode = entity.errorCode ?: "",
                errorMessage = entity.errorMessage ?: "",
                toolError = entity.toolError,
                retryable = entity.retryable
            )

            "NeedsApproval" -> ExecutionOutcome.NeedsApproval(
                id = entity.outcomeId,
                timestamp = timestamp,
                executorId = entity.executorId,
                actionType = entity.actionType,
                requestId = entity.approvalRequestId ?: "",
                description = "",
                requiredCapability = entity.requiredCapability ?: "",
                expiresAt = entity.approvalExpiresAt?.let { Instant.ofEpochMilli(it) }
            )

            else -> null
        }
    }
}
