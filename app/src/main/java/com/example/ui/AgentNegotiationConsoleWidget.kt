package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Gavel
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
import com.example.data.Agent
import com.example.data.AgentNegotiationProposal
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgentNegotiationConsoleWidget(
    negotiations: List<AgentNegotiationProposal>,
    agents: List<Agent>,
    viewModel: ColonyViewModel,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Pending") } // "Pending", "All", "Resolved"
    var showNewProposalDialog by remember { mutableStateOf(false) }
    var selectedProposerName by remember(agents) { mutableStateOf(agents.firstOrNull()?.name ?: "") }
    var selectedTargetName by remember(agents) { mutableStateOf(agents.getOrNull(1)?.name ?: agents.firstOrNull()?.name ?: "") }
    var customTopic by remember { mutableStateOf("Resource Lock") }
    var customAction by remember { mutableStateOf("Request 50% bandwidth reallocation during night processing.") }

    var selectedCounterItem by remember { mutableStateOf<AgentNegotiationProposal?>(null) }
    var counterText by remember { mutableStateOf("") }

    val filteredNegotiations = remember(negotiations, selectedFilter) {
        when (selectedFilter) {
            "Pending" -> negotiations.filter { it.status == "Pending" || it.status == "Countered" }
            "Resolved" -> negotiations.filter { it.status == "Accepted" || it.status == "Rejected" || it.status == "Escalated" }
            else -> negotiations
        }
    }

    val pendingCount = negotiations.count { it.status == "Pending" || it.status == "Countered" }
    val acceptedCount = negotiations.count { it.status == "Accepted" }
    val escalatedCount = negotiations.count { it.status == "Escalated" }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_negotiation_console_widget")
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
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = "Negotiation Console",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Autonomous Agent Negotiation Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Conflict Resolution • Counter-proposals & Human Escalate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.autoInitiateAgentNegotiation() },
                        modifier = Modifier.testTag("initiate_conflict_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Initiate Conflict",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(
                        onClick = { showNewProposalDialog = true },
                        modifier = Modifier.testTag("open_create_negotiation_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Proposal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Active Disputes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$pendingCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Consensus", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$acceptedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Escalated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$escalatedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Pending", "Resolved", "All").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        modifier = Modifier.testTag("negotiation_filter_$filter")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proposals List
            if (filteredNegotiations.isEmpty()) {
                Text(
                    text = "No negotiations found matching status '$selectedFilter'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredNegotiations.take(10).forEach { item ->
                        val statusColor = when (item.status) {
                            "Accepted" -> Color(0xFF10B981)
                            "Rejected" -> Color(0xFFEF4444)
                            "Escalated" -> Color(0xFFF59E0B)
                            "Countered" -> Color(0xFF8B5CF6)
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("negotiation_item_${item.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${item.proposerAgent} ➔ ${item.targetAgent}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = item.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Topic: ${item.conflictTopic}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.proposedAction,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (item.counterProposal.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Counter Proposal: ${item.counterProposal}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                if (item.status == "Pending" || item.status == "Countered") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.resolveNegotiation(item.id, "Accepted") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("accept_negotiation_${item.id}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Accept", style = MaterialTheme.typography.labelSmall)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                selectedCounterItem = item
                                                counterText = "Offer 30% reduction in CPU lock duration."
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("counter_negotiation_${item.id}")
                                        ) {
                                            Text("Counter", style = MaterialTheme.typography.labelSmall)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.resolveNegotiation(item.id, "Rejected") },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("reject_negotiation_${item.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject", style = MaterialTheme.typography.labelSmall)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.resolveNegotiation(item.id, "Escalated") },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("escalate_negotiation_${item.id}")
                                        ) {
                                            Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Escalate", style = MaterialTheme.typography.labelSmall)
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

    // New Proposal Dialog
    if (showNewProposalDialog) {
        AlertDialog(
            onDismissRequest = { showNewProposalDialog = false },
            title = { Text("Initiate Agent Negotiation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select two agents to engage in resource or policy conflict resolution:")

                    OutlinedTextField(
                        value = selectedProposerName,
                        onValueChange = { selectedProposerName = it },
                        label = { Text("Proposer Agent") },
                        modifier = Modifier.fillMaxWidth().testTag("proposer_agent_input")
                    )

                    OutlinedTextField(
                        value = selectedTargetName,
                        onValueChange = { selectedTargetName = it },
                        label = { Text("Target Agent") },
                        modifier = Modifier.fillMaxWidth().testTag("target_agent_input")
                    )

                    OutlinedTextField(
                        value = customTopic,
                        onValueChange = { customTopic = it },
                        label = { Text("Conflict Topic") },
                        modifier = Modifier.fillMaxWidth().testTag("conflict_topic_input")
                    )

                    OutlinedTextField(
                        value = customAction,
                        onValueChange = { customAction = it },
                        label = { Text("Proposed Action / Demand") },
                        modifier = Modifier.fillMaxWidth().testTag("proposed_action_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedProposerName.isNotBlank() && selectedTargetName.isNotBlank()) {
                            viewModel.initiateNegotiation(
                                proposer = selectedProposerName,
                                target = selectedTargetName,
                                proposedAction = customAction,
                                conflictTopic = customTopic
                            )
                            showNewProposalDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_new_negotiation_btn")
                ) {
                    Text("Start Negotiation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProposalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Counter Dialog
    selectedCounterItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedCounterItem = null },
            title = { Text("Submit Counter-Proposal") },
            text = {
                Column {
                    Text("Counter proposal for negotiation between ${item.proposerAgent} and ${item.targetAgent}:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = counterText,
                        onValueChange = { counterText = it },
                        label = { Text("Counter Proposal Offer") },
                        modifier = Modifier.fillMaxWidth().testTag("counter_offer_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (counterText.isNotBlank()) {
                            viewModel.resolveNegotiation(item.id, "Countered", counterText.trim())
                            selectedCounterItem = null
                        }
                    },
                    modifier = Modifier.testTag("submit_counter_offer_btn")
                ) {
                    Text("Send Counter")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCounterItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
