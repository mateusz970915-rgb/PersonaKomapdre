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
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.BuildConfig
import com.example.data.Agent
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.viewmodel.ColonyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedAgentsScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // UI states
    val selectedApps = remember { mutableStateListOf("Notion", "Gmail", "Spotify") }
    val selectedCategories = remember { mutableStateListOf("Productivity", "Health & Fitness") }
    
    var isLoading by remember { mutableStateOf(false) }
    var recommendations = remember { mutableStateListOf<Agent>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val appOptions = listOf("Slack", "Figma", "Spotify", "GitHub", "Notion", "Google Calendar", "Gmail", "Duolingo", "Strava")
    val categoryOptions = listOf("Productivity", "Personal Finance", "Health & Fitness", "Smart Home", "Study & Education")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suggested Agents") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("suggested_back_button")) {
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI Agent Discovery",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Gemini will analyze your app usage and lifestyle categories to recommend the ultimate personalized agents for your colony.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Text("Your Most Used Apps", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                appOptions.forEach { app ->
                    val isSelected = selectedApps.contains(app)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedApps.remove(app) else selectedApps.add(app)
                        },
                        label = { Text(app) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.testTag("app_chip_$app")
                    )
                }
            }

            Text("Daily Life Focus Categories", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryOptions.forEach { cat ->
                    val isSelected = selectedCategories.contains(cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedCategories.remove(cat) else selectedCategories.add(cat)
                        },
                        label = { Text(cat) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }
            
            Button(
                onClick = {
                    if (!viewModel.isApiKeyConfigured) {
                        errorMessage = "Active AI provider's API key is not configured in settings."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    recommendations.clear()
                    
                    coroutineScope.launch {
                        try {
                            val appsList = selectedApps.joinToString(", ")
                            val catsList = selectedCategories.joinToString(", ")
                            val prompt = """
                                Recommend exactly 3 custom, highly specialized personal AI Agent archetypes designed to assist a user who frequently uses these apps: [$appsList] and focuses on these life categories: [$catsList].
                                For each agent, suggest:
                                1. A catchy name (e.g., 'Notion Architect' or 'Duolingo Motivator')
                                2. Type/Category (e.g., 'Productivity' or 'Education')
                                3. A concise but powerful role description
                                4. Required Android permissions (comma-separated, e.g., 'Notifications, Calendar')
                                5. 3 personality traits (comma-separated, e.g., 'Organized, Direct, Inquisitive')
                                6. An elegant, 1-sentence systemPrompt instruction.
                                
                                Return ONLY a valid JSON array of objects with keys: "name", "type", "role", "permissions", "traits", "systemPrompt". 
                                Do not wrap with any markdown block backticks or 'json' headers. Just output the clean JSON text.
                            """.trimIndent()
                            
                            val systemInstructionText = "You are a specialized agent generator helping users optimize their phone colony."
                            
                            val text = com.example.network.AILlmClient.generateContent(context, prompt, systemInstructionText)
                            val cleanedJson = text.substringAfter("```json").substringAfter("```").substringBefore("```").trim()
                            
                            val jsonArray = JSONArray(cleanedJson.ifBlank { text })
                            val list = mutableListOf<Agent>()
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                list.add(
                                    Agent(
                                        name = obj.getString("name"),
                                        type = obj.getString("type"),
                                        role = obj.getString("role"),
                                        permissions = obj.optString("permissions", "Basic"),
                                        traits = obj.optString("traits", "Focused"),
                                        systemPrompt = obj.optString("systemPrompt", "Help user thrive."),
                                        iconName = obj.optString("type", "Productivity").lowercase()
                                    )
                                )
                            }
                            recommendations.addAll(list)
                        } catch (e: Exception) {
                            errorMessage = "Failed to fetch suggestions: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_suggestions_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Archetypes")
                }
            }
            
            errorMessage?.let { err ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (recommendations.isNotEmpty()) {
                Text(
                    "Suggested Agent Archetypes",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                recommendations.forEach { agent ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("suggested_agent_card_${agent.name}"),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val suggestedMood = remember(agent) { com.example.data.calculateAgentMood(agent) }
                                    Text("${agent.name} ${suggestedMood.emoji}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = agent.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("•", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = suggestedMood.moodTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        viewModel.addAgent(agent)
                                        android.widget.Toast.makeText(context, "${agent.name} added to your colony!", android.widget.Toast.LENGTH_SHORT).show()
                                        recommendations.remove(agent)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("add_suggested_btn_${agent.name}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Role:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text(agent.role, style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Traits:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text(agent.traits, style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("System Prompt:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text(agent.systemPrompt, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
