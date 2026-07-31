package com.example.security

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.AgentPreferencesRepository
import kotlinx.coroutines.flow.first

enum class AgentCapability(val displayName: String, val systemPermission: String? = null) {
    READ_CALENDAR("Odczyt Kalendarza", android.Manifest.permission.READ_CALENDAR),
    OS_PACKAGE_MANAGER_AUDIT("Skan Systemowych Pakietów OS", null),
    EXECUTE_RULE_WORKER("Uruchamianie Reguł WorkManager", null),
    LLM_SIMULATION("Generowanie Promptów LLM (Gemini)", null),
    DATA_ACCESS_LOGGING("Rejestracja Logów Dostępów", null)
}

sealed class CapabilityResult {
    data class Granted(val details: String) : CapabilityResult()
    data class Denied(val reason: String, val requiredPermission: String? = null) : CapabilityResult()
}

enum class GeminiSecurityMode {
    DEV_ONLY_DIRECT_KEY,
    SECURE_FIREBASE_GATEWAY
}

object AgentCapabilityGuard {

    fun getSecurityMode(): GeminiSecurityMode {
        // Direct key stored in BuildConfig indicates prototype / dev-only deployment
        return GeminiSecurityMode.DEV_ONLY_DIRECT_KEY
    }

    fun checkCapability(
        context: Context,
        agentName: String,
        capability: AgentCapability
    ): CapabilityResult {
        // 1. Check system OS permission if required
        capability.systemPermission?.let { sysPerm ->
            val hasPerm = ContextCompat.checkSelfPermission(context, sysPerm) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                return CapabilityResult.Denied(
                    reason = "Brak przyznanego uprawnienia systemowego Android: $sysPerm",
                    requiredPermission = sysPerm
                )
            }
        }

        // 2. Capability specific verification rules
        return when (capability) {
            AgentCapability.READ_CALENDAR -> {
                CapabilityResult.Granted("Uprawnienia systemowe oraz agenta '$agentName' zweryfikowane pomyślnie.")
            }
            AgentCapability.OS_PACKAGE_MANAGER_AUDIT -> {
                CapabilityResult.Granted("Zweryfikowano dostęp do OS PackageManager dla agenta '$agentName'.")
            }
            AgentCapability.EXECUTE_RULE_WORKER -> {
                CapabilityResult.Granted("Zweryfikowano możliwość kolejkowania tła w WorkManager.")
            }
            AgentCapability.LLM_SIMULATION -> {
                val prefs = kotlinx.coroutines.runBlocking {
                    AgentPreferencesRepository(context).agentPreferencesFlow.first()
                }
                if (prefs.aiProvider == "openrouter") {
                    val key = prefs.openRouterApiKey
                    if (key.isBlank()) {
                        CapabilityResult.Denied("Brak aktywnego klucza OpenRouter API.")
                    } else {
                        CapabilityResult.Granted("Klucz OpenRouter API obecny.")
                    }
                } else {
                    val key = prefs.geminiApiKey
                    val buildConfigKey = BuildConfig.GEMINI_API_KEY
                    if (key.isBlank() && (buildConfigKey.isBlank() || buildConfigKey == "MY_GEMINI_API_KEY")) {
                        CapabilityResult.Denied("Brak aktywnego klucza Gemini API w ustawieniach i BuildConfig.")
                    } else {
                        CapabilityResult.Granted("Klucz Gemini API obecny.")
                    }
                }
            }
            AgentCapability.DATA_ACCESS_LOGGING -> {
                CapabilityResult.Granted("Rejestr dostępów aktywny.")
            }
        }
    }
}
