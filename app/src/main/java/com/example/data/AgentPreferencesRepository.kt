package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.security.LocalEncryptedVault
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
    val aiProvider: String = "openrouter",
    val openRouterApiKey: String = "",
    val openRouterSelectedModel: String = "meta-llama/llama-3.3-70b-instruct:free",
    val geminiApiKey: String = "",
    val geminiSelectedModel: String = "gemini-3.5-flash",
    val themeMode: String = "Default",
    val isFocusModeActive: Boolean = false,
    val hasSeenWalkthrough: Boolean = false,
    val primaryLanguage: String = "English",
    val autoUpdatesEnabled: Boolean = true,
    val allowAgentCommunication: Boolean = true,
    val allowMeshBroadcasts: Boolean = true,
    val encryptAgentMessages: Boolean = true,
    val allowCrossColonySync: Boolean = false,
    val logAgentCommunication: Boolean = true,
    val selectedChartType: String = "Line",
    val selectedChartDateRange: Int = 7,
    val chartColorIntensity: String = "Standard",
    val trendAlertThreshold: Int = 20,
    val trendAlertsEnabled: Boolean = true,
    val trendComparisonInterval: String = "Weekly",
    val trendAggregationMethod: String = "Total Sum",
    val showCalendarOverlay: Boolean = false,
    val retentionPolicyDays: Int = 30,
    val autoArchivingEnabled: Boolean = true,
    val dailyMemorySnapshot: String = ""
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
        val OPENROUTER_SELECTED_MODEL = stringPreferencesKey("openrouter_selected_model")
        val GEMINI_SELECTED_MODEL = stringPreferencesKey("gemini_selected_model")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_FOCUS_MODE_ACTIVE = booleanPreferencesKey("is_focus_mode_active")
        val HAS_SEEN_WALKTHROUGH = booleanPreferencesKey("has_seen_walkthrough")
        val PRIMARY_LANGUAGE = stringPreferencesKey("primary_language")
        val AUTO_UPDATES_ENABLED = booleanPreferencesKey("auto_updates_enabled")
        val ALLOW_AGENT_COMMUNICATION = booleanPreferencesKey("allow_agent_communication")
        val ALLOW_MESH_BROADCASTS = booleanPreferencesKey("allow_mesh_broadcasts")
        val ENCRYPT_AGENT_MESSAGES = booleanPreferencesKey("encrypt_agent_messages")
        val ALLOW_CROSS_COLONY_SYNC = booleanPreferencesKey("allow_cross_colony_sync")
        val LOG_AGENT_COMMUNICATION = booleanPreferencesKey("log_agent_communication")
        val SELECTED_CHART_TYPE = stringPreferencesKey("selected_chart_type")
        val SELECTED_CHART_DATE_RANGE = intPreferencesKey("selected_chart_date_range")
        val CHART_COLOR_INTENSITY = stringPreferencesKey("chart_color_intensity")
        val TREND_ALERT_THRESHOLD = intPreferencesKey("trend_alert_threshold")
        val TREND_ALERTS_ENABLED = booleanPreferencesKey("trend_alerts_enabled")
        val TREND_COMPARISON_INTERVAL = stringPreferencesKey("trend_comparison_interval")
        val TREND_AGGREGATION_METHOD = stringPreferencesKey("trend_aggregation_method")
        val SHOW_CALENDAR_OVERLAY = booleanPreferencesKey("show_calendar_overlay")
        val RETENTION_POLICY_DAYS = intPreferencesKey("retention_policy_days")
        val DAILY_MEMORY_SNAPSHOT = stringPreferencesKey("daily_memory_snapshot")
        val AUTO_ARCHIVING_ENABLED = booleanPreferencesKey("auto_archiving_enabled")
        val LAST_UPDATED = longPreferencesKey("last_updated")


    }

    val agentPreferencesFlow: Flow<AgentPreferences> = context.agentDataStore.data.map { preferences ->
        AgentPreferences(
            globalAutonomyThreshold = preferences[GLOBAL_AUTONOMY_THRESHOLD] ?: "Semi-Autonomous",
            allowBackgroundExecution = preferences[ALLOW_BACKGROUND_EXECUTION] ?: true,
            allowDataAccess = preferences[ALLOW_DATA_ACCESS] ?: true,
            maxActiveTasksPerPersona = preferences[MAX_ACTIVE_TASKS] ?: 5,
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            strictManualOverride = preferences[STRICT_MANUAL_OVERRIDE] ?: false,
            aiProvider = preferences[AI_PROVIDER] ?: "openrouter",
            openRouterApiKey = LocalEncryptedVault.getSecret(context, "openRouterApiKey") ?: "",
            openRouterSelectedModel = preferences[OPENROUTER_SELECTED_MODEL] ?: "meta-llama/llama-3.3-70b-instruct:free",
            geminiApiKey = LocalEncryptedVault.getSecret(context, "geminiApiKey") ?: "",
            geminiSelectedModel = preferences[GEMINI_SELECTED_MODEL] ?: "gemini-3.5-flash",
            themeMode = preferences[THEME_MODE] ?: "Default",
            isFocusModeActive = preferences[IS_FOCUS_MODE_ACTIVE] ?: false,
            hasSeenWalkthrough = preferences[HAS_SEEN_WALKTHROUGH] ?: false,
            primaryLanguage = preferences[PRIMARY_LANGUAGE] ?: "English",
            autoUpdatesEnabled = preferences[AUTO_UPDATES_ENABLED] ?: true,
            allowAgentCommunication = preferences[ALLOW_AGENT_COMMUNICATION] ?: true,
            allowMeshBroadcasts = preferences[ALLOW_MESH_BROADCASTS] ?: true,
            encryptAgentMessages = preferences[ENCRYPT_AGENT_MESSAGES] ?: true,
            allowCrossColonySync = preferences[ALLOW_CROSS_COLONY_SYNC] ?: false,
            logAgentCommunication = preferences[LOG_AGENT_COMMUNICATION] ?: true,
            selectedChartType = preferences[SELECTED_CHART_TYPE] ?: "Line",
            selectedChartDateRange = preferences[SELECTED_CHART_DATE_RANGE] ?: 7,
            chartColorIntensity = preferences[CHART_COLOR_INTENSITY] ?: "Standard",
            trendAlertThreshold = preferences[TREND_ALERT_THRESHOLD] ?: 20,
            trendAlertsEnabled = preferences[TREND_ALERTS_ENABLED] ?: true,
            trendComparisonInterval = preferences[TREND_COMPARISON_INTERVAL] ?: "Weekly",
            trendAggregationMethod = preferences[TREND_AGGREGATION_METHOD] ?: "Total Sum",
            showCalendarOverlay = preferences[SHOW_CALENDAR_OVERLAY] ?: false,
            retentionPolicyDays = preferences[RETENTION_POLICY_DAYS] ?: 30,
            autoArchivingEnabled = preferences[AUTO_ARCHIVING_ENABLED] ?: true,
            dailyMemorySnapshot = preferences[DAILY_MEMORY_SNAPSHOT] ?: ""
        )
    }



    suspend fun updateTrendAggregationMethod(method: String) {
        context.agentDataStore.edit { preferences ->
            preferences[TREND_AGGREGATION_METHOD] = method
        }
    }


    suspend fun updateShowCalendarOverlay(show: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[SHOW_CALENDAR_OVERLAY] = show
        }
    }


    suspend fun updateSelectedChartType(chartType: String) {
        context.agentDataStore.edit { preferences ->
            preferences[SELECTED_CHART_TYPE] = chartType
        }
    }


    suspend fun updateSelectedChartDateRange(dateRange: Int) {
        context.agentDataStore.edit { preferences ->
            preferences[SELECTED_CHART_DATE_RANGE] = dateRange
        }
    }


    suspend fun updateChartColorIntensity(intensity: String) {
        context.agentDataStore.edit { preferences ->
            preferences[CHART_COLOR_INTENSITY] = intensity
        }
    }


    suspend fun updateTrendAlertThreshold(threshold: Int) {
        context.agentDataStore.edit { preferences ->
            preferences[TREND_ALERT_THRESHOLD] = threshold
        }
    }


    suspend fun updateTrendAlertsEnabled(enabled: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[TREND_ALERTS_ENABLED] = enabled
        }
    }


    suspend fun updateTrendComparisonInterval(interval: String) {
        context.agentDataStore.edit { preferences ->
            preferences[TREND_COMPARISON_INTERVAL] = interval
        }
    }


    suspend fun updateThemeMode(mode: String) {
        context.agentDataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }


    suspend fun updateFocusModeActive(active: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[IS_FOCUS_MODE_ACTIVE] = active
        }
    }


    suspend fun updateAiProvider(provider: String) {
        context.agentDataStore.edit { preferences ->
            preferences[AI_PROVIDER] = provider
        }
    }


    suspend fun updateOpenRouterApiKey(key: String) {
        LocalEncryptedVault.saveSecret(context, "openRouterApiKey", key)
    }



    suspend fun updateOpenRouterSelectedModel(model: String) {
        context.agentDataStore.edit { preferences ->
            preferences[OPENROUTER_SELECTED_MODEL] = model
        }
    }


    suspend fun updateGeminiApiKey(key: String) {
        LocalEncryptedVault.saveSecret(context, "geminiApiKey", key)
    }



    suspend fun updateGeminiSelectedModel(model: String) {
        context.agentDataStore.edit { preferences ->
            preferences[GEMINI_SELECTED_MODEL] = model
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


    suspend fun updateHasSeenWalkthrough(seen: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[HAS_SEEN_WALKTHROUGH] = seen
        }
    }


    suspend fun updatePrimaryLanguage(language: String) {
        context.agentDataStore.edit { preferences ->
            preferences[PRIMARY_LANGUAGE] = language
        }
    }


    suspend fun updateAutoUpdatesEnabled(enabled: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[AUTO_UPDATES_ENABLED] = enabled
        }
    }


    suspend fun updateAllowAgentCommunication(allow: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[ALLOW_AGENT_COMMUNICATION] = allow
        }
    }


    suspend fun updateAllowMeshBroadcasts(allow: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[ALLOW_MESH_BROADCASTS] = allow
        }
    }


    suspend fun updateEncryptAgentMessages(encrypt: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[ENCRYPT_AGENT_MESSAGES] = encrypt
        }
    }


    suspend fun updateAllowCrossColonySync(allow: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[ALLOW_CROSS_COLONY_SYNC] = allow
        }
    }


    suspend fun updateLogAgentCommunication(log: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[LOG_AGENT_COMMUNICATION] = log
        }
    }


    suspend fun updateRetentionPolicyDays(days: Int) {
        context.agentDataStore.edit { preferences ->
            preferences[RETENTION_POLICY_DAYS] = days
        }
    }


    suspend fun updateAutoArchivingEnabled(enabled: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[AUTO_ARCHIVING_ENABLED] = enabled
        }
    }



    suspend fun updateLastUpdatedTimestamp(timestamp: Long = System.currentTimeMillis()) {
        context.agentDataStore.edit { preferences ->
            preferences[LAST_UPDATED] = timestamp
        }
    }

    suspend fun updateDailyMemorySnapshot(snapshot: String) {
        context.agentDataStore.edit { preferences ->
            preferences[DAILY_MEMORY_SNAPSHOT] = snapshot
        }
    }
}
