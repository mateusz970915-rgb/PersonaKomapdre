package com.example.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.BuildConfig
import com.example.data.DataAccessRequest
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class PrivacyAuditor(private val context: Context) {

    suspend fun performAudit(activeAgentCount: Int): PrivacyAuditResult {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        val thirdPartyApps = packages.filter {
            ((it.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM) == 0
        }.take(10)

        val appPermSummary = thirdPartyApps.joinToString("\n") { pkg ->
            val perms = pkg.requestedPermissions?.joinToString(", ") ?: "None"
            "App: ${pkg.packageName}, Permissions: $perms"
        }

        val prompt = """
            You are a strict Privacy Sentinel auditor.
            Review the following third-party Android applications installed on the device and their requested permissions.
            System state: $activeAgentCount active colony agents monitoring privacy.
            Installed Third-Party Apps (Sample):
            $appPermSummary

            Respond ONLY with a short, professional assessment string (max 2 sentences) indicating if there are security risks, suspicious permission combinations, or if the system appears safe. Do not include JSON or formatting.
        """.trimIndent()

        val auditResultText = try {
            val prefs = com.example.data.AgentPreferencesRepository(context).agentPreferencesFlow.first()
            val isConfigured = if (prefs.aiProvider == "openrouter") {
                prefs.openRouterApiKey.isNotBlank()
            } else {
                prefs.geminiApiKey.isNotBlank() || (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")
            }
            if (!isConfigured) {
                "Audit failed: LLM Engine Offline. No API Key provided."
            } else {
                com.example.network.AILlmClient.generateContent(context, prompt)
            }
        } catch (e: Exception) {
            "Audit Error: ${e.message}"
        }

        val isViolationFlag = auditResultText.contains("risk", ignoreCase = true) ||
                auditResultText.contains("suspicious", ignoreCase = true) ||
                auditResultText.contains("violation", ignoreCase = true) ||
                auditResultText.contains("anomaly", ignoreCase = true)

        val localScanLog = DataAccessRequest(
            agentName = "Privacy Sentinel (Local)",
            dataType = "[Lokalny Skan OS] PackageManager",
            isPolicyViolation = false,
            violationReason = "Lokalnie odczytano ${thirdPartyApps.size} pakietów aplikacji z systemowego PackageManager (bez transmisji sieciowej)."
        )

        val cloudEvaluationLog = DataAccessRequest(
            agentName = "Privacy Sentinel (Cloud AI)",
            dataType = "[Analiza Chmurowa Gemini] API Call",
            isPolicyViolation = isViolationFlag,
            violationReason = "Przesłano próbkę ${thirdPartyApps.size} aplikacji do Gemini API w celu analizy bezpieczeństwa. Wynik AI: ${auditResultText.trim()}"
        )

        return PrivacyAuditResult(localScanLog, cloudEvaluationLog)
    }
}

data class PrivacyAuditResult(
    val localScanLog: DataAccessRequest,
    val cloudEvaluationLog: DataAccessRequest
)
