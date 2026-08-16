package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.ui.components.ChartColorThemeSelector
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ColonyViewModel,
    chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val agents by viewModel.agents.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    
    // Agent preferences state
    val agentPrefs by viewModel.agentPreferencesState.collectAsState()
    
    var openRouterKeyInput by remember(agentPrefs.openRouterApiKey) { mutableStateOf(agentPrefs.openRouterApiKey) }
    var keyVisible by remember { mutableStateOf(false) }
    var selectedModelFilter by remember { mutableStateOf("") }
    
    var geminiKeyInput by remember(agentPrefs.geminiApiKey) { mutableStateOf(agentPrefs.geminiApiKey) }
    var geminiKeyVisible by remember { mutableStateOf(false) }
    
    // Preferences state
    val prefs = remember { context.getSharedPreferences("colony_prefs", android.content.Context.MODE_PRIVATE) }
    var restEnabled by remember { mutableStateOf(prefs.getBoolean("rest_enabled", false)) }
    var startHour by remember { mutableStateOf(prefs.getInt("rest_start_hour", 22)) }
    var startMinute by remember { mutableStateOf(prefs.getInt("rest_start_minute", 0)) }
    var endHour by remember { mutableStateOf(prefs.getInt("rest_end_hour", 7)) }
    var endMinute by remember { mutableStateOf(prefs.getInt("rest_end_minute", 0)) }
    
    var schedulerEnabled by remember { mutableStateOf(prefs.getBoolean("scheduler_enabled", true)) }
    var workloadThreshold by remember { mutableStateOf(prefs.getInt("workload_threshold", 2)) }
    var restDurationSeconds by remember { mutableStateOf(prefs.getInt("rest_duration_seconds", 30)) }
    
    // Dropdown visibility states
    var showStartHourMenu by remember { mutableStateOf(false) }
    var showStartMinMenu by remember { mutableStateOf(false) }
    var showEndHourMenu by remember { mutableStateOf(false) }
    var showEndMinMenu by remember { mutableStateOf(false) }
    
    // Dialog states for adding calendar events
    var showAddEventDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventDesc by remember { mutableStateOf("") }
    var newEventAgent by remember { mutableStateOf("") }
    var newEventOffsetMins by remember { mutableStateOf("30") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Colony Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val themeScope = rememberCoroutineScope()
            // UI & Context Theme Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("theme_settings_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "UI & Context Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select a theme context:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val themes = listOf(
                        "Default" to "System Default",
                        "Zen" to "Personal (Zen)",
                        "DeepFocus" to "Work (Deep Focus)",
                        "Creative" to "Creative Flow"
                    )
                    
                    themes.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    themeScope.launch { viewModel.updateThemeMode(mode) }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = agentPrefs.themeMode == mode,
                                onClick = {
                                    themeScope.launch { viewModel.updateThemeMode(mode) }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // OpenRouter & AI Provider Selection Card
            val openRouterModels by chatViewModel.openRouterFreeModels.collectAsState()
            val isLoadingModels by chatViewModel.isLoadingOpenRouterModels.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth().testTag("openrouter_settings_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Provider & OpenRouter",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (agentPrefs.aiProvider == "openrouter") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = if (agentPrefs.aiProvider == "openrouter") "OPENROUTER" else "GEMINI DIRECT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (agentPrefs.aiProvider == "openrouter") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select active AI provider engine:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = agentPrefs.aiProvider == "gemini",
                            onClick = { viewModel.updateAiProvider("gemini") },
                            label = { Text("Google Gemini API") },
                            leadingIcon = {
                                if (agentPrefs.aiProvider == "gemini") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("select_gemini_chip")
                        )

                        FilterChip(
                            selected = agentPrefs.aiProvider == "openrouter",
                            onClick = { viewModel.updateAiProvider("openrouter") },
                            label = { Text("OpenRouter (Free Models)") },
                            leadingIcon = {
                                if (agentPrefs.aiProvider == "openrouter") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("select_openrouter_chip")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    if (agentPrefs.aiProvider == "openrouter") {
                        // OpenRouter API Key Configuration
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OpenRouter API Key",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = openRouterKeyInput,
                            onValueChange = { openRouterKeyInput = it },
                            label = { Text("sk-or-v1-...") },
                            placeholder = { Text("Enter your OpenRouter API Key") },
                            modifier = Modifier.fillMaxWidth().testTag("openrouter_key_input"),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { keyVisible = !keyVisible }) {
                                    Icon(
                                        imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle key visibility"
                                    )
                                }
                            },
                            visualTransformation = if (keyVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateOpenRouterApiKey(openRouterKeyInput.trim())
                                    android.widget.Toast.makeText(context, "OpenRouter API Key saved!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("save_openrouter_key_btn")
                            ) {
                                Text("Save OpenRouter Key")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(16.dp))

                        // Free Models Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "OpenRouter Free Models",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${openRouterModels.size} free models available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { chatViewModel.fetchOpenRouterFreeModels() },
                                modifier = Modifier.testTag("refresh_openrouter_models_btn")
                            ) {
                                if (isLoadingModels) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Models")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = selectedModelFilter,
                            onValueChange = { selectedModelFilter = it },
                            label = { Text("Filter free models...") },
                            modifier = Modifier.fillMaxWidth().testTag("filter_models_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val filteredModels = remember(openRouterModels, selectedModelFilter) {
                            if (selectedModelFilter.isBlank()) {
                                openRouterModels
                            } else {
                                openRouterModels.filter {
                                    it.id.contains(selectedModelFilter, ignoreCase = true) ||
                                    (it.name?.contains(selectedModelFilter, ignoreCase = true) == true)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (filteredModels.isEmpty()) {
                                    Text(
                                        text = "No free models match filter",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                } else {
                                    filteredModels.forEach { model ->
                                        val isSelected = agentPrefs.openRouterSelectedModel == model.id
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.updateOpenRouterSelectedModel(model.id)
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = model.name ?: model.id,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = model.id,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                    if (model.context_length != null) {
                                                        Text(
                                                            text = "Context: ${model.context_length / 1024}k tokens",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.outline
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Text(
                                                        text = "FREE",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Google Gemini API Key Configuration
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Gemini API Key",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = geminiKeyInput,
                            onValueChange = { geminiKeyInput = it },
                            label = { Text("AIzaSy...") },
                            placeholder = { Text("Wprowadź swój klucz Gemini API") },
                            modifier = Modifier.fillMaxWidth().testTag("gemini_key_input"),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { geminiKeyVisible = !geminiKeyVisible }) {
                                    Icon(
                                        imageVector = if (geminiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle key visibility"
                                    )
                                }
                            },
                            visualTransformation = if (geminiKeyVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pozostaw puste, aby korzystać z klucza systemowego w BuildConfig.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateGeminiApiKey(geminiKeyInput.trim())
                                    android.widget.Toast.makeText(context, "Klucz Gemini API zapisany!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("save_gemini_key_btn")
                            ) {
                                Text("Zapisz klucz Gemini")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gemini Model Selection
                        Text(
                            text = "Wybór Modelu Google Gemini",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Wybierz model dostosowany do Twoich potrzeb:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val geminiModelsList = listOf(
                            Pair("gemini-3.5-flash", "Google Gemini 3.5 Flash (Szybki i zoptymalizowany)"),
                            Pair("gemini-3.1-pro-preview", "Google Gemini 3.1 Pro Preview (Zaawansowane wnioskowanie)"),
                            Pair("gemini-3.1-flash-lite-preview", "Google Gemini 3.1 Flash Lite (Lekki i szybki)"),
                            Pair("gemini-2.5-flash-image", "Google Gemini 2.5 Flash Image (Multimodalny / Grafika)"),
                            Pair("gemini-3.1-flash-image-preview", "Google Gemini 3.1 Flash Image Preview (Wysoka jakość multimodalna)")
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                geminiModelsList.forEach { (modelId, modelName) ->
                                    val isSelected = agentPrefs.geminiSelectedModel == modelId
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updateGeminiSelectedModel(modelId)
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = modelName,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = modelId,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // API Key Status & Security Card
            val isApiKeyConfigured = viewModel.isApiKeyConfigured
            val apiErrorState by viewModel.apiErrorState.collectAsState()
            val securityMode = com.example.security.AgentCapabilityGuard.getSecurityMode()

            Card(
                modifier = Modifier.fillMaxWidth().testTag("api_key_status_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isApiKeyConfigured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isApiKeyConfigured) Icons.Default.AutoAwesome else Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = if (isApiKeyConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isApiKeyConfigured) {
                                    if (agentPrefs.aiProvider == "openrouter") "OpenRouter API: Connected" else "Gemini API: Connected"
                                } else {
                                    if (agentPrefs.aiProvider == "openrouter") "OpenRouter API Key Missing" else "Gemini API Key Missing"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isApiKeyConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (securityMode == com.example.security.GeminiSecurityMode.DEV_ONLY_DIRECT_KEY) "DEV-ONLY KEY" else "SECURE GATEWAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isApiKeyConfigured) {
                            if (agentPrefs.aiProvider == "openrouter") {
                                "• Tryb OpenRouter: Zapytania są kierowane do OpenRouter przy użyciu klucza.\n• Wybrany Model: ${agentPrefs.openRouterSelectedModel}"
                            } else {
                                "• Dev-Only Mode: Klucz pobierany bezpośrednio z BuildConfig / .env. Na potrzeby prototypu zapytań lokalnych.\n• Przygotowanie pod Produkcję: Docelowa architektonicznie migracja do Firebase AI & App Check proxy, aby uniknąć dystrybucji niezaszyfrowanych kluczy API na urządzeniach klienckich."
                            }
                        } else {
                            if (agentPrefs.aiProvider == "openrouter") {
                                "Klucz OpenRouter API nie jest skonfigurowany w ustawieniach. Podaj klucz powyżej i zapisz."
                            } else {
                                "GEMINI_API_KEY nie jest skonfigurowany w pliku .env. Model AI wymaga aktywnego klucza."
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isApiKeyConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    if (!apiErrorState.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ostatni błąd API: $apiErrorState",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Global AI Agent Settings Card (DataStore Preferences)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_agent_settings_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Global User Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Primary Language:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val languages = listOf("English", "Polish", "Spanish", "German")
                        languages.forEach { lang ->
                            FilterChip(
                                selected = agentPrefs.primaryLanguage == lang,
                                onClick = { viewModel.updatePrimaryLanguage(lang) },
                                label = { Text(lang) },
                                leadingIcon = {
                                    if (agentPrefs.primaryLanguage == lang) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.testTag("lang_chip_$lang")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Global Autonomy Threshold:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val levels = listOf("Autonomous", "Semi-Autonomous", "Strict Approval")
                        levels.forEach { level ->
                            FilterChip(
                                selected = agentPrefs.globalAutonomyThreshold == level,
                                onClick = { viewModel.updateGlobalAutonomyThreshold(level) },
                                label = { Text(level) },
                                modifier = Modifier.testTag("autonomy_chip_$level")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 1: Automatic Updates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automatic Updates",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Automatically sync agent status & model capabilities in background via WorkManager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.autoUpdatesEnabled,
                            onCheckedChange = { viewModel.updateAutoUpdatesEnabled(it) },
                            modifier = Modifier.testTag("auto_updates_switch")
                        )
                    }
                    if (agentPrefs.autoUpdatesEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { viewModel.triggerAgentDataSync() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_trigger_agent_data_sync_now")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Agent Data Now",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Agent Data & Models Now (WorkManager)")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Database Cleanup & Retention Policy Section (Feature 6)
                    Text(
                        text = "Automatyczne Czyszczenie i Archiwizacja Bazy (DatabaseCleanupWorker)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Konfiguracja okresu przechowywania logów i telemetrii (Retention Policy):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val retentionOptions = listOf(7, 14, 30, 90, 180)
                        retentionOptions.forEach { days ->
                            FilterChip(
                                selected = agentPrefs.retentionPolicyDays == days,
                                onClick = { viewModel.updateRetentionPolicyDays(days) },
                                label = { Text("${days}d") },
                                modifier = Modifier.testTag("retention_chip_${days}d")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automatyczna Archiwizacja",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Zapisuj wyczyszczone wpisy w lokalnym pliku plikowej archiwum JSON przed usunięciem.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.autoArchivingEnabled,
                            onCheckedChange = { viewModel.updateAutoArchivingEnabled(it) },
                            modifier = Modifier.testTag("auto_archiving_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.triggerDatabaseCleanupNow()
                            android.widget.Toast.makeText(context, "Uruchomiono DatabaseCleanupWorker w tle!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_trigger_database_cleanup_now")
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uruchom Czyszczenie i Archiwizację Bazy Teraz")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            // FTS Search navigation or quick alert
                            android.widget.Toast.makeText(context, "Przejdź do wyszukiwarki FTS5 z poziomu ekranu Analityki!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_fts_search")
                    ) {
                        Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wyszukiwarka Logów FTS5 (Pełnotekstowa)")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 2: Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Agent Notifications",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Receive system notifications when agents trigger milestone events",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.notificationsEnabled,
                            onCheckedChange = { viewModel.updateNotificationsEnabled(it) },
                            modifier = Modifier.testTag("notifications_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 3: Background Execution
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Execution",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Allow AI processes to continue running when screen is locked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.allowBackgroundExecution,
                            onCheckedChange = { viewModel.updateAllowBackgroundExecution(it) },
                            modifier = Modifier.testTag("bg_execution_switch")
                        )
                    }
                }
            }

            // Agent Communication & Inter-Agent Messaging Settings Card (DataStore Preferences)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agent_communication_settings_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Agent Communication & Mesh Toggles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 1: Agent-to-Agent Communication
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Agent-to-Agent Messaging",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Allow AI agents to directly send signals & coordinate with each other",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.allowAgentCommunication,
                            onCheckedChange = { viewModel.updateAllowAgentCommunication(it) },
                            modifier = Modifier.testTag("allow_agent_comm_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 2: Mesh Broadcasts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mesh Network Broadcasts",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Allow colony-wide system broadcasts for global status synchronization",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.allowMeshBroadcasts,
                            onCheckedChange = { viewModel.updateAllowMeshBroadcasts(it) },
                            modifier = Modifier.testTag("allow_mesh_broadcasts_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 3: Message Encryption
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "End-to-End Encryption",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Enforce cryptographic signing for all inter-agent messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.encryptAgentMessages,
                            onCheckedChange = { viewModel.updateEncryptAgentMessages(it) },
                            modifier = Modifier.testTag("encrypt_messages_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 4: Cross-Colony Remote Syncing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cross-Colony Remote Syncing",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Allow agent communication with remote peer colonies",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.allowCrossColonySync,
                            onCheckedChange = { viewModel.updateAllowCrossColonySync(it) },
                            modifier = Modifier.testTag("cross_colony_sync_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 5: Security Audit Logging
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Security Audit Logging",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Log all agent communication attempts to local audit ledger",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.logAgentCommunication,
                            onCheckedChange = { viewModel.updateLogAgentCommunication(it) },
                            modifier = Modifier.testTag("log_comm_switch")
                        )
                    }
                }
            }

            // Failover Agent Configuration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("failover_agent_config_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Konfiguracja Agenta Failover (Zastępczego)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wybierz głównego agenta, przypisz mu agenta rezerwowego oraz maksymalny progu opóźnienia response latency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val allAgentsList by viewModel.agents.collectAsState()
                    var primaryAgentId by remember { mutableStateOf<Int?>(allAgentsList.firstOrNull()?.id) }
                    var backupAgentId by remember { mutableStateOf<Int?>(null) }
                    var latencyThresholdText by remember { mutableStateOf("5000") }

                    if (allAgentsList.isNotEmpty()) {
                        Text("Główny Agent:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            allAgentsList.take(3).forEach { ag ->
                                FilterChip(
                                    selected = primaryAgentId == ag.id,
                                    onClick = { primaryAgentId = ag.id },
                                    label = { Text(ag.name) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Agent Rezerwowy (Failover):", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            FilterChip(
                                selected = backupAgentId == null,
                                onClick = { backupAgentId = null },
                                label = { Text("Brak") }
                            )
                            allAgentsList.filter { it.id != primaryAgentId }.take(3).forEach { ag ->
                                FilterChip(
                                    selected = backupAgentId == ag.id,
                                    onClick = { backupAgentId = ag.id },
                                    label = { Text(ag.name) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = latencyThresholdText,
                            onValueChange = { latencyThresholdText = it },
                            label = { Text("Próg Opóźnienia (ms)") },
                            modifier = Modifier.fillMaxWidth().testTag("failover_latency_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val primaryId = primaryAgentId
                                val threshold = latencyThresholdText.toLongOrNull() ?: 5000L
                                if (primaryId != null) {
                                    viewModel.updateAgentFailoverConfig(primaryId, backupAgentId, threshold)
                                    android.widget.Toast.makeText(context, "Zapisano konfigurację Failover!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_save_failover_config")
                        ) {
                            Text("Zapisz Konfigurację Failover")
                        }
                    } else {
                        Text("Brak aktywnych agentów do skonfigurowania.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 1. API CONNECTION PROBER & LATENCY TESTER CARD
            var isProbing by remember { mutableStateOf(false) }
            var probeResult by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()

            Card(
                modifier = Modifier.fillMaxWidth().testTag("api_prober_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Diagnostyka & Opóźnienie API",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wyślij szybkie zapytanie testowe (ping) do aktywnego dostawcy chmurowego, aby zweryfikować dostępność sieciową oraz zmierzyć czas odpowiedzi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                isProbing = true
                                probeResult = null
                                coroutineScope.launch {
                                    val startTime = System.currentTimeMillis()
                                    try {
                                        val res = com.example.network.AILlmClient.generateContent(
                                            context,
                                            prompt = "Hello. Respond in exactly one word: 'OK'."
                                        )
                                        val duration = System.currentTimeMillis() - startTime
                                        if (res.startsWith("Error") || res.contains("Connection Error") || res.contains("Error")) {
                                            probeResult = "BŁĄD: $res"
                                        } else {
                                            probeResult = "POŁĄCZONO: ${duration}ms (Model: ${if (agentPrefs.aiProvider == "openrouter") agentPrefs.openRouterSelectedModel.substringAfterLast("/") else agentPrefs.geminiSelectedModel})"
                                        }
                                    } catch (e: Exception) {
                                        probeResult = "BŁĄD: ${e.localizedMessage ?: e.message}"
                                    } finally {
                                        isProbing = false
                                    }
                                }
                            },
                            enabled = !isProbing,
                            modifier = Modifier.testTag("api_probe_button")
                        ) {
                            if (isProbing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Testowanie...")
                            } else {
                                Text("Testuj Opóźnienie")
                            }
                        }

                        if (probeResult != null) {
                            val isSuccess = probeResult?.startsWith("POŁĄCZONO") == true
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.padding(start = 8.dp).weight(1f)
                            ) {
                                Text(
                                    text = probeResult ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Brak pomiarów",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // 2. VISUAL LLM TELEMETRY & PRIVACY SENTINEL PANEL
            val telemetryList by viewModel.llmTelemetry.collectAsState()
            val totalCalls = telemetryList.size
            val successfulCalls = telemetryList.count { it.status == "SUCCESS" }
            val successRate = if (totalCalls > 0) (successfulCalls * 100) / totalCalls else 100
            val avgLatency = if (successfulCalls > 0) telemetryList.filter { it.status == "SUCCESS" }.map { it.durationMs }.average().toInt() else 0

            Card(
                modifier = Modifier.fillMaxWidth().testTag("api_telemetry_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monitor Telemetrii API Chmury",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (totalCalls > 0) {
                            IconButton(
                                onClick = { viewModel.clearLlmTelemetry() },
                                modifier = Modifier.testTag("clear_telemetry_btn").size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear logs",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats grid row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Zapytania", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("$totalCalls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Success Rate
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Skuteczność", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("$successRate%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (successRate > 80) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            }
                        }

                        // Avg Latency
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Śr. Latencja", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("${avgLatency}ms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. Dynamic Agent Latency Sparkline Canvas
                    val successfulTelemetry = remember(telemetryList) {
                        telemetryList.filter { it.status == "SUCCESS" }.take(10).reversed()
                    }
                    if (successfulTelemetry.size > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Wykres Latencji (Ostatnie 10 udanych zapytań):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            val maxLat = successfulTelemetry.maxOf { it.durationMs }.toFloat()
                            val minLat = successfulTelemetry.minOf { it.durationMs }.toFloat()
                            val diff = if (maxLat == minLat) 1f else (maxLat - minLat)
                            val pointsCount = successfulTelemetry.size
                            val stepX = size.width / (pointsCount - 1)
                            
                            val path = androidx.compose.ui.graphics.Path()
                            successfulTelemetry.forEachIndexed { index, item ->
                                val x = index * stepX
                                val ratio = (item.durationMs.toFloat() - minLat) / diff
                                val y = size.height - (ratio * (size.height - 12.dp.toPx())) - 6.dp.toPx()
                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }
                            
                            drawPath(
                                path = path,
                                color = androidx.compose.ui.graphics.Color(0xFF4CAF50), // Nice green
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 3.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                )
                            )
                            
                            successfulTelemetry.forEachIndexed { index, item ->
                                val x = index * stepX
                                val ratio = (item.durationMs.toFloat() - minLat) / diff
                                val y = size.height - (ratio * (size.height - 12.dp.toPx())) - 6.dp.toPx()
                                drawCircle(
                                    color = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    radius = 4.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ostatnie połączenia chmurowe (Log systemowy):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (telemetryList.isEmpty()) {
                        Text(
                            text = "Brak odnotowanych zapytań LLM w bazie lokalnej.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            telemetryList.take(10).forEach { item ->
                                val isSuccess = item.status == "SUCCESS"
                                val dateStr = remember(item.timestamp) {
                                    try {
                                        java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp))
                                    } catch (e: Exception) {
                                        ""
                                    }
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                                                ) {
                                                    Text(
                                                        text = item.status,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = item.provider.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = item.model.substringAfterLast("/"),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.weight(1f, fill = false),
                                                    maxLines = 1
                                                )
                                            }
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Pytanie: ${item.promptLength}zn. • Odp: ${item.responseLength}zn.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Text(
                                                text = "${item.durationMs}ms",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        if (!item.errorMessage.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.errorMessage,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Rest Periods section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("rest_period_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NightsStay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Colony Rest Period", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Switch(
                            checked = restEnabled,
                            onCheckedChange = { restEnabled = it },
                            modifier = Modifier.testTag("rest_enabled_switch")
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Define a quiet-hours window where all active agents will automatically enter 'Idle/Paused' state to save battery and limit notification usage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (restEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Quiet Period Schedule:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Start Time Selection
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Start Time:", style = MaterialTheme.typography.labelMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Start Hour Dropdown
                                    Box {
                                        Button(onClick = { showStartHourMenu = true }, modifier = Modifier.testTag("start_hour_btn")) {
                                            Text(String.format(Locale.getDefault(), "%02d", startHour))
                                        }
                                        DropdownMenu(expanded = showStartHourMenu, onDismissRequest = { showStartHourMenu = false }) {
                                            (0..23).forEach { hour ->
                                                DropdownMenuItem(
                                                    text = { Text(String.format(Locale.getDefault(), "%02d", hour)) },
                                                    onClick = {
                                                        startHour = hour
                                                        showStartHourMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                                    // Start Minute Dropdown
                                    Box {
                                        Button(onClick = { showStartMinMenu = true }, modifier = Modifier.testTag("start_min_btn")) {
                                            Text(String.format(Locale.getDefault(), "%02d", startMinute))
                                        }
                                        DropdownMenu(expanded = showStartMinMenu, onDismissRequest = { showStartMinMenu = false }) {
                                            listOf(0, 15, 30, 45).forEach { min ->
                                                DropdownMenuItem(
                                                    text = { Text(String.format(Locale.getDefault(), "%02d", min)) },
                                                    onClick = {
                                                        startMinute = min
                                                        showStartMinMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // End Time Selection
                            Column(modifier = Modifier.weight(1f)) {
                                Text("End Time:", style = MaterialTheme.typography.labelMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // End Hour Dropdown
                                    Box {
                                        Button(onClick = { showEndHourMenu = true }, modifier = Modifier.testTag("end_hour_btn")) {
                                            Text(String.format(Locale.getDefault(), "%02d", endHour))
                                        }
                                        DropdownMenu(expanded = showEndHourMenu, onDismissRequest = { showEndHourMenu = false }) {
                                            (0..23).forEach { hour ->
                                                DropdownMenuItem(
                                                    text = { Text(String.format(Locale.getDefault(), "%02d", hour)) },
                                                    onClick = {
                                                        endHour = hour
                                                        showEndHourMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                                    // End Minute Dropdown
                                    Box {
                                        Button(onClick = { showEndMinMenu = true }, modifier = Modifier.testTag("end_min_btn")) {
                                            Text(String.format(Locale.getDefault(), "%02d", endMinute))
                                        }
                                        DropdownMenu(expanded = showEndMinMenu, onDismissRequest = { showEndMinMenu = false }) {
                                            listOf(0, 15, 30, 45).forEach { min ->
                                                DropdownMenuItem(
                                                    text = { Text(String.format(Locale.getDefault(), "%02d", min)) },
                                                    onClick = {
                                                        endMinute = min
                                                        showEndMinMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            prefs.edit()
                                .putBoolean("rest_enabled", restEnabled)
                                .putInt("rest_start_hour", startHour)
                                .putInt("rest_start_minute", startMinute)
                                .putInt("rest_end_hour", endHour)
                                .putInt("rest_end_minute", endMinute)
                                .apply()
                            
                            viewModel.applyRestPeriods()
                            android.widget.Toast.makeText(context, "Rest Period settings saved!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_rest_settings_btn")
                    ) {
                        Text("Save Schedule Settings")
                    }
                }
            }
            
            // Agent Workload Rest Scheduler Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("scheduler_settings_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agent Workload Rest Scheduler", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Switch(
                            checked = schedulerEnabled,
                            onCheckedChange = { schedulerEnabled = it },
                            modifier = Modifier.testTag("scheduler_enabled_switch")
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Automatically monitors active tasks and transitions overworked agents into a 'Resting' state. It prevents task assignment and notifies you.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (schedulerEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Workload Threshold Adjustment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Workload Threshold", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Max active tasks before rest", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (workloadThreshold > 1) workloadThreshold-- },
                                    modifier = Modifier.testTag("decrease_threshold_btn")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                Text(
                                    text = workloadThreshold.toString(),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 8.dp).testTag("threshold_value_text")
                                )
                                IconButton(
                                    onClick = { if (workloadThreshold < 10) workloadThreshold++ },
                                    modifier = Modifier.testTag("increase_threshold_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Rest Duration Adjustment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Rest Duration", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Duration of resting period", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (restDurationSeconds > 10) restDurationSeconds -= 10 },
                                    modifier = Modifier.testTag("decrease_duration_btn")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                Text(
                                    text = "${restDurationSeconds}s",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 8.dp).testTag("duration_value_text")
                                )
                                IconButton(
                                    onClick = { if (restDurationSeconds < 300) restDurationSeconds += 10 },
                                    modifier = Modifier.testTag("increase_duration_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            prefs.edit()
                                .putBoolean("scheduler_enabled", schedulerEnabled)
                                .putInt("workload_threshold", workloadThreshold)
                                .putInt("rest_duration_seconds", restDurationSeconds)
                                .apply()
                            
                            android.widget.Toast.makeText(context, "Scheduler settings saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_scheduler_settings_btn")
                    ) {
                        Text("Save Workload Scheduler Settings")
                    }
                }
            }
            
            // Chart Styling & Trend Alerts Section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("chart_settings_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chart Visuals & Trend Threshold Alerts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Customize chart color contrast modes and set automated notifications for significant data trend shifts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Color Theme Preference
                    Text("Chart Color Theme", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    ChartColorThemeSelector(
                        selectedTheme = agentPrefs.chartColorIntensity,
                        onThemeSelected = { viewModel.updateChartColorIntensity(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Trend Alerts Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Trend Shift Notifications", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Receive system alerts when interaction volume or activity shifts significantly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = agentPrefs.trendAlertsEnabled,
                            onCheckedChange = { viewModel.updateTrendAlertsEnabled(it) },
                            modifier = Modifier.testTag("trend_alerts_toggle_switch")
                        )
                    }

                    if (agentPrefs.trendAlertsEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Notification Threshold: ±${agentPrefs.trendAlertThreshold}%",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = agentPrefs.trendAlertThreshold.toFloat(),
                            onValueChange = { viewModel.updateTrendAlertThreshold(it.toInt()) },
                            valueRange = 5f..50f,
                            steps = 8,
                            modifier = Modifier.testTag("settings_trend_threshold_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10, 15, 20, 30, 50).forEach { preset ->
                                FilterChip(
                                    selected = agentPrefs.trendAlertThreshold == preset,
                                    onClick = { viewModel.updateTrendAlertThreshold(preset) },
                                    label = { Text("$preset%") }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Integration & Sandbox section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("calendar_section_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calendar Events Sandbox", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        
                        IconButton(
                            onClick = { viewModel.clearCalendarEvents() },
                            modifier = Modifier.testTag("clear_calendar_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All Events", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Text(
                        "Manage calendar events to test real-time agent status prediction changes triggered by your schedule.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (calendarEvents.isEmpty()) {
                        Text(
                            "No upcoming events registered.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        calendarEvents.forEach { event ->
                            val startTimeStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(event.startTime))
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(event.description, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(startTimeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            Text("Target: ${event.agentName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { showAddEventDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag("add_calendar_event_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Calendar Event")
                    }
                }
    }
    
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("Add Calendar Event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newEventTitle,
                        onValueChange = { newEventTitle = it },
                        label = { Text("Event Title") },
                        modifier = Modifier.fillMaxWidth().testTag("event_title_input")
                    )
                    OutlinedTextField(
                        value = newEventDesc,
                        onValueChange = { newEventDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().testTag("event_desc_input")
                    )
                    
                    Text("Target Agent (to analyze):", style = MaterialTheme.typography.labelMedium)
                    // Display current agents list as selection
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        agents.forEach { agent ->
                            val isSelected = newEventAgent == agent.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { newEventAgent = agent.name },
                                label = { Text(agent.name) }
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = newEventOffsetMins,
                        onValueChange = { newEventOffsetMins = it },
                        label = { Text("Starts in (Minutes from now)") },
                        modifier = Modifier.fillMaxWidth().testTag("event_offset_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val offsetMins = newEventOffsetMins.toLongOrNull() ?: 30L
                        val start = System.currentTimeMillis() + offsetMins * 60 * 1000L
                        val end = start + 60 * 60 * 1000L // 1 hour duration
                        if (newEventTitle.isNotBlank() && newEventAgent.isNotBlank()) {
                            viewModel.addCalendarEvent(
                                title = newEventTitle,
                                description = newEventDesc,
                                startTime = start,
                                endTime = end,
                                agentName = newEventAgent
                            )
                            showAddEventDialog = false
                            newEventTitle = ""
                            newEventDesc = ""
                            newEventAgent = ""
                            newEventOffsetMins = "30"
                            android.widget.Toast.makeText(context, "Event added!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Please fill in all fields", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("dialog_add_event_confirm")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}
