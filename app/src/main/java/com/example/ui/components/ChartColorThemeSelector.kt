package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Interactive selector for Chart Color Themes (e.g. Standard, Vibrant, High Contrast,
 * Soft Pastel, Monochrome, Neon Pulse). Persists selection to DataStore.
 */
@Composable
fun ChartColorThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    useDropdownMenu: Boolean = false
) {
    if (useDropdownMenu) {
        var expanded by remember { mutableStateOf(false) }
        val currentConfig = getChartColorConfig(selectedTheme)

        Box(modifier = modifier) {
            FilterChip(
                selected = true,
                onClick = { expanded = true },
                label = { Text("Theme: $selectedTheme") },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(currentConfig.primaryColor)
                    )
                },
                modifier = Modifier.testTag("chart_theme_dropdown_trigger")
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AVAILABLE_CHART_THEMES.forEach { themeName ->
                    val themeConfig = getChartColorConfig(themeName)
                    DropdownMenuItem(
                        text = { Text(themeName) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(themeConfig.primaryColor)
                            )
                        },
                        onClick = {
                            expanded = false
                            onThemeSelected(themeName)
                        },
                        modifier = Modifier.testTag("chart_theme_dropdown_item_$themeName")
                    )
                }
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Chart Theme Palette",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            AVAILABLE_CHART_THEMES.forEach { themeName ->
                val isSelected = selectedTheme.equals(themeName, ignoreCase = true)
                val themeConfig = getChartColorConfig(themeName)

                FilterChip(
                    selected = isSelected,
                    onClick = { onThemeSelected(themeName) },
                    label = { Text(themeName, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(themeConfig.primaryColor)
                        )
                    },
                    modifier = Modifier.testTag("chart_theme_chip_${themeName.replace(" ", "_")}")
                )
            }
        }
    }
}
