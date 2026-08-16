package com.example.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter

object ChartDataExportUtils {
    fun exportToCsv(context: Context, data: List<Pair<Float, Float>>, title: String) {
        try {
            val safeTitle = title.replace(" ", "_").lowercase()
            val fileName = "\${safeTitle}_export.csv"
            val file = File(context.cacheDir, fileName)
            val writer = FileWriter(file)
            
            writer.append("Timestamp,Value\n")
            for (point in data) {
                writer.append("\${point.first},\${point.second}\n")
            }
            writer.flush()
            writer.close()
            
            val uri = FileProvider.getUriForFile(context, "\${context.packageName}.provider", file)
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Eksport danych: \$title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Udostępnij dane wykresu"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
