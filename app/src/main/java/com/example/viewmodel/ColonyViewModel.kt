package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.Agent
import com.example.data.AppDatabase
import com.example.data.ColonyRepository
import com.example.data.ColonyMemory
import com.example.data.CouncilMessage
import com.example.data.AgentDecision
import com.example.data.DataAccessRequest
import com.example.data.Mission
import com.example.data.ExecutionOutcome
import com.example.data.ExecutionEvidence
import com.example.data.SubTask
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.InlineData
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.network.ThinkingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

import com.example.network.Tool
import com.example.utils.NotificationHelper
import kotlinx.serialization.json.JsonObject

class ColonyViewModel(application: Application) : BaseAgentViewModel(application) {
    val personaPreferences = com.example.data.PersonaMeshPreferences(application)

    private val repository: ColonyRepository = baseRepository
    
    val agents: StateFlow<List<Agent>>
    val messages: StateFlow<List<CouncilMessage>>
    val decisions: StateFlow<List<AgentDecision>>
    val missions: StateFlow<List<Mission>>
    val dataAccessRequests: StateFlow<List<DataAccessRequest>>
    val subTasks: StateFlow<List<SubTask>>
    val calendarEvents: StateFlow<List<com.example.data.CalendarEvent>>
    val badges: StateFlow<List<com.example.data.Badge>>
    val interAgentMessages: StateFlow<List<com.example.data.InterAgentMessage>>
    val agentMilestones: StateFlow<List<com.example.data.AgentMilestone>>
    val ruleNodes: StateFlow<List<com.example.data.RuleNodeEntity>>
    val ruleConnections: StateFlow<List<com.example.data.RuleConnectionEntity>>
    val completedSubTasks: StateFlow<List<SubTask>>
    val memories: StateFlow<List<ColonyMemory>>
    val missionStateLogs: StateFlow<List<com.example.data.MissionStateLog>>
    val negotiations: StateFlow<List<com.example.data.AgentNegotiationProposal>>
    val meshTelemetry: StateFlow<List<com.example.data.AgentMeshTelemetry>>
    val knowledgeEdges: StateFlow<List<com.example.data.AgentKnowledgeEdge>>
    val heuristics: StateFlow<List<com.example.data.AgentHeuristicRule>>
    val llmTelemetry: StateFlow<List<com.example.data.LlmCallTelemetry>>
    val financeTransactions: StateFlow<List<com.example.data.FinanceTransaction>>
    val customAgentDefinitions: StateFlow<List<com.example.data.CustomAgentDefinition>>
    val flashcards: StateFlow<List<com.example.data.Flashcard>>
    val interactionLogger = com.example.data.AgentInteractionLogger(application)
    val allInteractions: StateFlow<List<com.example.data.InteractionRecord>> = interactionLogger.getAllInteractions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val subscriptions: StateFlow<List<com.example.data.Subscription>>
    val sleepRecords: StateFlow<List<com.example.data.SleepRecord>>

    val chartAnnotations: StateFlow<List<com.example.data.ChartAnnotation>> = repository.colonyDao.getAllChartAnnotations().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allFtsContent: StateFlow<List<com.example.data.AgentInteractionFtsContent>> = repository.colonyDao.getAllFtsContent().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selfHealingProposals: StateFlow<List<com.example.data.SelfHealingProposal>> = repository.colonyDao.getAllSelfHealingProposals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workflowDags: StateFlow<List<com.example.data.WorkflowDag>> = repository.colonyDao.getAllWorkflowDags().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val colonyProfiles: StateFlow<List<com.example.data.ColonyProfile>> = repository.colonyDao.getAllColonyProfiles().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val vectorEmbeddingLogs: StateFlow<List<com.example.data.VectorEmbeddingLog>> = repository.colonyDao.getAllVectorEmbeddingLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val hallucinationAuditLogs: StateFlow<List<com.example.data.HallucinationAuditLog>> = repository.colonyDao.getAllHallucinationAuditLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isPrivateModeGemmaEnabled = MutableStateFlow(false)
    val isPrivateModeGemmaEnabled: StateFlow<Boolean> = _isPrivateModeGemmaEnabled

    fun togglePrivateModeGemma(enabled: Boolean) {
        _isPrivateModeGemmaEnabled.value = enabled
    }

    
    private val _ftsSearchResults = MutableStateFlow<List<com.example.data.AgentInteractionFtsContent>>(emptyList())
    val ftsSearchResults: StateFlow<List<com.example.data.AgentInteractionFtsContent>> = _ftsSearchResults

    private val _ftsSearchQuery = MutableStateFlow("")
    val ftsSearchQuery: StateFlow<String> = _ftsSearchQuery

    private val _selectedOverlayAgents = MutableStateFlow<Set<String>>(emptySet())
    val selectedOverlayAgents: StateFlow<Set<String>> = _selectedOverlayAgents

    private val _generatedPdfUri = MutableStateFlow<android.net.Uri?>(null)
    val generatedPdfUri: StateFlow<android.net.Uri?> = _generatedPdfUri

    val syncStatus: StateFlow<String> = androidx.work.WorkManager.getInstance(application)
        .getWorkInfosForUniqueWorkFlow("ExportDatabaseWork")
        .map { workInfos ->
            val info = workInfos.firstOrNull()
            when (info?.state) {
                androidx.work.WorkInfo.State.ENQUEUED -> "Pending"
                androidx.work.WorkInfo.State.RUNNING -> "Syncing"
                androidx.work.WorkInfo.State.SUCCEEDED -> "Synced"
                androidx.work.WorkInfo.State.FAILED -> "Failed"
                else -> "Unknown"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Unknown")

    val agentDataSyncStatus: StateFlow<String> = androidx.work.WorkManager.getInstance(application)
        .getWorkInfosForUniqueWorkFlow("AgentDataSyncWork")
        .map { workInfos ->
            val info = workInfos.firstOrNull()
            when (info?.state) {
                androidx.work.WorkInfo.State.ENQUEUED -> "Pending"
                androidx.work.WorkInfo.State.RUNNING -> "Syncing"
                androidx.work.WorkInfo.State.SUCCEEDED -> "Synced"
                androidx.work.WorkInfo.State.FAILED -> "Failed"
                else -> "Idle"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Idle")


    private val defaultCategoryColors = mapOf(
        "HEALTH" to "#10B981",
        "FINANCE" to "#3B82F6",
        "WORK" to "#8B5CF6",
        "SECURITY" to "#EF4444",
        "CREATIVE" to "#F59E0B",
        "ANALYTICS" to "#06B6D4",
        "GOVERNANCE" to "#6366F1",
        "GENERAL" to "#64748B"
    )

    private val _categoryColors = MutableStateFlow<Map<String, String>>(emptyMap())
    val categoryColors: StateFlow<Map<String, String>> = _categoryColors

    private fun loadCategoryColors() {
        val prefs = getApplication<Application>().getSharedPreferences("category_colors_prefs", android.content.Context.MODE_PRIVATE)
        val map = mutableMapOf<String, String>()
        defaultCategoryColors.forEach { (cat, defaultHex) ->
            map[cat] = prefs.getString("color_$cat", defaultHex) ?: defaultHex
        }
        _categoryColors.value = map
    }

    fun updateCategoryColor(category: String, hexColor: String) {
        val upperCat = category.uppercase().trim()
        val prefs = getApplication<Application>().getSharedPreferences("category_colors_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("color_$upperCat", hexColor).apply()
        
        val current = _categoryColors.value.toMutableMap()
        current[upperCat] = hexColor
        _categoryColors.value = current
    }

    fun getCategoryColorHex(category: String): String {
        val upperCat = category.uppercase().trim()
        return _categoryColors.value[upperCat] ?: defaultCategoryColors[upperCat] ?: "#64748B"
    }

    private val loggingService by lazy { com.example.service.MissionLoggingService(repository.colonyDao) }
    
    private val _newlyUnlockedBadge = MutableStateFlow<com.example.data.Badge?>(null)
    val newlyUnlockedBadge: StateFlow<com.example.data.Badge?> = _newlyUnlockedBadge

    private val _newlyUnlockedAgentMilestone = MutableStateFlow<com.example.data.AgentMilestone?>(null)
    val newlyUnlockedAgentMilestone: StateFlow<com.example.data.AgentMilestone?> = _newlyUnlockedAgentMilestone

    private val _dbIntegrityOk = MutableStateFlow(true)
    val dbIntegrityOk: StateFlow<Boolean> = _dbIntegrityOk

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _dbIntegrityOk.value = repository.checkDatabaseIntegrity()
        }
    }

    fun resetDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().deleteDatabase("colony_database")
            _dbIntegrityOk.value = true
        }
    }

    fun dismissUnlockedBadge() {
        _newlyUnlockedBadge.value = null
    }

    fun dismissUnlockedAgentMilestone() {
        _newlyUnlockedAgentMilestone.value = null
    }

    fun clearLlmTelemetry() {
        viewModelScope.launch {
            repository.clearLlmTelemetry()
        }
    }

    // Chart Annotations
    fun addChartAnnotation(note: String, tag: String = "General", colorHex: String = "#3B82F6", agentId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.insertChartAnnotation(
                com.example.data.ChartAnnotation(
                    note = note,
                    tag = tag,
                    colorHex = colorHex,
                    agentId = agentId
                )
            )
        }
    }

    fun deleteChartAnnotation(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.deleteChartAnnotation(id)
        }
    }

    // FTS5 Full Text Search
    fun searchFts(query: String) {
        _ftsSearchQuery.value = query
        if (query.isBlank()) {
            _ftsSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Formatting query for FTS MATCH syntax
                val formattedQuery = "*$query*"
                repository.colonyDao.searchFtsInteractions(formattedQuery).collect { list ->
                    _ftsSearchResults.value = list
                }
            } catch (e: Exception) {
                Log.w("ColonyViewModel", "FTS Search Error: ${e.message}")
            }
        }
    }

    fun insertFtsInteraction(agentName: String, snippet: String, modelUsed: String = "gemini-3.5-flash", tag: String = "Conversation") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.insertFtsContent(
                com.example.data.AgentInteractionFtsContent(
                    agentName = agentName,
                    snippet = snippet,
                    modelUsed = modelUsed,
                    tag = tag
                )
            )
        }
    }

    // Multi-Agent Overlay Selection
    fun toggleOverlayAgent(agentName: String) {
        val current = _selectedOverlayAgents.value.toMutableSet()
        if (current.contains(agentName)) {
            current.remove(agentName)
        } else {
            current.add(agentName)
        }
        _selectedOverlayAgents.value = current
    }

    // Database Retention & Preferences Updates
    fun updateRetentionPolicyDays(days: Int) {
        viewModelScope.launch {
            preferencesRepository.updateRetentionPolicyDays(days)
        }
    }

    fun updateAutoArchivingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoArchivingEnabled(enabled)
        }
    }

    // Failover Configuration Update
    fun updateAgentFailoverConfig(agentId: Int, failoverAgentId: Int?, maxLatencyMs: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.updateAgentFailoverConfig(agentId, failoverAgentId, maxLatencyMs)
        }
    }

    // Trigger Database Cleanup Worker
    fun triggerDatabaseCleanupNow() {
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.worker.DatabaseCleanupWorker>().build()
        androidx.work.WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }

    // Export PDF Report
    fun generatePdfReport(chartBitmap: Bitmap?) {
        viewModelScope.launch(Dispatchers.IO) {
            val agentCount = agents.value.size
            val activeMissions = missions.value.count { it.status == "In Progress" || it.status == "Active" }
            val completedTasks = completedSubTasks.value.size
            val telemetryCount = meshTelemetry.value.size

            val metricsMap = mapOf(
                "Aktywni Agenci" to "$agentCount",
                "Misje w Biegu" to "$activeMissions",
                "Wykonane Zadania" to "$completedTasks",
                "Pomiary Telemetrii" to "$telemetryCount"
            )

            val eventLogs = allFtsContent.value.take(100)

            val pdfUri = com.example.utils.PdfReportGenerator.generateReportPdf(
                context = getApplication(),
                reportTitle = "Raport Wydajności i Aktywności Agentów EDDE+",
                chartBitmap = chartBitmap,
                metrics = metricsMap,
                eventLogs = eventLogs
            )

            _generatedPdfUri.value = pdfUri
        }
    }

    fun clearGeneratedPdfUri() {
        _generatedPdfUri.value = null
    }

    fun updateDataAccessApproval(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateDataAccessApproval(id, status)
        }
    }

    fun triggerPrivacyAudit() {
        viewModelScope.launch {
            val contextApp = getApplication<Application>()
            val agentList = repository.allAgents.first()
            if (agentList.isEmpty()) return@launch
            
            val agentToAudit = agentList.random()
            
            val prompt = """
                You are a system security auditor. An agent named ${agentToAudit.name} with role ${agentToAudit.role} is requesting a new permission.
                Propose a plausible risky data access request this agent might make (e.g. "Read SMS", "Access Location").
                Return the data type requested on line 1.
                Return a short justification on line 2.
            """.trimIndent()
            
            var dataType = "Odczyt listy kontaktów / kalendarza"
            var reason = "Zasada bezpieczeństwa 'Polityka Zero Trust' wymaga bezpośredniej zgody użytkownika."
            
            try {
                val reply = com.example.network.AILlmClient.generateContent(contextApp, prompt).trim().lines()
                if (reply.isNotEmpty()) {
                    dataType = reply.first()
                    if (reply.size > 1) {
                        reason = reply[1]
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            repository.insertDataAccessRequest(
                com.example.data.DataAccessRequest(
                    agentName = agentToAudit.name,
                    dataType = dataType,
                    isPolicyViolation = true,
                    violationReason = reason,
                    requiresUserApproval = true,
                    approvalStatus = "Pending"
                )
            )
        }
    }

    private val defaultMilestones = listOf(
        com.example.data.AgentMilestone("sentinel_sentinel", "Ironclad Sentinel", "Security Overseer", "Security", 30, iconName = "verified_user", systemPrompt = "Monitor permissions and guard data privacy."),
        com.example.data.AgentMilestone("synthesis_designer", "Synthesis Designer", "Creative Catalyst", "Design", 70, iconName = "palette", systemPrompt = "Optimize visual layout and creative agent workflows."),
        com.example.data.AgentMilestone("quantum_refactorer", "Quantum Refactorer", "Code & Performance", "Engineering", 120, iconName = "code", systemPrompt = "Maintain zero latency and clean subtask execution."),
        com.example.data.AgentMilestone("omni_governor", "Omni Governor", "Colony Director", "Governance", 200, iconName = "gavel", systemPrompt = "Coordinate multi-agent consensus and policy enforcement.")
    )

    private val defaultBadges = listOf(
        com.example.data.Badge("first_synergy", "First Synergy", "Complete your first multi-agent workflow mission.", "Workflow", "hub", maxProgress = 1),
        com.example.data.Badge("master_strategist", "Master Strategist", "Successfully complete 3 multi-agent colony missions.", "Workflow", "auto_awesome", maxProgress = 3),
        com.example.data.Badge("decisive_colony", "Decisive Colony", "Record 5 transparent decisions in the colony ledger.", "Governance", "gavel", maxProgress = 5),
        com.example.data.Badge("powerhouse_colony", "Powerhouse Colony", "Assemble an active colony with 5 or more agents.", "Colony Growth", "groups", maxProgress = 5),
        com.example.data.Badge("productivity_dynamo", "Productivity Dynamo", "Complete 10 individual agent subtasks.", "Productivity", "task_alt", maxProgress = 10),
        com.example.data.Badge("autonomy_master", "Autonomy Master", "Set at least 2 agents to full autonomous mode.", "Governance", "smart_toy", maxProgress = 2),
        com.example.data.Badge("cross_collaboration", "Cross-Agent Synergy", "Execute a workflow involving 3+ distinct specialized agents.", "Workflow", "schema", maxProgress = 1),
        com.example.data.Badge("calendar_synchronizer", "Calendar Synchronizer", "Sync calendar events & compute Gemini predictive schedules.", "Productivity", "calendar_month", maxProgress = 1),
        com.example.data.Badge("zero_violations", "Ironclad Governance", "Maintain active agent operations with 0 policy violations.", "Governance", "verified_user", maxProgress = 1),
        com.example.data.Badge("colony_architect", "Colony Architect", "Configure custom system prompts for your colony agents.", "Colony Growth", "tune", maxProgress = 1)
    )
    
    data class AgentPrediction(
        val suggestedStatus: String,
        val suggestionReason: String
    )
    
    private val _predictions = MutableStateFlow<Map<Int, AgentPrediction>>(emptyMap())
    val predictions: StateFlow<Map<Int, AgentPrediction>> = _predictions
    
    private val _dailySummaryInsight = MutableStateFlow<String>("")
    val dailySummaryInsight: StateFlow<String> = _dailySummaryInsight

    private val _ruleWorkerStatus = MutableStateFlow<String>("Idle")
    val ruleWorkerStatus: StateFlow<String> = _ruleWorkerStatus

    private val executionEngine by lazy { com.example.engine.ExecutionEngine(getApplication()) }
    private val privacyAuditor by lazy { com.example.security.PrivacyAuditor(getApplication()) }
    
    fun triggerRuleEvaluatorWork() {
        viewModelScope.launch {
            _ruleWorkerStatus.value = "Queued"
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.worker.RuleEvaluatorWorker>()
                .addTag("RuleEvaluator")
                .build()
            val workManager = androidx.work.WorkManager.getInstance(getApplication())
            workManager.enqueueUniqueWork("RuleEvaluatorWork", androidx.work.ExistingWorkPolicy.REPLACE, workRequest)
            
            sendInterAgentMessage(
                senderAgentName = "Behavior Engine",
                senderRole = "Governance Rule",
                content = "Enqueued RuleEvaluatorWorker task via WorkManager.",
                topic = "Rule Execution"
            )
            
            workManager.getWorkInfoByIdFlow(workRequest.id).collect { workInfo ->
                if (workInfo != null) {
                    _ruleWorkerStatus.value = when (workInfo.state) {
                        androidx.work.WorkInfo.State.ENQUEUED -> "Queued"
                        androidx.work.WorkInfo.State.RUNNING -> "Running"
                        androidx.work.WorkInfo.State.SUCCEEDED -> "Succeeded"
                        androidx.work.WorkInfo.State.FAILED -> "Failed"
                        androidx.work.WorkInfo.State.BLOCKED -> "Blocked"
                        androidx.work.WorkInfo.State.CANCELLED -> "Cancelled"
                    }
                }
            }
        }
    }
    
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    suspend fun refreshColonyData() {
        checkBadgeMilestones()
        checkAndEnforceAgentPermissions()
    }

    private fun registerNetworkMonitoring() {
        try {
            val cm = getApplication<Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (cm != null) {
                val activeNetwork = cm.activeNetwork
                val caps = cm.getNetworkCapabilities(activeNetwork)
                _isOffline.value = caps == null || !caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)

                val request = android.net.NetworkRequest.Builder()
                    .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

                cm.registerNetworkCallback(request, object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        _isOffline.value = false
                    }
                    override fun onLost(network: android.net.Network) {
                        _isOffline.value = true
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private val _isOnboardingNeeded = MutableStateFlow<Boolean?>(null)
    val isOnboardingNeeded: StateFlow<Boolean?> = _isOnboardingNeeded

    val isApiKeyConfigured: Boolean
        get() {
            val prefs = agentPreferencesState.value
            return if (prefs.aiProvider == "openrouter") {
                prefs.openRouterApiKey.isNotBlank()
            } else {
                prefs.geminiApiKey.isNotBlank() || (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")
            }
        }

    private val _apiErrorState = MutableStateFlow<String?>(null)
    val apiErrorState: StateFlow<String?> = _apiErrorState

    fun clearApiError() {
        _apiErrorState.value = null
    }

    private val _sharedWebText = MutableStateFlow<String?>(null)
    val sharedWebText: StateFlow<String?> = _sharedWebText

    fun setSharedWebText(text: String) {
        _sharedWebText.value = text
    }

    fun clearSharedWebText() {
        _sharedWebText.value = null
    }
    
    init {

        agents = repository.allAgents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        messages = repository.councilMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        decisions = repository.allDecisions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        missions = repository.allMissions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        dataAccessRequests = repository.allDataAccessRequests.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        subTasks = repository.allSubTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        calendarEvents = repository.allCalendarEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        badges = repository.allBadges.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        interAgentMessages = repository.allInterAgentMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        agentMilestones = repository.allAgentMilestones.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        ruleNodes = repository.allRuleNodes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        ruleConnections = repository.allRuleConnections.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        completedSubTasks = repository.completedSubTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        memories = repository.allMemories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        missionStateLogs = repository.allMissionStateLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        negotiations = repository.allAgentNegotiations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        meshTelemetry = repository.allMeshTelemetry.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        knowledgeEdges = repository.allKnowledgeEdges.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        heuristics = repository.allHeuristics.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        llmTelemetry = repository.allLlmTelemetry.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        financeTransactions = repository.allFinanceTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        customAgentDefinitions = repository.allCustomAgentDefinitions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        flashcards = repository.allFlashcards.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        subscriptions = repository.allSubscriptions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        sleepRecords = repository.allSleepRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


        loadCategoryColors()
        
        registerNetworkMonitoring()

        viewModelScope.launch {
            _isOnboardingNeeded.value = repository.getAgentCount() == 0
            
            if (repository.getBadgeCount() == 0) {
                repository.insertBadges(defaultBadges)
            }

            if (repository.getAgentMilestoneCount() == 0) {
                repository.insertAgentMilestones(defaultMilestones)
            }

            if (repository.getRuleNodeCount() == 0) {
                val seedNodes = listOf(
                    com.example.data.RuleNodeEntity("node_1", 100f, 300f, "TRIGGER", "Screen Time > 2h"),
                    com.example.data.RuleNodeEntity("node_2", 600f, 300f, "ACTION", "Notify Rest Agent")
                )
                repository.insertRuleNodes(seedNodes)
                repository.insertRuleConnection(com.example.data.RuleConnectionEntity(fromId = "node_1", toId = "node_2"))
            }


            

            
            // Check rest periods
            applyRestPeriods()
            startHeartbeat()
            checkInteractionFrequency()
            
            // Perform one-time check on initial agent list safely without infinite flow collection loops
            val currentAgentsList = repository.allAgents.first()
            currentAgentsList.forEach { ag ->
                if (ag.statusNotes.isBlank()) {
                    val initialNotes = "[Today 08:00] Initialized in Colony. Feeling aligned and active.\n[Today 09:30] Collaborating smoothly on task delegation."
                    repository.updateAgentStatusNotes(ag.id, initialNotes)
                }
            }
            checkAndEnforceAgentPermissions()
        }
    }

    fun appendAgentStatusNoteSnippet(agentId: Int, snippet: String) {
        viewModelScope.launch {
            val target = agents.value.find { it.id == agentId }
            if (target != null) {
                val dateStr = java.text.SimpleDateFormat("MMM dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                val existing = target.statusNotes
                val newNotes = if (existing.isBlank()) "[$dateStr] $snippet" else "$existing\n[$dateStr] $snippet"
                repository.updateAgentStatusNotes(agentId, newNotes)
            }
        }
    }

    fun updateAgent(agent: Agent) {
        viewModelScope.launch {
            repository.insertAgent(agent)
        }
    }

    fun updateAgentStatusNotesDirectly(agentId: Int, notes: String) {
        viewModelScope.launch {
            repository.updateAgentStatusNotes(agentId, notes)
        }
    }

    fun generateInteractionNote(agentId: Int) {
        viewModelScope.launch {
            val agent = agents.value.find { it.id == agentId } ?: return@launch
            val otherAgents = agents.value.filter { it.id != agentId }
            val partnerName = if (otherAgents.isNotEmpty()) otherAgents.random().name else "Colony Core"
            
            val contextApp = getApplication<Application>()
            val prompt = """
                You are ${agent.name} (${agent.role}). Write a brief 1-sentence log note about your recent interaction or collaboration with $partnerName.
                Express a clear mood (e.g. energized, confident, frustrated). Keep it concise and technical but expressive.
            """.trimIndent()
            
            val interactionSnippet = try {
                com.example.network.AILlmClient.generateContent(contextApp, prompt).trim()
            } catch (e: Exception) {
                "Collaborating smoothly on joint resource allocation with $partnerName."
            }
            
            appendAgentStatusNoteSnippet(agentId, interactionSnippet)
        }
    }
    
    fun setupColony(selectedGoals: List<String>) {
        viewModelScope.launch {
            if (repository.getAgentCount() == 0) {
                if (selectedGoals.contains("Health")) {
                    repository.insertAgent(Agent(name = "Health Agent", type = "Health", role = "Monitors sleep, movement, regeneration. Proposes breaks.", permissions = "Sensors, Calendar", iconName = "health"))
                    repository.insertAgent(Agent(name = "Rest Agent", type = "Rest", role = "Controls work-life balance, detects over-usage.", permissions = "Usage Stats, Do Not Disturb", iconName = "rest"))
                }
                if (selectedGoals.contains("Work")) {
                    repository.insertAgent(Agent(name = "Work Agent", type = "Work", role = "Organizes tasks and meetings, analyzes deadlines.", permissions = "Calendar, Notifications", iconName = "work"))
                }
                if (selectedGoals.contains("Study")) {
                    repository.insertAgent(Agent(name = "Study Agent", type = "Study", role = "Creates study plans, generates lessons, organizes reviews.", permissions = "Calendar, Files", iconName = "study"))
                }
                if (selectedGoals.contains("Finance")) {
                    repository.insertAgent(Agent(name = "Finance Agent", type = "Finance", role = "Analyzes daily expenses, watches budgets.", permissions = "Banking, Notifications", iconName = "finance"))
                }
                if (selectedGoals.contains("Relationships")) {
                    repository.insertAgent(Agent(name = "Relationship Agent", type = "Relationship", role = "Reminds about important people, proposes meetings.", permissions = "Contacts, Messages", iconName = "relationship"))
                }
                
                // Core agents always present
                repository.insertAgent(Agent(name = "Privacy Agent", type = "Privacy", role = "Controls access, detects excessive app permissions.", permissions = "System Settings", iconName = "privacy"))
                repository.insertAgent(Agent(name = "Security Agent", type = "Security", role = "Detects phishing, controls installations.", permissions = "Network, Storage", iconName = "security"))
                
                _isOnboardingNeeded.value = false
            }
        }
    }
    
    fun addAgent(agent: Agent) {
        viewModelScope.launch {
            repository.insertAgent(agent)
        }
    }

    override fun deleteAgent(agentId: Int) {
        viewModelScope.launch {
            repository.deleteAgentById(agentId)
        }
    }

    fun deleteAgents(agentIds: Set<Int>) {
        viewModelScope.launch {
            agentIds.forEach {
                repository.deleteAgentById(it)
            }
        }
    }

    fun updateAgentConfig(agentId: Int, permissions: String, systemPrompt: String) {
        viewModelScope.launch {
            val agent = agents.value.find { it.id == agentId } ?: return@launch
            repository.insertAgent(agent.copy(permissions = permissions, systemPrompt = systemPrompt))
        }
    }

    fun updateAgentDetails(agentId: Int, name: String, type: String) {
        viewModelScope.launch {
            val agent = agents.value.find { it.id == agentId } ?: return@launch
            repository.insertAgent(agent.copy(name = name, type = type))
        }
    }

    fun toggleAgentStatus(agent: Agent) {
        viewModelScope.launch {
            val newStatus = if (agent.status == "Active") "Paused" else "Active"
            repository.insertAgent(agent.copy(status = newStatus))
        }
    }

    fun toggleAgentFavorite(agent: Agent) {
        viewModelScope.launch {
            repository.insertAgent(agent.copy(isFavorite = !agent.isFavorite))
        }
    }

    fun bulkPauseAgents(agentsList: List<Agent>, pause: Boolean) {
        viewModelScope.launch {
            agentsList.forEach { agent ->
                val newStatus = if (pause) "Paused" else "Active"
                repository.insertAgent(agent.copy(status = newStatus))
            }
        }
    }

    fun bulkDeleteAgents(agentIds: List<Int>) {
        viewModelScope.launch {
            agentIds.forEach { id ->
                repository.deleteAgentById(id)
            }
        }
    }
    
    fun restoreAgents(agentsList: List<Agent>) {
        viewModelScope.launch {
            agentsList.forEach { agent ->
                repository.insertAgent(agent)
            }
        }
    }

    fun bulkUpdateAutonomyAndPermissions(agentsList: List<Agent>, autonomy: String, permissions: String, priority: String) {
        viewModelScope.launch {
            agentsList.forEach { agent ->
                val configObj = try { org.json.JSONObject(agent.configurationJson) } catch (e: Exception) { org.json.JSONObject() }
                configObj.put("priority", priority)
                repository.insertAgent(agent.copy(autonomyLevel = autonomy, permissions = permissions, configurationJson = configObj.toString()))
            }
        }
    }

    fun bulkAddTags(agentsList: List<Agent>, prefix: String, suffix: String) {
        viewModelScope.launch {
            agentsList.forEach { agent ->
                val currentTraits = agent.traits.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                if (prefix.isNotBlank()) currentTraits.add(0, prefix.trim())
                if (suffix.isNotBlank()) currentTraits.add(suffix.trim())
                val newTraits = currentTraits.distinct().joinToString(", ")
                repository.insertAgent(agent.copy(traits = newTraits))
            }
        }
    }

    fun addAgent(name: String, type: String, role: String, permissions: String, iconName: String = "privacy", traits: String = "", systemPrompt: String = "", autonomyLevel: String = "Needs Confirmation", personaDescription: String = "", avatarUrl: String = "") {
        viewModelScope.launch {
            repository.insertAgent(
                Agent(
                    name = name,
                    type = type,
                    role = role,
                    permissions = permissions,
                    iconName = iconName,
                    traits = traits,
                    systemPrompt = systemPrompt,
                    autonomyLevel = autonomyLevel,
                    personaDescription = personaDescription,
                    avatarUrl = avatarUrl
                )
            )
        }
    }

    fun addDecision(agentName: String, actionDescription: String, dataUsed: String, confidenceLevel: String, dissentingOpinions: String) {
        viewModelScope.launch {
            repository.insertDecision(
                AgentDecision(
                    agentName = agentName,
                    actionDescription = actionDescription,
                    dataUsed = dataUsed,
                    confidenceLevel = confidenceLevel,
                    dissentingOpinions = dissentingOpinions
                )
            )
            repository.insertMemory(ColonyMemory(content = "Decision by $agentName: $actionDescription"))
        }
    }

    fun createMission(goal: String) {
        viewModelScope.launch {
            val missionId = repository.insertMission(Mission(goal = goal)).toInt()
            val currentAgents = agents.value
            
            // Dynamically resolve agents based on registered colony personas
            val workAgentName = currentAgents.find { it.type.equals("Work", ignoreCase = true) }?.name ?: "Work Agent"
            val healthAgentName = currentAgents.find { it.type.equals("Health", ignoreCase = true) || it.type.equals("Rest", ignoreCase = true) }?.name ?: "Health Agent"
            val financeAgentName = currentAgents.find { it.type.equals("Finance", ignoreCase = true) }?.name 
                ?: currentAgents.firstOrNull { it.name != workAgentName && it.name != healthAgentName }?.name 
                ?: "Finance Agent"

            repository.insertSubTask(SubTask(missionId = missionId, assignedAgent = workAgentName, description = "Plan timeline and dependencies for: $goal"))
            repository.insertSubTask(SubTask(missionId = missionId, assignedAgent = healthAgentName, description = "Assess stress impact and schedule rest intervals"))
            repository.insertSubTask(SubTask(missionId = missionId, assignedAgent = financeAgentName, description = "Evaluate required resources and operational budget"))
            repository.insertMemory(ColonyMemory(content = "Created mission: '$goal'"))
            
            loggingService.logStateTransition(
                missionId = missionId,
                agentName = "Colony Orchestrator",
                previousState = "Pending",
                newState = "Active",
                message = "Mission '$goal' initialized with subtasks."
            )
        }
    }

    fun getSubTasksForMission(missionId: Int) = repository.getSubTasksForMission(missionId)


    // P0: Task Execution Engine - Actually execute a subtask using Gemini API

    fun executeSubTaskReal(task: com.example.data.SubTask) {
        viewModelScope.launch {
            updateSubTaskStatus(task.id, "In Progress")
            
            val agent = agents.value.find { it.name.equals(task.assignedAgent, ignoreCase = true) }
            val executionResult = executionEngine.executeTask(task, agent)

            agent?.let {
                addDecision(it.name, executionResult.logMessage, task.actionType, "High", "Outcome: ${executionResult.outcome::class.simpleName}")
            }

            updateSubTaskStatus(task.id, executionResult.status)
        }
    }
    fun updateSubTaskStatus(id: Int, status: String) {
        viewModelScope.launch {
            val task = subTasks.value.find { it.id == id }
            val previousStatus = task?.status ?: "Pending"
            repository.updateSubTaskStatus(id, status)
            
            val mId = task?.missionId
            if (task != null && mId != null) {
                loggingService.logStateTransition(
                    missionId = mId,
                    agentName = task.assignedAgent,
                    previousState = previousStatus,
                    newState = status,
                    message = "Subtask '${task.description}' updated: $previousStatus -> $status"
                )
            }

            if (task != null && mId != null && status == "Completed") {
                val missionSubtasks = repository.getSubTasksForMission(mId).first()
                val context = getApplication<Application>()
                if (missionSubtasks.all { it.status == "Completed" }) {
                    val mission = missions.value.find { it.id == mId }
                    if (mission != null) {
                        repository.insertMission(mission.copy(status = "Completed"))
                        repository.insertMemory(ColonyMemory(content = "Completed colony mission: '${mission.goal}'"))
                        loggingService.logStateTransition(
                            missionId = mId,
                            agentName = "Colony Orchestrator",
                            previousState = "Active",
                            newState = "Completed",
                            message = "All subtasks finished. Mission '${mission.goal}' marked as COMPLETED."
                        )

                        // Send high priority system notification alert to user
                        NotificationHelper.sendHighPriorityMissionNotification(
                            context = context,
                            missionId = mId,
                            agentName = task.assignedAgent,
                            missionGoal = mission.goal
                        )
                    }
                } else {
                    repository.insertMemory(ColonyMemory(content = "Completed subtask: '${task.description}'"))
                    // Trigger notification for high priority subtask
                    NotificationHelper.sendHighPriorityMissionNotification(
                        context = context,
                        missionId = mId,
                        agentName = task.assignedAgent,
                        missionGoal = task.description
                    )
                }
            }
            
            checkBadgeMilestones()
        }
    }

    fun addRuleNode(id: String, posX: Float, posY: Float, nodeType: String, text: String) {
        viewModelScope.launch {
            repository.insertRuleNode(com.example.data.RuleNodeEntity(id, posX, posY, nodeType, text))
        }
    }

    fun deleteRuleNode(id: String) {
        viewModelScope.launch {
            repository.deleteRuleNodeById(id)
        }
    }

    fun addRuleConnection(fromId: String, toId: String) {
        viewModelScope.launch {
            repository.insertRuleConnection(com.example.data.RuleConnectionEntity(fromId = fromId, toId = toId))
        }
    }

    fun deleteRuleConnection(fromId: String, toId: String) {
        viewModelScope.launch {
            repository.deleteRuleConnection(fromId, toId)
        }
    }

    fun addMemory(content: String) {
        viewModelScope.launch {
            repository.insertMemory(ColonyMemory(content = content))
        }
    }

    private val chatViewModel by lazy { ChatViewModel(getApplication()) }

    fun clearChat() {
        chatViewModel.clearChat()
    }

    fun logDataAccess(agentName: String, dataType: String, isViolation: Boolean = false, reason: String? = null) {
        viewModelScope.launch {
            repository.insertDataAccessRequest(
                com.example.data.DataAccessRequest(
                    agentName = agentName,
                    dataType = dataType,
                    isPolicyViolation = isViolation,
                    violationReason = reason
                )
            )
        }
    }

    fun checkAndEnforceAgentPermissions() {
        viewModelScope.launch {
            val currentAgents = agents.value
            val context = getApplication<android.app.Application>()
            
            for (agent in currentAgents) {
                var hasAll = true
                val perms = agent.permissions.lowercase()
                
                if (perms.contains("contact")) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        hasAll = false
                    }
                }
                if (perms.contains("calendar")) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        hasAll = false
                    }
                }
                
                if (!hasAll && agent.status != "Blocked") {
                    repository.insertAgent(agent.copy(status = "Blocked"))
                } else if (hasAll && agent.status == "Blocked") {
                    repository.insertAgent(agent.copy(status = "Active"))
                }
            }
        }
    }
    fun runDiagnosticAudit() {
        viewModelScope.launch {
            val result = privacyAuditor.performAudit(agents.value.size)
            repository.insertDataAccessRequest(result.localScanLog)
            repository.insertDataAccessRequest(result.cloudEvaluationLog)
        }
    }

    fun triggerPanic() {
        viewModelScope.launch {
            repository.haltSystem()
            // Optional: insert a council message about the panic
            repository.insertMessage(CouncilMessage(role = "system", content = "SYSTEM HALTED: Panic button pressed. All agents and active missions stopped."))
        }
    }

    fun sendMessage(userText: String, bitmap: Bitmap? = null, mode: String = "Fast") {
        chatViewModel.sendMessage(userText, bitmap, mode)
    }

    fun getRealCalendarEvents(): List<com.example.data.CalendarEvent> {
        val realEvents = mutableListOf<com.example.data.CalendarEvent>()
        val context = getApplication<android.app.Application>()
        val capCheck = com.example.security.AgentCapabilityGuard.checkCapability(
            context,
            "Calendar Manager",
            com.example.security.AgentCapability.READ_CALENDAR
        )
        
        if (capCheck is com.example.security.CapabilityResult.Granted) {
            try {
                // Calendar access capability verified
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (capCheck is com.example.security.CapabilityResult.Denied) {
            logDataAccess("Calendar Manager", "READ_CALENDAR", isViolation = true, reason = capCheck.reason)
        }
        return realEvents
    }

    fun analyzeCalendarEventsWithGemini() {
        viewModelScope.launch {
            if (!isApiKeyConfigured) {
                return@launch
            }
            
            val currentAgents = agents.value
            
            
            val realEvents = getRealCalendarEvents()
            val events = if (realEvents.isNotEmpty()) realEvents else calendarEvents.value
            
            if (currentAgents.isEmpty() || events.isEmpty()) return@launch
            
            val newPredictions = _predictions.value.toMutableMap()
            
            currentAgents.forEach { agent ->
                val agentEvents = events.filter { it.agentName.equals(agent.name, ignoreCase = true) }
                if (agentEvents.isNotEmpty()) {
                    val eventsText = agentEvents.joinToString("\n") { 
                        "- ${it.title} (${it.description}) starting at ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it.startTime))}"
                    }
                    
                    val systemPrompt = "You are an assistant predicting agent status changes based on calendar events."
                    val promptText = """
                        Analyze these upcoming calendar events for agent '${agent.name}' (Role: ${agent.role}, Current Status: ${agent.status}):
                        $eventsText
                        
                        Predict a suitable status change (e.g. Focus, Busy, Idle, Active, Silent) before the event begins.
                        Return ONLY a single valid JSON object, with no markdown code blocks, backticks, or 'json' headers. Format exactly as:
                        {"suggestedStatus": "Focus", "suggestionReason": "To prepare for the upcoming Project sync."}
                    """.trimIndent()
                    
                    try {
                        val rawText = com.example.network.AILlmClient.generateContent(getApplication(), promptText, systemPrompt)
                        val cleanedJson = rawText.substringAfter("```json").substringAfter("```").substringBefore("```").trim()
                        val jsonObject = org.json.JSONObject(cleanedJson.ifBlank { rawText })
                        val suggestedStatus = jsonObject.optString("suggestedStatus", "Focus")
                        val suggestionReason = jsonObject.optString("suggestionReason", "Upcoming event scheduled.")
                        
                        newPredictions[agent.id] = AgentPrediction(suggestedStatus, suggestionReason)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            _predictions.value = newPredictions
        }
    }

    fun generateDailySummaryInsight() {
        viewModelScope.launch {
            if (!isApiKeyConfigured) {
                _dailySummaryInsight.value = "Configure Gemini API Key in settings to enable AI insights."
                return@launch
            }
            
            _dailySummaryInsight.value = "Generating insight..."
            
            val totalSubTasks = subTasks.value.size
            val completedSubTasks = subTasks.value.count { it.status == "Completed" }
            val totalDecisions = decisions.value.size
            val totalLogs = missionStateLogs.value.size
            
            val systemPrompt = "You are a concise AI assistant."
            val promptText = """
                Analyze this aggregate performance data for an agent colony:
                Total SubTasks: $totalSubTasks
                Completed SubTasks: $completedSubTasks
                Decisions Made: $totalDecisions
                Mission Logs: $totalLogs
                
                Generate a single, one-sentence insightful summary about the colony's daily performance and productivity trend. Do not use markdown or quotes.
            """.trimIndent()
            
            try {
                val rawText = com.example.network.AILlmClient.generateContent(getApplication(), promptText, systemPrompt)
                _dailySummaryInsight.value = rawText.trim()
            } catch (e: Exception) {
                e.printStackTrace()
                _dailySummaryInsight.value = "Failed to generate insight."
            }
        }
    }

    private fun startHeartbeat() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000L) // 1 minute
                val activeAgents = agents.value.filter { it.status.equals("Active", ignoreCase = true) || it.status.equals("Busy", ignoreCase = true) }
                if (activeAgents.isNotEmpty()) {
                    val agentNameStr = activeAgents.joinToString(", ") { it.name }
                    repository.insertMissionStateLog(
                        com.example.data.MissionStateLog(
                            missionId = 0,
                            agentName = "System",
                            previousState = "Unknown",
                            newState = "Heartbeat",
                            message = "System check: $agentNameStr active.",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private fun checkInteractionFrequency() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            val sevenDaysAgo = now - (7 * oneDayMs)
            
            val currentAgents = repository.allAgents.first()
            val currentSubTasks = repository.allSubTasks.first()
            val currentDecisions = repository.allDecisions.first()
            
            val notificationManager = getApplication<Application>().getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel("colony_channel", "Colony Agent Alerts", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }
            
            currentAgents.forEach { agent ->
                val agentTasks = currentSubTasks.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) }
                val totalTasks = agentTasks.size
                if (totalTasks >= 5) {
                    val completedTasks = agentTasks.count { it.status == "Completed" }
                    val completionRate = completedTasks.toFloat() / totalTasks.toFloat()
                    if (completionRate < 0.3f) { // Threshold 30%
                        val notification = androidx.core.app.NotificationCompat.Builder(getApplication(), "colony_channel")
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setContentTitle("Low Task Completion Rate")
                            .setContentText("${agent.name} is struggling with tasks (${(completionRate * 100).toInt()}% completion).")
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                            .build()
                        notificationManager.notify(agent.id + 10000, notification)
                    }
                }

                val recentTasks = currentSubTasks.count { it.assignedAgent == agent.name && it.timestamp >= sevenDaysAgo }
                val recentDecisionsCount = currentDecisions.count { it.agentName == agent.name && it.timestamp >= sevenDaysAgo }
                val totalRecent = recentTasks + recentDecisionsCount
                
                val olderTasks = currentSubTasks.count { it.assignedAgent == agent.name && it.timestamp < sevenDaysAgo }
                val olderDecisionsCount = currentDecisions.count { it.agentName == agent.name && it.timestamp < sevenDaysAgo }
                val totalOlder = olderTasks + olderDecisionsCount
                
                // If the agent was active historically (e.g. > 10 interactions) but very inactive recently (e.g. < 2 interactions in 7 days)
                if (totalOlder > 10 && totalRecent < 2) {
                    val notification = androidx.core.app.NotificationCompat.Builder(getApplication(), "colony_channel")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Agent Inactivity Alert")
                        .setContentText("${agent.name}'s interaction frequency has dropped significantly.")
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                        .build()
                    notificationManager.notify(agent.id, notification)
                }
            }
        }
    }

    fun applyRestPeriods() {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("colony_prefs", android.content.Context.MODE_PRIVATE)
            val restEnabled = prefs.getBoolean("rest_enabled", false)
            if (!restEnabled) return@launch
            
            val startHour = prefs.getInt("rest_start_hour", 22)
            val startMinute = prefs.getInt("rest_start_minute", 0)
            val endHour = prefs.getInt("rest_end_hour", 7)
            val endMinute = prefs.getInt("rest_end_minute", 0)
            
            val calendar = java.util.Calendar.getInstance()
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(java.util.Calendar.MINUTE)
            
            val currentTimeInMinutes = currentHour * 60 + currentMinute
            val startTimeInMinutes = startHour * 60 + startMinute
            val endTimeInMinutes = endHour * 60 + endMinute
            
            val isResting = if (startTimeInMinutes < endTimeInMinutes) {
                currentTimeInMinutes in startTimeInMinutes..endTimeInMinutes
            } else {
                currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes <= endTimeInMinutes
            }
            
            if (isResting) {
                val activeAgents = agents.value.filter { it.status == "Active" }
                activeAgents.forEach { agent ->
                    repository.insertAgent(agent.copy(status = "Paused"))
                }
            } else {
                val pausedAgents = agents.value.filter { it.status == "Paused" }
                pausedAgents.forEach { agent ->
                    repository.insertAgent(agent.copy(status = "Active"))
                }
            }
        }
    }

    fun addCalendarEvent(title: String, description: String, startTime: Long, endTime: Long, agentName: String) {
        viewModelScope.launch {
            repository.insertCalendarEvent(
                com.example.data.CalendarEvent(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    agentName = agentName
                )
            )
            // Re-trigger analysis
            analyzeCalendarEventsWithGemini()
        }
    }

    fun clearCalendarEvents() {
        viewModelScope.launch {
            repository.clearCalendarEvents()
        }
    }

    fun checkBadgeMilestones() {
        viewModelScope.launch {
            val currentBadges = badges.value.ifEmpty {
                if (repository.getBadgeCount() == 0) {
                    repository.insertBadges(defaultBadges)
                }
                repository.allBadges.first()
            }

            val currentAgents = agents.value
            val currentSubtasks = subTasks.value
            val currentMissions = missions.value
            val currentDecisions = decisions.value
            val currentEvents = calendarEvents.value
            val currentRequests = dataAccessRequests.value

            val completedMissionsCount = currentMissions.count { it.status.equals("Completed", ignoreCase = true) }
            val completedTasksCount = currentSubtasks.count { it.status.equals("Completed", ignoreCase = true) }
            val decisionsCount = currentDecisions.size
            val activeAgentsCount = currentAgents.size
            val autonomousAgentsCount = currentAgents.count { it.autonomyLevel.contains("Autonomous", ignoreCase = true) || it.autonomyLevel.contains("Full", ignoreCase = true) }
            val customPromptAgentsCount = currentAgents.count { it.systemPrompt.isNotBlank() }
            val violationsCount = currentRequests.count { it.isPolicyViolation }

            currentBadges.forEach { badge ->
                var newProgress = badge.currentProgress

                when (badge.id) {
                    "first_synergy" -> {
                        newProgress = if (completedMissionsCount >= 1 || currentMissions.isNotEmpty()) 1 else 0
                    }
                    "master_strategist" -> {
                        newProgress = (completedMissionsCount + (if (currentMissions.isNotEmpty()) 1 else 0)).coerceAtMost(3)
                    }
                    "decisive_colony" -> {
                        newProgress = decisionsCount.coerceAtMost(5)
                    }
                    "powerhouse_colony" -> {
                        newProgress = activeAgentsCount.coerceAtMost(5)
                    }
                    "productivity_dynamo" -> {
                        newProgress = completedTasksCount.coerceAtMost(10)
                    }
                    "autonomy_master" -> {
                        newProgress = autonomousAgentsCount.coerceAtMost(2)
                    }
                    "cross_collaboration" -> {
                        val distinctAgentsInTasks = currentSubtasks.map { it.assignedAgent }.distinct().size
                        newProgress = if (distinctAgentsInTasks >= 3 || currentMissions.isNotEmpty()) 1 else 0
                    }
                    "calendar_synchronizer" -> {
                        newProgress = if (currentEvents.isNotEmpty()) 1 else 0
                    }
                    "zero_violations" -> {
                        newProgress = if (violationsCount == 0 && activeAgentsCount > 0) 1 else 0
                    }
                    "colony_architect" -> {
                        newProgress = if (customPromptAgentsCount > 0) 1 else 0
                    }
                }

                val nowUnlocked = newProgress >= badge.maxProgress
                if (nowUnlocked && !badge.isUnlocked) {
                    val updated = badge.copy(
                        isUnlocked = true,
                        currentProgress = badge.maxProgress,
                        unlockedAt = System.currentTimeMillis()
                    )
                    repository.insertBadge(updated)
                    _newlyUnlockedBadge.value = updated
                } else if (newProgress != badge.currentProgress && !badge.isUnlocked) {
                    repository.insertBadge(badge.copy(currentProgress = newProgress))
                }
            }

            // Also check productivity agent unlock milestones
            checkProductivityMilestones()
        }
    }

    fun calculateTotalProductivityXp(): Int {
        val currentSubtasks = subTasks.value
        val currentMissions = missions.value
        val currentDecisions = decisions.value
        val currentBadges = badges.value

        val completedTasks = currentSubtasks.count { it.status.equals("Completed", ignoreCase = true) }
        val completedMissions = currentMissions.count { it.status.equals("Completed", ignoreCase = true) }
        val decisionCount = currentDecisions.size
        val unlockedBadgeCount = currentBadges.count { it.isUnlocked }

        return (completedTasks * 10) + (completedMissions * 50) + (decisionCount * 15) + (unlockedBadgeCount * 25)
    }

    fun checkProductivityMilestones() {
        viewModelScope.launch {
            val totalXp = calculateTotalProductivityXp()
            val milestones = agentMilestones.value.ifEmpty {
                if (repository.getAgentMilestoneCount() == 0) {
                    repository.insertAgentMilestones(defaultMilestones)
                }
                repository.allAgentMilestones.first()
            }

            milestones.forEach { milestone ->
                if (!milestone.isUnlocked && totalXp >= milestone.requiredXp) {
                    val updated = milestone.copy(
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis()
                    )
                    repository.insertAgentMilestone(updated)
                    _newlyUnlockedAgentMilestone.value = updated

                    // Add the agent to the colony automatically!
                    val existingAgents = agents.value
                    if (existingAgents.none { it.name.equals(milestone.agentName, ignoreCase = true) }) {
                        repository.insertAgent(
                            Agent(
                                name = milestone.agentName,
                                type = milestone.type,
                                role = milestone.role,
                                permissions = "Full System Access",
                                autonomyLevel = "Semi-Autonomous",
                                iconName = milestone.iconName,
                                traits = "Milestone Unlocked, Advanced AI",
                                systemPrompt = milestone.systemPrompt
                            )
                        )
                    }
                }
            }
        }
    }

    fun sendInterAgentMessage(senderAgentName: String, senderRole: String = "Agent", content: String, targetAgentName: String? = null, topic: String = "General Colony") {
        viewModelScope.launch {
            repository.insertInterAgentMessage(
                com.example.data.InterAgentMessage(
                    senderAgentName = senderAgentName,
                    senderRole = senderRole,
                    targetAgentName = targetAgentName,
                    content = content,
                    topic = topic
                )
            )
        }
    }

    fun reassignSubTask(taskId: Int, newAgentName: String) {
        viewModelScope.launch {
            val task = subTasks.value.find { it.id == taskId }
            if (task != null) {
                repository.insertSubTask(task.copy(assignedAgent = newAgentName))
                sendInterAgentMessage(
                    senderAgentName = "System",
                    senderRole = "Dispatcher",
                    content = "Task '${task.description}' reassigned to $newAgentName.",
                    topic = "Task Assignment"
                )
            }
        }
    }

    fun deleteSubTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteSubTaskById(taskId)
        }
    }
    
    fun deleteSubTasksByIds(taskIds: List<Int>) {
        viewModelScope.launch {
            repository.deleteSubTasksByIds(taskIds)
        }
    }
    
    fun restoreSubTasks(tasks: List<SubTask>) {
        viewModelScope.launch {
            repository.insertSubTasks(tasks)
        }
    }

    fun updateSubTaskPriority(taskId: Int, priority: String) {
        viewModelScope.launch {
            val task = subTasks.value.find { it.id == taskId }
            if (task != null) {
                repository.insertSubTask(task.copy(priority = priority))
            }
        }
    }

    fun createStandaloneSubTask(description: String, assignedAgent: String, status: String = "Pending", priority: String = "Medium") {
        viewModelScope.launch {
            repository.insertSubTask(
                SubTask(
                    missionId = null,
                    assignedAgent = assignedAgent,
                    description = description,
                    status = status,
                    priority = priority
                )
            )
            sendInterAgentMessage(
                senderAgentName = "Alpha Coordinator",
                senderRole = "Lead Agent",
                targetAgentName = assignedAgent,
                content = "New task assigned: '$description'. Please commence processing when ready.",
                topic = "Task Assignment"
            )
            checkBadgeMilestones()
        }
    }

    private val agentGreetings = mutableMapOf<Int, MutableStateFlow<String>>()

    fun getAgentGreeting(agent: Agent): StateFlow<String> {
        val flow = agentGreetings.getOrPut(agent.id) { MutableStateFlow("...") }
        if (flow.value == "...") {
            flow.value = "Generating..."
            viewModelScope.launch {
                try {
                    val prompt = "Generate a short, unique daily greeting and status summary (1-2 sentences) for an AI agent named ${agent.name} whose role is ${agent.role}. Be creative, concise, and in-character."
                    val result = com.example.network.AILlmClient.generateContent(getApplication(), prompt)
                    flow.value = result
                } catch (e: Exception) {
                    flow.value = "Welcome back, ${agent.name}!"
                }
            }
        }
        return flow
    }

    fun autoTagTask(description: String, onResult: (priority: String, agent: String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentAgents = agents.value.joinToString(", ") { it.name + " (" + it.role + ")" }
                val prompt = """
                    Analyze the following task description and suggest the most relevant priority level (High, Medium, Low) and the most appropriate agent from the following list: $currentAgents.
                    Task: "$description"
                    Respond strictly in JSON format like: {"priority": "High", "agent": "Agent Name"}
                """.trimIndent()
                
                val result = com.example.network.AILlmClient.generateContent(getApplication(), prompt).trim()
                
                // Extremely simple parsing, expecting strict JSON or at least something similar
                var suggestedPriority = "Medium"
                var suggestedAgent = agents.value.firstOrNull()?.name ?: ""
                
                if (result.contains("\"priority\": \"High\"", ignoreCase = true)) suggestedPriority = "High"
                else if (result.contains("\"priority\": \"Low\"", ignoreCase = true)) suggestedPriority = "Low"
                
                val bestAgentMatch = agents.value.find { result.contains(it.name, ignoreCase = true) }
                if (bestAgentMatch != null) {
                    suggestedAgent = bestAgentMatch.name
                }
                
                onResult(suggestedPriority, suggestedAgent)
            } catch (e: Exception) {
                // Ignore, use fallback if needed
            }
        }
    }
    fun initiateAgentDiscussion(topic: String) {
        viewModelScope.launch {
            val currentAgents = agents.value.filter { it.status != "Paused" && it.status != "Halted" }.take(3)
            if (currentAgents.isEmpty()) return@launch

            if (isApiKeyConfigured) {
                val discussionHistory = mutableListOf<String>()
                
                for (agent in currentAgents) {
                    try {
                        val historyText = if (discussionHistory.isEmpty()) "No prior messages." else discussionHistory.joinToString("\n")
                        val prompt = """
                            You are ${agent.name}, an AI Agent in a colony. Your role is ${agent.role}.
                            You are participating in a discussion on the topic: '$topic'.
                            
                            Here is the discussion so far:
                            $historyText
                            
                            Write your response (1-2 sentences) contributing to the topic from your specific persona's perspective.
                            Do not include your name at the beginning, just the message.
                        """.trimIndent()
                        
                        val responseContent = com.example.network.AILlmClient.generateContent(getApplication(), prompt).trim()
                        
                        discussionHistory.add("${agent.name}: $responseContent")
                        
                        sendInterAgentMessage(
                            senderAgentName = agent.name,
                            senderRole = agent.type,
                            content = responseContent,
                            topic = topic
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // Return honest system error if no API key is provided
                sendInterAgentMessage(
                    senderAgentName = "System",
                    senderRole = "System Error",
                    content = "System Halted: LLM Engine Offline. Please configure your API Key in settings.",
                    topic = "System Alert"
                )
            }
        }
    }

    fun initiateNegotiation(
        proposer: String,
        target: String,
        proposedAction: String,
        conflictTopic: String = "Resource Allocation",
        missionId: Int = 0
    ) {
        viewModelScope.launch {
            val proposal = com.example.data.AgentNegotiationProposal(
                missionId = missionId,
                proposerAgent = proposer,
                targetAgent = target,
                proposedAction = proposedAction,
                status = "Pending",
                conflictTopic = conflictTopic
            )
            repository.insertAgentNegotiation(proposal)
            repository.insertMemory(ColonyMemory(content = "Negotiation Initiated: $proposer vs $target regarding '$conflictTopic'"))
        }
    }

    fun resolveNegotiation(id: Int, status: String, counterProposal: String = "") {
        viewModelScope.launch {
            repository.updateNegotiationStatus(id, status, counterProposal)
            val neg = negotiations.value.find { it.id == id }
            if (neg != null) {
                val proposerAgentObj = agents.value.find { it.name == neg.proposerAgent }
                val targetAgentObj = agents.value.find { it.name == neg.targetAgent }
                
                proposerAgentObj?.let {
                    appendAgentStatusNoteSnippet(it.id, "Negotiation with ${neg.targetAgent} marked $status. ${if (counterProposal.isNotBlank()) "Counter: $counterProposal" else ""}")
                }
                targetAgentObj?.let {
                    appendAgentStatusNoteSnippet(it.id, "Negotiation with ${neg.proposerAgent} marked $status. ${if (counterProposal.isNotBlank()) "Counter: $counterProposal" else ""}")
                }
                
                repository.insertMemory(ColonyMemory(content = "Negotiation #$id resolved as $status (${neg.proposerAgent} <-> ${neg.targetAgent})"))
            }
        }
    }

    fun autoInitiateAgentNegotiation() {
        viewModelScope.launch {
            val active = agents.value.filter { it.status == "Active" }
            if (active.size < 2) return@launch
            
            val p1 = active.random()
            var p2 = active.random()
            var attempts = 0
            while (p2.id == p1.id && active.size > 1 && attempts < 10) {
                p2 = active.random()
                attempts++
            }
            if (p1.id == p2.id) return@launch
            
            val contextApp = getApplication<Application>()
            val prompt = """
                You are ${p1.name} (${p1.role}). You need to initiate a system resource or task negotiation with ${p2.name} (${p2.role}).
                Propose a single specific topic of conflict (e.g. "Compute Quota", "Data Access", etc.) on the first line.
                Propose the action or demand you are making on the second line.
                Keep both extremely short.
            """.trimIndent()
            
            var selectedTopic = "Compute Quota"
            var action = "Request exclusive priority lock for compute quota during peak cycles."
            
            try {
                val reply = com.example.network.AILlmClient.generateContent(contextApp, prompt).trim().lines()
                if (reply.isNotEmpty()) {
                    selectedTopic = reply.first().take(50)
                    if (reply.size > 1) {
                        action = reply[1].take(200)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val proposal = com.example.data.AgentNegotiationProposal(
                proposerAgent = p1.name,
                targetAgent = p2.name,
                proposedAction = action,
                status = "Pending",
                conflictTopic = selectedTopic
            )
            repository.insertAgentNegotiation(proposal)
            
            repository.insertMemory(ColonyMemory(content = "Conflict Detected: ${p1.name} initiated negotiation with ${p2.name} over $selectedTopic"))
            appendAgentStatusNoteSnippet(p1.id, "Initiated resource dispute with ${p2.name} concerning $selectedTopic.")
            appendAgentStatusNoteSnippet(p2.id, "Received negotiation proposal from ${p1.name} concerning $selectedTopic.")
        }
    }

    fun triggerColonyWidePolicyVote(policyName: String) {
        viewModelScope.launch {
            val activeAgentsList = agents.value.filter { it.status == "Active" }
            if (activeAgentsList.isEmpty()) return@launch

            val proposer = activeAgentsList.random().name
            val voteTopic = "Colony Policy: $policyName"
            val action = "Enforce new $policyName rules across all agent execution pipelines."
            
            val contextApp = getApplication<Application>()

            activeAgentsList.filter { it.name != proposer }.forEach { target ->
                
                val prompt = """
                    You are ${target.name} (${target.role}). You are voting on a colony-wide policy proposed by $proposer.
                    The policy is: "$policyName".
                    Will you accept this policy? Respond with exactly "Accepted" or "Countered" on the first line.
                    If you counter, provide a 1-sentence counter-proposal on the second line.
                """.trimIndent()
                
                var status = "Accepted"
                var counter = ""
                
                try {
                    val reply = com.example.network.AILlmClient.generateContent(contextApp, prompt).trim().lines()
                    if (reply.isNotEmpty()) {
                        val firstLine = reply.first().uppercase()
                        if (firstLine.contains("COUNTER")) {
                            status = "Countered"
                            if (reply.size > 1) {
                                counter = reply[1].take(200)
                            } else {
                                counter = "Require further review based on ${target.role} constraints."
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                val proposal = com.example.data.AgentNegotiationProposal(
                    proposerAgent = proposer,
                    targetAgent = target.name,
                    proposedAction = action,
                    counterProposal = counter,
                    status = status,
                    conflictTopic = voteTopic
                )
                repository.insertAgentNegotiation(proposal)
                appendAgentStatusNoteSnippet(target.id, "Voted '$status' on $voteTopic proposed by $proposer.")
            }

            repository.insertMemory(ColonyMemory(content = "Colony Policy Vote Executed: '$policyName' with ${activeAgentsList.size} agents participating."))
        }
    }

    fun pingAllAgentMeshNodes() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAgents = agents.value
            if (currentAgents.isEmpty()) return@launch

            val runtime = Runtime.getRuntime()
            val maxMemMb = runtime.maxMemory() / (1024 * 1024)
            val totalMemMb = runtime.totalMemory() / (1024 * 1024)
            val freeMemMb = runtime.freeMemory() / (1024 * 1024)
            val usedMemMb = (totalMemMb - freeMemMb).toFloat()
            val cpuLoad = ((totalMemMb - freeMemMb).toFloat() / maxMemMb.toFloat()) * 100f // proxy for app load

            // ping google.com for real network latency
            var realLatency = 0L
            try {
                val start = System.currentTimeMillis()
                val url = java.net.URL("https://www.google.com")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.connect()
                if (connection.responseCode == 200) {
                    realLatency = System.currentTimeMillis() - start
                }
                connection.disconnect()
            } catch (e: Exception) {
                realLatency = -1L
            }

            currentAgents.forEach { agent ->
                val health = when {
                    realLatency < 0L -> "Disconnected"
                    realLatency > 500L -> "Degraded"
                    cpuLoad > 85f -> "Warning"
                    else -> "Optimal"
                }

                val telemetry = com.example.data.AgentMeshTelemetry(
                    agentId = agent.id,
                    agentName = agent.name,
                    latencyMs = if (realLatency < 0L) 999L else realLatency,
                    cpuLoadPct = cpuLoad,
                    memoryUsageMb = usedMemMb,
                    activeConnectionsCount = 1, // Single host app
                    healthStatus = health
                )
                repository.insertMeshTelemetry(telemetry)
            }

            repository.insertMemory(ColonyMemory(content = "Mesh Network Ping Completed across ${currentAgents.size} nodes."))
        }
    }

    fun addKnowledgeEdge(
        sourceLabel: String,
        sourceType: String,
        targetLabel: String,
        targetType: String,
        relationType: String,
        weight: Float,
        creatorAgent: String
    ) {
        viewModelScope.launch {
            val edge = com.example.data.AgentKnowledgeEdge(
                sourceLabel = sourceLabel,
                sourceType = sourceType,
                targetLabel = targetLabel,
                targetType = targetType,
                relationType = relationType,
                weight = weight,
                creatorAgent = creatorAgent
            )
            repository.insertKnowledgeEdge(edge)
            appendAgentStatusNoteSnippet(1, "Added semantic edge: [$sourceLabel] -$relationType-> [$targetLabel]")
        }
    }

    fun deleteKnowledgeEdge(id: Int) {
        viewModelScope.launch {
            repository.deleteKnowledgeEdge(id)
        }
    }

    fun synthesizeKnowledgeGraphFromColonyState() {
        viewModelScope.launch {
            val agentList = agents.value
            val activeSubtasks = repository.allSubTasks.first()
            val currentDecisions = decisions.value.take(10)

            if (agentList.isEmpty()) return@launch

            // Create real edges based on actual SubTask assignments (Mission -> SubTask -> Agent)
            activeSubtasks.forEach { subTask ->
                val assignedAgent = agentList.find { it.name == subTask.assignedAgent }
                if (assignedAgent != null) {
                    val edge = com.example.data.AgentKnowledgeEdge(
                        sourceLabel = assignedAgent.name,
                        sourceType = "AGENT",
                        targetLabel = subTask.description.take(50),
                        targetType = "SUBTASK",
                        relationType = "EXECUTES",
                        weight = 0.95f,
                        creatorAgent = assignedAgent.name
                    )
                    repository.insertKnowledgeEdge(edge)
                }
            }

            // Create real edges based on actual Decisions
            currentDecisions.forEach { decision ->
                val agentName = decision.agentName
                val edge = com.example.data.AgentKnowledgeEdge(
                    sourceLabel = decision.actionDescription.take(50),
                    sourceType = "DECISION",
                    targetLabel = agentName,
                    targetType = "AGENT",
                    relationType = "MADE_BY",
                    weight = 0.9f,
                    creatorAgent = agentName
                )
                repository.insertKnowledgeEdge(edge)
            }

            repository.insertMemory(ColonyMemory(content = "Autonomous Knowledge Graph Edge Synthesis Executed from real state."))
        }
    }

    fun runSelfEvolutionCycle() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val agentList = repository.allAgents.first()
                if (agentList.isEmpty()) {
                    repository.insertMemory(
                        ColonyMemory(
                            content = "[Self-Evolution Engine] Evolution cycle aborted: No active agents found to evolve."
                        )
                    )
                    return@launch
                }

                val currentHeuristics = repository.allHeuristics.first()
                val nextGeneration = (currentHeuristics.maxOfOrNull { it.generation } ?: 0) + 1

                // Extract actual live Android system permissions
                val contactPermGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_CONTACTS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                val calendarPermGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_CALENDAR
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                // Extract actual live mesh telemetry average latency
                val latestTelemetry = repository.allMeshTelemetry.first()
                val avgLatency = if (latestTelemetry.isNotEmpty()) {
                    latestTelemetry.map { it.latencyMs }.average()
                } else {
                    0.0
                }

                // Extract actual live subtask load
                val allSubTasks = repository.allSubTasks.first()
                val activeSubTaskCount = allSubTasks.count { it.status == "In Progress" || it.status == "Pending" }

                // Generate smart rules based on agent specializations and current system state
                agentList.forEach { agent ->
                    val spec = agent.role.uppercase()
                    val key = when {
                        spec.contains("SECURITY") || spec.contains("SENTINEL") -> "RISK_THRESHOLD"
                        spec.contains("DESIGN") || spec.contains("CREATIVE") -> "DELEGATION_PRIORITY"
                        spec.contains("ENGINEERING") || spec.contains("CODE") -> "EXECUTION_SPEED"
                        else -> "REASONING_DEPTH"
                    }

                    val target = when {
                        spec.contains("SECURITY") -> "SECURITY_AUDIT"
                        spec.contains("ENGINEERING") -> "RESOURCE_OPT"
                        spec.contains("CREATIVE") -> "HIGH_CONCURRENCY_TASKS"
                        else -> "GENERAL"
                    }

                    val systemContext = "System context: Contacts Perm: $contactPermGranted, Calendar Perm: $calendarPermGranted, Mesh Latency: $avgLatency ms, Active Subtasks: $activeSubTaskCount. Agent Role: $spec."
                    val prompt = """
                        You are the EDDE Evolution Engine. Generate a single sentence heuristic policy for the agent '${agent.name}' (key: $key) based on the following context.
                        $systemContext
                        Respond with ONLY the policy sentence in Polish.
                    """.trimIndent()

                    val policyResponse = try {
                        com.example.network.AILlmClient.generateContent(getApplication(), prompt).trim()
                    } catch (e: Exception) {
                        "Utrzymuj standardowe procedury operacyjne."
                    }
                    val finalPolicy = if (policyResponse.isNotEmpty()) policyResponse else "Utrzymuj standardowe procedury operacyjne."

                    val newHeuristic = com.example.data.AgentHeuristicRule(
                        agentName = agent.name,
                        heuristicKey = key,
                        patternTarget = target,
                        confidenceScore = 0.85f,
                        successCount = 0,
                        failureCount = 0,
                        adaptedPolicy = finalPolicy,
                        generation = nextGeneration,
                        lastEvolvedTimestamp = System.currentTimeMillis()
                    )

                    repository.insertHeuristic(newHeuristic)
                }

                repository.insertMemory(
                    ColonyMemory(
                        content = "[Self-Evolution Engine] Generacja $nextGeneration zakończona. Wyewoluowano ${agentList.size} reguł heurystycznych na podstawie rzeczywistego stanu uprawnień i telemetrii."
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("ColonyViewModel", "Failed to execute Self-Evolution cycle", e)
            }
        }
    }

    fun deleteHeuristic(id: Int) {
        viewModelScope.launch {
            repository.deleteHeuristic(id)
            repository.insertMemory(
                ColonyMemory(
                    content = "[Self-Evolution Engine] Pruned heuristic rule ID $id from active database ledger."
                )
            )
        }
    }

    fun insertFinanceTransaction(transaction: com.example.data.FinanceTransaction) {
        viewModelScope.launch {
            repository.insertFinanceTransaction(transaction)
        }
    }

    fun clearFinanceTransactions() {
        viewModelScope.launch {
            repository.clearFinanceTransactions()
            repository.insertMemory(
                ColonyMemory(
                    content = "[Smart Finance Agent] Ledger wyczyszczony."
                )
            )
        }
    }

    fun importCsvTransactions(csvText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = mutableListOf<com.example.data.FinanceTransaction>()
                val lines = csvText.split('\n')
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.lowercase().startsWith("title") || trimmed.lowercase().startsWith("nazwa")) {
                        continue // Skip headers and comments
                    }
                    
                    // Simple CSV splitting (handling quoted values optionally or basic split)
                    val parts = trimmed.split(Regex("[,;](?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                    if (parts.size >= 2) {
                        val rawTitle = parts[0].replace("\"", "").trim()
                        val rawAmount = parts[1].replace("\"", "").trim()
                        val parsedAmount = rawAmount.toDoubleOrNull() ?: 0.0
                        
                        var category = if (parts.size >= 3) {
                            parts[2].replace("\"", "").trim()
                        } else {
                            // Deduce category from title
                            when {
                                rawTitle.lowercase().contains("food") || rawTitle.lowercase().contains("jedzenie") || rawTitle.lowercase().contains("restauracja") || rawTitle.lowercase().contains("sklep") -> "Food"
                                rawTitle.lowercase().contains("rent") || rawTitle.lowercase().contains("czynsz") || rawTitle.lowercase().contains("wynajem") -> "Rent"
                                rawTitle.lowercase().contains("utility") || rawTitle.lowercase().contains("prąd") || rawTitle.lowercase().contains("gaz") || rawTitle.lowercase().contains("woda") -> "Utilities"
                                rawTitle.lowercase().contains("salary") || rawTitle.lowercase().contains("pensja") || rawTitle.lowercase().contains("wypłata") || rawTitle.lowercase().contains("przelew") -> "Salary"
                                rawTitle.lowercase().contains("ent") || rawTitle.lowercase().contains("kino") || rawTitle.lowercase().contains("netflix") || rawTitle.lowercase().contains("pub") || rawTitle.lowercase().contains("gra") -> "Entertainment"
                                else -> "Other"
                            }
                        }
                        
                        if (category.isBlank()) {
                            category = "Other"
                        }
                        
                        list.add(
                            com.example.data.FinanceTransaction(
                                title = rawTitle,
                                amount = parsedAmount,
                                category = category
                            )
                        )
                    }
                }
                
                if (list.isNotEmpty()) {
                    repository.insertFinanceTransactions(list)
                    repository.insertMemory(
                        ColonyMemory(
                            content = "[Smart Finance Agent] Pomyślnie zaimportowano ${list.size} transakcji z pliku CSV do lokalnego rejestru."
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCalendarEventById(id: Int) {
        viewModelScope.launch {
            repository.deleteCalendarEventById(id)
        }
    }

    fun insertCalendarEvent(event: com.example.data.CalendarEvent) {
        viewModelScope.launch {
            repository.insertCalendarEvent(event)
        }
    }

    fun insertKnowledgeEdge(edge: com.example.data.AgentKnowledgeEdge) {
        viewModelScope.launch {
            repository.insertKnowledgeEdge(edge)
        }
    }

    fun insertMemory(memory: com.example.data.ColonyMemory) {
        viewModelScope.launch {
            repository.insertMemory(memory)
        }
    }

    // Custom Agents
    fun insertCustomAgentDefinition(definition: com.example.data.CustomAgentDefinition) {
        viewModelScope.launch {
            repository.insertCustomAgentDefinition(definition)
        }
    }

    fun deleteCustomAgentDefinition(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomAgentDefinition(id)
        }
    }

    // Flashcards
    fun insertFlashcard(flashcard: com.example.data.Flashcard) {
        viewModelScope.launch {
            repository.insertFlashcard(flashcard)
        }
    }

    fun deleteFlashcard(id: Int) {
        viewModelScope.launch {
            repository.deleteFlashcard(id)
        }
    }

    // Subscriptions
    fun insertSubscription(subscription: com.example.data.Subscription) {
        viewModelScope.launch {
            repository.insertSubscription(subscription)
        }
    }

    fun insertSubscriptions(list: List<com.example.data.Subscription>) {
        viewModelScope.launch {
            repository.insertSubscriptions(list)
        }
    }

    fun deleteSubscription(id: Int) {
        viewModelScope.launch {
            repository.deleteSubscription(id)
        }
    }

    fun updateSubscriptionCancelled(id: Int, isCancelled: Boolean) {
        viewModelScope.launch {
            repository.updateSubscriptionCancelled(id, isCancelled)
        }
    }

    // Sleep Records
    fun insertSleepRecord(record: com.example.data.SleepRecord) {
        viewModelScope.launch {
            repository.insertSleepRecord(record)
        }
    }

    fun clearSleepRecords() {
        viewModelScope.launch {
            repository.clearSleepRecords()
        }
    }

    // --- FAZA 4: NICE-TO-HAVE (Efekt WOW) ---

    // 17. Voice Command Surface
    private val _voiceText = MutableStateFlow("")
    val voiceText: StateFlow<String> = _voiceText

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _voiceError = MutableStateFlow<String?>(null)
    val voiceError: StateFlow<String?> = _voiceError

    private val _lastVoiceAction = MutableStateFlow<String?>(null)
    val lastVoiceAction: StateFlow<String?> = _lastVoiceAction

    private var speechRecognizer: android.speech.SpeechRecognizer? = null

    fun startVoiceRecognition(context: android.content.Context) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            _voiceError.value = "Brak uprawnień do mikrofonu."
            return
        }

        _voiceError.value = null
        _voiceText.value = "Słucham..."
        _isListening.value = true

        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
                }

                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "pl-PL")
                    putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
                    override fun onReadyForSpeech(params: android.os.Bundle?) {
                        _voiceText.value = "Mów teraz..."
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }
                    override fun onError(error: Int) {
                        _isListening.value = false
                        val errorMsg = when (error) {
                            android.speech.SpeechRecognizer.ERROR_AUDIO -> "Błąd audio."
                            android.speech.SpeechRecognizer.ERROR_CLIENT -> "Błąd klienta (spróbuj wpisać komendę)."
                            android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Niewystarczające uprawnienia."
                            android.speech.SpeechRecognizer.ERROR_NETWORK -> "Błąd sieci."
                            android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Przekroczono limit czasu sieci."
                            android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "Nie dopasowano mowy."
                            android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Usługa mowy jest zajęta."
                            android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nie wykryto mowy (timeout)."
                            else -> "Błąd rozpoznawania ($error)."
                        }
                        _voiceError.value = errorMsg
                    }
                    override fun onResults(results: android.os.Bundle?) {
                        val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val speech = matches[0]
                            _voiceText.value = speech
                            processVoiceCommand(speech)
                        } else {
                            _voiceError.value = "Nie usłyszałem komendy."
                        }
                        _isListening.value = false
                    }
                    override fun onPartialResults(partialResults: android.os.Bundle?) {
                        val matches = partialResults?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _voiceText.value = matches[0]
                        }
                    }
                    override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
                })

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                _voiceError.value = "Błąd inicjalizacji: ${e.message}"
            }
        }
    }

    fun stopVoiceRecognition() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun clearVoiceState() {
        _voiceText.value = ""
        _voiceError.value = null
        _lastVoiceAction.value = null
    }

    fun processVoiceCommand(command: String) {
        val normalized = command.lowercase().trim()
        viewModelScope.launch {
            when {
                normalized.contains("zadania") || normalized.contains("zadanie") -> {
                    _lastVoiceAction.value = "NAVIGATE_TASKS"
                }
                normalized.contains("ustawienia") || normalized.contains("opcje") -> {
                    _lastVoiceAction.value = "NAVIGATE_SETTINGS"
                }
                normalized.contains("skupien") || normalized.contains("focus") || normalized.contains("skupić") || normalized.contains("cichy") -> {
                    val currentFocus = agentPreferencesState.value.isFocusModeActive
                    updateFocusModeActive(!currentFocus)
                    _lastVoiceAction.value = "TOGGLE_FOCUS"
                }
                normalized.contains("budownic") || normalized.contains("nowy agent") || normalized.contains("stworz") -> {
                    _lastVoiceAction.value = "NAVIGATE_BUILDER"
                }
                normalized.contains("finans") || normalized.contains("subskryp") -> {
                    _lastVoiceAction.value = "NAVIGATE_FINANCE"
                }
                normalized.contains("sn") || normalized.contains("sen") || normalized.contains("recovery") -> {
                    _lastVoiceAction.value = "NAVIGATE_SLEEP"
                }
                else -> {
                    _lastVoiceAction.value = "UNKNOWN_COMMAND"
                }
            }
        }
    }

    // 20. Relationship Nudge Engine
    data class RelationshipNudge(
        val name: String,
        val phoneNumber: String?,
        val lastContactDays: Int,
        val status: String,
        val avatarUrl: String? = null
    )

    private val _relationshipNudges = MutableStateFlow<List<RelationshipNudge>>(emptyList())
    val relationshipNudges: StateFlow<List<RelationshipNudge>> = _relationshipNudges

    private val _nudgesPermissionGranted = MutableStateFlow(false)
    val nudgesPermissionGranted: StateFlow<Boolean> = _nudgesPermissionGranted

    fun checkNudgePermissions(context: android.content.Context) {
        val contactsGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val callLogGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED
        _nudgesPermissionGranted.value = contactsGranted && callLogGranted
    }

    fun fetchRelationshipNudges(context: android.content.Context) {
        checkNudgePermissions(context)
        if (!_nudgesPermissionGranted.value) {
            // Graceful default list when not yet approved
            _relationshipNudges.value = listOf(
                RelationshipNudge("Mama", "123456789", 15, "Pilne"),
                RelationshipNudge("Karol Kowalski (Mentor)", "987654321", 8, "Wymaga kontaktu"),
                RelationshipNudge("Anna Nowak", "555666777", 2, "Stabilna relacja")
            )
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<RelationshipNudge>()
            try {
                val contactsMap = mutableMapOf<String, String>()
                val contactsUri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                context.contentResolver.query(contactsUri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (cursor.moveToNext()) {
                        val name = if (nameIdx >= 0) cursor.getString(nameIdx) else "Nieznany"
                        val num = if (numIdx >= 0) cursor.getString(numIdx) else ""
                        if (num.isNotBlank()) {
                            contactsMap[num.replace(" ", "")] = name
                        }
                    }
                }

                val callLogUri = android.provider.CallLog.Calls.CONTENT_URI
                val projection = arrayOf(
                    android.provider.CallLog.Calls.NUMBER,
                    android.provider.CallLog.Calls.DATE
                )
                
                val lastContactsMap = mutableMapOf<String, Long>()
                context.contentResolver.query(callLogUri, projection, null, null, "${android.provider.CallLog.Calls.DATE} DESC")?.use { cursor ->
                    val numIdx = cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER)
                    val dateIdx = cursor.getColumnIndex(android.provider.CallLog.Calls.DATE)
                    while (cursor.moveToNext()) {
                        val num = if (numIdx >= 0) cursor.getString(numIdx) else ""
                        val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L
                        val cleanNum = num.replace(" ", "")
                        val contactName = contactsMap[cleanNum] ?: num
                        if (contactName.isNotBlank() && !lastContactsMap.containsKey(contactName)) {
                            lastContactsMap[contactName] = date
                        }
                    }
                }

                val now = System.currentTimeMillis()
                lastContactsMap.forEach { (name, timestamp) ->
                    val diffDays = ((now - timestamp) / (24 * 3600 * 1000L)).toInt().coerceAtLeast(0)
                    val status = when {
                        diffDays >= 14 -> "Pilne"
                        diffDays >= 7 -> "Wymaga kontaktu"
                        else -> "Stabilna relacja"
                    }
                    list.add(RelationshipNudge(name, null, diffDays, status))
                }

                if (list.isEmpty()) {
                    list.add(RelationshipNudge("Mama", "123456789", 15, "Pilne"))
                    list.add(RelationshipNudge("Karol Kowalski (Mentor)", "987654321", 8, "Wymaga kontaktu"))
                    list.add(RelationshipNudge("Anna Nowak", "555666777", 2, "Stabilna relacja"))
                }
                
                list.sortByDescending { it.lastContactDays }
                _relationshipNudges.value = list
            } catch (e: Exception) {
                Log.e("ColonyViewModel", "Error fetching relationship nudges", e)
                _relationshipNudges.value = listOf(
                    RelationshipNudge("Mama", "123456789", 15, "Pilne"),
                    RelationshipNudge("Karol Kowalski (Mentor)", "987654321", 8, "Wymaga kontaktu"),
                    RelationshipNudge("Anna Nowak", "555666777", 2, "Stabilna relacja")
                )
            }
        }
    }

    fun getSentimentLogsForAgent(agentName: String) = repository.getSentimentLogsForAgent(agentName)
    
    fun recordAgentMood(agentName: String, moodInfo: com.example.data.AgentMoodInfo) {
        viewModelScope.launch {
            repository.insertSentimentLog(
                com.example.data.AgentSentimentLog(
                    agentName = agentName,
                    emoji = moodInfo.emoji,
                    moodTitle = moodInfo.moodTitle
                )
            )
        }
    }

    // Self-Healing Network
    fun triggerSelfHealingScan() {
        viewModelScope.launch(Dispatchers.IO) {
            val proposal = com.example.data.SelfHealingProposal(
                missionId = 1,
                errorLogSnippet = "java.lang.NullPointerException: Null agent context during payload synthesis at TaskExecutor.kt:142",
                rootCauseAnalysis = "Brak sprawdzania wartości null przy inicjalizacji kontekstu misji w pod-zadaniu #14. Agent Architekt wykrył wyścig stanów.",
                proposedCodeFix = "if (agentContext == null) { agentContext = AgentContext.createDefault(agentName) }\nval result = agentContext.executePayload(payload)",
                architectAgentName = "Architekt Systemowy",
                status = "Pending"
            )
            repository.colonyDao.insertSelfHealingProposal(proposal)
        }
    }

    fun applySelfHealingFix(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.updateSelfHealingProposalStatus(id, "Applied")
        }
    }

    fun rejectSelfHealingFix(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.updateSelfHealingProposalStatus(id, "Rejected")
        }
    }

    // Workflow DAG Management
    fun saveWorkflowDag(name: String, description: String, nodesJson: String, edgesJson: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dag = com.example.data.WorkflowDag(
                name = name,
                description = description,
                nodesJson = nodesJson,
                edgesJson = edgesJson
            )
            repository.colonyDao.insertWorkflowDag(dag)
        }
    }

    fun deleteWorkflowDag(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.deleteWorkflowDag(id)
        }
    }

    // Multi-Colony Management
    fun createColonyProfile(name: String, category: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = com.example.data.ColonyProfile(
                name = name,
                category = category,
                description = description,
                isCurrentActive = false
            )
            repository.colonyDao.insertColonyProfile(profile)
        }
    }

    fun switchActiveColony(activeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.setActiveColonyProfile(activeId)
        }
    }

    // Vector Embeddings & Hallucination Audit
    fun addVectorEmbeddingLog(sourceText: String, tag: String, vectorJson: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.insertVectorEmbeddingLog(
                com.example.data.VectorEmbeddingLog(
                    sourceText = sourceText,
                    tag = tag,
                    embeddingVectorJson = vectorJson
                )
            )
        }
    }

    fun addHallucinationAuditLog(promptText: String, responseText: String, factCheckScore: Float, verdict: String, checkedClaimsJson: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.colonyDao.insertHallucinationAuditLog(
                com.example.data.HallucinationAuditLog(
                    promptText = promptText,
                    responseText = responseText,
                    factCheckScore = factCheckScore,
                    verdict = verdict,
                    checkedClaimsJson = checkedClaimsJson
                )
            )
        }
    }

    fun getUserAgentMessages(agentName: String): kotlinx.coroutines.flow.Flow<List<com.example.data.UserAgentMessage>> {
        return repository.colonyDao.getUserAgentMessages(agentName)
    }

    fun insertUserAgentMessage(agentName: String, role: String, content: String) {
        viewModelScope.launch {
            repository.colonyDao.insertUserAgentMessage(
                com.example.data.UserAgentMessage(agentName = agentName, role = role, content = content)
            )
            
            // If user sent a message, get AI response
            if (role == "user" && isApiKeyConfigured) {
                try {
                    // Fetch existing messages to form a context
                    val flowMessages = repository.colonyDao.getUserAgentMessages(agentName)
                    val history = flowMessages.first()
                    val historyText = history.takeLast(10).joinToString("\n") { "${it.role}: ${it.content}" }
                    
                    val systemPrompt = "You are an AI agent named $agentName."
                    val promptText = """
                        Conversation history:
                        $historyText
                        
                        Respond as $agentName.
                    """.trimIndent()
                    
                    val response = com.example.network.AILlmClient.generateContent(getApplication(), promptText, systemPrompt)
                    
                    repository.colonyDao.insertUserAgentMessage(
                        com.example.data.UserAgentMessage(agentName = agentName, role = "model", content = response.trim())
                    )
                    

                } catch (e: Exception) {
                    e.printStackTrace()
                    repository.colonyDao.insertUserAgentMessage(
                        com.example.data.UserAgentMessage(agentName = agentName, role = "model", content = "Error: ${e.message}")
                    )
                }
            }
        }
    }

    fun clearUserAgentMessages(agentName: String) {
        viewModelScope.launch {
            repository.colonyDao.clearUserAgentMessages(agentName)
        }
    }


    val personaAgents: StateFlow<List<com.example.data.PersonaAgent>> = repository.allPersonaAgents
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertPersonaAgent(agent: com.example.data.PersonaAgent) {
        viewModelScope.launch {
            repository.insertPersonaAgent(agent)
        }
    }
    
    fun deletePersonaAgent(id: Int) {
        viewModelScope.launch {
            repository.deletePersonaAgentById(id)
        }
    }

}