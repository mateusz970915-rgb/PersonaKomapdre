package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.ColonyMemory
import com.example.utils.NotificationHelper
import kotlinx.coroutines.flow.first

class AgentRestSchedulerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.colonyDao()
        val prefs = applicationContext.getSharedPreferences("colony_prefs", Context.MODE_PRIVATE)
        val isSchedulerEnabled = prefs.getBoolean("scheduler_enabled", true)
        
        if (!isSchedulerEnabled) {
            return Result.success()
        }

        try {
            val agents = dao.getAllAgents().first()
            val subTasks = dao.getAllSubTasks().first()
            val workloadThreshold = prefs.getInt("workload_threshold", 2)
            val restDurationSec = prefs.getInt("rest_duration_seconds", 30)
            val restDurationMs = restDurationSec * 1000L
            val now = System.currentTimeMillis()

            val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
            val batteryPct = batteryManager?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)?.toFloat() ?: 100f
            val isLowBattery = batteryPct < 20f

            agents.forEach { agent ->
                val activeTasks = subTasks.filter {
                    it.assignedAgent.equals(agent.name, ignoreCase = true) &&
                    (it.status == "Pending" || it.status == "In Progress")
                }
                val activeCount = activeTasks.size

                when (agent.status) {
                    "Resting" -> {
                        val restEndTime = prefs.getLong("rest_end_${agent.id}", 0L)
                        if (isLowBattery) {
                            prefs.edit().putLong("rest_end_${agent.id}", now + restDurationMs).apply()
                        } else if (restEndTime == 0L || now >= restEndTime) {
                            val updatedAgent = agent.copy(status = "Active")
                            dao.insertAgent(updatedAgent)
                            prefs.edit().remove("rest_end_${agent.id}").apply()
                            dao.insertMemory(
                                ColonyMemory(
                                    content = "Agent ${agent.name} has completed rest and returned to Active status (Battery: ${batteryPct.toInt()}%)."
                                )
                            )
                            NotificationHelper.sendNotification(
                                applicationContext,
                                agent.id,
                                "Agent Rest Completed",
                                "${agent.name} is fully recovered and active again!"
                            )
                        }
                    }
                    "Active" -> {
                        if (isLowBattery || activeCount >= workloadThreshold) {
                            val updatedAgent = agent.copy(status = "Resting")
                            dao.insertAgent(updatedAgent)
                            val restEndTime = now + restDurationMs
                            prefs.edit().putLong("rest_end_${agent.id}", restEndTime).apply()
                            val reason = if (isLowBattery) "OS Resource Check: Battery low (${batteryPct.toInt()}%)!" else "Workload: $activeCount active tasks"
                            dao.insertMemory(
                                ColonyMemory(
                                    content = "Agent ${agent.name} is overworked ($reason) and has started a ${restDurationSec}s rest period."
                                )
                            )
                            NotificationHelper.sendNotification(
                                applicationContext,
                                agent.id,
                                "Agent Resting",
                                "${agent.name} is overworked and is now resting."
                            )
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("AgentRestScheduler", "Error in scheduler worker", e)
            return Result.failure()
        }
    }
}
