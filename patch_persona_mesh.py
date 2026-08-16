import re

with open('app/src/main/java/com/example/ui/PersonaMeshScreen.kt', 'r') as f:
    content = f.read()

chart_model_old = """    val chartModel = remember(agents) {
        if (agents.isEmpty()) {
            entryModelOf(FloatEntry(0f, 0f))
        } else {
            // Visualize activity based on taskSummary length or arbitrary score
            val entries = agents.mapIndexed { index, agent ->
                FloatEntry(index.toFloat(), agent.taskSummary.length.toFloat() % 100f) // Mock score
            }
            entryModelOf(*entries.toTypedArray())
        }
    }"""

chart_model_new = """    val chartModel = remember(agents) {
        if (agents.isEmpty()) {
            entryModelOf(listOf(FloatEntry(0f, 0f)))
        } else {
            // Visualize activity based on taskSummary length or arbitrary score
            val entries = agents.mapIndexed { index, agent ->
                FloatEntry(index.toFloat(), agent.taskSummary.length.toFloat() % 100f) // Mock score
            }
            entryModelOf(entries)
        }
    }"""

content = content.replace(chart_model_old, chart_model_new)

with open('app/src/main/java/com/example/ui/PersonaMeshScreen.kt', 'w') as f:
    f.write(content)
