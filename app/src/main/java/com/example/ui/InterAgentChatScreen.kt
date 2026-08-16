package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.InterAgentMessage
import com.example.data.calculateAgentMood
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterAgentChatScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.interAgentMessages.collectAsState()
    val agents by viewModel.agents.collectAsState()
    val subTasks by viewModel.subTasks.collectAsState()

    var selectedTopic by remember { mutableStateOf("All") }
    var userBroadcastText by remember { mutableStateOf("") }
    var showInitiateDialog by remember { mutableStateOf(false) }
    var simTopicText by remember { mutableStateOf("") }

    val topics = listOf("All", "Task Synchronization", "Colony Health & Balance", "Governance & Privacy", "Task Assignment")

    val filteredMessages = remember(messages, selectedTopic) {
        if (selectedTopic == "All") {
            messages
        } else {
            messages.filter { it.topic.equals(selectedTopic, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Forum,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Inter-Agent Central Feed", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Agent-to-agent chatter & shared task discussion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("agent_chat_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { showInitiateDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("initiate_agent_chat_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Initiate Chat", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Topic Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(topics) { topic ->
                    FilterChip(
                        selected = selectedTopic == topic,
                        onClick = { selectedTopic = topic },
                        label = { Text(topic, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("filter_topic_$topic")
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Chat Messages Feed
            if (filteredMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No inter-agent chatter recorded for '$selectedTopic'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.initiateAgentDiscussion(if (selectedTopic == "All") "General Colony Progress" else selectedTopic) }) {
                            Text("Trigger Discussion")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    reverseLayout = false
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        InterAgentMessageCard(
                            message = msg,
                            agentList = agents,
                            subTaskList = subTasks
                        )
                    }
                }
            }

            // Input Bar for Colony Director Broadcast
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userBroadcastText,
                        onValueChange = { userBroadcastText = it },
                        placeholder = { Text("Broadcast directive or topic to agents...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("agent_chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (userBroadcastText.isNotBlank()) {
                                val text = userBroadcastText.trim()
                                viewModel.sendInterAgentMessage(
                                    senderAgentName = "Colony Director",
                                    senderRole = "Human Supervisor",
                                    content = text,
                                    topic = if (selectedTopic == "All") "Director Directive" else selectedTopic
                                )
                                viewModel.initiateAgentDiscussion(text)
                                userBroadcastText = ""
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .testTag("send_broadcast_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Broadcast",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }

    // Dialog to initiate custom topic discussion
    if (showInitiateDialog) {
        AlertDialog(
            onDismissRequest = { showInitiateDialog = false },
            title = { Text("Initiate Inter-Agent Discussion", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a shared task or colony topic for agents to debate and coordinate:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = simTopicText,
                        onValueChange = { simTopicText = it },
                        placeholder = { Text("e.g. Weekly Task Prioritization & Sleep Optimization") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_topic_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val topic = if (simTopicText.isNotBlank()) simTopicText.trim() else "Colony Optimization"
                        viewModel.initiateAgentDiscussion(topic)
                        showInitiateDialog = false
                        simTopicText = ""
                    },
                    modifier = Modifier.testTag("start_sim_discussion_btn")
                ) {
                    Text("Start Discussion")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInitiateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InterAgentMessageCard(
    message: InterAgentMessage,
    agentList: List<com.example.data.Agent>,
    subTaskList: List<com.example.data.SubTask>
) {
    val senderAgent = remember(message.senderAgentName, agentList) {
        agentList.find { it.name.equals(message.senderAgentName, ignoreCase = true) }
    }

    val moodInfo = remember(senderAgent, subTaskList) {
        if (senderAgent != null) {
            calculateAgentMood(senderAgent, subTaskList)
        } else null
    }

    val isDirector = message.senderAgentName.contains("Director", ignoreCase = true) || message.senderAgentName.contains("System", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDirector) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inter_agent_msg_${message.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDirector) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = moodInfo?.emoji ?: if (isDirector) "👑" else "🤖",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.senderAgentName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (moodInfo != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = moodInfo.emoji,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Text(
                            text = message.senderRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = message.topic,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (message.targetAgentName != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Addressing: @${message.targetAgentName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            val formattedTime = remember(message.timestamp) {
                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                sdf.format(java.util.Date(message.timestamp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
