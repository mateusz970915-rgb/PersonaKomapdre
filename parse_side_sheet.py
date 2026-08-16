import re

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    text = f.read()

start_index = text.find('// Side Sheet Overlay for Agent chronological Activity Log')
if start_index == -1:
    print("Not found")
    exit(1)

brace_count = 0
found_brace = False
end_index = -1

for i in range(start_index, len(text)):
    if text[i] == '{':
        brace_count += 1
        found_brace = True
    elif text[i] == '}':
        brace_count -= 1
        
    if found_brace and brace_count == 0:
        end_index = i
        break

if end_index != -1:
    original_block = text[start_index:end_index+1]
    
    new_block = """// ModalBottomSheet for Agent Interaction History
        if (selectedAgentForActivity != null) {
            val agent = selectedAgentForActivity!!
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            val interactions = allFtsContent.filter { it.agentName.equals(agent.name, ignoreCase = true) }
            
            ModalBottomSheet(
                onDismissRequest = { selectedAgentForActivity = null },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${agent.name} - Interaction History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (interactions.isEmpty()) {
                        Text(
                            text = "No interaction history available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(interactions.size) { index ->
                                val interaction = interactions[index]
                                val dateStr = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(interaction.timestamp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = interaction.tag.ifEmpty { "Interaction" },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = interaction.snippet,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Model: ${interaction.modelUsed}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }"""
    
    new_text = text[:start_index] + new_block + text[end_index+1:]
    with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
        f.write(new_text)
    print("Replaced successfully")
else:
    print("Could not find end of block")
