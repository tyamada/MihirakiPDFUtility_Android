package com.takumayamada22.pdfutility.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val LOG_FILE_NAME = "app_audit_logs.txt"
    private const val MAX_RETENTION_HOURS = 24L
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    @Synchronized
    fun log(context: Context, level: String, message: String) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val timestamp = dateFormat.format(Date())
            val logEntry = "[$timestamp] [$level] $message\n"

            // Append log
            file.appendText(logEntry)

            // Clean up logs older than 24 hours
            cleanOldLogs(file)
        } catch (e: Exception) {
            Log.e("AppLogger", "Failed to write log", e)
        }
    }

    @Synchronized
    fun getLogs(context: Context): String {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (!file.exists()) return "No logs recorded."
            cleanOldLogs(file)
            return file.readText().ifEmpty { "No logs recorded." }
        } catch (e: Exception) {
            return "Failed to read logs: ${e.message}"
        }
    }

    private fun cleanOldLogs(file: File) {
        if (!file.exists()) return
        try {
            val cutoffTime = System.currentTimeMillis() - (MAX_RETENTION_HOURS * 60 * 60 * 1000L)
            val lines = file.readLines()
            val validLines = mutableListOf<String>()

            for (line in lines) {
                // Expecting format: [yyyy-MM-dd HH:mm:ss] [LEVEL] message
                if (line.length >= 19) {
                    val dateStr = line.substring(1, 20)
                    try {
                        val date = dateFormat.parse(dateStr)
                        if (date != null && date.time >= cutoffTime) {
                            validLines.add(line)
                        }
                    } catch (_: Exception) {
                        // If parsing fails, keep or discard safely
                        validLines.add(line)
                    }
                }
            }

            file.writeText(validLines.joinToString("\n") + if (validLines.isNotEmpty()) "\n" else "")
        } catch (e: Exception) {
            Log.e("AppLogger", "Failed to clean old logs", e)
        }
    }
}
