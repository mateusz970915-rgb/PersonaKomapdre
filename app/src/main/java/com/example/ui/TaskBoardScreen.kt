package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Agent
import com.example.data.SubTask
import com.example.data.calculateAgentMood
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskBoardScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val onExecute: (com.example.data.SubTask) -> Unit = { task -> viewModel.executeSubTaskReal(task) }
    val subTasks by viewModel.subTasks.collectAsState()
    val agents by viewModel.agents.collectAsState()

    var viewMode by remember { mutableStateOf("Status") } // "Status" or "Agent"
    var priorityFilter by remember { mutableStateOf("All") } // "All", "High", "Medium", "Low"
    var showAddTaskDialog by remember { mutableStateOf(false) }

    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskAgent by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("Medium") }
    
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedTasks = remember { mutableStateListOf<SubTask>() }
    var showBulkDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showBulkStatusDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val activeAgents = remember(agents) {
        agents.ifEmpty {
            listOf(
                Agent(id = 1, name = "Work Agent", type = "Work", role = "Task Organizer"),
                Agent(id = 2, name = "Health Agent", type = "Health", role = "Wellness Guard")
            )
        }
    }

    val filteredSubTasks = remember(subTasks, priorityFilter) {
        if (priorityFilter == "All") subTasks else subTasks.filter { it.priority.equals(priorityFilter, ignoreCase = true) }
    }

    LaunchedEffect(activeAgents) {
        if (newTaskAgent.isBlank() && activeAgents.isNotEmpty()) {
            newTaskAgent = activeAgents.first().name
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Visual Task Board", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Agent digital task assignment & progress tracking", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("task_board_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isMultiSelectMode) {
                            Text("${selectedTasks.size} selected", style = MaterialTheme.typography.labelMedium)
                            IconButton(onClick = { showBulkStatusDialog = true }, enabled = selectedTasks.isNotEmpty()) {
                                Icon(Icons.Default.Edit, contentDescription = "Change Status")
                            }
                            IconButton(onClick = { showBulkDeleteConfirmDialog = true }, enabled = selectedTasks.isNotEmpty()) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                            }
                            IconButton(onClick = {
                                isMultiSelectMode = false
                                selectedTasks.clear()
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Done")
                            }
                        } else {
                            IconButton(onClick = { isMultiSelectMode = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Select Tasks")
                            }
                            FilterChip(
                            selected = viewMode == "Status",
                            onClick = { viewMode = "Status" },
                            label = { Text("Status", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("view_mode_status")
                        )
                        FilterChip(
                            selected = viewMode == "Agent",
                            onClick = { viewMode = "Agent" },
                            label = { Text("By Agent", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("view_mode_agent")
                        )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_digital_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Priority:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp, end = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val priorities = listOf("All", "High", "Medium", "Low")
                items(priorities) { priority ->
                    FilterChip(
                        selected = priorityFilter == priority,
                        onClick = { priorityFilter = priority },
                        label = { Text(priority, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (viewMode == "Status") {
                // Horizontal Column Board: Pending | In Progress | Completed
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val statusList = listOf("Pending", "In Progress", "EXECUTED", "BLOCKED", "Failed")
                    items(statusList) { status ->
                        StatusColumnCard(
                            status = status,
                            subTasks = filteredSubTasks.filter {
                                if (status == "Pending") it.status == "Pending" || it.status.isBlank()
                                else it.status.equals(status, ignoreCase = true)
                            },
                            agentList = activeAgents,
                            allSubTasks = filteredSubTasks,
                            onReassign = { taskId, newAgent -> viewModel.reassignSubTask(taskId, newAgent) },
                            onStatusChange = { taskId, newStatus -> viewModel.updateSubTaskStatus(taskId, newStatus) },
                            onPriorityChange = { taskId, priority -> viewModel.updateSubTaskPriority(taskId, priority) },
                            isMultiSelectMode = isMultiSelectMode,
                            selectedTasks = selectedTasks,
                            onSelectToggle = { task -> 
                                if (selectedTasks.contains(task)) selectedTasks.remove(task) else selectedTasks.add(task)
                            },
                            onDelete = { taskId ->
                                val taskToDelete = subTasks.find { it.id == taskId }
                                viewModel.deleteSubTask(taskId)
                                if (taskToDelete != null) {
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Task deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreSubTasks(listOf(taskToDelete))
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.width(310.dp)
                        )
                    }
                }
            } else {
                // Grouped by Agent Swimlanes
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(activeAgents, key = { it.id }) { agent ->
                        AgentTaskSwimlaneCard(
                            agent = agent,
                            subTasks = filteredSubTasks.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) },
                            agentList = activeAgents,
                            allSubTasks = filteredSubTasks,
                            onReassign = { taskId, newAgent -> viewModel.reassignSubTask(taskId, newAgent) },
                            onStatusChange = { taskId, newStatus -> viewModel.updateSubTaskStatus(taskId, newStatus) },
                            onPriorityChange = { taskId, priority -> viewModel.updateSubTaskPriority(taskId, priority) },
                            onExecute = onExecute,
                            isMultiSelectMode = isMultiSelectMode,
                            selectedTasks = selectedTasks,
                            onSelectToggle = { task -> 
                                if (selectedTasks.contains(task)) selectedTasks.remove(task) else selectedTasks.add(task)
                            },
                            onDelete = { taskId ->
                                val taskToDelete = subTasks.find { it.id == taskId }
                                viewModel.deleteSubTask(taskId)
                                if (taskToDelete != null) {
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Task deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreSubTasks(listOf(taskToDelete))
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Assign Digital Task", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter task description and select target agent:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var isAutoTagging by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text("Task Description") },
                        placeholder = { Text("e.g. Analyze server logs and optimize query budget") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_task_desc_input"),
                        trailingIcon = {
                            if (isAutoTagging) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                IconButton(onClick = {
                                    if (newTaskDesc.isNotBlank()) {
                                        isAutoTagging = true
                                        viewModel.autoTagTask(newTaskDesc) { priority, agentName ->
                                            newTaskPriority = priority
                                            if (agentName.isNotBlank() && activeAgents.any { it.name == agentName }) {
                                                newTaskAgent = agentName
                                            }
                                            isAutoTagging = false
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Auto-Tag Priority & Agent", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Assigned Agent:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            var expandedAgentMenu by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedAgentMenu,
                                onExpandedChange = { expandedAgentMenu = !expandedAgentMenu }
                            ) {
                                OutlinedTextField(
                                    value = newTaskAgent,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAgentMenu) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                        .testTag("select_assigned_agent_dropdown")
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedAgentMenu,
                                    onDismissRequest = { expandedAgentMenu = false }
                                ) {
                                    activeAgents.forEach { ag ->
                                        val displayText = if (ag.status == "Resting") "${ag.name} (Resting 😴)" else ag.name
                                        DropdownMenuItem(
                                            text = { Text(displayText) },
                                            onClick = {
                                                newTaskAgent = ag.name
                                                expandedAgentMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Priority:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            var expandedPriorityMenu by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedPriorityMenu,
                                onExpandedChange = { expandedPriorityMenu = !expandedPriorityMenu }
                            ) {
                                OutlinedTextField(
                                    value = newTaskPriority,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriorityMenu) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedPriorityMenu,
                                    onDismissRequest = { expandedPriorityMenu = false }
                                ) {
                                    listOf("High", "Medium", "Low").forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p) },
                                            onClick = {
                                                newTaskPriority = p
                                                expandedPriorityMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    val selectedAgentObj = activeAgents.find { it.name == newTaskAgent }
                    if (selectedAgentObj?.status == "Resting") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Warning: ${newTaskAgent} is currently resting! Assigning tasks may interrupt recovery.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskDesc.isNotBlank() && newTaskAgent.isNotBlank()) {
                            viewModel.createStandaloneSubTask(
                                description = newTaskDesc.trim(),
                                assignedAgent = newTaskAgent,
                                priority = newTaskPriority
                            )
                            newTaskDesc = ""
                            showAddTaskDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_new_task_btn")
                ) {
                    Text("Assign Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBulkDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirmDialog = false },
            title = { Text("Delete ${selectedTasks.size} Tasks") },
            text = { Text("Are you sure you want to delete the selected tasks?") },
            confirmButton = {
                Button(
                    onClick = {
                        val deletedTasks = selectedTasks.toList()
                        viewModel.deleteSubTasksByIds(deletedTasks.map { it.id })
                        selectedTasks.clear()
                        isMultiSelectMode = false
                        showBulkDeleteConfirmDialog = false
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${deletedTasks.size} tasks deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreSubTasks(deletedTasks)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBulkStatusDialog) {
        var bulkStatus by remember { mutableStateOf("Pending") }
        AlertDialog(
            onDismissRequest = { showBulkStatusDialog = false },
            title = { Text("Change Status for ${selectedTasks.size} Tasks") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statuses = listOf("Pending", "In Progress", "EXECUTED", "BLOCKED", "Failed")
                    statuses.forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { bulkStatus = status }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = bulkStatus == status,
                                onClick = { bulkStatus = status }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(status)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedTasks.forEach { task ->
                        viewModel.updateSubTaskStatus(task.id, bulkStatus)
                    }
                    selectedTasks.clear()
                    isMultiSelectMode = false
                    showBulkStatusDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusColumnCard(
    status: String,
    subTasks: List<SubTask>,
    agentList: List<Agent>,
    allSubTasks: List<SubTask>,
    onReassign: (Int, String) -> Unit,
    onStatusChange: (Int, String) -> Unit,
    onPriorityChange: (Int, String) -> Unit = { _, _ -> },
    isMultiSelectMode: Boolean = false,
    selectedTasks: List<SubTask> = emptyList(),
    onSelectToggle: (SubTask) -> Unit = {},
    onDelete: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val columnColor = when (status) {
        "In Progress" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        "Completed" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = columnColor),
        modifier = modifier.fillMaxHeight()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "${subTasks.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (subTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks in $status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(subTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            agentList = agentList,
                            allSubTasks = allSubTasks,
                            onReassign = onReassign,
                            onStatusChange = onStatusChange,
                            onPriorityChange = onPriorityChange,
                            isMultiSelectMode = isMultiSelectMode,
                            isSelected = selectedTasks.contains(task),
                            onSelectToggle = { onSelectToggle(task) },
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgentTaskSwimlaneCard(
    agent: Agent,
    subTasks: List<SubTask>,
    agentList: List<Agent>,
    allSubTasks: List<SubTask>,
    onReassign: (Int, String) -> Unit,
    onStatusChange: (Int, String) -> Unit,
    onPriorityChange: (Int, String) -> Unit = { _, _ -> },
    onExecute: (com.example.data.SubTask) -> Unit = {},
    isMultiSelectMode: Boolean = false,
    selectedTasks: List<SubTask> = emptyList(),
    onSelectToggle: (SubTask) -> Unit = {},
    onDelete: (Int) -> Unit = {}
) {
    val moodInfo = remember(agent, allSubTasks) { calculateAgentMood(agent, allSubTasks) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_swimlane_${agent.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = moodInfo.emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${agent.role} • ${moodInfo.moodTitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${subTasks.size} Tasks",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (subTasks.isEmpty()) {
                Text(
                    text = "No active digital tasks assigned.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    subTasks.forEach { task ->
                        TaskCardItem(
                            task = task,
                            agentList = agentList,
                            allSubTasks = allSubTasks,
                            onReassign = onReassign,
                            onStatusChange = onStatusChange,
                            onPriorityChange = onPriorityChange,
                            isMultiSelectMode = isMultiSelectMode,
                            isSelected = selectedTasks.contains(task),
                            onSelectToggle = { onSelectToggle(task) },
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCardItem(
    task: SubTask,
    agentList: List<Agent>,
    allSubTasks: List<SubTask>,
    onReassign: (Int, String) -> Unit,
    onStatusChange: (Int, String) -> Unit,
    onExecute: (com.example.data.SubTask) -> Unit = {},
    onPriorityChange: (Int, String) -> Unit = { _, _ -> },
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
    onDelete: (Int) -> Unit = {}
) {
    val assignedAgentObj = remember(task.assignedAgent, agentList) {
        agentList.find { it.name.equals(task.assignedAgent, ignoreCase = true) }
    }
    val agentMood = remember(assignedAgentObj, allSubTasks) {
        if (assignedAgentObj != null) calculateAgentMood(assignedAgentObj, allSubTasks) else null
    }
    
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    if (isMultiSelectMode) onSelectToggle() 
                },
                onLongClick = { onSelectToggle() }
            )
            .testTag("task_card_${task.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (isMultiSelectMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        modifier = Modifier.padding(end = 8.dp).size(24.dp)
                    )
                }
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("task_menu_btn_${task.id}")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reassign Agent") },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                // Pick next available agent
                                val otherAgent = agentList.find { !it.name.equals(task.assignedAgent, ignoreCase = true) }
                                if (otherAgent != null) {
                                    onReassign(task.id, otherAgent.name)
                                }
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Set Priority: High") },
                            leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onPriorityChange(task.id, "High")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Set Priority: Medium") },
                            leadingIcon = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onPriorityChange(task.id, "Medium")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Set Priority: Low") },
                            leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onPriorityChange(task.id, "Low")
                            }
                        )

                        if (task.status != "In Progress") {
                            DropdownMenuItem(
                                text = { Text("Set In Progress") },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onStatusChange(task.id, "In Progress")
                                }
                            )
                        }
                        
                        // Added Real Execution Integration
                        if (task.status == "Pending" || task.status == "In Progress") {
                            DropdownMenuItem(
                                text = { Text("Execute via Agent (LLM)") },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onExecute(task)
                                }
                            )
                        }
                        
                        if (task.status != "Completed") {
                            DropdownMenuItem(
                                text = { Text("Mark Completed") },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onStatusChange(task.id, "Completed")
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (task.status == "In Progress") {
                LinearProgressIndicator(
                    progress = { task.progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = agentMood?.emoji ?: "🤖",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.assignedAgent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (task.priority) {
                            "High" -> MaterialTheme.colorScheme.errorContainer
                            "Medium" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (task.priority) {
                                "High" -> MaterialTheme.colorScheme.onErrorContainer
                                "Medium" -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (task.status.uppercase()) {
                        "EXECUTED", "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
                        "IN PROGRESS" -> MaterialTheme.colorScheme.secondaryContainer
                        "BLOCKED", "FAILED" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = task.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (task.status.uppercase()) {
                            "EXECUTED", "COMPLETED" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "IN PROGRESS" -> MaterialTheme.colorScheme.onSecondaryContainer
                            "BLOCKED", "FAILED" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
