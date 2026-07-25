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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                                text = if (isApiKeyConfigured) "Gemini API: Connected" else "Gemini API Key Missing",
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
                        text = if (isApiKeyConfigured)
                            "• Dev-Only Mode: Klucz pobierany bezpośrednio z BuildConfig / .env. Na potrzeby prototypu zapytań lokalnych.\n• Przygotowanie pod Produkcję: Docelowa architektonicznie migracja do Firebase AI & App Check proxy, aby uniknąć dystrybucji niezaszyfrowanych kluczy API na urządzeniach klienckich."
                        else
                            "GEMINI_API_KEY nie jest skonfigurowany w pliku .env. Model AI wymaga aktywnego klucza.",
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
