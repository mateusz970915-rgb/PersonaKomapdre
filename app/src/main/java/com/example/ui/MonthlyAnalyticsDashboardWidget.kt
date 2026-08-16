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
import com.example.data.MissionStateLog
import com.example.data.SubTask
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MonthlyAnalyticsDashboardWidget(
    subTasksList: List<SubTask> = emptyList(),
    modifier: Modifier = Modifier
) {
    val (chartModelProducer, labels) = remember(subTasksList) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val rates = FloatArray(30) { 0f }
        val cal = Calendar.getInstance()
        val dayLabels = mutableMapOf<Int, String>()
        val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())

        for (i in 29 downTo 0) {
            val dayTime = now - i * oneDayMs
            cal.timeInMillis = dayTime
            dayLabels[29 - i] = sdf.format(Date(dayTime))
            
            val dayStart = dayTime - (cal.get(Calendar.HOUR_OF_DAY) * 3600000L + cal.get(Calendar.MINUTE) * 60000L)
            val dayEnd = dayStart + oneDayMs
            val dayTasks = subTasksList.filter { it.timestamp in dayStart..dayEnd || it.completedAt in dayStart..dayEnd }
            val completed = dayTasks.count { it.status.equals("Completed", ignoreCase = true) || it.completedAt > 0L }
            if (dayTasks.isNotEmpty()) {
                rates[29 - i] = completed.toFloat()
            }
        }
        val entries = rates.mapIndexed { index, value -> FloatEntry(x = index.toFloat(), y = value) }
        Pair(ChartEntryModelProducer(entries), dayLabels)
    }

    val bottomAxisFormatter = remember(labels) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val index = value.toInt()
            if (index % 5 == 0 && index in labels.keys) {
                labels[index] ?: ""
            } else {
                ""
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "30-Day Task Completion Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Real-time analytics over the last 30 days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Chart(
                    chart = lineChart(),
                    chartModelProducer = chartModelProducer,
                    startAxis = rememberStartAxis(valueFormatter = { value, _ -> value.toInt().toString() }),
                    bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
