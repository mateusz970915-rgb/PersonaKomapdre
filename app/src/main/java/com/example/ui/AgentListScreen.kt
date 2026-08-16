package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Agent
import com.example.viewmodel.AgentViewModel
import kotlinx.coroutines.launch

private fun parseAccentColor(json: String): Color? {
    return try {
        if (json.contains("accentColor")) {
            val match = Regex("\"accentColor\"\\s*:\\s*\"([^\"]+)\"").find(json)
            val hex = match?.groupValues?.get(1)
            if (hex != null) {
                Color(android.graphics.Color.parseColor(hex))
            } else null
        } else null
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentListScreen(
    modifier: Modifier = Modifier,
    viewModel: AgentViewModel = viewModel()
) {
    val agents by viewModel.agentsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAgentForColor by remember { mutableStateOf<Agent?>(null) }
    var selectedAgentForDetail by remember { mutableStateOf<Agent?>(null) }
    var selectedAgentIds by remember { mutableStateOf(setOf<Int>()) }
    val isMultiSelectMode = selectedAgentIds.isNotEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val filteredAgents = remember(agents, searchQuery) {
        if (searchQuery.isBlank()) {
            agents
        } else {
            agents.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isMultiSelectMode) {
                TopAppBar(
                    title = { Text("${selectedAgentIds.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedAgentIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            selectedAgentIds.forEach { id ->
                                agents.find { it.id == id }?.let { viewModel.deleteAgent(it) }
                            }
                            selectedAgentIds = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                        }
                        var expandedBatchStatus by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expandedBatchStatus = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Set Status")
                            }
                            DropdownMenu(
                                expanded = expandedBatchStatus,
                                onDismissRequest = { expandedBatchStatus = false }
                            ) {
                                listOf("Online", "Offline", "Busy").forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            selectedAgentIds.forEach { id ->
                                                agents.find { it.id == id }?.let { viewModel.updateAgentStatus(it, status) }
                                            }
                                            selectedAgentIds = emptySet()
                                            expandedBatchStatus = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("AI Agent Registry") },
                    actions = {
                        IconButton(
                            onClick = { viewModel.refreshAgentStatuses() },
                            modifier = Modifier.testTag("manual_refresh_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Statuses")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_agent_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Agent")
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshAgentStatuses() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search agents by name or role...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("agent_search_bar")
                )

                if (agents.isEmpty()) {
                    EmptyAgentListState(onAddClick = { showAddDialog = true })
                } else if (filteredAgents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No agents found matching \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { searchQuery = "" }) {
                                Text("Clear Search")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("agent_lazy_column")
                    ) {
                        items(
                            items = filteredAgents,
                            key = { it.id }
                        ) { agent ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteAgent(agent)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        else -> Color.Transparent
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, shape = RoundedCornerShape(12.dp))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                AgentItemCard(
                                    agent = agent,
                                    isSelected = selectedAgentIds.contains(agent.id),
                                    isMultiSelectMode = isMultiSelectMode,
                                    onSelect = { selectedAgentForDetail = agent },
                                    onLongSelect = {
                                        selectedAgentIds = if (selectedAgentIds.contains(agent.id)) {
                                            selectedAgentIds - agent.id
                                        } else {
                                            selectedAgentIds + agent.id
                                        }
                                    },
                                    onDelete = { viewModel.deleteAgent(agent) },
                                    onChangeColor = { selectedAgentForColor = agent },
                                    onInvoke = {
                                        viewModel.invokeAgentFunction(agent) { message ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    },
                                    onToggleStatus = { isActive ->
                                        viewModel.updateAgentStatus(agent, if (isActive) "Online" else "Offline")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAgentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, role, category, status, prompt ->
                viewModel.addAgent(
                    name = name,
                    role = role,
                    category = category,
                    status = status,
                    systemPrompt = prompt
                )
                showAddDialog = false
            }
        )
    }

    selectedAgentForColor?.let { agent ->
        AgentColorPickerDialog(
            agent = agent,
            onDismiss = { selectedAgentForColor = null },
            onColorSelected = { hex ->
                viewModel.updateAgentAccentColor(agent, hex)
                selectedAgentForColor = null
            }
        )
    }

    selectedAgentForDetail?.let { agent ->
        AgentDetailSheet(
            agent = agent,
            onDismiss = { selectedAgentForDetail = null }
        )
    }
}

@Composable
fun EmptyAgentListState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "No Agents Illustration",
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "No AI Agents Registered",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your agent registry is currently empty. Tap below to create your first autonomous AI agent.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("empty_state_add_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register New Agent")
                }
            }
        }
    }
}

@Composable
fun AgentStatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, label, icon) = when (status.lowercase()) {
        "online", "active" -> Triple(Color(0xFF4CAF50), "Active", Icons.Default.CheckCircle)
        "busy", "working" -> Triple(Color(0xFFFF9800), "Busy", Icons.Default.Pending)
        "idle" -> Triple(Color(0xFF9E9E9E), "Idle", Icons.Default.DoNotDisturbOn)
        else -> Triple(Color(0xFF9E9E9E), "Offline", Icons.Default.DoNotDisturbOn)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier.testTag("status_indicator_$status")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgentItemCard(
    agent: Agent,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onSelect: () -> Unit,
    onLongSelect: () -> Unit,
    onDelete: () -> Unit,
    onChangeColor: () -> Unit,
    onInvoke: () -> Unit,
    onToggleStatus: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = parseAccentColor(agent.configurationJson)
    val cardBorder = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else if (accentColor != null) {
        BorderStroke(2.dp, accentColor)
    } else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    if (isMultiSelectMode) onLongSelect() else onSelect()
                },
                onLongClick = { onLongSelect() }
            )
            .testTag("agent_card_${agent.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = (accentColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = accentColor ?: MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
                            val isRecentlyActive = agent.lastActiveTimestamp > 0 && (System.currentTimeMillis() - agent.lastActiveTimestamp) < 15 * 60 * 1000
                            val activityStatus = if (isRecentlyActive) "Active" else "Idle"
                            AgentStatusIndicator(status = activityStatus)
                            
                            val isOnline = agent.status.lowercase() == "online" || agent.status.lowercase() == "active"
                            Switch(
                                checked = isOnline,
                                onCheckedChange = { onToggleStatus(it) },
                                modifier = Modifier.scale(0.8f).testTag("status_switch_${agent.id}")
                            )
                        }
                        Text(
                            text = "${agent.category} • Role: ${agent.role} • Autonomy: ${agent.autonomyLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_agent_${agent.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Agent",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInvoke,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("invoke_agent_${agent.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Quick Invoke",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Quick Invoke", fontSize = 12.sp)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onChangeColor,
                        modifier = Modifier.testTag("color_agent_${agent.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Change Accent Color",
                            tint = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgentColorPickerDialog(
    agent: Agent,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val colorOptions = listOf(
        "#E53935" to "Red",
        "#1E88E5" to "Blue",
        "#43A047" to "Green",
        "#FB8C00" to "Orange",
        "#8E24AA" to "Purple",
        "#00ACC1" to "Cyan",
        "#FDD835" to "Yellow",
        "#3949AB" to "Indigo"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Accent Color for ${agent.name}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select a custom highlight accent color for this agent:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    colorOptions.take(4).forEach { (hex, name) ->
                        ColorOptionItem(hex = hex, name = name, onSelect = { onColorSelected(hex) })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    colorOptions.drop(4).forEach { (hex, name) ->
                        ColorOptionItem(hex = hex, name = name, onSelect = { onColorSelected(hex) })
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorOptionItem(
    hex: String,
    name: String,
    onSelect: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(hex))
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable { onSelect() }
            .testTag("color_option_$name"),
        contentAlignment = Alignment.Center
    ) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAgentDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, category: String, status: String, prompt: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("GENERAL") }
    var category by remember { mutableStateOf("Work") }
    var status by remember { mutableStateOf("Online") }
    var systemPrompt by remember { mutableStateOf("") }

    var expandedRole by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val roles = listOf("GENERAL", "SECURITY", "FINANCE", "WORK", "RESEARCH", "ANALYTICS")
    val statusOptions = listOf("Online", "Offline", "Busy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New AI Agent") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Agent Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("agent_name_input")
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Role")
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("agent_role_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        roles.forEach { roleOption ->
                            DropdownMenuItem(
                                text = { Text(roleOption) },
                                onClick = {
                                    role = roleOption
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("agent_category_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Work", "Creative", "Finance").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("category_chip_$cat")
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Status")
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("agent_status_selector")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statusOptions.forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption) },
                                onClick = {
                                    status = statusOption
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("System Prompt / Instructions") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("agent_prompt_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, role, category, status, systemPrompt)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_agent_button")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDetailSheet(
    agent: Agent,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = agent.name,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = agent.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Role: ${agent.role} • Status: ${agent.status}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            AgentActivityChart(agent = agent)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
