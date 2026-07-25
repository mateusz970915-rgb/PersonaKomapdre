package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Agent
import com.example.viewmodel.ColonyViewModel
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AgentDashboardScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val agents by viewModel.agents.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredAgents = remember(agents, searchQuery) {
        if (searchQuery.isBlank()) {
            agents
        } else {
            agents.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.type.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var agentToEdit by remember { mutableStateOf<Agent?>(null) }
    var selectedAgentIds by remember { mutableStateOf(setOf<Int>()) }
    var isGroupedBySpecialty by remember { mutableStateOf(false) }
    val isSelectionMode = selectedAgentIds.isNotEmpty()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val json = kotlinx.serialization.json.Json.encodeToString(agents)
                        outputStream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (agentToEdit != null) {
        EditAgentDialog(
            agent = agentToEdit!!,
            onDismiss = { agentToEdit = null },
            onSave = { name, type ->
                viewModel.updateAgentDetails(agentToEdit!!.id, name, type)
                agentToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedAgentIds.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedAgentIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.deleteAgents(selectedAgentIds)
                            selectedAgentIds = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Agent Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isGroupedBySpecialty = !isGroupedBySpecialty }) {
                            Icon(if (isGroupedBySpecialty) Icons.Default.GridView else Icons.Default.ViewList, contentDescription = "Toggle Grouping")
                        }
                        IconButton(onClick = { exportLauncher.launch("agents_backup.json") }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export JSON")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search agents...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            if (filteredAgents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.GroupOff,
                            contentDescription = "No agents",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (agents.isEmpty()) "No agents found in colony." else "No agents match your search.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                        } else {
                if (isGroupedBySpecialty) {
                    val groupedAgents = filteredAgents.groupBy { it.type }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        groupedAgents.forEach { (type, agentsInGroup) ->
                            stickyHeader {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
                                ) {
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                                    )
                                }
                            }
                            
                            val chunked = agentsInGroup.chunked(2)
                            items(chunked) { rowAgents ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    for (agent in rowAgents) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            AgentCard(
                                                agent = agent,
                                                isSelected = selectedAgentIds.contains(agent.id),
                                                isSelectionMode = isSelectionMode,
                                                onDelete = { viewModel.deleteAgent(agent.id) },
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        if (selectedAgentIds.contains(agent.id)) {
                                                            selectedAgentIds -= agent.id
                                                        } else {
                                                            selectedAgentIds += agent.id
                                                        }
                                                    } else {
                                                        agentToEdit = agent
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!isSelectionMode) {
                                                        selectedAgentIds += agent.id
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (rowAgents.size < 2) {
                                        repeat(2 - rowAgents.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                    ) {
                        items(filteredAgents, key = { it.id }) { agent ->
                            AgentCard(
                                agent = agent,
                                isSelected = selectedAgentIds.contains(agent.id),
                                isSelectionMode = isSelectionMode,
                                onDelete = { viewModel.deleteAgent(agent.id) },
                                onClick = {
                                    if (isSelectionMode) {
                                        if (selectedAgentIds.contains(agent.id)) {
                                            selectedAgentIds -= agent.id
                                        } else {
                                            selectedAgentIds += agent.id
                                        }
                                    } else {
                                        agentToEdit = agent
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        selectedAgentIds += agent.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgentCard(
    agent: Agent,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isAgentActive = agent.status == "Active"
    
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else if (isAgentActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
    } else {
        Color.Transparent
    }

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CardDefaults.shape)
            .border(if (isSelected || isAgentActive) 2.dp else 0.dp, borderColor, CardDefaults.shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = agent.name,
                    modifier = Modifier.size(48.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                CircularProgressIndicator(
                    progress = { agent.performanceScore },
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = agent.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = agent.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            if (!isSelectionMode) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditAgentDialog(
    agent: Agent,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(agent.name) }
    var specialty by remember { mutableStateOf(agent.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Agent") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Agent Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Specialty") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), specialty.trim()) },
                enabled = name.isNotBlank() && specialty.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
