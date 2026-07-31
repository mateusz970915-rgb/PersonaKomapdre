package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Agent
import com.example.data.AgentRepository
import com.example.di.DatabaseModule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AgentRepository = AgentRepository(
        DatabaseModule.provideAgentDao(application)
    )

    val agentsState: StateFlow<List<Agent>> = repository.allAgents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAgent(name: String, role: String, configurationJson: String = "{}") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newAgent = Agent(
                name = name,
                role = role,
                type = role.uppercase(),
                status = "Active",
                lastActiveTimestamp = System.currentTimeMillis(),
                configurationJson = configurationJson
            )
            repository.insertAgent(newAgent)
        }
    }

    fun deleteAgent(agent: Agent) {
        viewModelScope.launch {
            repository.deleteAgent(agent)
        }
    }

    fun updateAgentAccentColor(agent: Agent, colorHex: String) {
        viewModelScope.launch {
            // Parse or create JSON configuration containing accentColor
            val updatedJson = try {
                if (agent.configurationJson.isBlank() || agent.configurationJson == "{}") {
                    "{\"accentColor\":\"$colorHex\"}"
                } else if (agent.configurationJson.contains("accentColor")) {
                    agent.configurationJson.replace(Regex("\"accentColor\"\\s*:\\s*\"[^\"]*\""), "\"accentColor\":\"$colorHex\"")
                } else {
                    val trimmed = agent.configurationJson.trim().removeSuffix("}")
                    "$trimmed,\"accentColor\":\"$colorHex\"}"
                }
            } catch (e: Exception) {
                "{\"accentColor\":\"$colorHex\"}"
            }
            val updatedAgent = agent.copy(configurationJson = updatedJson)
            repository.insertAgent(updatedAgent)
        }
    }
}
