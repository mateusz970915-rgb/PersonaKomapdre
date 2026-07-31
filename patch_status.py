import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

old_status_logic = """    val (statusText, dotColor) = when {
        agent.status == "Resting" -> "Resting" to Color(0xFFFF9800)
        agent.status != "Active" -> "Idle" to MaterialTheme.colorScheme.outline
        isWorking -> "Working" to MaterialTheme.colorScheme.tertiary
        else -> "Active" to Color(0xFF4CAF50)
    }"""

new_status_logic = """    val now = System.currentTimeMillis()
    val isOnline = (now - agent.lastActiveTimestamp) < (5 * 60 * 1000)
    
    val (statusText, dotColor) = when {
        agent.status == "Resting" -> "Resting" to Color(0xFFFF9800)
        agent.status == "Paused" || agent.status == "Halted" -> "Offline" to MaterialTheme.colorScheme.outline
        isWorking -> "Busy" to MaterialTheme.colorScheme.tertiary
        isOnline -> "Online" to Color(0xFF4CAF50)
        else -> "Offline" to MaterialTheme.colorScheme.outline
    }"""

content = content.replace(old_status_logic, new_status_logic)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
