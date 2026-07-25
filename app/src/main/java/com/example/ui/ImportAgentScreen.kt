package com.example.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Agent
import com.example.viewmodel.ColonyViewModel
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportAgentScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    var jsonInput by remember { mutableStateOf("") }
    var parsedAgent by remember { mutableStateOf<Agent?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }
    var isVerified by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Agent") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Paste Agent Profile JSON",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = jsonInput,
                onValueChange = { jsonInput = it; parsedAgent = null; isVerified = false; parseError = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = {
                    Text("{\n  \"name\": \"Focus Agent\",\n  \"type\": \"Productivity\",\n  \"role\": \"Blocks distractions\",\n  \"permissions\": \"Usage Stats, Notifications\",\n  \"iconName\": \"focus\"\n}")
                },
                isError = parseError != null
            )
            if (parseError != null) {
                Text(
                    text = parseError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = {
                    try {
                        val json = org.json.JSONObject(jsonInput)
                        val name = json.getString("name")
                        val type = json.getString("type")
                        val role = json.getString("role")
                        val permissions = json.getString("permissions")
                        val iconName = json.getString("iconName")
                        val systemPrompt = json.optString("systemPrompt", "")
                        
                        // Sandbox Policy: Security Validation
                        val lowerPrompt = systemPrompt.lowercase() + role.lowercase()
                        if (lowerPrompt.contains("ignore") || lowerPrompt.contains("bypass") || lowerPrompt.contains("jailbreak")) {
                            throw Exception("Prompt Injection Detected! Malicious keywords found.")
                        }
                        
                        val allowedPermissions = listOf("basic", "calendar", "notifications", "internet", "contacts", "usage stats")
                        val requestedPerms = permissions.lowercase().split(",").map { it.trim() }
                        for (p in requestedPerms) {
                            if (!allowedPermissions.any { p.contains(it) }) {
                                throw Exception("Permission '$p' is not supported by the host application.")
                            }
                        }

                        parsedAgent = com.example.data.Agent(
                            name = name,
                            type = type,
                            role = role,
                            permissions = permissions,
                            iconName = iconName,
                            systemPrompt = systemPrompt,
                            status = "Quarantined" // P3: Sandbox Policy Quarantine
                        )
                        parseError = null
                        isVerified = false
                    } catch (e: Exception) {
                        parsedAgent = null
                        parseError = "Validation Failed: ${e.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Validate JSON")
            }

            if (parsedAgent != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    "Agent Security Sandbox",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isVerified) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Name: ${parsedAgent?.name}", fontWeight = FontWeight.Bold)
                        Text("Role: ${parsedAgent?.role}")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (isVerified) Icons.Filled.Security else Icons.Filled.Warning,
                                contentDescription = "Security Status",
                                tint = if (isVerified) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )
                            Text(
                                if (isVerified) "Permissions Verified" else "Requested OS Permissions:",
                                fontWeight = FontWeight.Bold,
                                color = if (isVerified) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = parsedAgent?.permissions ?: "None",
                            color = if (isVerified) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        if (!isVerified) {
                            Text(
                                "WARNING: This agent is requesting hardware/system permissions. In reality, Android prevents side-loading permissions dynamically.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!isVerified) {
                    Button(
                        onClick = {
                            isVerified = true
                            parsedAgent = parsedAgent?.copy(status = "Active")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = "Verify", modifier = Modifier.padding(end = 8.dp))
                        Text("Acknowledge & Verify Security")
                    }
                } else {
                    Button(
                        onClick = {
                            parsedAgent?.let { agent ->
                                viewModel.addAgent(agent)
                                viewModel.logDataAccess(
                                    agentName = agent.name,
                                    dataType = "Permissions: ${agent.permissions}",
                                    isViolation = false,
                                    reason = "Agent passed security sandbox & imported via JSON"
                                )
                                onNavigateToDashboard()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Approve", modifier = Modifier.padding(end = 8.dp))
                        Text("Add to Colony")
                    }
                }
            }
        }
    }
}
