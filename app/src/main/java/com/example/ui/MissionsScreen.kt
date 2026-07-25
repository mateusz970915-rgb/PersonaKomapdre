package com.example.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Mission
import com.example.data.SubTask
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val missions by viewModel.missions.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Missions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Create Mission")
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
            items(missions) { mission ->
                MissionCard(mission, viewModel)
            }
        }

        if (showCreateDialog) {
            CreateMissionDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { goal ->
                    viewModel.createMission(goal)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun MissionCard(mission: Mission, viewModel: ColonyViewModel) {
    val subTasks by viewModel.getSubTasksForMission(mission.id).collectAsState(initial = emptyList())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = mission.goal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Status: ${mission.status}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            subTasks.forEach { task ->
                SubTaskRow(task, viewModel)
            }
        }
    }
}

@Composable
fun SubTaskRow(task: SubTask, viewModel: ColonyViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Agent: ${task.assignedAgent} (${task.status})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        if (task.status != "Completed") {
            if (task.status == "Pending") {
                IconButton(onClick = { viewModel.executeSubTaskReal(task) }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Execute Agent Task", tint = MaterialTheme.colorScheme.primary)
                }
            } else if (task.status == "In Progress") {
                CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(4.dp))
            }
        } else {
            Text("Done", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun CreateMissionDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var goal by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Mission") },
        text = {
            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                label = { Text("Mission Goal") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (goal.isNotBlank()) onCreate(goal)
                }
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
