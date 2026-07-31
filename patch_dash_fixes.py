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
"""                            onPauseToggle = { viewModel.toggleAgentStatus(agent) },
                            onDelete = { viewModel.deleteAgent(agent.id) }
                        )""",
"""                            onPauseToggle = { viewModel.toggleAgentStatus(agent) },
                            onDelete = { viewModel.deleteAgent(agent.id) },
                            onExport = {
                                exportSelectedAgents(context, listOf(agent)) { path, json ->
                                    exportFilePath = path
                                    exportJsonContent = json
                                    showExportDialog = true
                                }
                            }
                        )"""
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
            Column(
                modifier = Modifier""",
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
            Column(
                modifier = Modifier"""
    )
]

patch_file("app/src/main/java/com/example/ui/DashboardScreen.kt", dash_replacements)

