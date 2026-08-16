import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Modify theme logic
theme_logic_old = """        val actualThemeMode = if (personaIsDarkMode) "Dark" else "Light"

        MyApplicationTheme(
            themeMode = actualThemeMode,
            dominantMood = dominantMood
        ) {"""

theme_logic_new = """        val actualThemeMode = if (personaIsDarkMode) "Dark" else "Light"

        MyApplicationTheme(
            darkTheme = personaIsDarkMode,
            themeMode = actualThemeMode,
            dominantMood = dominantMood
        ) {"""

content = content.replace(theme_logic_old, theme_logic_new)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
