package com.example.execution

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Immutable audit trail for all task/action executions.
 *
 * Every execution outcome is persisted here for:
 * - Audit: Who did what, when, with what result
 * - Replay: Reconstruct execution history
 * - Verification: Prove execution actually happened
 * - Compliance: Evidence of system behavior
 */
@Entity(tableName = "execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey
    val outcomeId: String,

    val missionId: String?,      // Parent mission, if any
    val taskId: String?,          // Parent task, if any
    val executorId: String,       // Agent, worker, or user ID

    val actionType: String,       // CALENDAR_SYNC, RULE_EVALUATION, LLM_PROMPT
    val outcomeType: String,      // Executed, Simulated, Blocked, Failed, NeedsApproval

    val timestamp: Long,          // Unix milliseconds
    val durationMs: Long?,        // Only for Executed

    // Evidence tracking (for Executed outcomes)
    val evidenceType: String?,    // CALENDAR_EVENT_CREATED, FILE_WRITTEN, etc.
    val evidenceId: String?,      // External ID (event UID, file path hash)
    val verifier: String?,        // Tool that provided evidence (CalendarProvider, FileSystem)
    val metadata: String?,        // JSON metadata

    // Error tracking (for Failed outcomes)
    val errorCode: String?,
    val errorMessage: String?,
    val toolError: String?,       // Original error from provider
    val retryable: Boolean = false,

    // Block tracking (for Blocked outcomes)
    val blockType: String?,       // POLICY_VIOLATION, INSUFFICIENT_CAPABILITY
    val requestedCapability: String?,
    val reason: String?,

    // Simulated tracking
    val explanation: String?,     // LLM prose for Simulated
    val reasoning: String?,       // Why it was simulated

    // Approval tracking (for NeedsApproval)
    val approvalRequestId: String?,
    val requiredCapability: String?,
    val approvalExpiresAt: Long?,
    val approvalGrantedAt: Long?,
    val approvalGrantedBy: String?
)

@Dao
interface ExecutionLogDao {
    @Insert
    suspend fun insert(log: ExecutionLogEntity)

    @Query("SELECT * FROM execution_logs WHERE outcomeId = :outcomeId")
    suspend fun getByOutcomeId(outcomeId: String): ExecutionLogEntity?

    @Query("SELECT * FROM execution_logs WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getByTaskIdFlow(taskId: String): Flow<List<ExecutionLogEntity>>

    @Query("SELECT * FROM execution_logs WHERE missionId = :missionId ORDER BY timestamp DESC")
    fun getByMissionIdFlow(missionId: String): Flow<List<ExecutionLogEntity>>

    @Query("SELECT * FROM execution_logs WHERE executorId = :executorId ORDER BY timestamp DESC LIMIT :limit")
    fun getByExecutorIdFlow(executorId: String, limit: Int = 100): Flow<List<ExecutionLogEntity>>

    @Query("SELECT * FROM execution_logs WHERE outcomeType = :outcomeType ORDER BY timestamp DESC LIMIT :limit")
    fun getByOutcomeTypeFlow(outcomeType: String, limit: Int = 100): Flow<List<ExecutionLogEntity>>

    @Query("""SELECT * FROM execution_logs 
        WHERE timestamp >= :startMs AND timestamp <= :endMs 
        ORDER BY timestamp DESC""")
    fun getByTimestampRangeFlow(startMs: Long, endMs: Long): Flow<List<ExecutionLogEntity>>

    @Query("""SELECT COUNT(*) FROM execution_logs 
        WHERE outcomeType = 'Executed' AND executorId = :executorId""")
    fun countExecutedByExecutor(executorId: String): Flow<Long>

    @Query("""SELECT COUNT(*) FROM execution_logs 
        WHERE outcomeType = 'Failed' AND executorId = :executorId AND timestamp >= :sinceMs""")
    fun countFailuresSince(executorId: String, sinceMs: Long): Flow<Long>

    @Query("""SELECT COUNT(*) FROM execution_logs 
        WHERE outcomeType = 'Blocked' AND executorId = :executorId""")
    fun countBlockedByExecutor(executorId: String): Flow<Long>

    @Query("""SELECT COUNT(*) FROM execution_logs 
        WHERE outcomeType = 'Simulated' AND taskId = :taskId""")
    fun countSimulatedInTask(taskId: String): Flow<Long>
}
