package com.example.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SleepRecord
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepRecoveryOptimizerScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sleepRecords by viewModel.sleepRecords.collectAsStateWithLifecycle()
    
    // Form Inputs for custom log (simulating importing/retrieving Fit/Health Connect data)
    var sleepHoursInput by remember { mutableStateOf("7.5") }
    var deepSleepInput by remember { mutableStateOf("120") }
    var remSleepInput by remember { mutableStateOf("90") }
    var lightSleepInput by remember { mutableStateOf("240") }
    var heartRateInput by remember { mutableStateOf("58") }
    
    // Live Sensor Telemetry (Accelerometer) to detect rest/movement
    var sensorValue by remember { mutableFloatStateOf(0f) }
    var restState by remember { mutableStateOf("Niewykryty") }
    
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = kotlin.math.sqrt(x*x + y*y + z*z)
                    sensorValue = magnitude
                    
                    // Standard gravity is ~9.81 m/s^2. If close to 9.81, the phone is fully stationary (resting)
                    val delta = kotlin.math.abs(magnitude - 9.81f)
                    restState = if (delta < 0.15f) "Głęboki odpoczynek (Telefon nieruchomy)" else "Ruch / Aktywność"
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep & Recovery Optimizer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("sleep_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Powrót")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Left Panel: Logs and Analyzer
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                // Live Sensor Analytics Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("live_sensor_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Telemetria Ruchu (Akcelerometr)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sygnał przeciążenia: ${"%.4f".format(sensorValue)} m/s²", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text("Stan restowy: $restState", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                // Form Card: Import or Enter Sleep Log
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "Dodaj Rejestr Snu (Google Fit / Ręczny)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = sleepHoursInput,
                                onValueChange = { sleepHoursInput = it },
                                label = { Text("Długość snu (Godziny)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sleep_hours_input"),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Hotel, contentDescription = null) }
                            )
                        }
                        
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = deepSleepInput,
                                    onValueChange = { deepSleepInput = it },
                                    label = { Text("Głęboki (Min)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("deep_sleep_input"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = remSleepInput,
                                    onValueChange = { remSleepInput = it },
                                    label = { Text("REM (Min)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("rem_sleep_input"),
                                    singleLine = true
                                )
                            }
                        }
                        
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = lightSleepInput,
                                    onValueChange = { lightSleepInput = it },
                                    label = { Text("Płytki (Min)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("light_sleep_input"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = heartRateInput,
                                    onValueChange = { heartRateInput = it },
                                    label = { Text("Tętno spoczynkowe") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("heart_rate_input"),
                                    singleLine = true
                                )
                            }
                        }
                        
                        item {
                            Button(
                                onClick = {
                                    val hours = sleepHoursInput.toFloatOrNull() ?: 8.0f
                                    val deep = deepSleepInput.toIntOrNull() ?: 120
                                    val rem = remSleepInput.toIntOrNull() ?: 90
                                    val light = lightSleepInput.toIntOrNull() ?: 240
                                    val hr = heartRateInput.toIntOrNull() ?: 60
                                    
                                    // Advanced Recovery Score Algorithm
                                    // Base score of 50. Added value for duration (optimal is 7-9 hours)
                                    val durationScore = if (hours in 7f..9f) 30 else if (hours > 9f) 20 else (hours / 7f * 30).toInt()
                                    // Value for deep + rem phases ratio
                                    val totalPhasesMin = deep + rem
                                    val phasesScore = if (totalPhasesMin >= 180) 30 else (totalPhasesMin / 180f * 30).toInt()
                                    // Resting heart rate score (optimal is 50-65 bpm)
                                    val hrScore = if (hr in 50..65) 40 else if (hr < 50) 30 else (100 - hr).coerceAtLeast(10)
                                    
                                    val rawScore = (durationScore + phasesScore + hrScore)
                                    val finalScore = rawScore.coerceIn(0, 100)
                                    
                                    val recommendation = when {
                                        finalScore >= 85 -> "Doskonała regeneracja! Twój organizm jest w pełni gotowy na maksymalny wysiłek fizyczny i intelektualny."
                                        finalScore >= 65 -> "Dobra regeneracja. Umysł jest czysty, choć faza głęboka mogłaby być nieco dłuższa. Idealny czas na produktywną pracę."
                                        finalScore >= 45 -> "Średnia regeneracja. Unikaj nadmiernego stresu i ciężkiego treningu dzisiaj. Zwróć uwagę na wczesne pójście spać."
                                        else -> "Słaba regeneracja! Twój organizm wykazuje wysokie zmęczenie. Zadbaj o drzemkę regeneracyjną i unikaj kofeiny po godzinie 14:00."
                                    }
                                    
                                    val format = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault())
                                    val currentDateStr = format.format(Date())
                                    
                                    viewModel.insertSleepRecord(
                                        SleepRecord(
                                            date = currentDateStr,
                                            sleepDurationHours = hours,
                                            deepSleepMinutes = deep,
                                            remSleepMinutes = rem,
                                            lightSleepMinutes = light,
                                            recoveryScore = finalScore,
                                            heartRateAvg = hr,
                                            recommendation = recommendation
                                        )
                                    )
                                    
                                    viewModel.insertMemory(
                                        com.example.data.ColonyMemory(
                                            content = "[Sleep Agent] Dodano rejestr snu z dnia $currentDateStr (Wynik regeneracji: $finalScore%). Wygenerowano spersonalizowane zalecenie."
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_sleep_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analizuj i Zapisz Dane", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            // Right Panel: Analysis & History
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Historia Snu i Regeneracji",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { viewModel.clearSleepRecords() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("clear_sleep_btn")
                    ) {
                        Text("Wyczyść")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (sleepRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Brak danych w bazie", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(sleepRecords) { record ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sleep_record_card_${record.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(record.date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        record.recoveryScore >= 80 -> Color(0xFF10B981)
                                                        record.recoveryScore >= 60 -> Color(0xFFF59E0B)
                                                        else -> Color(0xFFEF4444)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "Regeneracja: ${record.recoveryScore}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Długość: ${record.sleepDurationHours}h", style = MaterialTheme.typography.bodySmall)
                                        Text("Głęboki: ${record.deepSleepMinutes}m", style = MaterialTheme.typography.bodySmall)
                                        Text("Tętno: ${record.heartRateAvg} bpm", style = MaterialTheme.typography.bodySmall)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Zalecenie Optymalizatora:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        record.recommendation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
