package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Palette


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Agent
import com.example.data.InterAgentMessage
import com.example.data.MissionStateLog
import com.example.data.SubTask
import com.example.viewmodel.ColonyViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CombinedActivityLogItem(
    val id: String,
    val agentName: String,
    val actionType: String, // "Mission Log", "Inter-Agent Signal", "Task Event"
    val description: String,
    val status: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AgentDashboardScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val agents by viewModel.agents.collectAsState()
    val missionLogs by viewModel.missionStateLogs.collectAsState()
    val interAgentMessages by viewModel.interAgentMessages.collectAsState()
    val subTasks by viewModel.subTasks.collectAsState()

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedAgents = remember { mutableStateListOf<Agent>() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") } // "All", "Active", "Busy", "Idle", "Offline"
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active Agents, 1: Activity Logs
    
    var agentToEdit by remember { mutableStateOf<Agent?>(null) }
    var selectedAgentForHistory by remember { mutableStateOf<Agent?>(null) }
    var isGridView by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Map agents to their active tasks
    val agentActiveTaskMap = remember(agents, subTasks) {
        agents.associateWith { agent ->
            subTasks.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) }
                .sortedByDescending { it.timestamp }
                .firstOrNull { it.status != "Completed" }
        }
    }

    // Agent status counts
    val activeCount = agents.count { it.status.equals("Active", ignoreCase = true) }
    val busyCount = agents.count { agentActiveTaskMap[it] != null }
    val idleCount = agents.count { it.status.equals("Active", ignoreCase = true) && agentActiveTaskMap[it] == null }
    val offlineCount = agents.count { it.status.equals("Paused", ignoreCase = true) || it.status.equals("Offline", ignoreCase = true) }

    // Filter agents
    val filteredAgents = remember(agents, searchQuery, selectedStatusFilter, agentActiveTaskMap) {
        agents.filter { agent ->
            val matchesSearch = searchQuery.isBlank() ||
                    agent.name.contains(searchQuery, ignoreCase = true) ||
                    agent.type.contains(searchQuery, ignoreCase = true) ||
                    agent.role.contains(searchQuery, ignoreCase = true) ||
                    agent.status.contains(searchQuery, ignoreCase = true)

            val currentTask = agentActiveTaskMap[agent]
            val matchesFilter = when (selectedStatusFilter) {
                "Favorites" -> agent.isFavorite
                "Active" -> agent.status.equals("Active", ignoreCase = true)
                "Busy" -> currentTask != null
                "Idle" -> agent.status.equals("Active", ignoreCase = true) && currentTask == null
                "Offline" -> agent.status.equals("Paused", ignoreCase = true) || agent.status.equals("Offline", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesFilter
        }.sortedWith(compareBy<com.example.data.Agent> { !it.isFavorite }.thenBy { it.name })
    }

    // Combined activity logs sorted chronologically
    val combinedActivityLogs = remember(missionLogs, interAgentMessages, subTasks) {
        val list = mutableListOf<CombinedActivityLogItem>()
        
        missionLogs.forEach { log ->
            list.add(
                CombinedActivityLogItem(
                    id = "ml_${log.id}",
                    agentName = log.agentName,
                    actionType = "Mission Log",
                    description = log.message,
                    status = log.newState,
                    timestamp = log.timestamp
                )
            )
        }

        interAgentMessages.forEach { msg ->
            val target = if (msg.targetAgentName != null) " -> ${msg.targetAgentName}" else ""
            list.add(
                CombinedActivityLogItem(
                    id = "iam_${msg.id}",
                    agentName = msg.senderAgentName,
                    actionType = "Inter-Agent Signal",
                    description = "[${msg.topic}$target] ${msg.content}",
                    status = "Sent",
                    timestamp = msg.timestamp
                )
            )
        }

        subTasks.forEach { st ->
            list.add(
                CombinedActivityLogItem(
                    id = "st_${st.id}",
                    agentName = st.assignedAgent,
                    actionType = "Task Event",
                    description = "Task: ${st.description} (${st.status})",
                    status = st.status,
                    timestamp = st.timestamp
                )
            )
        }

        list.sortedByDescending { it.timestamp }
    }

    var showThemeMenu by remember { mutableStateOf(false) }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val json = kotlinx.serialization.json.Json.encodeToString(agents)
                        outputStream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Agents exported successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.testTag("agent_dashboard_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            if (selectedAgents.size == filteredAgents.size) {
                                selectedAgents.clear()
                            } else {
                                selectedAgents.clear()
                                selectedAgents.addAll(filteredAgents)
                            }
                        }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Select All")
                        }
                        IconButton(onClick = {
                            if (selectedAgents.isNotEmpty()) {
                                val deletedAgents = selectedAgents.toList()
                                viewModel.bulkDeleteAgents(deletedAgents.map { it.id })
                                selectedAgents.clear()
                                isMultiSelectMode = false
                                
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${deletedAgents.size} agent(s) deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreAgents(deletedAgents)
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text("Active Agents Dashboard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("$activeCount Active • $busyCount Busy • $idleCount Idle • $offlineCount Offline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("dashboard_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showThemeMenu = true }, modifier = Modifier.testTag("theme_toggle_btn")) {
                                Icon(Icons.Default.Palette, contentDescription = "Toggle Theme")
                            }
                            DropdownMenu(
                                expanded = showThemeMenu,
                                onDismissRequest = { showThemeMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Default") },
                                    onClick = { 
                                        coroutineScope.launch { viewModel.updateThemeMode("Default") }
                                        showThemeMenu = false 
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Personal (Zen)") },
                                    onClick = { 
                                        coroutineScope.launch { viewModel.updateThemeMode("Zen") }
                                        showThemeMenu = false 
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Work (Deep Focus)") },
                                    onClick = { 
                                        coroutineScope.launch { viewModel.updateThemeMode("DeepFocus") }
                                        showThemeMenu = false 
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Creative") },
                                    onClick = { 
                                        coroutineScope.launch { viewModel.updateThemeMode("Creative") }
                                        showThemeMenu = false 
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { 
                            isMultiSelectMode = true 
                            selectedAgents.clear()
                        }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Select")
                        }
                        IconButton(onClick = { isGridView = !isGridView }, modifier = Modifier.testTag("toggle_view_btn")) {
                            Icon(if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView, contentDescription = "Toggle Grid/List")
                        }
                        IconButton(onClick = { exportLauncher.launch("active_agents_backup.json") }, modifier = Modifier.testTag("export_agents_btn")) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export JSON")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (isMultiSelectMode && selectedAgents.isNotEmpty()) {
                var showBulkMenu by remember { mutableStateOf(false) }
                
                Box {
                    FloatingActionButton(
                        onClick = { showBulkMenu = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Bulk Actions")
                    }
                    
                    DropdownMenu(
                        expanded = showBulkMenu,
                        onDismissRequest = { showBulkMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Set Active (Refresh)") },
                            onClick = {
                                viewModel.bulkPauseAgents(selectedAgents.toList(), false)
                                showBulkMenu = false
                                isMultiSelectMode = false
                                selectedAgents.clear()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Set Offline") },
                            onClick = {
                                viewModel.bulkPauseAgents(selectedAgents.toList(), true)
                                showBulkMenu = false
                                isMultiSelectMode = false
                                selectedAgents.clear()
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Stats Cards
            AgentStatusOverviewCard(
                totalCount = agents.size,
                activeCount = activeCount,
                busyCount = busyCount,
                idleCount = idleCount,
                offlineCount = offlineCount,
                logCount = combinedActivityLogs.size,
                onPauseAll = {
                    coroutineScope.launch {
                        viewModel.bulkPauseAgents(agents.filter { it.status == "Active" }, true)
                        snackbarHostState.showSnackbar("All active agents paused")
                    }
                },
                onResumeAll = {
                    coroutineScope.launch {
                        viewModel.bulkPauseAgents(agents.filter { it.status == "Paused" || it.status == "Offline" }, false)
                        snackbarHostState.showSnackbar("All agents resumed")
                    }
                }
            )

            // Section Tabs (Active Agents vs Recent Activity Logs)
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("dashboard_tab_row")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Agents (${filteredAgents.size})") },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_active_agents")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Activity Logs (${combinedActivityLogs.size})") },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_activity_logs")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> {
                    // Active Agents View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Search bar & Status Filter Chips
                        var expandedSearch by remember { mutableStateOf(false) }
                        val searchSuggestions = remember(agents, searchQuery) {
                            if (searchQuery.isBlank()) emptyList()
                            else {
                                val nameMatches = agents.filter { it.name.contains(searchQuery, ignoreCase = true) }.map { it.name }
                                val roleMatches = agents.filter { it.role.contains(searchQuery, ignoreCase = true) }.map { it.role }
                                val tagsMatches = agents.filter { it.type.contains(searchQuery, ignoreCase = true) }.map { it.type }
                                (nameMatches + roleMatches + tagsMatches).distinct().take(5)
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expandedSearch,
                            onExpandedChange = { expandedSearch = !expandedSearch }
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it 
                                    expandedSearch = it.isNotBlank() && searchSuggestions.isNotEmpty()
                                },
                                placeholder = { Text("Search agents by name, role, or status...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { 
                                            searchQuery = "" 
                                            expandedSearch = false
                                        }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .testTag("agent_search_bar")
                            )

                            ExposedDropdownMenu(
                                expanded = expandedSearch && searchSuggestions.isNotEmpty(),
                                onDismissRequest = { expandedSearch = false }
                            ) {
                                searchSuggestions.forEach { suggestion ->
                                    DropdownMenuItem(
                                        text = { Text(suggestion) },
                                        onClick = {
                                            searchQuery = suggestion
                                            expandedSearch = false
                                        }
                                    )
                                }
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("status_filter_chips")
                        ) {
                            val filters = listOf("All", "Favorites", "Active", "Busy", "Idle", "Offline")
                            items(filters) { filter ->
                                FilterChip(
                                    selected = selectedStatusFilter == filter,
                                    onClick = { selectedStatusFilter = filter },
                                    label = { Text(filter) },
                                    modifier = Modifier.testTag("chip_filter_$filter")
                                )
                            }
                        }

                        if (filteredAgents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.GroupOff,
                                        contentDescription = "No agents",
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (agents.isEmpty()) "No registered agents found." else "No agents matching filter \"$selectedStatusFilter\".",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else if (isGridView) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(160.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                modifier = Modifier.fillMaxSize().testTag("agent_grid_view")
                            ) {
                                items(filteredAgents, key = { it.id }) { agent ->
                                    val isSelected = selectedAgents.contains(agent)
                                    ActiveAgentGridCard(
                                        agent = agent,
                                        activeTask = agentActiveTaskMap[agent],
                                        isSelectionMode = isMultiSelectMode,
                                        isSelected = isSelected,
                                        onSelectToggle = {
                                            if (isSelected) selectedAgents.remove(agent) else selectedAgents.add(agent)
                                        },
                                        onEdit = { agentToEdit = agent },
                                        onViewHistory = { selectedAgentForHistory = agent },
                                        onTogglePause = {
                                            viewModel.bulkPauseAgents(listOf(agent), agent.status == "Active")
                                        },
                                        onToggleFavorite = { viewModel.toggleAgentFavorite(agent) }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                modifier = Modifier.fillMaxSize().testTag("agent_list_view")
                            ) {
                                items(filteredAgents, key = { it.id }) { agent ->
                                    val isSelected = selectedAgents.contains(agent)
                                    ActiveAgentListCard(
                                        agent = agent,
                                        activeTask = agentActiveTaskMap[agent],
                                        isSelectionMode = isMultiSelectMode,
                                        isSelected = isSelected,
                                        onSelectToggle = {
                                            if (isSelected) selectedAgents.remove(agent) else selectedAgents.add(agent)
                                        },
                                        onEdit = { agentToEdit = agent },
                                        onViewHistory = { selectedAgentForHistory = agent },
                                        onTogglePause = {
                                            viewModel.bulkPauseAgents(listOf(agent), agent.status == "Active")
                                        },
                                        onToggleFavorite = { viewModel.toggleAgentFavorite(agent) }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Activity Logs View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Recent Agent Activity & Execution Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (combinedActivityLogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recent agent activity logs recorded.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                modifier = Modifier.fillMaxSize().testTag("activity_logs_list")
                            ) {
                                items(combinedActivityLogs, key = { it.id }) { item ->
                                    ActivityLogCard(item = item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (agentToEdit != null) {
        EditAgentDialog(
            agent = agentToEdit!!,
            onDismiss = { agentToEdit = null },
            onSave = { name, type ->
                viewModel.updateAgentDetails(agentToEdit!!.id, name, type)
                agentToEdit = null
            }
        )
    }

    selectedAgentForHistory?.let { agent ->
        AgentActivityHistoryBottomSheet(
            agent = agent,
            logs = combinedActivityLogs.filter { it.agentName.equals(agent.name, ignoreCase = true) },
            viewModel = viewModel,
            onDismiss = { selectedAgentForHistory = null }
        )
    }
}

@Composable
fun AgentStatusOverviewCard(
    totalCount: Int,
    activeCount: Int,
    busyCount: Int,
    idleCount: Int,
    offlineCount: Int,
    logCount: Int,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("agent_status_overview_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Agent Swarm Health",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalCount Agents • $logCount Executed Activity Events",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onPauseAll, modifier = Modifier.testTag("pause_all_btn")) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause All", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onResumeAll, modifier = Modifier.testTag("resume_all_btn")) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume All", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusBadgeMetric(count = activeCount, label = "Active", color = Color(0xFF4CAF50), icon = Icons.Default.CheckCircle)
                StatusBadgeMetric(count = busyCount, label = "Busy", color = Color(0xFFFF9800), icon = Icons.Default.Pending)
                StatusBadgeMetric(count = idleCount, label = "Idle", color = Color(0xFF2196F3), icon = Icons.Default.TaskAlt)
                StatusBadgeMetric(count = offlineCount, label = "Offline", color = MaterialTheme.colorScheme.outline, icon = Icons.Default.DoNotDisturbOn)
            }
        }
    }
}

@Composable
private fun StatusBadgeMetric(
    count: Int,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(text = "$count", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ActiveAgentListCard(
    agent: Agent,
    activeTask: SubTask?,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
    onEdit: () -> Unit,
    onViewHistory: () -> Unit,
    onTogglePause: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    val isOnline = agent.status.equals("Active", ignoreCase = true)
    val isBusy = activeTask != null

    val (statusLabel, statusColor) = when {
        !isOnline -> "Offline" to MaterialTheme.colorScheme.outline
        isBusy -> "Busy" to Color(0xFFFF9800)
        else -> "Idle" to Color(0xFF4CAF50)
    }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOnline) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOnline) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val cardColor = if (isSelectionMode && isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (isSelectionMode) {
                    onSelectToggle()
                } else {
                    onViewHistory()
                }
            }
            .testTag("agent_list_card_${agent.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onSelectToggle() },
                            modifier = Modifier.testTag("agent_list_card_checkbox_${agent.id}")
                        )
                    }
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = agent.name,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd)
                                .graphicsLayer {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                    alpha = pulseAlpha
                                }
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = agent.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${agent.role} • Autonomy: ${agent.autonomyLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("agent_list_card_favorite_${agent.id}")) {
                        Icon(
                            imageVector = if (agent.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (agent.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onTogglePause, modifier = Modifier.testTag("toggle_pause_${agent.id}")) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isOnline) "Pause" else "Activate",
                            tint = if (isOnline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onViewHistory, modifier = Modifier.testTag("history_btn_${agent.id}")) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Performance Score Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Performance Index", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(agent.performanceScore * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { agent.performanceScore },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (agent.performanceScore > 0.5f) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Current Active Task Banner
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isBusy) Icons.Default.FlashOn else Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = if (isBusy) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isBusy) "Current Task: ${activeTask?.description}" else "Status: Awaiting task assignment",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveAgentGridCard(
    agent: Agent,
    activeTask: SubTask?,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
    onEdit: () -> Unit,
    onViewHistory: () -> Unit,
    onTogglePause: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    val isOnline = agent.status.equals("Active", ignoreCase = true)
    val isBusy = activeTask != null

    val (statusLabel, statusColor) = when {
        !isOnline -> "Offline" to MaterialTheme.colorScheme.outline
        isBusy -> "Busy" to Color(0xFFFF9800)
        else -> "Idle" to Color(0xFF4CAF50)
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOnline) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScaleGrid"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOnline) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlphaGrid"
    )

    val cardColor = if (isSelectionMode && isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = cardColor),
        border = if (isSelectionMode && isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (isSelectionMode) {
                    onSelectToggle()
                } else {
                    onViewHistory()
                }
            }
            .testTag("agent_grid_card_${agent.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        modifier = Modifier.testTag("agent_grid_card_checkbox_${agent.id}")
                    )
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                Box {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = agent.name,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha
                            }
                    )
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("agent_grid_card_favorite_${agent.id}")) {
                    Icon(
                        imageVector = if (agent.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (agent.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = agent.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = agent.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onTogglePause, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Pause",
                        tint = if (isOnline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onViewHistory, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ActivityLogCard(item: CombinedActivityLogItem) {
    val (icon, color) = when (item.actionType) {
        "Mission Log" -> Icons.Default.CheckCircle to Color(0xFF2196F3)
        "Inter-Agent Signal" -> Icons.AutoMirrored.Filled.Chat to Color(0xFF9C27B0)
        else -> Icons.Default.FlashOn to Color(0xFFFF9800)
    }

    val timeFormatted = remember(item.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_log_card_${item.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.agentName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.actionType,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentActivityHistoryBottomSheet(
    agent: Agent,
    logs: List<CombinedActivityLogItem>,
    viewModel: com.example.viewmodel.ColonyViewModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Column {
                    Text(agent.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Role: ${agent.role} • Status: ${agent.status}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider()
            
            var selectedTab by remember { mutableStateOf(1) }
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Config", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelMedium)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Logs", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelMedium)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Metrics", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelMedium)
                }
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                    Text("Chat", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 350.dp)) {
                        Text("Detailed Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Role: ${agent.role}", style = MaterialTheme.typography.bodyMedium)
                        Text("Type: ${agent.type}", style = MaterialTheme.typography.bodyMedium)
                        Text("Autonomy Level: ${agent.autonomyLevel}", style = MaterialTheme.typography.bodyMedium)
                        Text("Permissions: ${agent.permissions}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (agent.systemPrompt.isNotBlank()) {
                            Text("System Prompt:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text(agent.systemPrompt, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
                1 -> {
                    Text("Activity History Logs (${logs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (logs.isEmpty()) {
                        Text(
                            text = "No recorded activity logs for this agent yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 350.dp)
                        ) {
                            items(logs, key = { it.id }) { log ->
                                ActivityLogCard(item = log)
                            }
                        }
                    }
                }
                2 -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 350.dp)) {
                        Text("Historical Performance Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        val successRate = (agent.performanceScore * 100).toInt()
                        Text("Success Rate: $successRate%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { agent.performanceScore },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (agent.performanceScore > 0.7f) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tasks Executed: ${logs.count { it.actionType == "Task Event" }}", style = MaterialTheme.typography.bodyMedium)
                        Text("Messages Sent: ${logs.count { it.actionType == "Inter-Agent Signal" }}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditAgentDialog(
    agent: Agent,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(agent.name) }
    var specialty by remember { mutableStateOf(agent.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Agent") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Agent Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Specialty") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), specialty.trim()) },
                enabled = name.isNotBlank() && specialty.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
