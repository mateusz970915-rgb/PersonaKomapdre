import re

with open('app/src/main/java/com/example/viewmodel/ColonyViewModel.kt', 'r') as f:
    content = f.read()

# Fix 1: first() extension
content = content.replace(
    "val history = kotlinx.coroutines.flow.first(flowMessages)",
    "val history = kotlinx.coroutines.flow.first(flowMessages)"
) # wait, I should do flowMessages.first()

content = content.replace(
    "val history = kotlinx.coroutines.flow.first(flowMessages)\n                    val historyText = history.takeLast(10).joinToString(\"\\n\") { \"${it.role}: ${it.content}\" }",
    "val history = flowMessages.first()\n                    val historyText = history.takeLast(10).joinToString(\"\\n\") { \"${it.role}: ${it.content}\" }"
)

# Fix 2: logActivity undefined
old_log = """                    // Log the event
                    logActivity(
                        agentName = agentName,
                        actionType = "Agent Chat",
                        description = "Responded to user message",
                        importance = 2
                    )"""
content = content.replace(old_log, "")

with open('app/src/main/java/com/example/viewmodel/ColonyViewModel.kt', 'w') as f:
    f.write(content)
