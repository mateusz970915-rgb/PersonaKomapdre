package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_personas")
data class AgentPersona(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agentName: String,
    val characterTraits: String,
    val operationalRole: String,
    val communicationStyle: String
)
