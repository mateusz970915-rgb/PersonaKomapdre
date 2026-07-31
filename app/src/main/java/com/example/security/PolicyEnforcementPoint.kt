package com.example.security

import android.content.Context
import com.example.data.AgentPreferencesRepository
import kotlinx.coroutines.flow.first

object PolicyEnforcementPoint {

    suspend fun enforceAutonomy(context: Context, requestLevel: String): Boolean {
        val prefs = AgentPreferencesRepository(context).agentPreferencesFlow.first()
        if (prefs.strictManualOverride) return false
        
        // Simple mock of threshold logic
        val threshold = prefs.globalAutonomyThreshold
        return when (threshold) {
            "High" -> true
            "Semi-Autonomous" -> requestLevel != "High"
            "Manual" -> false
            else -> false
        }
    }

    suspend fun enforceDataAccess(context: Context): Boolean {
        val prefs = AgentPreferencesRepository(context).agentPreferencesFlow.first()
        return prefs.allowDataAccess
    }

    suspend fun enforceBackgroundExecution(context: Context): Boolean {
        val prefs = AgentPreferencesRepository(context).agentPreferencesFlow.first()
        return prefs.allowBackgroundExecution
    }
}
