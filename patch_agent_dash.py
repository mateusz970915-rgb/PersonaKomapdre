import re

with open("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", "r") as f:
    content = f.read()

old_header = """fun AgentCard(
    agent: Agent,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isAgentActive = agent.status == "Active"
    
    val infiniteTransition = rememberInfiniteTransition()"""

new_header = """fun AgentCard(
    agent: Agent,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isAgentActive = agent.status == "Active"
    
    val now = System.currentTimeMillis()
    val isOnline = (now - agent.lastActiveTimestamp) < (5 * 60 * 1000)
    
    val (statusText, statusColor) = when {
        agent.status == "Resting" -> "Resting" to Color(0xFFFF9800)
        agent.status == "Paused" || agent.status == "Halted" -> "Offline" to MaterialTheme.colorScheme.outline
        isOnline -> "Online" to Color(0xFF4CAF50)
        else -> "Offline" to MaterialTheme.colorScheme.outline
    }
    
    val infiniteTransition = rememberInfiniteTransition()"""

content = content.replace(old_header, new_header)

old_text = """                Text(
                    text = agent.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )"""

new_text = """                Text(
                    text = agent.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }"""

content = content.replace(old_text, new_text)

with open("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", "w") as f:
    f.write(content)
