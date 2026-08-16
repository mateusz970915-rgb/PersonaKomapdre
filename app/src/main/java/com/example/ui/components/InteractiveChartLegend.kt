package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class LegendSeriesItem(
    val id: String,
    val name: String,
    val color: Color,
    val count: Int = 0
)

/**
 * Interactive Legend component that allows users to toggle specific chart series on or off
 * by clicking on legend labels.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveChartLegend(
    seriesList: List<LegendSeriesItem>,
    disabledSeriesIds: Set<String>,
    onToggleSeries: (String) -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (seriesList.isEmpty()) return

    val totalActive = seriesList.size - disabledSeriesIds.size

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chart Series ($totalActive/${seriesList.size} active)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (disabledSeriesIds.isNotEmpty()) {
                    TextButton(
                        onClick = onResetAll,
                        modifier = Modifier.testTag("legend_reset_all_btn")
                    ) {
                        Text(
                            text = "Show All",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                seriesList.forEach { item ->
                    val isEnabled = !disabledSeriesIds.contains(item.id)

                    val chipBg = if (isEnabled) {
                        item.color.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }

                    val chipBorder = if (isEnabled) {
                        item.color.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }

                    val textColor = if (isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    }

                    Surface(
                        color = chipBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                            .clickable { onToggleSeries(item.id) }
                            .testTag("legend_chip_${item.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = if (isEnabled) item.color else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal,
                                color = textColor
                            )
                            if (item.count > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${item.count})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.VisibilityOff,
                                contentDescription = if (isEnabled) "Hide series" else "Show series",
                                tint = if (isEnabled) item.color else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
