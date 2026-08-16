package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.AgentInteractionFtsContent
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for exporting complete PDF reports containing agent activity charts,
 * statistical summaries, and tabular event logs.
 */
object PdfReportGenerator {

    fun generateReportPdf(
        context: Context,
        reportTitle: String,
        chartBitmap: Bitmap?,
        metrics: Map<String, String>,
        eventLogs: List<AgentInteractionFtsContent>
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in points at 72dpi
        val pageHeight = 842 // A4 height in points at 72dpi

        val paint = Paint().apply { isAntiAlias = true }
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 20f
            isFakeBoldText = true
            color = Color.rgb(30, 41, 59)
        }
        val subTitlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
            color = Color.rgb(100, 116, 139)
        }
        val headerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 14f
            isFakeBoldText = true
            color = Color.rgb(15, 23, 42)
        }
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.rgb(51, 65, 85)
        }
        val tableHeaderPaint = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            isFakeBoldText = true
            color = Color.rgb(255, 255, 255)
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // --- PAGE 1: Header, Stats, Chart ---
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // Accent top banner
        paint.color = Color.rgb(16, 185, 129) // Primary Emerald
        canvas1.drawRect(0f, 0f, pageWidth.toFloat(), 12f, paint)

        var y = 45f
        canvas1.drawText(reportTitle, 36f, y, titlePaint)
        y += 18f
        canvas1.drawText("Wygenerowano: $dateStr | System EDDE+ AI Colony Control", 36f, y, subTitlePaint)

        y += 30f
        // Metrics Summary Header
        canvas1.drawText("Podsumowanie Statystyczne Wydajności", 36f, y, headerPaint)
        y += 12f

        // Draw Metrics Cards
        var xCard = 36f
        val cardWidth = 120f
        val cardHeight = 50f
        paint.color = Color.rgb(241, 245, 249)

        metrics.forEach { (label, value) ->
            if (xCard + cardWidth > pageWidth - 36) {
                xCard = 36f
                y += cardHeight + 10f
            }
            canvas1.drawRoundRect(xCard, y, xCard + cardWidth, y + cardHeight, 8f, 8f, paint)

            val valPaint = Paint().apply {
                isAntiAlias = true
                textSize = 14f
                isFakeBoldText = true
                color = Color.rgb(15, 23, 42)
            }
            val lblPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9f
                color = Color.rgb(100, 116, 139)
            }
            canvas1.drawText(value, xCard + 10f, y + 22f, valPaint)
            canvas1.drawText(label, xCard + 10f, y + 38f, lblPaint)

            xCard += cardWidth + 12f
        }

        y += cardHeight + 30f

        // Render Chart Image if present
        if (chartBitmap != null) {
            canvas1.drawText("Wykres Aktywności Agentów (Visual Multi-Series)", 36f, y, headerPaint)
            y += 15f

            val availableWidth = (pageWidth - 72).toFloat()
            val aspectRatio = chartBitmap.height.toFloat() / chartBitmap.width.toFloat()
            val scaledHeight = (availableWidth * aspectRatio).coerceAtMost(280f)

            val destRect = Rect(36, y.toInt(), (36 + availableWidth).toInt(), (y + scaledHeight).toInt())
            canvas1.drawBitmap(chartBitmap, null, destRect, paint)
            y += scaledHeight + 30f
        }

        // Preview of Event Log Table Header on Page 1 if space permits
        canvas1.drawText("Dziennik Zdarzeń i Aktywności (Tabela)", 36f, y, headerPaint)
        pdfDocument.finishPage(page1)

        // --- PAGE 2+: Event Log Table ---
        val recordsPerPage = 22
        val totalPages = (eventLogs.size + recordsPerPage - 1) / recordsPerPage
        val pageCount = if (totalPages < 1) 1 else totalPages

        for (p in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 2).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Page header
            var py = 40f
            canvas.drawText("$reportTitle - Tabela Zdarzeń (Strona ${p + 1}/${pageCount})", 36f, py, subTitlePaint)
            py += 20f

            // Table Header Bar
            paint.color = Color.rgb(30, 41, 59)
            canvas.drawRoundRect(36f, py, (pageWidth - 36).toFloat(), py + 24f, 4f, 4f, paint)

            canvas.drawText("Czas", 44f, py + 16f, tableHeaderPaint)
            canvas.drawText("Agent", 140f, py + 16f, tableHeaderPaint)
            canvas.drawText("Model", 240f, py + 16f, tableHeaderPaint)
            canvas.drawText("Treść Zdarzenia / Fragment", 340f, py + 16f, tableHeaderPaint)

            py += 28f

            val startIndex = p * recordsPerPage
            val endIndex = (startIndex + recordsPerPage).coerceAtMost(eventLogs.size)

            val rowBgPaint = Paint()
            for (i in startIndex until endIndex) {
                val logItem = eventLogs[i]
                if ((i - startIndex) % 2 == 1) {
                    rowBgPaint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(36f, py - 12f, (pageWidth - 36).toFloat(), py + 16f, rowBgPaint)
                }

                val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(logItem.timestamp))
                val truncatedSnippet = if (logItem.snippet.length > 35) logItem.snippet.take(35) + "..." else logItem.snippet

                canvas.drawText(timeFormatted, 44f, py, bodyPaint)
                canvas.drawText(logItem.agentName.take(14), 140f, py, bodyPaint)
                canvas.drawText(logItem.modelUsed.take(14), 240f, py, bodyPaint)
                canvas.drawText(truncatedSnippet, 340f, py, bodyPaint)

                py += 28f
            }

            pdfDocument.finishPage(page)
        }

        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val pdfFile = File(reportsDir, "Colony_Report_${System.currentTimeMillis()}.pdf")
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        } catch (e: Exception) {
            Log.e("PdfReportGenerator", "Error writing PDF report", e)
            pdfDocument.close()
            null
        }
    }
}
