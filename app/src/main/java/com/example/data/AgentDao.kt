package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: Agent)

    @Delete
    suspend fun deleteAgent(agent: Agent)

    @Query("SELECT * FROM agents")
    fun getAllAgents(): Flow<List<Agent>>

    @Query("SELECT * FROM agents WHERE id = :id")
    suspend fun getAgentById(id: Int): Agent?
}
