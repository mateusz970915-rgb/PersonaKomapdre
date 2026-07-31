import re

with open("app/src/main/java/com/example/data/AgentPreferencesRepository.kt", "r") as f:
    content = f.read()

# 1. Update AgentPreferences
content = content.replace(
    "val isFocusModeActive: Boolean = false\n)",
    "val isFocusModeActive: Boolean = false,\n    val hasSeenWalkthrough: Boolean = false\n)"
)

# 2. Add preference key
content = content.replace(
    "val IS_FOCUS_MODE_ACTIVE = booleanPreferencesKey(\"is_focus_mode_active\")",
    "val IS_FOCUS_MODE_ACTIVE = booleanPreferencesKey(\"is_focus_mode_active\")\n        val HAS_SEEN_WALKTHROUGH = booleanPreferencesKey(\"has_seen_walkthrough\")"
)

# 3. Add to flow mapping
content = content.replace(
    "isFocusModeActive = preferences[IS_FOCUS_MODE_ACTIVE] ?: false\n        )",
    "isFocusModeActive = preferences[IS_FOCUS_MODE_ACTIVE] ?: false,\n            hasSeenWalkthrough = preferences[HAS_SEEN_WALKTHROUGH] ?: false\n        )"
)

# 4. Add update method
update_method = """    suspend fun updateHasSeenWalkthrough(seen: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[HAS_SEEN_WALKTHROUGH] = seen
        }
    }
}"""
content = content.replace("}\n}", update_method)

with open("app/src/main/java/com/example/data/AgentPreferencesRepository.kt", "w") as f:
    f.write(content)
