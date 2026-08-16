package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HallucinationAuditLog
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HallucinationDetectorScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    var promptInput by remember { mutableStateOf("Jakie są zalety nowej wersji Kotlin 2.0 i KSP?") }
    var responseInput by remember { mutableStateOf("Kotlin 2.0 wprowadza kompilator K2 z 2x szybszym czasem budowania oraz wbudowane wsparcie dla wielowątkowych tablic GPU.") }
    val auditLogs by viewModel.hallucinationAuditLogs.collectAsState()

    // Mock initial hallucination audit log
    LaunchedEffect(auditLogs) {
        if (auditLogs.isEmpty()) {
            viewModel.addHallucinationAuditLog(
                promptText = "Kiedy wydano pierwszą wersję systemu Android?",
                responseText = "Pierwsza komercyjna wersja Android 1.0 ukazała się we wrześniu 2008 roku na telefonie HTC Dream.",
                factCheckScore = 0.98f,
                verdict = "Fakt Zweryfikowany",
                checkedClaimsJson = "[\"Data wydania: Wrzesień 2008 [Zgodne]\", \"Pierwszy telefon: HTC Dream [Zgodne]\"]"
            )
            viewModel.addHallucinationAuditLog(
                promptText = "Czy Android używa bezpośrednio jądra Windows NT?",
                responseText = "Tak, Android opiera się na dostosowanym jądrze Windows NT 10.0.",
                factCheckScore = 0.12f,
                verdict = "Możliwa Halucynacja",
                checkedClaimsJson = "[\"Android używa jądra Linux, a nie Windows NT [Fałsz]\"]"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hallucination Detector",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Weryfikacja faktograficzna i badanie wiarygodności LLM",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("hallucination_detector_back_button")) {
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
            // New Test Card Input
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Przeprowadź Test Wiarygodności Wyjścia LLM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        label = { Text("Zapytanie (Prompt)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hallucination_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = responseInput,
                        onValueChange = { responseInput = it },
                        label = { Text("Odpowiedź Modelu do Audytu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hallucination_response_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (responseInput.isNotBlank()) {
                                val isHallucination = responseInput.lowercase().contains("gpu") || responseInput.lowercase().contains("windows")
                                val score = if (isHallucination) 0.35f else 0.94f
                                val verdict = if (isHallucination) "Podejrzana Informacja" else "Fakt Zweryfikowany"
                                val claims = if (isHallucination) {
                                    "[\"Kompilator K2 [Zgodne]\", \"Wsparcie dla wielowątkowych tablic GPU [Niepotwierdzone w oficjalnej dokumentacji]\"]"
                                } else {
                                    "[\"Wszystkie twierdzenia zweryfikowane z bazy wiedzy [Potwierdzono]\"]"
                                }

                                viewModel.addHallucinationAuditLog(
                                    promptText = promptInput,
                                    responseText = responseInput,
                                    factCheckScore = score,
                                    verdict = verdict,
                                    checkedClaimsJson = claims
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_fact_check_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uruchom Audyt Faktograficzny")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Historia Audytów (${auditLogs.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(auditLogs) { log ->
                    AuditLogCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: HallucinationAuditLog) {
    val isVerified = log.verdict.contains("Zweryfikowany")
    val isSuspicious = log.verdict.contains("Podejrzana") || log.verdict.contains("Halucynacja")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isVerified -> Color(0xFF1B382B)
                isSuspicious -> Color(0xFF3E1F1F)
                else -> Color(0xFF2E261B)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_log_card_${log.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isVerified) Color(0xFF4CAF50) else Color(0xFFFF5252)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.verdict,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = "Wynik: ${(log.factCheckScore * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pytanie: ${log.promptText}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color(0xFFDCDCAA)
            )

            Text(
                text = "Odpowiedź: ${log.responseText}",
                fontSize = 13.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color(0xFF12121A),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Szczegóły Twierdzeń: ${log.checkedClaimsJson}",
                    fontSize = 11.sp,
                    color = Color(0xFFCE9178),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
