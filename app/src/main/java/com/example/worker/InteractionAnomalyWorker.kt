package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.AgentInteractionLogger
import com.example.data.AgentPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlin.math.abs

/**
 * WorkManager task that checks for significant interaction spikes or drops
 * based on user-defined thresholds in DataStore and triggers a local notification
 * if a change exceeding the threshold is detected.
 */
class InteractionAnomalyWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefsRepository = AgentPreferencesRepository(applicationContext)
            val prefs = prefsRepository.agentPreferencesFlow.first()

            if (!prefs.trendAlertsEnabled) {
                return Result.success()
            }

            val logger = AgentInteractionLogger(applicationContext)
            val interactions = logger.getAllInteractions().first()

            val intervalDays = when (prefs.trendComparisonInterval) {
                "Daily" -> 1
                "Monthly" -> 30
                else -> 7 // "Weekly"
            }

            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            val currentPeriodStart = now - (intervalDays * oneDayMs)
            val previousPeriodStart = now - (2 * intervalDays * oneDayMs)

            val curDailyCounts = FloatArray(intervalDays) { 0f }
            val prevDailyCounts = FloatArray(intervalDays) { 0f }

            interactions.forEach { record ->
                if (record.timestamp in currentPeriodStart..now) {
                    val idx = (((now - record.timestamp) / oneDayMs).toInt()).coerceIn(0, intervalDays - 1)
                    curDailyCounts[idx] += 1f
                } else if (record.timestamp in previousPeriodStart until currentPeriodStart) {
                    val idx = (((currentPeriodStart - record.timestamp) / oneDayMs).toInt()).coerceIn(0, intervalDays - 1)
                    prevDailyCounts[idx] += 1f
                }
            }

            fun computeMetric(counts: FloatArray, method: String): Double {
                if (counts.isEmpty()) return 0.0
                return when (method) {
                    "Average" -> counts.average()
                    "Median" -> {
                        val sorted = counts.sorted()
                        val mid = sorted.size / 2
                        if (sorted.size % 2 == 1) sorted[mid].toDouble()
                        else (sorted[mid - 1] + sorted[mid]) / 2.0
                    }
                    else -> counts.sum().toDouble() // "Total Sum"
                }
            }

            val curVal = computeMetric(curDailyCounts, prefs.trendAggregationMethod)
            val prevVal = computeMetric(prevDailyCounts, prefs.trendAggregationMethod)

            val percentageChange = if (prevVal > 0.0) {
                ((curVal - prevVal) / prevVal) * 100.0
            } else if (curVal > 0.0) {
                100.0
            } else {
                0.0
            }

            val threshold = prefs.trendAlertThreshold.toDouble()
            if (abs(percentageChange) >= threshold) {
                val isSpike = percentageChange >= 0
                val formattedPct = String.format("%.1f", percentageChange)
                val formattedCur = String.format("%.1f", curVal)
                val formattedPrev = String.format("%.1f", prevVal)

                val title = if (isSpike) "Interaction Spike Detected!" else "Interaction Drop Detected!"
                val message = "Activity changed by ${if (isSpike) "+" else ""}$formattedPct% ($formattedCur vs $formattedPrev in $intervalDays-day ${prefs.trendComparisonInterval.lowercase()} period). Threshold: ${prefs.trendAlertThreshold}%."

                triggerNotification(title, message)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun triggerNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "interaction_trend_alerts_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Interaction Trend Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when interaction volume spikes or drops beyond user threshold"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.parse("colony://dashboard")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2002, notification)
    }
}
