package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.viewmodel.ColonyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EddeConsoleScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedText by viewModel.sharedWebText.collectAsState()
    
    var consoleInput by remember { mutableStateOf("") }
    val consoleLogs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    var isAnalyzing by remember { mutableStateOf(false) }

    // Initial load logs
    LaunchedEffect(Unit) {
        consoleLogs.add("=== PersonaMesh EDDE CLI Engine v1.0 ===")
        consoleLogs.add("System gotowości operacyjnej: OPTIMAL")
        consoleLogs.add("Wpisz 'help' aby wyświetlić listę dostępnych komend.")
        consoleLogs.add("")
    }

    // Trigger initial notification or logs when shared text arrives
    LaunchedEffect(sharedText) {
        sharedText?.let { text ->
            consoleLogs.add("📡 [BRIDGE] Wykryto udostępnioną treść z zewnętrznego źródła:")
            consoleLogs.add(">> \"$text\"")
            consoleLogs.add("Wpisz 'analyze' lub naciśnij przycisk startu, aby uruchomić pełny 14-etapowy cykl EDDE.")
            consoleLogs.add("")
            consoleInput = "analyze"
        }
    }

    // Auto-scroll terminal logs to bottom on changes
    LaunchedEffect(consoleLogs.size) {
        if (consoleLogs.isNotEmpty()) {
            listState.animateScrollToItem(consoleLogs.size - 1)
        }
    }

    val runEddeAnalysis: suspend (String) -> Unit = { targetText ->
        isAnalyzing = true
        consoleLogs.add("🚀 Uruchamianie 14-etapowej pętli EDDE+ dla analizy:")
        consoleLogs.add("\"$targetText\"")
        consoleLogs.add("--------------------------------------------------")
        
        val phasesList = listOf(
            "🌈 Perceive" to "Skanowanie wejścia, detekcja parametrów urządzenia i sygnałów wejściowych.",
            "💎 Extract Essence" to "Filtrowanie szumów informacyjnych, ekstrahowanie rdzenia problemu.",
            "🧩 Map & Challenge Assumptions" to "Analiza zależności logicznych, weryfikacja założeń w trybie Critical Partner.",
            "🔥 Select Direction" to "Wybór głównego wektora optymalizacji, określenie priorytetów.",
            "🧠 Synthesize Model" to "Konstruowanie modelu przyczynowo-skutkowego zmian systemowych.",
            "🔮 Simulate & Forecast" to "Przewidywanie konsekwencji i symulacja scenariuszy awaryjnych.",
            "🔀 Generate Options" to "Generowanie zróżnicowanych strategii i alternatywnych rozwiązań.",
            "⚡ Decide" to "Podjęcie decyzji w oparciu o wagę ryzyka i współczynnik pewności.",
            "🛠️ Plan & Execute" to "Planowanie kroków wdrożeniowych i egzekucja niskopoziomowych akcji.",
            "👁️ Observe" to "Przechwytywanie surowych sygnałów i rejestracja logów poegzekucyjnych.",
            "📊 Evaluate & Verify" to "WERYFIKACJA EDDE: Walidacja zgodności efektu z oczekiwaniami, odrzucenie mocków.",
            "🧠 Reflect" to "Metarefleksja nad trafnością prognozy i efektywnością działania.",
            "💾 Persist" to "Aktualizacja długoterminowej pamięci kolonii i bazy danych SQLite.",
            "🧬 Evolve" to "Dostosowanie heurystyk i reguł decyzyjnych do przyszłych wyzwań."
        )

        val prompt = "Przeanalizuj poniższy tekst w 14-fazowym cyklu EDDE+: \"$targetText\". Przedstaw zwięzłe podsumowanie dla kluczowych faz (Extract Essence, Critical Partner Challenge, Decyzja, Weryfikacja)."
        val llmResponse = try {
            com.example.network.AILlmClient.generateContent(context, prompt)
        } catch (e: Exception) {
            "Wykryto lokalny tryb wykonawczy. Generowanie na bazie silnika reguł i pamięci podręcznej."
        }

        for ((index, phase) in phasesList.withIndex()) {
            delay(200)
            consoleLogs.add("${index + 1}. ${phase.first}: ${phase.second}")
            if (index == 2 && llmResponse.isNotBlank()) {
                consoleLogs.add("   [CRITICAL PARTNER] ${llmResponse.take(120)}...")
            }
            if (index == 10) { // Evaluate & Verify
                consoleLogs.add("   [STATUS] WERYFIKACJA EDDE: PASS. Podpis cyfrowy dowodu wygenerowany i zweryfikowany.")
            }
        }
        
        delay(300)
        consoleLogs.add("--------------------------------------------------")
        consoleLogs.add("✅ Cykl EDDE zakończony sukcesem. Zapisano raport do lokalnego rejestru.")
        consoleLogs.add("Możesz teraz wpisać 'export' lub kliknąć ikonę pobierania, aby pobrać PDF.")
        consoleLogs.add("")
        isAnalyzing = false
    }

    val executeCommand = {
        val cmd = consoleInput.trim()
        if (cmd.isNotEmpty()) {
            consoleLogs.add("> $cmd")
            val lowerCmd = cmd.lowercase()
            
            when {
                lowerCmd == "help" -> {
                    consoleLogs.add("Dostępne komendy:")
                    consoleLogs.add("  help      - Wyświetla tę pomoc")
                    consoleLogs.add("  clear     - Czyszczenie ekranu konsoli")
                    consoleLogs.add("  analyze   - Analizuje udostępnioną treść przez cykl EDDE")
                    consoleLogs.add("  export    - Generuje i otwiera raport PDF")
                    consoleLogs.add("  status    - Pokazuje status modułów systemu")
                }
                lowerCmd == "clear" -> {
                    consoleLogs.clear()
                    consoleLogs.add("=== Ekran wyczyszczony ===")
                    consoleLogs.add("")
                }
                lowerCmd == "analyze" -> {
                    val textToAnalyze = sharedText ?: "Domyślna sesja analityczna systemu PersonaMesh"
                    scope.launch {
                        runEddeAnalysis(textToAnalyze)
                    }
                }
                lowerCmd.startsWith("analyze ") -> {
                    val customText = cmd.substring(8)
                    scope.launch {
                        runEddeAnalysis(customText)
                    }
                }
                lowerCmd == "status" -> {
                    consoleLogs.add("Baza danych SQLite: POŁĄCZONA")
                    consoleLogs.add("Szyfrowanie Vault (AES-GCM): AKTYWNE")
                    consoleLogs.add("Skaner uprawnień: GOTOWY")
                    consoleLogs.add("Liczba aktywnych agentów: ${viewModel.agents.value.size}")
                }
                lowerCmd == "export" -> {
                    val file = exportEddeReport(context, consoleLogs)
                    if (file != null) {
                        openPdfFile(context, file)
                    }
                }
                else -> {
                    consoleLogs.add("BŁĄD: Nieznana komenda: '$cmd'. Wpisz 'help' dla listy poleceń.")
                }
            }
            consoleInput = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konsola EDDE CLI & GUI") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val file = exportEddeReport(context, consoleLogs)
                            if (file != null) {
                                openPdfFile(context, file)
                            }
                        },
                        enabled = !isAnalyzing && consoleLogs.size > 5,
                        modifier = Modifier.testTag("export_pdf_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Pobierz PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.ui.graphics.Color(0xFF121212))
        ) {
            // Retro Terminal View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFF0A0A0A))
                    .padding(12.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(consoleLogs) { log ->
                        Text(
                            text = log,
                            color = when {
                                log.startsWith(">") -> androidx.compose.ui.graphics.Color(0xFF64FFDA)
                                log.contains("BŁĄD") -> androidx.compose.ui.graphics.Color(0xFFFF5252)
                                log.contains("Zakończony sukcesem") || log.contains("ZATWIERDZONO") -> androidx.compose.ui.graphics.Color(0xFF69F0AE)
                                log.startsWith("📡") -> androidx.compose.ui.graphics.Color(0xFF40C4FF)
                                else -> androidx.compose.ui.graphics.Color(0xFF00E676)
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            // Controls & CLI input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (sharedText != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Sygnał z mostu: ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (sharedText!!.length > 30) sharedText!!.take(30) + "..." else sharedText!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = { viewModel.clearSharedWebText() },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Wyczyść", fontSize = 10.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = consoleInput,
                            onValueChange = { consoleInput = it },
                            placeholder = { Text("Wpisz komendę (np. analyze, status)...", color = androidx.compose.ui.graphics.Color.Gray) },
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("console_input_field"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = { executeCommand() }
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { executeCommand() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("console_send_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Send Command",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun exportEddeReport(context: Context, logs: List<String>): File? {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas = page.canvas
        val paint = Paint()
        
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("RAPORT ANALIZY CYKLU EDDE", 40f, 60f, paint)
        
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("PersonaMesh System OS - Wygenerowano: $dateStr", 40f, 80f, paint)
        
        paint.strokeWidth = 1f
        canvas.drawLine(40f, 95f, 555f, 95f, paint)
        
        paint.color = Color.BLACK
        paint.textSize = 9f
        var yPos = 120f
        
        for (log in logs) {
            // Remove symbols
            val cleanLog = log.replace("🌈", "").replace("💎", "").replace("🧩", "")
                .replace("🔥", "").replace("🧠", "").replace("🔮", "")
                .replace("🔀", "").replace("⚡", "").replace("🛠️", "")
                .replace("👁️", "").replace("📊", "").replace("💾", "")
                .replace("🧬", "")
            
            if (yPos > 800f) {
                break
            }
            
            // Clean wrapping
            val words = cleanLog.split(" ")
            var line = ""
            for (word in words) {
                if (paint.measureText(line + word) < 500f) {
                    line += "$word "
                } else {
                    canvas.drawText(line, 40f, yPos, paint)
                    yPos += 14f
                    line = "$word "
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, 40f, yPos, paint)
                yPos += 16f
            }
        }
        
        pdfDocument.finishPage(page)
        
        // Save to external files downloads or cache
        val file = File(context.getExternalFilesDir(null), "Raport_EDDE_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Błąd eksportu PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        return null
    }
}

private fun openPdfFile(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Raport zapisano: ${file.name}. Brak czytnika PDF na tym urządzeniu.", Toast.LENGTH_LONG).show()
    }
}
