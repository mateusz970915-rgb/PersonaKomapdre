with open("app/src/main/java/com/example/data/AgentPreferencesRepository.kt", "r") as f:
    content = f.read()

bad = """    suspend fun updateStrictManualOverride(override: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[STRICT_MANUAL_OVERRIDE] = override
        }
        suspend fun updateHasSeenWalkthrough(seen: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[HAS_SEEN_WALKTHROUGH] = seen
        }
    }
}"""

good = """    suspend fun updateStrictManualOverride(override: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[STRICT_MANUAL_OVERRIDE] = override
        }
    }

    suspend fun updateHasSeenWalkthrough(seen: Boolean) {
        context.agentDataStore.edit { preferences ->
            preferences[HAS_SEEN_WALKTHROUGH] = seen
        }
    }
}"""

content = content.replace(bad, good)
with open("app/src/main/java/com/example/data/AgentPreferencesRepository.kt", "w") as f:
    f.write(content)
