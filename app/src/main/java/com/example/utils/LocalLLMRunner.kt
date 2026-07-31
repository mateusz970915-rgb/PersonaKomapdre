package com.example.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object LocalLLMRunner {
    private var isModelLoaded = false

    // Simulating loading of Gemma 2B via MediaPipe or llama.cpp (JNI)
    suspend fun loadModel(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            Log.d("LocalLLMRunner", "Sandbox: Loading Gemma 2B weights (Simulated)...")
            delay(1500) // Simulated IO load time
            isModelLoaded = true
            true
        }
    }

    suspend fun generateResponse(prompt: String): String {
        return withContext(Dispatchers.IO) {
            if (!isModelLoaded) {
                return@withContext "[ERROR] Local model not loaded. Please load weights first."
            }
            Log.d("LocalLLMRunner", "Sandbox: Generating response for prompt: ${prompt.take(50)}...")
            delay(800) // Simulated inference time
            "[ON-DEVICE GEMMA 2B] Zrozumiałem: '$prompt'. To jest symulowana odpowiedź offline działająca całkowicie na urządzeniu (JNI/MediaPipe Engine)."
        }
    }
}
