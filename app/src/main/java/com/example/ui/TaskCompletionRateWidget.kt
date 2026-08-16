package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.MissionStateLog
import com.example.data.SubTask
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.example.ui.components.ChartContainerWithEmptyState
import com.example.ui.components.ChartTrendSummaryView
import com.example.ui.components.isZeroOrEmpty
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TaskCompletionRateWidget(
    subTasksList: List<SubTask> = emptyList(),
    missionLogs: List<MissionStateLog> = emptyList(),
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf("Column") } // "Column" or "Line"

    // Calculate daily completion rate (%) over the last 7 days
    val (dailyRates, dayLabels, totalCompleted, avgRate) = remember(subTasksList, missionLogs) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val sevenDaysAgo = now - 7 * oneDayMs

        val rates = FloatArray(7) { 100f }
        val labels = mutableListOf<String>()
        val cal = Calendar.getInstance()

        // Generate day labels (Mon..Sun) for the last 7 days ending today
        for (i in 6 downTo 0) {
            val dayTime = now - i * oneDayMs
            cal.timeInMillis = dayTime
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
            labels.add(dayName)

            // Filter subtasks created or updated on that day
            val dayStart = dayTime - (cal.get(Calendar.HOUR_OF_DAY) * 3600000L + cal.get(Calendar.MINUTE) * 60000L)
            val dayEnd = dayStart + oneDayMs

            val dayTasks = subTasksList.filter { it.timestamp in dayStart..dayEnd || it.completedAt in dayStart..dayEnd }
            val completed = dayTasks.count { it.status.equals("Completed", ignoreCase = true) || it.completedAt > 0L }

            if (dayTasks.isNotEmpty()) {
                rates[6 - i] = (completed.toFloat() / dayTasks.size) * 100f
            } else {
                // Check if mission logs exist for this day
                val logsForDay = missionLogs.filter { it.timestamp in dayStart..dayEnd }
                if (logsForDay.isNotEmpty()) {
                    val completedLogs = logsForDay.count { it.newState.equals("Completed", ignoreCase = true) }
                    rates[6 - i] = (completedLogs.toFloat() / logsForDay.size) * 100f
                } else {
                    // Default baseline when no tasks scheduled
                    rates[6 - i] = 100f
                }
            }
        }

        val allCompletedCount = subTasksList.count { it.status.equals("Completed", ignoreCase = true) || it.completedAt > 0L }
        val calculatedAvg = if (rates.isNotEmpty()) rates.average().toFloat() else 100f

        Quadruple(rates, labels, allCompletedCount, calculatedAvg)
    }

    val (currentPeriodCompleted, previousPeriodCompleted) = remember(subTasksList) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val currentPeriodStart = now - (7 * oneDayMs)
        val previousPeriodStart = now - (14 * oneDayMs)

        val cur = subTasksList.count { (it.timestamp in currentPeriodStart..now || it.completedAt in currentPeriodStart..now) && (it.status.equals("Completed", ignoreCase = true) || it.completedAt > 0L) }
        val prev = subTasksList.count { (it.timestamp in previousPeriodStart until currentPeriodStart || it.completedAt in previousPeriodStart until currentPeriodStart) && (it.status.equals("Completed", ignoreCase = true) || it.completedAt > 0L) }
        cur to prev
    }

    val chartModel = entryModelOf(
        dailyRates[0],
        dailyRates[1],
        dailyRates[2],
        dailyRates[3],
        dailyRates[4],
        dailyRates[5],
        dailyRates[6]
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_completion_rate_widget_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "7-Day Task Completion Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Vico Chart • Agent Reliability & Execution Velocity",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = chartType == "Column",
                        onClick = { chartType = "Column" },
                        label = { Text("Bars", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("completion_rate_bars_btn")
                    )
                    FilterChip(
                        selected = chartType == "Line",
                        onClick = { chartType = "Line" },
                        label = { Text("Line", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("completion_rate_line_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ChartTrendSummaryView(
                currentTotal = currentPeriodCompleted,
                previousTotal = previousPeriodCompleted,
                unitLabel = "completed tasks",
                periodDays = 7,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Vico Chart Rendering
            ChartContainerWithEmptyState(
                hasData = !chartModel.isZeroOrEmpty(),
                emptyTitle = "No Task Completion Data",
                emptyMessage = "No tasks were marked complete during the last 7 days.",
                emptyStateHeight = 180.dp
            ) {
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "7D Avg Rate: ${avgRate.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "Tasks Done: $totalCompleted",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
