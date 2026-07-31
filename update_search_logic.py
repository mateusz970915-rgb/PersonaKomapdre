import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

old_logic = """        var result = agents.filter { agent ->
            val nameMatch = agent.name.contains(searchQuery, ignoreCase = true)
            val roleMatch = agent.role.contains(searchQuery, ignoreCase = true) || agent.type.contains(searchQuery, ignoreCase = true)
            val personaMatch = agent.traits.contains(searchQuery, ignoreCase = true) || agent.systemPrompt.contains(searchQuery, ignoreCase = true)
            nameMatch || roleMatch || personaMatch
        }"""

new_logic = """        var result = agents.filter { agent ->
            agent.name.contains(searchQuery, ignoreCase = true)
        }"""

content = content.replace(old_logic, new_logic)

old_placeholder = """placeholder = { Text("Search agents by name or role...") }"""
new_placeholder = """placeholder = { Text("Search agents by name...") }"""

content = content.replace(old_placeholder, new_placeholder)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
