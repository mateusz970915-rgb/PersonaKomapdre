package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class TrendShiftAnnotation(
    val dayIndex: Int,
    val dateLabel: String,
    val value: Float,
    val percentChange: Float,
    val isSpike: Boolean
)

/**
 * Overlay component that renders visual threshold lines, data point hit markers,
 * and floating text annotations for specific dates where major trend shifts occurred.
 */
@Composable
fun ChartThresholdOverlay(
    thresholdValue: Float,
    maxChartValue: Float,
    dailyValues: List<Float>,
    thresholdPercentage: Int,
    dateLabels: List<String> = emptyList(),
    isEnabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.error,
    modifier: Modifier = Modifier
) {
    if (!isEnabled || maxChartValue <= 0f || thresholdValue <= 0f) return

    val density = LocalDensity.current
    val thresholdFraction = (thresholdValue / maxChartValue).coerceIn(0.05f, 0.95f)
    val hits = dailyValues.filter { it >= thresholdValue }
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    // Detect dates where major trend shifts occurred based on alert thresholds
    val annotations = mutableListOf<TrendShiftAnnotation>()
    if (dailyValues.isNotEmpty()) {
        dailyValues.forEachIndexed { index, valAtDay ->
            val prevVal = if (index > 0) dailyValues[index - 1] else null
            val pctChange = if (prevVal != null && prevVal > 0f) {
                ((valAtDay - prevVal) / prevVal) * 100f
            } else 0f

            val hitsThreshold = valAtDay >= thresholdValue
            val isMajorShift = abs(pctChange) >= thresholdPercentage && prevVal != null && prevVal > 0f

            if (hitsThreshold || isMajorShift) {
                val label = dateLabels.getOrNull(index) ?: "Day ${index + 1}"
                annotations.add(
                    TrendShiftAnnotation(
                        dayIndex = index,
                        dateLabel = label,
                        value = valAtDay,
                        percentChange = pctChange,
                        isSpike = pctChange >= 0f || hitsThreshold
                    )
                )
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate Y position for horizontal threshold line
            val yPos = height * (1f - thresholdFraction)

            // Draw horizontal threshold dashed line
            drawLine(
                color = accentColor.copy(alpha = 0.85f),
                start = Offset(0f, yPos),
                end = Offset(width, yPos),
                strokeWidth = 2.dp.toPx(),
                pathEffect = pathEffect
            )

            // Draw hit dots for data points meeting or exceeding threshold
            if (dailyValues.isNotEmpty()) {
                val stepX = width / (dailyValues.size - 1).coerceAtLeast(1)
                dailyValues.forEachIndexed { index, valAtDay ->
                    if (valAtDay >= thresholdValue) {
                        val xPos = index * stepX
                        val valFraction = (valAtDay / maxChartValue).coerceIn(0f, 1f)
                        val pointY = height * (1f - valFraction)

                        // Outer glowing aura
                        drawCircle(
                            color = accentColor.copy(alpha = 0.25f),
                            radius = 10.dp.toPx(),
                            center = Offset(xPos, pointY)
                        )
                        // Core dot
                        drawCircle(
                            color = accentColor,
                            radius = 5.dp.toPx(),
                            center = Offset(xPos, pointY)
                        )
                    }
                }
            }
        }

        // Floating Text Annotations on specific dates with major trend shifts
        if (dailyValues.size > 1) {
            val stepX = containerWidthPx / (dailyValues.size - 1).coerceAtLeast(1)

            annotations.forEach { annotation ->
                val xPos = annotation.dayIndex * stepX
                val valFraction = (annotation.value / maxChartValue).coerceIn(0.1f, 0.9f)
                val pointY = containerHeightPx * (1f - valFraction)

                // Clamp offset inside container bounds
                val offsetX = (xPos - 50.dp.value * density.density).coerceIn(
                    10f,
                    containerWidthPx - 130.dp.value * density.density
                ).roundToInt()

                val offsetY = (pointY - 32.dp.value * density.density).coerceIn(
                    10f,
                    containerHeightPx - 30.dp.value * density.density
                ).roundToInt()

                Surface(
                    color = if (annotation.isSpike) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .border(
                            1.dp,
                            if (annotation.isSpike) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("chart_floating_annotation_${annotation.dayIndex}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (annotation.isSpike) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (annotation.isSpike) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (annotation.percentChange != 0f) {
                                String.format(Locale.US, "%s: %+.0f%%", annotation.dateLabel, annotation.percentChange)
                            } else {
                                String.format(Locale.US, "%s: %.0f", annotation.dateLabel, annotation.value)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (annotation.isSpike) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Threshold Badge on top right
        Surface(
            color = accentColor,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 4.dp)
                .testTag("chart_threshold_indicator_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hits.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = String.format(Locale.US, "Threshold (±%d%%): %.1f", thresholdPercentage, thresholdValue),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

