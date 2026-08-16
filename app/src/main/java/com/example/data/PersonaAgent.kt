package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persona_agents")
data class PersonaAgent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val personalityType: String,
    val role: String,
    val currentStatus: String = "Idle",
    val taskSummary: String = "No tasks currently."
)
