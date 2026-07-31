package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentKnowledgeEdge
import com.example.viewmodel.ColonyViewModel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeGraphScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val edges by viewModel.knowledgeEdges.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showAddEdgeDialog by remember { mutableStateOf(false) }
    var sourceLabelInput by remember { mutableStateOf("") }
    var sourceTypeInput by remember { mutableStateOf("AGENT") }
    var targetLabelInput by remember { mutableStateOf("") }
    var targetTypeInput by remember { mutableStateOf("CONCEPT") }
    var relationTypeInput by remember { mutableStateOf("DEPENDS_ON") }
    
    val relationColors = mapOf(
        "DEPENDS_ON" to Color(0xFF2196F3),
        "CONFLICTS_WITH" to Color(0xFFF44336),
        "CAUSES" to Color(0xFFFF9800),
        "IMPLEMENTS" to Color(0xFF4CAF50),
        "ENFORCES" to Color(0xFF9C27B0)
    )

    // Build the set of unique nodes
    val uniqueNodes = remember(edges) {
        val nodesSet = mutableSetOf<Pair<String, String>>() // Label to Type
        edges.forEach { edge ->
            nodesSet.add(edge.sourceLabel to edge.sourceType)
            nodesSet.add(edge.targetLabel to edge.targetType)
        }
        nodesSet.toList()
    }

    // Dynamic node coordinates layout (Circle Layout)
    var dragOffsets = remember { mutableStateMapOf<String, Offset>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Knowledge Graph") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEdgeDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_edge_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj zależność", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Hub, contentDescription = "Hub", tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "Semantic Association Mesh",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Wizualny graf relacji i powiązań między agentami, misjami, regułami a abstrakcyjnymi konceptami systemowymi. Możesz przeciągać węzły lub dodawać nowe asocjacje.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Visual Graph Canvas Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (uniqueNodes.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Brak węzłów w grafie. Dodaj asocjację poniżej.", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 30f
                                isFakeBoldText = true
                            }
                            
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(uniqueNodes) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            // Find closest node to the drag position to move it
                                            val touchOffset = change.position
                                            var closestNode: String? = null
                                            var minDistance = Float.MAX_VALUE
                                            
                                            // Compute positions
                                            uniqueNodes.forEachIndexed { index, node ->
                                                val basePos = dragOffsets[node.first] ?: run {
                                                    val angle = (index.toDouble() / uniqueNodes.size) * 2.0 * Math.PI
                                                    val r = 220f
                                                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                                                    Offset(
                                                        (centerOffset.x + r * cos(angle)).toFloat(),
                                                        (centerOffset.y + r * sin(angle)).toFloat()
                                                    )
                                                }
                                                val dist = (touchOffset - basePos).getDistance()
                                                if (dist < minDistance && dist < 120f) {
                                                    minDistance = dist
                                                    closestNode = node.first
                                                }
                                            }
                                            
                                            closestNode?.let { nodeLabel ->
                                                val currentOffset = dragOffsets[nodeLabel] ?: run {
                                                    val index = uniqueNodes.indexOfFirst { it.first == nodeLabel }
                                                    val angle = (index.toDouble() / uniqueNodes.size) * 2.0 * Math.PI
                                                    val r = 220f
                                                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                                                    Offset(
                                                        (centerOffset.x + r * cos(angle)).toFloat(),
                                                        (centerOffset.y + r * sin(angle)).toFloat()
                                                    )
                                                }
                                                dragOffsets[nodeLabel] = currentOffset + dragAmount
                                            }
                                        }
                                    }
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                                val radius = 220f

                                // Calculated node positions mapped to labels
                                val nodePositions = uniqueNodes.mapIndexed { index, node ->
                                    val customPos = dragOffsets[node.first]
                                    val finalPos = customPos ?: run {
                                        val angle = (index.toDouble() / uniqueNodes.size) * 2.0 * Math.PI
                                        Offset(
                                            (center.x + radius * cos(angle)).toFloat(),
                                            (center.y + radius * sin(angle)).toFloat()
                                        )
                                    }
                                    node.first to finalPos
                                }.toMap()

                                // 1. Draw connection lines
                                edges.forEach { edge ->
                                    val startPos = nodePositions[edge.sourceLabel]
                                    val endPos = nodePositions[edge.targetLabel]
                                    if (startPos != null && endPos != null) {
                                        val color = relationColors[edge.relationType] ?: Color.Gray
                                        drawLine(
                                            color = color,
                                            start = startPos,
                                            end = endPos,
                                            strokeWidth = 5f
                                        )
                                        // Draw a small circle in the middle to denote the relation direction
                                        val midPoint = Offset(
                                            (startPos.x + endPos.x) / 2f,
                                            (startPos.y + endPos.y) / 2f
                                        )
                                        drawCircle(
                                            color = color,
                                            radius = 10f,
                                            center = midPoint
                                        )
                                    }
                                }

                                // 2. Draw nodes
                                uniqueNodes.forEachIndexed { _, node ->
                                    val pos = nodePositions[node.first] ?: center
                                    val color = when (node.second) {
                                        "AGENT" -> Color(0xFF2196F3)
                                        "MISSION" -> Color(0xFF4CAF50)
                                        "RULE" -> Color(0xFF9C27B0)
                                        else -> Color(0xFF607D8B) // CONCEPT
                                    }
                                    
                                    // Base Circle
                                    drawCircle(
                                        color = color,
                                        radius = 45f,
                                        center = pos
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 45f,
                                        center = pos,
                                        style = Stroke(width = 4f)
                                    )
                                    
                                    // Text inside circle (Android Native Canvas)
                                    drawContext.canvas.nativeCanvas.drawText(
                                        if (node.first.length > 5) node.first.substring(0, 4) + ".." else node.first,
                                        pos.x - 22f,
                                        pos.y + 10f,
                                        paint
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Relationship list / Table of relations
            item {
                Text(
                    text = "Asocjacje i Relacje (${edges.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (edges.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Brak krawędzi w bazie danych.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(edges) { edge ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = edge.sourceLabel,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "(${edge.sourceType})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background((relationColors[edge.relationType] ?: Color.Gray).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = edge.relationType,
                                        color = relationColors[edge.relationType] ?: Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = edge.targetLabel,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "(${edge.targetType})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = {
                                    viewModel.deleteKnowledgeEdge(edge.id)
                                },
                                modifier = Modifier.testTag("delete_edge_btn_${edge.id}")
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Usuń relację")
                            }
                        }
                    }
                }
            }
        }

        // Add Edge Dialog
        if (showAddEdgeDialog) {
            AlertDialog(
                onDismissRequest = { showAddEdgeDialog = false },
                title = { Text("Dodaj nową relację") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = sourceLabelInput,
                            onValueChange = { sourceLabelInput = it },
                            label = { Text("Etykieta źródła (np. SecurityAgent)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Typ źródła:")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("AGENT", "MISSION", "MEMORY", "DECISION").forEach { t ->
                                val sel = sourceTypeInput == t
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { sourceTypeInput = t }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(t, color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 10.sp)
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = targetLabelInput,
                            onValueChange = { targetLabelInput = it },
                            label = { Text("Etykieta celu (np. PrivacyLaw)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Typ celu:")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("CONCEPT", "MISSION", "AGENT", "RULE").forEach { t ->
                                val sel = targetTypeInput == t
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { targetTypeInput = t }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(t, color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 10.sp)
                                }
                            }
                        }

                        Text("Relacja:")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("DEPENDS_ON", "CONFLICTS_WITH", "IMPLEMENTS").forEach { r ->
                                val sel = relationTypeInput == r
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { relationTypeInput = r }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(r, color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (sourceLabelInput.isNotBlank() && targetLabelInput.isNotBlank()) {
                                viewModel.insertKnowledgeEdge(
                                    AgentKnowledgeEdge(
                                        sourceLabel = sourceLabelInput,
                                        sourceType = sourceTypeInput,
                                        targetLabel = targetLabelInput,
                                        targetType = targetTypeInput,
                                        relationType = relationTypeInput
                                    )
                                )
                                sourceLabelInput = ""
                                targetLabelInput = ""
                                showAddEdgeDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_add_edge_btn")
                    ) {
                        Text("Dodaj relację")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEdgeDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}
