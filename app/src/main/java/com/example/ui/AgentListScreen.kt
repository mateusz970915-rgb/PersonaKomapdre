package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Agent
import com.example.viewmodel.AgentViewModel

private fun parseAccentColor(json: String): Color? {
    return try {
        if (json.contains("accentColor")) {
            val match = Regex("\"accentColor\"\\s*:\\s*\"([^\"]+)\"").find(json)
            val hex = match?.groupValues?.get(1)
            if (hex != null) {
                Color(android.graphics.Color.parseColor(hex))
            } else null
        } else null
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentListScreen(
    modifier: Modifier = Modifier,
    viewModel: AgentViewModel = viewModel()
) {
    val agents by viewModel.agentsState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAgentForColor by remember { mutableStateOf<Agent?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Agent Registry") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_agent_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Agent")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (agents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No agents registered yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("agent_lazy_column")
                ) {
                    items(
                        items = agents,
                        key = { it.id }
                    ) { agent ->
                        AgentItemCard(
                            agent = agent,
                            onDelete = { viewModel.deleteAgent(agent) },
                            onChangeColor = { selectedAgentForColor = agent }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAgentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, role ->
                viewModel.addAgent(name, role)
                showAddDialog = false
            }
        )
    }

    selectedAgentForColor?.let { agent ->
        AgentColorPickerDialog(
            agent = agent,
            onDismiss = { selectedAgentForColor = null },
            onColorSelected = { hex ->
                viewModel.updateAgentAccentColor(agent, hex)
                selectedAgentForColor = null
            }
        )
    }
}

@Composable
fun AgentItemCard(
    agent: Agent,
    onDelete: () -> Unit,
    onChangeColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = parseAccentColor(agent.configurationJson)
    val cardBorder = if (accentColor != null) BorderStroke(2.dp, accentColor) else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_card_${agent.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = accentColor ?: MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = agent.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Role: ${agent.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onChangeColor,
                    modifier = Modifier.testTag("color_agent_${agent.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Change Accent Color",
                        tint = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_agent_${agent.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Agent",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AgentColorPickerDialog(
    agent: Agent,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val colorOptions = listOf(
        "#E53935" to "Red",
        "#1E88E5" to "Blue",
        "#43A047" to "Green",
        "#FB8C00" to "Orange",
        "#8E24AA" to "Purple",
        "#00ACC1" to "Cyan",
        "#FDD835" to "Yellow",
        "#3949AB" to "Indigo"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Accent Color for ${agent.name}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select a custom highlight accent color for this agent:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    colorOptions.take(4).forEach { (hex, name) ->
                        ColorOptionItem(hex = hex, name = name, onSelect = { onColorSelected(hex) })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    colorOptions.drop(4).forEach { (hex, name) ->
                        ColorOptionItem(hex = hex, name = name, onSelect = { onColorSelected(hex) })
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorOptionItem(
    hex: String,
    name: String,
    onSelect: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(hex))
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable { onSelect() }
            .testTag("color_option_$name"),
        contentAlignment = Alignment.Center
    ) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAgentDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("GENERAL") }
    var expanded by remember { mutableStateOf(false) }

    val roles = listOf("GENERAL", "SECURITY", "FINANCE", "WORK", "RESEARCH", "ANALYTICS")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Agent") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Agent Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("agent_name_input")
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Role")
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("agent_role_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { roleOption ->
                            DropdownMenuItem(
                                text = { Text(roleOption) },
                                onClick = {
                                    role = roleOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, role)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_agent_button")
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
