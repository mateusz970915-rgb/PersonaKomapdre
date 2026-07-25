package com.example.data

import kotlinx.coroutines.flow.Flow

class ColonyRepository(private val colonyDao: ColonyDao) {
    val allAgents: Flow<List<Agent>> = colonyDao.getAllAgents()
    val councilMessages: Flow<List<CouncilMessage>> = colonyDao.getCouncilMessages()
    val allMemories: Flow<List<ColonyMemory>> = colonyDao.getMemories()
    val allDecisions: Flow<List<AgentDecision>> = colonyDao.getAgentDecisions()
    val allMissions: Flow<List<Mission>> = colonyDao.getMissions()
    val allDataAccessRequests: Flow<List<DataAccessRequest>> = colonyDao.getDataAccessRequests()
    val allSubTasks: Flow<List<SubTask>> = colonyDao.getAllSubTasks()

    suspend fun insertAgent(agent: Agent) = colonyDao.insertAgent(agent)
    suspend fun deleteAgentById(id: Int) = colonyDao.deleteAgentById(id)
    suspend fun getAgentCount(): Int = colonyDao.getAgentCount()

    suspend fun insertMessage(message: CouncilMessage) = colonyDao.insertMessage(message)
    suspend fun clearMessages() = colonyDao.clearMessages()

    suspend fun insertMemory(memory: ColonyMemory) = colonyDao.insertMemory(memory)
    
    suspend fun insertDecision(decision: AgentDecision) = colonyDao.insertDecision(decision)
    suspend fun insertMission(mission: Mission): Long = colonyDao.insertMission(mission)
    fun getSubTasksForMission(missionId: Int): Flow<List<SubTask>> = colonyDao.getSubTasksForMission(missionId)
    suspend fun insertSubTask(subTask: SubTask) = colonyDao.insertSubTask(subTask)
    suspend fun updateSubTaskStatus(id: Int, status: String) {
        val completedAt = if (status == "Completed" || status == "EXECUTED" || status == "SIMULATED") System.currentTimeMillis() else 0L
        colonyDao.updateSubTaskStatusWithCompletion(id, status, completedAt)
    }
    
    suspend fun insertDataAccessRequest(request: DataAccessRequest) = colonyDao.insertDataAccessRequest(request)
    
    suspend fun haltSystem() {
        colonyDao.haltAllSystems()
    }

    val allCalendarEvents: Flow<List<CalendarEvent>> = colonyDao.getAllCalendarEvents()
    suspend fun insertCalendarEvent(event: CalendarEvent) = colonyDao.insertCalendarEvent(event)
    suspend fun deleteCalendarEventById(id: Int) = colonyDao.deleteCalendarEventById(id)
    suspend fun clearCalendarEvents() = colonyDao.clearCalendarEvents()

    val allBadges: Flow<List<Badge>> = colonyDao.getAllBadges()
    suspend fun insertBadge(badge: Badge) = colonyDao.insertBadge(badge)
    suspend fun insertBadges(badges: List<Badge>) = colonyDao.insertBadges(badges)
    suspend fun getBadgeCount(): Int = colonyDao.getBadgeCount()

    val allInterAgentMessages: Flow<List<InterAgentMessage>> = colonyDao.getAllInterAgentMessages()
    suspend fun insertInterAgentMessage(message: InterAgentMessage) = colonyDao.insertInterAgentMessage(message)
    suspend fun insertInterAgentMessages(messages: List<InterAgentMessage>) = colonyDao.insertInterAgentMessages(messages)
    suspend fun getInterAgentMessageCount(): Int = colonyDao.getInterAgentMessageCount()

    val allAgentMilestones: Flow<List<AgentMilestone>> = colonyDao.getAllAgentMilestones()
    suspend fun insertAgentMilestone(milestone: AgentMilestone) = colonyDao.insertAgentMilestone(milestone)
    suspend fun insertAgentMilestones(milestones: List<AgentMilestone>) = colonyDao.insertAgentMilestones(milestones)
    suspend fun getAgentMilestoneCount(): Int = colonyDao.getAgentMilestoneCount()

    // Rule Nodes & Connections
    val allRuleNodes: Flow<List<RuleNodeEntity>> = colonyDao.getAllRuleNodes()
    val allRuleConnections: Flow<List<RuleConnectionEntity>> = colonyDao.getAllRuleConnections()
    val completedSubTasks: Flow<List<SubTask>> = colonyDao.getCompletedSubTasks()

    suspend fun insertRuleNode(node: RuleNodeEntity) = colonyDao.insertRuleNode(node)
    suspend fun insertRuleNodes(nodes: List<RuleNodeEntity>) = colonyDao.insertRuleNodes(nodes)
    suspend fun deleteRuleNodeById(id: String) {
        colonyDao.deleteRuleNodeById(id)
        colonyDao.deleteConnectionsForNode(id)
    }
    suspend fun getRuleNodeCount(): Int = colonyDao.getRuleNodeCount()

    suspend fun insertRuleConnection(connection: RuleConnectionEntity) = colonyDao.insertRuleConnection(connection)
    suspend fun insertRuleConnections(connections: List<RuleConnectionEntity>) = colonyDao.insertRuleConnections(connections)
    suspend fun deleteRuleConnection(fromId: String, toId: String) = colonyDao.deleteRuleConnection(fromId, toId)
    suspend fun getRuleConnectionCount(): Int = colonyDao.getRuleConnectionCount()
}
