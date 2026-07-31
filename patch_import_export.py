import re

def patch_file(filepath, replacements):
    with open(filepath, "r") as f:
        content = f.read()
    
    for old, new in replacements:
        if old in content:
            content = content.replace(old, new)
        else:
            print(f"Warning: could not find target in {filepath}:\n{old}")
            
    with open(filepath, "w") as f:
        f.write(content)

dash_replacements = [
    (
"""    val sleepRecords by viewModel.sleepRecords.collectAsState()
    val transactions by viewModel.financeTransactions.collectAsState()
    
    val preferences by viewModel.agentPreferencesState.collectAsState()""",
"""    val sleepRecords by viewModel.sleepRecords.collectAsState()
    val transactions by viewModel.financeTransactions.collectAsState()
    
    val preferences by viewModel.agentPreferencesState.collectAsState()
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                        val jsonContent = reader.readText()
                        if (jsonContent.trim().startsWith("[")) {
                            val jsonArray = org.json.JSONArray(jsonContent)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                viewModel.addAgent(
                                    name = obj.getString("name"),
                                    type = obj.getString("type"),
                                    role = obj.getString("role"),
                                    permissions = obj.optString("permissions", "Basic"),
                                    traits = obj.optString("traits", ""),
                                    systemPrompt = obj.optString("systemPrompt", "")
                                )
                            }
                        } else if (jsonContent.trim().startsWith("{")) {
                            val obj = org.json.JSONObject(jsonContent)
                            viewModel.addAgent(
                                name = obj.getString("name"),
                                type = obj.getString("type"),
                                role = obj.getString("role"),
                                permissions = obj.optString("permissions", "Basic"),
                                traits = obj.optString("traits", ""),
                                systemPrompt = obj.optString("systemPrompt", "")
                            )
                        }
                        android.widget.Toast.makeText(context, "Agent(s) imported successfully", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Failed to import agents", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }"""
    ),
    (
"""                    FloatingActionButton(
                        onClick = { showVoiceDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("fab_voice_commands")
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Asystent głosowy")
                    }""",
"""                    FloatingActionButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("fab_import_agent")
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Import Agent")
                    }
                    FloatingActionButton(
                        onClick = { showVoiceDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("fab_voice_commands")
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Asystent głosowy")
                    }"""
    ),
    (
"""    subTasks: List<SubTask> = emptyList(),
    onSelectToggle: () -> Unit,
    onTap: () -> Unit,
    onPauseToggle: () -> Unit,
    onDelete: () -> Unit
) {""",
"""    subTasks: List<SubTask> = emptyList(),
    onSelectToggle: () -> Unit,
    onTap: () -> Unit,
    onPauseToggle: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit = {}
) {"""
    ),
    (
"""                DropdownMenuItem(
                    text = { Text("Remove Agent") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
            Column(""",
"""                DropdownMenuItem(
                    text = { Text("Remove Agent") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export Agent") },
                    onClick = {
                        onExport()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null
                        )
                    }
                )
            }
            Column("""
    ),
    (
"""                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAgentDetailsId = agent.id 
                            },
                            onPauseToggle = {
                                if (agent.status == "Active") {
                                    viewModel.updateAgentStatus(agent.id, "Paused")
                                } else {
                                    viewModel.updateAgentStatus(agent.id, "Active")
                                }
                            },
                            onDelete = {
                                viewModel.deleteAgent(agent.id)
                            }
                        )
                    }""",
"""                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAgentDetailsId = agent.id 
                            },
                            onPauseToggle = {
                                if (agent.status == "Active") {
                                    viewModel.updateAgentStatus(agent.id, "Paused")
                                } else {
                                    viewModel.updateAgentStatus(agent.id, "Active")
                                }
                            },
                            onDelete = {
                                viewModel.deleteAgent(agent.id)
                            },
                            onExport = {
                                exportSelectedAgents(context, listOf(agent)) { path, json ->
                                    exportFilePath = path
                                    exportJsonContent = json
                                    showExportDialog = true
                                }
                            }
                        )
                    }"""
    )
]

patch_file("app/src/main/java/com/example/ui/DashboardScreen.kt", dash_replacements)

