package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.utils.NotificationHelper
import java.util.Locale

/**
 * Summary view displayed above a chart to show calculated totals,
 * daily averages, calculated percentage trends, and trend alert notifications
 * when custom thresholds are exceeded.
 */
@Composable
fun ChartTrendSummaryView(
    currentTotal: Number,
    previousTotal: Number,
    unitLabel: String = "items",
    periodDays: Int = 7,
    trendAlertThreshold: Int = 20,
    trendAlertsEnabled: Boolean = true,
    comparisonInterval: String = "Weekly",
    aggregationMethod: String = "Total Sum",
    onUpdateInterval: ((String) -> Unit)? = null,
    onUpdateAggregation: ((String) -> Unit)? = null,
    onUpdateThreshold: ((Int) -> Unit)? = null,
    onToggleAlerts: ((Boolean) -> Unit)? = null,
    onTriggerWorkManagerCheck: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cur = currentTotal.toDouble()
    val prev = previousTotal.toDouble()

    val percentageChange = when {
        prev == 0.0 && cur == 0.0 -> 0.0
        prev == 0.0 -> 100.0
        else -> ((cur - prev) / prev) * 100.0
    }

    val absChange = Math.abs(percentageChange)
    val isThresholdExceeded = trendAlertsEnabled && absChange >= trendAlertThreshold && (cur > 0.0 || prev > 0.0)

    val isPositive = percentageChange > 0.0
    val isNegative = percentageChange < 0.0
    val isDark = isSystemInDarkTheme()

    var showThresholdDialog by remember { mutableStateOf(false) }

    // Fire system notification when threshold is exceeded
    LaunchedEffect(isThresholdExceeded, percentageChange, trendAlertThreshold) {
        if (isThresholdExceeded) {
            val directionStr = if (isPositive) "Upward 📈" else "Downward 📉"
            val changeStr = String.format(Locale.US, "%.1f%%", percentageChange)
            val title = "Significant $directionStr Trend Detected ($changeStr)"
            val message = "Your $unitLabel volume shifted by $changeStr over the last $periodDays days, exceeding your set threshold of ±$trendAlertThreshold%."
            NotificationHelper.sendNotification(context, 8881, title, message)
        }
    }

    val trendColor = when {
        isPositive -> if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
        isNegative -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val trendBgColor = when {
        isPositive -> if (isDark) Color(0xFF1B381E) else Color(0xFFE8F5E9)
        isNegative -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val trendIcon = when {
        isPositive -> Icons.AutoMirrored.Filled.TrendingUp
        isNegative -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }

    val formattedPercentage = if (Math.abs(percentageChange % 1.0) < 0.05) {
        String.format(Locale.US, "%.0f%%", percentageChange)
    } else {
        String.format(Locale.US, "%.1f%%", Math.abs(percentageChange))
    }

    val trendText = when {
        isPositive -> "+$formattedPercentage"
        isNegative -> "-$formattedPercentage"
        else -> "0%"
    }

    val dailyAvg = if (periodDays > 0) cur / periodDays else 0.0
    val formattedAvg = if (Math.abs(dailyAvg % 1.0) < 0.05) {
        String.format(Locale.US, "%.0f", dailyAvg)
    } else {
        String.format(Locale.US, "%.1f", dailyAvg)
    }

    val formattedTotal = if (Math.abs(cur % 1.0) < 0.05) {
        String.format(Locale.US, "%.0f", cur)
    } else {
        String.format(Locale.US, "%.1f", cur)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(10.dp),
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formattedTotal,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = unitLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$formattedAvg / day avg in last ${periodDays}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                // Row for Aggregation & Interval Filters
                if (onUpdateAggregation != null || onUpdateInterval != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Aggregation Method Toggle (Total Sum, Average, Median)
                        if (onUpdateAggregation != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Metric:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                listOf("Total Sum", "Average", "Median").forEach { method ->
                                    FilterChip(
                                        selected = aggregationMethod.equals(method, ignoreCase = true),
                                        onClick = { onUpdateAggregation(method) },
                                        label = { Text(method, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.testTag("trend_aggregation_chip_${method.replace(" ", "_")}")
                                    )
                                }
                            }
                        }

                        // Comparison Period Interval Toggle
                        if (onUpdateInterval != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Daily", "Weekly", "Monthly").forEach { interval ->
                                    FilterChip(
                                        selected = comparisonInterval.equals(interval, ignoreCase = true),
                                        onClick = { onUpdateInterval(interval) },
                                        label = { Text(interval, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.testTag("trend_interval_chip_$interval")
                                    )
                                }
                            }
                        }
                    }
                }

                        // Trend Badge
                        Surface(
                            color = trendBgColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = trendIcon,
                                    contentDescription = null,
                                    tint = trendColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = trendText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = trendColor
                                )
                            }
                        }

                        // Threshold Settings Gear Icon
                        if (onUpdateThreshold != null) {
                            IconButton(
                                onClick = { showThresholdDialog = true },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("trend_threshold_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Trend Alert Threshold Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Threshold Alert Banner
                if (isThresholdExceeded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (isPositive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("trend_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Trend Alert: ${if (isPositive) "Upward +" else "Downward -"}${formattedPercentage} exceeds threshold (±$trendAlertThreshold%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to adjust threshold
    if (showThresholdDialog && onUpdateThreshold != null) {
        var sliderValue by remember { mutableFloatStateOf(trendAlertThreshold.toFloat()) }
        var alertsEnabledState by remember { mutableStateOf(trendAlertsEnabled) }

        AlertDialog(
            onDismissRequest = { showThresholdDialog = false },
            title = { Text("Trend Detection Threshold") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Trend Alerts", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = alertsEnabledState,
                            onCheckedChange = {
                                alertsEnabledState = it
                                onToggleAlerts?.invoke(it)
                            },
                            modifier = Modifier.testTag("toggle_trend_alerts_switch")
                        )
                    }

                    if (alertsEnabledState) {
                        Text(
                            text = "Alert Threshold: ±${sliderValue.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Triggers a notification when period-over-period data shifts by more than ${sliderValue.toInt()}%.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            valueRange = 5f..50f,
                            steps = 8,
                            modifier = Modifier.testTag("trend_threshold_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(10, 15, 20, 30, 50).forEach { preset ->
                                FilterChip(
                                    selected = sliderValue.toInt() == preset,
                                    onClick = { sliderValue = preset.toFloat() },
                                    label = { Text("$preset%") }
                                )
                            }
                        }
                        if (onTriggerWorkManagerCheck != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    onUpdateThreshold.invoke(sliderValue.toInt())
                                    onTriggerWorkManagerCheck.invoke()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_run_workmanager_anomaly_check")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Evaluate WorkManager Task Now")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateThreshold.invoke(sliderValue.toInt())
                        showThresholdDialog = false
                    },
                    modifier = Modifier.testTag("confirm_threshold_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showThresholdDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
