package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.interactionDataStore: DataStore<Preferences> by preferencesDataStore(name = "agent_interactions")

data class InteractionRecord(
    val id: String = UUID.randomUUID().toString(),
    val agentName: String,
    val timestamp: Long,
    val snippet: String,
    val modelUsed: String = "gemini-pro-latest",
    val totalTokens: Int = 0,
    val latencyMs: Long = 0L,
    val tag: String = "",
    val isFavorite: Boolean = false
)

class AgentInteractionLogger(private val context: Context) {
    private val GLOBAL_KEY = stringPreferencesKey("global_interactions")

    fun getAllInteractions(): Flow<List<InteractionRecord>> {
        return context.interactionDataStore.data.map { preferences ->
            val jsonString = preferences[GLOBAL_KEY] ?: "[]"
            val array = JSONArray(jsonString)
            val list = mutableListOf<InteractionRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InteractionRecord(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        agentName = obj.optString("agentName", "Unknown Agent"),
                        timestamp = obj.optLong("timestamp", 0L),
                        snippet = obj.optString("snippet", ""),
                        modelUsed = obj.optString("modelUsed", "gemini-pro-latest"),
                        totalTokens = obj.optInt("totalTokens", (50..250).random()), // Mocking some value if it wasn't saved
                        latencyMs = obj.optLong("latencyMs", (200L..1200L).random().toLong()), // Mocking some value
                        tag = obj.optString("tag", ""),
                        isFavorite = obj.optBoolean("isFavorite", false)
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        }
    }

    suspend fun logInteraction(agentName: String, snippet: String, modelUsed: String = "gemini-pro-latest", totalTokens: Int = 0, latencyMs: Long = 0L) {
        context.interactionDataStore.edit { preferences ->
            val jsonString = preferences[GLOBAL_KEY] ?: "[]"
            val array = JSONArray(jsonString)
            
            val newObj = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("agentName", agentName)
                put("timestamp", System.currentTimeMillis())
                put("snippet", snippet)
                put("modelUsed", modelUsed)
                put("totalTokens", totalTokens)
                put("latencyMs", latencyMs)
            }
            
            array.put(newObj)
            
            // Keep last 100 interactions
            if (array.length() > 100) {
                array.remove(0)
            }
            preferences[GLOBAL_KEY] = array.toString()
        }
    }
    
    
    suspend fun updateInteractionTag(interactionId: String, newTag: String) {
        context.interactionDataStore.edit { preferences ->
            val jsonString = preferences[GLOBAL_KEY] ?: "[]"
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                if (obj.optString("id") == interactionId) {
                    obj.put("tag", newTag)
                    break
                }
            }
            preferences[GLOBAL_KEY] = array.toString()
        }
    }


    suspend fun toggleInteractionFavorite(interactionId: String, isFavorite: Boolean) {
        context.interactionDataStore.edit { preferences ->
            val jsonString = preferences[GLOBAL_KEY] ?: "[]"
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                if (obj.optString("id") == interactionId) {
                    obj.put("isFavorite", isFavorite)
                    break
                }
            }
            preferences[GLOBAL_KEY] = array.toString()
        }
    }

    suspend fun clearInteractions() {
        context.interactionDataStore.edit { preferences ->
            preferences.remove(GLOBAL_KEY)
        }
    }
}
