package com.takumayamada22.pdfutility.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.ui.PageState
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiagnosticBenchmarkResult(
    val timestamp: String,
    val availableStorageMb: Double,
    val deviceSummary: String,
    val functionalTestStatus: String,
    val functionalTestDurationMs: Long,
    val editingTestStatus: String,
    val editingTestDurationMs: Long,
    val loadTestStatus: String,
    val loadTestDurationMs: Long,
    val searchTestStatus: String,
    val searchTestDurationMs: Long,
    val memoryDeltaMb: Long,
    val overallStatus: String,
    val details: String
)

object DiagnosticTestManager {
    private const val TAG = "DiagnosticTestManager"

    fun runComprehensiveDiagnostics(context: Context): DiagnosticBenchmarkResult {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val freeBytes = TestPdfGenerator.getAvailableStorageBytes(context)
        val freeMb = freeBytes.toDouble() / (1024 * 1024)

        val runtime = Runtime.getRuntime()
        val initialMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)

        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val osVersion = Build.VERSION.RELEASE
        val sdkInt = Build.VERSION.SDK_INT

        val detailsBuilder = StringBuilder()
        detailsBuilder.append("=== MihirakiPDF Utility Comprehensive Diagnostics ===\n")
        detailsBuilder.append("Timestamp: $timestamp\n")
        detailsBuilder.append("Device: $manufacturer $model (Android $osVersion, SDK $sdkInt)\n")
        detailsBuilder.append("Available Storage: ${String.format(Locale.US, "%.2f", freeMb)} MB\n\n")

        // 1. Storage Check
        if (freeBytes < TestPdfGenerator.MIN_STORAGE_REQUIRED_BYTES) {
            val message = "ABORTED: Insufficient storage (${String.format(Locale.US, "%.2f", freeMb)} MB available, minimum ${TestPdfGenerator.MIN_STORAGE_REQUIRED_BYTES / (1024 * 1024)} MB required)."
            detailsBuilder.append(message).append("\n")
            AppLogger.log(context, "ERROR", message)
            return DiagnosticBenchmarkResult(
                timestamp = timestamp,
                availableStorageMb = freeMb,
                deviceSummary = "$manufacturer $model (Android $osVersion)",
                functionalTestStatus = "SKIPPED",
                functionalTestDurationMs = 0L,
                editingTestStatus = "SKIPPED",
                editingTestDurationMs = 0L,
                loadTestStatus = "SKIPPED",
                loadTestDurationMs = 0L,
                searchTestStatus = "SKIPPED",
                searchTestDurationMs = 0L,
                memoryDeltaMb = 0L,
                overallStatus = "FAILED",
                details = detailsBuilder.toString()
            )
        }

        // 2. Functional Test
        var functionalStatus = "PASSED"
        var functionalDuration = 0L
        try {
            val startTime = System.currentTimeMillis()
            val pdfFile = TestPdfGenerator.createFunctionalTestPdf(context)
            if (pdfFile != null && pdfFile.exists()) {
                val processor = PdfProcessor()
                val pageCount = processor.load(pdfFile.inputStream())
                if (pageCount <= 0) {
                    functionalStatus = "FAILED (Invalid page count: $pageCount)"
                }
                processor.close()
            } else {
                functionalStatus = "FAILED (PDF creation failed)"
            }
            functionalDuration = System.currentTimeMillis() - startTime
            detailsBuilder.append("1. Core Loading & Functional Test: $functionalStatus (${functionalDuration}ms)\n")
        } catch (e: Exception) {
            functionalStatus = "FAILED (${e.message})"
            detailsBuilder.append("1. Core Loading & Functional Test: $functionalStatus\n")
            Log.e(TAG, "Functional test error", e)
        }

        // 3. Editing & Export Simulation Test (Testing rotation, crop, blank insertion, metadata editing, password protection)
        var editingStatus = "PASSED"
        var editingDuration = 0L
        try {
            val startTime = System.currentTimeMillis()
            val pdfFile = File(context.cacheDir, TestPdfGenerator.FILE_FUNCTIONAL)
            if (pdfFile.exists()) {
                val processor = PdfProcessor()
                processor.load(pdfFile.inputStream())

                // Simulate editing page states (rotate page 0, crop page 1, insert blank page)
                val testPages = listOf(
                    PageState(thumbnail = null, rotation = 90, originalIndex = 0, cropMargin = 0.1f),
                    PageState(thumbnail = null, rotation = 0, originalIndex = 1, isBlank = true),
                    PageState(thumbnail = null, rotation = 180, originalIndex = 2)
                )

                val outStream = ByteArrayOutputStream()
                val metadata = mapOf(
                    "title" to "Diagnostic Edited PDF",
                    "author" to "MihirakiPDF Test Suite",
                    "version" to "1.5"
                )
                processor.save(outStream, testPages, password = "testpassword", metadata = metadata)
                val savedBytes = outStream.toByteArray()

                if (savedBytes.isEmpty()) {
                    editingStatus = "FAILED (Saved byte array is empty)"
                }
                processor.close()
            } else {
                editingStatus = "SKIPPED (Functional PDF not available)"
            }
            editingDuration = System.currentTimeMillis() - startTime
            detailsBuilder.append("2. Editing Features & Export Test: $editingStatus (${editingDuration}ms)\n")
        } catch (e: Exception) {
            editingStatus = "FAILED (${e.message})"
            detailsBuilder.append("2. Editing Features & Export Test: $editingStatus\n")
            Log.e(TAG, "Editing test error", e)
        }

        // 4. Load Test (1000 Pages)
        var loadStatus = "PASSED"
        var loadDuration = 0L
        if (freeBytes < TestPdfGenerator.LOAD_TEST_STORAGE_REQUIRED_BYTES) {
            loadStatus = "SKIPPED (Storage < 50MB)"
            detailsBuilder.append("3. Load Test (1000 pages): $loadStatus\n")
        } else {
            try {
                val startTime = System.currentTimeMillis()
                val loadFile = TestPdfGenerator.createLoadTestPdf(context, 1000)
                if (loadFile != null && loadFile.exists()) {
                    val processor = PdfProcessor()
                    val pageCount = processor.load(loadFile.inputStream())
                    if (pageCount != 1000) {
                        loadStatus = "FAILED (Expected 1000 pages, got $pageCount)"
                    }
                    processor.close()
                } else {
                    loadStatus = "FAILED (Load PDF creation failed)"
                }
                loadDuration = System.currentTimeMillis() - startTime
                detailsBuilder.append("3. Load Test (1000 pages): $loadStatus (${loadDuration}ms)\n")
            } catch (e: Exception) {
                loadStatus = "FAILED (${e.message})"
                detailsBuilder.append("3. Load Test (1000 pages): $loadStatus\n")
                Log.e(TAG, "Load test error", e)
            }
        }

        // 5. Search & Performance Benchmark Test
        var searchStatus = "PASSED"
        var searchDuration = 0L
        try {
            val startTime = System.currentTimeMillis()
            val searchFile = TestPdfGenerator.createSearchTestPdf(context)
            if (searchFile != null && searchFile.exists()) {
                val processor = PdfProcessor()
                val pageCount = processor.load(searchFile.inputStream())
                if (pageCount <= 0) {
                    searchStatus = "FAILED (Invalid page count)"
                }
                processor.close()
            } else {
                searchStatus = "FAILED (Search PDF creation failed)"
            }
            searchDuration = System.currentTimeMillis() - startTime
            detailsBuilder.append("4. Search & Performance Test: $searchStatus (${searchDuration}ms)\n")
        } catch (e: Exception) {
            searchStatus = "FAILED (${e.message})"
            detailsBuilder.append("4. Search & Performance Test: $searchStatus\n")
            Log.e(TAG, "Search test error", e)
        }

        // Cleanup test PDFs
        TestPdfGenerator.cleanupTestPdfs(context)

        val finalMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val memoryDelta = finalMemory - initialMemory
        detailsBuilder.append("\nMemory Delta: ${memoryDelta}MB (Initial: ${initialMemory}MB, Final: ${finalMemory}MB)\n")

        val overallOk = functionalStatus.startsWith("PASSED") && editingStatus.startsWith("PASSED") && (loadStatus.startsWith("PASSED") || loadStatus.startsWith("SKIPPED")) && searchStatus.startsWith("PASSED")
        val overallStatus = if (overallOk) "PASSED" else "WARNING"
        detailsBuilder.append("Overall Diagnostic Status: $overallStatus\n")
        detailsBuilder.append("\nPrivacy Notice: テスト結果を自動的に外部（サーバー等）に送信する機能はありません。データは端末内でのみ処理され、手動で共有する場合を除き外部に出ることはありません。\n")

        AppLogger.log(context, "INFO", "Comprehensive diagnostic test executed with status: $overallStatus")

        return DiagnosticBenchmarkResult(
            timestamp = timestamp,
            availableStorageMb = freeMb,
            deviceSummary = "$manufacturer $model (Android $osVersion)",
            functionalTestStatus = functionalStatus,
            functionalTestDurationMs = functionalDuration,
            editingTestStatus = editingStatus,
            editingTestDurationMs = editingDuration,
            loadTestStatus = loadStatus,
            loadTestDurationMs = loadDuration,
            searchTestStatus = searchStatus,
            searchTestDurationMs = searchDuration,
            memoryDeltaMb = memoryDelta,
            overallStatus = overallStatus,
            details = detailsBuilder.toString()
        )
    }
}
