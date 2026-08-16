package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AgentPersona
import com.example.viewmodel.PersonaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: PersonaViewModel = viewModel(factory = PersonaViewModel.Factory)
) {
    val personas by viewModel.personas.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarządzanie Personami") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj Personę")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(personas) { persona ->
                PersonaItemCard(persona = persona)
            }
        }

        if (showDialog) {
            PersonaDialog(
                onDismiss = { showDialog = false },
                onSave = { agentName, traits, role, style ->
                    viewModel.addPersona(
                        AgentPersona(
                            agentName = agentName,
                            characterTraits = traits,
                            operationalRole = role,
                            communicationStyle = style
                        )
                    )
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun PersonaItemCard(persona: AgentPersona) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = persona.agentName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Cechy: \${persona.characterTraits}", fontSize = 14.sp)
            Text("Rola: \${persona.operationalRole}", fontSize = 14.sp)
            Text("Styl: \${persona.communicationStyle}", fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var agentName by remember { mutableStateOf("") }
    var characterTraits by remember { mutableStateOf("") }
    var operationalRole by remember { mutableStateOf("") }
    var communicationStyle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowa Persona") },
        text = {
            Column {
                OutlinedTextField(
                    value = agentName,
                    onValueChange = { agentName = it },
                    label = { Text("Nazwa Agenta") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = characterTraits,
                    onValueChange = { characterTraits = it },
                    label = { Text("Cechy charakteru") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = operationalRole,
                    onValueChange = { operationalRole = it },
                    label = { Text("Rola operacyjna") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = communicationStyle,
                    onValueChange = { communicationStyle = it },
                    label = { Text("Styl komunikacji") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (agentName.isNotBlank() && characterTraits.isNotBlank() && operationalRole.isNotBlank() && communicationStyle.isNotBlank()) {
                        onSave(agentName, characterTraits, operationalRole, communicationStyle)
                    }
                }
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
