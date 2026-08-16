package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ExportDatabaseWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val agents = database.agentDao().getAllAgents().first()
            
            val jsonArray = JSONArray()
            for (agent in agents) {
                val jsonObject = JSONObject()
                jsonObject.put("id", agent.id)
                jsonObject.put("name", agent.name)
                jsonObject.put("type", agent.type)
                jsonObject.put("role", agent.role)
                jsonObject.put("status", agent.status)
                jsonObject.put("permissions", agent.permissions)
                jsonObject.put("iconName", agent.iconName)
                jsonObject.put("traits", agent.traits)
                jsonObject.put("systemPrompt", agent.systemPrompt)
                jsonObject.put("autonomyLevel", agent.autonomyLevel)
                jsonObject.put("personaDescription", agent.personaDescription)
                jsonObject.put("avatarUrl", agent.avatarUrl)
                jsonObject.put("configurationJson", agent.configurationJson)
                jsonObject.put("isFavorite", agent.isFavorite)
                jsonArray.put(jsonObject)
            }

            val exportDir = File(applicationContext.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val exportFile = File(exportDir, "agent_database_backup_${System.currentTimeMillis()}.json")
            exportFile.writeText(jsonArray.toString(4))
            
            showNotification()
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun showNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "export_reminder_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Backup Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to export your data"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.parse("colony://settings") 
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setContentTitle("Data Backup Complete")
            .setContentText("Your agents have been backed up. Consider exporting your logs externally to keep your data safe.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1003, notification)
    }
}
