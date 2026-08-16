#!/bin/bash
sed -i 's/^    suspend fun updateLastUpdatedTimestamp/    }\n\n    suspend fun updateLastUpdatedTimestamp/' app/src/main/java/com/example/data/AgentPreferencesRepository.kt
