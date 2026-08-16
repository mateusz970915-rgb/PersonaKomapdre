package com.example.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.viewmodel.ColonyViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import java.io.File
import java.io.FileOutputStream
import com.example.ui.components.rememberMarker
import com.patrykandpatrick.vico.compose.component.lineComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaCategorySummaryScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val agents by viewModel.agents.collectAsState()
    val subTasks by viewModel.subTasks.collectAsState()
    val decisions by viewModel.decisions.collectAsState()
    
    var selectedRange by remember { mutableStateOf("All Time") }
    val dateRanges = listOf("1W", "1M", "3M", "All Time")
    
    val categoryCounts = remember(agents, subTasks, decisions, selectedRange) {
        val now = System.currentTimeMillis()
        val timeLimit = when (selectedRange) {
            "1W" -> now - 7L * 24 * 60 * 60 * 1000
            "1M" -> now - 30L * 24 * 60 * 60 * 1000
            "3M" -> now - 90L * 24 * 60 * 60 * 1000
            else -> 0L
        }
        
        val agentRoles = agents.associate { it.name to (if (it.role.isNotBlank()) it.role else "Uncategorized") }
        val counts = mutableMapOf<String, Int>()
        
        subTasks.filter { it.timestamp >= timeLimit }.forEach { task ->
            val role = agentRoles[task.assignedAgent] ?: "Unknown"
            counts[role] = (counts[role] ?: 0) + 1
        }
        
        decisions.filter { it.timestamp >= timeLimit }.forEach { decision ->
            val role = agentRoles[decision.agentName] ?: "Unknown"
            counts[role] = (counts[role] ?: 0) + 1
        }
        
        counts.toList().sortedByDescending { it.second }.take(10)
    }

    val chartEntryModel = remember(categoryCounts) {
        if (categoryCounts.isEmpty()) {
            entryModelOf(0 to 0)
        } else {
            val entries = categoryCounts.mapIndexed { index, pair ->
                index to pair.second
            }.toTypedArray()
            entryModelOf(*entries)
        }
    }

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val intValue = value.toInt()
        if (intValue >= 0 && intValue < categoryCounts.size) {
            categoryCounts[intValue].first.take(8) + if (categoryCounts[intValue].first.length > 8) "..." else ""
        } else {
            ""
        }
    }

    val context = LocalContext.current
    val view = LocalView.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Persona Activity") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        try {
                            val bitmap = Bitmap.createBitmap(view.width.coerceAtLeast(100), view.height.coerceAtLeast(100), Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            view.draw(canvas)
                            
                            val file = File(context.cacheDir, "chart_snapshot.png")
                            val fos = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            fos.flush()
                            fos.close()
                            
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Persona Activity Chart")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Chart"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Persona Category Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Activity volume (tasks & decisions) by persona role.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date Range Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dateRanges.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { selectedRange = range },
                        label = { Text(range) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (categoryCounts.isEmpty() || categoryCounts.all { it.second == 0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No activity data available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val marker = rememberMarker()
                Chart(
                    chart = columnChart(
                        columns = listOf(
                            lineComponent(
                                color = primaryColor,
                                thickness = 16.dp
                            )
                        )
                    ),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(
                        valueFormatter = { value, _ -> value.toInt().toString() }
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = bottomAxisValueFormatter
                    ),
                    marker = marker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
        }
    }
}
