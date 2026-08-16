import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add composable for persona_settings
new_composable = """
                    composable("persona_settings") {
                        com.example.ui.PersonaSettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
"""

if "composable(\"persona_settings\")" not in content:
    content = content.replace("composable(\"persona_mesh\") {", new_composable + "                    composable(\"persona_mesh\") {")

# Modify theme logic
theme_logic_old = """
        val dominantMood = androidx.compose.runtime.remember(agents, subTasks) {
            val activeAgent = agents.firstOrNull { it.status == "Active" } ?: agents.firstOrNull()
            if (activeAgent != null) {
                com.example.data.calculateAgentMood(activeAgent, subTasks).moodTitle
            } else null
        }

        MyApplicationTheme(
            themeMode = preferences.themeMode,
            dominantMood = dominantMood
        ) {
"""

theme_logic_new = """
        val personaIsDarkMode by viewModel.personaPreferences.isDarkMode.collectAsState(initial = false)
        val dominantMood = androidx.compose.runtime.remember(agents, subTasks) {
            val activeAgent = agents.firstOrNull { it.status == "Active" } ?: agents.firstOrNull()
            if (activeAgent != null) {
                com.example.data.calculateAgentMood(activeAgent, subTasks).moodTitle
            } else null
        }
        
        val actualThemeMode = if (personaIsDarkMode) "Dark" else "Light"

        MyApplicationTheme(
            themeMode = actualThemeMode,
            dominantMood = dominantMood
        ) {
"""

content = content.replace(theme_logic_old, theme_logic_new)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
