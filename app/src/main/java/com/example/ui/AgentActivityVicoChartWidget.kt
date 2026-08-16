package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Agent
import com.example.data.AgentDecision
import com.example.data.SubTask
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.example.ui.components.ChartContainerWithEmptyState
import com.example.ui.components.isZeroOrEmpty
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AgentActivityVicoChartWidget(
    agents: List<Agent>,
    subTasks: List<SubTask>,
    decisions: List<AgentDecision>,
    modifier: Modifier = Modifier
) {
    if (agents.isEmpty()) return

    var selectedAgentId by remember { mutableStateOf(agents.first().id) }
    val selectedAgent = agents.find { it.id == selectedAgentId } ?: agents.first()

    // 7-day data for selected agent
    val (activityCounts, completionRates, dayLabels) = remember(selectedAgentId, subTasks, decisions) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val cal = Calendar.getInstance()

        val activities = FloatArray(7) { 0f }
        val rates = FloatArray(7) { 0f }
        val labels = mutableListOf<String>()

        for (i in 6 downTo 0) {
            val dayTime = now - i * oneDayMs
            cal.timeInMillis = dayTime
            labels.add(SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time))

            val dayStart = dayTime - (cal.get(Calendar.HOUR_OF_DAY) * 3600000L + cal.get(Calendar.MINUTE) * 60000L)
            val dayEnd = dayStart + oneDayMs

            // Activity: Decisions + Tasks
            val agentDecisions = decisions.count { 
                it.agentName.equals(selectedAgent.name, ignoreCase = true) && it.timestamp in dayStart..dayEnd 
            }
            val agentTasks = subTasks.filter { 
                it.assignedAgent.equals(selectedAgent.name, ignoreCase = true) && 
                (it.timestamp in dayStart..dayEnd || it.completedAt in dayStart..dayEnd)
            }
            activities[6 - i] = (agentDecisions + agentTasks.size).toFloat()

            // Completion Rate
            val completed = agentTasks.count { it.status.equals("Completed", ignoreCase = true) || it.completedAt > 0L }
            rates[6 - i] = if (agentTasks.isNotEmpty()) (completed.toFloat() / agentTasks.size) * 100f else 0f
        }
        Triple(activities, rates, labels)
    }

    val activityModel = remember(activityCounts) {
        entryModelOf(activityCounts[0], activityCounts[1], activityCounts[2], activityCounts[3], activityCounts[4], activityCounts[5], activityCounts[6])
    }
    
    val rateModel = remember(completionRates) {
        entryModelOf(completionRates[0], completionRates[1], completionRates[2], completionRates[3], completionRates[4], completionRates[5], completionRates[6])
    }

    var showActivity by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Agent Performance (Vico Chart)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Agent selector
            ScrollableTabRow(
                selectedTabIndex = agents.indexOfFirst { it.id == selectedAgentId }.coerceAtLeast(0),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                divider = {}
            ) {
                agents.forEach { agent ->
                    Tab(
                        selected = selectedAgentId == agent.id,
                        onClick = { selectedAgentId = agent.id },
                        text = { Text(agent.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = showActivity,
                    onClick = { showActivity = true },
                    label = { Text("Activity Level") }
                )
                FilterChip(
                    selected = !showActivity,
                    onClick = { showActivity = false },
                    label = { Text("Completion Rate %") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            val activeModel = if (showActivity) activityModel else rateModel
            ChartContainerWithEmptyState(
                hasData = !activeModel.isZeroOrEmpty(),
                emptyTitle = if (showActivity) "No Activity Recorded" else "No Completion Rate Data",
                emptyMessage = "No recent data found for the selected agent.",
                emptyStateHeight = 180.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    Chart(
                        chart = lineChart(),
                        model = activeModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEach {
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
