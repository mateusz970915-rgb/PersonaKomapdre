package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.viewmodel.ColonyViewModel

private val COLOR_PALETTE = listOf(
    "#10B981" to "Emerald",
    "#3B82F6" to "Royal Blue",
    "#8B5CF6" to "Violet",
    "#EF4444" to "Crimson",
    "#F59E0B" to "Amber",
    "#06B6D4" to "Cyan",
    "#6366F1" to "Indigo",
    "#EC4899" to "Pink",
    "#14B8A6" to "Teal",
    "#64748B" to "Slate"
)

private val AGENT_CATEGORIES = listOf(
    "HEALTH", "FINANCE", "WORK", "SECURITY", "CREATIVE", "ANALYTICS", "GOVERNANCE", "GENERAL"
)

private val COMMUNICATION_STYLES = listOf(
    "Formal & Precise",
    "Concise & Direct",
    "Empathetic & Supportive",
    "Technical & Detailed",
    "Casual & Friendly"
)

private val AUTONOMY_LEVELS = listOf(
    "Needs Confirmation",
    "Semi-Autonomous",
    "Full Autonomous"
)

private val PERMISSION_OPTIONS = listOf(
    "Basic",
    "Read Calendar",
    "Read Contacts",
    "Sensors & Notifications",
    "Full Access"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiStepCreateAgentDialog(
    viewModel: ColonyViewModel,
    onDismiss: () -> Unit,
    onAgentCreated: () -> Unit = {}
) {
    var currentStep by remember { mutableIntStateOf(1) } // 1..4

    // Form State
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("WORK") }
    var role by remember { mutableStateOf("") }
    var personaTraits by remember { mutableStateOf("") }

    var primaryGoal by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var autonomyLevel by remember { mutableStateOf("Needs Confirmation") }

    var communicationStyle by remember { mutableStateOf("Concise & Direct") }
    var selectedPermissions by remember { mutableStateOf(setOf("Basic")) }
    
    val categoryColors by viewModel.categoryColors.collectAsState()
    var categoryColorHex by remember(category, categoryColors) {
        mutableStateOf(categoryColors[category.uppercase()] ?: "#8B5CF6")
    }

    val parsedColor = remember(categoryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(categoryColorHex))
        } catch (e: Exception) {
            Color(0xFF8B5CF6)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(12.dp)
                .testTag("multi_step_create_agent_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(parsedColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = parsedColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Create AI Agent Wizard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Step $currentStep of 4: " + when(currentStep) {
                                    1 -> "Persona & Identity"
                                    2 -> "Mission & Autonomy"
                                    3 -> "Style & Color Theme"
                                    else -> "Confirm & Deploy"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_agent_wizard_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step Progress Indicator
                LinearProgressIndicator(
                    progress = { currentStep / 4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = parsedColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentStep) {
                        1 -> Step1PersonaIdentity(
                            name = name,
                            onNameChange = { name = it },
                            category = category,
                            onCategoryChange = {
                                category = it
                                categoryColorHex = viewModel.getCategoryColorHex(it)
                            },
                            role = role,
                            onRoleChange = { role = it },
                            personaTraits = personaTraits,
                            onPersonaTraitsChange = { personaTraits = it },
                            categoryColor = parsedColor
                        )
                        2 -> Step2GoalsAutonomy(
                            primaryGoal = primaryGoal,
                            onPrimaryGoalChange = { primaryGoal = it },
                            systemPrompt = systemPrompt,
                            onSystemPromptChange = { systemPrompt = it },
                            autonomyLevel = autonomyLevel,
                            onAutonomyLevelChange = { autonomyLevel = it },
                            categoryColor = parsedColor
                        )
                        3 -> Step3StyleAndColorTheme(
                            communicationStyle = communicationStyle,
                            onCommunicationStyleChange = { communicationStyle = it },
                            selectedPermissions = selectedPermissions,
                            onPermissionsChange = { selectedPermissions = it },
                            category = category,
                            selectedColorHex = categoryColorHex,
                            onColorSelected = { hex ->
                                categoryColorHex = hex
                                viewModel.updateCategoryColor(category, hex)
                            },
                            categoryColor = parsedColor
                        )
                        4 -> Step4ConfirmationPreview(
                            name = name,
                            category = category,
                            role = role,
                            personaTraits = personaTraits,
                            primaryGoal = primaryGoal,
                            systemPrompt = systemPrompt,
                            autonomyLevel = autonomyLevel,
                            communicationStyle = communicationStyle,
                            selectedPermissions = selectedPermissions,
                            categoryColor = parsedColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.testTag("wizard_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (currentStep < 4) {
                        Button(
                            onClick = { currentStep++ },
                            enabled = when (currentStep) {
                                1 -> name.isNotBlank() && role.isNotBlank()
                                2 -> primaryGoal.isNotBlank()
                                else -> true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = parsedColor),
                            modifier = Modifier.testTag("wizard_next_btn")
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = {
                                val combinedTraits = buildString {
                                    append("Persona: ").append(personaTraits.ifBlank { "Specialized AI Assistant" })
                                    append(" | Style: ").append(communicationStyle)
                                }
                                val fullSystemPrompt = buildString {
                                    append("Goal: ").append(primaryGoal)
                                    if (systemPrompt.isNotBlank()) {
                                        append("\nDirectives: ").append(systemPrompt)
                                    }
                                }
                                val permissionsStr = selectedPermissions.joinToString(", ")

                                viewModel.addAgent(
                                    name = name.trim(),
                                    type = category.uppercase().trim(),
                                    role = role.trim(),
                                    permissions = permissionsStr,
                                    iconName = "smart_toy",
                                    traits = combinedTraits,
                                    systemPrompt = fullSystemPrompt,
                                    autonomyLevel = autonomyLevel
                                )

                                viewModel.updateCategoryColor(category, categoryColorHex)
                                viewModel.addMemory("Created new AI Agent '${name.trim()}' with category $category and custom persona.")

                                onAgentCreated()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = parsedColor),
                            modifier = Modifier.testTag("wizard_deploy_agent_btn")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Agent to Room DB", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step1PersonaIdentity(
    name: String,
    onNameChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    personaTraits: String,
    onPersonaTraitsChange: (String) -> Unit,
    categoryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Step 1: Define Persona & Identity",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = categoryColor
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Agent Name *") },
            placeholder = { Text("e.g. Health Guardian, Budget Sentinel, Focus Dispatcher") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("agent_name_input"),
            singleLine = true
        )

        Text(
            text = "Agent Category / Specialty",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AGENT_CATEGORIES.forEach { cat ->
                FilterChip(
                    selected = category.equals(cat, ignoreCase = true),
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat) },
                    modifier = Modifier.testTag("category_chip_$cat")
                )
            }
        }

        OutlinedTextField(
            value = role,
            onValueChange = onRoleChange,
            label = { Text("Role Title *") },
            placeholder = { Text("e.g. Vitals & Sleep Advisor, Spending Auditor") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("agent_role_input"),
            singleLine = true
        )

        OutlinedTextField(
            value = personaTraits,
            onValueChange = onPersonaTraitsChange,
            label = { Text("Persona & Behavioral Traits") },
            placeholder = { Text("e.g. Analytical, risk-averse, proactive, encouraging...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("agent_traits_input"),
            maxLines = 4
        )
    }
}

@Composable
private fun Step2GoalsAutonomy(
    primaryGoal: String,
    onPrimaryGoalChange: (String) -> Unit,
    systemPrompt: String,
    onSystemPromptChange: (String) -> Unit,
    autonomyLevel: String,
    onAutonomyLevelChange: (String) -> Unit,
    categoryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Step 2: Mission Goals & System Directives",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = categoryColor
        )

        OutlinedTextField(
            value = primaryGoal,
            onValueChange = onPrimaryGoalChange,
            label = { Text("Primary Mission Goal *") },
            placeholder = { Text("e.g. Monitor sleep patterns and ensure optimal circadian recovery") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("agent_goal_input"),
            singleLine = true
        )

        OutlinedTextField(
            value = systemPrompt,
            onValueChange = onSystemPromptChange,
            label = { Text("System Directives / Prompt Rules") },
            placeholder = { Text("e.g. Always verify permissions before accessing calendar; alert user if rest hours are violated.") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("agent_prompt_input"),
            maxLines = 4
        )

        Text(
            text = "Autonomy Level",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AUTONOMY_LEVELS.forEach { level ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (autonomyLevel == level) categoryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAutonomyLevelChange(level) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("autonomy_radio_$level")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = level,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when(level) {
                                    "Needs Confirmation" -> "Prompts user before executing any subtask action."
                                    "Semi-Autonomous" -> "Executes safe background tasks; asks for sensitive actions."
                                    else -> "Fully autonomous agent workflow execution."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(
                            selected = autonomyLevel == level,
                            onClick = { onAutonomyLevelChange(level) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3StyleAndColorTheme(
    communicationStyle: String,
    onCommunicationStyleChange: (String) -> Unit,
    selectedPermissions: Set<String>,
    onPermissionsChange: (Set<String>) -> Unit,
    category: String,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    categoryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Step 3: Communication Style & Dynamic Category Color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = categoryColor
        )

        Text(
            text = "Communication Style",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            COMMUNICATION_STYLES.forEach { style ->
                FilterChip(
                    selected = communicationStyle == style,
                    onClick = { onCommunicationStyleChange(style) },
                    label = { Text(style) },
                    modifier = Modifier.testTag("style_chip_$style")
                )
            }
        }

        Text(
            text = "Granted System Permissions",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PERMISSION_OPTIONS.forEach { perm ->
                val isSelected = selectedPermissions.contains(perm)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSet = selectedPermissions.toMutableSet()
                        if (isSelected) newSet.remove(perm) else newSet.add(perm)
                        if (newSet.isEmpty()) newSet.add("Basic")
                        onPermissionsChange(newSet)
                    },
                    label = { Text(perm) },
                    modifier = Modifier.testTag("permission_chip_$perm")
                )
            }
        }

        HorizontalDivider()

        // Dynamic Color Scheme Picker
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = categoryColor)
            Text(
                text = "Assign Dashboard Color for Category: '$category'",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Select a custom color to highlight all agents under '$category' on the Dashboard:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            COLOR_PALETTE.forEach { (hex, colorName) ->
                val parsed = try {
                    Color(android.graphics.Color.parseColor(hex))
                } catch (e: Exception) {
                    Color.Gray
                }
                val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(parsed)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) }
                        .testTag("color_picker_$hex"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = colorName,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step4ConfirmationPreview(
    name: String,
    category: String,
    role: String,
    personaTraits: String,
    primaryGoal: String,
    systemPrompt: String,
    autonomyLevel: String,
    communicationStyle: String,
    selectedPermissions: Set<String>,
    categoryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Step 4: Review Agent Deployment Card",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = categoryColor
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(2.dp, categoryColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("agent_wizard_preview_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(categoryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = name.ifBlank { "Unnamed Agent" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = role.ifBlank { "Specialist" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Primary Goal: $primaryGoal",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                if (personaTraits.isNotBlank()) {
                    Text(
                        text = "Persona Traits: $personaTraits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Style: $communicationStyle",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Autonomy: $autonomyLevel",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Permissions: " + selectedPermissions.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
