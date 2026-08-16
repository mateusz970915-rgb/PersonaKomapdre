package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
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
import kotlin.math.roundToInt

data class CalendarEventOverlay(
    val title: String,
    val dateLabel: String,
    val dayIndex: Int,
    val category: String = "Scheduled Event"
)

/**
 * Overlay component that renders vertical event timelines and floating calendar event badges
 * across the chart, enabling users to correlate interaction spikes with scheduled activities.
 */
@Composable
fun ChartCalendarOverlay(
    events: List<CalendarEventOverlay>,
    totalDays: Int,
    showOverlay: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!showOverlay || events.isEmpty() || totalDays <= 1) return

    val density = LocalDensity.current
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    val eventColor = MaterialTheme.colorScheme.secondary

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val stepX = containerWidthPx / (totalDays - 1).coerceAtLeast(1)

        // Draw vertical dashed lines for each event day
        Canvas(modifier = Modifier.fillMaxSize()) {
            events.forEach { event ->
                if (event.dayIndex in 0 until totalDays) {
                    val xPos = event.dayIndex * stepX
                    drawLine(
                        color = eventColor.copy(alpha = 0.65f),
                        start = Offset(xPos, 0f),
                        end = Offset(xPos, size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }
            }
        }

        // Render Floating Event Badges
        events.forEachIndexed { idx, event ->
            if (event.dayIndex in 0 until totalDays) {
                val xPos = event.dayIndex * stepX
                val offsetX = (xPos - 40.dp.value * density.density).coerceIn(
                    10f,
                    containerWidthPx - 140.dp.value * density.density
                ).roundToInt()

                val offsetY = ((15 + (idx % 3) * 26).dp.value * density.density).coerceIn(
                    10f,
                    containerHeightPx - 30.dp.value * density.density
                ).roundToInt()

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            RoundedCornerShape(10.dp)
                        )
                        .testTag("chart_calendar_event_badge_${event.dayIndex}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Scheduled Event",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
