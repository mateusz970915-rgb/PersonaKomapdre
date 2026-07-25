package com.example.execution

import java.time.Instant
import java.util.UUID

/**
 * Sealed interface representing the outcome of a task/action execution.
 *
 * PRINCIPLE: Distinguishes between real system effects and model-generated descriptions.
 * - Executed: System state changed, evidence exists
 * - Simulated: Model reasoning without real effect
 * - Blocked: Prevented by policy or capability
 * - Failed: Attempted but encountered error
 * - NeedsApproval: Awaiting authorization
 *
 * NEVER create Executed status from LLM prose alone.
 * ALWAYS require tool/system callback with external evidence.
 */
seal class ExecutionOutcome {
    abstract val id: String
    abstract val timestamp: Instant
    abstract val executorId: String  // task ID, agent ID, or worker ID
    abstract val actionType: String  // CALENDAR_SYNC, RULE_EVALUATION, LLM_PROMPT, etc.

    /**
     * Represents a real execution with verifiable evidence.
     *
     * @param evidenceType: CALENDAR_EVENT_CREATED, FILE_WRITTEN, NETWORK_REQUEST, etc.
     * @param evidenceId: External ID (event UID, file path hash, request ID)
     * @param verifier: Tool or provider that verified (CalendarProvider, FileSystem, API)
     * @param durationMs: Time from start to completion
     * @param metadata: Tool-specific metadata (event start time, bytes written, HTTP status)
     */
    data class Executed(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Instant = Instant.now(),
        override val executorId: String,
        override val actionType: String,
        val evidenceType: String,
        val evidenceId: String,
        val verifier: String,  // e.g., "CalendarProvider", "FileSystem", "Retrofit"
        val durationMs: Long,
        val metadata: Map<String, String> = emptyMap()
    ) : ExecutionOutcome()

    /**
     * Represents a model-generated description without real system effect.
     *
     * Used when LLM describes what it would do, what it thinks happened,
     * or generates synthetic output for analysis.
     *
     * @param explanation: LLM prose describing the simulated action
     * @param reasoning: Why this was simulated (user requested, policy forbids, etc.)
     */
    data class Simulated(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Instant = Instant.now(),
        override val executorId: String,
        override val actionType: String,
        val explanation: String,
        val reasoning: String
    ) : ExecutionOutcome()

    /**
     * Represents an execution that was prevented by policy or capability.
     *
     * @param reason: Human-readable block reason
     * @param blockType: POLICY_VIOLATION, INSUFFICIENT_CAPABILITY, PERMISSION_DENIED, etc.
     * @param requestedCapability: Capability that was missing
     */
    data class Blocked(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Instant = Instant.now(),
        override val executorId: String,
        override val actionType: String,
        val reason: String,
        val blockType: String,  // POLICY_VIOLATION, INSUFFICIENT_CAPABILITY, PERMISSION_DENIED
        val requestedCapability: String? = null
    ) : ExecutionOutcome()

    /**
     * Represents an execution attempt that failed.
     *
     * @param errorCode: Machine-readable error (NETWORK_ERROR, PARSE_ERROR, TIMEOUT, etc.)
     * @param errorMessage: Human-readable error description
     * @param toolError: Original error from underlying tool/provider
     * @param retryable: Whether this error allows retry
     */
    data class Failed(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Instant = Instant.now(),
        override val executorId: String,
        override val actionType: String,
        val errorCode: String,
        val errorMessage: String,
        val toolError: String? = null,
        val retryable: Boolean = false
    ) : ExecutionOutcome()

    /**
     * Represents an execution awaiting user authorization.
     *
     * @param requestId: Approval request ID for tracking
     * @param description: What action needs approval
     * @param requiredCapability: Capability level required
     * @param expiresAt: When approval expires (null = never)
     */
    data class NeedsApproval(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Instant = Instant.now(),
        override val executorId: String,
        override val actionType: String,
        val requestId: String,
        val description: String,
        val requiredCapability: String,
        val expiresAt: Instant? = null
    ) : ExecutionOutcome()

    companion object {
        /**
         * Check if outcome represents actual system change.
         */
        fun isExecuted(outcome: ExecutionOutcome): Boolean = outcome is Executed

        /**
         * Check if outcome is terminal (no further action possible).
         */
        fun isTerminal(outcome: ExecutionOutcome): Boolean =
            outcome is Executed || outcome is Failed || outcome is Blocked

        /**
         * Check if outcome is transient (requires future state update).
         */
        fun isTransient(outcome: ExecutionOutcome): Boolean =
            outcome is NeedsApproval || outcome is Simulated
    }
}
