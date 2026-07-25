package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.unit.dp
import com.example.data.Agent
import com.example.data.AgentPreferences
import com.example.data.SubTask
import com.example.viewmodel.BaseAgentViewModel
import com.example.viewmodel.ColonyViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaColonyScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val agents by viewModel.agentsState.collectAsState()
    val agentPreferences by viewModel.agentPreferencesState.collectAsState()
    val subTasks by viewModel.subTasks.collectAsState()
    val completedSubTasks by viewModel.completedSubTasks.collectAsState()

    var showAddAgentDialog by remember { mutableStateOf(false) }
    var preferencesExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Persona Agent Colony",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${agents.size} Active Personas • Room DB & DataStore",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("persona_colony_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddAgentDialog = true },
                        modifier = Modifier.testTag("add_persona_agent_btn")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Persona Agent",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddAgentDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Persona") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("fab_add_persona")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Section 1: DataStore Preferences & Operational Thresholds
            item {
                DataStorePreferencesCard(
                    preferences = agentPreferences,
                    expanded = preferencesExpanded,
                    onToggleExpand = { preferencesExpanded = !preferencesExpanded },
                    onThresholdChange = { viewModel.updateGlobalAutonomyThreshold(it) },
                    onMaxTasksChange = { viewModel.updateMaxActiveTasksPerPersona(it) },
                    onBackgroundToggle = { viewModel.updateAllowBackgroundExecution(it) },
                    onDataAccessToggle = { viewModel.updateAllowDataAccess(it) },
                    onNotificationsToggle = { viewModel.updateNotificationsEnabled(it) },
                    onManualOverrideToggle = { viewModel.updateStrictManualOverride(it) }
                )
            }

            // Section 2: Vico Chart - Aggregate Activity per Persona
            item {
                PersonaActivityVicoChartCard(
                    agents = agents,
                    completedSubTasks = completedSubTasks,
                    allSubTasks = subTasks
                )
            }

            // Section 3: Interactive Persona Agent Cards Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Colony Personas (${agents.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${agents.count { it.status == "Active" }} Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Section 4: Interactive Persona Cards List
            if (agents.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No Persona Agents Registered",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Tap the + button to add a new specialized persona agent.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(agents, key = { it.id }) { agent ->
                    PersonaAgentCard(
                        agent = agent,
                        agentTasks = subTasks.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) },
                        onCycleStatus = { viewModel.cycleAgentStatus(agent.id) },
                        onSetStatus = { status -> viewModel.setAgentStatus(agent.id, status) },
                        onUpdateAutonomy = { level, perms -> viewModel.updateAgentAutonomy(agent.id, level, perms) },
                        onDeleteAgent = { viewModel.deleteAgent(agent.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Extra FAB spacing
            }
        }
    }

    if (showAddAgentDialog) {
        AddPersonaAgentDialog(
            onDismiss = { showAddAgentDialog = false },
            onConfirm = { newAgent ->
                viewModel.registerAgent(newAgent)
                showAddAgentDialog = false
            }
        )
    }
}

@Composable
fun DataStorePreferencesCard(
    preferences: AgentPreferences,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onThresholdChange: (String) -> Unit,
    onMaxTasksChange: (Int) -> Unit,
    onBackgroundToggle: (Boolean) -> Unit,
    onDataAccessToggle: (Boolean) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onManualOverrideToggle: (Boolean) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("datastore_preferences_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Operational Preferences (DataStore)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Global Autonomy: ${preferences.globalAutonomyThreshold} • Max Tasks: ${preferences.maxActiveTasksPerPersona}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Preferences"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Threshold Selector
                    Text(
                        "Global Autonomy Threshold",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    val thresholds = listOf("Strict Manual", "Needs Confirmation", "Semi-Autonomous", "Full Autonomy")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(thresholds) { threshold ->
                            FilterChip(
                                selected = preferences.globalAutonomyThreshold == threshold,
                                onClick = { onThresholdChange(threshold) },
                                label = { Text(threshold, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (preferences.globalAutonomyThreshold == threshold) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.testTag("chip_threshold_$threshold")
                            )
                        }
                    }

                    // Max Tasks Stepper/Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Max Active Tasks Per Persona: ${preferences.maxActiveTasksPerPersona}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedIconButton(
                                onClick = { if (preferences.maxActiveTasksPerPersona > 1) onMaxTasksChange(preferences.maxActiveTasksPerPersona - 1) },
                                enabled = preferences.maxActiveTasksPerPersona > 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedIconButton(
                                onClick = { if (preferences.maxActiveTasksPerPersona < 15) onMaxTasksChange(preferences.maxActiveTasksPerPersona + 1) },
                                enabled = preferences.maxActiveTasksPerPersona < 15,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // DataStore Toggle Switches
                    PreferenceSwitchRow(
                        title = "Allow Background Processing",
                        subtitle = "Enable agent background task cycles",
                        checked = preferences.allowBackgroundExecution,
                        onCheckedChange = onBackgroundToggle,
                        tag = "switch_background_processing"
                    )

                    PreferenceSwitchRow(
                        title = "Allow Data Access",
                        subtitle = "Allow personas to read colony ledger & memory",
                        checked = preferences.allowDataAccess,
                        onCheckedChange = onDataAccessToggle,
                        tag = "switch_data_access"
                    )

                    PreferenceSwitchRow(
                        title = "Agent Notifications",
                        subtitle = "Alert on critical agent decisions and milestone updates",
                        checked = preferences.notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        tag = "switch_notifications"
                    )

                    PreferenceSwitchRow(
                        title = "Strict Manual Override",
                        subtitle = "Force all persona actions to require manual approval",
                        checked = preferences.strictManualOverride,
                        onCheckedChange = onManualOverrideToggle,
                        tag = "switch_manual_override"
                    )
                }
            }
        }
    }
}

@Composable
fun PreferenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
fun PersonaActivityVicoChartCard(
    agents: List<Agent>,
    completedSubTasks: List<SubTask>,
    allSubTasks: List<SubTask>
) {
    var chartType by remember { mutableStateOf("Column") }

    // Aggregate completed tasks per persona agent
    val personaCounts = remember(agents, completedSubTasks, allSubTasks) {
        val counts = FloatArray(agents.size.coerceAtLeast(1)) { 0f }
        agents.forEachIndexed { index, agent ->
            val count = completedSubTasks.count { it.assignedAgent.equals(agent.name, ignoreCase = true) }
            counts[index] = count.toFloat()
        }
        // If no tasks exist yet, provide a baseline distribution for visualization
        if (completedSubTasks.isEmpty() && agents.isNotEmpty()) {
            agents.forEachIndexed { index, _ ->
                counts[index] = ((index + 1) * 2f)
            }
        }
        counts
    }

    val chartModel = remember(personaCounts) {
        entryModelOf(*personaCounts.toTypedArray())
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vico_persona_chart_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Persona Aggregate Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Vico Chart • Completed Tasks by Persona Agent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = chartType == "Column",
                        onClick = { chartType = "Column" },
                        label = { Text("Bars", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chart_type_bars")
                    )
                    FilterChip(
                        selected = chartType == "Line",
                        onClick = { chartType = "Line" },
                        label = { Text("Line", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chart_type_line")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (chartType == "Column") {
                    Chart(
                        chart = columnChart(),
                        model = chartModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Chart(
                        chart = lineChart(),
                        model = chartModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // X-Axis persona names legend
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(agents) { agent ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        val taskCount = completedSubTasks.count { it.assignedAgent.equals(agent.name, ignoreCase = true) }
                        Text(
                            text = "${agent.name}: $taskCount tasks",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonaAgentCard(
    agent: Agent,
    agentTasks: List<SubTask>,
    onCycleStatus: () -> Unit,
    onSetStatus: (String) -> Unit,
    onUpdateAutonomy: (String, String) -> Unit,
    onDeleteAgent: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (agent.status) {
        "Active" -> Color(0xFF2E7D32)
        "Paused" -> Color(0xFFED6C02)
        "Syncing" -> Color(0xFF0288D1)
        "Halted" -> Color(0xDCD32F2F)
        else -> MaterialTheme.colorScheme.secondary
    }

    val iconVector = getAgentIcon(agent.type, agent.iconName)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("persona_card_${agent.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon, Name, Role, Type, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = agent.name,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = agent.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = agent.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = agent.role,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interactive Status Pill Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clickable { onCycleStatus() }
                        .testTag("btn_cycle_status_${agent.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = agent.status,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Activity & Task Metrics
            val completedCount = agentTasks.count { it.status.equals("Completed", ignoreCase = true) }
            val pendingCount = agentTasks.size - completedCount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Autonomy: ${agent.autonomyLevel}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Tasks: $completedCount completed / $pendingCount pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (agentTasks.isNotEmpty()) {
                val latestTask = agentTasks.first()
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Recent Task: ${latestTask.description} (${latestTask.status})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Status Quick Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = { onSetStatus("Active") },
                        label = { Text("Activate") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (agent.status == "Active") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("chip_activate_${agent.id}")
                    )

                    AssistChip(
                        onClick = { onSetStatus("Paused") },
                        label = { Text("Pause") },
                        leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (agent.status == "Paused") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("chip_pause_${agent.id}")
                    )
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.testTag("btn_expand_persona_${agent.id}")
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Configuration"
                    )
                }
            }

            // Expandable Accordion for Configuration & System Prompt
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "Persona Configuration",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (agent.traits.isNotEmpty()) {
                        Text(
                            text = "Traits: ${agent.traits}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (agent.systemPrompt.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "System Prompt Instructions:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = agent.systemPrompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Autonomy Level Selector
                    Text("Change Autonomy Threshold:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Needs Confirmation", "Semi-Autonomous", "Full Autonomy").forEach { level ->
                            FilterChip(
                                selected = agent.autonomyLevel == level,
                                onClick = { onUpdateAutonomy(level, agent.permissions) },
                                label = { Text(level, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("chip_autonomy_${agent.id}_$level")
                            )
                        }
                    }

                    // Delete Persona Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDeleteAgent,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("btn_delete_agent_${agent.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove Persona")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPersonaAgentDialog(
    onDismiss: () -> Unit,
    onConfirm: (Agent) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Executive") }
    var autonomyLevel by remember { mutableStateOf("Semi-Autonomous") }
    var traits by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }

    val types = listOf("Executive", "Health", "Finance", "Security", "Code", "Study", "Creative")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Persona Agent")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Persona Name (e.g., Dr. Sentinel)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_persona_name")
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Specialty (e.g., Security Overseer)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_persona_role")
                )

                Text("Persona Domain:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = traits,
                    onValueChange = { traits = it },
                    label = { Text("Traits (e.g., Analytical, Cautious)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_persona_traits")
                )

                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("System Prompt Instructions") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_persona_prompt")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && role.isNotBlank()) {
                        onConfirm(
                            Agent(
                                name = name.trim(),
                                type = type,
                                role = role.trim(),
                                status = "Active",
                                permissions = "Standard",
                                autonomyLevel = autonomyLevel,
                                iconName = type.lowercase(),
                                traits = traits.trim(),
                                systemPrompt = systemPrompt.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && role.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_persona_btn")
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getAgentIcon(type: String, iconName: String): ImageVector {
    return when (type.lowercase()) {
        "health", "fitness" -> Icons.Default.HealthAndSafety
        "finance", "budget" -> Icons.Default.MonetizationOn
        "security", "privacy" -> Icons.Default.Lock
        "code", "engineering" -> Icons.Default.Code
        "study", "academic" -> Icons.Default.School
        "governance", "legal" -> Icons.Default.Gavel
        "executive", "leadership" -> Icons.Default.Psychology
        else -> Icons.Default.SmartToy
    }
}
