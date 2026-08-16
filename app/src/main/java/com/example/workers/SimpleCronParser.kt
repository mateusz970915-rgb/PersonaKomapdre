package com.example.workers

import java.util.Calendar

object SimpleCronParser {
    // Basic parser for "minute hour * * *"
    // Returns delay in milliseconds until next execution
    fun calculateDelayToNextRun(cronExpression: String): Long {
        try {
            val parts = cronExpression.split(" ")
            if (parts.size >= 2) {
                val minuteStr = parts[0]
                val hourStr = parts[1]
                
                val now = Calendar.getInstance()
                val nextRun = Calendar.getInstance()
                
                if (minuteStr != "*" && hourStr != "*") {
                    val targetMinute = minuteStr.toInt()
                    val targetHour = hourStr.toInt()
                    
                    nextRun.set(Calendar.HOUR_OF_DAY, targetHour)
                    nextRun.set(Calendar.MINUTE, targetMinute)
                    nextRun.set(Calendar.SECOND, 0)
                    nextRun.set(Calendar.MILLISECOND, 0)
                    
                    if (nextRun.before(now)) {
                        nextRun.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    return nextRun.timeInMillis - now.timeInMillis
                } else if (minuteStr != "*" && hourStr == "*") {
                    // e.g. "15 * * * *" -> every hour at minute 15
                    val targetMinute = minuteStr.toInt()
                    nextRun.set(Calendar.MINUTE, targetMinute)
                    nextRun.set(Calendar.SECOND, 0)
                    nextRun.set(Calendar.MILLISECOND, 0)
                    if (nextRun.before(now)) {
                        nextRun.add(Calendar.HOUR_OF_DAY, 1)
                    }
                    return nextRun.timeInMillis - now.timeInMillis
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Fallback: 1 hour delay
        return 60 * 60 * 1000L
    }
}
