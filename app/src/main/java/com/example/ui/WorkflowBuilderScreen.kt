package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ColonyViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

@Serializable
data class DagNodeData(
    val id: String,
    val agentName: String,
    val role: String,
    var x: Float,
    var y: Float
)

@Serializable
data class DagEdgeData(
    val fromId: String,
    val toId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowBuilderScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    var dagName by remember { mutableStateOf("Przepływ Misji Kolonii #1") }
    var nodes by remember {
        mutableStateOf(
            listOf(
                DagNodeData("n1", "Analityk Danych", "Analiza", 100f, 150f),
                DagNodeData("n2", "Architekt Systemu", "Architektura", 350f, 150f),
                DagNodeData("n3", "Koder Kotlin", "Implementacja", 600f, 100f),
                DagNodeData("n4", "Tester QA", "Weryfikacja", 600f, 300f)
            )
        )
    }

    var edges by remember {
        mutableStateOf(
            listOf(
                DagEdgeData("n1", "n2"),
                DagEdgeData("n2", "n3"),
                DagEdgeData("n2", "n4")
            )
        )
    }

    var selectedFromNodeId by remember { mutableStateOf<String?>(null) }
    var executionStatusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kreator Przepływu (DAG)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Wizualne łączenie agentów w ukierunkowany graf acykliczny",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("workflow_builder_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cofnij")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val jsonNodes = Json.encodeToString(nodes)
                            val jsonEdges = Json.encodeToString(edges)
                            viewModel.saveWorkflowDag(dagName, "Graf stworzony w edytorze DAG", jsonNodes, jsonEdges)
                            executionStatusMessage = "Graf '$dagName' zapisany w bazie!"
                        },
                        modifier = Modifier.testTag("save_dag_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Zapisz DAG")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Workflow Control Toolbar
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = dagName,
                            onValueChange = { dagName = it },
                            label = { Text("Nazwa Przepływu") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dag_name_input")
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                executionStatusMessage = "Uruchomiono sekwencyjne wykonanie grafu DAG z 4 agentami!"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("run_dag_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Uruchom Graf")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Available Agent Templates Bar
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Text(
                                text = "Dodaj Agenta:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(
                            listOf(
                                "Badacz LLM" to "Badania",
                                "Strateg Kolonii" to "Strategia",
                                "Audytor Bezpieczeństwa" to "Audyt",
                                "Syntezator Mowy" to "Głos"
                            )
                        ) { (agentName, role) ->
                            AssistChip(
                                onClick = {
                                    val newId = "n${nodes.size + 1}"
                                    val newNodes = nodes.toMutableList().apply {
                                        add(DagNodeData(newId, agentName, role, 200f, 250f))
                                    }
                                    nodes = newNodes
                                },
                                label = { Text("+ $agentName") },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.testTag("add_agent_chip_$agentName")
                            )
                        }
                    }
                }
            }

            executionStatusMessage?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Canvas Area for DAG Nodes and Connectors
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF12121A))
            ) {
                // Draw Edge Arrows
                Canvas(modifier = Modifier.fillMaxSize()) {
                    edges.forEach { edge ->
                        val fromNode = nodes.find { it.id == edge.fromId }
                        val toNode = nodes.find { it.id == edge.toId }

                        if (fromNode != null && toNode != null) {
                            val startX = fromNode.x + 140f
                            val startY = fromNode.y + 60f
                            val endX = toNode.x + 10f
                            val endY = toNode.y + 60f

                            val path = Path().apply {
                                moveTo(startX, startY)
                                cubicTo(
                                    startX + 80f, startY,
                                    endX - 80f, endY,
                                    endX, endY
                                )
                            }

                            drawPath(
                                path = path,
                                color = Color(0xFF6C5CE7),
                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Render Draggable Interactive Nodes
                nodes.forEach { node ->
                    var offsetX by remember { mutableStateOf(node.x) }
                    var offsetY by remember { mutableStateOf(node.y) }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .pointerInput(node.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                    node.x = offsetX
                                    node.y = offsetY
                                }
                            }
                    ) {
                        DagNodeCard(
                            node = node,
                            isSelectedForConnection = selectedFromNodeId == node.id,
                            onConnectClick = {
                                if (selectedFromNodeId == null) {
                                    selectedFromNodeId = node.id
                                } else if (selectedFromNodeId != node.id) {
                                    val newEdge = DagEdgeData(selectedFromNodeId!!, node.id)
                                    if (!edges.contains(newEdge)) {
                                        edges = edges + newEdge
                                    }
                                    selectedFromNodeId = null
                                } else {
                                    selectedFromNodeId = null
                                }
                            },
                            onDeleteClick = {
                                nodes = nodes.filter { it.id != node.id }
                                edges = edges.filter { it.fromId != node.id && it.toId != node.id }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DagNodeCard(
    node: DagNodeData,
    isSelectedForConnection: Boolean,
    onConnectClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedForConnection) MaterialTheme.colorScheme.primaryContainer else Color(0xFF1E1E2E)
        ),
        modifier = Modifier
            .width(160.dp)
            .border(
                width = if (isSelectedForConnection) 2.dp else 1.dp,
                color = if (isSelectedForConnection) MaterialTheme.colorScheme.primary else Color(0xFF313244),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("dag_node_${node.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = node.role.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Usuń wędzeł",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = node.agentName,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )

            Text(
                text = "Rola: ${node.role}",
                fontSize = 11.sp,
                color = Color(0xFFA6ADC8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onConnectClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelectedForConnection) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSelectedForConnection) "Wybrany (Połącz)" else "Połącz →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
