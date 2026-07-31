package com.example.utils

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object LocalTranslator {
    private const val TAG = "LocalTranslator"

    private val offlineDictionary = mapOf(
        "active" to "Aktywny",
        "idle" to "Bezczynny",
        "resting" to "Odpoczywa",
        "syncing" to "Synchronizuje",
        "halted" to "Zatrzymany",
        "sentinel agent" to "Agent Strażnik",
        "monitor permissions and guard data privacy." to "Monitoruj uprawnienia i chroń prywatność danych.",
        "synthesis designer" to "Projektant Syntezy",
        "optimize visual layout and creative agent workflows." to "Optymalizuj układ wizualny i kreatywne przepływy pracy agentów.",
        "quantum refactorer" to "Kwantowy Refaktoryzator",
        "maintain zero latency and clean subtask execution." to "Utrzymuj zerowe opóźnienia i czyste wykonanie podzadań.",
        "omni governor" to "Omni Zarządca",
        "coordinate multi-agent consensus and policy enforcement." to "Koordynuj konsensus wieloagentowy i egzekwowanie zasad.",
        "enqueued ruleevaluatorworker task via workmanager." to "Zakolejkowano zadanie RuleEvaluatorWorker przez WorkManager.",
        "complete your first multi-agent workflow mission." to "Ukończ swoją pierwszą misję wieloagentową.",
        "successfully complete 3 multi-agent colony missions." to "Pomyślnie ukończ 3 misje wieloagentowe w kolonii.",
        "record 5 transparent decisions in the colony ledger." to "Zapisz 5 przejrzystych decyzji w rejestrze kolonii.",
        "assemble an active colony with 5 or more agents." to "Zgromadź aktywną kolonię z 5 lub więcej agentami.",
        "complete 10 individual agent subtasks." to "Ukończ 10 indywidualnych podzadań agentów.",
        "set at least 2 agents to full autonomous mode." to "Ustaw co najmniej 2 agenty w pełny tryb autonomiczny.",
        "execute a workflow involving 3+ distinct specialized agents." to "Wykonaj przepływ pracy angażujący 3+ różne wyspecjalizowane agenty.",
        "sync calendar events & compute gemini predictive schedules." to "Synchronizuj wydarzenia z kalendarza i obliczaj predykcyjne harmonogramy Gemini.",
        "maintain active agent operations with 0 policy violations." to "Utrzymuj aktywne operacje agentów przy zerowych naruszeniach zasad.",
        "configure custom system prompts for your colony agents." to "Skonfiguruj niestandardowe prompty systemowe dla agentów kolonii.",
        "all task status checked. colony in perfect synergy." to "Sprawdzono stany wszystkich zadań. Kolonia w doskonałej synergii.",
        "colony integrity audit completed with no violations." to "Zakończono audyt integralności kolonii bez naruszeń."
    )

    fun translateOffline(text: String, sourceLang: String = "en", targetLang: String = "pl", onComplete: (String, Boolean) -> Unit) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            onComplete("", true)
            return
        }

        try {
            val sourceCode = when (sourceLang.lowercase()) {
                "pl" -> TranslateLanguage.POLISH
                "de" -> TranslateLanguage.GERMAN
                "es" -> TranslateLanguage.SPANISH
                else -> TranslateLanguage.ENGLISH
            }
            val targetCode = when (targetLang.lowercase()) {
                "pl" -> TranslateLanguage.POLISH
                "de" -> TranslateLanguage.GERMAN
                "es" -> TranslateLanguage.SPANISH
                else -> TranslateLanguage.ENGLISH
            }

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()

            val translator = Translation.getClient(options)
            val conditions = DownloadConditions.Builder().build()

            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    translator.translate(trimmed)
                        .addOnSuccessListener { translatedText ->
                            onComplete(translatedText, true)
                            translator.close()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "ML Kit Translate failed, using backup dictionary", e)
                            val fallback = getDictionaryTranslation(trimmed, sourceLang, targetLang)
                            onComplete(fallback, false)
                            translator.close()
                        }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "ML Kit models not downloaded yet, using backup dictionary")
                    val fallback = getDictionaryTranslation(trimmed, sourceLang, targetLang)
                    onComplete(fallback, false)
                    translator.close()
                }
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit init error, using backup dictionary", e)
            val fallback = getDictionaryTranslation(trimmed, sourceLang, targetLang)
            onComplete(fallback, false)
        }
    }

    private fun getDictionaryTranslation(text: String, sourceLang: String, targetLang: String): String {
        val lowerText = text.lowercase().trim()
        
        if (sourceLang == "pl" && targetLang == "en") {
            for ((enKey, plValue) in offlineDictionary) {
                if (lowerText == plValue.lowercase()) {
                    return enKey.replaceFirstChar { it.uppercase() }
                }
            }
            return "$text (EN)"
        }

        val matched = offlineDictionary[lowerText]
        if (matched != null) {
            return matched
        }

        for ((enKey, plValue) in offlineDictionary) {
            if (lowerText.contains(enKey)) {
                return text.replace(enKey, plValue, ignoreCase = true)
            }
        }

        return "$text (PL)"
    }
}
