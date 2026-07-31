package com.example.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HeuristicRuleDao {
    @Query("SELECT * FROM heuristic_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<HeuristicRule>>

    @Query("SELECT * FROM heuristic_rules WHERE isApproved = 1 ORDER BY confidenceScore DESC")
    fun getApprovedRules(): Flow<List<HeuristicRule>>

    @Insert
    suspend fun insertRule(rule: HeuristicRule)

    @Update
    suspend fun updateRule(rule: HeuristicRule)
    
    @Query("UPDATE heuristic_rules SET isApproved = :approved WHERE id = :ruleId")
    suspend fun updateApproval(ruleId: Long, approved: Boolean)
}
