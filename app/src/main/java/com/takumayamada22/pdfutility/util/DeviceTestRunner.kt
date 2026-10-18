package com.takumayamada22.pdfutility.util

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.StatFs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceTestResult(
    val timestamp: String,
    val manufacturer: String,
    val model: String,
    val osVersion: String,
    val sdkInt: Int,
    val totalMemoryMb: Long,
    val freeMemoryMb: Long,
    val totalRamMb: Long,
    val freeRamMb: Long,
    val totalStorageGb: Double,
    val freeStorageGb: Double,
    val isLowRamDevice: Boolean,
    val cpuCores: Int,
    val screenResolution: String,
    val systemLocale: String,
    val deviceOrientation: String,
    val themeMode: String,
    val fontScale: Float,
    val cacheDirWritable: Boolean,
    val pdfRendererApiWorking: Boolean,
    val contentResolverAvailable: Boolean,
    val status: String
)

object DeviceTestRunner {
    fun runTest(context: Context): DeviceTestResult {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val runtime = Runtime.getRuntime()
        val totalMem = runtime.totalMemory() / 1024 / 1024
        val freeMem = runtime.freeMemory() / 1024 / 1024

        // RAM Info
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamMb = memoryInfo.totalMem / 1024 / 1024
        val freeRamMb = memoryInfo.availMem / 1024 / 1024
        val totalRamGb = memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
        val isLowRamDevice = activityManager.isLowRamDevice || totalRamGb < 4.0

        // Storage Info
        val stat = StatFs(context.filesDir.absolutePath)
        val totalStorageBytes = stat.totalBytes
        val availableStorageBytes = stat.availableBytes
        val totalStorageGb = totalStorageBytes.toDouble() / (1024 * 1024 * 1024)
        val freeStorageGb = availableStorageBytes.toDouble() / (1024 * 1024 * 1024)

        // CPU & Screen & System Settings
        val cpuCores = runtime.availableProcessors()
        val metrics = context.resources.displayMetrics
        val screenResolution = "${metrics.widthPixels}x${metrics.heightPixels} (${metrics.densityDpi}dpi)"
        
        val configuration = context.resources.configuration
        val systemLocale = Locale.getDefault().toLanguageTag()
        val deviceOrientation = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "Landscape (横)" else "Portrait (縦)"
        val isDarkMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val themeMode = if (isDarkMode) "Dark (ダーク)" else "Light (ライト)"
        val fontScale = configuration.fontScale

        val cacheWritable = context.cacheDir?.let { it.exists() || it.mkdirs() } ?: false

        // API Functional Checks
        val pdfRendererApiWorking = runCatching {
            android.graphics.pdf.PdfRenderer::class.java.name.isNotBlank()
        }.getOrDefault(false)

        val contentResolverAvailable = runCatching {
            context.contentResolver != null
        }.getOrDefault(false)

        val allOk = cacheWritable && pdfRendererApiWorking && contentResolverAvailable

        return DeviceTestResult(
            timestamp = timestamp,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            totalMemoryMb = totalMem,
            freeMemoryMb = freeMem,
            totalRamMb = totalRamMb,
            freeRamMb = freeRamMb,
            totalStorageGb = totalStorageGb,
            freeStorageGb = freeStorageGb,
            isLowRamDevice = isLowRamDevice,
            cpuCores = cpuCores,
            screenResolution = screenResolution,
            systemLocale = systemLocale,
            deviceOrientation = deviceOrientation,
            themeMode = themeMode,
            fontScale = fontScale,
            cacheDirWritable = cacheWritable,
            pdfRendererApiWorking = pdfRendererApiWorking,
            contentResolverAvailable = contentResolverAvailable,
            status = if (allOk) "PASSED" else "WARNING"
        )
    }

    fun formatResultString(result: DeviceTestResult): String {
        return buildString {
            appendLine("=== MihirakiPDF Utility Device Test Report ===")
            appendLine("Timestamp: ${result.timestamp}")
            appendLine("Manufacturer: ${result.manufacturer}")
            appendLine("Model: ${result.model}")
            appendLine("OS Version: Android ${result.osVersion} (SDK ${result.sdkInt})")
            appendLine("System Locale: ${result.systemLocale}")
            appendLine("Device Orientation: ${result.deviceOrientation}")
            appendLine("Theme Mode: ${result.themeMode}")
            appendLine("Font Scale: ${result.fontScale}x")
            appendLine("Total RAM: ${result.totalRamMb} MB (Available: ${result.freeRamMb} MB)")
            appendLine("Total Storage: ${String.format(Locale.US, "%.2f", result.totalStorageGb)} GB (Available: ${String.format(Locale.US, "%.2f", result.freeStorageGb)} GB)")
            appendLine("Low RAM Optimization Mode: ${result.isLowRamDevice}")
            appendLine("CPU Cores: ${result.cpuCores}")
            appendLine("Screen Resolution: ${result.screenResolution}")
            appendLine("JVM Heap Total: ${result.totalMemoryMb} MB (Free: ${result.freeMemoryMb} MB)")
            appendLine("Cache Writable: ${result.cacheDirWritable}")
            appendLine("PDF Renderer API: ${result.pdfRendererApiWorking}")
            appendLine("ContentResolver (SAF): ${result.contentResolverAvailable}")
            appendLine("Status: ${result.status}")
            appendLine("-------------------------------------------")
            appendLine("Privacy Notice: Test results are NOT automatically sent externally.")
        }
    }
}
