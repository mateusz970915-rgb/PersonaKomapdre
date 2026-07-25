package com.example.worker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.CouncilMessage
import com.example.data.RuleNodeEntity
import kotlinx.coroutines.flow.firstOrNull

class RuleEvaluatorWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val db = AppDatabase.getDatabase(context)
            val dao = db.colonyDao()
            
            val nodes = dao.getAllRuleNodes().firstOrNull() ?: return Result.success()
            val connections = dao.getAllRuleConnections().firstOrNull() ?: return Result.success()

            val triggers = nodes.filter { it.nodeType == "TRIGGER" || it.nodeType == "trigger" || it.nodeType == "Trigger" }
            
            for (trigger in triggers) {
                if (isTriggerConditionMet(trigger.text)) {
                    val outgoingConnections = connections.filter { it.fromId == trigger.id }
                    for (conn in outgoingConnections) {
                        val actionNode = nodes.find { it.id == conn.toId }
                        if (actionNode != null) {
                            executeAction(actionNode, dao)
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun isTriggerConditionMet(conditionText: String): Boolean {
        val lowerText = conditionText.lowercase()
        if (lowerText.contains("battery") || lowerText.contains("bateria")) {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                context.registerReceiver(null, ifilter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()
            
            val numberMatch = Regex("\\d+").find(conditionText)
            if (numberMatch != null) {
                val threshold = numberMatch.value.toInt()
                if (lowerText.contains("<") || lowerText.contains("low")) {
                    return batteryPct < threshold
                }
                if (lowerText.contains(">") || lowerText.contains("high")) {
                    return batteryPct > threshold
                }
            }
            // fallback
            return batteryPct < 20
        }
        
        if (lowerText.contains("screen time") || lowerText.contains("czas przed ekranem")) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val time = System.currentTimeMillis()
            val usageEvents = usageStatsManager.queryEvents(time - 1000 * 60 * 60 * 24, time)
            
            var totalScreenTimeMs = 0L
            var lastEventTime = 0L
            val event = android.app.usage.UsageEvents.Event()
            
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == android.app.usage.UsageEvents.Event.SCREEN_INTERACTIVE) {
                    lastEventTime = event.timeStamp
                } else if (event.eventType == android.app.usage.UsageEvents.Event.SCREEN_NON_INTERACTIVE) {
                    if (lastEventTime > 0) {
                        totalScreenTimeMs += (event.timeStamp - lastEventTime)
                        lastEventTime = 0L
                    }
                }
            }
            if (lastEventTime > 0) {
                totalScreenTimeMs += (time - lastEventTime)
            }
            
            val totalScreenTimeHours = totalScreenTimeMs / (1000f * 60f * 60f)

            val numberMatch = Regex("\\d+").find(conditionText)
            if (numberMatch != null) {
                val threshold = numberMatch.value.toInt()
                if (lowerText.contains(">") || lowerText.contains("more")) {
                    return totalScreenTimeHours > threshold
                }
                if (lowerText.contains("<") || lowerText.contains("less")) {
                    return totalScreenTimeHours < threshold
                }
            }
            return totalScreenTimeHours > 2 // default 2 hours
        }
        
        // Default false if we don't understand the trigger
        return false
    }

    private suspend fun executeAction(actionNode: RuleNodeEntity, dao: com.example.data.ColonyDao) {
        val lowerText = actionNode.text.lowercase()
        
        if (lowerText.contains("notify") || lowerText.contains("powiadom")) {
            val msg = CouncilMessage(
                role = "system",
                content = "Rule triggered: ${actionNode.text}",
                timestamp = System.currentTimeMillis()
            )
            dao.insertMessage(msg)
        } else if (lowerText.contains("task") || lowerText.contains("zadanie")) {
             val subTask = com.example.data.SubTask(
                missionId = null,
                assignedAgent = "System",
                description = actionNode.text,
                status = "Pending"
             )
             dao.insertSubTask(subTask)
        } else {
             val msg = CouncilMessage(
                role = "system",
                content = "Rule Action Executed: ${actionNode.text}",
                timestamp = System.currentTimeMillis()
            )
            dao.insertMessage(msg)
        }
    }
}
