package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.personaDataStore by preferencesDataStore(name = "persona_mesh_settings")

class PersonaMeshPreferences(private val context: Context) {
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATION_FREQUENCY = stringPreferencesKey("notification_frequency")
    }

    val isDarkMode: Flow<Boolean> = context.personaDataStore.data
        .map { preferences ->
            preferences[DARK_MODE] ?: false
        }

    val notificationFrequency: Flow<String> = context.personaDataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_FREQUENCY] ?: "Normal"
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.personaDataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun setNotificationFrequency(frequency: String) {
        context.personaDataStore.edit { preferences ->
            preferences[NOTIFICATION_FREQUENCY] = frequency
        }
    }
}
