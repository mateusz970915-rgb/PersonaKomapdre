package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.delay
import coil.compose.AsyncImage


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.example.data.*
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgentCard(
    agent: Agent,
    isWorking: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    categoryColorHex: String = "#8B5CF6",
    prediction: ColonyViewModel.AgentPrediction?,
    subTasks: List<SubTask> = emptyList(),
    onSelectToggle: () -> Unit,
    onTap: () -> Unit,
    onPauseToggle: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val parsedCatColor = remember(categoryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(categoryColorHex))
        } catch (e: Exception) {
            Color(0xFF8B5CF6)
        }
    }

    val mood = remember(agent, subTasks) {
        com.example.data.calculateAgentMood(agent, subTasks)
    }

    val now = System.currentTimeMillis()
    val isOnline = (now - agent.lastActiveTimestamp) < (5 * 60 * 1000)
    
    val (statusText, dotColor) = when {
        agent.status == "Resting" -> "Resting" to Color(0xFFFF9800)
        agent.status == "Paused" || agent.status == "Halted" -> "Offline" to MaterialTheme.colorScheme.outline
        isWorking -> "Busy" to MaterialTheme.colorScheme.tertiary
        isOnline -> "Online" to Color(0xFF4CAF50)
        else -> "Offline" to MaterialTheme.colorScheme.outline
    }

    // Pulse animation states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTap() 
                },
                onLongClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectToggle() 
                }
            )
            .testTag("agent_card_${agent.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (agent.status == "Paused" || agent.status == "Halted") {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else if (agent.status == "Resting") {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.5.dp, parsedCatColor.copy(alpha = 0.5f))
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectToggle() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .testTag("agent_checkbox_${agent.id}")
                )
            }

            if (!isSelectionMode) {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 4.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = agent.status == "Active" || agent.status == "Working",
                        onCheckedChange = { onPauseToggle() },
                        modifier = Modifier.testTag("agent_status_toggle_${agent.id}")
                    )
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("agent_menu_btn_${agent.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Manage Agent",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (agent.status == "Active") "Pause Agent" else "Activate Agent") },
                    onClick = {
                        onPauseToggle()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (agent.status == "Active") Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Remove Agent") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            DropdownMenuItem(text={Text("Export Agent")},onClick={onExport();showMenu=false},leadingIcon={Icon(imageVector=Icons.Default.Share,contentDescription=null)})
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (agent.avatarUrl.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = agent.avatarUrl,
                            contentDescription = agent.name,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (agent.status != "Active") {
                                        MaterialTheme.colorScheme.outline
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = agent.name.firstOrNull()?.toString() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                    
                    // Status Dot with pulse ring behind it if Working
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        if (statusText == "Busy") {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.Center)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                        alpha = pulseAlpha
                                    }
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(1.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${agent.name} ${mood.emoji}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (agent.status == "Paused" || agent.status == "Halted") {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (agent.status == "Paused" || agent.status == "Halted") {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = agent.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (agent.status == "Paused" || agent.status == "Halted") {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${mood.emoji} ${mood.moodTitle}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = dotColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Permissions: ${agent.permissions}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val lastTask = subTasks.filter { it.assignedAgent.equals(agent.name, ignoreCase = true) }.maxByOrNull { it.timestamp }
                if (lastTask != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Last Activity:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            TranslateTextInPlace(
                                originalText = lastTask.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                // Predicted status indicator badge
                prediction?.let { pred ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("prediction_badge_${agent.id}")
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Predicted: ${pred.suggestedStatus}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            TranslateTextInPlace(
                                originalText = pred.suggestionReason,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
