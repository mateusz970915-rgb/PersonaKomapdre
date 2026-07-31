import re

def patch_file(filepath, replacements):
    with open(filepath, "r") as f:
        content = f.read()
    
    # Add import
    if "import androidx.compose.ui.platform.LocalHapticFeedback" not in content:
        import_stmt = "import androidx.compose.ui.platform.LocalHapticFeedback\nimport androidx.compose.ui.hapticfeedback.HapticFeedbackType\n"
        content = content.replace("import androidx.compose.ui.Modifier", import_stmt + "import androidx.compose.ui.Modifier")
    
    for old, new in replacements:
        if old in content:
            content = content.replace(old, new)
        else:
            print(f"Warning: could not find target in {filepath}:\n{old}")
            
    with open(filepath, "w") as f:
        f.write(content)

# DashboardScreen patches
dash_replacements = [
    (
"""                    val catHex = viewModel.getCategoryColorHex(agent.type)
                    
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteAgent(agent.id)
                                true""",
"""                    val catHex = viewModel.getCategoryColorHex(agent.type)
                    val haptic = LocalHapticFeedback.current
                    
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteAgent(agent.id)
                                true"""
    ),
    (
"""    var showMenu by remember { mutableStateOf(false) }

    val parsedCatColor = remember(categoryColorHex) {""",
"""    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val parsedCatColor = remember(categoryColorHex) {"""
    ),
    (
"""    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onTap() },
                onLongClick = { onSelectToggle() }
            )""",
"""    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTap() 
                },
                onLongClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectToggle() 
                }
            )"""
    )
]

patch_file("app/src/main/java/com/example/ui/DashboardScreen.kt", dash_replacements)

# AgentDashboardScreen patches
agent_dash_replacements = [
    (
"""    val now = System.currentTimeMillis()
    val isOnline = (now - agent.lastActiveTimestamp) < (5 * 60 * 1000)""",
"""    val haptic = LocalHapticFeedback.current
    
    val now = System.currentTimeMillis()
    val isOnline = (now - agent.lastActiveTimestamp) < (5 * 60 * 1000)"""
    ),
    (
"""    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CardDefaults.shape)
            .border(if (isSelected || isAgentActive) 2.dp else 0.dp, borderColor, CardDefaults.shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),""",
"""    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CardDefaults.shape)
            .border(if (isSelected || isAgentActive) 2.dp else 0.dp, borderColor, CardDefaults.shape)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),"""
    )
]

patch_file("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", agent_dash_replacements)

