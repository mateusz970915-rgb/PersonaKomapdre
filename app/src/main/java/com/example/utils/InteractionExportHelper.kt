package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.InteractionRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InteractionExportHelper {

    /**
     * Exports interaction records within the specified date range to a CSV file.
     * Saves locally and launches share/save intent.
     */
    fun exportInteractionsToCsv(
        context: Context,
        interactions: List<InteractionRecord>,
        dateRangeDays: Int
    ): File? {
        try {
            val filtered = filterInteractionsByDateRange(interactions, dateRangeDays)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val sb = StringBuilder()
            // CSV Header
            sb.append("ID,Timestamp,Formatted Date,Agent Name,Model Used,Total Tokens,Latency (ms),Tag,Snippet\n")

            // Rows
            filtered.forEach { rec ->
                val formattedDate = dateFormat.format(Date(rec.timestamp))
                val escapedId = escapeCsv(rec.id)
                val escapedDate = escapeCsv(formattedDate)
                val escapedAgent = escapeCsv(rec.agentName)
                val escapedModel = escapeCsv(rec.modelUsed)
                val escapedTag = escapeCsv(rec.tag)
                val escapedSnippet = escapeCsv(rec.snippet)

                sb.append("$escapedId,${rec.timestamp},$escapedDate,$escapedAgent,$escapedModel,${rec.totalTokens},${rec.latencyMs},$escapedTag,$escapedSnippet\n")
            }

            val fileName = "interaction_data_${dateRangeDays}d_${System.currentTimeMillis()}.csv"
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(sb.toString())

            // Attempt copy to Documents folder for local persistent file access
            val docDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (docDir != null) {
                if (!docDir.exists()) docDir.mkdirs()
                val persistentFile = File(docDir, fileName)
                persistentFile.writeText(sb.toString())
            }

            shareFile(context, cacheFile, "text/csv", "Export Interaction Data CSV")
            Toast.makeText(context, "Exported ${filtered.size} records to CSV locally!", Toast.LENGTH_SHORT).show()
            return cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "CSV Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    /**
     * Exports interaction records to a styled PDF document using Android's native PdfDocument.
     * Saves locally and opens share/save chooser.
     */
    fun exportInteractionsToPdf(
        context: Context,
        interactions: List<InteractionRecord>,
        dateRangeDays: Int
    ): File? {
        try {
            val filtered = filterInteractionsByDateRange(interactions, dateRangeDays)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.parseColor("#1C1B1F")
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#49454F")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val headerBgPaint = Paint().apply {
                color = Color.parseColor("#E8DEF8")
            }

            val headerTextPaint = Paint().apply {
                color = Color.parseColor("#1D192B")
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val rowTextPaint = Paint().apply {
                color = Color.parseColor("#1C1B1F")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val rowAltBgPaint = Paint().apply {
                color = Color.parseColor("#F7F2FA")
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#CAC4D0")
                strokeWidth = 0.8f
            }

            val cardBgPaint = Paint().apply {
                color = Color.parseColor("#F3EDF7")
            }

            var y = 40f

            // Title
            canvas.drawText("PersonaMesh - Interaction Data Report", 40f, y, titlePaint)
            y += 20f

            // Metadata
            val exportTimeStr = dateFormat.format(Date())
            canvas.drawText("Generated: $exportTimeStr  |  Filter: Last $dateRangeDays Days  |  Records: ${filtered.size}", 40f, y, subtitlePaint)
            y += 20f

            // Summary Metrics Box
            val totalTokens = filtered.sumOf { it.totalTokens }
            val avgLatency = if (filtered.isNotEmpty()) filtered.map { it.latencyMs }.average() else 0.0
            val summaryText = "Total Interactions: ${filtered.size}   |   Total Tokens: $totalTokens   |   Avg Latency: ${String.format(Locale.US, "%.1f", avgLatency)} ms"

            canvas.drawRect(40f, y, pageWidth - 40f, y + 26f, cardBgPaint)
            canvas.drawText(summaryText, 50f, y + 17f, headerTextPaint)
            y += 38f

            // Table Columns
            fun drawTableHeader(c: Canvas, currentY: Float) {
                c.drawRect(40f, currentY, pageWidth - 40f, currentY + 20f, headerBgPaint)
                c.drawText("Date & Time", 45f, currentY + 14f, headerTextPaint)
                c.drawText("Agent", 155f, currentY + 14f, headerTextPaint)
                c.drawText("Model", 255f, currentY + 14f, headerTextPaint)
                c.drawText("Tokens", 340f, currentY + 14f, headerTextPaint)
                c.drawText("Latency", 400f, currentY + 14f, headerTextPaint)
                c.drawText("Snippet", 460f, currentY + 14f, headerTextPaint)
            }

            drawTableHeader(canvas, y)
            y += 20f

            val rowHeight = 18f

            filtered.forEachIndexed { index, rec ->
                if (y + rowHeight > pageHeight - 50f) {
                    // Footer on current page
                    canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - 25f, subtitlePaint)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    y = 40f
                    drawTableHeader(canvas, y)
                    y += 20f
                }

                if (index % 2 == 1) {
                    canvas.drawRect(40f, y, pageWidth - 40f, y + rowHeight, rowAltBgPaint)
                }

                val dateStr = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(rec.timestamp))
                val agentStr = truncateString(rec.agentName, 15)
                val modelStr = truncateString(rec.modelUsed, 12)
                val snippetStr = truncateString(rec.snippet.replace("\n", " "), 18)

                canvas.drawText(dateStr, 45f, y + 13f, rowTextPaint)
                canvas.drawText(agentStr, 155f, y + 13f, rowTextPaint)
                canvas.drawText(modelStr, 255f, y + 13f, rowTextPaint)
                canvas.drawText(rec.totalTokens.toString(), 340f, y + 13f, rowTextPaint)
                canvas.drawText("${rec.latencyMs}ms", 400f, y + 13f, rowTextPaint)
                canvas.drawText(snippetStr, 460f, y + 13f, rowTextPaint)

                canvas.drawLine(40f, y + rowHeight, pageWidth - 40f, y + rowHeight, linePaint)
                y += rowHeight
            }

            // Footer
            canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - 25f, subtitlePaint)
            pdfDocument.finishPage(page)

            val fileName = "interaction_report_${dateRangeDays}d_${System.currentTimeMillis()}.pdf"
            val cacheFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(cacheFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            // Save copy to Documents
            val docDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (docDir != null) {
                if (!docDir.exists()) docDir.mkdirs()
                val persistentFile = File(docDir, fileName)
                cacheFile.copyTo(persistentFile, overwrite = true)
            }

            shareFile(context, cacheFile, "application/pdf", "Export Interaction Data PDF")
            Toast.makeText(context, "Exported PDF report locally!", Toast.LENGTH_SHORT).show()
            return cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    /**
     * Captures and renders a high-resolution PNG screenshot of the chart view
     * including data points, trend lines, and summary metrics for sharing or saving.
     */
    fun exportChartScreenshotToPng(
        context: Context,
        chartTitle: String = "Agent Colony Activity Trends",
        dateRangeDays: Int = 7,
        dailyTotals: List<Float> = emptyList(),
        dateLabels: List<String> = emptyList()
    ): File? {
        val width = 1200
        val height = 800
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Paints
        val bgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#12141C")
            style = android.graphics.Paint.Style.FILL
        }
        val cardPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1E2230")
            style = android.graphics.Paint.Style.FILL
        }
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 38f
            isAntiAlias = true
            isFakeBoldText = true
        }
        val subtitlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#9AA2B8")
            textSize = 22f
            isAntiAlias = true
        }
        val gridPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#2C3246")
            strokeWidth = 2f
            style = android.graphics.Paint.Style.STROKE
        }
        val linePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#6366F1") // Indigo primary
            strokeWidth = 6f
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
        }
        val pointPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#A5B4FC")
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw Canvas Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw Card Frame
        val margin = 50f
        canvas.drawRoundRect(margin, margin, width - margin, height - margin, 24f, 24f, cardPaint)

        // Draw Header
        canvas.drawText(chartTitle, margin + 40f, margin + 70f, titlePaint)
        val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        canvas.drawText("Range: Last $dateRangeDays Days • Exported: $timestampStr", margin + 40f, margin + 110f, subtitlePaint)

        // Chart Plot Area
        val plotLeft = margin + 80f
        val plotTop = margin + 160f
        val plotRight = width - margin - 60f
        val plotBottom = height - margin - 100f

        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        // Draw Grid Lines & Y-Axis Labels
        val gridRows = 4
        val maxVal = (dailyTotals.maxOrNull() ?: 10f).coerceAtLeast(5f)
        for (i in 0..gridRows) {
            val y = plotBottom - (plotHeight * i / gridRows)
            canvas.drawLine(plotLeft, y, plotRight, y, gridPaint)
            val yLabel = String.format(Locale.US, "%.0f", maxVal * i / gridRows)
            canvas.drawText(yLabel, plotLeft - 50f, y + 8f, subtitlePaint)
        }

        // Draw Data Series (Line + Points)
        if (dailyTotals.isNotEmpty()) {
            val count = dailyTotals.size
            val stepX = if (count > 1) plotWidth / (count - 1) else plotWidth

            val path = android.graphics.Path()
            for (i in dailyTotals.indices) {
                val x = plotLeft + i * stepX
                val normY = (dailyTotals[i] / maxVal).coerceIn(0f, 1f)
                val y = plotBottom - (normY * plotHeight)

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            canvas.drawPath(path, linePaint)

            // Draw Points and X-Axis Labels
            for (i in dailyTotals.indices) {
                val x = plotLeft + i * stepX
                val normY = (dailyTotals[i] / maxVal).coerceIn(0f, 1f)
                val y = plotBottom - (normY * plotHeight)

                canvas.drawCircle(x, y, 9f, pointPaint)

                if (i < dateLabels.size) {
                    val label = dateLabels[i]
                    canvas.drawText(label, x - 20f, plotBottom + 40f, subtitlePaint)
                }
            }
        }

        // Footer Branding
        canvas.drawText("⚡ AI Studio Colony Platform - Verified Chart Snapshot", margin + 40f, height - margin - 30f, subtitlePaint)

        // Save PNG to cache & share
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "chart_screenshot_$timeStamp.png"
            val cacheDir = context.cacheDir
            val cacheFile = File(cacheDir, fileName)

            val fos = FileOutputStream(cacheFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            // Save copy to Pictures
            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (picturesDir != null) {
                if (!picturesDir.exists()) picturesDir.mkdirs()
                val persistentFile = File(picturesDir, fileName)
                cacheFile.copyTo(persistentFile, overwrite = true)
            }

            shareFile(context, cacheFile, "image/png", "Share Chart Screenshot")
            Toast.makeText(context, "Chart screenshot saved & shared!", Toast.LENGTH_SHORT).show()
            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Chart Screenshot Failed: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun filterInteractionsByDateRange(
        interactions: List<InteractionRecord>,
        dateRangeDays: Int
    ): List<InteractionRecord> {
        if (dateRangeDays <= 0) return interactions.sortedByDescending { it.timestamp }
        val now = System.currentTimeMillis()
        val rangeMs = dateRangeDays * 24 * 60 * 60 * 1000L
        val cutoff = now - rangeMs
        return interactions.filter { it.timestamp >= cutoff }.sortedByDescending { it.timestamp }
    }

    private fun escapeCsv(value: String): String {
        val safe = value.replace("\"", "\"\"")
        return "\"$safe\""
    }

    private fun truncateString(str: String, maxLength: Int): String {
        if (str.length <= maxLength) return str
        return str.substring(0, maxLength - 1) + "…"
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
