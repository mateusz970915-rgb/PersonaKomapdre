package com.example.service
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.Agent
import com.example.data.ColonyMemory
import com.example.utils.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest

class AgentRestSchedulerService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d("AgentRestScheduler", "Scheduler Service Created")
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AgentRestScheduler", "Scheduler Service Started")
        val notification = NotificationHelper.createForegroundNotification(this, "Agent Colony active", "Monitoring agent health")
        try {
            startForeground(1, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!isRunning) {
            isRunning = true
            startSchedulerObserver()
        }
        return START_STICKY
    }

    private fun startSchedulerObserver() {
        serviceScope.launch {
            val database = AppDatabase.getDatabase(this@AgentRestSchedulerService)
            val dao = database.colonyDao()
            val prefs = getSharedPreferences("colony_prefs", Context.MODE_PRIVATE)

            // React to Database updates reactively via Flow without endless tight loop polling
            combine(dao.getAllAgents(), dao.getAllSubTasks()) { agents, subTasks ->
                Pair(agents, subTasks)
            }.collectLatest { (agents, subTasks) ->
                val isSchedulerEnabled = prefs.getBoolean("scheduler_enabled", true)
                if (!isSchedulerEnabled) return@collectLatest

                val workloadThreshold = prefs.getInt("workload_threshold", 2)
                val restDurationSec = prefs.getInt("rest_duration_seconds", 30)
                val restDurationMs = restDurationSec * 1000L
                val now = System.currentTimeMillis()

                val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
                val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()) else 100f
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
                                // Rest finished! Restore to Active
                                val updatedAgent = agent.copy(status = "Active")
                                dao.insertAgent(updatedAgent)
                                prefs.edit().remove("rest_end_${agent.id}").apply()
                                dao.insertMemory(
                                    ColonyMemory(
                                        content = "Agent ${agent.name} has completed rest and returned to Active status (Battery: ${batteryPct.toInt()}%)."
                                    )
                                )
                                NotificationHelper.sendNotification(
                                    this@AgentRestSchedulerService,
                                    agent.id,
                                    "Agent Rest Completed",
                                    "${agent.name} is fully recovered and active again!"
                                )
                                Log.d("AgentRestScheduler", "Agent ${agent.name} is active again.")
                            } else {
                                // Schedule timed wake-up for when rest duration completes
                                val remainingMs = restEndTime - now
                                if (remainingMs > 0) {
                                    launch {
                                        delay(remainingMs)
                                        // Re-check
                                        val refreshedAgent = dao.getAgentById(agent.id)
                                        if (refreshedAgent != null && refreshedAgent.status == "Resting") {
                                            val refreshedNow = System.currentTimeMillis()
                                            val targetEndTime = prefs.getLong("rest_end_${agent.id}", 0L)
                                            if (refreshedNow >= targetEndTime) {
                                                dao.insertAgent(refreshedAgent.copy(status = "Active"))
                                                prefs.edit().remove("rest_end_${agent.id}").apply()
                                                dao.insertMemory(
                                                    ColonyMemory(
                                                        content = "Agent ${agent.name} completed rest and is now Active."
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
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
                                    this@AgentRestSchedulerService,
                                    agent.id,
                                    "Agent Resting",
                                    "${agent.name} is overworked and is now resting."
                                )
                                Log.d("AgentRestScheduler", "Agent ${agent.name} sent to rest.")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AgentRestScheduler", "Scheduler Service Destroyed")
        serviceJob.cancel()
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
