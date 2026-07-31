import re
with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

# Replace LazyVerticalGrid with LazyColumn
content = content.replace("LazyVerticalGrid(\n                columns = GridCells.Fixed(2),", "LazyColumn(\n")
content = content.replace("item(span = { GridItemSpan(2) }) {", "item {")

# Find the items(filteredAgents block
items_match = re.search(r'items\(filteredAgents, key = \{ it\.id \}\) \{ agent ->(.*?AgentCard\([^)]*\)\s*.*?\n\s*\}\n\s*\})', content, re.DOTALL)
if items_match:
    old_items_block = items_match.group(0)
    
    # We will replace this block with SwipeToDismiss wrapped AgentCard, plus empty state check.
    # But wait, empty state should be inside the LazyColumn? Or just render empty state if filteredAgents.isEmpty() inside the LazyColumn?
    
    new_items_block = """if (filteredAgents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "No Agents",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No agents found.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add an agent to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateToAddAgent) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Agent")
                        }
                    }
                }
            } else {
                items(filteredAgents, key = { it.id }) { agent ->
                    val isWorking = subTasksList.any { task ->
                        task.assignedAgent.equals(agent.name, ignoreCase = true) && task.status == "In Progress"
                    }
                    val isSelected = selectedAgents.contains(agent)
                    val catHex = viewModel.getCategoryColorHex(agent.type)
                    
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteAgent(agent.id)
                                true
                            } else {
                                false
                            }
                        }
                    )
                    
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                else -> androidx.compose.ui.graphics.Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp)
                                    .background(color, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Agent",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        AgentCard(
                            agent = agent,
                            isWorking = isWorking,
                            isSelectionMode = isMultiSelectMode,
                            isSelected = isSelected,
                            categoryColorHex = catHex,
                            prediction = predictions[agent.id],
                            subTasks = subTasksList,
                            onSelectToggle = {
                                if (isSelected) {
                                    selectedAgents.remove(agent)
                                } else {
                                    selectedAgents.add(agent)
                                }
                            },
                            onTap = {
                                if (isMultiSelectMode) {
                                    if (isSelected) {
                                        selectedAgents.remove(agent)
                                    } else {
                                        selectedAgents.add(agent)
                                    }
                                } else {
                                    selectedAgentForActivity = agent
                                }
                            },
                            onPauseToggle = { viewModel.toggleAgentStatus(agent) },
                            onDelete = { viewModel.deleteAgent(agent.id) }
                        )
                    }
                }
            }"""
    
    content = content.replace(old_items_block, new_items_block)
    
with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
