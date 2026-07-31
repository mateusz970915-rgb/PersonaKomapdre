with open("app/src/main/java/com/example/ui/Phase5EvolutionScreen.kt", "r") as f:
    content = f.read()

import re

badge = """
@Composable
fun SimulationBadge() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = "[SIMULATION MODE]",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
"""

if "fun SimulationBadge()" not in content:
    content = content + "\n" + badge

target_1 = """Text("1. On-Device LLM Runner (Gemma 2B JNI/MediaPipe)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)"""
repl_1 = """Text("1. On-Device LLM Runner (Gemma 2B JNI/MediaPipe)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            SimulationBadge()"""
content = content.replace(target_1, repl_1)

target_4 = """Text("4. Smart Home Bridge (Matter/Thread)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)"""
repl_4 = """Text("4. Smart Home Bridge (Matter/Thread)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            SimulationBadge()"""
content = content.replace(target_4, repl_4)

target_5 = """Text("5. Sandbox Simulation State Machine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)"""
repl_5 = """Text("5. Sandbox Simulation State Machine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            SimulationBadge()"""
content = content.replace(target_5, repl_5)

# Also fix "Generowane Losowo" in logs for Auto-Evolution Engine.
# The user clicked "Wymuś Ewolucję Heurystyk". Let's see what `viewModel.triggerAutoEvolution()` does.

with open("app/src/main/java/com/example/ui/Phase5EvolutionScreen.kt", "w") as f:
    f.write(content)
