import re

with open('app/src/main/java/com/example/ui/AgentActivityHeatmapWidget.kt', 'r') as f:
    text = f.read()

bad_block = """        val themes = mapOf(
        "Ocean" to listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFF3B82F6).copy(alpha = 0.4f),
            Color(0xFF8B5CF6).copy(alpha = 0.7f),
            Color(0xFFF59E0B).copy(alpha = 0.85f),
            Color(0xFF10B981)
        ),
        "Fire" to listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFFFBBF24).copy(alpha = 0.4f),
            Color(0xFFF59E0B).copy(alpha = 0.7f),
            Color(0xFFEA580C).copy(alpha = 0.85f),
            Color(0xFFE11D48)
        ),
        "Forest" to listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFF6EE7B7).copy(alpha = 0.4f),
            Color(0xFF10B981).copy(alpha = 0.7f),
            Color(0xFF059669).copy(alpha = 0.85f),
            Color(0xFF047857)
        ),
        "Sunset" to listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFFF472B6).copy(alpha = 0.4f),
            Color(0xFFEC4899).copy(alpha = 0.7f),
            Color(0xFFD946EF).copy(alpha = 0.85f),
            Color(0xFFA855F7)
        )
    )
    var selectedTheme by remember { mutableStateOf("Ocean") }
    val currentPalette = themes[selectedTheme] ?: themes["Ocean"]!!"""

text = text.replace(bad_block, "", 1)

with open('app/src/main/java/com/example/ui/AgentActivityHeatmapWidget.kt', 'w') as f:
    f.write(text)
