import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

old_logic = """    val now = System.currentTimeMillis()
    val isOnline = (now - agent.lastActiveTimestamp) < (5 * 60 * 1000)
    
    val (statusText, dotColor) = when {
        agent.status == "Resting" -> "Resting" to Color(0xFFFF9800)
        agent.status == "Paused" || agent.status == "Halted" -> "Offline" to MaterialTheme.colorScheme.outline
        isWorking -> "Busy" to MaterialTheme.colorScheme.tertiary
        isOnline -> "Online" to Color(0xFF4CAF50)
        else -> "Offline" to MaterialTheme.colorScheme.outline
    }"""

new_logic = """    val now = System.currentTimeMillis()
    val isOnline = (now - agent.lastActiveTimestamp) < (5 * 60 * 1000)
    
    val (statusText, dotColor) = when {
        agent.status == "Resting" -> "Resting" to Color(0xFFFF9800)
        agent.status == "Paused" || agent.status == "Halted" -> "Offline" to MaterialTheme.colorScheme.outline
        isWorking -> "Busy" to MaterialTheme.colorScheme.tertiary
        isOnline -> "Online" to Color(0xFF4CAF50)
        else -> "Offline" to MaterialTheme.colorScheme.outline
    }"""

# Actually looking at the dashboard code the old status text logic was Working not Busy. Let's make sure it matches

old_text = """if (statusText == "Working" || statusText == "Busy")"""
new_text = """if (statusText == "Busy")"""

content = content.replace(old_text, new_text)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
