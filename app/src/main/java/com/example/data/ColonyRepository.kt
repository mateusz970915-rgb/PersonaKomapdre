package com.example.data

import kotlinx.coroutines.flow.Flow

class ColonyRepository(val colonyDao: ColonyDao) {
    val allPersonaAgents: Flow<List<PersonaAgent>> = colonyDao.getAllPersonaAgents()
    suspend fun insertPersonaAgent(agent: PersonaAgent) = safeDbCall { colonyDao.insertPersonaAgent(agent) }
    suspend fun deletePersonaAgentById(id: Int) = safeDbCall { colonyDao.deletePersonaAgentById(id) }

    val allAgents: Flow<List<Agent>> = colonyDao.getAllAgents()
    val councilMessages: Flow<List<CouncilMessage>> = colonyDao.getCouncilMessages()
    val allMemories: Flow<List<ColonyMemory>> = colonyDao.getMemories()
    val allDecisions: Flow<List<AgentDecision>> = colonyDao.getAgentDecisions()
    val allMissions: Flow<List<Mission>> = colonyDao.getMissions()
    val allDataAccessRequests: Flow<List<DataAccessRequest>> = colonyDao.getDataAccessRequests()
    val allSubTasks: Flow<List<SubTask>> = colonyDao.getAllSubTasks()

    suspend fun insertAgent(agent: Agent) = safeDbCall { colonyDao.insertAgent(agent) }
    suspend fun updateAgentStatusNotes(id: Int, notes: String) = safeDbCall { colonyDao.updateAgentStatusNotes(id, notes) }
    suspend fun deleteAgentById(id: Int) = safeDbCall { colonyDao.deleteAgentById(id) }
    suspend fun getAgentCount(): Int = safeDbCall { colonyDao.getAgentCount() }

    suspend fun insertMessage(message: CouncilMessage) = safeDbCall { colonyDao.insertMessage(message) }
    suspend fun clearMessages() = safeDbCall { colonyDao.clearMessages() }

    suspend fun insertMemory(memory: ColonyMemory) = safeDbCall { colonyDao.insertMemory(memory) }
    
    suspend fun insertDecision(decision: AgentDecision) = safeDbCall { colonyDao.insertDecision(decision) }
    suspend fun insertMission(mission: Mission): Long = safeDbCall { colonyDao.insertMission(mission) }
    fun getSubTasksForMission(missionId: Int): Flow<List<SubTask>> = colonyDao.getSubTasksForMission(missionId)
    suspend fun insertSubTask(subTask: SubTask) = safeDbCall { colonyDao.insertSubTask(subTask) }
    
    suspend fun deleteSubTaskById(id: Int) = safeDbCall { colonyDao.deleteSubTaskById(id) }
    suspend fun deleteSubTasksByIds(ids: List<Int>) = safeDbCall { colonyDao.deleteSubTasksByIds(ids) }
    
    suspend fun insertSubTasks(subTasks: List<SubTask>) {
        subTasks.forEach { colonyDao.insertSubTask(it) }
    }
    suspend fun updateSubTaskStatus(id: Int, status: String) {
        val completedAt = if (status == "Completed" || status == "EXECUTED") System.currentTimeMillis() else 0L
        colonyDao.updateSubTaskStatusWithCompletion(id, status, completedAt)
    }
    
    suspend fun insertDataAccessRequest(request: DataAccessRequest) = safeDbCall { colonyDao.insertDataAccessRequest(request) }
    suspend fun updateDataAccessApproval(id: Int, status: String) = safeDbCall { colonyDao.updateDataAccessApproval(id, status) }
    
    suspend fun haltSystem() {
        colonyDao.haltAllSystems()
    }

    val allCalendarEvents: Flow<List<CalendarEvent>> = colonyDao.getAllCalendarEvents()
    suspend fun insertCalendarEvent(event: CalendarEvent) = safeDbCall { colonyDao.insertCalendarEvent(event) }
    suspend fun deleteCalendarEventById(id: Int) = safeDbCall { colonyDao.deleteCalendarEventById(id) }
    suspend fun clearCalendarEvents() = safeDbCall { colonyDao.clearCalendarEvents() }

    val allBadges: Flow<List<Badge>> = colonyDao.getAllBadges()
    suspend fun insertBadge(badge: Badge) = safeDbCall { colonyDao.insertBadge(badge) }
    suspend fun insertBadges(badges: List<Badge>) = safeDbCall { colonyDao.insertBadges(badges) }
    suspend fun getBadgeCount(): Int = safeDbCall { colonyDao.getBadgeCount() }

    val allInterAgentMessages: Flow<List<InterAgentMessage>> = colonyDao.getAllInterAgentMessages()
    suspend fun insertInterAgentMessage(message: InterAgentMessage) = safeDbCall { colonyDao.insertInterAgentMessage(message) }
    suspend fun insertInterAgentMessages(messages: List<InterAgentMessage>) = safeDbCall { colonyDao.insertInterAgentMessages(messages) }
    suspend fun getInterAgentMessageCount(): Int = safeDbCall { colonyDao.getInterAgentMessageCount() }

    val allAgentMilestones: Flow<List<AgentMilestone>> = colonyDao.getAllAgentMilestones()
    suspend fun insertAgentMilestone(milestone: AgentMilestone) = safeDbCall { colonyDao.insertAgentMilestone(milestone) }
    suspend fun insertAgentMilestones(milestones: List<AgentMilestone>) = safeDbCall { colonyDao.insertAgentMilestones(milestones) }
    suspend fun getAgentMilestoneCount(): Int = safeDbCall { colonyDao.getAgentMilestoneCount() }

    // Rule Nodes & Connections
    val allRuleNodes: Flow<List<RuleNodeEntity>> = colonyDao.getAllRuleNodes()
    val allRuleConnections: Flow<List<RuleConnectionEntity>> = colonyDao.getAllRuleConnections()
    val completedSubTasks: Flow<List<SubTask>> = colonyDao.getCompletedSubTasks()

    suspend fun insertRuleNode(node: RuleNodeEntity) = safeDbCall { colonyDao.insertRuleNode(node) }
    suspend fun insertRuleNodes(nodes: List<RuleNodeEntity>) = safeDbCall { colonyDao.insertRuleNodes(nodes) }
    suspend fun deleteRuleNodeById(id: String) {
        colonyDao.deleteRuleNodeById(id)
        colonyDao.deleteConnectionsForNode(id)
    }
    suspend fun getRuleNodeCount(): Int = safeDbCall { colonyDao.getRuleNodeCount() }

    suspend fun insertRuleConnection(connection: RuleConnectionEntity) = safeDbCall { colonyDao.insertRuleConnection(connection) }
    suspend fun insertRuleConnections(connections: List<RuleConnectionEntity>) = safeDbCall { colonyDao.insertRuleConnections(connections) }
    suspend fun deleteRuleConnection(fromId: String, toId: String) = safeDbCall { colonyDao.deleteRuleConnection(fromId, toId) }
    suspend fun getRuleConnectionCount(): Int = safeDbCall { colonyDao.getRuleConnectionCount() }

    // Mission State Logs
    val allMissionStateLogs: Flow<List<MissionStateLog>> = colonyDao.getAllMissionStateLogs()
    fun getMissionStateLogsForMission(missionId: Int): Flow<List<MissionStateLog>> = colonyDao.getMissionStateLogsForMission(missionId)
    suspend fun insertMissionStateLog(log: MissionStateLog) = safeDbCall { colonyDao.insertMissionStateLog(log) }

    // Agent Negotiations
    val allAgentNegotiations: Flow<List<AgentNegotiationProposal>> = colonyDao.getAllAgentNegotiations()
    suspend fun insertAgentNegotiation(negotiation: AgentNegotiationProposal) = safeDbCall { colonyDao.insertAgentNegotiation(negotiation) }
    suspend fun updateNegotiationStatus(id: Int, status: String, counterProposal: String = "") = safeDbCall { colonyDao.updateNegotiationStatus(id, status, counterProposal) }

    // Agent Mesh Telemetry
    val allMeshTelemetry: Flow<List<AgentMeshTelemetry>> = colonyDao.getAllMeshTelemetry()
    suspend fun insertMeshTelemetry(telemetry: AgentMeshTelemetry) {
        colonyDao.insertMeshTelemetry(telemetry)
        colonyDao.trimOldTelemetry()
    }

    // Agent Knowledge Edges
    val allKnowledgeEdges: Flow<List<AgentKnowledgeEdge>> = colonyDao.getAllKnowledgeEdges()
    suspend fun insertKnowledgeEdge(edge: AgentKnowledgeEdge) = safeDbCall { colonyDao.insertKnowledgeEdge(edge) }
    suspend fun deleteKnowledgeEdge(id: Int) = safeDbCall { colonyDao.deleteKnowledgeEdge(id) }

    // Agent Heuristics (Evolution Engine)
    val allHeuristics: Flow<List<AgentHeuristicRule>> = colonyDao.getAllHeuristics()
    suspend fun insertHeuristic(heuristic: AgentHeuristicRule) = safeDbCall { colonyDao.insertHeuristic(heuristic) }
    suspend fun deleteHeuristic(id: Int) = safeDbCall { colonyDao.deleteHeuristic(id) }

    // LLM Call Telemetry
    val allLlmTelemetry: Flow<List<LlmCallTelemetry>> = colonyDao.getAllLlmTelemetry()
    suspend fun insertLlmTelemetry(telemetry: LlmCallTelemetry) = safeDbCall { colonyDao.insertLlmTelemetry(telemetry) }
    suspend fun clearLlmTelemetry() = safeDbCall { colonyDao.clearLlmTelemetry() }

    // Finance Transactions
    val allFinanceTransactions: Flow<List<FinanceTransaction>> = colonyDao.getAllFinanceTransactions()
    suspend fun insertFinanceTransaction(transaction: FinanceTransaction) = safeDbCall { colonyDao.insertFinanceTransaction(transaction) }
    suspend fun insertFinanceTransactions(transactions: List<FinanceTransaction>) = safeDbCall { colonyDao.insertFinanceTransactions(transactions) }
    suspend fun clearFinanceTransactions() = safeDbCall { colonyDao.clearFinanceTransactions() }

    // Custom Agent Definitions
    val allCustomAgentDefinitions: Flow<List<CustomAgentDefinition>> = colonyDao.getAllCustomAgentDefinitions()
    suspend fun insertCustomAgentDefinition(definition: CustomAgentDefinition) = safeDbCall { colonyDao.insertCustomAgentDefinition(definition) }
    suspend fun deleteCustomAgentDefinition(id: Int) = safeDbCall { colonyDao.deleteCustomAgentDefinition(id) }

    // Flashcards (Spaced Repetition)
    val allFlashcards: Flow<List<Flashcard>> = colonyDao.getAllFlashcards()
    suspend fun insertFlashcard(flashcard: Flashcard) = safeDbCall { colonyDao.insertFlashcard(flashcard) }
    suspend fun deleteFlashcard(id: Int) = safeDbCall { colonyDao.deleteFlashcard(id) }

    // Subscriptions
    val allSubscriptions: Flow<List<Subscription>> = colonyDao.getAllSubscriptions()
    suspend fun insertSubscription(subscription: Subscription) = safeDbCall { colonyDao.insertSubscription(subscription) }
    suspend fun insertSubscriptions(subscriptions: List<Subscription>) = safeDbCall { colonyDao.insertSubscriptions(subscriptions) }
    suspend fun deleteSubscription(id: Int) = safeDbCall { colonyDao.deleteSubscription(id) }
    suspend fun updateSubscriptionCancelled(id: Int, isCancelled: Boolean) = safeDbCall { colonyDao.updateSubscriptionCancelled(id, isCancelled) }

    // Sleep Records
    val allSleepRecords: Flow<List<SleepRecord>> = colonyDao.getAllSleepRecords()
    suspend fun insertSleepRecord(record: SleepRecord) = safeDbCall { colonyDao.insertSleepRecord(record) }
    suspend fun clearSleepRecords() = safeDbCall { colonyDao.clearSleepRecords() }
    
    // Agent Sentiment Logs
    fun getSentimentLogsForAgent(agentName: String): Flow<List<AgentSentimentLog>> = colonyDao.getSentimentLogsForAgent(agentName)
    suspend fun insertSentimentLog(log: AgentSentimentLog) = safeDbCall { colonyDao.insertSentimentLog(log) }

    suspend fun checkDatabaseIntegrity(): Boolean {
        return safeDbCall {
            val query = androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA integrity_check")
            val cursor = colonyDao.checkIntegrity(query)
            if (cursor.moveToFirst()) {
                val result = cursor.getString(0)
                cursor.close()
                result.equals("ok", ignoreCase = true)
            } else {
                cursor.close()
                false
            }
        }
    }

    private inline fun <T> safeDbCall(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            android.util.Log.e("ColonyRepository", "Database execution failed", e)
            throw e
        }
    }
}
