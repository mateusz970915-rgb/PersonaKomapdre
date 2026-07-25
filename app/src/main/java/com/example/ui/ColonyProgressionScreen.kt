package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AgentMilestone
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColonyProgressionScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val milestones by viewModel.agentMilestones.collectAsState()
    val subTasks by viewModel.subTasks.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val decisions by viewModel.decisions.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val newlyUnlocked by viewModel.newlyUnlockedAgentMilestone.collectAsState()

    val completedTasksCount = remember(subTasks) { subTasks.count { it.status.equals("Completed", ignoreCase = true) } }
    val completedMissionsCount = remember(missions) { missions.count { it.status.equals("Completed", ignoreCase = true) } }
    val decisionsCount = remember(decisions) { decisions.size }
    val unlockedBadgesCount = remember(badges) { badges.count { it.isUnlocked } }

    val totalXp = remember(completedTasksCount, completedMissionsCount, decisionsCount, unlockedBadgesCount) {
        viewModel.calculateTotalProductivityXp()
    }

    val nextMilestone = remember(milestones, totalXp) {
        milestones.filter { !it.isUnlocked }.minByOrNull { it.requiredXp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Colony Progression & Unlocks", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Productivity milestones expand your AI workforce", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("progression_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Hero XP & Next Milestone Progress Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("xp_hero_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL PRODUCTIVITY XP",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$totalXp XP",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${milestones.count { it.isUnlocked }} / ${milestones.size} Agents Unlocked",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (nextMilestone != null) {
                            val progressFloat = (totalXp.toFloat() / nextMilestone.requiredXp.toFloat()).coerceIn(0f, 1f)
                            Text(
                                text = "Next Unlock: ${nextMilestone.agentName} (${nextMilestone.requiredXp - totalXp} XP remaining)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        } else {
                            Text(
                                text = "🎉 All current colony agent milestones achieved! Your AI workforce is fully unlocked.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // XP Breakdown Chips
                        Text("Productivity XP Breakdown:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            XpPointStat("Tasks (${completedTasksCount})", "+${completedTasksCount * 10} XP")
                            XpPointStat("Missions (${completedMissionsCount})", "+${completedMissionsCount * 50} XP")
                            XpPointStat("Decisions (${decisionsCount})", "+${decisionsCount * 15} XP")
                            XpPointStat("Badges (${unlockedBadgesCount})", "+${unlockedBadgesCount * 25} XP")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "COLONY AGENT UNLOCK MILESTONES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(milestones, key = { it.id }) { milestone ->
                MilestoneAgentCard(
                    milestone = milestone,
                    currentXp = totalXp
                )
            }
        }
    }

    // Celebration Dialog when new agent unlocks
    if (newlyUnlocked != null) {
        val unlocked = newlyUnlocked!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissUnlockedAgentMilestone() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NEW AGENT UNLOCKED!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Congratulations! Your colony productivity achieved ${unlocked.requiredXp} XP.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(unlocked.agentName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            Text("${unlocked.role} • ${unlocked.type}", style = MaterialTheme.typography.bodySmall)
                            if (unlocked.systemPrompt.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "\"${unlocked.systemPrompt}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "This agent has automatically joined your active colony workspace and is ready to process tasks!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissUnlockedAgentMilestone() },
                    modifier = Modifier.testTag("dismiss_unlock_dialog_btn")
                ) {
                    Text("Recruit Agent")
                }
            }
        )
    }
}

@Composable
fun XpPointStat(label: String, xp: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = xp, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun MilestoneAgentCard(
    milestone: AgentMilestone,
    currentXp: Int
) {
    val icon = when (milestone.iconName) {
        "verified_user" -> Icons.Default.VerifiedUser
        "palette" -> Icons.Default.Palette
        "code" -> Icons.Default.Code
        "gavel" -> Icons.Default.Gavel
        else -> Icons.Default.SmartToy
    }

    val progressFloat = (currentXp.toFloat() / milestone.requiredXp.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isUnlocked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_card_${milestone.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (milestone.isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (milestone.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = milestone.agentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${milestone.role} • ${milestone.type}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (milestone.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            if (milestone.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (milestone.isUnlocked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (milestone.isUnlocked) "Unlocked" else "${milestone.requiredXp} XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (milestone.isUnlocked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!milestone.isUnlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currentXp / ${milestone.requiredXp} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (milestone.systemPrompt.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${milestone.systemPrompt}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
