package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MultilineChart
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ChartTypeOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)

val defaultChartTypeOptions = listOf(
    ChartTypeOption("Line", "Line", Icons.AutoMirrored.Filled.ShowChart),
    ChartTypeOption("Bar", "Bar", Icons.Default.BarChart),
    ChartTypeOption("Area", "Area", Icons.AutoMirrored.Filled.MultilineChart)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    options: List<ChartTypeOption> = defaultChartTypeOptions
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selectedType == option.id,
                onClick = { onTypeSelected(option.id) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.label,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        }
    }
}
