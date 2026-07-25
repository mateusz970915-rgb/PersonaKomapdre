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
    val performanceScore: Float = 1.0f
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
    val completedAt: Long = 0L
)

@Entity(tableName = "data_access_requests")
data class DataAccessRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentName: String,
    val dataType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPolicyViolation: Boolean = false,
    val violationReason: String? = null
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

