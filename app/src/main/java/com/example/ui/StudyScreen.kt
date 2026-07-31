package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Flashcard
import com.example.viewmodel.ColonyViewModel
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val cards by viewModel.flashcards.collectAsStateWithLifecycle()
    
    var questionInput by remember { mutableStateOf("") }
    var answerInput by remember { mutableStateOf("") }
    
    var activeTab by remember { mutableStateOf(0) } // 0 = Review, 1 = Manage Cards
    
    // Review Session State
    val now = System.currentTimeMillis()
    val dueCards = remember(cards) { cards.filter { it.nextReviewTime <= now } }
    
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }
    
    var snackbarHostState = remember { SnackbarHostState() }
    var scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Repetition (SM-2)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("study_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Powrót")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0; showAnswer = false; currentCardIndex = 0 },
                    modifier = Modifier.testTag("tab_review"),
                    text = { Text("Sesja Powtórek (${dueCards.size} zaległych)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.testTag("tab_manage"),
                    text = { Text("Zarządzaj Fiszkami (${cards.size})", fontWeight = FontWeight.Bold) }
                )
            }
            
            if (activeTab == 0) {
                // REVIEW MODE
                if (dueCards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Wszystko powtórzone na dziś!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Świetna robota! Wróć później, gdy nadejdzie czas kolejnych powtórek według algorytmu SM-2.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val currentCard = dueCards.getOrNull(currentCardIndex)
                    if (currentCard == null) {
                        // Finished last card of this session
                        LaunchedEffect(Unit) {
                            currentCardIndex = 0
                            showAnswer = false
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Karta ${currentCardIndex + 1} z ${dueCards.size}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Flashcard Display
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .testTag("flashcard_display_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (showAnswer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            if (showAnswer) "ODPOWIEDŹ" else "PYTANIE",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            if (showAnswer) currentCard.answer else currentCard.question,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = if (showAnswer) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            if (!showAnswer) {
                                Button(
                                    onClick = { showAnswer = true },
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(50.dp)
                                        .testTag("show_answer_btn")
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pokaż Odpowiedź", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // SuperMemo-2 rating buttons (0 to 5)
                                Text(
                                    "Oceń poziom zapamiętania (SM-2):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    val ratings = listOf(
                                        "0 - Pustka" to 0,
                                        "1 - Błąd" to 1,
                                        "2 - Trudno" to 2,
                                        "3 - Ok" to 3,
                                        "4 - Dobrze" to 4,
                                        "5 - Idealnie" to 5
                                    )
                                    ratings.forEach { (label, score) ->
                                        Button(
                                            onClick = {
                                                // Execute SuperMemo-2 calculation
                                                val q = score
                                                var newRepetition = currentCard.repetition
                                                var newInterval = currentCard.interval
                                                var newEf = currentCard.easinessFactor
                                                
                                                if (q >= 3) {
                                                    if (newRepetition == 0) {
                                                        newInterval = 1
                                                    } else if (newRepetition == 1) {
                                                        newInterval = 6
                                                    } else {
                                                        newInterval = ceil(newInterval * newEf).toInt()
                                                    }
                                                    newRepetition++
                                                } else {
                                                    newRepetition = 0
                                                    newInterval = 1
                                                }
                                                
                                                // EF modification formula
                                                newEf = (newEf + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)))
                                                if (newEf < 1.3f) newEf = 1.3f
                                                
                                                val nextReview = System.currentTimeMillis() + (newInterval.toLong() * 24 * 3600 * 1000)
                                                
                                                val updatedCard = currentCard.copy(
                                                    repetition = newRepetition,
                                                    interval = newInterval,
                                                    easinessFactor = newEf,
                                                    nextReviewTime = nextReview
                                                )
                                                
                                                viewModel.insertFlashcard(updatedCard)
                                                
                                                // Move to next card
                                                if (currentCardIndex + 1 < dueCards.size) {
                                                    currentCardIndex++
                                                    showAnswer = false
                                                } else {
                                                    // Finished session
                                                    currentCardIndex = 0
                                                    showAnswer = false
                                                    viewModel.insertMemory(
                                                        com.example.data.ColonyMemory(
                                                            content = "[Study Agent] Zakończono sesję powtórek fiszek w module Spaced Repetition (SuperMemo-2)."
                                                        )
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.dp)
                                                .testTag("rate_btn_$score"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = when (score) {
                                                    0, 1 -> MaterialTheme.colorScheme.error
                                                    2, 3 -> MaterialTheme.colorScheme.tertiary
                                                    else -> MaterialTheme.colorScheme.primary
                                                }
                                            ),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                label.substringBefore(" -"),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // MANAGE CARDS MODE
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Create New Flashcard Panel
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Dodaj Nową Fiszkę",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            OutlinedTextField(
                                value = questionInput,
                                onValueChange = { questionInput = it },
                                label = { Text("Pytanie (Front)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_question_input"),
                                leadingIcon = { Icon(Icons.Default.QuestionMark, contentDescription = null) }
                            )
                            
                            OutlinedTextField(
                                value = answerInput,
                                onValueChange = { answerInput = it },
                                label = { Text("Odpowiedź (Back)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_answer_input"),
                                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                            )
                            
                            Button(
                                onClick = {
                                    if (questionInput.isNotBlank() && answerInput.isNotBlank()) {
                                        viewModel.insertFlashcard(
                                            Flashcard(
                                                question = questionInput,
                                                answer = answerInput,
                                                nextReviewTime = System.currentTimeMillis() // Ready immediately
                                            )
                                        )
                                        questionInput = ""
                                        answerInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("add_card_btn")
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dodaj do bazy SM-2", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    // List of All Flashcards Panel
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            "Wszystkie Fiszki w bazie (${cards.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (cards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Brak fiszek. Dodaj swoją pierwszą fiszkę!", color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(cards) { card ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("card_list_item_${card.id}"),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Q: ${card.question}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.deleteFlashcard(card.id) },
                                                    modifier = Modifier.testTag("delete_card_btn_${card.id}")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                            Text("A: ${card.answer}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Przedział (dni): ${card.interval}", style = MaterialTheme.typography.labelSmall)
                                                Text("Powtórzenia: ${card.repetition}", style = MaterialTheme.typography.labelSmall)
                                                Text("Mnożnik EF: ${"%.2f".format(card.easinessFactor)}", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
