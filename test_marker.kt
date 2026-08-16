package com.example.ui.components

import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter

class MyFormatter : MarkerLabelFormatter {
    override fun getLabel(
        markedEntries: List<com.patrykandpatrick.vico.core.marker.Marker.EntryModel>,
        chartValues: com.patrykandpatrick.vico.core.chart.values.ChartValues,
    ): CharSequence {
        return ""
    }
}
