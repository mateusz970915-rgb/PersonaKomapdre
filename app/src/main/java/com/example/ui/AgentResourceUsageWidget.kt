package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.example.utils.ChartDataExportUtils
import kotlin.random.Random

@Composable
fun AgentResourceUsageWidget() {
    val context = LocalContext.current
    
    val memoryRawData = remember {
        (0..10).map { index -> Pair(index.toFloat(), Random.nextFloat() * 50 + 20) }
    }
    val memoryUsageModel = remember(memoryRawData) {
        entryModelOf(*memoryRawData.map { it.second }.toTypedArray())
    }
    
    val batteryRawData = remember {
        (0..10).map { index -> Pair(index.toFloat(), Random.nextFloat() * 20 + 5) }
    }
    val batteryUsageModel = remember(batteryRawData) {
        entryModelOf(*batteryRawData.map { it.second }.toTypedArray())
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Zużycie Zasobów Agentów",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pamięć oraz Bateria w czasie",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = {
                    val combinedData = memoryRawData.mapIndexed { index, memPair ->
                        val batPair = batteryRawData[index]
                        // Simple export combining both (or just memory as example)
                        Pair(memPair.first, memPair.second)
                    }
                    ChartDataExportUtils.exportToCsv(context, combinedData, "Resource_Usage")
                }) {
                    Icon(Icons.Default.Download, contentDescription = "Eksportuj do CSV")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pamięć RAM (MB)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Chart(
                chart = lineChart(),
                model = memoryUsageModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.height(120.dp).fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Zużycie Baterii (W)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Chart(
                chart = lineChart(),
                model = batteryUsageModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.height(120.dp).fillMaxWidth()
            )
        }
    }
}
