package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.utils.ApiGateway
import com.example.viewmodel.ColonyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phase5EvolutionScreen(
    onBack: () -> Unit,
    viewModel: ColonyViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var apiStatus by remember { mutableStateOf("Zatrzymany") }
    var apiLogs by remember { mutableStateOf(emptyList<String>()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phase 5: Evolution Engine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // 1. API Gateway
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("1. Ktor API Gateway & Webhook", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("Status: $apiStatus", style = MaterialTheme.typography.bodySmall)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                ApiGateway.startServer(port = 8080) { msg ->
                                    apiLogs = apiLogs + msg
                                }
                                apiStatus = "Serwer Działa na porcie 8080"
                            }) {
                                Text("Uruchom Ktor")
                            }
                            Button(onClick = {
                                ApiGateway.stopServer()
                                apiStatus = "Serwer Zatrzymany"
                            }) {
                                Text("Zatrzymaj")
                            }
                        }
                        
                        Text("Webhooks Odebrane:", style = MaterialTheme.typography.labelMedium)
                        apiLogs.takeLast(5).forEach {
                            Text("- $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
            
            // 3. Smart Home Bridge Agent
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp)) {
                            Text("[SIMULATION MODE]", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                        Text("3. Smart Home Bridge", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Brak fizycznych urządzeń IoT (Matter/Thread). Środowisko działa w izolowanym trybie symulacji.", style = MaterialTheme.typography.bodySmall)
                        
                        var lightsOn by remember { mutableStateOf(false) }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Symulowane Światła")
                            Switch(checked = lightsOn, onCheckedChange = { lightsOn = it })
                        }
                    }
                }
            }

            // 4. Sandbox Simulation Environment
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp)) {
                            Text("[SIMULATION MODE]", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                        Text("4. Sandbox Simulation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Izolowane środowisko do testowania dróg autonomii EDDE. Prawdziwa baza danych (Room) nie ulega mutacji.", style = MaterialTheme.typography.bodySmall)
                        
                        var simulationResult by remember { mutableStateOf("") }
                        Button(onClick = {
                            simulationResult = "Symulacja mutacji (Wektor Autonomii: +15%). Odrzucono (przez Policy Guard). Baza nietknięta."
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Uruchom Test W Piaskownicy")
                        }
                        if (simulationResult.isNotEmpty()) {
                            Text("> $simulationResult", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            // 2. Auto-Evolution
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("2. Auto-Evolution Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("Analizuje logi z bazy i Room, aby generować nowe ewolucyjne heurystyki decyzyjne EDDE.", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = {
                            viewModel.runSelfEvolutionCycle()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Wymuś Ewolucję Heurystyk")
                        }
                    }
                }
            }
        }
    }
}
