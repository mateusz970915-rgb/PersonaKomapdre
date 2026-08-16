package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class ChartColorConfig(
    val primaryColor: Color,
    val secondaryColor: Color,
    val barColor: Color,
    val gradientColors: Array<Color>,
    val strokeWidthDp: Float = 2f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChartColorConfig
        if (primaryColor != other.primaryColor) return false
        if (secondaryColor != other.secondaryColor) return false
        if (barColor != other.barColor) return false
        if (!gradientColors.contentEquals(other.gradientColors)) return false
        if (strokeWidthDp != other.strokeWidthDp) return false
        return true
    }

    override fun hashCode(): Int {
        var result = primaryColor.hashCode()
        result = 31 * result + secondaryColor.hashCode()
        result = 31 * result + barColor.hashCode()
        result = 31 * result + gradientColors.contentHashCode()
        result = 31 * result + strokeWidthDp.hashCode()
        return result
    }
}

val AVAILABLE_CHART_THEMES = listOf(
    "Standard",
    "Vibrant",
    "High Contrast",
    "Soft Pastel",
    "Monochrome",
    "Neon Pulse"
)

@Composable
fun getChartColorConfig(intensity: String): ChartColorConfig {
    val basePrimary = MaterialTheme.colorScheme.primary
    val baseSecondary = MaterialTheme.colorScheme.secondary
    val isDark = isSystemInDarkTheme()

    return when (intensity) {
        "Vibrant" -> ChartColorConfig(
            primaryColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF6200EA),
            secondaryColor = if (isDark) Color(0xFFFF4081) else Color(0xFF00B0FF),
            barColor = if (isDark) Color(0xFF00E676) else Color(0xFFD500F9),
            gradientColors = arrayOf(
                (if (isDark) Color(0xFF00E5FF) else Color(0xFF6200EA)).copy(alpha = 0.75f),
                (if (isDark) Color(0xFF7C4DFF) else Color(0xFF00B0FF)).copy(alpha = 0.05f)
            ),
            strokeWidthDp = 2.5f
        )
        "High Contrast" -> ChartColorConfig(
            primaryColor = if (isDark) Color(0xFFFFD600) else Color(0xFFD50000),
            secondaryColor = if (isDark) Color(0xFF00E676) else Color(0xFF0091EA),
            barColor = if (isDark) Color(0xFFFF6D00) else Color(0xFF2962FF),
            gradientColors = arrayOf(
                (if (isDark) Color(0xFFFFD600) else Color(0xFFD50000)).copy(alpha = 0.85f),
                (if (isDark) Color(0xFFFF3D00) else Color(0xFF0091EA)).copy(alpha = 0.2f)
            ),
            strokeWidthDp = 3.5f
        )
        "Soft Pastel" -> ChartColorConfig(
            primaryColor = if (isDark) Color(0xFFCE93D8) else Color(0xFFAB47BC),
            secondaryColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF26A69A),
            barColor = if (isDark) Color(0xFFFFCC80) else Color(0xFFA1887F),
            gradientColors = arrayOf(
                (if (isDark) Color(0xFFCE93D8) else Color(0xFFAB47BC)).copy(alpha = 0.55f),
                (if (isDark) Color(0xFF80CBC4) else Color(0xFF26A69A)).copy(alpha = 0.08f)
            ),
            strokeWidthDp = 2.2f
        )
        "Monochrome" -> ChartColorConfig(
            primaryColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF37474F),
            secondaryColor = if (isDark) Color(0xFFBDBDBD) else Color(0xFF78909C),
            barColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF263238),
            gradientColors = arrayOf(
                (if (isDark) Color(0xFFE0E0E0) else Color(0xFF37474F)).copy(alpha = 0.60f),
                (if (isDark) Color(0xFF9E9E9E) else Color(0xFF78909C)).copy(alpha = 0.05f)
            ),
            strokeWidthDp = 2.0f
        )
        "Neon Pulse" -> ChartColorConfig(
            primaryColor = if (isDark) Color(0xFF00FF66) else Color(0xFF00C853),
            secondaryColor = if (isDark) Color(0xFFFF007F) else Color(0xFFD81B60),
            barColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF00ACC1),
            gradientColors = arrayOf(
                (if (isDark) Color(0xFF00FF66) else Color(0xFF00C853)).copy(alpha = 0.75f),
                (if (isDark) Color(0xFFFF007F) else Color(0xFFD81B60)).copy(alpha = 0.10f)
            ),
            strokeWidthDp = 2.8f
        )
        else -> // "Standard"
            ChartColorConfig(
                primaryColor = basePrimary,
                secondaryColor = baseSecondary,
                barColor = basePrimary,
                gradientColors = arrayOf(
                    basePrimary.copy(alpha = 0.45f),
                    basePrimary.copy(alpha = 0.0f)
                ),
                strokeWidthDp = 2.0f
            )
    }
}
