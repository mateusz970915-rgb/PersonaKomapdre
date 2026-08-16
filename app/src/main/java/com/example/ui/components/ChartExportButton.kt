package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.InteractionRecord
import com.example.utils.InteractionExportHelper

@Composable
fun ChartExportButton(
    interactions: List<InteractionRecord>,
    dateRangeDays: Int,
    dailyTotals: List<Float> = emptyList(),
    dateLabels: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.size(height = 32.dp, width = 100.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Export chart data",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Export",
                style = MaterialTheme.typography.labelMedium
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Export as CSV (.csv)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    expanded = false
                    InteractionExportHelper.exportInteractionsToCsv(
                        context = context,
                        interactions = interactions,
                        dateRangeDays = dateRangeDays
                    )
                }
            )

            DropdownMenuItem(
                text = { Text("Export as PDF (.pdf)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    expanded = false
                    InteractionExportHelper.exportInteractionsToPdf(
                        context = context,
                        interactions = interactions,
                        dateRangeDays = dateRangeDays
                    )
                }
            )

            DropdownMenuItem(
                text = { Text("Take Screenshot (.png)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    expanded = false
                    InteractionExportHelper.exportChartScreenshotToPng(
                        context = context,
                        chartTitle = "Interaction Activity Trends",
                        dateRangeDays = dateRangeDays,
                        dailyTotals = dailyTotals,
                        dateLabels = dateLabels
                    )
                }
            )
        }
    }
}
