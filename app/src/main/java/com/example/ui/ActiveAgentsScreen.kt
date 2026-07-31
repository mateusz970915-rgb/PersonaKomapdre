package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Agent
import com.example.data.SubTask
import com.example.viewmodel.ColonyViewModel

enum class AgentVisualStatus {
    IDLE,
    BUSY,
    OFFLINE
}

fun getAgentVisualStatus(agent: Agent, currentTask: SubTask?): AgentVisualStatus {
    val isOffline = agent.status.equals("Paused", ignoreCase = true) || agent.status.equals("Offline", ignoreCase = true)
    if (isOffline) return AgentVisualStatus.OFFLINE
    return if (currentTask != null) AgentVisualStatus.BUSY else AgentVisualStatus.IDLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveAgentsScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit,
    onNavigateToTaskBoard: () -> Unit = {},
    onNavigateToMissions: () -> Unit = {},
    onNavigateToAddAgent: () -> Unit = {}
) {
    val agents by viewModel.agents.collectAsState()
    val subTasks by viewModel.subTasks.collectAsState()
    val missions by viewModel.missions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Idle, Busy, Offline, Active
    var selectedAgentForNewTask by remember { mutableStateOf<Agent?>(null) }
    var newTaskDescription by remember { mutableStateOf("") }
    var showAddAgentDialog by remember { mutableStateOf(false) }
    var selectedAgentForDetails by remember { mutableStateOf<Agent?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Map each agent to their active subtask (if any)
    val agentTaskMap = remember(agents, subTasks) {
        agents.associateWith { agent ->
            subTasks.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) }
                .sortedByDescending { it.timestamp }
                .firstOrNull { it.status != "Completed" }
        }
    }

    // Counts by visual status
    val idleCount = agents.count { getAgentVisualStatus(it, agentTaskMap[it]) == AgentVisualStatus.IDLE }
    val busyCount = agents.count { getAgentVisualStatus(it, agentTaskMap[it]) == AgentVisualStatus.BUSY }
    val offlineCount = agents.count { getAgentVisualStatus(it, agentTaskMap[it]) == AgentVisualStatus.OFFLINE }

    // Filter agents list
    val filteredAgents = remember(agents, searchQuery, selectedFilter, agentTaskMap) {
        agents.filter { agent ->
            val matchesSearch = agent.name.contains(searchQuery, ignoreCase = true) ||
                    agent.role.contains(searchQuery, ignoreCase = true) ||
                    agent.type.contains(searchQuery, ignoreCase = true) ||
                    agent.traits.contains(searchQuery, ignoreCase = true)

            val visualStatus = getAgentVisualStatus(agent, agentTaskMap[agent])
            val matchesFilter = when (selectedFilter) {
                "Idle" -> visualStatus == AgentVisualStatus.IDLE
                "Busy" -> visualStatus == AgentVisualStatus.BUSY
                "Offline" -> visualStatus == AgentVisualStatus.OFFLINE
                "Active" -> visualStatus != AgentVisualStatus.OFFLINE
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        modifier = Modifier.testTag("active_agents_screen"),


        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Active Agents",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "$idleCount Idle • $busyCount Busy • $offlineCount Offline",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("active_agents_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToMissions,
                        modifier = Modifier.testTag("active_agents_missions_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "Missions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = onNavigateToTaskBoard,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.testTag("fab_task_board")
                ) {
                    Icon(Icons.Default.TaskAlt, contentDescription = "Task Board")
                }

                ExtendedFloatingActionButton(
                    onClick = { showAddAgentDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add New Agent") },
                    text = { Text("Add Agent") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("fab_add_agent")
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Stats Overview Card with Visual Dots Legend
            item {
                ActiveAgentsSummaryCard(
                    totalAgents = agents.size,
                    idleCount = idleCount,
                    busyCount = busyCount,
                    offlineCount = offlineCount,
                    onPauseAll = {
                        viewModel.bulkPauseAgents(agents.filter { it.status == "Active" }, true)
                    },
                    onResumeAll = {
                        viewModel.bulkPauseAgents(agents.filter { it.status == "Paused" }, false)
                    }
                )
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_agents_search_input"),
                        placeholder = { Text("Search agents by name, role, or purpose...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Filter row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val filters = listOf("All", "Idle", "Busy", "Offline")
                        items(filters) { filter ->
                            val filterStatus = when (filter) {
                                "Idle" -> AgentVisualStatus.IDLE
                                "Busy" -> AgentVisualStatus.BUSY
                                "Offline" -> AgentVisualStatus.OFFLINE
                                else -> null
                            }

                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                leadingIcon = {
                                    if (filterStatus != null) {
                                        StatusDot(status = filterStatus, size = 8.dp)
                                    } else if (selectedFilter == filter) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                modifier = Modifier.testTag("filter_chip_$filter")
                            )
                        }
                    }
                }
            }

            // Empty State Handling
            if (filteredAgents.isEmpty()) {
                item {
                    EmptyAgentsCard(
                        filter = selectedFilter,
                        searchQuery = searchQuery,
                        onClearFilter = {
                            selectedFilter = "All"
                            searchQuery = ""
                        },
                        onAddAgentClick = { showAddAgentDialog = true }
                    )
                }
            } else {
                // Agent Cards List
                items(
                    items = filteredAgents,
                    key = { it.id }
                ) { agent ->
                    val currentTask = agentTaskMap[agent]
                    val missionTitle = currentTask?.let { task ->
                        missions.find { it.id == task.missionId }?.goal
                    }

                    ActiveAgentCard(
                        modifier = Modifier.animateItem(),
                        agent = agent,
                        currentTask = currentTask,
                        missionTitle = missionTitle,
                        onToggleStatus = {
                            viewModel.toggleAgentStatus(agent)
                        },
                        onCompleteTask = { taskId ->
                            viewModel.updateSubTaskStatus(taskId, "Completed")
                        },
                        onAssignTaskClick = {
                            selectedAgentForNewTask = agent
                        },
                        onCardClick = {
                            selectedAgentForDetails = agent
                        },
                        onDeleteClick = {
                            viewModel.deleteAgent(agent.id)
                        }
                    )
                }
            }
        }
    }

    // Modal Dialog 1: Add New Agent (Name, Role, Purpose)
    if (showAddAgentDialog) {
        CreateAgentDialog(
            onDismiss = { showAddAgentDialog = false },
            onConfirm = { name, role, purpose ->
                viewModel.addAgent(
                    name = name,
                    type = "Custom",
                    role = role,
                    permissions = "Standard",
                    traits = purpose,
                    systemPrompt = purpose
                )
                showAddAgentDialog = false
            }
        )
    }

    // Modal Dialog 2: Assign Task to Agent
    selectedAgentForNewTask?.let { targetAgent ->
        AlertDialog(
            onDismissRequest = {
                selectedAgentForNewTask = null
                newTaskDescription = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getAgentIcon(targetAgent.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assign Task to ${targetAgent.name}")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter task description or objective for ${targetAgent.name} (${targetAgent.role}):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = newTaskDescription,
                        onValueChange = { newTaskDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("assign_task_input"),
                        placeholder = { Text("e.g. Audit calendar permissions, Schedule rest break...") },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskDescription.isNotBlank()) {
                            viewModel.createMission("Task for ${targetAgent.name}: $newTaskDescription")
                            selectedAgentForNewTask = null
                            newTaskDescription = ""
                        }
                    },
                    enabled = newTaskDescription.isNotBlank(),
                    modifier = Modifier.testTag("confirm_assign_task_btn")
                ) {
                    Text("Assign Task")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedAgentForNewTask = null
                    newTaskDescription = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Dialog 3: Bottom Sheet for Agent Details
    selectedAgentForDetails?.let { agent ->
        val currentTask = agentTaskMap[agent]
        val visualStatus = getAgentVisualStatus(agent, currentTask)
        ModalBottomSheet(
            onDismissRequest = { selectedAgentForDetails = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAgentIcon(agent.type),
                            contentDescription = agent.name,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = agent.role,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    VisualStatusBadge(status = visualStatus, agentId = agent.id)
                }

                HorizontalDivider()

                Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                if (agent.traits.isNotBlank()) {
                    Text(
                        text = "Purpose & Behavior: ${agent.traits}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (agent.systemPrompt.isNotBlank() && agent.systemPrompt != agent.traits) {
                    Text(
                        text = "System Instruction: ${agent.systemPrompt}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Autonomy: ${agent.autonomyLevel}") }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Permissions: ${agent.permissions}") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddAgentDialog) {
        MultiStepCreateAgentDialog(
            viewModel = viewModel,
            onDismiss = { showAddAgentDialog = false }
        )
    }
}

@Composable
fun CreateAgentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, role: String, purpose: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Create New Agent", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Input the agent's name, role, and purpose to add them to your active colony:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Agent Name") },
                    placeholder = { Text("e.g. Data Sentinel") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_agent_name")
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role") },
                    placeholder = { Text("e.g. Privacy & Security Audit") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_agent_role")
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose / Core Responsibilities") },
                    placeholder = { Text("e.g. Continuously monitors app permissions, checks for unauthorized data access, and logs compliance audits.") },
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_agent_purpose")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && role.isNotBlank()) {
                        onConfirm(name.trim(), role.trim(), purpose.trim())
                    }
                },
                enabled = name.isNotBlank() && role.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_agent_btn")
            ) {
                Text("Add Agent")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_create_agent_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StatusDot(
    status: AgentVisualStatus,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp
) {
    val dotColor = when (status) {
        AgentVisualStatus.IDLE -> Color(0xFF2E7D32)   // Vibrant Green
        AgentVisualStatus.BUSY -> Color(0xFFEF6C00)   // Amber / Orange
        AgentVisualStatus.OFFLINE -> Color(0xFFD32F2F) // Red / Gray
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(dotColor)
    )
}

@Composable
fun VisualStatusBadge(
    status: AgentVisualStatus,
    onClick: (() -> Unit)? = null,
    agentId: Int
) {
    val (bgColor, fgColor, textLabel) = when (status) {
        AgentVisualStatus.IDLE -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF1B5E20),
            "Idle"
        )
        AgentVisualStatus.BUSY -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            "Busy"
        )
        AgentVisualStatus.OFFLINE -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFB71C1C),
            "Offline"
        )
    }

    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("agent_status_badge_$agentId")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusDot(status = status, size = 8.dp)
            Text(
                text = textLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fgColor
            )
        }
    }
}

@Composable
fun ActiveAgentsSummaryCard(
    totalAgents: Int,
    idleCount: Int,
    busyCount: Int,
    offlineCount: Int,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_agents_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Colony Status Overview",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (idleCount + busyCount > 0) {
                        OutlinedButton(
                            onClick = onPauseAll,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("pause_all_btn")
                        ) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause Active", fontSize = 12.sp)
                        }
                    } else if (offlineCount > 0) {
                        Button(
                            onClick = onResumeAll,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("resume_all_btn")
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume All", fontSize = 12.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItemWithDot(
                    label = "Total",
                    value = "$totalAgents",
                    status = null
                )
                MetricItemWithDot(
                    label = "Idle",
                    value = "$idleCount",
                    status = AgentVisualStatus.IDLE
                )
                MetricItemWithDot(
                    label = "Busy",
                    value = "$busyCount",
                    status = AgentVisualStatus.BUSY
                )
                MetricItemWithDot(
                    label = "Offline",
                    value = "$offlineCount",
                    status = AgentVisualStatus.OFFLINE
                )
            }
        }
    }
}

@Composable
fun MetricItemWithDot(label: String, value: String, status: AgentVisualStatus?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (status != null) {
                StatusDot(status = status, size = 8.dp)
            }
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = when (status) {
                    AgentVisualStatus.IDLE -> Color(0xFF1B5E20)
                    AgentVisualStatus.BUSY -> Color(0xFFE65100)
                    AgentVisualStatus.OFFLINE -> Color(0xFFB71C1C)
                    null -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveAgentCard(
    modifier: Modifier = Modifier,
    agent: Agent,
    currentTask: SubTask?,
    missionTitle: String?,
    onToggleStatus: () -> Unit,
    onCompleteTask: (Int) -> Unit,
    onAssignTaskClick: () -> Unit,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val visualStatus = getAgentVisualStatus(agent, currentTask)

    val cardBorderColor = when (visualStatus) {
        AgentVisualStatus.IDLE -> Color(0xFF2E7D32).copy(alpha = 0.4f)
        AgentVisualStatus.BUSY -> Color(0xFFEF6C00).copy(alpha = 0.6f)
        AgentVisualStatus.OFFLINE -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (visualStatus == AgentVisualStatus.BUSY) 1.5.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("active_agent_card_${agent.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (visualStatus != AgentVisualStatus.OFFLINE) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Agent Avatar with Status Dot + Name/Role + Visual Status Badge & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar Box with Status Dot Overlay
                    Box(modifier = Modifier.size(48.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    when (visualStatus) {
                                        AgentVisualStatus.IDLE -> MaterialTheme.colorScheme.primaryContainer
                                        AgentVisualStatus.BUSY -> MaterialTheme.colorScheme.tertiaryContainer
                                        AgentVisualStatus.OFFLINE -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getAgentIcon(agent.type),
                                contentDescription = agent.name,
                                tint = when (visualStatus) {
                                    AgentVisualStatus.IDLE -> MaterialTheme.colorScheme.onPrimaryContainer
                                    AgentVisualStatus.BUSY -> MaterialTheme.colorScheme.onTertiaryContainer
                                    AgentVisualStatus.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Colored Status Dot indicator anchored at bottom end of avatar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(2.dp)
                        ) {
                            StatusDot(status = visualStatus, size = 10.dp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = agent.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = agent.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Visual Status Badge with Dot + Label + Delete Button
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    VisualStatusBadge(
                        status = visualStatus,
                        onClick = onToggleStatus,
                        agentId = agent.id
                    )
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp).testTag("delete_agent_${agent.id}")) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Agent",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Current Task Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = if (currentTask != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CURRENT TASK",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentTask != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = currentTask.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (currentTask != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTask.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (!missionTitle.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Mission: $missionTitle",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { onCompleteTask(currentTask.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("complete_task_btn_${currentTask.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Complete Task",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (visualStatus == AgentVisualStatus.IDLE) "Idle • Ready for task assignment" else "Agent is currently offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (visualStatus == AgentVisualStatus.IDLE) {
                                TextButton(
                                    onClick = onAssignTaskClick,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("assign_task_btn_${agent.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Assign", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyAgentsCard(
    filter: String,
    searchQuery: String,
    onClearFilter: () -> Unit,
    onAddAgentClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("empty_agents_card"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = if (searchQuery.isNotBlank()) "No agents match '$searchQuery'"
                else "No $filter agents found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Try clearing your filters or create a new agent persona to expand your colony.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onClearFilter,
                    modifier = Modifier.testTag("clear_filter_btn")
                ) {
                    Text("Show All")
                }

                Button(
                    onClick = onAddAgentClick,
                    modifier = Modifier.testTag("empty_add_agent_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Agent")
                }
            }
        }
    }
}

fun getAgentIcon(type: String): ImageVector {
    return when (type.lowercase()) {
        "health", "wellness" -> Icons.Default.HealthAndSafety
        "work", "productivity" -> Icons.Default.Work
        "rest", "sleep" -> Icons.Default.Schedule
        "study", "education" -> Icons.Default.School
        "finance", "budget" -> Icons.Default.MonetizationOn
        "privacy", "security" -> Icons.Default.Security
        "governance" -> Icons.Default.Lock
        "creative", "design" -> Icons.Default.AutoAwesome
        else -> Icons.Default.Psychology
    }
}
