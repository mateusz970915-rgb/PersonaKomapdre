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
import com.example.data.AppDatabase
import com.example.data.Badge
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MilestoneTrackerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.colonyDao()
            
            // Get all subtasks, decisions, logs to calculate streak
            val subTasks = dao.getAllSubTasks().first()
            val decisions = dao.getAgentDecisions().first()
            val missionLogs = dao.getAllMissionStateLogs().first()
            
            val allTimestamps = mutableListOf<Long>()
            subTasks.forEach { allTimestamps.add(it.timestamp) }
            decisions.forEach { allTimestamps.add(it.timestamp) }
            missionLogs.forEach { allTimestamps.add(it.timestamp) }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val distinctDays = allTimestamps.map { dateFormat.format(Date(it)) }.distinct().sortedDescending()
            
            // Calculate current streak
            var streak = 0
            val calendar = Calendar.getInstance()
            
            for (i in 0..100) {
                val checkDate = dateFormat.format(calendar.time)
                if (distinctDays.contains(checkDate)) {
                    streak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    // if it's today and we don't have activity, check yesterday to not break a streak just because today just started
                    if (i == 0) {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        val checkYesterday = dateFormat.format(calendar.time)
                        if (distinctDays.contains(checkYesterday)) {
                            streak++
                            calendar.add(Calendar.DAY_OF_YEAR, -1)
                            continue
                        }
                    }
                    break
                }
            }

            // Ensure badges exist
            val existingBadges = dao.getAllBadges().first()
            
            val streakBadges = listOf(
                Badge("streak_3", "3-Day Streak", "Interact with your agents for 3 consecutive days.", "Productivity", "local_fire_department", false, 0L, streak, 3),
                Badge("streak_7", "7-Day Streak", "Interact with your agents for a full week.", "Productivity", "local_fire_department", false, 0L, streak, 7),
                Badge("streak_30", "30-Day Streak", "A whole month of agent interactions!", "Productivity", "local_fire_department", false, 0L, streak, 30)
            )

            var newlyUnlocked = 0
            
            for (badgeDef in streakBadges) {
                val existing = existingBadges.find { it.id == badgeDef.id }
                val progress = minOf(streak, badgeDef.maxProgress)
                val isUnlocked = progress >= badgeDef.maxProgress
                
                if (existing == null) {
                    dao.insertBadge(badgeDef.copy(
                        currentProgress = progress,
                        isUnlocked = isUnlocked,
                        unlockedAt = if (isUnlocked) System.currentTimeMillis() else 0L
                    ))
                    if (isUnlocked) newlyUnlocked++
                } else {
                    if (!existing.isUnlocked && isUnlocked) {
                        dao.insertBadge(existing.copy(
                            currentProgress = progress,
                            isUnlocked = true,
                            unlockedAt = System.currentTimeMillis()
                        ))
                        newlyUnlocked++
                    } else if (!existing.isUnlocked) {
                         dao.insertBadge(existing.copy(
                            currentProgress = progress
                        ))
                    }
                }
            }

            if (newlyUnlocked > 0) {
                showNotification(newlyUnlocked)
            }
            
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun showNotification(unlockedCount: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "milestone_badge_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Milestone Achievements",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.parse("colony://badges") 
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setContentTitle("New Milestone Reached!")
            .setContentText("You unlocked $unlockedCount new activity streak badge(s)!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1004, notification)
    }
}
