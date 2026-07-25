package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ColonyDao {
    @Query("SELECT * FROM agents")
    fun getAllAgents(): Flow<List<Agent>>

    @Query("SELECT * FROM agents WHERE id = :id")
    suspend fun getAgentById(id: Int): Agent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: Agent)

    @Query("DELETE FROM agents WHERE id = :id")
    suspend fun deleteAgentById(id: Int)
    
    @Query("SELECT COUNT(*) FROM agents")
    suspend fun getAgentCount(): Int

    @Query("SELECT * FROM council_messages ORDER BY timestamp ASC")
    fun getCouncilMessages(): Flow<List<CouncilMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CouncilMessage)

    @Query("DELETE FROM council_messages")
    suspend fun clearMessages()

    @Query("SELECT * FROM colony_memories ORDER BY timestamp DESC")
    fun getMemories(): Flow<List<ColonyMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: ColonyMemory)

    @Query("SELECT * FROM agent_decisions ORDER BY timestamp DESC")
    fun getAgentDecisions(): Flow<List<AgentDecision>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: AgentDecision)

    @Query("SELECT * FROM missions ORDER BY timestamp DESC")
    fun getMissions(): Flow<List<Mission>>

    @Query("DELETE FROM missions WHERE id = :id")
    suspend fun deleteMission(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: Mission): Long

    @Query("SELECT * FROM sub_tasks WHERE missionId = :missionId ORDER BY timestamp ASC")
    fun getSubTasksForMission(missionId: Int): Flow<List<SubTask>>

    @Query("SELECT * FROM sub_tasks ORDER BY timestamp DESC")
    fun getAllSubTasks(): Flow<List<SubTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask)
    
    @Query("UPDATE agents SET status = :status WHERE id = :id")
    suspend fun updateAgentStatus(id: Int, status: String)

    @Query("UPDATE agents SET permissions = :permissions, systemPrompt = :systemPrompt WHERE id = :id")
    suspend fun updateAgentConfig(id: Int, permissions: String, systemPrompt: String)

    @Query("UPDATE sub_tasks SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateSubTaskStatusWithCompletion(id: Int, status: String, completedAt: Long)

    @Query("SELECT * FROM data_access_requests ORDER BY timestamp DESC")
    fun getDataAccessRequests(): Flow<List<DataAccessRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataAccessRequest(request: DataAccessRequest)
    
    @Query("UPDATE sub_tasks SET status = 'Halted' WHERE status != 'Completed'")
    suspend fun haltAllSubTasks()
    
    @Query("UPDATE missions SET status = 'Halted' WHERE status != 'Completed'")
    suspend fun haltAllMissions()

    @Query("UPDATE agents SET status = 'Halted'")
    suspend fun haltAllAgents()

    @androidx.room.Transaction
    suspend fun haltAllSystems() {
        haltAllAgents()
        haltAllMissions()
        haltAllSubTasks()
    }

    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun getAllCalendarEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteCalendarEventById(id: Int)

    @Query("DELETE FROM calendar_events")
    suspend fun clearCalendarEvents()

    @Query("SELECT * FROM badges ORDER BY isUnlocked DESC, id ASC")
    fun getAllBadges(): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<Badge>)

    @Query("SELECT COUNT(*) FROM badges")
    suspend fun getBadgeCount(): Int

    @Query("SELECT * FROM inter_agent_messages ORDER BY timestamp ASC")
    fun getAllInterAgentMessages(): Flow<List<InterAgentMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterAgentMessage(message: InterAgentMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterAgentMessages(messages: List<InterAgentMessage>)

    @Query("SELECT COUNT(*) FROM inter_agent_messages")
    suspend fun getInterAgentMessageCount(): Int

    @Query("SELECT * FROM agent_milestones ORDER BY requiredXp ASC")
    fun getAllAgentMilestones(): Flow<List<AgentMilestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentMilestone(milestone: AgentMilestone)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentMilestones(milestones: List<AgentMilestone>)

    @Query("SELECT COUNT(*) FROM agent_milestones")
    suspend fun getAgentMilestoneCount(): Int

    // Rule Nodes
    @Query("SELECT * FROM rule_nodes")
    fun getAllRuleNodes(): Flow<List<RuleNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleNode(node: RuleNodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleNodes(nodes: List<RuleNodeEntity>)

    @Query("DELETE FROM rule_nodes WHERE id = :id")
    suspend fun deleteRuleNodeById(id: String)

    @Query("SELECT COUNT(*) FROM rule_nodes")
    suspend fun getRuleNodeCount(): Int

    // Rule Connections
    @Query("SELECT * FROM rule_connections")
    fun getAllRuleConnections(): Flow<List<RuleConnectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleConnection(connection: RuleConnectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleConnections(connections: List<RuleConnectionEntity>)

    @Query("DELETE FROM rule_connections WHERE fromId = :fromId AND toId = :toId")
    suspend fun deleteRuleConnection(fromId: String, toId: String)

    @Query("DELETE FROM rule_connections WHERE fromId = :nodeId OR toId = :nodeId")
    suspend fun deleteConnectionsForNode(nodeId: String)

    @Query("SELECT COUNT(*) FROM rule_connections")
    suspend fun getRuleConnectionCount(): Int

    @Query("SELECT * FROM sub_tasks WHERE status IN ('Completed', 'EXECUTED', 'SIMULATED')")
    fun getCompletedSubTasks(): Flow<List<SubTask>>
}
