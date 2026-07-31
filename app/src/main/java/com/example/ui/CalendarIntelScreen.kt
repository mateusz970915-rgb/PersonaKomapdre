package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarEvent
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarIntelScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val events by viewModel.calendarEvents.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var manualTitle by remember { mutableStateOf("") }
    var manualDesc by remember { mutableStateOf("") }
    var manualStartTimeStr by remember { mutableStateOf("") } // HH:mm
    var manualDurationHours by remember { mutableStateOf("1") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    // Conflict detection logic
    val conflicts = remember(events) {
        val list = mutableListOf<Pair<CalendarEvent, CalendarEvent>>()
        for (i in events.indices) {
            for (j in (i + 1) until events.size) {
                val e1 = events[i]
                val e2 = events[j]
                // Overlap exists if start1 < end2 and start2 < end1
                if (e1.startTime < e2.endTime && e2.startTime < e1.endTime) {
                    list.add(e1 to e2)
                }
            }
        }
        list
    }

    // Deep work block finder logic:
    // Suggest gaps in a standard day (e.g., 9:00 - 18:00) of 2-hour duration
    val deepWorkSuggestions = remember(events) {
        val suggestions = mutableListOf<Pair<Long, Long>>()
        val calendar = Calendar.getInstance()
        // Today at 9 AM
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val startOfWorkDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 18)
        val endOfWorkDay = calendar.timeInMillis
        
        // Loop by 1-hour increments to find any 2-hour free block
        var checkStart = startOfWorkDay
        while (checkStart + 2 * 3600 * 1000 <= endOfWorkDay && suggestions.size < 3) {
            val checkEnd = checkStart + 2 * 3600 * 1000
            
            // Check if this block overlaps with any existing calendar event
            val overlaps = events.any { e ->
                checkStart < e.endTime && e.startTime < checkEnd
            }
            
            if (!overlaps) {
                suggestions.add(checkStart to checkEnd)
            }
            checkStart += 3600 * 1000 // increment by 1 hour
        }
        suggestions
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar Intelligence Engine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.clearCalendarEvents()
                        },
                        modifier = Modifier.testTag("clear_calendar_btn")
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear All Events")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_calendar_event_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj wydarzenie", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                "AI Calendar Diagnostics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Liczba wydarzeń dzisiaj: ${events.size}\nWykryte konflikty terminów: ${conflicts.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Conflict Warning Section
            if (conflicts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Conflict", tint = MaterialTheme.colorScheme.onErrorContainer)
                                Text(
                                    "Konflikt terminów! (${conflicts.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            
                            conflicts.forEach { (e1, e2) ->
                                Text(
                                    text = "⚠️ \"${e1.title}\" nakłada się na \"${e2.title}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // AI Deep Work Suggestions Block
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Sugerowane bloki Deep Work",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Calendar Intelligence przeanalizował Twój dzień i znalazł optymalne okna wolne od innych spotkań na skupienie i pracę głęboką:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (deepWorkSuggestions.isEmpty()) {
                            Text(
                                "Brak wolnych, nieprzerwanych bloków 2-godzinnych w godzinach pracy (9:00 - 18:00).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            deepWorkSuggestions.forEach { (start, end) ->
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val formattedBlock = "${timeFormat.format(Date(start))} - ${timeFormat.format(Date(end))}"
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable {
                                            // Prefill the dialog
                                            manualTitle = "Deep Work Focus Session"
                                            manualDesc = "Nieprzerwany czas dedykowany na pracę głęboką i programowanie."
                                            val c = Calendar.getInstance()
                                            c.timeInMillis = start
                                            manualStartTimeStr = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
                                            manualDurationHours = "2"
                                            showAddDialog = true
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Wolny Blok: $formattedBlock",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "Kliknij, aby zarezerwować na Deep Work",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Book",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // List of Events
            item {
                Text(
                    text = "Dzisiejszy Plan Dnia (${events.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (events.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Brak zarejestrowanych spotkań na dzisiaj.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(events) { ev ->
                    val tf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val timeSpan = "${tf.format(Date(ev.startTime))} - ${tf.format(Date(ev.endTime))}"
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (ev.description.isNotEmpty()) {
                                    Text(
                                        text = ev.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = ev.agentName,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Text(
                                        text = timeSpan,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteCalendarEventById(ev.id)
                                },
                                modifier = Modifier.testTag("delete_event_${ev.id}")
                            ) {
                                Icon(Icons.Default.ClearAll, contentDescription = "Usuń wydarzenie")
                            }
                        }
                    }
                }
            }
        }

        // Add Event Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Dodaj wydarzenie do kalendarza") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = manualTitle,
                            onValueChange = { manualTitle = it },
                            label = { Text("Tytuł spotkania / sesji") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = manualDesc,
                            onValueChange = { manualDesc = it },
                            label = { Text("Krótki opis") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = manualStartTimeStr,
                            onValueChange = { manualStartTimeStr = it },
                            label = { Text("Godzina startu (HH:mm, np. 11:30)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = manualDurationHours,
                            onValueChange = { manualDurationHours = it },
                            label = { Text("Czas trwania (godziny)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualTitle.isNotBlank() && manualStartTimeStr.contains(":")) {
                                try {
                                    val parts = manualStartTimeStr.split(":")
                                    val hr = parts[0].trim().toInt()
                                    val min = parts[1].trim().toInt()
                                    
                                    val calendar = Calendar.getInstance()
                                    calendar.set(Calendar.HOUR_OF_DAY, hr)
                                    calendar.set(Calendar.MINUTE, min)
                                    calendar.set(Calendar.SECOND, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)
                                    
                                    val startMs = calendar.timeInMillis
                                    val durationMs = (manualDurationHours.toDoubleOrNull() ?: 1.0) * 3600 * 1000
                                    val endMs = startMs + durationMs.toLong()
                                    
                                    viewModel.insertCalendarEvent(
                                        CalendarEvent(
                                            title = manualTitle,
                                            description = manualDesc,
                                            startTime = startMs,
                                            endTime = endMs,
                                            agentName = "Calendar Agent"
                                        )
                                    )
                                    
                                    // Reset inputs
                                    manualTitle = ""
                                    manualDesc = ""
                                    manualStartTimeStr = ""
                                    showAddDialog = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier.testTag("confirm_add_event_btn")
                    ) {
                        Text("Zarezerwuj")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}
