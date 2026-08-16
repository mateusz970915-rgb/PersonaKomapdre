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
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TeamPerformanceVicoChartWidget(
    agents: List<Agent>,
    subTasks: List<SubTask>,
    decisions: List<AgentDecision>,
    modifier: Modifier = Modifier
) {
    if (agents.isEmpty()) return

    val (activityCounts, dayLabels) = remember(subTasks, decisions) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val cal = Calendar.getInstance()
        val activities = FloatArray(30) { 0f }
        val labels = mutableListOf<String>()

        for (i in 29 downTo 0) {
            val dayTime = now - i * oneDayMs
            cal.timeInMillis = dayTime
            // only label every 5th day to avoid crowding
            if (i % 5 == 0 || i == 29 || i == 0) {
                labels.add(SimpleDateFormat("MM/dd", Locale.getDefault()).format(cal.time))
            } else {
                labels.add("")
            }

            val dayStart = dayTime - (cal.get(Calendar.HOUR_OF_DAY) * 3600000L + cal.get(Calendar.MINUTE) * 60000L)
            val dayEnd = dayStart + oneDayMs

            // Aggregate Activity: Decisions + Tasks
            val agentDecisions = decisions.count { 
                 it.timestamp in dayStart..dayEnd 
             }
            val agentTasks = subTasks.filter { 
                 (it.timestamp in dayStart..dayEnd || it.completedAt in dayStart..dayEnd)
            }
            activities[29 - i] = (agentDecisions + agentTasks.size).toFloat()
        }
        Pair(activities, labels)
    }

    val activityModel = remember(activityCounts) {
        entryModelOf(*(activityCounts.map { it as Number }.toTypedArray()))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Team Performance (Last 30 Days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Chart(
                    chart = lineChart(),
                    model = activityModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
