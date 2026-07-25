package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Agent
import com.example.data.AgentPreferences
import com.example.data.AgentPreferencesRepository
import com.example.data.AppDatabase
import com.example.data.ColonyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class BaseAgentViewModel(application: Application) : AndroidViewModel(application) {

    protected val baseRepository: ColonyRepository = ColonyRepository(
        AppDatabase.getDatabase(application).colonyDao()
    )

    protected val preferencesRepository: AgentPreferencesRepository = AgentPreferencesRepository(application)

    val agentsState: StateFlow<List<Agent>> = baseRepository.allAgents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val agentPreferencesState: StateFlow<AgentPreferences> = preferencesRepository.agentPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AgentPreferences()
        )

    fun cycleAgentStatus(agentId: Int) {
        viewModelScope.launch {
            val currentList = agentsState.value
            val target = currentList.find { it.id == agentId } ?: return@launch
            val nextStatus = when (target.status) {
                "Active" -> "Paused"
                "Paused" -> "Syncing"
                "Syncing" -> "Halted"
                else -> "Active"
            }
            baseRepository.insertAgent(target.copy(status = nextStatus))
        }
    }

    fun setAgentStatus(agentId: Int, newStatus: String) {
        viewModelScope.launch {
            val target = agentsState.value.find { it.id == agentId } ?: return@launch
            baseRepository.insertAgent(target.copy(status = newStatus))
        }
    }

    fun updateAgentConfig(agent: Agent) {
        viewModelScope.launch {
            baseRepository.insertAgent(agent)
        }
    }

    fun updateAgentAutonomy(agentId: Int, autonomyLevel: String, permissions: String) {
        viewModelScope.launch {
            val target = agentsState.value.find { it.id == agentId } ?: return@launch
            baseRepository.insertAgent(
                target.copy(
                    autonomyLevel = autonomyLevel,
                    permissions = permissions
                )
            )
        }
    }

    fun registerAgent(agent: Agent) {
        viewModelScope.launch {
            baseRepository.insertAgent(agent)
        }
    }

    open fun deleteAgent(agentId: Int) {
        viewModelScope.launch {
            baseRepository.deleteAgentById(agentId)
        }
    }

    fun updateGlobalAutonomyThreshold(threshold: String) {
        viewModelScope.launch {
            preferencesRepository.updateGlobalAutonomyThreshold(threshold)
        }
    }

    fun updateAllowBackgroundExecution(allow: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAllowBackgroundExecution(allow)
        }
    }

    fun updateAllowDataAccess(allow: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAllowDataAccess(allow)
        }
    }

    fun updateMaxActiveTasksPerPersona(maxTasks: Int) {
        viewModelScope.launch {
            preferencesRepository.updateMaxActiveTasksPerPersona(maxTasks)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun updateStrictManualOverride(override: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateStrictManualOverride(override)
        }
    }

    fun updateAiProvider(provider: String) {
        viewModelScope.launch {
            preferencesRepository.updateAiProvider(provider)
        }
    }

    fun updateOpenRouterApiKey(key: String) {
        viewModelScope.launch {
            preferencesRepository.updateOpenRouterApiKey(key)
        }
    }

    fun updateOpenRouterSelectedModel(model: String) {
        viewModelScope.launch {
            preferencesRepository.updateOpenRouterSelectedModel(model)
        }
    }
}
