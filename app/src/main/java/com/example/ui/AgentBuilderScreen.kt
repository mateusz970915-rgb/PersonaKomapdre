package com.example.ui

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomAgentDefinition
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentBuilderScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val customAgents by viewModel.customAgentDefinitions.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    
    val availableTools = listOf("Web Scraper", "Finance Tracker", "Calendar Intel", "Knowledge Graph", "Email Sender")
    val selectedTools = remember { mutableStateMapOf<String, Boolean>().apply {
        availableTools.forEach { put(it, false) }
    }}
    
    var selectedAutonomy by remember { mutableStateOf("Medium") }
    val autonomyLevels = listOf("Low", "Medium", "High", "Full Autonomous")
    
    var snackbarHostState = remember { SnackbarHostState() }
    var scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Autonomous Agent Builder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("agent_builder_back_btn")) {
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Left Column: Builder Form
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "Zaprojektuj Nowego Agenta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nazwa Agenta") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("agent_name_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.SmartToy, contentDescription = null) }
                        )
                    }
                    
                    item {
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            label = { Text("System Prompt (Rola i Cel)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("agent_prompt_input"),
                            maxLines = 5,
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                        )
                    }
                    
                    item {
                        Text(
                            "Temperatura LLM: ${"%.2f".format(temperature)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            valueRange = 0.0f..1.5f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("agent_temp_slider")
                        )
                    }
                    
                    item {
                        Text(
                            "Dostęp do Narzędzi (Tool Access)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        availableTools.forEach { tool ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedTools[tool] ?: false,
                                    onCheckedChange = { selectedTools[tool] = it },
                                    modifier = Modifier.testTag("tool_checkbox_$tool")
                                )
                                Text(tool, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    
                    item {
                        Text(
                            "Poziom Autonomii",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        autonomyLevels.forEach { level ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedAutonomy == level,
                                    onClick = { selectedAutonomy = level },
                                    modifier = Modifier.testTag("autonomy_radio_$level")
                                )
                                Text(level, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    
                    item {
                        Button(
                            onClick = {
                                if (name.isNotBlank() && prompt.isNotBlank()) {
                                    val toolsList = selectedTools.filter { it.value }.keys.joinToString(", ")
                                    val definition = CustomAgentDefinition(
                                        name = name,
                                        systemPrompt = prompt,
                                        temperature = temperature.toDouble(),
                                        toolsAccess = if (toolsList.isEmpty()) "Brak narzędzi" else toolsList,
                                        autonomyLevel = selectedAutonomy
                                    )
                                    viewModel.insertCustomAgentDefinition(definition)
                                    viewModel.insertMemory(
                                        com.example.data.ColonyMemory(
                                            content = "[Agent Builder] Utworzono nowego agenta autonomicznego o nazwie '$name' (Autonomia: $selectedAutonomy, Temp: ${"%.2f".format(temperature)})."
                                        )
                                    )
                                    
                                    // Reset fields
                                    name = ""
                                    prompt = ""
                                    temperature = 0.7f
                                    availableTools.forEach { selectedTools[it] = false }
                                    selectedAutonomy = "Medium"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_agent_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wdróż Agenta do Kolonii", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Right Column: Active Custom Agents list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text(
                    "Wdrożone Agenty (${customAgents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (customAgents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Brak customowych agentów", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(customAgents) { agent ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_agent_card_${agent.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(agent.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteCustomAgentDefinition(agent.id) },
                                            modifier = Modifier.testTag("delete_agent_btn_${agent.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    
                                    Text("System Prompt:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                                    Text(agent.systemPrompt, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Temp: ${agent.temperature}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                        Text("Autonomia: ${agent.autonomyLevel}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Narzędzia: ${agent.toolsAccess}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
