package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.entry.ChartEntryModel

/**
 * A visually appealing, M3-styled empty state component for charts.
 * Displays a subtle decorative canvas grid/chart graphic behind an iconic badge,
 * title, description, and optional action button.
 */
@Composable
fun ChartEmptyState(
    modifier: Modifier = Modifier,
    title: String = "No Data Available",
    message: String = "There is no recorded activity for the selected time range.",
    icon: ImageVector = Icons.AutoMirrored.Outlined.ShowChart,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    height: Dp = 160.dp
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Background decorative empty chart grid graphic
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val heightPx = size.height
                val gridLines = 4
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // Draw dashed horizontal lines
                for (i in 1..gridLines) {
                    val y = heightPx * (i.toFloat() / (gridLines + 1))
                    drawLine(
                        color = outlineColor,
                        start = Offset(24.dp.toPx(), y),
                        end = Offset(width - 24.dp.toPx(), y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashPathEffect
                    )
                }

                // Draw faint waveform line representing an empty placeholder chart
                val path = Path().apply {
                    moveTo(24.dp.toPx(), heightPx * 0.7f)
                    cubicTo(
                        width * 0.25f, heightPx * 0.68f,
                        width * 0.35f, heightPx * 0.72f,
                        width * 0.5f, heightPx * 0.7f
                    )
                    cubicTo(
                        width * 0.65f, heightPx * 0.68f,
                        width * 0.75f, heightPx * 0.71f,
                        width - 24.dp.toPx(), heightPx * 0.7f
                    )
                }
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = dashPathEffect
                    )
                )
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Badge Icon with soft glowing background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                if (actionLabel != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onActionClick,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (actionIcon != null) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Utility wrapper that handles seamless crossfade transition between
 * a chart view and the empty state based on whether data exists.
 */
@Composable
fun ChartContainerWithEmptyState(
    hasData: Boolean,
    modifier: Modifier = Modifier,
    emptyTitle: String = "No Data Available",
    emptyMessage: String = "No activity recorded for this period.",
    emptyIcon: ImageVector = Icons.AutoMirrored.Outlined.ShowChart,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    emptyStateHeight: Dp = 150.dp,
    chartContent: @Composable () -> Unit
) {
    Crossfade(
        targetState = hasData,
        label = "ChartEmptyStateCrossfade",
        modifier = modifier
    ) { dataAvailable ->
        if (dataAvailable) {
            chartContent()
        } else {
            ChartEmptyState(
                title = emptyTitle,
                message = emptyMessage,
                icon = emptyIcon,
                actionLabel = actionLabel,
                actionIcon = actionIcon,
                onActionClick = onActionClick,
                height = emptyStateHeight
            )
        }
    }
}

/**
 * Extension helper to determine if a Vico [ChartEntryModel] contains only zero entries or is empty.
 */
fun ChartEntryModel.isZeroOrEmpty(): Boolean {
    if (entries.isEmpty()) return true
    return entries.all { series ->
        series.all { entry -> entry.y == 0f }
    }
}
