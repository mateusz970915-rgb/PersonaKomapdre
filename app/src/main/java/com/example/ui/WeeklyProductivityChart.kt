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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

import com.example.data.SubTask
import com.example.ui.components.ChartContainerWithEmptyState
import com.example.ui.components.isZeroOrEmpty
import java.util.Calendar

@Composable
fun WeeklyProductivityChart(
    completedSubTasksCount: Int = 0,
    decisionsCount: Int = 0,
    missionsCount: Int = 0,
    completedTasksList: List<SubTask> = emptyList(),
    selectedChartType: String? = null,
    onChartTypeSelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val initialType = if (selectedChartType == "Bar" || selectedChartType == "Column") "Column" else if (selectedChartType == "Line") "Line" else "Column"
    var chartType by remember(selectedChartType) { mutableStateOf(initialType) }

    // Compute exact daily completions from database timestamps if available
    val dailyCounts = remember(completedTasksList, completedSubTasksCount, decisionsCount, missionsCount) {
        val counts = FloatArray(7) { 0f }
        // Real mode: compute ONLY from actual completedTasksList completedAt within 7-day window
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000L
        completedTasksList.filter { it.completedAt >= sevenDaysAgo }.forEach { task ->
            cal.timeInMillis = task.completedAt
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2...
            val dayIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
            if (dayIndex in 0..6) {
                counts[dayIndex] += 1f
            }
        }
        counts
    }

    val chartModel = entryModelOf(
        dailyCounts[0],
        dailyCounts[1],
        dailyCounts[2],
        dailyCounts[3],
        dailyCounts[4],
        dailyCounts[5],
        dailyCounts[6]
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_productivity_chart_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Colony Productivity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Vico Chart • Daily Task & Output Velocity",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = chartType == "Column",
                        onClick = {
                            chartType = "Column"
                            onChartTypeSelected?.invoke("Bar")
                        },
                        label = { Text("Bars", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chart_type_bars")
                    )
                    FilterChip(
                        selected = chartType == "Line",
                        onClick = {
                            chartType = "Line"
                            onChartTypeSelected?.invoke("Line")
                        },
                        label = { Text("Line", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chart_type_line")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vico Chart Rendering
            ChartContainerWithEmptyState(
                hasData = !chartModel.isZeroOrEmpty(),
                emptyTitle = "No Productivity Data",
                emptyMessage = "No task completions recorded for this week.",
                emptyStateHeight = 200.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
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
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                days.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Total Output: ${dailyCounts.sum().toInt()} units",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                ) {
                    val maxVal = dailyCounts.maxOrNull()?.toInt() ?: 0
                    Text(
                        text = "Peak Output: $maxVal tasks",
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
