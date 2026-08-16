package com.example.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.utils.InteractionExportHelper

/**
 * Dedicated button in the chart header for capturing the current Vico chart visualization
 * as an image (.png) for easy sharing and export.
 */
@Composable
fun ChartScreenshotButton(
    chartTitle: String = "Interaction Frequency Chart",
    dateRangeDays: Int = 7,
    dailyTotals: List<Float> = emptyList(),
    dateLabels: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    iconOnly: Boolean = false
) {
    val context = LocalContext.current

    if (iconOnly) {
        IconButton(
            onClick = {
                InteractionExportHelper.exportChartScreenshotToPng(
                    context = context,
                    chartTitle = chartTitle,
                    dateRangeDays = dateRangeDays,
                    dailyTotals = dailyTotals,
                    dateLabels = dateLabels
                )
            },
            modifier = modifier
                .size(32.dp)
                .testTag("btn_chart_header_screenshot_icon")
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture Chart Screenshot",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    } else {
        OutlinedButton(
            onClick = {
                InteractionExportHelper.exportChartScreenshotToPng(
                    context = context,
                    chartTitle = chartTitle,
                    dateRangeDays = dateRangeDays,
                    dailyTotals = dailyTotals,
                    dateLabels = dateLabels
                )
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = modifier
                .size(height = 32.dp, width = 120.dp)
                .testTag("btn_chart_header_screenshot")
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture Chart Screenshot",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Screenshot",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
