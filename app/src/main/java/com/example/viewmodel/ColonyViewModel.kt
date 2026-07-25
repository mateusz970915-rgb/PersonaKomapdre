package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

import com.example.network.Tool
import kotlinx.serialization.json.JsonObject

class ColonyViewModel(application: Application) : BaseAgentViewModel(application) {
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
    
    private val _newlyUnlockedBadge = MutableStateFlow<com.example.data.Badge?>(null)
    val newlyUnlockedBadge: StateFlow<com.example.data.Badge?> = _newlyUnlockedBadge

    private val _newlyUnlockedAgentMilestone = MutableStateFlow<com.example.data.AgentMilestone?>(null)
    val newlyUnlockedAgentMilestone: StateFlow<com.example.data.AgentMilestone?> = _newlyUnlockedAgentMilestone

    fun dismissUnlockedBadge() {
        _newlyUnlockedBadge.value = null
    }

    fun dismissUnlockedAgentMilestone() {
        _newlyUnlockedAgentMilestone.value = null
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
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        }

    private val _apiErrorState = MutableStateFlow<String?>(null)
    val apiErrorState: StateFlow<String?> = _apiErrorState

    fun clearApiError() {
        _apiErrorState.value = null
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
            
            agents.collect {
                checkAndEnforceAgentPermissions()
            }
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

    fun bulkUpdateAutonomyAndPermissions(agentsList: List<Agent>, autonomy: String, permissions: String) {
        viewModelScope.launch {
            agentsList.forEach { agent ->
                repository.insertAgent(agent.copy(autonomyLevel = autonomy, permissions = permissions))
            }
        }
    }

    fun addAgent(name: String, type: String, role: String, permissions: String, iconName: String = "privacy", traits: String = "", systemPrompt: String = "") {
        viewModelScope.launch {
            repository.insertAgent(
                Agent(
                    name = name,
                    type = type,
                    role = role,
                    permissions = permissions,
                    iconName = iconName,
                    traits = traits,
                    systemPrompt = systemPrompt
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

            repository.updateSubTaskStatus(task.id, executionResult.status)
        }
    }
    fun updateSubTaskStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateSubTaskStatus(id, status)
            
            // Check if mission subtasks are all completed
            val task = subTasks.value.find { it.id == id }
            val mId = task?.missionId
            if (task != null && mId != null && status == "Completed") {
                val missionSubtasks = repository.getSubTasksForMission(mId).first()
                if (missionSubtasks.all { it.status == "Completed" }) {
                    val mission = missions.value.find { it.id == mId }
                    if (mission != null) {
                        repository.insertMission(mission.copy(status = "Completed"))
                        repository.insertMemory(ColonyMemory(content = "Completed colony mission: '${mission.goal}'"))
                    }
                } else {
                    repository.insertMemory(ColonyMemory(content = "Completed subtask: '${task.description}'"))
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
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
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
                        val request = GenerateContentRequest(
                            contents = listOf(Content(parts = listOf(Part(text = promptText)))),
                            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                        )
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                        }
                        val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
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

    fun createStandaloneSubTask(description: String, assignedAgent: String, status: String = "Pending") {
        viewModelScope.launch {
            repository.insertSubTask(
                SubTask(
                    missionId = null,
                    assignedAgent = assignedAgent,
                    description = description,
                    status = status
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
    fun simulateAgentDiscussion(topic: String) {
        viewModelScope.launch {
            val currentAgents = agents.value.filter { it.status != "Paused" && it.status != "Halted" }.take(3)
            if (currentAgents.isEmpty()) return@launch

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
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
                        
                        val request = GenerateContentRequest(
                            contents = listOf(Content(parts = listOf(Part(text = prompt))))
                        )
                        
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                        }
                        
                        val responseContent = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: "Analyzing topic from ${agent.role} perspective."
                        
                        discussionHistory.add("${agent.name}: $responseContent")
                        
                        sendInterAgentMessage(
                            senderAgentName = agent.name,
                            senderRole = agent.type,
                            content = responseContent,
                            topic = topic
                        )
                        delay(300)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // Return honest system error if no API key is provided
                sendInterAgentMessage(
                    senderAgentName = "System",
                    senderRole = "System Error",
                    content = "System Halted: LLM Engine Offline. Please provide a valid Gemini API Key in settings.",
                    topic = "System Alert"
                )
            }
        }
    }
}
