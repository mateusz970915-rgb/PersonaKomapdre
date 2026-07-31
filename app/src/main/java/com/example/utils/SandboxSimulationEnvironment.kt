package com.example.utils

import android.util.Log
import com.example.data.Agent
import com.example.data.Mission
import com.example.data.SubTask
import kotlinx.coroutines.delay

object SandboxSimulationEnvironment {

    data class SimulationSnapshot(
        val agents: List<Agent>,
        val tasks: List<Mission>,
        val activeMissions: List<SubTask>
    )

    data class SimulationResult(
        val success: Boolean,
        val predictedFinalState: SimulationSnapshot,
        val logs: List<String>,
        val estimatedTimeMs: Long
    )

    /**
     * Runs a simulated action on a deep copy of the state.
     * Does NOT mutate the real database.
     */
    suspend fun runSimulation(
        initialState: SimulationSnapshot,
        action: suspend (SimulationSnapshot, (String) -> Unit) -> SimulationSnapshot
    ): SimulationResult {
        val logs = mutableListOf<String>()
        val logger: (String) -> Unit = { msg: String -> 
            val simMsg = "[SIMULATION] $msg"
            logs.add(simMsg)
            Log.d("SandboxSimulator", simMsg)
            Unit
        }
        
        logger("Initializing Sandbox Environment...")
        
        // Deep copy by using data class copy (assuming no complex nested mutable references)
        val sandboxState = initialState.copy(
            agents = initialState.agents.map { it.copy() },
            tasks = initialState.tasks.map { it.copy() },
            activeMissions = initialState.activeMissions.map { it.copy() }
        )

        val startTime = System.currentTimeMillis()
        
        return try {
            logger("Executing logic in Isolated Context...")
            val finalState = action(sandboxState, logger)
            val endTime = System.currentTimeMillis()
            
            SimulationResult(
                success = true,
                predictedFinalState = finalState,
                logs = logs,
                estimatedTimeMs = endTime - startTime
            )
        } catch (e: Exception) {
            logger("Simulation Failed: ${e.message}")
            SimulationResult(
                success = false,
                predictedFinalState = sandboxState,
                logs = logs,
                estimatedTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }
}
