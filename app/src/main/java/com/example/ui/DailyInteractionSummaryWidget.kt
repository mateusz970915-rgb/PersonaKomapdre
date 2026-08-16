package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AgentDecision
import com.example.data.MissionStateLog
import com.example.data.SubTask
import java.util.Calendar

@Composable
fun DailyInteractionSummaryWidget(
    decisions: List<AgentDecision>,
    subTasks: List<SubTask>,
    missionLogs: List<MissionStateLog>,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfDay = cal.timeInMillis

    val todaysDecisions = decisions.filter { it.timestamp >= startOfDay }
    val todaysTasks = subTasks.filter { it.timestamp >= startOfDay }
    val todaysLogs = missionLogs.filter { it.timestamp >= startOfDay }
    
    val totalCount = todaysDecisions.size + todaysTasks.size + todaysLogs.size

    val hourlyCounts = IntArray(24) { 0 }
    
    fun processItems(items: List<Long>) {
        items.forEach { timestamp ->
            if (timestamp >= startOfDay) {
                cal.timeInMillis = timestamp
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                if (hour in 0..23) {
                    hourlyCounts[hour]++
                }
            }
        }
    }
    
    processItems(todaysDecisions.map { it.timestamp })
    processItems(todaysTasks.map { it.timestamp })
    processItems(todaysLogs.map { it.timestamp })

    val maxCount = hourlyCounts.maxOrNull()?.toFloat() ?: 1f
    val maxActivity = if (maxCount > 0) maxCount else 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Interactions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (hour in 0..23 step 2) {
                    val count1 = hourlyCounts[hour]
                    val count2 = if (hour + 1 < 24) hourlyCounts[hour + 1] else 0
                    val totalForTwoHours = count1 + count2
                    val intensityRatio = (totalForTwoHours / (maxActivity * 2)).coerceIn(0f, 1f)
                    
                    val heatColor = when {
                        totalForTwoHours == 0 -> MaterialTheme.colorScheme.surfaceVariant
                        intensityRatio < 0.25f -> Color(0xFF3B82F6).copy(alpha = 0.4f)
                        intensityRatio < 0.60f -> Color(0xFF8B5CF6).copy(alpha = 0.7f)
                        intensityRatio < 0.85f -> Color(0xFFF59E0B).copy(alpha = 0.85f)
                        else -> Color(0xFF10B981)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .padding(horizontal = 1.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(heatColor)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("12 AM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp))
                Text("12 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp))
                Text("11 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp))
            }
        }
    }
}
