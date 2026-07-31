package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heuristic_rules")
data class HeuristicRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleName: String,
    val description: String,
    val conditionRegex: String,
    val actionScript: String, // E.g., JSON or simple script
    val confidenceScore: Float = 0.5f,
    val isApproved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
