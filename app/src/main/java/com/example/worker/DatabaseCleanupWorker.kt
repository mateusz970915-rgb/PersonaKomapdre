package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AgentPreferencesRepository
import com.example.data.AppDatabase
import com.example.data.ColonyMemory
import com.example.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker Czyszczenia i Archiwizacji Bazy (DatabaseCleanupWorker)
 * Automatically purges or archives logs and telemetry older than the configured retention policy.
 */
class DatabaseCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("DatabaseCleanupWorker", "Starting automated database cleanup & archiving...")
        return try {
            val prefsRepo = AgentPreferencesRepository(applicationContext)
            val prefs = prefsRepo.agentPreferencesFlow.first()

            val retentionDays = prefs.retentionPolicyDays
            if (retentionDays <= 0) {
                Log.d("DatabaseCleanupWorker", "Retention policy set to unlimited (0 days). Skipping cleanup.")
                return Result.success()
            }

            val cutoffMs = System.currentTimeMillis() - (retentionDays.toLong() * 24L * 3600L * 1000L)
            val database = AppDatabase.getDatabase(applicationContext)
            val colonyDao = database.colonyDao()

            // Perform archiving if enabled
            if (prefs.autoArchivingEnabled) {
                try {
                    val archiveDir = File(applicationContext.filesDir, "archives")
                    if (!archiveDir.exists()) archiveDir.mkdirs()

                    val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val archiveFile = File(archiveDir, "telemetry_archive_$timestampStr.json")
                    
                    val archiveSummary = "{\"archiveDate\":\"$timestampStr\", \"retentionDays\":$retentionDays, \"cutoffTimestamp\":$cutoffMs}"
                    archiveFile.writeText(archiveSummary)
                    Log.d("DatabaseCleanupWorker", "Archived telemetry metadata to ${archiveFile.absolutePath}")
                } catch (e: Exception) {
                    Log.w("DatabaseCleanupWorker", "Archiving error: ${e.message}")
                }
            }

            // Execute purge queries
            val deletedMesh = colonyDao.deleteTelemetryOlderThan(cutoffMs)
            val deletedLlm = colonyDao.deleteLlmTelemetryOlderThan(cutoffMs)
            val deletedRequests = colonyDao.deleteDataAccessRequestsOlderThan(cutoffMs)
            val deletedFts = colonyDao.deleteFtsInteractionsOlderThan(cutoffMs)

            val totalPurged = deletedMesh + deletedLlm + deletedRequests + deletedFts

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(cutoffMs))
            val memoryNote = "Automated Database Cleanup: Purged $totalPurged records older than $retentionDays days (Before $dateStr). Mesh: $deletedMesh, LLM: $deletedLlm, Requests: $deletedRequests, FTS: $deletedFts."

            colonyDao.insertMemory(
                ColonyMemory(
                    content = memoryNote,
                    timestamp = System.currentTimeMillis()
                )
            )

            if (prefs.notificationsEnabled) {
                NotificationHelper.sendNotification(
                    applicationContext,
                    8009,
                    "Czyszczenie Bazy Danych Completed",
                    "Usunięto $totalPurged wpisów telemetrii i logów starszych niż $retentionDays dni."
                )
            }

            Log.d("DatabaseCleanupWorker", "Database cleanup finished successfully. Purged total $totalPurged records.")
            Result.success()
        } catch (e: Exception) {
            Log.e("DatabaseCleanupWorker", "Error executing database cleanup worker", e)
            Result.failure()
        }
    }
}
