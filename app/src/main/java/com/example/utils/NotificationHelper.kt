package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "colony_agent_rest_channel"
    private const val CHANNEL_NAME = "Colony Agent Rest Alerts"
    private const val CHANNEL_DESC = "Notifications for overworked agent resting periods and recoveries"

    private const val MISSION_CHANNEL_ID = "colony_high_priority_mission_channel"
    private const val MISSION_CHANNEL_NAME = "High-Priority Mission Alerts"
    private const val MISSION_CHANNEL_DESC = "System alerts when agents complete high-priority colony missions"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val restChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_DESC
            }
            notificationManager.createNotificationChannel(restChannel)

            val missionChannel = NotificationChannel(MISSION_CHANNEL_ID, MISSION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = MISSION_CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(missionChannel)
        }
    }

    fun sendHighPriorityMissionNotification(context: Context, missionId: Int, agentName: String, missionGoal: String) {
        createNotificationChannel(context)
        try {
            val builder = NotificationCompat.Builder(context, MISSION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🔥 High-Priority Mission Completed!")
                .setContentText("Agent $agentName successfully finished: '$missionGoal'")
                .setStyle(NotificationCompat.BigTextStyle().bigText("Agent '$agentName' completed high-priority mission:\n'$missionGoal'"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val manager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    manager.notify(missionId + 10000, builder.build())
                }
            } else {
                manager.notify(missionId + 10000, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createForegroundNotification(context: Context, title: String, message: String): android.app.Notification {
        createNotificationChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun sendNotification(context: Context, id: Int, title: String, message: String) {
        createNotificationChannel(context)

        // Ensure we check and don't crash if permission is missing on Android 13+
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Safe built-in system icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val manager = NotificationManagerCompat.from(context)
            // Note: Since we are running in the context of the service, check permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    manager.notify(id, builder.build())
                }
            } else {
                manager.notify(id, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
