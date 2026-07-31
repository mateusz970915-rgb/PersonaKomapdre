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

    @Query("UPDATE agents SET statusNotes = :notes WHERE id = :id")
    suspend fun updateAgentStatusNotes(id: Int, notes: String)

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

    @Query("UPDATE data_access_requests SET approvalStatus = :status WHERE id = :id")
    suspend fun updateDataAccessApproval(id: Int, status: String)
    
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

    // Mission State Logs
    @Query("SELECT * FROM mission_state_logs ORDER BY timestamp DESC")
    fun getAllMissionStateLogs(): Flow<List<MissionStateLog>>

    @Query("SELECT * FROM mission_state_logs WHERE missionId = :missionId ORDER BY timestamp DESC")
    fun getMissionStateLogsForMission(missionId: Int): Flow<List<MissionStateLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissionStateLog(log: MissionStateLog)

    // Agent Negotiations
    @Query("SELECT * FROM agent_negotiations ORDER BY timestamp DESC")
    fun getAllAgentNegotiations(): Flow<List<AgentNegotiationProposal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentNegotiation(negotiation: AgentNegotiationProposal)

    @Query("UPDATE agent_negotiations SET status = :status, counterProposal = :counterProposal WHERE id = :id")
    suspend fun updateNegotiationStatus(id: Int, status: String, counterProposal: String)

    // Agent Mesh Telemetry
    @Query("SELECT * FROM agent_mesh_telemetry ORDER BY timestamp DESC")
    fun getAllMeshTelemetry(): Flow<List<AgentMeshTelemetry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeshTelemetry(telemetry: AgentMeshTelemetry)

    @Query("DELETE FROM agent_mesh_telemetry WHERE id NOT IN (SELECT id FROM agent_mesh_telemetry ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimOldTelemetry()

    // Agent Knowledge Edges
    @Query("SELECT * FROM agent_knowledge_edges ORDER BY timestamp DESC")
    fun getAllKnowledgeEdges(): Flow<List<AgentKnowledgeEdge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeEdge(edge: AgentKnowledgeEdge)

    @Query("DELETE FROM agent_knowledge_edges WHERE id = :id")
    suspend fun deleteKnowledgeEdge(id: Int)

    // Agent Heuristics (Evolution Engine)
    @Query("SELECT * FROM agent_heuristics ORDER BY generation DESC, confidenceScore DESC")
    fun getAllHeuristics(): Flow<List<AgentHeuristicRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeuristic(heuristic: AgentHeuristicRule)

    @Query("DELETE FROM agent_heuristics WHERE id = :id")
    suspend fun deleteHeuristic(id: Int)

    // LLM Call Telemetry
    @Query("SELECT * FROM llm_call_telemetry ORDER BY timestamp DESC")
    fun getAllLlmTelemetry(): Flow<List<LlmCallTelemetry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLlmTelemetry(telemetry: LlmCallTelemetry)

    @Query("DELETE FROM llm_call_telemetry")
    suspend fun clearLlmTelemetry()

    // Finance Transactions
    @Query("SELECT * FROM finance_transactions ORDER BY timestamp DESC")
    fun getAllFinanceTransactions(): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceTransaction(transaction: FinanceTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceTransactions(transactions: List<FinanceTransaction>)

    @Query("DELETE FROM finance_transactions")
    suspend fun clearFinanceTransactions()

    // Custom Agent Definitions
    @Query("SELECT * FROM custom_agent_definitions ORDER BY timestamp DESC")
    fun getAllCustomAgentDefinitions(): Flow<List<CustomAgentDefinition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAgentDefinition(definition: CustomAgentDefinition)

    @Query("DELETE FROM custom_agent_definitions WHERE id = :id")
    suspend fun deleteCustomAgentDefinition(id: Int)

    // Flashcards (Spaced Repetition)
    @Query("SELECT * FROM flashcards ORDER BY nextReviewTime ASC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcard(id: Int)

    // Subscriptions
    @Query("SELECT * FROM subscriptions ORDER BY amount DESC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptions(subscriptions: List<Subscription>)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscription(id: Int)

    @Query("UPDATE subscriptions SET isCancelled = :isCancelled WHERE id = :id")
    suspend fun updateSubscriptionCancelled(id: Int, isCancelled: Boolean)

    // Sleep Records
    @Query("SELECT * FROM sleep_records ORDER BY date DESC")
    fun getAllSleepRecords(): Flow<List<SleepRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepRecord(record: SleepRecord)

    @Query("DELETE FROM sleep_records")
    suspend fun clearSleepRecords()
}

