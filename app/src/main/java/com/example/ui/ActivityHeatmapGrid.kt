package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentDecision
import com.example.data.SubTask
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Heatmapa Aktywności (Contribution Grid)
 * GitHub-style activity grid visualizing agent work intensity over time.
 */
@Composable
fun ActivityHeatmapGrid(
    subTasks: List<SubTask>,
    decisions: List<AgentDecision>,
    modifier: Modifier = Modifier
) {
    // Generate 16 weeks (112 days) of activity
    val totalWeeks = 16
    val daysPerWeek = 7

    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L

    var selectedDayInfo by remember { mutableStateOf<Pair<String, Int>?>(null) }

    // Map dayIndex -> count
    val dayCounts = remember(subTasks, decisions) {
        val counts = IntArray(totalWeeks * daysPerWeek) { 0 }
        val cal = Calendar.getInstance()

        val totalDays = totalWeeks * daysPerWeek
        for (i in (totalDays - 1) downTo 0) {
            val dayTime = now - i * oneDayMs
            cal.timeInMillis = dayTime
            val dayStart = dayTime - (cal.get(Calendar.HOUR_OF_DAY) * 3600000L + cal.get(Calendar.MINUTE) * 60000L)
            val dayEnd = dayStart + oneDayMs

            val countDecisions = decisions.count { it.timestamp in dayStart..dayEnd }
            val countTasks = subTasks.count { it.timestamp in dayStart..dayEnd || it.completedAt in dayStart..dayEnd }

            counts[totalDays - 1 - i] = countDecisions + countTasks
        }
        counts
    }

    val maxCount = remember(dayCounts) {
        dayCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    }

    fun getColorForCount(count: Int): Color {
        if (count == 0) return Color(0xFF1E293B) // Dark background
        val ratio = count.toFloat() / maxCount.toFloat()
        return when {
            ratio > 0.75f -> Color(0xFF10B981) // High intensity Emerald
            ratio > 0.50f -> Color(0xFF34D399) // Medium-High
            ratio > 0.25f -> Color(0xFF6EE7B7) // Medium
            else -> Color(0xFFA7F3D0) // Low
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Heatmapa Aktywności (Contribution Grid)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Intensywność pracy agentów w czasie (16 tygodni)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${dayCounts.sum()} zdarzeń",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Heatmap Grid Render
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(totalWeeks) { weekIndex ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (dayInWeek in 0 until daysPerWeek) {
                            val dayIndex = weekIndex * daysPerWeek + dayInWeek
                            val count = if (dayIndex < dayCounts.size) dayCounts[dayIndex] else 0

                            val dayTime = now - ((totalWeeks * daysPerWeek - 1 - dayIndex) * oneDayMs)
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dayTime))

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(getColorForCount(count))
                                    .clickable {
                                        selectedDayInfo = Pair(dateStr, count)
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend & Day Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedDayInfo != null) {
                    Text(
                        text = "${selectedDayInfo!!.first}: ${selectedDayInfo!!.second} aktywności",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "Kliknij kwadrat, aby zobaczyć szczegóły",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Legend bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Mniej", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1E293B)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFA7F3D0)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF34D399)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF10B981)))
                    Text(text = "Więcej", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
        }
    }
}
