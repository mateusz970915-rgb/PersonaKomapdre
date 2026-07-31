import re

with open("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", "r") as f:
    content = f.read()

old_logic = """    val filteredAgents = remember(agents, searchQuery) {
        if (searchQuery.isBlank()) {
            agents
        } else {
            agents.filter {
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.type.contains(searchQuery, ignoreCase = true)
            }
        }
    }"""

new_logic = """    val filteredAgents = remember(agents, searchQuery) {
        if (searchQuery.isBlank()) {
            agents
        } else {
            agents.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }"""

content = content.replace(old_logic, new_logic)

old_placeholder = """placeholder = { Text("Search agents...") }"""
new_placeholder = """placeholder = { Text("Search agents by name...") }"""

content = content.replace(old_placeholder, new_placeholder)

with open("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", "w") as f:
    f.write(content)
