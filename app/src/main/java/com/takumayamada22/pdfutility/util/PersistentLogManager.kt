package com.takumayamada22.pdfutility.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PersistentLogManager {
    private const val TAG = "PersistentLog"
    private const val LOG_DIR_NAME = "app_logs"
    private const val MAX_RETENTION_HOURS = 24L
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private var logDir: File? = null

    fun init(context: Context) {
        if (logDir == null) {
            logDir = File(context.filesDir, LOG_DIR_NAME).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            cleanupOldLogs()
        }
    }

    @Synchronized
    fun log(context: Context, level: String, tag: String, message: String) {
        val sanitizedMessage = if (message.length > 1000) message.substring(0, 1000) + "... [truncated]" else message
        val timeStr = timeFormat.format(Date())
        val logLine = "[$timeStr] [$level] [$tag]: $sanitizedMessage\n"

        AppLogger.log(context, level, "[$tag] $sanitizedMessage")

        logDir?.let { dir ->
            try {
                val dateStr = dateFormat.format(Date())
                val logFile = File(dir, "log_$dateStr.txt")
                FileWriter(logFile, true).use { writer ->
                    writer.append(logLine)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write persistent log", e)
            }
        }
    }

    fun getAllLogs(): List<String> {
        val dir = logDir ?: return emptyList()
        val files = dir.listFiles { file -> file.name.startsWith("log_") && file.name.endsWith(".txt") }
            ?.sortedByDescending { it.name } ?: emptyList()

        val allLines = mutableListOf<String>()
        for (file in files) {
            try {
                allLines.add("--- File: ${file.name} ---")
                allLines.addAll(file.readLines())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read log file ${file.name}", e)
            }
        }
        return allLines
    }

    fun cleanupOldLogs() {
        val dir = logDir ?: return
        val now = System.currentTimeMillis()
        val maxAgeMillis = MAX_RETENTION_HOURS * 60 * 60 * 1000L

        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                val age = now - file.lastModified()
                if (age > maxAgeMillis) {
                    file.delete()
                }
            }
        }
    }

    fun clearAllLogs() {
        logDir?.listFiles()?.forEach { it.delete() }
    }
}
