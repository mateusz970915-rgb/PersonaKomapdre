package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AgentDecision
import com.example.data.ColonyMemory
import com.example.data.MissionStateLog
import com.example.data.SubTask
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgentActivityHeatmapWidget(
    subTasks: List<SubTask> = emptyList(),
    decisions: List<AgentDecision> = emptyList(),
    missionLogs: List<MissionStateLog> = emptyList(),
    memories: List<ColonyMemory> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedViewType by remember { mutableStateOf("All Activity") } // "All Activity", "Subtasks", "Decisions", "Logs"
    var chartStyle by remember { mutableStateOf("Column") } // "Column", "Line"
    var selectedHourDetails by remember { mutableStateOf<Pair<Int, Float>?>(null) }

    // 24-hour activity computation across 00:00 to 23:00
    val hourlyActivityCounts = remember(subTasks, decisions, missionLogs, memories, selectedViewType) {
        val counts = FloatArray(24) { 0f }
        val cal = Calendar.getInstance()

        if (selectedViewType == "All Activity" || selectedViewType == "Subtasks") {
            subTasks.forEach { st ->
                val time = if (st.completedAt > 0) st.completedAt else st.timestamp
                if (time > 0) {
                    cal.timeInMillis = time
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    if (hour in 0..23) counts[hour] += 1f
                }
            }
        }

        if (selectedViewType == "All Activity" || selectedViewType == "Decisions") {
            decisions.forEach { d ->
                if (d.timestamp > 0) {
                    cal.timeInMillis = d.timestamp
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    if (hour in 0..23) counts[hour] += 1f
                }
            }
        }

        if (selectedViewType == "All Activity" || selectedViewType == "Logs") {
            missionLogs.forEach { log ->
                if (log.timestamp > 0) {
                    cal.timeInMillis = log.timestamp
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    if (hour in 0..23) counts[hour] += 1f
                }
            }
        }

        counts
    }

    val maxActivity = remember(hourlyActivityCounts) {
        hourlyActivityCounts.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    }

    val chartEntryModel = remember(hourlyActivityCounts) {
        entryModelOf(
            hourlyActivityCounts[0], hourlyActivityCounts[1], hourlyActivityCounts[2], hourlyActivityCounts[3],
            hourlyActivityCounts[4], hourlyActivityCounts[5], hourlyActivityCounts[6], hourlyActivityCounts[7],
            hourlyActivityCounts[8], hourlyActivityCounts[9], hourlyActivityCounts[10], hourlyActivityCounts[11],
            hourlyActivityCounts[12], hourlyActivityCounts[13], hourlyActivityCounts[14], hourlyActivityCounts[15],
            hourlyActivityCounts[16], hourlyActivityCounts[17], hourlyActivityCounts[18], hourlyActivityCounts[19],
            hourlyActivityCounts[20], hourlyActivityCounts[21], hourlyActivityCounts[22], hourlyActivityCounts[23]
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_activity_heatmap_widget")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Activity Heatmap",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "24-Hour Agent Activity Heatmap",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vico Chart • Colony execution intensity across times of day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = chartStyle == "Column",
                        onClick = { chartStyle = "Column" },
                        label = { Text("Bars", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("heatmap_bars_chip")
                    )
                    FilterChip(
                        selected = chartStyle == "Line",
                        onClick = { chartStyle = "Line" },
                        label = { Text("Line", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("heatmap_line_chip")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Types
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Activity", "Subtasks", "Decisions", "Logs").forEach { view ->
                    val isSelected = selectedViewType == view
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedViewType = view },
                        label = { Text(view) },
                        modifier = Modifier.testTag("heatmap_filter_$view")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vico Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("vico_heatmap_chart")
            ) {
                if (chartStyle == "Column") {
                    Chart(
                        chart = columnChart(),
                        model = chartEntryModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Chart(
                        chart = lineChart(),
                        model = chartEntryModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual 24-Hour Heat Grid Matrix
            Text(
                text = "Hourly Intensity Grid (00:00 - 23:00)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (hour in 0..23) {
                    val count = hourlyActivityCounts[hour]
                    val intensityRatio = if (maxActivity > 0) (count / maxActivity).coerceIn(0f, 1f) else 0f

                    val heatColor = when {
                        count == 0f -> MaterialTheme.colorScheme.surfaceVariant
                        intensityRatio < 0.25f -> Color(0xFF3B82F6).copy(alpha = 0.4f)
                        intensityRatio < 0.60f -> Color(0xFF8B5CF6).copy(alpha = 0.7f)
                        intensityRatio < 0.85f -> Color(0xFFF59E0B).copy(alpha = 0.85f)
                        else -> Color(0xFF10B981)
                    }

                    Box(
                        modifier = Modifier
                            .size(height = 36.dp, width = 38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(heatColor)
                            .border(
                                width = if (selectedHourDetails?.first == hour) 2.dp else 0.5.dp,
                                color = if (selectedHourDetails?.first == hour) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedHourDetails = Pair(hour, count)
                            }
                            .testTag("heat_grid_cell_$hour"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d:00", hour),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (count > 0f) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (count > 0f) {
                                Text(
                                    text = count.toInt().toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            selectedHourDetails?.let { (hour, count) ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Time slot ${String.format("%02d:00 - %02d:59", hour, hour)}: ${count.toInt()} agent execution events recorded.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
