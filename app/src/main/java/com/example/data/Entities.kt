package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "agents")
data class Agent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val role: String,
    val status: String = "Active",
    val permissions: String = "Basic",
    val autonomyLevel: String = "Needs Confirmation",
    val iconName: String = "default",
    val traits: String = "",
    val systemPrompt: String = "",
    val performanceScore: Float = 1.0f,
    val statusNotes: String = "",
    val personaDescription: String = "",
    val lastActiveTimestamp: Long = 0L,
    val avatarUrl: String = "",
    val configurationJson: String = "{}",
    val isFavorite: Boolean = false,
    val category: String = "General",
    val failoverAgentId: Int? = null,
    val maxLatencyThresholdMs: Long = 5000L
)

@Entity(tableName = "council_messages")
data class CouncilMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user", "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "colony_memories")
data class ColonyMemory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "agent_decisions")
data class AgentDecision(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentName: String,
    val actionDescription: String,
    val dataUsed: String,
    val confidenceLevel: String,
    val dissentingOpinions: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "missions")
data class Mission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goal: String,
    val status: String = "Active", // Active, Completed, Failed
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sub_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Mission::class,
            parentColumns = ["id"],
            childColumns = ["missionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["missionId"]),
        Index(value = ["assignedAgent"])
    ]
)
data class SubTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val missionId: Int? = null,
    val assignedAgent: String,
    val description: String,
    val actionType: String = "LLM_PROMPT",
    val status: String = "Pending", // Pending, In Progress, Completed
    val timestamp: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L,
    val priority: String = "Medium", // High, Medium, Low
    val progress: Int = 0 // 0 to 100
)

@Entity(tableName = "data_access_requests")
data class DataAccessRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentName: String,
    val dataType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPolicyViolation: Boolean = false,
    val violationReason: String? = null,
    val requiresUserApproval: Boolean = false,
    val approvalStatus: String = "Approved" // Pending, Approved, Denied
)

@Serializable
@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val agentName: String
)

@Serializable
@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // "Productivity", "Workflow", "Colony Growth", "Governance"
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1
)

@Serializable
@Entity(tableName = "inter_agent_messages")
data class InterAgentMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderAgentName: String,
    val senderRole: String = "Agent",
    val targetAgentName: String? = null, // null means central colony feed
    val content: String,
    val topic: String = "General Colony",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "agent_milestones")
data class AgentMilestone(
    @PrimaryKey val id: String,
    val agentName: String,
    val role: String,
    val type: String,
    val requiredXp: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val iconName: String = "smart_toy",
    val systemPrompt: String = "",
    val performanceScore: Float = 1.0f
)

@Serializable
@Entity(tableName = "rule_nodes")
data class RuleNodeEntity(
    @PrimaryKey val id: String,
    val posX: Float,
    val posY: Float,
    val nodeType: String,
    val text: String
)

@Serializable
@Entity(tableName = "rule_connections")
data class RuleConnectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromId: String,
    val toId: String
)

@Serializable
@Entity(
    tableName = "mission_state_logs",
    foreignKeys = [
        ForeignKey(
            entity = Mission::class,
            parentColumns = ["id"],
            childColumns = ["missionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["missionId"]),
        Index(value = ["agentName"])
    ]
)
data class MissionStateLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val missionId: Int,
    val agentName: String,
    val previousState: String,
    val newState: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "agent_negotiations")
data class AgentNegotiationProposal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val missionId: Int = 0,
    val proposerAgent: String,
    val targetAgent: String,
    val proposedAction: String,
    val counterProposal: String = "",
    val status: String = "Pending", // Pending, Accepted, Rejected, Escalated, Countered
    val conflictTopic: String = "Resource Allocation",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "agent_mesh_telemetry")
data class AgentMeshTelemetry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentId: Int,
    val agentName: String,
    val latencyMs: Long,
    val cpuLoadPct: Float,
    val memoryUsageMb: Float,
    val activeConnectionsCount: Int,
    val healthStatus: String = "Optimal", // Optimal, Warning, Degraded, Syncing
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "agent_knowledge_edges")
data class AgentKnowledgeEdge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceLabel: String,
    val sourceType: String = "AGENT", // AGENT, MISSION, MEMORY, DECISION
    val targetLabel: String,
    val targetType: String = "CONCEPT", // CONCEPT, MISSION, AGENT, RULE
    val relationType: String = "DEPENDS_ON", // DEPENDS_ON, CONFLICTS_WITH, CAUSES, IMPLEMENTS, ENFORCES
    val weight: Float = 1.0f,
    val creatorAgent: String = "System",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "agent_heuristics")
data class AgentHeuristicRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentName: String,
    val heuristicKey: String, // DELEGATION_PRIORITY, RISK_THRESHOLD, REASONING_DEPTH, EXECUTION_SPEED
    val patternTarget: String, // HIGH_CONCURRENCY_TASKS, SECURITY_AUDIT, RESOURCE_OPT, GENERAL
    val confidenceScore: Float = 0.8f,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val adaptedPolicy: String,
    val generation: Int = 1,
    val lastEvolvedTimestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "llm_call_telemetry")
data class LlmCallTelemetry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val provider: String,
    val model: String,
    val promptLength: Int,
    val responseLength: Int,
    val durationMs: Long,
    val status: String, // "SUCCESS", "FAILED"
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String, // "Food", "Rent", "Utilities", "Salary", "Entertainment", "Other"
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "custom_agent_definitions")
data class CustomAgentDefinition(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val systemPrompt: String,
    val temperature: Double,
    val toolsAccess: String, // Comma-separated list or JSON
    val autonomyLevel: String = "Medium",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val interval: Int = 1, // in days
    val repetition: Int = 0, // consecutive successful repetitions
    val easinessFactor: Float = 2.5f,
    val nextReviewTime: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val frequency: String = "Monthly",
    val isCancelled: Boolean = false,
    val nextBillingDate: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000
)

@Serializable
@Entity(
    tableName = "agent_sentiment_logs",
    indices = [Index(value = ["agentName"])]
)
data class AgentSentimentLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentName: String,
    val emoji: String,
    val moodTitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val sleepDurationHours: Float,
    val deepSleepMinutes: Int,
    val remSleepMinutes: Int,
    val lightSleepMinutes: Int,
    val recoveryScore: Int,
    val heartRateAvg: Int,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "chart_annotations")
data class ChartAnnotation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String,
    val tag: String = "General",
    val agentId: Int? = null,
    val colorHex: String = "#3B82F6"
)

@Serializable
@Entity(tableName = "agent_interaction_fts_content")
data class AgentInteractionFtsContent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val snippet: String,
    val modelUsed: String = "gemini-3.5-flash",
    val tag: String = ""
)

@Serializable
@Entity(tableName = "self_healing_proposals")
data class SelfHealingProposal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val missionId: Int = 0,
    val errorLogSnippet: String,
    val rootCauseAnalysis: String,
    val proposedCodeFix: String,
    val architectAgentName: String = "Architekt Systemowy",
    val status: String = "Pending", // Pending, Applied, Rejected
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "workflow_dags")
data class WorkflowDag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val nodesJson: String, // List<DagNode> as JSON
    val edgesJson: String, // List<DagEdge> as JSON
    val colonyId: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "colony_profiles")
data class ColonyProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Zawodowa", "Prywatna", "R&D", "Edukacyjna"
    val description: String,
    val iconName: String = "group_work",
    val isCurrentActive: Boolean = false,
    val agentCount: Int = 5,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val metadataJson: String = "{}"
)

@Serializable
@Entity(tableName = "vector_embedding_logs")
data class VectorEmbeddingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceText: String,
    val embeddingVectorJson: String, // JSON float array representation
    val tag: String = "General",
    val agentName: String = "System",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "hallucination_audit_logs")
data class HallucinationAuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val promptText: String,
    val responseText: String,
    val factCheckScore: Float, // 0.0 to 1.0
    val verdict: String, // "Verified Fact", "Plausible", "Potential Hallucination"
    val checkedClaimsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)







@Entity(tableName = "user_agent_messages")
data class UserAgentMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentName: String,
    val role: String, // "user", "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
