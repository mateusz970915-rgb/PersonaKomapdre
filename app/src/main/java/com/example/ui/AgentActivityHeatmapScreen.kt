package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.ColonyViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AgentActivityHeatmapScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val subTasks by viewModel.subTasks.collectAsState(initial = emptyList())
    val decisions by viewModel.decisions.collectAsState(initial = emptyList())
    val missionLogs by viewModel.missionStateLogs.collectAsState(initial = emptyList())
    val memories = viewModel.memories.value

    val context = LocalContext.current
    val themes = mapOf(
        "Ocean" to listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFF3B82F6).copy(alpha = 0.4f),
            Color(0xFF8B5CF6).copy(alpha = 0.7f),
            Color(0xFFF59E0B).copy(alpha = 0.85f),
            Color(0xFF10B981)
        ),
        "Fire" to listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFFFBBF24).copy(alpha = 0.4f),
            Color(0xFFF59E0B).copy(alpha = 0.7f),
            Color(0xFFEA580C).copy(alpha = 0.85f),
            Color(0xFFE11D48)
        ),
        "Forest" to listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFF6EE7B7).copy(alpha = 0.4f),
            Color(0xFF10B981).copy(alpha = 0.7f),
            Color(0xFF059669).copy(alpha = 0.85f),
            Color(0xFF047857)
        ),
        "Sunset" to listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFFF472B6).copy(alpha = 0.4f),
            Color(0xFFEC4899).copy(alpha = 0.7f),
            Color(0xFFD946EF).copy(alpha = 0.85f),
            Color(0xFFA855F7)
        )
    )
    var selectedTheme by remember { mutableStateOf("Ocean") }
    val currentPalette = themes[selectedTheme] ?: themes["Ocean"]!!

    var chartStyle by remember { mutableStateOf("Column") }
    var selectedViewType by remember { mutableStateOf("All Activity") }
    var selectedDateRange by remember { mutableStateOf("All Time") }

    val filteredTimestamps = remember(subTasks, decisions, missionLogs, memories, selectedViewType, selectedDateRange) {
        val allTimestamps = mutableListOf<Long>()

        if (selectedViewType == "All Activity" || selectedViewType == "Subtasks") {
            subTasks.forEach { allTimestamps.add(it.timestamp) }
        }
        if (selectedViewType == "All Activity" || selectedViewType == "Decisions") {
            decisions.forEach { allTimestamps.add(it.timestamp) }
        }
        if (selectedViewType == "All Activity" || selectedViewType == "Logs") {
            missionLogs.forEach { allTimestamps.add(it.timestamp) }
            memories.forEach { allTimestamps.add(it.timestamp) }
        }

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        val cutoff = when (selectedDateRange) {
            "Last 7 Days" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            "Last 30 Days" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.timeInMillis
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.timeInMillis
            }
            else -> 0L
        }

        allTimestamps.filter { it >= cutoff }
    }

    val hourlyActivityCounts = remember(filteredTimestamps) {
        val counts = FloatArray(24) { 0f }
        filteredTimestamps.forEach { ts ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = ts
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            if (hour in 0..23) {
                counts[hour] += 1f
            }
        }
        counts
    }

    val topProductiveDays = remember(filteredTimestamps) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dayCounts = mutableMapOf<String, Int>()
        filteredTimestamps.forEach { ts ->
            val dateString = dateFormat.format(Date(ts))
            dayCounts[dateString] = (dayCounts[dateString] ?: 0) + 1
        }
        dayCounts.entries.sortedByDescending { it.value }.take(3).map { Pair(it.key, it.value) }
    }

    val maxActivity = remember(hourlyActivityCounts) {
        hourlyActivityCounts.maxOrNull() ?: 1f
    }

    val chartEntryModel = remember(hourlyActivityCounts) {
        entryModelOf(hourlyActivityCounts.mapIndexed { index, value -> 
            com.patrykandpatrick.vico.core.entry.FloatEntry(index.toFloat(), value) 
        })
    }

    var selectedHourDetails by remember { mutableStateOf<Pair<Int, Float>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Activity Heatmap") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "My Colony Activity Heatmap")
                            putExtra(Intent.EXTRA_TEXT, "I just checked my Agent Activity Heatmap! Over the selected period, my agents completed many tasks and decisions. Check out my productivity trends!")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Heatmap Stats"))
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
                .horizontalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                                text = "24-Hour Agent Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Execution intensity across times of day",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = chartStyle == "Column",
                        onClick = { chartStyle = "Column" },
                        label = { Text("Bar Chart", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("heatmap_bars_chip")
                    )
                    FilterChip(
                        selected = chartStyle == "Line",
                        onClick = { chartStyle = "Line" },
                        label = { Text("Line Chart", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("heatmap_line_chip")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Theme Palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.keys.forEach { themeName ->
                    FilterChip(
                        selected = selectedTheme == themeName,
                        onClick = { selectedTheme = themeName },
                        label = { Text(themeName) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Date Range
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Time", "Last 7 Days", "Last 30 Days", "This Month").forEach { range ->
                    FilterChip(
                        selected = selectedDateRange == range,
                        onClick = { selectedDateRange = range },
                        label = { Text(range) },
                        modifier = Modifier.testTag("heatmap_daterange_${range.replace(" ", "")}")
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

            Spacer(modifier = Modifier.height(24.dp))

            // Vico Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
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

            Spacer(modifier = Modifier.height(32.dp))

            // Visual 24-Hour Heat Grid Matrix
            Text(
                text = "Hourly Intensity Grid (00:00 - 23:00)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (hour in 0..23) {
                    val count = hourlyActivityCounts[hour]
                    val intensityRatio = if (maxActivity > 0) (count / maxActivity).coerceIn(0f, 1f) else 0f
                    val heatColor = when {
                        count == 0f -> currentPalette[0]
                        intensityRatio < 0.25f -> currentPalette[1]
                        intensityRatio < 0.60f -> currentPalette[2]
                        intensityRatio < 0.85f -> currentPalette[3]
                        else -> currentPalette[4]
                    }

                    Box(
                        modifier = Modifier
                            .size(height = 42.dp, width = 44.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    currentPalette.forEach { color ->
                        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(color))
                    }
                }
                Text("More", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Top Productive Days
            if (topProductiveDays.isNotEmpty()) {
                Text(
                    text = "Top Productive Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topProductiveDays.forEachIndexed { index, (day, count) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(day, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                                Text("$count actions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            selectedHourDetails?.let { (hour, count) ->
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Time slot ${String.format("%02d:00 - %02d:59", hour, hour)}: ${count.toInt()} agent execution events recorded.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
