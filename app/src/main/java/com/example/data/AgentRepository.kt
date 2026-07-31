package com.example.data

import kotlinx.coroutines.flow.Flow

class AgentRepository(private val agentDao: AgentDao) {

    val allAgents: Flow<List<Agent>> = agentDao.getAllAgents()

    suspend fun getAgentById(id: Int): Agent? {
        return agentDao.getAgentById(id)
    }

    suspend fun insertAgent(agent: Agent) {
        agentDao.insertAgent(agent)
    }

    suspend fun deleteAgent(agent: Agent) {
        agentDao.deleteAgent(agent)
    }
}
