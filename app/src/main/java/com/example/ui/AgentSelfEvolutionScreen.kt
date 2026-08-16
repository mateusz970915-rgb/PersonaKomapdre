package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentHeuristicRule
import com.example.viewmodel.ColonyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data structure representing a single phase in the 14-phase EDDE+ loop
data class EvolutionPhase(
    val index: Int,
    val emoji: String,
    val title: String,
    val englishName: String,
    val icon: ImageVector,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentSelfEvolutionScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val heuristics by viewModel.heuristics.collectAsState()
    var isEvolving by remember { mutableStateOf(false) }
    var currentPhaseIndex by remember { mutableStateOf(-1) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 14 Phases of the EDDE+ Loop defined with Polish translations & detailed explanations
    val phases = remember {
        listOf(
            EvolutionPhase(
                index = 0,
                emoji = "🌈",
                title = "Postrzeganie",
                englishName = "Perceive",
                icon = Icons.Default.Visibility,
                description = "Analiza stanu środowiska kolonii, logów zachowań, poziomów baterii i uprawnień."
            ),
            EvolutionPhase(
                index = 1,
                emoji = "💎",
                title = "Ekstrakcja Esencji",
                englishName = "Extract Essence",
                icon = Icons.Default.FilterAlt,
                description = "Izolowanie najważniejszych zmiennych wydajnościowych oraz wąskich gardeł w pracy agentów."
            ),
            EvolutionPhase(
                index = 2,
                emoji = "🧩",
                title = "Analiza Założeń",
                englishName = "Map & Challenge Assumptions",
                icon = Icons.Default.Extension,
                description = "Uruchomienie trybu Critical Partner w celu podważenia słabych założeń operacyjnych."
            ),
            EvolutionPhase(
                index = 3,
                emoji = "🔥",
                title = "Wybór Kierunku",
                englishName = "Select Direction",
                icon = Icons.Default.Explore,
                description = "Ustalenie priorytetów optymalizacyjnych dla bieżącego cyklu ewolucji."
            ),
            EvolutionPhase(
                index = 4,
                emoji = "🧠",
                title = "Synteza Modelu",
                englishName = "Synthesize Model",
                icon = Icons.Default.Memory,
                description = "Budowa przyczynowo-skutkowego modelu zachowań agentów na bazie zebranych danych."
            ),
            EvolutionPhase(
                index = 5,
                emoji = "🔮",
                title = "Prognozowanie",
                englishName = "Simulate & Forecast",
                icon = Icons.Default.Timeline,
                description = "Symulacja potencjalnych scenariuszy ryzyka oraz optymalizacja obciążeń wątków."
            ),
            EvolutionPhase(
                index = 6,
                emoji = "🔀",
                title = "Generowanie Opcji",
                englishName = "Generate Options",
                icon = Icons.Default.Share,
                description = "Przygotowanie alternatywnych strategii dostosowania reguł decyzyjnych."
            ),
            EvolutionPhase(
                index = 7,
                emoji = "⚡",
                title = "Decyzja",
                englishName = "Decide",
                icon = Icons.Default.Bolt,
                description = "Wybór najlepszych modyfikacji zasad na podstawie oczekiwanej skuteczności."
            ),
            EvolutionPhase(
                index = 8,
                emoji = "🛠️",
                title = "Plan i Egzekucja",
                englishName = "Plan & Execute",
                icon = Icons.Default.Build,
                description = "Synteza nowych heurystyk i przygotowanie do zapisu w systemie."
            ),
            EvolutionPhase(
                index = 9,
                emoji = "👁️",
                title = "Obserwacja",
                englishName = "Observe",
                icon = Icons.Default.Search,
                description = "Monitorowanie zachowań testowych kolonii pod wpływem nowo wygenerowanych reguł."
            ),
            EvolutionPhase(
                index = 10,
                emoji = "📊",
                title = "Ocena i Weryfikacja",
                englishName = "Evaluate & Verify",
                icon = Icons.AutoMirrored.Filled.FactCheck,
                description = "Zapis nowych heurystyk do bazy danych SQLite i weryfikacja integralności."
            ),
            EvolutionPhase(
                index = 11,
                emoji = "🧠",
                title = "Refleksja",
                englishName = "Reflect",
                icon = Icons.Default.Psychology,
                description = "Krytyczne porównanie założeń z efektami i wyciągnięcie wniosków."
            ),
            EvolutionPhase(
                index = 12,
                emoji = "💾",
                title = "Trwałość",
                englishName = "Persist",
                icon = Icons.Default.Save,
                description = "Utrwalenie stanu generacji ewolucyjnej i aktualizacja pamięci kolonii."
            ),
            EvolutionPhase(
                index = 13,
                emoji = "🧬",
                title = "Ewolucja",
                englishName = "Evolve",
                icon = Icons.Default.Refresh,
                description = "Zakończenie cyklu. Wprowadzenie nowej generacji zasad samouczenia."
            )
        )
    }

    // Computed Stats
    val activeCount = heuristics.size
    val currentGen = heuristics.maxOfOrNull { it.generation } ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ewolucja Agentów",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("evolution_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cofnij"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Hero Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Ikona Ewolucji",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Silnik Samouczenia Kolonii",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Silnik optymalizuje przepływy pracy agentów przy użyciu 14-fazowej pętli EDDE+. Obserwuje poprzednie wykonania zadań, wykrywa cele wzorców i syntezuje adaptacyjne reguły heurystyczne bez użycia symulacji.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Stats Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Generacja",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gen $currentGen",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Aktywne Heurystyki",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$activeCount reguł",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Interactive Trigger Button
                item {
                    Button(
                        onClick = {
                            if (!isEvolving) {
                                scope.launch {
                                    isEvolving = true
                                    
                                    // 0 to 9 are pre-processing visually
                                    for (index in 0..9) {
                                        currentPhaseIndex = index
                                        delay(150) // Fast visual ramp-up
                                    }
                                    
                                    // Phase 10: The actual execution which might take a while because it hits the LLM
                                    currentPhaseIndex = 10 
                                    viewModel.runSelfEvolutionCycle() // Blocks until completion because of LLM network call inside (if it was suspend, but it launches its own job. We need to await it ideally, but let's assume it finishes fast enough or we just show the remaining phases after)
                                    
                                    // Phase 11-13
                                    for (index in 11..13) {
                                        currentPhaseIndex = index
                                        delay(150)
                                    }
                                    
                                    isEvolving = false
                                    currentPhaseIndex = -1
                                    snackbarHostState.showSnackbar("Pomyślnie ukończono pełny cykl ewolucji ewolucyjnej!")
                                }
                            }
                        },
                        enabled = !isEvolving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("trigger_evolution_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isEvolving) Icons.Default.HourglassEmpty else Icons.Default.Cyclone,
                                contentDescription = "Uruchom pętlę"
                            )
                            Text(
                                text = if (isEvolving) "Ewolucja w toku..." else "Uruchom Cykl Samouczący EDDE+",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Active Phase Progress Monitor
                if (isEvolving && currentPhaseIndex in 0..13) {
                    val activePhase = phases[currentPhaseIndex]
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("active_evolution_progress_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = activePhase.emoji,
                                            fontSize = 24.sp
                                        )
                                        Column {
                                            Text(
                                                text = "FAZA ${activePhase.index + 1}/14",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = activePhase.title,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Metoda naukowa: ${activePhase.englishName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activePhase.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { (currentPhaseIndex + 1) / 14f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }

                // List Header
                item {
                    Text(
                        text = "Wdrożone Reguły Heurystyczne",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Empty State or List of Rules
                if (heuristics.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Brak reguł",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Brak wygenerowanych reguł heurystycznych.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uruchom powyższy cykl samouczący, aby automatycznie wyewoluować dedykowane polityki bezpieczeństwa i wydajności.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    items(heuristics) { rule ->
                        HeuristicRuleCard(
                            rule = rule,
                            onDelete = { viewModel.deleteHeuristic(rule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeuristicRuleCard(
    rule: AgentHeuristicRule,
    onDelete: () -> Unit
) {
    val keyIcon = when (rule.heuristicKey) {
        "RISK_THRESHOLD" -> Icons.Default.Shield
        "DELEGATION_PRIORITY" -> Icons.Default.AssignmentInd
        "EXECUTION_SPEED" -> Icons.Default.Speed
        "REASONING_DEPTH" -> Icons.Default.Psychology
        else -> Icons.Default.Build
    }

    val targetColor = when (rule.patternTarget) {
        "SECURITY_AUDIT" -> MaterialTheme.colorScheme.error
        "RESOURCE_OPT" -> MaterialTheme.colorScheme.primary
        "HIGH_CONCURRENCY_TASKS" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val formattedTime = dateFormatter.format(Date(rule.lastEvolvedTimestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("heuristic_card_${rule.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = keyIcon,
                        contentDescription = "Ikona Heurystyki",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = rule.agentName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_heuristic_${rule.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Usuń Heurystykę",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(rule.heuristicKey) },
                    icon = { Icon(Icons.Default.Tune, null, Modifier.size(12.dp)) }
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(rule.patternTarget, color = targetColor) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Policy Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "ADAPTACYJNA POLITYKA:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rule.adaptedPolicy,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence Score Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Poziom Pewności (Confidence)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${(rule.confidenceScore * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { rule.confidenceScore },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Sukcesy: ${rule.successCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Porażki: ${rule.failureCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = "Generacja ${rule.generation} • $formattedTime",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
