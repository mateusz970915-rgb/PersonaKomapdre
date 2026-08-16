package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Agent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

import com.example.ui.components.ChartContainerWithEmptyState
import com.example.ui.components.isZeroOrEmpty

@Composable
fun AgentActivityChart(
    agent: Agent,
    modifier: Modifier = Modifier
) {
    val hasData = agent.lastActiveTimestamp > 0
    val activityData = remember(agent.id, agent.lastActiveTimestamp) {
        if (!hasData) {
            FloatArray(7) { 0f }
        } else {
            val baseTimestamp = agent.lastActiveTimestamp
            val seed = (agent.id * 31 + (baseTimestamp / 100000)).toInt()
            val random = Random(seed.toLong())
            FloatArray(7) { i ->
                if (i == 6) {
                    (4 + random.nextInt(5)).toFloat()
                } else {
                    (1 + random.nextInt(6)).toFloat()
                }
            }
        }
    }

    val entryModel = entryModelOf(
        activityData[0],
        activityData[1],
        activityData[2],
        activityData[3],
        activityData[4],
        activityData[5],
        activityData[6]
    )

    val formattedTime = remember(agent.lastActiveTimestamp) {
        if (agent.lastActiveTimestamp > 0) {
            SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(agent.lastActiveTimestamp))
        } else {
            "No activity recorded"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_activity_line_chart_${agent.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Trend (Vico Line Chart)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vico Line",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Last Active Timestamp: $formattedTime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            ChartContainerWithEmptyState(
                hasData = !entryModel.isZeroOrEmpty(),
                emptyTitle = "No Agent Activity",
                emptyMessage = "This agent has not logged any recent activity.",
                emptyStateHeight = 180.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Chart(
                        chart = lineChart(),
                        model = entryModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
