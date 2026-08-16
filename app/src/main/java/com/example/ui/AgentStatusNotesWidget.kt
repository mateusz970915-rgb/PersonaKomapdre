package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Agent
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgentStatusNotesWidget(
    agents: List<Agent>,
    viewModel: ColonyViewModel,
    modifier: Modifier = Modifier
) {
    var selectedAgentId by remember(agents) { mutableStateOf(agents.firstOrNull()?.id ?: 0) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var customSnippetText by remember { mutableStateOf("") }

    val currentAgent = remember(agents, selectedAgentId) {
        agents.find { it.id == selectedAgentId } ?: agents.firstOrNull()
    }

    // Parse status notes lines
    val notesLines = remember(currentAgent?.statusNotes) {
        currentAgent?.statusNotes?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_status_notes_widget")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Status Notes",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Agent Interaction & Feeling Logs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Room Database • Generated daily interaction snippets & mood notes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        currentAgent?.let { viewModel.generateInteractionNote(it.id) }
                    },
                    modifier = Modifier.testTag("generate_interaction_note_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generate Interaction",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Agent Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                agents.forEach { agent ->
                    val isSelected = agent.id == currentAgent?.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedAgentId = agent.id },
                        label = {
                            Text(
                                text = agent.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (agent.status == "Active") Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                        },
                        modifier = Modifier.testTag("status_notes_agent_chip_${agent.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            currentAgent?.let { agent ->
                // Summary sentiment banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${agent.name} Collaboration Sentiment",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Status: ${agent.status} • Autonomy: ${agent.autonomyLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddNoteDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_custom_status_note_btn")
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Note", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes List
                Text(
                    text = "Daily Interaction Snippets (${notesLines.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (notesLines.isEmpty()) {
                    Text(
                        text = "No status notes logged yet for ${agent.name}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notesLines.takeLast(6).reversed().forEachIndexed { idx, line ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("agent_status_note_item_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = line,
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

    if (showAddNoteDialog && currentAgent != null) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Status Snippet for ${currentAgent.name}") },
            text = {
                Column {
                    Text(
                        text = "Record a daily snippet of how this agent is feeling or collaborating with others:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customSnippetText,
                        onValueChange = { customSnippetText = it },
                        label = { Text("Interaction Snippet / Feeling") },
                        placeholder = { Text("e.g., Feeling aligned on security audits with Privacy Agent") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_status_snippet_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customSnippetText.isNotBlank()) {
                            viewModel.appendAgentStatusNoteSnippet(currentAgent.id, customSnippetText.trim())
                            customSnippetText = ""
                            showAddNoteDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_status_note_btn")
                ) {
                    Text("Save Snippet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
