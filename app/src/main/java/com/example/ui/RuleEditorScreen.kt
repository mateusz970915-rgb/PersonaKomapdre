package com.example.ui

import com.example.viewmodel.ColonyViewModel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.util.UUID
import kotlin.math.roundToInt

import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class NodeType { TRIGGER, ACTION }

data class RuleNode(
    val id: String = UUID.randomUUID().toString(),
    var position: Offset = Offset.Zero,
    val type: NodeType,
    val text: String
)

data class RuleConnection(val fromId: String, val toId: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditorScreen(
    viewModel: ColonyViewModel? = null,
    onBack: () -> Unit
) {
    val dbNodes by viewModel?.ruleNodes?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val dbConnections by viewModel?.ruleConnections?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val ruleWorkerStatus by viewModel?.ruleWorkerStatus?.collectAsStateWithLifecycle() ?: remember { mutableStateOf("Idle") }

    val nodes = remember(dbNodes) {
        if (dbNodes.isNotEmpty()) {
            dbNodes.map { entity ->
                RuleNode(
                    id = entity.id,
                    position = Offset(entity.posX, entity.posY),
                    type = if (entity.nodeType == "TRIGGER") NodeType.TRIGGER else NodeType.ACTION,
                    text = entity.text
                )
            }
        } else {
            listOf(
                RuleNode(id = "node_1", position = Offset(100f, 300f), type = NodeType.TRIGGER, text = "Screen Time > 2h"),
                RuleNode(id = "node_2", position = Offset(600f, 300f), type = NodeType.ACTION, text = "Notify Rest Agent")
            )
        }
    }

    val connections = remember(dbConnections) {
        if (dbConnections.isNotEmpty()) {
            dbConnections.map { entity ->
                RuleConnection(fromId = entity.fromId, toId = entity.toId)
            }
        } else {
            listOf(
                RuleConnection(fromId = "node_1", toId = "node_2")
            )
        }
    }
    
    var connectingFrom by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val nodeWidthPx = with(density) { 200.dp.toPx() }
    val nodeHeightPx = with(density) { 100.dp.toPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Behavior Rules")
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (ruleWorkerStatus) {
                                "Running" -> MaterialTheme.colorScheme.tertiaryContainer
                                "Succeeded" -> MaterialTheme.colorScheme.primaryContainer
                                "Failed" -> MaterialTheme.colorScheme.errorContainer
                                "Queued" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = "Worker: $ruleWorkerStatus",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            viewModel?.triggerRuleEvaluatorWork()
                        }
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Evaluate Rules via WorkManager")
                    }
                    IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Filled.Add, "Add Node") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            // Draw Connections
            Canvas(modifier = Modifier.fillMaxSize()) {
                val nodeMap = nodes.associateBy { it.id }
                for (conn in connections) {
                    val fromNode = nodeMap[conn.fromId]
                    val toNode = nodeMap[conn.toId]
                    if (fromNode != null && toNode != null) {
                        val start = fromNode.position + Offset(nodeWidthPx, nodeHeightPx / 2)
                        val end = toNode.position + Offset(0f, nodeHeightPx / 2)
                        
                        val path = Path().apply {
                            moveTo(start.x, start.y)
                            cubicTo(
                                start.x + 200f, start.y,
                                end.x - 200f, end.y,
                                end.x, end.y
                            )
                        }
                        drawPath(path, color = Color.Gray, style = Stroke(width = 6f))
                    }
                }
            }

            // Draw Nodes
            nodes.forEach { node ->
                Box(
                    modifier = Modifier
                        .offset { IntOffset(node.position.x.roundToInt(), node.position.y.roundToInt()) }
                        .size(width = 200.dp, height = 100.dp)
                        .background(
                            color = if (node.type == NodeType.TRIGGER) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = if (connectingFrom == node.id) 4.dp else 1.dp,
                            color = if (connectingFrom == node.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .pointerInput(node.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    viewModel?.addRuleNode(node.id, node.position.x, node.position.y, node.type.name, node.text)
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                node.position = node.position + dragAmount
                            }
                        }
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = if (node.type == NodeType.TRIGGER) "TRIGGER" else "ACTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (node.type == NodeType.TRIGGER) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = node.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (node.type == NodeType.TRIGGER) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { 
                                    viewModel?.deleteRuleNode(node.id)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Delete, "Delete", modifier = Modifier.size(16.dp))
                            }
                            Button(
                                onClick = {
                                    if (connectingFrom == null) {
                                        connectingFrom = node.id
                                    } else if (connectingFrom != node.id) {
                                        val sourceId = connectingFrom!!
                                        viewModel?.addRuleConnection(sourceId, node.id)
                                        val sourceNode = nodes.find { it.id == sourceId }
                                        if (sourceNode != null && viewModel != null) {
                                            viewModel.sendInterAgentMessage(
                                                senderAgentName = "Behavior Engine",
                                                senderRole = "Governance Rule",
                                                content = "Linked Behavior Rule: '${sourceNode.text}' -> '${node.text}'",
                                                topic = "Behavior Dynamics"
                                            )
                                        }
                                        connectingFrom = null
                                    } else {
                                        connectingFrom = null
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(if (connectingFrom == node.id) "Cancel" else "Connect", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddNodeDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { type, text ->
                    val newId = UUID.randomUUID().toString()
                    viewModel?.addRuleNode(newId, 100f, 100f, type.name, text)
                    if (viewModel != null) {
                        viewModel.sendInterAgentMessage(
                            senderAgentName = "Behavior Engine",
                            senderRole = "Governance Rule",
                            content = "Created new behavior rule node ($type): '$text'",
                            topic = "Behavior Dynamics"
                        )
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddNodeDialog(onDismiss: () -> Unit, onAdd: (NodeType, String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(NodeType.TRIGGER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Node") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == NodeType.TRIGGER,
                        onClick = { selectedType = NodeType.TRIGGER }
                    )
                    Text("Trigger")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedType == NodeType.ACTION,
                        onClick = { selectedType = NodeType.ACTION }
                    )
                    Text("Action")
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onAdd(selectedType, text) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
