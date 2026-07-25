package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.agentDataStore: DataStore<Preferences> by preferencesDataStore(name = "agent_preferences")

data class AgentPreferences(
    val globalAutonomyThreshold: String = "Semi-Autonomous",
    val allowBackgroundExecution: Boolean = true,
    val allowDataAccess: Boolean = true,
    val maxActiveTasksPerPersona: Int = 5,
    val notificationsEnabled: Boolean = true,
    val strictManualOverride: Boolean = false,
    val aiProvider: String = "gemini",
    val openRouterApiKey: String = "",
    val openRouterSelectedModel: String = "meta-llama/llama-3.3-70b-instruct:free"
)

class AgentPreferencesRepository(private val context: Context) {

    companion object {
        val GLOBAL_AUTONOMY_THRESHOLD = stringPreferencesKey("global_autonomy_threshold")
        val ALLOW_BACKGROUND_EXECUTION = booleanPreferencesKey("allow_background_execution")
        val ALLOW_DATA_ACCESS = booleanPreferencesKey("allow_data_access")
        val MAX_ACTIVE_TASKS = intPreferencesKey("max_active_tasks")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STRICT_MANUAL_OVERRIDE = booleanPreferencesKey("strict_manual_override")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
        val OPENROUTER_SELECTED_MODEL = stringPreferencesKey("openrouter_selected_model")
    }

    val agentPreferencesFlow: Flow<AgentPreferences> = context.agentDataStore.data.map { preferences ->
        AgentPreferences(
            globalAutonomyThreshold = preferences[GLOBAL_AUTONOMY_THRESHOLD] ?: "Semi-Autonomous",
            allowBackgroundExecution = preferences[ALLOW_BACKGROUND_EXECUTION] ?: true,
            allowDataAccess = preferences[ALLOW_DATA_ACCESS] ?: true,
            maxActiveTasksPerPersona = preferences[MAX_ACTIVE_TASKS] ?: 5,
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            strictManualOverride = preferences[STRICT_MANUAL_OVERRIDE] ?: false,
            aiProvider = preferences[AI_PROVIDER] ?: "gemini",
            openRouterApiKey = preferences[OPENROUTER_API_KEY] ?: "",
            openRouterSelectedModel = preferences[OPENROUTER_SELECTED_MODEL] ?: "meta-llama/llama-3.3-70b-instruct:free"
        )
    }

    suspend fun updateAiProvider(provider: String) {
        context.agentDataStore.edit { preferences ->
            preferences[AI_PROVIDER] = provider
        }
    }

    suspend fun updateOpenRouterApiKey(key: String) {
        context.agentDataStore.edit { preferences ->
            preferences[OPENROUTER_API_KEY] = key
        }
    }

    suspend fun updateOpenRouterSelectedModel(model: String) {
        context.agentDataStore.edit { preferences ->
            preferences[OPENROUTER_SELECTED_MODEL] = model
        }
    }

    suspend fun updateGlobalAutonomyThreshold(threshold: String) {
        context.agentDataStore.edit { preferences ->
            preferences[GLOBAL_AUTONOMY_THRESHOLD] = threshold
        }
    }

    suspend fun updateAllowBackgroundExecution(allow: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[ALLOW_BACKGROUND_EXECUTION] = allow
        }
    }

    suspend fun updateAllowDataAccess(allow: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[ALLOW_DATA_ACCESS] = allow
        }
    }

    suspend fun updateMaxActiveTasksPerPersona(maxTasks: Int) {
        context.agentDataStore.edit { preferences ->
            preferences[MAX_ACTIVE_TASKS] = maxTasks
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateStrictManualOverride(override: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[STRICT_MANUAL_OVERRIDE] = override
        }
    }
}
