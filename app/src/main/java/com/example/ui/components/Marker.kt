package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.marker.markerComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.chart.values.ChartValues

class CustomMarkerLabelFormatter : MarkerLabelFormatter {
    override fun getLabel(
        markedEntries: List<Marker.EntryModel>,
        chartValues: ChartValues
    ): CharSequence {
        return markedEntries.joinToString(separator = "\n") { 
            "${it.entry.y.toInt()}" 
        }
    }
}

@Composable
fun rememberMarker(): MarkerComponent {
    val labelBackgroundShape = remember { MarkerCorneredShape(Corner.FullyRounded) }
    val labelBackground = shapeComponent(
        shape = labelBackgroundShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    )
    val label = textComponent(
        background = labelBackground,
        lineCount = 1,
        padding = dimensionsOf(8.dp, 4.dp),
        color = MaterialTheme.colorScheme.onSurface,
        textSize = 14.sp
    )
    val indicatorInnerComponent = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.primary)
    val indicatorCenterComponent = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.surface)
    val indicatorOuterComponent = shapeComponent(Shapes.pillShape, Color.Transparent)
    val indicator = com.patrykandpatrick.vico.compose.component.overlayingComponent(
        outer = indicatorOuterComponent,
        inner = com.patrykandpatrick.vico.compose.component.overlayingComponent(
            outer = indicatorCenterComponent,
            inner = indicatorInnerComponent,
            innerPaddingAll = 5.dp
        ),
        innerPaddingAll = 10.dp
    )
    val guideline = lineComponent(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
        thickness = 2.dp
    )
    return markerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline
    ).apply {
        labelFormatter = CustomMarkerLabelFormatter()
    }
}
