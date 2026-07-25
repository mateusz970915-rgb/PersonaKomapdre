package com.example.execution

/**
 * Verifier interface: Proves execution actually happened in external system.
 *
 * Implementations:
 * - CalendarVerifier: Reads CalendarProvider to confirm event exists
 * - FileVerifier: Checks filesystem for written files
 * - NetworkVerifier: Confirms HTTP request was sent (via logs, not just response)
 * - PolicyVerifier: Confirms policy engine evaluated rules
 *
 * PRINCIPLE: Never trust LLM prose as evidence. Always verify against system state.
 */
interface ExecutionVerifier {
    /**
     * Verify that an action produced the claimed effect in the external system.
     *
     * @param evidenceType: What kind of effect (CALENDAR_EVENT_CREATED, FILE_WRITTEN)
     * @param evidenceId: External ID to look up (event UID, file path hash)
     * @return true if evidence found and matches, false otherwise
     * @throws VerificationException if unable to verify (e.g., missing permission)
     */
    suspend fun verify(
        evidenceType: String,
        evidenceId: String,
        metadata: Map<String, String> = emptyMap()
    ): Boolean

    /**
     * Get human-readable name of this verifier.
     */
    fun getName(): String

    /**
     * Check if this verifier can handle the given evidence type.
     */
    fun canVerify(evidenceType: String): Boolean
}

class VerificationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * No-op verifier for when verification is not possible.
 * Used for local-only actions or development/testing.
 *
 * IMPORTANT: Never use in production for sensitive actions.
 */
class NoOpVerifier : ExecutionVerifier {
    override suspend fun verify(
        evidenceType: String,
        evidenceId: String,
        metadata: Map<String, String>
    ): Boolean {
        // Log that we're not verifying
        println("[NoOpVerifier] Skipping verification for $evidenceType:$evidenceId")
        return true  // Assume success
    }

    override fun getName(): String = "NoOpVerifier"
    override fun canVerify(evidenceType: String): Boolean = false
}
