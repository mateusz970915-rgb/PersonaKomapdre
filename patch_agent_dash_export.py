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

agent_dash_replacements = [
    (
"""    isSelected: Boolean,
    isSelectionMode: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {""",
"""    isSelected: Boolean,
    isSelectionMode: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onExport: () -> Unit = {}
) {"""
    ),
    (
"""                        if (!isSelectionMode) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }""",
"""            if (!isSelectionMode) {
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(
                        onClick = onExport,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Agent",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }"""
    ),
    (
"""                        AgentCard(
                            agent = agent,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onDelete = { viewModel.deleteAgent(agent.id) },
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedAgentIds -= agent.id
                                    else selectedAgentIds += agent.id
                                } else {
                                    agentToEdit = agent
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedAgentIds += agent.id
                                }
                            }
                        )""",
"""                        AgentCard(
                            agent = agent,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onDelete = { viewModel.deleteAgent(agent.id) },
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedAgentIds -= agent.id
                                    else selectedAgentIds += agent.id
                                } else {
                                    agentToEdit = agent
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedAgentIds += agent.id
                                }
                            },
                            onExport = {
                                com.example.ui.exportSelectedAgents(context, listOf(agent)) { _, _ -> }
                            }
                        )"""
    )
]

patch_file("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", agent_dash_replacements)
