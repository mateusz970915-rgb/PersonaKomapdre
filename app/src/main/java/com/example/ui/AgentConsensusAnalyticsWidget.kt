package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Shield
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
fun AgentConsensusAnalyticsWidget(
    negotiations: List<AgentNegotiationProposal>,
    agents: List<Agent>,
    viewModel: ColonyViewModel,
    modifier: Modifier = Modifier
) {
    var showVoteDialog by remember { mutableStateOf(false) }
    var policyInputText by remember { mutableStateOf("Autonomous Resource Rebalancing") }

    val totalCount = negotiations.size
    val acceptedCount = negotiations.count { it.status == "Accepted" }
    val counteredCount = negotiations.count { it.status == "Countered" }
    val rejectedCount = negotiations.count { it.status == "Rejected" }
    val escalatedCount = negotiations.count { it.status == "Escalated" }

    val consensusRate = if (totalCount > 0) {
        ((acceptedCount + counteredCount).toFloat() / totalCount.toFloat() * 100f).toInt()
    } else 100

    val topicDistribution = remember(negotiations) {
        negotiations.groupBy { it.conflictTopic }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(4)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_consensus_analytics_widget")
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Consensus Analytics",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Colony Alignment & Policy Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Consensus Index • Agent Alignment & Referendum Trigger",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showVoteDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("launch_policy_vote_btn")
                ) {
                    Icon(Icons.Default.HowToVote, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Policy Vote", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Consensus Health Gauge Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Colony Consensus Index",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$consensusRate%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (consensusRate >= 70) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "($totalCount Total Negotiations)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (consensusRate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (consensusRate >= 70) Color(0xFF10B981) else Color(0xFFF59E0B),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Topic Breakdown
            Text(
                text = "Primary Dispute Categories",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (topicDistribution.isEmpty()) {
                Text(
                    text = "No negotiation data available yet. Trigger a conflict simulation or policy vote above.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    topicDistribution.forEach { (topic, count) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupWork,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$topic: $count",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVoteDialog) {
        AlertDialog(
            onDismissRequest = { showVoteDialog = false },
            title = { Text("Launch Colony Policy Referendum") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Submit a colony-wide policy vote. All active agents will evaluate, vote, and log their consensus Snippets in Room DB:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = policyInputText,
                        onValueChange = { policyInputText = it },
                        label = { Text("Policy Title") },
                        placeholder = { Text("e.g., Nightly Data Purge & Multi-Sig Lock") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("policy_vote_title_input")
                    )

                    Text(
                        text = "Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Autonomous Resource Rebalancing",
                            "Strict Security Multi-Sig Protocol",
                            "Energy Conservation Quiet Hours",
                            "High-Priority Mission Override"
                        ).forEach { preset ->
                            SuggestionChip(
                                onClick = { policyInputText = preset },
                                label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("policy_preset_$preset")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (policyInputText.isNotBlank()) {
                            viewModel.triggerColonyWidePolicyVote(policyInputText.trim())
                            showVoteDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_policy_vote_btn")
                ) {
                    Text("Execute Referendum")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
