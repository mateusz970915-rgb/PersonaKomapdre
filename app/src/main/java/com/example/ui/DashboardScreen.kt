package com.example.ui

import com.example.ui.components.AgentCard
import com.example.ui.components.TranslateTextInPlace
import com.example.ui.components.FocusModeSuite
import com.example.ui.components.VoiceCommandDialog
import com.example.ui.components.RelationshipNudgesWidget

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Mic
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.Agent
import com.example.data.CouncilMessage
import com.example.data.SubTask
import com.example.viewmodel.ColonyViewModel
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ColonyViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToCouncil: () -> Unit,
    onNavigateToEfficiency: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToBehaviors: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToSuggested: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddAgent: () -> Unit = {},
    onNavigateToBadges: () -> Unit,
    onNavigateToInterAgentChat: () -> Unit = {},
    onNavigateToTaskBoard: () -> Unit = {},
    onNavigateToProgression: () -> Unit = {},
    onNavigateToPersonaColony: () -> Unit = {},
    onNavigateToActiveAgents: () -> Unit = {},
    onNavigateToAgentDashboard: () -> Unit = {},
    onNavigateToEvolution: () -> Unit = {},
    onNavigateToEddeConsole: () -> Unit = {},
    onNavigateToSmartFinance: () -> Unit = {},
    onNavigateToCalendarIntel: () -> Unit = {},
    onNavigateToWebAnalyzer: () -> Unit = {},
    onNavigateToKnowledgeGraph: () -> Unit = {},
    onNavigateToAgentBuilder: () -> Unit = {},
    onNavigateToStudy: () -> Unit = {},
    onNavigateToSleepOptimizer: () -> Unit = {},
    onNavigateToPhase5: () -> Unit = {}
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val agents by viewModel.agents.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val subTasksList by viewModel.subTasks.collectAsState()
    val decisions by viewModel.decisions.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val missionLogs by viewModel.missionStateLogs.collectAsState()
    val negotiations by viewModel.negotiations.collectAsState()
    val meshTelemetry by viewModel.meshTelemetry.collectAsState()
    val knowledgeEdges by viewModel.knowledgeEdges.collectAsState()
    val customAgents by viewModel.customAgentDefinitions.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val sleepRecords by viewModel.sleepRecords.collectAsState()
    val transactions by viewModel.financeTransactions.collectAsState()
    
    val preferences by viewModel.agentPreferencesState.collectAsState()
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                        val jsonContent = reader.readText()
                        if (jsonContent.trim().startsWith("[")) {
                            val jsonArray = org.json.JSONArray(jsonContent)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                viewModel.addAgent(
                                    name = obj.getString("name"),
                                    type = obj.getString("type"),
                                    role = obj.getString("role"),
                                    permissions = obj.optString("permissions", "Basic"),
                                    traits = obj.optString("traits", ""),
                                    systemPrompt = obj.optString("systemPrompt", "")
                                )
                            }
                        } else if (jsonContent.trim().startsWith("{")) {
                            val obj = org.json.JSONObject(jsonContent)
                            viewModel.addAgent(
                                name = obj.getString("name"),
                                type = obj.getString("type"),
                                role = obj.getString("role"),
                                permissions = obj.optString("permissions", "Basic"),
                                traits = obj.optString("traits", ""),
                                systemPrompt = obj.optString("systemPrompt", "")
                            )
                        }
                        android.widget.Toast.makeText(context, "Agent(s) imported successfully", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Failed to import agents", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    val isFocusModeActive = preferences.isFocusModeActive
    val relationshipNudges by viewModel.relationshipNudges.collectAsState()

    
    // Auto prediction trigger on load
    LaunchedEffect(calendarEvents) {
        if (calendarEvents.isNotEmpty()) {
            viewModel.analyzeCalendarEventsWithGemini()
        }
    }

    // Relationship Nudges trigger on load
    LaunchedEffect(Unit) {
        viewModel.fetchRelationshipNudges(context)
    }
    
    // Backup state
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFilePath by remember { mutableStateOf("") }
    var exportJsonContent by remember { mutableStateOf("") }
    
    var showCreateDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedRoleFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Name") } // "Name", "Role", "Status"
    
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedAgents = remember { mutableStateListOf<Agent>() }
    var showPriorityDialog by remember { mutableStateOf(false) }
    
    var selectedAgentForActivity by remember { mutableStateOf<Agent?>(null) }

    val availableRoles = remember(agents) {
        listOf("All") + agents.map { it.type }.distinct().filter { it.isNotBlank() }
    }

    val filteredAgents = remember(agents, searchQuery, selectedStatusFilter, selectedRoleFilter, sortBy, subTasksList) {
        var result = agents.filter { agent ->
            agent.name.contains(searchQuery, ignoreCase = true)
        }
        
        if (selectedStatusFilter != "All") {
            result = result.filter { agent ->
                val isWorking = subTasksList.any { task ->
                    task.assignedAgent.equals(agent.name, ignoreCase = true) && task.status == "In Progress"
                }
                val statusText = when {
                    agent.status == "Resting" -> "Resting"
                    agent.status != "Active" -> "Idle"
                    isWorking -> "Working"
                    else -> "Active"
                }
                statusText.equals(selectedStatusFilter, ignoreCase = true)
            }
        }
        
        if (selectedRoleFilter != "All") {
            result = result.filter { agent ->
                agent.type.equals(selectedRoleFilter, ignoreCase = true)
            }
        }
        
        when (sortBy) {
            "Name", "name" -> result.sortedBy { it.name.lowercase() }
            "Role" -> result.sortedBy { it.type.lowercase() }
            "Status" -> result.sortedBy { it.status.lowercase() }
            "last_active" -> result.sortedByDescending { it.lastActiveTimestamp }
            else -> result
        }
    }

    val agentActivityLog = remember<List<ActivityEntry>>(selectedAgentForActivity, decisions, subTasksList) {
        val agent = selectedAgentForActivity ?: return@remember emptyList<ActivityEntry>()
        val entries = mutableListOf<ActivityEntry>()
        
        decisions.filter { it.agentName.equals(agent.name, ignoreCase = true) }.forEach { d ->
            entries.add(
                ActivityEntry(
                    title = d.actionDescription,
                    description = "Confidence: ${d.confidenceLevel}. Data used: ${d.dataUsed}",
                    timestamp = d.timestamp,
                    type = "decision",
                    metadata = d.dissentingOpinions
                )
            )
        }
        
        subTasksList.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) }.forEach { s ->
            entries.add(
                ActivityEntry(
                    title = "SubTask: ${s.description}",
                    description = "Status: ${s.status}",
                    timestamp = s.timestamp,
                    type = "task"
                )
            )
        }
        
        entries.sortByDescending { it.timestamp }
        entries
    }

    val activityModel = remember(decisions, subTasksList) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val counts = FloatArray(7) { 0f }
        
        val decisionsList: List<com.example.data.AgentDecision> = decisions
        val tasksList: List<com.example.data.SubTask> = subTasksList
        
        decisionsList.forEach { d ->
            val diff = ((now - d.timestamp) / oneDayMs).toInt()
            if (diff in 0..6) {
                counts[6 - diff] += 1f
            }
        }
        
        tasksList.forEach { s ->
            val diff = ((now - s.timestamp) / oneDayMs).toInt()
            if (diff in 0..6) {
                counts[6 - diff] += 1f
            }
        }

        entryModelOf(
            counts[0],
            counts[1],
            counts[2],
            counts[3],
            counts[4],
            counts[5],
            counts[6]
        )
    }

    Scaffold(
        topBar = {
            if (isMultiSelectMode) {
                TopAppBar(
                    title = { Text("${selectedAgents.size} Selected", style = MaterialTheme.typography.titleMedium) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            isMultiSelectMode = false
                            selectedAgents.clear()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel multi-select")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (selectedAgents.isNotEmpty()) {
                                viewModel.bulkPauseAgents(selectedAgents.toList(), true)
                                selectedAgents.clear()
                                isMultiSelectMode = false
                            }
                        }, modifier = Modifier.testTag("bulk_pause_agents_btn")) {
                            Icon(Icons.Filled.Pause, contentDescription = "Pause Selected Agents")
                        }

                        IconButton(onClick = {
                            if (selectedAgents.isNotEmpty()) {
                                viewModel.bulkPauseAgents(selectedAgents.toList(), false)
                                selectedAgents.clear()
                                isMultiSelectMode = false
                            }
                        }, modifier = Modifier.testTag("bulk_resume_agents_btn")) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Resume Selected Agents")
                        }

                        IconButton(onClick = {
                            if (selectedAgents.isNotEmpty()) {
                                com.example.utils.ExportRoomBackupHelper.exportFullRoomBackupToJson(
                                    context = context,
                                    agents = selectedAgents.toList(),
                                    missions = viewModel.missions.value,
                                    subTasks = subTasksList,
                                    decisions = decisions,
                                    missionLogs = missionLogs,
                                    memories = viewModel.memories.value
                                ) { path, json ->
                                    exportFilePath = path
                                    exportJsonContent = json
                                    showExportDialog = true
                                }
                            }
                        }, modifier = Modifier.testTag("bulk_export_json_btn")) {
                            Icon(Icons.Default.Share, contentDescription = "Export Full Backup JSON")
                        }

                        IconButton(onClick = {
                            if (selectedAgents.isNotEmpty()) {
                                showPriorityDialog = true
                            }
                        }) {
                            Icon(Icons.Filled.Tune, contentDescription = "Reassign Autonomy")
                        }

                        IconButton(onClick = {
                            if (selectedAgents.isNotEmpty()) {
                                viewModel.bulkDeleteAgents(selectedAgents.map { it.id })
                                selectedAgents.clear()
                                isMultiSelectMode = false
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("PersonaMesh") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    actions = {
                        var showSortMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Sort Options")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sort by Last Active") },
                                onClick = {
                                    sortBy = "last_active"
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Name") },
                                onClick = {
                                    sortBy = "name"
                                    showSortMenu = false
                                }
                            )
                        }
                        Button(
                            onClick = { viewModel.triggerPanic() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("PANIC")
                        }
                        IconButton(onClick = onNavigateToSuggested, modifier = Modifier.testTag("nav_to_suggested_btn")) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Suggested Agents")
                        }
                        IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("nav_to_settings_btn")) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = onNavigateToChat) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Council Chat")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isFocusModeActive) {
                var showVoiceDialog by remember { mutableStateOf(false) }
                if (showVoiceDialog) {
                    VoiceCommandDialog(
                        viewModel = viewModel,
                        onDismiss = { showVoiceDialog = false },
                        onNavigateToTaskBoard = onNavigateToTaskBoard,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToAgentBuilder = onNavigateToAgentBuilder,
                        onNavigateToSmartFinance = onNavigateToSmartFinance,
                        onNavigateToSleepOptimizer = onNavigateToSleepOptimizer
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = { viewModel.updateFocusModeActive(true) },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.testTag("fab_toggle_focus_mode")
                    ) {
                        Icon(Icons.Filled.Psychology, contentDescription = "Głębokie skupienie")
                    }

                    FloatingActionButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("fab_import_agent")
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Import Agent")
                    }
                    FloatingActionButton(
                        onClick = { showVoiceDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("fab_voice_commands")
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Asystent głosowy")
                    }

                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("fab_add_agent")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Agent")
                    }
                }
            }
        }
    ) { padding ->
        if (isFocusModeActive) {
            FocusModeSuite(
                viewModel = viewModel,
                padding = padding
            )
        } else {
            PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel?.refreshColonyData()
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(


                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (isOffline) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CloudOff,
                                    contentDescription = "Offline Mode",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tryb offline: operating locally, sync pending.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    
                    // 1. SEARCH BAR
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search agents by name...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("search_bar_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_mesh_hero),
                        contentDescription = "PersonaMesh Hero",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .padding(bottom = 12.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = onNavigateToActiveAgents, modifier = Modifier.testTag("nav_to_active_agents_btn")) {
                            Text("Active Agents")
                        }
                        Button(onClick = onNavigateToAgentDashboard) { Text("Agent Dashboard") }
                        Button(onClick = onNavigateToCouncil) {
                            Text("Council")
                        }
                        Button(onClick = onNavigateToMissions) {
                            Text("Missions")
                        }
                        Button(onClick = onNavigateToTaskBoard, modifier = Modifier.testTag("nav_to_task_board_btn")) {
                            Text("Task Board")
                        }
                        Button(onClick = onNavigateToInterAgentChat, modifier = Modifier.testTag("nav_to_inter_agent_chat_btn")) {
                            Text("Agent Chat")
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = onNavigateToPersonaColony, modifier = Modifier.testTag("nav_to_personas_btn")) {
                            Text("Personas")
                        }
                        Button(onClick = onNavigateToEfficiency) {
                            Text("Stats")
                        }
                        Button(onClick = onNavigateToBadges, modifier = Modifier.testTag("nav_to_badges_btn")) {
                            Text("Badges")
                        }
                        Button(onClick = onNavigateToProgression, modifier = Modifier.testTag("nav_to_progression_btn")) {
                            Text("Unlocks")
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = onNavigateToPrivacy) {
                            Text("Privacy")
                        }
                        Button(onClick = onNavigateToBehaviors) {
                            Text("Rules")
                        }
                        Button(onClick = onNavigateToMarket) {
                            Text("Market")
                        }
                        Button(
                            onClick = onNavigateToEvolution, 
                            modifier = Modifier.testTag("nav_to_evolution_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Evolution")
                        }
                        Button(
                            onClick = onNavigateToEddeConsole, 
                            modifier = Modifier.testTag("nav_to_edde_console_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("EDDE CLI")
                        }
                    }

                    // FAZA 2: HIGH (Główna Wartość Dodana) buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onNavigateToSmartFinance,
                            modifier = Modifier.testTag("nav_to_finance_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Finance")
                        }
                        Button(
                            onClick = onNavigateToCalendarIntel,
                            modifier = Modifier.testTag("nav_to_calendar_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Calendar")
                        }
                        Button(
                            onClick = onNavigateToWebAnalyzer,
                            modifier = Modifier.testTag("nav_to_analyzer_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Scraper")
                        }
                        Button(
                            onClick = onNavigateToKnowledgeGraph,
                            modifier = Modifier.testTag("nav_to_graph_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Graph")
                        }
                    }

                    // FAZA 3: MEDIUM (Inteligentne Rozszerzenia) buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onNavigateToAgentBuilder,
                            modifier = Modifier.testTag("nav_to_builder_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Agent Builder")
                        }
                        Button(
                            onClick = onNavigateToStudy,
                            modifier = Modifier.testTag("nav_to_study_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Study Repeater")
                        }
                        Button(
                            onClick = onNavigateToSleepOptimizer,
                            modifier = Modifier.testTag("nav_to_sleep_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Sleep Optimizer")
                        }
                    }

                    // FAZA 5: EVOLUTION
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onNavigateToPhase5,
                            modifier = Modifier.testTag("nav_to_phase5_btn").fillMaxWidth(0.9f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Phase 5: Evolution Engine (On-Device LLM & Sandbox)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                com.example.utils.ExportRoomBackupHelper.exportFullRoomBackupToJson(
                                    context = context,
                                    agents = agents,
                                    missions = viewModel.missions.value,
                                    subTasks = subTasksList,
                                    decisions = decisions,
                                    missionLogs = missionLogs,
                                    memories = viewModel.memories.value,
                                    negotiations = negotiations,
                                    meshTelemetry = meshTelemetry,
                                    knowledgeEdges = knowledgeEdges
                                ) { path, json ->

                                    exportFilePath = path
                                    exportJsonContent = json
                                    showExportDialog = true
                                }
                            },
                            modifier = Modifier.testTag("export_backup_json_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Room Backup")
                        }

                        OutlinedButton(
                            onClick = {
                                val topAgent = agents.firstOrNull()?.name ?: "Alpha Agent"
                                com.example.utils.NotificationHelper.sendHighPriorityMissionNotification(
                                    context = context,
                                    missionId = (100..999).random(),
                                    agentName = topAgent,
                                    missionGoal = "Urgent Colony Security & Data Backup Verification"
                                )
                                android.widget.Toast.makeText(context, "High-priority notification sent!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("trigger_mission_notification_btn")
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Mission Alert")
                        }
                    }

                    // Cross-Agent Intelligence Aggregation Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .testTag("cross_agent_intelligence_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cross-Agent Intelligence Hub",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Stats Grid or Rows
                            val totalSubCost = subscriptions.sumOf { it.amount }
                            val activeSubsCount = subscriptions.count { !it.isCancelled }
                            val avgRecovery = if (sleepRecords.isNotEmpty()) sleepRecords.map { it.recoveryScore }.average().toInt() else 0
                            val customAgentsCount = customAgents.size

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Column 1: Financial Security
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "FINANCIAL SECURITY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Monthly Subs: $${String.format(Locale.US, "%.2f", totalSubCost)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$activeSubsCount Active Services",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Column 2: Health & Recovery
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "HEALTH & RECOVERY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Avg Recovery: $avgRecovery%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when {
                                            avgRecovery >= 80 -> MaterialTheme.colorScheme.primary
                                            avgRecovery >= 50 -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.error
                                        }
                                    )
                                    val sleepStatus = if (avgRecovery >= 75) "Optimal Sleep" else "Needs Recovery"
                                    Text(
                                        text = sleepStatus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Column 3: Autonomous Agents
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "AUTONOMOUS BUILDER",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$customAgentsCount Custom Models",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Active and persistent",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Column 4: System Audit Status
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "SYSTEM INTEGRITY AUDIT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "SECURE (100%)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                    Text(
                                        text = "EDDE-14 loop active",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Colony Activity & Throughput (Last 24 Hours)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Chart(
                                chart = lineChart(),
                                model = activityModel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("7 Days Ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AgentActivityHeatmapWidget(
                        subTasks = subTasksList,
                        decisions = decisions,
                        missionLogs = missionLogs,
                        memories = viewModel.memories.value,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    AgentStatusNotesWidget(
                        agents = agents,
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    RelationshipNudgesWidget(
                        nudges = relationshipNudges,
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    AgentNegotiationConsoleWidget(
                        negotiations = negotiations,
                        agents = agents,
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    AgentConsensusAnalyticsWidget(
                        negotiations = negotiations,
                        agents = agents,
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    AgentMeshTelemetryWidget(
                        telemetryList = meshTelemetry,
                        agents = agents,
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    AgentKnowledgeGraphWidget(
                        knowledgeEdges = knowledgeEdges,
                        agents = agents,
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )


                    TaskCompletionRateWidget(
                        subTasksList = subTasksList,
                        missionLogs = missionLogs,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    CategoryColorSchemePickerWidget(
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Colony Agents",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(
                            onClick = {
                                isMultiSelectMode = !isMultiSelectMode
                                if (!isMultiSelectMode) {
                                    selectedAgents.clear()
                                }
                            },
                            modifier = Modifier.testTag("bulk_select_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isMultiSelectMode) Icons.Default.CheckCircle else Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (isMultiSelectMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMultiSelectMode) "Done" else "Multi-Select",
                                color = if (isMultiSelectMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    // 2. FILTER CHIPS (Status & Assigned Role) & SORT OPTIONS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status Filters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.width(55.dp)
                            )
                            val statusFilters = listOf("All", "Active", "Working", "Idle", "Resting")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                statusFilters.forEach { status ->
                                    val isSelected = selectedStatusFilter == status
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedStatusFilter = status },
                                        label = { Text(status) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        modifier = Modifier.testTag("status_filter_$status")
                                    )
                                }
                            }
                        }

                        // Role Filters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Role: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.width(55.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableRoles.forEach { role ->
                                    val isSelected = selectedRoleFilter == role
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedRoleFilter = role },
                                        label = { Text(role) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        modifier = Modifier.testTag("role_filter_$role")
                                    )
                                }
                            }
                        }

                        // Sorting controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sort: ",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.width(55.dp)
                                )
                                val sortOptions = listOf("Name", "Role", "Status", "Recent Activity")
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState())
                                ) {
                                    sortOptions.forEach { opt ->
                                        val isSelected = sortBy == opt
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { sortBy = opt },
                                            label = { Text(opt) },
                                            modifier = Modifier.testTag("sort_$opt")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredAgents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "No Agents",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No agents found.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add an agent to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateToAddAgent) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Agent")
                        }
                    }
                }
            } else {
                items(filteredAgents, key = { it.id }) { agent ->
                    val isWorking = subTasksList.any { task ->
                        task.assignedAgent.equals(agent.name, ignoreCase = true) && task.status == "In Progress"
                    }
                    val isSelected = selectedAgents.contains(agent)
                    val catHex = viewModel.getCategoryColorHex(agent.type)
                    val haptic = LocalHapticFeedback.current
                    
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteAgent(agent.id)
                                true
                            } else {
                                false
                            }
                        }
                    )
                    
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                else -> androidx.compose.ui.graphics.Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp)
                                    .background(color, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Agent",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        AgentCard(
                            agent = agent,
                            isWorking = isWorking,
                            isSelectionMode = isMultiSelectMode,
                            isSelected = isSelected,
                            categoryColorHex = catHex,
                            prediction = predictions[agent.id],
                            subTasks = subTasksList,
                            onSelectToggle = {
                                if (isSelected) {
                                    selectedAgents.remove(agent)
                                } else {
                                    selectedAgents.add(agent)
                                }
                            },
                            onTap = {
                                if (isMultiSelectMode) {
                                    if (isSelected) {
                                        selectedAgents.remove(agent)
                                    } else {
                                        selectedAgents.add(agent)
                                    }
                                } else {
                                    selectedAgentForActivity = agent
                                }
                            },
                            onPauseToggle = { viewModel.toggleAgentStatus(agent) },
                            onDelete = { viewModel.deleteAgent(agent.id) },
                            onExport = {
                                exportSelectedAgents(context, listOf(agent)) { path, json ->
                                    exportFilePath = path
                                    exportJsonContent = json
                                    showExportDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
        }
        
        if (!preferences.hasSeenWalkthrough) {
            WalkthroughOverlay(
                onDismiss = {
                    viewModel.updateHasSeenWalkthrough(true)
                }
            )
        }
        
        if (showExportDialog) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            val annotatedString = androidx.compose.ui.text.AnnotatedString(exportJsonContent)
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Backup Saved Successfully") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Your custom agent prompts and configurations have been successfully saved to local storage.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "File Path:\n$exportFilePath",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "JSON Payload:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .verticalScroll(rememberScrollState()),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = exportJsonContent,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboardManager.setText(annotatedString)
                            android.widget.Toast.makeText(context, "JSON copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                        },
                        modifier = Modifier.testTag("export_dialog_confirm")
                    ) {
                        Text("Copy & Close")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showCreateDialog) {
            MultiStepCreateAgentDialog(
                viewModel = viewModel,
                onDismiss = { showCreateDialog = false }
            )
        }

        if (showPriorityDialog) {
            var selectedAutonomy by remember { mutableStateOf("Needs Confirmation") }
            var selectedPerms by remember { mutableStateOf("Basic") }
            
            val autonomyLevels = listOf("Strict Control", "Needs Confirmation", "Full Autonomy")
            val permissionLevels = listOf("Basic", "Intermediate", "Admin")

            AlertDialog(
                onDismissRequest = { showPriorityDialog = false },
                title = { Text("Reassign Priorities / Autonomy") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "You are updating ${selectedAgents.size} selected agents in bulk.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Column {
                            Text("Autonomy Level", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            autonomyLevels.forEach { level ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAutonomy = level }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(selected = selectedAutonomy == level, onClick = { selectedAutonomy = level })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(level, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Column {
                            Text("Permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            permissionLevels.forEach { perms ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPerms = perms }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(selected = selectedPerms == perms, onClick = { selectedPerms = perms })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(perms, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.bulkUpdateAutonomyAndPermissions(
                                selectedAgents.toList(),
                                selectedAutonomy,
                                selectedPerms
                            )
                            selectedAgents.clear()
                            isMultiSelectMode = false
                            showPriorityDialog = false
                        }
                    ) {
                        Text("Apply to Bulk")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPriorityDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Side Sheet Overlay for Agent chronological Activity Log
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedAgentForActivity != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.36f))
                        .clickable { selectedAgentForActivity = null }
                )
            }
            
            AnimatedVisibility(
                visible = selectedAgentForActivity != null,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(0.85f)
                    .align(Alignment.CenterEnd)
            ) {
                selectedAgentForActivity?.let { agent ->
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Header
                            val agentMood = remember(agent, subTasksList) {
                                com.example.data.calculateAgentMood(agent, subTasksList)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${agent.name} ${agentMood.emoji}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { selectedAgentForActivity = null }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close sheet")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            var selectedTab by remember { mutableStateOf(0) }
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                                    Text("Metrics", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelMedium)
                                }
                                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                    Text("Logs", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelMedium)
                                }
                                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                                    Text("Config", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            when (selectedTab) {
                                0 -> {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        // Metrics Tab
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp)
                                                .testTag("agent_mood_card_${agent.id}")
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = agentMood.emoji,
                                                            style = MaterialTheme.typography.headlineMedium
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(
                                                                text = agentMood.moodTitle,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                            Text(
                                                                text = "Current Mood & Status",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = when {
                                                            agentMood.loadScore > 70 -> MaterialTheme.colorScheme.errorContainer
                                                            agentMood.loadScore > 40 -> MaterialTheme.colorScheme.tertiaryContainer
                                                            else -> MaterialTheme.colorScheme.secondaryContainer
                                                        }
                                                    ) {
                                                        Text(
                                                            text = "Workload: ${agentMood.loadScore}%",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = agentMood.description,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text("Task Load", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                                            Text("${agentMood.loadScore}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        LinearProgressIndicator(
                                                            progress = { agentMood.loadScore / 100f },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(6.dp)
                                                                .clip(RoundedCornerShape(3.dp)),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text("Complexity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                                            Text("${agentMood.complexityScore}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        LinearProgressIndicator(
                                                            progress = { agentMood.complexityScore / 100f },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(6.dp)
                                                                .clip(RoundedCornerShape(3.dp)),
                                                            color = MaterialTheme.colorScheme.tertiary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Weekly Task Productivity Heatmap & Vico Chart
                                        Text(
                                            text = "Weekly Task Productivity",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                        )
                                        
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    "Contribution Heatmap",
                                                     style = MaterialTheme.typography.labelMedium,
                                                     fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                                                val productivityByDay = remember(agent, decisions, subTasksList) {
                                                    val counts = IntArray(7) { 0 }
                                                    val now = System.currentTimeMillis()
                                                    val oneDayMs = 24 * 60 * 60 * 1000L
                                                    
                                                    // Decisions
                                                    decisions.filter { it.agentName.equals(agent.name, ignoreCase = true) }.forEach { d ->
                                                        val diff = ((now - d.timestamp) / oneDayMs).toInt()
                                                        if (diff in 0..6) {
                                                            counts[6 - diff] += 1
                                                        }
                                                    }
                                                    
                                                    // SubTasks
                                                    subTasksList.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) }.forEach { s ->
                                                        val diff = ((now - s.timestamp) / oneDayMs).toInt()
                                                        if (diff in 0..6) {
                                                            counts[6 - diff] += 1
                                                        }
                                                    }
                                                    counts
                                                }
                                                
                                                // Render 7-day contribution grid
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    days.forEachIndexed { index, day ->
                                                        val count = productivityByDay[index]
                                                        val color = when {
                                                            count == 0 -> MaterialTheme.colorScheme.surface
                                                            count in 1..2 -> MaterialTheme.colorScheme.primaryContainer
                                                            count in 3..4 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                                            else -> MaterialTheme.colorScheme.primary
                                                        }
                                                        
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(day, style = MaterialTheme.typography.labelSmall)
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(24.dp)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(color)
                                                            )
                                                            Text("${count}x", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                                                        }
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    "Productivity Trend (Vico)",
                                                     style = MaterialTheme.typography.labelMedium,
                                                     fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                // Vico Chart representing the trend
                                                val entryModel = remember(productivityByDay) {
                                                    entryModelOf(
                                                        productivityByDay[0].toFloat(),
                                                        productivityByDay[1].toFloat(),
                                                        productivityByDay[2].toFloat(),
                                                        productivityByDay[3].toFloat(),
                                                        productivityByDay[4].toFloat(),
                                                        productivityByDay[5].toFloat(),
                                                        productivityByDay[6].toFloat()
                                                    )
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(80.dp)
                                                ) {
                                                    Chart(
                                                        chart = lineChart(),
                                                        model = entryModel,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    // Logs Tab
                                    if (agentActivityLog.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "No recent actions recorded.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(agentActivityLog) { entry ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (entry.type == "decision") 
                                                             MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                        else 
                                                             MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                                    )
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = if (entry.type == "decision") "Decision Action" else "Mission Task",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (entry.type == "decision") 
                                                                     MaterialTheme.colorScheme.primary 
                                                                 else 
                                                                     MaterialTheme.colorScheme.tertiary
                                                            )
                                                            val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
                                                            Text(
                                                                text = sdf.format(Date(entry.timestamp)),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.outline
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = entry.title,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = entry.description,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        if (entry.metadata.isNotBlank()) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Dissenting Opinions: ${entry.metadata}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    // Config Tab
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Role: ${agent.role}", style = MaterialTheme.typography.bodyMedium)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Type: ${agent.type}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                if (agent.traits.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Traits: ${agent.traits}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                                }
                                            }
                                        }
                                        
                                        var editPermissions by remember(agent.permissions) { mutableStateOf(agent.permissions) }
                                        var editSystemPrompt by remember(agent.systemPrompt) { mutableStateOf(agent.systemPrompt) }
                                        
                                        OutlinedTextField(
                                            value = editPermissions,
                                            onValueChange = { editPermissions = it },
                                            label = { Text("Permissions") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedTextField(
                                            value = editSystemPrompt,
                                            onValueChange = { editSystemPrompt = it },
                                            label = { Text("System Prompt") },
                                            modifier = Modifier.fillMaxWidth().height(120.dp),
                                            textStyle = MaterialTheme.typography.bodySmall,
                                            maxLines = 5
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                // Assuming we can update the agent config
                                                // Actually we will just need a method in viewmodel or we'll add one.
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Save Configuration")
                                        }
                                    }
                                }
                            }
                        }
}
}
}
}
}
}

}


data class ActivityEntry(
    val title: String,
    val description: String,
    val timestamp: Long,
    val type: String, // "decision" or "task"
    val metadata: String = ""
)

fun exportSelectedAgents(
    context: android.content.Context,
    selected: List<Agent>,
    onComplete: (filePath: String, jsonContent: String) -> Unit
) {
    try {
        val jsonArray = org.json.JSONArray()
        selected.forEach { agent ->
            val obj = org.json.JSONObject()
            obj.put("id", agent.id)
            obj.put("name", agent.name)
            obj.put("type", agent.type)
            obj.put("role", agent.role)
            obj.put("permissions", agent.permissions)
            obj.put("traits", agent.traits)
            obj.put("systemPrompt", agent.systemPrompt)
            obj.put("status", agent.status)
            obj.put("autonomyLevel", agent.autonomyLevel)
            obj.put("iconName", agent.iconName)
            jsonArray.put(obj)
        }
        val jsonString = jsonArray.toString(4)
        
        // Save to cache directory
        val fileName = "agents_backup_${System.currentTimeMillis()}.json"
        val file = java.io.File(context.cacheDir, fileName)
        file.writeText(jsonString)
        
        // Trigger share intent
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Export Agents Backup"))
        
        onComplete(file.absolutePath, jsonString)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}




