package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.utils.ApiGateway
import com.example.utils.LocalLLMRunner
import com.example.utils.SandboxSimulationEnvironment
import com.example.utils.SmartHomeManager
import com.example.viewmodel.ColonyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phase5EvolutionScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var llmStatus by remember { mutableStateOf("Brak załadowanego modelu") }
    var llmOutput by remember { mutableStateOf("") }
    var llmPrompt by remember { mutableStateOf("Napisz wiersz o kodowaniu") }
    
    var apiStatus by remember { mutableStateOf("Serwer Zatrzymany") }
    var apiLogs by remember { mutableStateOf(listOf<String>()) }
    
    var smartHomeDevices by remember { mutableStateOf(listOf<String>()) }
    
    var sandboxLogs by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phase 5: Evolution Engine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Wróć")
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
            
            // 1. On-Device LLM Runner
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("1. On-Device LLM Runner (Gemma 2B JNI/MediaPipe)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            SimulationBadge()
                        }
                        Text("Status: $llmStatus", style = MaterialTheme.typography.bodySmall)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                coroutineScope.launch {
                                    llmStatus = "Ładowanie wag modelu..."
                                    val success = LocalLLMRunner.loadModel(context)
                                    llmStatus = if (success) "Model Załadowany (Symulacja sandbox)" else "Błąd ładowania"
                                }
                            }) {
                                Text("Wczytaj Model")
                            }
                        }
                        
                        OutlinedTextField(
                            value = llmPrompt,
                            onValueChange = { llmPrompt = it },
                            label = { Text("Prompt") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(onClick = {
                            coroutineScope.launch {
                                llmOutput = "Generowanie..."
                                llmOutput = LocalLLMRunner.generateResponse(llmPrompt)
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Generuj Offline")
                        }
                        
                        if (llmOutput.isNotEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface).padding(8.dp)) {
                                Text(llmOutput, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            
            // 2. API Gateway
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("2. Ktor API Gateway & Webhook", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
            
            // 3. Auto-Evolution
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("3. Auto-Evolution Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("Analizuje logi z bazy i Room, aby generować nowe ewolucyjne heurystyki decyzyjne EDDE.", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = {
                            viewModel.triggerAutoEvolution()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Wymuś Ewolucję Heurystyk")
                        }
                    }
                }
            }

            // 4. Smart Home Bridge
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("4. Smart Home Bridge (Matter/Thread)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            SimulationBadge()
                        }
                        
                        Button(onClick = {
                            SmartHomeManager.discoverDevices(context) { devices ->
                                smartHomeDevices = devices
                            }
                        }) {
                            Text("Skanuj sieć IoT (Sandbox)")
                        }
                        
                        smartHomeDevices.forEach { device ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(device, style = MaterialTheme.typography.bodySmall)
                                Button(onClick = {
                                    SmartHomeManager.executeCommand(device, "TOGGLE") { }
                                }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text("Toggle", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            
            // 5. Sandbox Simulation
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("5. Sandbox Simulation State Machine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            SimulationBadge()
                        }
                        Text("Wykonuje bezpieczną predykcję logiki bez modyfikowania bazy danych.", style = MaterialTheme.typography.bodySmall)
                        
                        Button(onClick = {
                            coroutineScope.launch {
                                val dummyState = SandboxSimulationEnvironment.SimulationSnapshot(emptyList<com.example.data.Agent>(), emptyList<com.example.data.Mission>(), emptyList<com.example.data.SubTask>())
                                val result = SandboxSimulationEnvironment.runSimulation(dummyState) { state, log ->
                                    log("Cloning state to Sandbox Memory...")
                                    kotlinx.coroutines.delay(500)
                                    log("Testing agent mutation: AI_1 (Aggressive Mode)")
                                    kotlinx.coroutines.delay(300)
                                    log("Mutation success, state remains isolated.")
                                    state
                                }
                                sandboxLogs = result.logs
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Uruchom Środowisko Symulacyjne")
                        }
                        
                        if (sandboxLogs.isNotEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.8f)).padding(12.dp)) {
                                Column {
                                    sandboxLogs.forEach {
                                        Text("> $it", color = Color.Green, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SimulationBadge() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = "[SIMULATION MODE]",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
