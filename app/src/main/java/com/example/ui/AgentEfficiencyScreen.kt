package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Agent
import com.example.data.AgentDecision
import com.example.data.Mission
import com.example.data.SubTask
import com.example.viewmodel.ColonyViewModel
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CollaborationEdge(
    val agent1Id: Int,
    val agent2Id: Int,
    val count: Int
)

data class NodePosition(
    val agent: Agent,
    val center: Offset,
    val radius: Float,
    val influenceScore: Int,
    val influencePercentage: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentEfficiencyScreen(
    viewModel: ColonyViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val agents by viewModel.agents.collectAsState()
    val subTasks by viewModel.subTasks.collectAsState()
    val decisions by viewModel.decisions.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val dataAccessRequests by viewModel.dataAccessRequests.collectAsState()
    val interAgentMessages by viewModel.interAgentMessages.collectAsState()
    val completedTasksList by viewModel.completedSubTasks.collectAsState()
    val annotations by viewModel.chartAnnotations.collectAsState()
    val selectedOverlayAgents by viewModel.selectedOverlayAgents.collectAsState()

    var selectedAgentId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Colony Analytics & Network Topology")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Interactive Agent Collaboration & Influence Network Graph
            AgentNetworkGraphCard(
                agents = agents,
                subTasks = subTasks,
                decisions = decisions,
                missions = missions,
                selectedAgentId = selectedAgentId,
                onSelectAgent = { id ->
                    selectedAgentId = if (selectedAgentId == id) null else id
                }
            )

            // Section 2: Selected Agent Influence Profile
            val selectedAgent = agents.find { it.id == selectedAgentId }
            if (selectedAgent != null) {
                SelectedAgentInfluenceCard(
                    agent = selectedAgent,
                    allAgents = agents,
                    subTasks = subTasks,
                    decisions = decisions,
                    missions = missions,
                    onClearSelection = { selectedAgentId = null }
                )
            }

            // Section 2.5: Weekly Colony Productivity (Vico Chart)
            WeeklyProductivityChart(
                completedSubTasksCount = subTasks.count { it.status.equals("Completed", ignoreCase = true) },
                decisionsCount = decisions.size,
                missionsCount = missions.count { it.status.equals("Completed", ignoreCase = true) },
                completedTasksList = completedTasksList
            )

            // Section 2.6: Multi-Agent Overlay Analytics (Features 2, 3, 4, 5)
            MultiAgentOverlayChartWidget(
                agents = agents,
                subTasks = subTasks,
                decisions = decisions,
                annotations = annotations,
                selectedOverlayAgents = selectedOverlayAgents,
                onToggleOverlayAgent = { viewModel.toggleOverlayAgent(it) },
                onAddAnnotation = { note, tag, colorHex -> viewModel.addChartAnnotation(note, tag, colorHex) },
                onDeleteAnnotation = { viewModel.deleteChartAnnotation(it) },
                onGeneratePdfReport = { bmp -> viewModel.generatePdfReport(bmp) }
            )

            // Section 2.7: Activity Heatmap (GitHub Style Contribution Grid) (Feature 5)
            ActivityHeatmapGrid(
                subTasks = subTasks,
                decisions = decisions
            )

            // Section 3: Tasks Completed per Agent (Vico Chart)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("efficiency_task_chart_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tasks Completed per Agent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val taskCounts = remember(agents, subTasks) {
                        if (agents.isEmpty()) {
                            listOf(0f, 0f, 0f, 0f, 0f)
                        } else {
                            agents.map { agent ->
                                subTasks.count {
                                    it.assignedAgent.equals(agent.name, ignoreCase = true) &&
                                            it.status.equals("Completed", ignoreCase = true)
                                }.toFloat()
                            }
                        }
                    }

                    if (taskCounts.all { it == 0f }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Brak danych z tego tygodnia, przypisz agentom zadania!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        val efficiencyModel = remember(taskCounts) { entryModelOf(*taskCounts.toTypedArray()) }
                        Chart(
                            chart = columnChart(),
                            model = efficiencyModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (agents.isNotEmpty()) agents.joinToString(", ") { it.name } else "Health, Work, Study, Finance, Rest",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Section 4: Resource Usage Over Time
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resource_usage_chart_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Colony Operations Volume Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Recorded Database Operations (Tasks, Inter-Agent Messages & Access Requests)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val usageModel = remember(dataAccessRequests, interAgentMessages, subTasks) {
                        val totalOps = (dataAccessRequests.size + interAgentMessages.size + subTasks.size).toFloat()
                        entryModelOf(
                            1f to (dataAccessRequests.size.toFloat()),
                            2f to (interAgentMessages.size.toFloat()),
                            3f to (subTasks.size.toFloat()),
                            4f to totalOps
                        )
                    }
                    Chart(
                        chart = lineChart(),
                        model = usageModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Activity Metrics: Data Requests, Messages, Tasks, Total Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AgentNetworkGraphCard(
    agents: List<Agent>,
    subTasks: List<SubTask>,
    decisions: List<AgentDecision>,
    missions: List<Mission>,
    selectedAgentId: Int?,
    onSelectAgent: (Int) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    // Calculate Collaboration Matrix
    val edges = remember(agents, subTasks, missions, decisions) {
        val result = mutableListOf<CollaborationEdge>()
        val agentList = agents

        for (i in agentList.indices) {
            for (j in i + 1 until agentList.size) {
                val a1 = agentList[i]
                val a2 = agentList[j]

                // Count shared missions
                var sharedCount = 0
                missions.forEach { mission ->
                    val mSubtasks = subTasks.filter { it.missionId == mission.id }
                    val hasA1 = mSubtasks.any { it.assignedAgent.equals(a1.name, ignoreCase = true) }
                    val hasA2 = mSubtasks.any { it.assignedAgent.equals(a2.name, ignoreCase = true) }
                    if (hasA1 && hasA2) sharedCount += 2
                }

                // Count decision mentions
                decisions.forEach { d ->
                    if (d.agentName.equals(a1.name, ignoreCase = true) && d.actionDescription.contains(a2.name, ignoreCase = true)) sharedCount++
                    if (d.agentName.equals(a2.name, ignoreCase = true) && d.actionDescription.contains(a1.name, ignoreCase = true)) sharedCount++
                }

                if (sharedCount > 0) {
                    result.add(CollaborationEdge(a1.id, a2.id, sharedCount))
                }
            }
        }
        result
    }

    // Compute Influence Scores
    val influenceScores = remember(agents, subTasks, decisions, edges) {
        val scores = mutableMapOf<Int, Int>()
        agents.forEach { agent ->
            val completedTasks = subTasks.count {
                it.assignedAgent.equals(agent.name, ignoreCase = true) &&
                        it.status.equals("Completed", ignoreCase = true)
            }
            val decCount = decisions.count { it.agentName.equals(agent.name, ignoreCase = true) }
            val colabCount = edges.filter { it.agent1Id == agent.id || it.agent2Id == agent.id }.sumOf { it.count }

            val score = (completedTasks * 3) + (decCount * 4) + (colabCount * 2) + 10
            scores[agent.id] = score
        }
        scores
    }

    val maxScore = (influenceScores.values.maxOrNull() ?: 1).toFloat()

    // Pulse animation for flow along edges
    val infiniteTransition = rememberInfiniteTransition(label = "flow")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_network_graph_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Agent Influence & Collaboration Graph",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Text(
                        text = "Tap node to inspect agent interactions & influence weight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = containerColor
                ) {
                    Text(
                        text = "${agents.size} Nodes",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Graph Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                var calculatedNodes by remember { mutableStateOf<List<NodePosition>>(emptyList()) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(agents) {
                            detectTapGestures { tapOffset ->
                                val tappedNode = calculatedNodes.find { node ->
                                    val dx = tapOffset.x - node.center.x
                                    val dy = tapOffset.y - node.center.y
                                    sqrt(dx * dx + dy * dy) <= (node.radius + 20f)
                                }
                                tappedNode?.let { onSelectAgent(it.agent.id) }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val center = Offset(width / 2f, height / 2f)
                    val graphRadius = (width.coerceAtMost(height) / 2f) - 50.dp.toPx()

                    val nodePositions = mutableListOf<NodePosition>()
                    val totalAgents = agents.size.coerceAtLeast(1)

                    agents.forEachIndexed { index, agent ->
                        val angle = (2.0 * Math.PI * index / totalAgents) - (Math.PI / 2.0)
                        val nx = center.x + (graphRadius * cos(angle.toFloat()))
                        val ny = center.y + (graphRadius * sin(angle.toFloat()))

                        val score = influenceScores[agent.id] ?: 10
                        val percentage = (score / maxScore) * 100f
                        val nodeRadius = (16.dp.toPx() + (percentage * 0.12f).dp.toPx()).coerceIn(18.dp.toPx(), 32.dp.toPx())

                        nodePositions.add(
                            NodePosition(
                                agent = agent,
                                center = Offset(nx, ny),
                                radius = nodeRadius,
                                influenceScore = score,
                                influencePercentage = percentage
                            )
                        )
                    }
                    calculatedNodes = nodePositions

                    // Map for quick lookup
                    val positionMap = nodePositions.associateBy { it.agent.id }

                    // Draw Edge Connection Lines
                    edges.forEach { edge ->
                        val n1 = positionMap[edge.agent1Id]
                        val n2 = positionMap[edge.agent2Id]

                        if (n1 != null && n2 != null) {
                            val isConnectedToSelected = selectedAgentId != null &&
                                    (selectedAgentId == edge.agent1Id || selectedAgentId == edge.agent2Id)
                            val isDimmed = selectedAgentId != null && !isConnectedToSelected

                            val sWidth = (edge.count * 1.5f + 1f).dp.toPx()
                            val edgeColor = if (isConnectedToSelected) {
                                primaryColor
                            } else if (isDimmed) {
                                outlineColor.copy(alpha = 0.15f)
                            } else {
                                primaryColor.copy(alpha = (0.25f + (edge.count * 0.1f)).coerceAtMost(0.7f))
                            }

                            // Draw main edge line
                            drawLine(
                                color = edgeColor,
                                start = n1.center,
                                end = n2.center,
                                strokeWidth = sWidth,
                                cap = StrokeCap.Round
                            )

                            // Draw animated flow packet along active edge
                            if (!isDimmed) {
                                val packetX = n1.center.x + (n2.center.x - n1.center.x) * flowProgress
                                val packetY = n1.center.y + (n2.center.y - n1.center.y) * flowProgress
                                drawCircle(
                                    color = if (isConnectedToSelected) secondaryColor else primaryColor,
                                    radius = 4.dp.toPx(),
                                    center = Offset(packetX, packetY)
                                )
                            }
                        }
                    }

                    // Draw Agent Nodes
                    nodePositions.forEach { node ->
                        val isSelected = selectedAgentId == node.agent.id
                        val isDimmed = selectedAgentId != null && !isSelected

                        val nodeBgColor = when {
                            isSelected -> primaryColor
                            isDimmed -> primaryColor.copy(alpha = 0.3f)
                            else -> containerColor
                        }

                        val strokeColor = if (isSelected) secondaryColor else primaryColor

                        // Draw outer glow / ring for influence
                        drawCircle(
                            color = strokeColor.copy(alpha = if (isSelected) 0.6f else 0.3f),
                            radius = node.radius + 6.dp.toPx(),
                            style = Stroke(width = if (isSelected) 3.dp.toPx() else 1.5.dp.toPx())
                        )

                        // Draw node circle body
                        drawCircle(
                            color = nodeBgColor,
                            radius = node.radius,
                            center = node.center
                        )

                        // Draw Agent Initials
                        val initials = node.agent.name.take(2).uppercase()
                        val paint = android.graphics.Paint().apply {
                            color = if (isSelected) android.graphics.Color.WHITE else primaryColor.hashCode()
                            textSize = 12.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        drawContext.canvas.nativeCanvas.drawText(
                            initials,
                            node.center.x,
                            node.center.y + (paint.textSize / 3f),
                            paint
                        )

                        // Draw Agent Name label below node
                        val namePaint = android.graphics.Paint().apply {
                            color = if (isDimmed) outlineColor.hashCode() else onSurfaceColor.hashCode()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }

                        val shortName = if (node.agent.name.length > 10) node.agent.name.take(8) + "…" else node.agent.name
                        drawContext.canvas.nativeCanvas.drawText(
                            shortName,
                            node.center.x,
                            node.center.y + node.radius + 14.dp.toPx(),
                            namePaint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend / Instructions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agent Node Size = Influence", style = MaterialTheme.typography.labelSmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(secondaryColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pulsing Dot = Active Flow", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SelectedAgentInfluenceCard(
    agent: Agent,
    allAgents: List<Agent>,
    subTasks: List<SubTask>,
    decisions: List<AgentDecision>,
    missions: List<Mission>,
    onClearSelection: () -> Unit
) {
    val completedSubtasks = remember(subTasks, agent) {
        subTasks.count {
            it.assignedAgent.equals(agent.name, ignoreCase = true) &&
                    it.status.equals("Completed", ignoreCase = true)
        }
    }

    val decisionCount = remember(decisions, agent) {
        decisions.count { it.agentName.equals(agent.name, ignoreCase = true) }
    }

    val topCollaborator = remember(allAgents, subTasks, missions, agent) {
        var topName = "None"
        var maxCount = 0

        allAgents.filter { it.id != agent.id }.forEach { other ->
            var count = 0
            missions.forEach { m ->
                val mSub = subTasks.filter { it.missionId == m.id }
                if (mSub.any { it.assignedAgent.equals(agent.name, ignoreCase = true) } &&
                    mSub.any { it.assignedAgent.equals(other.name, ignoreCase = true) }) {
                    count += 2
                }
            }
            if (count > maxCount) {
                maxCount = count
                topName = other.name
            }
        }
        if (topName == "None" && allAgents.size > 1) {
            topName = allAgents.first { it.id != agent.id }.name
            maxCount = 3
        }
        topName to maxCount
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_agent_influence_profile"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = agent.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val agentMood = remember(agent, subTasks) { com.example.data.calculateAgentMood(agent, subTasks) }
                        Text(
                            text = "${agent.name} ${agentMood.emoji}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${agent.role} • ${agentMood.moodTitle} (${agentMood.loadScore}% load)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(onClick = onClearSelection) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Completed Tasks", style = MaterialTheme.typography.labelSmall)
                    Text("$completedSubtasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("Decisions Logged", style = MaterialTheme.typography.labelSmall)
                    Text("$decisionCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("Top Collaborator", style = MaterialTheme.typography.labelSmall)
                    Text("${topCollaborator.first} (${topCollaborator.second}x)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
