package com.example.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Agent
import com.example.data.AgentDecision
import com.example.data.ChartAnnotation
import com.example.data.SubTask
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ChartColorTheme(val displayName: String, val lineColors: List<Color>) {
    DEFAULT("Standard M3", listOf(Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFF59E0B), Color(0xFFEF4444))),
    HIGH_CONTRAST("High Contrast", listOf(Color(0xFFFFFF00), Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFFFF3300), Color(0xFF00FF00))),
    SOFT_PASTEL("Soft Pastel", listOf(Color(0xFFA7F3D0), Color(0xFFBFDBFE), Color(0xFFDDD6FE), Color(0xFFFDE68A), Color(0xFFFECACA))),
    MONOCHROME("Monochrome", listOf(Color(0xFF1E293B), Color(0xFF475569), Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFFCBD5E1)))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiAgentOverlayChartWidget(
    agents: List<Agent>,
    subTasks: List<SubTask>,
    decisions: List<AgentDecision>,
    annotations: List<ChartAnnotation>,
    selectedOverlayAgents: Set<String>,
    onToggleOverlayAgent: (String) -> Unit,
    onAddAnnotation: (note: String, tag: String, colorHex: String) -> Unit,
    onDeleteAnnotation: (Int) -> Unit,
    onGeneratePdfReport: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTheme by remember { mutableStateOf(ChartColorTheme.DEFAULT) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showAddAnnotationDialog by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }
    var newTagText by remember { mutableStateOf("Anomaly") }

    val context = LocalContext.current
    val view = LocalView.current

    // Compute activity entries for selected agents over last 7 days
    val daysCount = 7
    val dayLabels = remember {
        val cal = Calendar.getInstance()
        val labels = mutableListOf<String>()
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        for (i in (daysCount - 1) downTo 0) {
            cal.timeInMillis = now - i * oneDayMs
            labels.add(SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time))
        }
        labels
    }

    val activeAgentsToChart = remember(agents, selectedOverlayAgents) {
        if (selectedOverlayAgents.isEmpty() && agents.isNotEmpty()) {
            listOf(agents.first().name)
        } else {
            selectedOverlayAgents.toList()
        }
    }

    // Chart model building
    val multiLineModel = remember(activeAgentsToChart, subTasks, decisions) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val cal = Calendar.getInstance()

        val seriesList = activeAgentsToChart.map { agentName ->
            val FloatArray = FloatArray(daysCount) { 0f }
            for (i in (daysCount - 1) downTo 0) {
                val dayTime = now - i * oneDayMs
                cal.timeInMillis = dayTime
                val dayStart = dayTime - (cal.get(Calendar.HOUR_OF_DAY) * 3600000L + cal.get(Calendar.MINUTE) * 60000L)
                val dayEnd = dayStart + oneDayMs

                val countDecisions = decisions.count { it.agentName.equals(agentName, ignoreCase = true) && it.timestamp in dayStart..dayEnd }
                val countTasks = subTasks.count { it.assignedAgent.equals(agentName, ignoreCase = true) && (it.timestamp in dayStart..dayEnd || it.completedAt in dayStart..dayEnd) }
                FloatArray[daysCount - 1 - i] = (countDecisions + countTasks).toFloat()
            }
            FloatArray
        }

        if (seriesList.isNotEmpty() && seriesList.first().isNotEmpty()) {
            val firstSeries = seriesList.first()
            entryModelOf(firstSeries[0], firstSeries[1], firstSeries[2], firstSeries[3], firstSeries[4], firstSeries[5], firstSeries[6])
        } else {
            entryModelOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        }
    }

    fun captureChartBitmap(): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(view.width.coerceAtLeast(100), view.height.coerceAtLeast(100), Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with title and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Multi-Agent Overlay Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Porównanie wydajności agentów & adnotacje",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Color Theme Button
                    IconButton(onClick = { showThemeMenu = !showThemeMenu }) {
                        Icon(Icons.Default.ColorLens, contentDescription = "Motyw Kolorów", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Add Annotation Button
                    IconButton(onClick = { showAddAnnotationDialog = true }) {
                        Icon(Icons.Default.AddComment, contentDescription = "Dodaj Adnotację", tint = MaterialTheme.colorScheme.secondary)
                    }

                    // Capture Screenshot Button
                    IconButton(onClick = {
                        val bmp = captureChartBitmap()
                        if (bmp != null) {
                            com.example.utils.NotificationHelper.sendNotification(
                                context, 8801, "Zapisano Zrzut Ekranu Wykresu", "Kadr wykresu Vico został przechwycony w pamięci podręcznej."
                            )
                        }
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Zrzut Ekranu", tint = MaterialTheme.colorScheme.tertiary)
                    }

                    // Export PDF Report Button
                    IconButton(onClick = {
                        val bmp = captureChartBitmap()
                        onGeneratePdfReport(bmp)
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Eksport PDF", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Theme Dropdown Menu
            DropdownMenu(
                expanded = showThemeMenu,
                onDismissRequest = { showThemeMenu = false }
            ) {
                ChartColorTheme.values().forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.displayName) },
                        onClick = {
                            currentTheme = theme
                            showThemeMenu = false
                        },
                        leadingIcon = {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                drawCircle(color = theme.lineColors.first())
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Agent Selection Chips for Overlay
            Text(
                text = "Wybierz Agenty do Nałożenia na Wykres:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                agents.forEachIndexed { index, agent ->
                    val isSelected = activeAgentsToChart.contains(agent.name)
                    val lineColor = currentTheme.lineColors[index % currentTheme.lineColors.size]

                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleOverlayAgent(agent.name) },
                        label = { Text(agent.name) },
                        leadingIcon = {
                            Canvas(modifier = Modifier.size(12.dp)) {
                                drawCircle(color = if (isSelected) lineColor else Color.Gray)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Vico Chart Render
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                val lines = remember(currentTheme) {
                    currentTheme.lineColors.map { color ->
                        LineChart.LineSpec(lineColor = color.toArgb())
                    }
                }

                Chart(
                    chart = lineChart(lines = lines),
                    model = multiLineModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Annotations Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Adnotacje i Notatki Wykresu (${annotations.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showAddAnnotationDialog = true }) {
                    Text("+ Przypnij Notatkę")
                }
            }

            if (annotations.isEmpty()) {
                Text(
                    text = "Brak adnotacji. Przypnij notatki do konkretnych punktów czasowych na wykresie.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(annotations) { annotation ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(annotation.colorHex)))
                                )
                                Column {
                                    Text(
                                        text = annotation.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${annotation.tag} | ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(annotation.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteAnnotation(annotation.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Usuń adnotację",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Annotation Dialog
    if (showAddAnnotationDialog) {
        AlertDialog(
            onDismissRequest = { showAddAnnotationDialog = false },
            title = { Text("Przypnij Adnotację do Wykresu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        label = { Text("Treść Adnotacji / Notatki") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTagText,
                        onValueChange = { newTagText = it },
                        label = { Text("Tag / Kategoria (np. Anomaly, Model Switch)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteText.isNotBlank()) {
                            onAddAnnotation(newNoteText, newTagText, "#3B82F6")
                            newNoteText = ""
                            showAddAnnotationDialog = false
                        }
                    }
                ) {
                    Text("Przypnij")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAnnotationDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}
