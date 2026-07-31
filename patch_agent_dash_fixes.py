import re

with open("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", "r") as f:
    content = f.read()

# Fix signature
target_sig = r'(onLongClick: \(\) -> Unit\s*)(\)\s*\{)'
repl_sig = r'\1, onExport: () -> Unit = {}\n\2'
content = re.sub(target_sig, repl_sig, content)

# Fix IconButton
target_icon = r'(if \(!isSelectionMode\) \{\s*IconButton\(\s*onClick = onDelete,\s*modifier = Modifier\.align\(Alignment\.TopEnd\)\s*\)\s*\{\s*Icon\(\s*imageVector = Icons\.Default\.Delete)'
repl_icon = r'if (!isSelectionMode) {\n                Row(modifier = Modifier.align(Alignment.TopEnd)) {\n                    IconButton(onClick = onExport) {\n                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export Agent", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))\n                    }\n                    IconButton(onClick = onDelete) {\n                        Icon(imageVector = Icons.Default.Delete'
content = re.sub(target_icon, repl_icon, content)

# Fix second part of IconButton row (closing tags) - Actually I only replaced the opening.
# Wait, let's do a literal replacement for the whole block.
target_icon_full = r'if \(!isSelectionMode\) \{\s*IconButton\(\s*onClick = onDelete,\s*modifier = Modifier\.align\(Alignment\.TopEnd\)\s*\)\s*\{\s*Icon\(\s*imageVector = Icons\.Default\.Delete,\s*contentDescription = "Delete",\s*tint = MaterialTheme\.colorScheme\.error,\s*modifier = Modifier\.size\(20\.dp\)\s*\)\s*\}\s*\}'
repl_icon_full = r"""if (!isSelectionMode) {
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(onClick = onExport) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }"""
content = re.sub(target_icon_full, repl_icon_full, content)

# Update AgentCard call
target_call = r'(onLongClick = \{\s*if \(!isSelectionMode\) \{\s*selectedAgentIds \+= agent\.id\s*\}\s*\})(\s*\)\s*)'
repl_call = r'\1,\n                            onExport = {\n                                com.example.ui.exportSelectedAgents(context, listOf(agent)) { _, _ -> }\n                            }\n\2'
content = re.sub(target_call, repl_call, content)

with open("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", "w") as f:
    f.write(content)

