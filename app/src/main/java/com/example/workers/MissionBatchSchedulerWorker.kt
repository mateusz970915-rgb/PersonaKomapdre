package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

class MissionBatchSchedulerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val batchName = inputData.getString("BATCH_NAME") ?: "Unknown Batch"
        val cronExpression = inputData.getString("CRON_EXPRESSION") ?: "0 8 * * *"

        Log.i("MissionBatchWorker", "Executing Mission Batch: \$batchName scheduled via CRON: \$cronExpression")
        
        // In a real scenario, we would retrieve tasks in the batch and execute them here.
        // For demonstration, we just log and reschedule.
        
        scheduleNextRun(applicationContext, batchName, cronExpression)

        return Result.success()
    }

    companion object {
        fun scheduleNextRun(context: Context, batchName: String, cronExpression: String) {
            val delayMs = SimpleCronParser.calculateDelayToNextRun(cronExpression)
            
            val inputData = Data.Builder()
                .putString("BATCH_NAME", batchName)
                .putString("CRON_EXPRESSION", cronExpression)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<MissionBatchSchedulerWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("MissionBatch_\$batchName")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "MissionBatch_\${batchName}_Unique",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            Log.d("MissionBatchWorker", "Scheduled next run for \$batchName in \${delayMs / 1000} seconds")
        }
    }
}
