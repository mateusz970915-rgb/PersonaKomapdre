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

    fun updateAgentAvatar(agentId: Int, newAvatarUrl: String) {
        viewModelScope.launch {
            val agent = agentsState.value.find { it.id == agentId }
            if (agent != null) {
                baseRepository.insertAgent(agent.copy(avatarUrl = newAvatarUrl))
            }
        }
    }

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

    fun updateGeminiApiKey(key: String) {
        viewModelScope.launch {
            preferencesRepository.updateGeminiApiKey(key)
        }
    }

    fun updateGeminiSelectedModel(model: String) {
        viewModelScope.launch {
            preferencesRepository.updateGeminiSelectedModel(model)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(mode)
        }
    }

    fun updateFocusModeActive(active: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateFocusModeActive(active)
            try {
                val context = getApplication<android.app.Application>()
                val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                if (notificationManager != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if (notificationManager.isNotificationPolicyAccessGranted) {
                            val filter = if (active) {
                                android.app.NotificationManager.INTERRUPTION_FILTER_NONE
                            } else {
                                android.app.NotificationManager.INTERRUPTION_FILTER_ALL
                            }
                            notificationManager.setInterruptionFilter(filter)
                        } else {
                            android.util.Log.d("BaseAgentViewModel", "ACCESS_NOTIFICATION_POLICY is not granted. Cannot automatically toggle DND.")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BaseAgentViewModel", "Error toggling system DND filter", e)
            }
        }
    }

    fun updateHasSeenWalkthrough(seen: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHasSeenWalkthrough(seen)
        }
    }

    fun updatePrimaryLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.updatePrimaryLanguage(language)
        }
    }

    fun updateAutoUpdatesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoUpdatesEnabled(enabled)
        }
    }

    fun updateAllowAgentCommunication(allow: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAllowAgentCommunication(allow)
        }
    }

    fun updateAllowMeshBroadcasts(allow: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAllowMeshBroadcasts(allow)
        }
    }

    fun updateEncryptAgentMessages(encrypt: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateEncryptAgentMessages(encrypt)
        }
    }

    fun updateAllowCrossColonySync(allow: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAllowCrossColonySync(allow)
        }
    }

    fun updateLogAgentCommunication(log: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateLogAgentCommunication(log)
        }
    }

    fun updateSelectedChartType(chartType: String) {
        viewModelScope.launch {
            preferencesRepository.updateSelectedChartType(chartType)
        }
    }

    fun updateSelectedChartDateRange(dateRange: Int) {
        viewModelScope.launch {
            preferencesRepository.updateSelectedChartDateRange(dateRange)
        }
    }

    fun updateChartColorIntensity(intensity: String) {
        viewModelScope.launch {
            preferencesRepository.updateChartColorIntensity(intensity)
        }
    }

    fun updateTrendAlertThreshold(threshold: Int) {
        viewModelScope.launch {
            preferencesRepository.updateTrendAlertThreshold(threshold)
        }
    }

    fun updateTrendAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateTrendAlertsEnabled(enabled)
        }
    }

    fun updateTrendComparisonInterval(interval: String) {
        viewModelScope.launch {
            preferencesRepository.updateTrendComparisonInterval(interval)
        }
    }

    fun updateTrendAggregationMethod(method: String) {
        viewModelScope.launch {
            preferencesRepository.updateTrendAggregationMethod(method)
        }
    }

    fun updateShowCalendarOverlay(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowCalendarOverlay(show)
        }
    }

    fun triggerInteractionAnomalyCheck() {
        try {
            val workManager = androidx.work.WorkManager.getInstance(getApplication())
            val request = androidx.work.OneTimeWorkRequestBuilder<com.example.worker.InteractionAnomalyWorker>().build()
            workManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerAgentDataSync() {
        try {
            val workManager = androidx.work.WorkManager.getInstance(getApplication())
            val request = androidx.work.OneTimeWorkRequestBuilder<com.example.worker.AgentDataSyncWorker>().build()
            workManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
