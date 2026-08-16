package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AgentPreferencesRepository
import com.example.network.AILlmClient

class MemorySnapshotWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext
        val prefRepo = AgentPreferencesRepository(app)

        val activityPrompt = "Summarize today's agent activity into a short daily digest."

        try {
            val content = AILlmClient.generateContent(
                context = app,
                prompt = activityPrompt,
                systemInstruction = "You are an AI generating daily memory snapshots for agents. Be concise and actionable."
            )
            prefRepo.updateDailyMemorySnapshot(content)
        } catch (e: Exception) {
            prefRepo.updateDailyMemorySnapshot("Błąd pobierania podsumowania: \${e.message}")
        }

        return Result.success()
    }
}
