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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.Event
import com.example.ui.components.ChartCalendarOverlay
import com.example.ui.components.CalendarEventOverlay
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
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.mutableIntStateOf
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.example.ui.components.rememberMarker
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.example.ui.components.ChartTypeSelector
import com.example.ui.components.ChartExportButton
import com.example.ui.components.ChartScreenshotButton
import com.example.ui.components.ChartContainerWithEmptyState
import com.example.ui.components.ChartTrendSummaryView
import com.example.ui.components.ChartColorThemeSelector
import com.example.ui.components.getChartColorConfig
import com.example.ui.components.isZeroOrEmpty
import com.example.ui.components.InteractiveChartLegend
import com.example.ui.components.LegendSeriesItem
import com.example.ui.components.ChartThresholdOverlay
import com.example.ui.components.ChartDataTable
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: ColonyViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToCouncil: () -> Unit,
    onNavigateToEfficiency: () -> Unit,
    onNavigateToActivityHeatmap: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit,
    onNavigateToBehaviors: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToSuggested: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddAgent: () -> Unit = {},
    onNavigateToBadges: () -> Unit,
    onNavigateToInterAgentChat: () -> Unit = {},
    onNavigateToCommandInjection: () -> Unit = {},
    onNavigateToTaskBoard: () -> Unit = {},
    onNavigateToPersonas: () -> Unit = {},
    onNavigateToPersonaCategories: () -> Unit = {},
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
    onNavigateToPhase5: () -> Unit = {},
    onNavigateToInteractions: () -> Unit = {},
    onNavigateToPersonaMesh: () -> Unit = {},
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    var showClearInteractionsDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val allInteractions by viewModel.allInteractions.collectAsState()
    val allFtsContent by viewModel.allFtsContent.collectAsState()

    var chartDateRange by remember { mutableIntStateOf(7) }
    var selectedChartType by remember { mutableStateOf("Bar") }
    var selectedActivityChartType by remember { mutableStateOf("Bar") }
    var chartsEntered by remember { androidx.compose.runtime.mutableStateOf(false) }
    var disabledAgentSeries by remember { mutableStateOf(setOf<String>()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        chartsEntered = true
    }

    val primaryColorForLegend = MaterialTheme.colorScheme.primary
    val secondaryColorForLegend = MaterialTheme.colorScheme.secondary
    val tertiaryColorForLegend = MaterialTheme.colorScheme.tertiary
    val errorColorForLegend = MaterialTheme.colorScheme.error

    val legendSeriesList = remember(allInteractions, primaryColorForLegend, secondaryColorForLegend, tertiaryColorForLegend, errorColorForLegend) {
        val seriesPalette = listOf(
            primaryColorForLegend,
            secondaryColorForLegend,
            tertiaryColorForLegend,
            errorColorForLegend,
            androidx.compose.ui.graphics.Color(0xFFE91E63),
            androidx.compose.ui.graphics.Color(0xFF9C27B0),
            androidx.compose.ui.graphics.Color(0xFF009688)
        )
        val groups = allInteractions.groupBy { it.agentName }
        groups.keys.toList().mapIndexed { index, agentName ->
            LegendSeriesItem(
                id = agentName,
                name = agentName,
                color = seriesPalette[index % seriesPalette.size],
                count = groups[agentName]?.size ?: 0
            )
        }
    }

    val interactionChartEntryModel = remember(allInteractions, chartDateRange, chartsEntered, disabledAgentSeries) {
        if (!chartsEntered) {
            return@remember com.patrykandpatrick.vico.core.entry.entryModelOf(
                (0 until chartDateRange).map { com.patrykandpatrick.vico.core.entry.FloatEntry(it.toFloat(), 0f) }
            )
        }
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        
        val activeInteractions = allInteractions.filter { !disabledAgentSeries.contains(it.agentName) }
        val agentInteractions = activeInteractions.groupBy { it.agentName }
        if (agentInteractions.isEmpty()) {
            return@remember com.patrykandpatrick.vico.core.entry.entryModelOf(
                (0 until chartDateRange).map { com.patrykandpatrick.vico.core.entry.FloatEntry(it.toFloat(), 0f) }
            )
        }
        
        val seriesList = agentInteractions.map { (_, interactions) ->
            val counts = Array<Float>(chartDateRange) { 0f }
            interactions.forEach { interaction ->
                val daysAgo = if (interaction.timestamp >= todayStart) {
                    0
                } else {
                    val diffMillis = todayStart - interaction.timestamp
                    (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1
                }
                if (daysAgo in 0 until chartDateRange) {
                    counts[(chartDateRange - 1) - daysAgo] = counts[(chartDateRange - 1) - daysAgo] + 1f
                }
            }
            counts.mapIndexed { index, value -> com.patrykandpatrick.vico.core.entry.FloatEntry(index.toFloat(), value) }
        }
        com.patrykandpatrick.vico.core.entry.entryModelOf(*seriesList.toTypedArray())
    }

    val dbIntegrityOk by viewModel.dbIntegrityOk.collectAsState()

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
    val dailySummaryInsight by viewModel.dailySummaryInsight.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    
    val preferences by viewModel.agentPreferencesState.collectAsState()

    val (interactionCurrentTotal, interactionPreviousTotal, comparisonDays) = remember(
        allInteractions,
        disabledAgentSeries,
        preferences.trendComparisonInterval,
        preferences.trendAggregationMethod,
        chartDateRange
    ) {
        val active = allInteractions.filter { !disabledAgentSeries.contains(it.agentName) }
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        val intervalDays = when (preferences.trendComparisonInterval) {
            "Daily" -> 1
            "Monthly" -> 30
            else -> 7 // "Weekly"
        }

        val curDailyCounts = FloatArray(intervalDays) { 0f }
        val prevDailyCounts = FloatArray(intervalDays) { 0f }

        val currentPeriodStart = now - (intervalDays * oneDayMs)
        val previousPeriodStart = now - (2 * intervalDays * oneDayMs)

        active.forEach { interaction ->
            if (interaction.timestamp in currentPeriodStart..now) {
                val idx = (((now - interaction.timestamp) / oneDayMs).toInt()).coerceIn(0, intervalDays - 1)
                curDailyCounts[idx] += 1f
            } else if (interaction.timestamp in previousPeriodStart until currentPeriodStart) {
                val idx = (((currentPeriodStart - interaction.timestamp) / oneDayMs).toInt()).coerceIn(0, intervalDays - 1)
                prevDailyCounts[idx] += 1f
            }
        }

        fun computeMetric(counts: FloatArray, method: String): Double {
            if (counts.isEmpty()) return 0.0
            return when (method) {
                "Average" -> counts.average()
                "Median" -> {
                    val sorted = counts.sorted()
                    val mid = sorted.size / 2
                    if (sorted.size % 2 == 1) sorted[mid].toDouble()
                    else (sorted[mid - 1] + sorted[mid]) / 2.0
                }
                else -> counts.sum().toDouble() // "Total Sum"
            }
        }

        val cur = computeMetric(curDailyCounts, preferences.trendAggregationMethod)
        val prev = computeMetric(prevDailyCounts, preferences.trendAggregationMethod)
        Triple(cur, prev, intervalDays)
    }

    val dailyTotals = remember(allInteractions, disabledAgentSeries, chartDateRange) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        val active = allInteractions.filter { !disabledAgentSeries.contains(it.agentName) }
        val counts = FloatArray(chartDateRange) { 0f }
        active.forEach { interaction ->
            val daysAgo = if (interaction.timestamp >= todayStart) {
                0
            } else {
                val diffMillis = todayStart - interaction.timestamp
                (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            }
            if (daysAgo in 0 until chartDateRange) {
                counts[(chartDateRange - 1) - daysAgo] += 1f
            }
        }
        counts.toList()
    }

    val chartDateLabels = remember(chartDateRange) {
        val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        (0 until chartDateRange).map { index ->
            val daysAgo = (chartDateRange - 1) - index
            sdf.format(java.util.Date(now - (daysAgo * oneDayMs)))
        }
    }

    val avgDailyCount = remember(dailyTotals) {
        if (dailyTotals.isEmpty()) 0f else dailyTotals.average().toFloat()
    }

    val thresholdValue = remember(avgDailyCount, preferences.trendAlertThreshold) {
        avgDailyCount * (1f + (preferences.trendAlertThreshold / 100f))
    }

    val maxChartValue = remember(dailyTotals, thresholdValue) {
        val maxVal = dailyTotals.maxOrNull() ?: 1f
        maxOf(maxVal, thresholdValue) * 1.15f
    }

    val (colonyActivityCurrentTotal, colonyActivityPreviousTotal) = remember(decisions, subTasksList) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val currentPeriodStart = now - (7 * oneDayMs)
        val previousPeriodStart = now - (14 * oneDayMs)

        val curDecisions = decisions.count { it.timestamp in currentPeriodStart..now }
        val curTasks = subTasksList.count { it.timestamp in currentPeriodStart..now }

        val prevDecisions = decisions.count { it.timestamp in previousPeriodStart until currentPeriodStart }
        val prevTasks = subTasksList.count { it.timestamp in previousPeriodStart until currentPeriodStart }

        (curDecisions + curTasks) to (prevDecisions + prevTasks)
    }

    androidx.compose.runtime.LaunchedEffect(preferences.selectedChartType) {
        if (preferences.selectedChartType.isNotEmpty()) {
            selectedChartType = preferences.selectedChartType
            selectedActivityChartType = preferences.selectedChartType
        }
    }

    androidx.compose.runtime.LaunchedEffect(preferences.selectedChartDateRange) {
        if (preferences.selectedChartDateRange > 0) {
            chartDateRange = preferences.selectedChartDateRange
        }
    }
    
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

    LaunchedEffect(Unit) {
        viewModel.generateDailySummaryInsight()
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
    var showQuickCreateDialog by remember { mutableStateOf(false) }
    var showCreatePersonaDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedRoleFilter by remember { mutableStateOf("All") }
    var selectedTagFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Name") } // "Name", "Role", "Status"
    
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var avatarTheme by remember { mutableStateOf("Default") }
    val selectedAgents = remember { mutableStateListOf<Agent>() }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showBulkTagsDialog by remember { mutableStateOf(false) }
    var showBulkDeleteConfirmDialog by remember { mutableStateOf(false) }
    var agentForAvatar by remember { mutableStateOf<Agent?>(null) }
    
    var selectedAgentForActivity by remember { mutableStateOf<Agent?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    val availableRoles = remember(agents) {
        listOf("All") + agents.map { it.type }.distinct().filter { it.isNotBlank() }
    }

    val availableTags = remember(agents) {
        listOf("All") + agents.flatMap { it.traits.split(",") }.map { it.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredAgents = remember(agents, searchQuery, selectedStatusFilter, selectedRoleFilter, selectedTagFilter, sortBy, subTasksList) {
        var result = agents.filter { agent ->
            val isWorking = subTasksList.any { task ->
                task.assignedAgent.equals(agent.name, ignoreCase = true) && task.status == "In Progress"
            }
            val statusText = when {
                agent.status == "Resting" -> "Resting"
                agent.status != "Active" -> "Idle"
                isWorking -> "Working"
                else -> "Active"
            }
            
            val agentTasks = subTasksList.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) }
            val taskMatches = agentTasks.any { task ->
                task.description.contains(searchQuery, ignoreCase = true) ||
                task.status.contains(searchQuery, ignoreCase = true)
            }
            
            agent.name.contains(searchQuery, ignoreCase = true) ||
            agent.type.contains(searchQuery, ignoreCase = true) ||
            statusText.contains(searchQuery, ignoreCase = true) ||
            agent.status.contains(searchQuery, ignoreCase = true) ||
            agent.traits.contains(searchQuery, ignoreCase = true) ||
            taskMatches
        }
        
        if (selectedStatusFilter != "All") {
            result = result.filter { agent ->
                val isBusy = subTasksList.any { task ->
                    task.assignedAgent.equals(agent.name, ignoreCase = true) && task.status == "In Progress"
                }
                val statusText = when {
                    agent.status == "Resting" -> "Resting"
                    agent.status == "Offline" -> "Offline"
                    agent.status != "Active" -> "Idle"
                    isBusy -> "Busy"
                    else -> "Active"
                }
                statusText.equals(selectedStatusFilter, ignoreCase = true) || agent.status.equals(selectedStatusFilter, ignoreCase = true)
            }
        }
        
        if (selectedRoleFilter != "All") {
            result = result.filter { agent ->
                agent.type.equals(selectedRoleFilter, ignoreCase = true)
            }
        }
        
        if (selectedTagFilter != "All") {
            result = result.filter { agent ->
                agent.traits.split(",").map { it.trim() }.any { it.equals(selectedTagFilter, ignoreCase = true) }
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

    val activityModel = remember(decisions, subTasksList, chartsEntered) {
        if (!chartsEntered) {
            return@remember com.patrykandpatrick.vico.core.entry.entryModelOf(
                (0..6).map { com.patrykandpatrick.vico.core.entry.FloatEntry(it.toFloat(), 0f) }
            )
        }
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

    SharedTransitionLayout {
    var showQuickMessageDialog by remember { mutableStateOf(false) }
    
    Scaffold(
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
                                showBulkTagsDialog = true
                            }
                        }) {
                            Icon(Icons.Default.Label, contentDescription = "Bulk Add Tags")
                        }

                        IconButton(onClick = {
                            if (selectedAgents.isNotEmpty()) {
                                showBulkDeleteConfirmDialog = true
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
                        IconButton(onClick = {
                            com.example.utils.ExportRoomBackupHelper.exportFullRoomBackupToJson(
                                context = context,
                                agents = agents,
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
                        }) {
                            Icon(Icons.Filled.FileDownload, contentDescription = "Export Full Backup JSON")
                        }
                        Button(
                            onClick = { viewModel.triggerPanic() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("PANIC")
                        }
                        var showThemeMenu by remember { mutableStateOf(false) }
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
                                DropdownMenuItem(
                                    text = { Text("Activity Heatmap") },
                                    onClick = {
                                        showThemeMenu = false
                                        onNavigateToActivityHeatmap()
                                    }
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToSuggested, modifier = Modifier.testTag("nav_to_suggested_btn")) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Suggested Agents")
                        }
                        Box {
                            var showAvatarThemeMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showAvatarThemeMenu = true }) {
                                Icon(Icons.Default.Palette, contentDescription = "Avatar Theme")
                            }
                            DropdownMenu(
                                expanded = showAvatarThemeMenu,
                                onDismissRequest = { showAvatarThemeMenu = false }
                            ) {
                                listOf("Default", "Neon", "Pastel", "Dark").forEach { theme ->
                                    DropdownMenuItem(
                                        text = { Text(theme) },
                                        onClick = { 
                                            avatarTheme = theme
                                            showAvatarThemeMenu = false
                                        }
                                    )
                                }
                            }
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
                    if (!isMultiSelectMode && allInteractions.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { showQuickMessageDialog = true },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "Quick Message")
                        }
                    }
                    if (allInteractions.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { exportInteractionsCsv(context, allInteractions) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.testTag("fab_export_interactions")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Export Interactions")
                        }
                    }
                    FloatingActionButton(
                        onClick = { viewModel.updateFocusModeActive(true) },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.testTag("fab_toggle_focus_mode")
                    ) {
                        Icon(Icons.Filled.Psychology, contentDescription = "Głębokie skupienie")
                    }
                    FloatingActionButton(
                        onClick = onNavigateToPersonas,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Personas")
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
                        onClick = { showQuickCreateDialog = true },
                        modifier = Modifier.testTag("fab_quick_add_agent"),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Quick Create Agent")
                    }

                    FloatingActionButton(
                        onClick = { showCreatePersonaDialog = true },
                        modifier = Modifier.testTag("fab_add_persona_agent")
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = "Add Persona Agent")
                    }
                }
            }
        }
    ) { padding ->

        if (showQuickMessageDialog) {
            val lastAgentName = allInteractions.firstOrNull()?.agentName ?: "Unknown Agent"
            var quickMessage by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showQuickMessageDialog = false },
                title = { Text("Quick Message to $lastAgentName") },
                text = {
                    OutlinedTextField(
                        value = quickMessage,
                        onValueChange = { quickMessage = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.interactionLogger.logInteraction(lastAgentName, quickMessage)
                                viewModel.sendInterAgentMessage("User", "User", quickMessage, lastAgentName)
                                showQuickMessageDialog = false
                            }
                        }
                    ) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuickMessageDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

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
                    if (!dbIntegrityOk) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = {  },
                            title = { androidx.compose.material3.Text("Database Corrupted") },
                            text = { androidx.compose.material3.Text("The local database file is corrupted. You can reset the database, which will erase all data, or contact support.") },
                            confirmButton = {
                                androidx.compose.material3.TextButton(onClick = { viewModel.resetDatabase() }) {
                                    androidx.compose.material3.Text("Reset Database")
                                }
                            }
                        )
                    }

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
                    
                    com.example.ui.components.ActiveAgentsGridWidget(
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Interaction Frequency Chart
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                        text = "Interaction Frequency",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    ChartScreenshotButton(
                                        chartTitle = "Interaction Frequency",
                                        dateRangeDays = chartDateRange,
                                        dailyTotals = dailyTotals,
                                        dateLabels = chartDateLabels,
                                        iconOnly = true
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(7 to "7D", 14 to "14D", 30 to "30D").forEach { (days, label) ->
                                        FilterChip(
                                            selected = chartDateRange == days,
                                            onClick = {
                                                chartDateRange = days
                                                viewModel.updateSelectedChartDateRange(days)
                                            },
                                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ChartTrendSummaryView(
                                currentTotal = interactionCurrentTotal,
                                previousTotal = interactionPreviousTotal,
                                unitLabel = if (preferences.trendAggregationMethod == "Total Sum") "interactions" else "avg/day",
                                periodDays = comparisonDays,
                                trendAlertThreshold = preferences.trendAlertThreshold,
                                trendAlertsEnabled = preferences.trendAlertsEnabled,
                                comparisonInterval = preferences.trendComparisonInterval,
                                aggregationMethod = preferences.trendAggregationMethod,
                                onUpdateInterval = { viewModel.updateTrendComparisonInterval(it) },
                                onUpdateAggregation = { viewModel.updateTrendAggregationMethod(it) },
                                onUpdateThreshold = { viewModel.updateTrendAlertThreshold(it) },
                                onToggleAlerts = { viewModel.updateTrendAlertsEnabled(it) },
                                onTriggerWorkManagerCheck = { viewModel.triggerInteractionAnomalyCheck() }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (legendSeriesList.isNotEmpty()) {
                                InteractiveChartLegend(
                                    seriesList = legendSeriesList,
                                    disabledSeriesIds = disabledAgentSeries,
                                    onToggleSeries = { id ->
                                        disabledAgentSeries = if (disabledAgentSeries.contains(id)) {
                                            disabledAgentSeries - id
                                        } else {
                                            disabledAgentSeries + id
                                        }
                                    },
                                    onResetAll = { disabledAgentSeries = emptySet() }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ChartExportButton(
                                        interactions = allInteractions,
                                        dateRangeDays = chartDateRange,
                                        dailyTotals = dailyTotals,
                                        dateLabels = chartDateLabels
                                    )
                                    ChartScreenshotButton(
                                        chartTitle = "Interaction Frequency",
                                        dateRangeDays = chartDateRange,
                                        dailyTotals = dailyTotals,
                                        dateLabels = chartDateLabels,
                                        iconOnly = false
                                    )
                                }
                                ChartTypeSelector(
                                    selectedType = selectedChartType,
                                    onTypeSelected = { newType ->
                                        selectedChartType = newType
                                        viewModel.updateSelectedChartType(newType)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ChartColorThemeSelector(
                                selectedTheme = preferences.chartColorIntensity,
                                onThemeSelected = { viewModel.updateChartColorIntensity(it) }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = preferences.showCalendarOverlay,
                                    onClick = { viewModel.updateShowCalendarOverlay(!preferences.showCalendarOverlay) },
                                    label = { Text("Calendar Events Overlay", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("toggle_calendar_overlay_chip")
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val hasInteractionData = remember(interactionChartEntryModel) {
                                !interactionChartEntryModel.isZeroOrEmpty()
                            }
                            ChartContainerWithEmptyState(
                                hasData = hasInteractionData,
                                emptyTitle = "No Interaction Data",
                                emptyMessage = "No agent interactions were recorded during the last $chartDateRange days.",
                                actionLabel = if (chartDateRange < 30) "View 30 Days" else null,
                                actionIcon = Icons.Default.Refresh,
                                onActionClick = if (chartDateRange < 30) {
                                    {
                                        chartDateRange = 30
                                        viewModel.updateSelectedChartDateRange(30)
                                    }
                                } else null,
                                emptyStateHeight = 160.dp
                            ) {
                                ProvideChartStyle(m3ChartStyle()) {
                                    val marker1 = rememberMarker()
                                    val chartColorConfig = getChartColorConfig(preferences.chartColorIntensity)

                                    val sampleCalendarOverlayEvents = remember(chartDateRange, chartDateLabels) {
                                        if (chartDateLabels.size < 2) emptyList()
                                        else {
                                            val midIndex = (chartDateLabels.size / 2).coerceIn(0, chartDateLabels.size - 1)
                                            val lastIndex = (chartDateLabels.size - 1).coerceIn(0, chartDateLabels.size - 1)
                                            listOf(
                                                CalendarEventOverlay(
                                                    title = "Model v3.5 Launch",
                                                    dateLabel = chartDateLabels.getOrElse(midIndex) { "Day ${midIndex + 1}" },
                                                    dayIndex = midIndex,
                                                    category = "Product Milestone"
                                                ),
                                                CalendarEventOverlay(
                                                    title = "Colony Benchmark",
                                                    dateLabel = chartDateLabels.getOrElse(lastIndex) { "Day ${lastIndex + 1}" },
                                                    dayIndex = lastIndex,
                                                    category = "Scheduled Sprint"
                                                )
                                            )
                                        }
                                    }

                                    AnimatedContent(
                                        targetState = selectedChartType,
                                        transitionSpec = {
                                            (fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.96f, animationSpec = tween(350)))
                                                .togetherWith(fadeOut(animationSpec = tween(200)))
                                        },
                                        label = "chart_view_mode_fade_transition"
                                    ) { chartType ->
                                        val chartSpec1 = when (chartType) {
                                            "Bar" -> columnChart()
                                            "Area" -> lineChart(
                                                lines = listOf(
                                                    lineSpec(
                                                        lineColor = chartColorConfig.primaryColor,
                                                        lineBackgroundShader = verticalGradient(chartColorConfig.gradientColors)
                                                    )
                                                )
                                            )
                                            else -> lineChart(
                                                lines = listOf(
                                                    lineSpec(
                                                        lineColor = chartColorConfig.primaryColor,
                                                        lineBackgroundShader = null
                                                    )
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                        ) {
                                            Chart(
                                                chart = chartSpec1,
                                                model = interactionChartEntryModel,
                                                marker = marker1,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                            )
                                            ChartThresholdOverlay(
                                                thresholdValue = thresholdValue,
                                                maxChartValue = maxChartValue,
                                                dailyValues = dailyTotals,
                                                thresholdPercentage = preferences.trendAlertThreshold,
                                                dateLabels = chartDateLabels,
                                                isEnabled = preferences.trendAlertsEnabled,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            ChartCalendarOverlay(
                                                events = sampleCalendarOverlayEvents,
                                                totalDays = chartDateRange,
                                                showOverlay = preferences.showCalendarOverlay,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Scrollable Raw Data Table
                            ChartDataTable(
                                interactions = allInteractions,
                                dateRangeDays = chartDateRange,
                                disabledAgentSeries = disabledAgentSeries
                            )
                        }
                    }

                    // Top Performing Agents Summary
                    val topPerformingAgents = remember(allInteractions) {
                        allInteractions.groupBy { it.agentName }
                            .mapValues { it.value.size }
                            .toList()
                            .sortedByDescending { it.second }
                            .take(3)
                    }

                    if (topPerformingAgents.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Top Performing Agents",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                topPerformingAgents.forEachIndexed { index, (agentName, count) ->
                                    var expanded by remember(agentName) { mutableStateOf(false) }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expanded = !expanded }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${index + 1}.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.width(24.dp)
                                                )
                                                val agent = agents.find { it.name == agentName }
                                                val statusColor = when (agent?.status?.lowercase()) {
                                                    "active", "online" -> Color(0xFF4CAF50)
                                                    "busy", "working" -> Color(0xFFFFEB3B)
                                                    "offline", "error" -> Color(0xFFF44336)
                                                    else -> Color.Gray
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(statusColor, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = agentName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "$count interactions",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (expanded) {
                                            val agentInteractions = allInteractions.filter { it.agentName == agentName }
                                            val breakdowns = remember(agentInteractions) {
                                                val emailCount = agentInteractions.count { it.snippet.contains("email", ignoreCase = true) || it.snippet.contains("mail", ignoreCase = true) }
                                                val calendarCount = agentInteractions.count { it.snippet.contains("calendar", ignoreCase = true) || it.snippet.contains("event", ignoreCase = true) || it.snippet.contains("schedule", ignoreCase = true) }
                                                val taskCount = agentInteractions.count { it.snippet.contains("task", ignoreCase = true) || it.snippet.contains("todo", ignoreCase = true) }
                                                val otherCount = Math.max(0, agentInteractions.size - (emailCount + calendarCount + taskCount))
                                                
                                                listOfNotNull(
                                                    if (emailCount > 0) "Email" to emailCount else null,
                                                    if (calendarCount > 0) "Calendar" to calendarCount else null,
                                                    if (taskCount > 0) "Tasks" to taskCount else null,
                                                    if (otherCount > 0) "Other" to otherCount else null
                                                ).ifEmpty { listOf("Other" to agentInteractions.size) }
                                            }
                                            Column(modifier = Modifier.padding(start = 38.dp, top = 8.dp, bottom = 4.dp)) {
                                                val totalCount = agentInteractions.size.coerceAtLeast(1)
                                                breakdowns.forEach { (type, typeCount) ->
                                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = type,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                            )
                                                            Text(
                                                                text = typeCount.toString(),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        LinearProgressIndicator(
                                                            progress = { typeCount.toFloat() / totalCount },
                                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                                            color = MaterialTheme.colorScheme.primary,
                                                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sync Status Indicator
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (icon, color) = when (syncStatus) {
                                "Synced", "SUCCEEDED" -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.primary
                                "Syncing", "RUNNING" -> Icons.Filled.CloudSync to MaterialTheme.colorScheme.tertiary
                                "Failed", "FAILED" -> Icons.Filled.ErrorOutline to MaterialTheme.colorScheme.error
                                else -> Icons.Filled.CloudQueue to MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Sync Status",
                                tint = color
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DB Export Sync Status: $syncStatus",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Top-Level Dashboard Summary
                    val activeAgentsCount = agents.count { it.status == "Active" }
                    val totalTasks = subTasksList.size
                    val completedTasks = subTasksList.count { it.status == "Completed" }
                    val completionPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) * 100 else 0f
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Active Agents", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$activeAgentsCount / ${agents.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Task Completion", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${completionPercentage.toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // Top summary widget
                    TeamPerformanceVicoChartWidget(
                        agents = filteredAgents,
                        subTasks = subTasksList,
                        decisions = decisions,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DailyInteractionSummaryWidget(
                        decisions = decisions,
                        subTasks = subTasksList,
                        missionLogs = missionLogs,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 1. SEARCH BAR
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search agents by name, category, status, or tags...") },
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
                        Button(onClick = onNavigateToCommandInjection, modifier = Modifier.testTag("nav_to_command_injection_btn")) {
                            Text("Cmd Inject")
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        
                        Button(onClick = onNavigateToPersonaMesh, modifier = Modifier.testTag("nav_to_persona_mesh_btn")) {
                            Text("PersonaMesh Agents")
                        }
                        Button(onClick = onNavigateToPersonaColony, modifier = Modifier.testTag("nav_to_personas_btn")) {
                            Text("Personas")
                        }
                        Button(onClick = onNavigateToEfficiency) {
                            Text("Stats")
                        }
                        Button(onClick = onNavigateToBadges, modifier = Modifier.testTag("nav_to_badges_btn")) {
                            Text("Badges")
                        }
                        Button(onClick = onNavigateToPersonaCategories, modifier = Modifier.testTag("nav_to_persona_categories_btn")) {
                            Text("Persona Chart")
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

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onNavigateToInteractions, modifier = Modifier.weight(1f)) {
                            Text("View Interactions")
                        }
                        OutlinedButton(onClick = { showClearInteractionsDialog = true }, modifier = Modifier.weight(1f)) {
                            Text("Clear Interactions")
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
                                    missionId = System.currentTimeMillis().toInt() % 100000,
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
                                        text = "Colony Activity & Throughput",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    ChartScreenshotButton(
                                        chartTitle = "Colony Activity & Throughput",
                                        dateRangeDays = 7,
                                        iconOnly = true
                                    )
                                }
                                ChartTypeSelector(
                                    selectedType = selectedActivityChartType,
                                    onTypeSelected = { newType ->
                                        selectedActivityChartType = newType
                                        viewModel.updateSelectedChartType(newType)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            ChartTrendSummaryView(
                                currentTotal = colonyActivityCurrentTotal,
                                previousTotal = colonyActivityPreviousTotal,
                                unitLabel = "actions",
                                periodDays = 7,
                                trendAlertThreshold = preferences.trendAlertThreshold,
                                trendAlertsEnabled = preferences.trendAlertsEnabled,
                                onUpdateThreshold = { viewModel.updateTrendAlertThreshold(it) },
                                onToggleAlerts = { viewModel.updateTrendAlertsEnabled(it) },
                                onTriggerWorkManagerCheck = { viewModel.triggerInteractionAnomalyCheck() }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val hasActivityData = remember(activityModel) {
                                !activityModel.isZeroOrEmpty()
                            }
                            ChartContainerWithEmptyState(
                                hasData = hasActivityData,
                                emptyTitle = "No Colony Activity",
                                emptyMessage = "No decisions or tasks were completed in the last 7 days.",
                                emptyStateHeight = 140.dp
                            ) {
                                ProvideChartStyle(m3ChartStyle()) {
                                    val marker2 = rememberMarker()
                                    val chartColorConfig2 = getChartColorConfig(preferences.chartColorIntensity)
                                    val chartSpec2 = when (selectedActivityChartType) {
                                        "Bar" -> columnChart()
                                        "Area" -> lineChart(
                                            lines = listOf(
                                                lineSpec(
                                                    lineColor = chartColorConfig2.primaryColor,
                                                    lineBackgroundShader = verticalGradient(chartColorConfig2.gradientColors)
                                                )
                                            )
                                        )
                                        else -> lineChart(
                                            lines = listOf(
                                                lineSpec(
                                                    lineColor = chartColorConfig2.primaryColor,
                                                    lineBackgroundShader = null
                                                )
                                            )
                                        )
                                    }
                                    Chart(
                                        chart = chartSpec2,
                                        model = activityModel,
                                        marker = marker2,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                    )
                                }
                            }
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
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (dailySummaryInsight.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini Insight",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Daily Summary",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dailySummaryInsight,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

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


                    MonthlyAnalyticsDashboardWidget(
                        subTasksList = subTasksList,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    TaskCompletionRateWidget(
                        subTasksList = subTasksList,
                        missionLogs = missionLogs,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    
                    ContributionGridWidget(modifier = Modifier.padding(bottom = 20.dp))
                    
                    AgentResourceUsageWidget()
                    MissionBatchWidget()

                    AgentActivityVicoChartWidget(
                        agents = agents,
                        subTasks = subTasksList,
                        decisions = decisions,
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
                            val statusFilters = listOf("All", "Active", "Busy", "Idle", "Offline", "Resting")
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
                        
                        // Tag Filters
                        if (availableTags.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tags: ",
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
                                    availableTags.forEach { tag ->
                                        val isSelected = selectedTagFilter == tag
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedTagFilter = tag },
                                            label = { Text(tag) },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null,
                                            modifier = Modifier.testTag("tag_filter_$tag")
                                        )
                                    }
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
                    val filteredSubTasksForAgentCard = remember(subTasksList, searchQuery) {
                        subTasksList.filter { task ->
                            if (searchQuery.isBlank()) true
                            else task.description.contains(searchQuery, ignoreCase = true) || task.status.contains(searchQuery, ignoreCase = true)
                        }
                    }
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
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${agent.name} deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreAgents(listOf(agent))
                                    }
                                }
                                true
                            } else if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleAgentStatus(agent)
                                coroutineScope.launch {
                                    val action = if (agent.status == "Active") "Paused" else "Activated"
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${agent.name} $action",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.toggleAgentStatus(agent)
                                    }
                                }
                                false
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
                                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> androidx.compose.ui.graphics.Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp)
                                    .background(color, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    else -> Alignment.Center
                                }
                            ) {
                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Agent",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                } else if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                    Icon(
                                        imageVector = if (agent.status == "Active") Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Toggle Status",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        },
                        enableDismissFromStartToEnd = true
                    ) {
                        AnimatedVisibility(visible = true) {
                            Box(
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "agent_card_${agent.id}"),
                                    animatedVisibilityScope = this@AnimatedVisibility
                                )
                            ) {
                                AgentCard(
                                    agent = agent,
                                    isWorking = isWorking,
                                    isSelectionMode = isMultiSelectMode,
                                    isSelected = isSelected,
                                    categoryColorHex = catHex,
                                    avatarTheme = avatarTheme,
                                    prediction = predictions[agent.id],
                                    subTasks = filteredSubTasksForAgentCard,
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
                                    },
                                    onAvatarClick = {
                                        agentForAvatar = agent
                                    },
                                    onUpdateAgent = { updatedAgent ->
                                        viewModel.updateAgent(updatedAgent)
                                    },
                                    onViewHistory = {
                                        selectedAgentForActivity = agent
                                    }
                                )
                            }
                        }
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
        
        agentForAvatar?.let { agent ->
            AvatarSelectionDialog(
                agent = agent,
                onDismiss = { agentForAvatar = null },
                onAvatarSelected = { url ->
                    viewModel.updateAgentAvatar(agent.id, url)
                    agentForAvatar = null
                }
            )
        }

        if (showClearInteractionsDialog) {
            AlertDialog(
                onDismissRequest = { showClearInteractionsDialog = false },
                title = { Text("Clear All Interactions") },
                text = { Text("Are you sure you want to delete all stored agent interactions? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.interactionLogger.clearInteractions()
                                showClearInteractionsDialog = false
                            }
                        }
                    ) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearInteractionsDialog = false }) {
                        Text("Cancel")
                    }
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

        if (showCreatePersonaDialog) {
            var name by remember { mutableStateOf("") }
            var expertiseCategory by remember { mutableStateOf("") }
            var initialStatus by remember { mutableStateOf("Active") }
            val categories = listOf("Utility", "Analyst", "Support", "Creative", "Developer", "General")
            val statuses = listOf("Active", "Resting", "Offline")
            
            AlertDialog(
                onDismissRequest = { showCreatePersonaDialog = false },
                title = { Text("Create Persona Agent") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Text("Expertise Category", style = MaterialTheme.typography.labelMedium)
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = expertiseCategory == category,
                                    onClick = { expertiseCategory = category },
                                    label = { Text(category) }
                                )
                            }
                        }
                        
                        Text("Initial Status", style = MaterialTheme.typography.labelMedium)
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(statuses) { status ->
                                FilterChip(
                                    selected = initialStatus == status,
                                    onClick = { initialStatus = status },
                                    label = { Text(status) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank() && expertiseCategory.isNotBlank()) {
                                viewModel.addAgent(
                                    com.example.data.Agent(
                                        name = name,
                                        type = expertiseCategory,
                                        role = "Persona Agent",
                                        status = initialStatus,
                                        permissions = "Basic",
                                        traits = expertiseCategory,
                                        systemPrompt = "You are a Persona Agent with expertise in $expertiseCategory."
                                    )
                                )
                                showCreatePersonaDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePersonaDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showQuickCreateDialog) {
            var newAgentName by remember { mutableStateOf("") }
            var selectedTemplateIdx by remember { mutableStateOf(0) }
            
            val agentTemplates = remember {
                listOf(
                    com.example.data.Agent(name = "", type = "Utility", role = "General Assistant", permissions = "Basic", iconName = "smart_toy", traits = "Helpful, Efficient", systemPrompt = "You are a general assistant ready to help with tasks.", autonomyLevel = "Needs Confirmation"),
                    com.example.data.Agent(name = "", type = "Analyst", role = "Data Processor", permissions = "Intermediate", iconName = "analytics", traits = "Analytical, Detail-Oriented", systemPrompt = "You process data and analyze patterns.", autonomyLevel = "Full Autonomy"),
                    com.example.data.Agent(name = "", type = "Support", role = "Customer Service", permissions = "Basic", iconName = "support_agent", traits = "Empathetic, Responsive", systemPrompt = "You assist users and answer questions politely.", autonomyLevel = "Needs Confirmation")
                )
            }
            
            AlertDialog(
                onDismissRequest = { showQuickCreateDialog = false },
                title = { Text("Quick Create Agent") },
                text = {
                    Column {
                        Text("Spawn a new agent instantly with default optimal settings.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newAgentName,
                            onValueChange = { newAgentName = it },
                            label = { Text("Agent Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select Template:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        agentTemplates.forEachIndexed { index, template ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTemplateIdx = index }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedTemplateIdx == index, onClick = { selectedTemplateIdx = index })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(template.type, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(template.traits, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newAgentName.isNotBlank()) {
                                val template = agentTemplates[selectedTemplateIdx]
                                viewModel.addAgent(
                                    name = newAgentName.trim(),
                                    type = template.type,
                                    role = template.role,
                                    permissions = template.permissions,
                                    iconName = template.iconName,
                                    traits = template.traits,
                                    systemPrompt = template.systemPrompt,
                                    autonomyLevel = template.autonomyLevel
                                )
                                showQuickCreateDialog = false
                            }
                        }
                    ) {
                        Text("Spawn Agent")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuickCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showPriorityDialog) {
            var selectedAutonomy by remember { mutableStateOf("Needs Confirmation") }
            var selectedPerms by remember { mutableStateOf("Basic") }
            var selectedPriority by remember { mutableStateOf("Normal") }
            
            val autonomyLevels = listOf("Strict Control", "Needs Confirmation", "Full Autonomy")
            val permissionLevels = listOf("Basic", "Intermediate", "Admin")
            val priorityLevels = listOf("Low", "Normal", "Medium", "High")

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
                            Text("Priority", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            priorityLevels.forEach { prio ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPriority = prio }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(selected = selectedPriority == prio, onClick = { selectedPriority = prio })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(prio, style = MaterialTheme.typography.bodyMedium)
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
                                selectedPerms,
                                selectedPriority
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

        if (showBulkTagsDialog) {
            var prefixTag by remember { mutableStateOf("") }
            var suffixTag by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showBulkTagsDialog = false },
                title = { Text("Bulk Add Tags/Traits") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Apply common tags to ${selectedAgents.size} selected agents.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = prefixTag,
                            onValueChange = { prefixTag = it },
                            label = { Text("Prefix Tag (e.g. 'Priority')") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = suffixTag,
                            onValueChange = { suffixTag = it },
                            label = { Text("Suffix Tag (e.g. 'Backend')") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val agentsToUpdate = selectedAgents.toList()
                            val prefix = prefixTag
                            val suffix = suffixTag
                            selectedAgents.clear()
                            isMultiSelectMode = false
                            showBulkTagsDialog = false
                            
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Applying bulk tags...")
                                viewModel.bulkAddTags(agentsToUpdate, prefix, suffix)
                                snackbarHostState.showSnackbar("Bulk tags applied to ${agentsToUpdate.size} agents.")
                            }
                        }
                    ) {
                        Text("Apply Tags")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBulkTagsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showBulkDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showBulkDeleteConfirmDialog = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete ${selectedAgents.size} agent(s)? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val deletedAgents = selectedAgents.toList()
                            viewModel.bulkDeleteAgents(deletedAgents.map { it.id })
                            selectedAgents.clear()
                            isMultiSelectMode = false
                            showBulkDeleteConfirmDialog = false
                            
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

        // ModalBottomSheet for Agent Interaction History
        if (selectedAgentForActivity != null) {
            val agent = selectedAgentForActivity!!
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            val interactions = allFtsContent.filter { it.agentName.equals(agent.name, ignoreCase = true) }.sortedByDescending { it.timestamp }
            var newNoteText by remember { mutableStateOf("") }
            
            ModalBottomSheet(
                onDismissRequest = { selectedAgentForActivity = null },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${agent.name} - Interaction History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newNoteText,
                            onValueChange = { newNoteText = it },
                            label = { Text("Add manual note...") },
                            modifier = Modifier.weight(1f),
                            singleLine = false,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newNoteText.isNotBlank()) {
                                    viewModel.insertFtsInteraction(agent.name, newNoteText, modelUsed = "Manual Note", tag = "Note")
                                    newNoteText = ""
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                    
                    if (interactions.isEmpty()) {
                        Text(
                            text = "No interaction history available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(interactions.size) { index ->
                                val interaction = interactions[index]
                                val dateStr = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(interaction.timestamp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = interaction.tag.ifEmpty { "Interaction" },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = interaction.snippet,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Model: ${interaction.modelUsed}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
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

fun exportTaskHistoryToCsv(context: android.content.Context, agentName: String, logs: List<ActivityEntry>) {
    try {
        val fileName = "task_history_${agentName.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
        val file = java.io.File(context.cacheDir, fileName)
        
        val writer = file.bufferedWriter()
        writer.write("Timestamp,Type,Title,Description,Metadata\n")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        logs.forEach { entry ->
            val dateStr = sdf.format(java.util.Date(entry.timestamp))
            val safeTitle = entry.title.replace("\"", "\"\"")
            val safeDesc = entry.description.replace("\"", "\"\"")
            val safeMeta = entry.metadata.replace("\"", "\"\"")
            writer.write("\"$dateStr\",\"${entry.type}\",\"$safeTitle\",\"$safeDesc\",\"$safeMeta\"\n")
        }
        writer.close()
        
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Export Task History"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

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





fun exportInteractionsCsv(
    context: android.content.Context,
    interactions: List<com.example.data.InteractionRecord>
) {
    try {
        val file = java.io.File(context.cacheDir, "interactions_export.csv")
        val writer = java.io.FileWriter(file)
        writer.write("ID,Agent Name,Timestamp,Snippet,Model Used,Total Tokens,Latency (ms)\n")
        
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        
        interactions.forEach { entry ->
            val dateStr = format.format(java.util.Date(entry.timestamp))
            val safeSnippet = entry.snippet.replace("\"", "\"\"")
            val safeModel = entry.modelUsed.replace("\"", "\"\"")
            writer.write("\"${entry.id}\",\"${entry.agentName}\",\"$dateStr\",\"$safeSnippet\",\"$safeModel\",${entry.totalTokens},${entry.latencyMs}\n")
        }
        writer.close()
        
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Export Interactions CSV"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
