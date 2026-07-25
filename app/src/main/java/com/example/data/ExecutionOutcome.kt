package com.example.data

data class ExecutionEvidence(
    val actionType: String,
    val toolProvider: String,
    val startTime: Long,
    val endTime: Long,
    val requestId: String,
    val effectId: String,
    val verifier: String,
    val evidenceHash: String
)

sealed interface ExecutionOutcome {
    data class Executed(val evidence: ExecutionEvidence) : ExecutionOutcome
    data class Simulated(val explanation: String) : ExecutionOutcome
    data class Blocked(val reason: String) : ExecutionOutcome
    data class Failed(val errorCode: String) : ExecutionOutcome
    data class NeedsApproval(val requestId: String) : ExecutionOutcome
}
