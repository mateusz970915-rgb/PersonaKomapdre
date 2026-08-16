package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ColonyViewModel

data class MctsNode(
    val id: String,
    val actionName: String,
    val visits: Int,
    val wins: Int,
    val ucb1Score: Float,
    val depth: Int,
    val isBestPath: Boolean,
    val childIds: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MctsVisualizationScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    var selectedNodeId by remember { mutableStateOf("root") }

    val treeNodes = remember {
        listOf(
            MctsNode("root", "Stan Początkowy Misji", 1000, 680, 2.45f, 0, true, listOf("c1", "c2", "c3")),
            MctsNode("c1", "Ścieżka A: Równoległe wykonanie w tle", 450, 380, 2.89f, 1, true, listOf("c1_1", "c1_2")),
            MctsNode("c2", "Ścieżka B: Sekwencyjna analiza ze stróżem", 300, 190, 1.62f, 1, false, listOf("c2_1")),
            MctsNode("c3", "Ścieżka C: Natychmiastowe zaniechanie zadania", 250, 110, 0.98f, 1, false),
            MctsNode("c1_1", "Wykonaj zadanie z modelem Gemini 1.5 Pro", 320, 290, 3.12f, 2, true),
            MctsNode("c1_2", "Wykonaj zadanie lokalnym Gemma Offline", 130, 90, 2.05f, 2, false),
            MctsNode("c2_1", "Ręczna weryfikacja przez użytkownika", 300, 190, 1.62f, 2, false)
        )
    }

    val selectedNode = remember(selectedNodeId) {
        treeNodes.find { it.id == selectedNodeId } ?: treeNodes.first()
    }

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
                                text = "Wizualizacja Drzewa MCTS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Monte Carlo Tree Search & UCB1 Exploration",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("mcts_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cofnij")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { selectedNodeId = "root" },
                        modifier = Modifier.testTag("mcts_reset_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Resetuj widok")
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
                .padding(16.dp)
        ) {
            // Summary MCTS Metrics Header Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Symulacje Monte Carlo: 1000 Rollouts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Algorytm UCB1 automatycznie wyłonił zoptymalizowaną ścieżkę c1_1",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Interactive Tree Nodes List arranged by Depth
            Text(
                text = "Krok 1: Wybierz Węzeł w Drzewie Decyzyjnym",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(treeNodes) { node ->
                    MctsNodeCard(
                        node = node,
                        isSelected = selectedNodeId == node.id,
                        onClick = { selectedNodeId = node.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Node Details Inspector
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mcts_details_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Szczegóły Węzła: ${selectedNode.actionName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (selectedNode.isBestPath) {
                            Surface(
                                color = Color(0xFFFFB703),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "★ Złota Ścieżka",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricItem("Wizyty (N)", "${selectedNode.visits}")
                        MetricItem("Wygrane (W)", "${selectedNode.wins}")
                        MetricItem("Win Rate", "${((selectedNode.wins.toFloat() / selectedNode.visits) * 100).toInt()}%")
                        MetricItem("Wynik UCB1", "%.2f".format(selectedNode.ucb1Score))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Wzór UCB1: Score = (W / N) + c * sqrt(ln(N_total) / N)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MctsNodeCard(
    node: MctsNode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val indentDp = (node.depth * 20).dp

    Row(
        modifier = Modifier
            .padding(start = indentDp)
            .fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (node.isBestPath) 2.dp else 1.dp,
                    color = if (node.isBestPath) Color(0xFFFFB703) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
                .testTag("mcts_node_${node.id}")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.actionName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Głębokość: ${node.depth} | Wizyty: ${node.visits} | UCB1: %.2f".format(node.ucb1Score),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (node.isBestPath) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
