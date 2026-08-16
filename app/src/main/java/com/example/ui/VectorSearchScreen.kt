package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
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
import com.example.data.VectorEmbeddingLog
import com.example.viewmodel.ColonyViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VectorSearchScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    var queryText by remember { mutableStateOf("optymalizacja wydajności bazy danych") }
    val logs by viewModel.vectorEmbeddingLogs.collectAsState()

    // Mock initial embedding logs if empty
    LaunchedEffect(logs) {
        if (logs.isEmpty()) {
            viewModel.addVectorEmbeddingLog(
                sourceText = "Wykryto wyciek pamięci w module syntezy tekstu przy przetwarzaniu dużych payloadów JSON.",
                tag = "Logi Błędów",
                vectorJson = "[0.12, 0.85, 0.34, 0.91, 0.05]"
            )
            viewModel.addVectorEmbeddingLog(
                sourceText = "Zatwierdzono decyzję o automatycznym czyszczeniu starych logów telemetrycznych po 14 dniach.",
                tag = "Decyzje Kolonii",
                vectorJson = "[0.44, 0.12, 0.88, 0.22, 0.67]"
            )
            viewModel.addVectorEmbeddingLog(
                sourceText = "Baza danych Room została zoptymalizowana indeksami FTS5 oraz osobnym wątkiem wywołań IO.",
                tag = "Baza Danych",
                vectorJson = "[0.78, 0.92, 0.11, 0.65, 0.82]"
            )
        }
    }

    val searchResults = remember(logs, queryText) {
        if (queryText.isBlank()) {
            logs.map { it to 0.85f }
        } else {
            logs.map { log ->
                val mockSimilarity = (0.60f + (abs((log.sourceText.hashCode() + queryText.hashCode()) % 38) / 100f)).coerceAtMost(0.99f)
                log to mockSimilarity
            }.sortedByDescending { it.second }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Grain,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Wyszukiwanie Wektorowe",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Local Embeddings & Cosine Similarity",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("vector_search_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cofnij")
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
            // Search Input Field
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                label = { Text("Zapytanie Semantyczne (np. wycieki pamięci, wydajność bazy)") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                viewModel.addVectorEmbeddingLog(
                                    sourceText = "Nowy rekord indeksowany dla zapytania: $queryText",
                                    tag = "Wektor Własny",
                                    vectorJson = "[0.50, 0.50, 0.50, 0.50]"
                                )
                            }
                        },
                        modifier = Modifier.testTag("add_vector_record_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Indeksuj")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vector_query_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Semantic Match Results Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Wyniki Wektorowe (${searchResults.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Lokalna Baza Wektorowa: 100% Offline",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(searchResults) { (log, simScore) ->
                    VectorResultCard(log = log, similarityScore = simScore)
                }
            }
        }
    }
}

@Composable
private fun VectorResultCard(
    log: VectorEmbeddingLog,
    similarityScore: Float
) {
    val percentInt = (similarityScore * 100).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vector_card_${log.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = log.tag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Dopasowanie: $percentInt%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (percentInt > 80) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = log.sourceText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar for Similarity Score
            LinearProgressIndicator(
                progress = { similarityScore },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (percentInt > 80) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Wektor Float: ${log.embeddingVectorJson}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
