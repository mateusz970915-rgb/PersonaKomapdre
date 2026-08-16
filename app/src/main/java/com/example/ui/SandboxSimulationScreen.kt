package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxSimulationScreen(onNavigateBack: () -> Unit) {
    var simulationLog by remember { mutableStateOf(listOf<String>()) }
    var isSimulating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sandbox Simulation Environment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // SIMULATION MODE BADGE
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[SIMULATION MODE] - Zmiany stanu są izolowane. Główna baza danych nie jest modyfikowana.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    if (!isSimulating) {
                        isSimulating = true
                        simulationLog = listOf("Inicjalizacja izolowanej instancji EDDE...")
                        coroutineScope.launch {
                            delay(1000)
                            simulationLog = simulationLog + "Klonowanie stanu repozytorium do pamięci podręcznej..."
                            delay(1500)
                            simulationLog = simulationLog + "Testowanie mutacji wektora autonomii: ZWIĘKSZ_O_20%"
                            delay(1000)
                            simulationLog = simulationLog + "Ostrzeżenie: Wykryto naruszenie PolicyEnforcementPoint na sklonowanym drzewie."
                            delay(1000)
                            simulationLog = simulationLog + "Odrzucanie wariantu. Odtwarzanie bezpiecznego stanu bazowego."
                            delay(1000)
                            simulationLog = simulationLog + "Zakończono. Baza danych nietknięta."
                            isSimulating = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSimulating
            ) {
                Icon(Icons.Default.Science, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSimulating) "Simulating..." else "Uruchom test mutacyjny (Bezpieczny)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Simulation Output Log:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(simulationLog) { logEntry ->
                        Text(
                            text = "> $logEntry",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
