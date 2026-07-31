package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ColonyMemory
import com.example.network.AILlmClient
import com.example.viewmodel.ColonyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebContentAnalyzerScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    val sharedText by viewModel.sharedWebText.collectAsState()
    
    var urlInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var rawTextOutput by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    
    // Auto-prefill if we have shared text
    LaunchedEffect(sharedText) {
        sharedText?.let {
            if (it.startsWith("http://") || it.startsWith("https://")) {
                urlInput = it
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Web Content Analyzer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = "Analyze", tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "AI Web Scraping & Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Wprowadź dowolny adres URL, aby pobrać treść strony internetowej, automatycznie oczyścić kod HTML i wygenerować inteligentne podsumowanie przez model Gemini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // URL input & actions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text("Adres URL strony") },
                            placeholder = { Text("https://example.com/news/article") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("web_analyzer_url_input"),
                            singleLine = true
                        )
                        
                        Button(
                            onClick = {
                                if (urlInput.isNotBlank()) {
                                    isLoading = true
                                    statusMessage = "Pobieranie zawartości strony..."
                                    analysisResult = ""
                                    rawTextOutput = ""
                                    
                                    scope.launch {
                                        try {
                                            val fetchedHtml = withContext(Dispatchers.IO) {
                                                val client = OkHttpClient.Builder().build()
                                                val request = Request.Builder().url(urlInput).build()
                                                client.newCall(request).execute().use { response ->
                                                    if (!response.isSuccessful) throw Exception("HTTP error code: ${response.code}")
                                                    response.body?.string() ?: ""
                                                }
                                            }
                                            
                                            statusMessage = "Oczyszczanie kodu HTML..."
                                            // Robust cleaning: remove tags, styles, scripts
                                            val cleanedText = withContext(Dispatchers.Default) {
                                                var text = fetchedHtml
                                                // Remove head, script and style tags
                                                text = text.replace(Regex("<script[^>]*?>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                                                text = text.replace(Regex("<style[^>]*?>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                                                text = text.replace(Regex("<head[^>]*?>[\\s\\S]*?</head>", RegexOption.IGNORE_CASE), "")
                                                // Strip all tags
                                                text = text.replace(Regex("<[^>]*?>"), " ")
                                                // Collapse whitespace
                                                text = text.replace(Regex("\\s+"), " ").trim()
                                                if (text.length > 5000) {
                                                    text.substring(0, 5000) // Cap to avoid context overflow
                                                } else {
                                                    text
                                                }
                                            }
                                            
                                            rawTextOutput = cleanedText
                                            statusMessage = "Generowanie analizy przez AI (Gemini)..."
                                            
                                            val prompt = """
                                                Oto oczyszczona treść ze strony internetowej ($urlInput).
                                                Przeanalizuj ją szczegółowo i stwórz eleganckie, czytelne podsumowanie po polsku w formacie Markdown zawierające:
                                                1. Główny temat / tezę strony.
                                                2. Kluczowe punkty i argumenty (podpunktami).
                                                3. Główne wnioski lub wezwania do działania (Call to Action).
                                                
                                                Treść strony:
                                                $cleanedText
                                            """.trimIndent()
                                            
                                            val summary = withContext(Dispatchers.IO) {
                                                AILlmClient.generateContent(
                                                    context = context,
                                                    prompt = prompt,
                                                    systemInstruction = "Jesteś ekspertem analizy treści internetowych i doradcą PersonaMesh."
                                                )
                                            }
                                            
                                            analysisResult = summary
                                            statusMessage = "Sukces!"
                                            
                                            // Persist as a Colony Memory
                                            viewModel.insertMemory(
                                                ColonyMemory(
                                                    content = "[Web Content Analyzer] Pomyślnie przeanalizowano stronę: $urlInput. Streszczenie zostało zapisane."
                                                )
                                            )
                                            
                                        } catch (e: Exception) {
                                            analysisResult = "Błąd podczas analizy: ${e.message ?: "Nieznany błąd"}"
                                            statusMessage = "Niepowodzenie."
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fetch_and_analyze_btn"),
                            enabled = !isLoading && urlInput.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(statusMessage)
                            } else {
                                Text("Pobierz i przeanalizuj stronę")
                            }
                        }
                    }
                }
            }

            // Results visualization and download options (Auto-Report Generator)
            if (analysisResult.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Analiza Zawartości (Markdown)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(analysisResult))
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopiuj do schowka")
                                    }
                                    
                                    // Zapisywanie raportu w MediaStore.Downloads (Faza 2, Krok 8: Auto-Report Generator!)
                                    IconButton(
                                        onClick = {
                                            try {
                                                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                                val fileName = "PersonaMesh_Report_$sdf.md"
                                                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                                                    android.os.Environment.DIRECTORY_DOWNLOADS
                                                )
                                                val file = File(downloadsDir, fileName)
                                                FileOutputStream(file).use { out ->
                                                    out.write(analysisResult.toByteArray())
                                                }
                                                statusMessage = "Raport zapisany w Downloads: $fileName"
                                            } catch (e: Exception) {
                                                statusMessage = "Błąd zapisu raportu: ${e.message}"
                                            }
                                        },
                                        modifier = Modifier.testTag("save_report_btn")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Zapisz raport MD")
                                    }
                                }
                            }

                            // Output Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = analysisResult,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                            
                            if (statusMessage.startsWith("Raport zapisany")) {
                                Text(
                                    text = statusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
