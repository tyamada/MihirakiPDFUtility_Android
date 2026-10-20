package com.takumayamada22.pdfutility.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.StatFs
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object TestPdfGenerator {
    private const val TAG = "TestPdfGenerator"

    const val FILE_FUNCTIONAL = "test_functional.pdf"
    const val FILE_LOAD = "test_load_1000.pdf"
    const val FILE_SEARCH = "test_search.pdf"

    // Storage threshold constants
    const val MIN_STORAGE_REQUIRED_BYTES = 10L * 1024 * 1024 // 10 MB absolute minimum
    const val LOAD_TEST_STORAGE_REQUIRED_BYTES = 50L * 1024 * 1024 // 50 MB recommended for 1000-page load test

    fun getAvailableStorageBytes(context: Context): Long {
        return try {
            val cacheDir = context.cacheDir
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val stat = StatFs(cacheDir.absolutePath)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get available storage", e)
            0L
        }
    }

    fun createFunctionalTestPdf(context: Context): File? {
        val freeBytes = getAvailableStorageBytes(context)
        if (freeBytes < MIN_STORAGE_REQUIRED_BYTES) {
            Log.w(TAG, "Insufficient storage for functional test PDF: ${freeBytes} bytes available")
            return null
        }

        val file = File(context.cacheDir, FILE_FUNCTIONAL)
        try {
            val document = PdfDocument()
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
            }
            val titlePaint = Paint().apply {
                color = Color.BLUE
                textSize = 18f
                isFakeBoldText = true
            }

            // Generate 5 pages for functional testing
            for (pageIndex in 1..5) {
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageIndex).create()
                val page = document.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                canvas.drawText("MihirakiPDF Utility - Functional Test Page $pageIndex", 50f, 50f, titlePaint)
                canvas.drawText("This is an in-app generated test PDF for functional testing.", 50f, 90f, paint)
                canvas.drawText("Checking layout, rendering, spread views, and metadata editing.", 50f, 120f, paint)

                for (line in 1..20) {
                    canvas.drawText("Sample line $line on test page $pageIndex for view rendering verification.", 50f, (140 + (line * 25)).toFloat(), paint)
                }

                document.finishPage(page)
            }

            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create functional test PDF", e)
            return null
        }
    }

    fun createLoadTestPdf(context: Context, pageCount: Int = 1000): File? {
        val freeBytes = getAvailableStorageBytes(context)
        if (freeBytes < LOAD_TEST_STORAGE_REQUIRED_BYTES) {
            Log.w(TAG, "Insufficient storage for load test PDF ($pageCount pages): ${freeBytes} bytes available")
            return null
        }

        val file = File(context.cacheDir, FILE_LOAD)
        try {
            val document = PdfDocument()
            val paint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
            }

            // Generate high page count efficiently for load testing
            for (pageIndex in 1..pageCount) {
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageIndex).create()
                val page = document.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                canvas.drawText("Load Test Page $pageIndex / $pageCount", 50f, 50f, paint)
                canvas.drawText("Memory & Rendering Benchmark Item #$pageIndex", 50f, 80f, paint)

                document.finishPage(page)
            }

            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create load test PDF", e)
            return null
        }
    }

    fun createSearchTestPdf(context: Context): File? {
        val freeBytes = getAvailableStorageBytes(context)
        if (freeBytes < MIN_STORAGE_REQUIRED_BYTES) {
            Log.w(TAG, "Insufficient storage for search test PDF")
            return null
        }

        val file = File(context.cacheDir, FILE_SEARCH)
        try {
            val document = PdfDocument()
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
            }

            // Generate pages with specific search target keywords
            for (pageIndex in 1..10) {
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageIndex).create()
                val page = document.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                canvas.drawText("Search Test Page $pageIndex", 50f, 50f, paint)
                canvas.drawText("Target Keyword: MihirakiPDFSearchKeyword_$pageIndex", 50f, 100f, paint)
                canvas.drawText("Another target: PerformanceTarget", 50f, 130f, paint)

                document.finishPage(page)
            }

            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create search test PDF", e)
            return null
        }
    }

    fun cleanupTestPdfs(context: Context) {
        try {
            listOf(FILE_FUNCTIONAL, FILE_LOAD, FILE_SEARCH).forEach { fileName ->
                val file = File(context.cacheDir, fileName)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup test PDFs", e)
        }
    }
}
