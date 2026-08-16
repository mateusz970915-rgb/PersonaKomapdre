package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Agent
import com.example.data.AgentPreferencesRepository
import com.example.data.AgentRepository
import com.example.di.DatabaseModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AgentRepository = AgentRepository(
        DatabaseModule.provideAgentDao(application)
    )

    private val preferencesRepository: AgentPreferencesRepository = AgentPreferencesRepository(application)

    val agentPreferencesState = preferencesRepository.agentPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.example.data.AgentPreferences()
        )

    val agentsState: StateFlow<List<Agent>> = repository.allAgents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshAgentStatuses() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Ping agents and refresh timestamps/statuses in Room database
            val currentList = agentsState.value
            currentList.forEach { agent ->
                val updatedAgent = agent.copy(
                    lastActiveTimestamp = System.currentTimeMillis()
                )
                repository.insertAgent(updatedAgent)
            }
            _isRefreshing.value = false
        }
    }

    fun invokeAgentFunction(agent: Agent, onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            // Set status to Busy while performing function
            val busyAgent = agent.copy(
                status = "Busy",
                lastActiveTimestamp = System.currentTimeMillis()
            )
            repository.insertAgent(busyAgent)
            
            val context = getApplication<Application>()
            val prompt = "You are ${agent.name} (${agent.role}). Perform a quick diagnostic check on your internal systems and report back with a 1-sentence status."
            
            val resultNote = try {
                val reply = com.example.network.AILlmClient.generateContent(context, prompt).trim()
                reply.ifEmpty { "Diagnostic complete. Systems nominal." }
            } catch (e: Exception) {
                "Error during diagnostic: ${e.message}"
            }

            val newStatus = "Online"
            val updatedAgent = busyAgent.copy(
                status = newStatus,
                lastActiveTimestamp = System.currentTimeMillis(),
                statusNotes = resultNote
            )
            repository.insertAgent(updatedAgent)
            onComplete("${agent.name} reported: $resultNote")
        }
    }

    fun addAgent(
        name: String,
        role: String,
        category: String = "General",
        status: String = "Online",
        systemPrompt: String = "",
        configurationJson: String = "{}"
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newAgent = Agent(
                name = name,
                role = role,
                type = role.uppercase(),
                status = status,
                systemPrompt = systemPrompt,
                lastActiveTimestamp = System.currentTimeMillis(),
                configurationJson = configurationJson,
                category = category
            )
            repository.insertAgent(newAgent)
        }
    }

    fun updateAgentStatus(agent: Agent, newStatus: String) {
        viewModelScope.launch {
            val updated = agent.copy(
                status = newStatus,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            repository.insertAgent(updated)
        }
    }

    fun deleteAgent(agent: Agent) {
        viewModelScope.launch {
            repository.deleteAgent(agent)
        }
    }

    fun updatePrimaryLanguage(lang: String) {
        viewModelScope.launch {
            preferencesRepository.updatePrimaryLanguage(lang)
        }
    }

    fun updateAutoUpdates(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoUpdatesEnabled(enabled)
        }
    }

    fun updateAgentAccentColor(agent: Agent, colorHex: String) {
        viewModelScope.launch {
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
