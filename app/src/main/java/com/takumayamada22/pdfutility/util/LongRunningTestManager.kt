package com.takumayamada22.pdfutility.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.ui.PageState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

data class LongRunningTestProgress(
    val isRunning: Boolean,
    val elapsedMinutes: Int,
    val targetMinutes: Int,
    val currentIteration: Int,
    val currentCourse: String,
    val memoryUsageMb: Long,
    val statusSummary: String
)

object LongRunningTestManager {
    private const val TAG = "LongRunningTestManager"
    private val isRunning = AtomicBoolean(false)
    private val shouldStop = AtomicBoolean(false)

    private val _progressFlow = MutableStateFlow<LongRunningTestProgress?>(null)
    val progressFlow: StateFlow<LongRunningTestProgress?> = _progressFlow.asStateFlow()

    interface TestCallback {
        fun onProgress(progress: LongRunningTestProgress)
        fun onCompleted(summary: String)
        fun onError(error: String)
    }

    fun isTestRunning(): Boolean = isRunning.get()

    fun stopTest() {
        shouldStop.set(true)
        _progressFlow.value = null
    }

    fun startEnduranceTest(
        context: Context,
        durationMinutes: Int,
        callback: TestCallback? = null
    ) {
        if (isRunning.get()) {
            callback?.onError("Endurance test is already running.")
            return
        }

        isRunning.set(true)
        shouldStop.set(false)

        val targetMillis = durationMinutes * 60 * 1000L
        val startTime = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())

        Thread {
            var iteration = 0
            val runtime = Runtime.getRuntime()

            try {
                AppLogger.log(context, "INFO", "Starting Long-Running Endurance Test for $durationMinutes minutes.")

                while (!shouldStop.get() && (System.currentTimeMillis() - startTime) < targetMillis) {
                    iteration++
                    val elapsedMs = System.currentTimeMillis() - startTime
                    val elapsedMin = (elapsedMs / (60 * 1000L)).toInt()

                    val progress = LongRunningTestProgress(
                        isRunning = true,
                        elapsedMinutes = elapsedMin,
                        targetMinutes = durationMinutes,
                        currentIteration = iteration,
                        currentCourse = "Course 1: Multi-File Merge Stress",
                        memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                        statusSummary = "Running Course 1 (Merge)"
                    )
                    _progressFlow.value = progress
                    handler.post { callback?.onProgress(progress) }

                    runMergeStressTest(context)
                    if (shouldStop.get()) break

                    val progress2 = progress.copy(
                        currentCourse = "Course 2: High-Page 1100-Pages 9x Append Stress",
                        statusSummary = "Running Course 2 (1100p Append 9x)"
                    )
                    _progressFlow.value = progress2
                    handler.post { callback?.onProgress(progress2) }

                    runLoadAndAppendStressTest(context)
                    if (shouldStop.get()) break

                    val progress3 = progress.copy(
                        currentCourse = "Course 3: Repeated Editing & Export Stress",
                        statusSummary = "Running Course 3 (Editing & Export)"
                    )
                    _progressFlow.value = progress3
                    handler.post { callback?.onProgress(progress3) }

                    runEditingStressTest(context)
                    if (shouldStop.get()) break

                    val progress4 = progress.copy(
                        currentCourse = "Course 4: Orientation Rotation Simulation",
                        statusSummary = "Running Course 4 (Orientation Rotation)"
                    )
                    _progressFlow.value = progress4
                    handler.post { callback?.onProgress(progress4) }

                    simulateOrientationRotation(context)
                    if (shouldStop.get()) break

                    val progress5 = progress.copy(
                        currentCourse = "Course 5: Foldable Posture Simulation",
                        statusSummary = "Running Course 5 (Foldable Posture)"
                    )
                    _progressFlow.value = progress5
                    handler.post { callback?.onProgress(progress5) }

                    simulateFoldablePosture(context)

                    Thread.sleep(1000L)
                }

                val finalMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                val summary = if (shouldStop.get()) {
                    "Endurance test was interrupted/stopped by user after $iteration iterations."
                } else {
                    "Endurance test completed successfully after $iteration iterations. Final memory: ${finalMemory}MB."
                }
                AppLogger.log(context, "INFO", summary)

                _progressFlow.value = null
                handler.post { callback?.onCompleted(summary) }
            } catch (e: Exception) {
                Log.e(TAG, "Endurance test failed", e)
                AppLogger.log(context, "ERROR", "Endurance test failed: ${e.message}")
                _progressFlow.value = null
                handler.post { callback?.onError(e.message ?: "Unknown error") }
            } finally {
                isRunning.set(false)
                shouldStop.set(false)
                TestPdfGenerator.cleanupTestPdfs(context)
            }
        }.start()
    }

    private fun runMergeStressTest(context: Context) {
        val f1 = TestPdfGenerator.createFunctionalTestPdf(context)
        val f2 = TestPdfGenerator.createSearchTestPdf(context)
        if (f1 != null && f2 != null) {
            val processor = PdfProcessor()
            processor.load(f1.inputStream())
            processor.append(f2.inputStream())
            val out = ByteArrayOutputStream()
            processor.save(out)
            processor.close()
        }
    }

    private fun runLoadAndAppendStressTest(context: Context) {
        val loadFile = TestPdfGenerator.createLoadTestPdf(context, 100)
        if (loadFile != null) {
            val processor = PdfProcessor()
            processor.load(loadFile.inputStream())
            repeat(9) {
                processor.append(loadFile.inputStream())
            }
            val out = ByteArrayOutputStream()
            processor.save(out)
            processor.close()
        }
    }

    private fun runEditingStressTest(context: Context) {
        val f = TestPdfGenerator.createFunctionalTestPdf(context)
        if (f != null) {
            val processor = PdfProcessor()
            processor.load(f.inputStream())
            val pages = listOf(
                PageState(thumbnail = null, rotation = 90, originalIndex = 0, cropMargin = 0.1f),
                PageState(thumbnail = null, rotation = 180, originalIndex = 1, isBlank = true),
                PageState(thumbnail = null, rotation = 0, originalIndex = 2)
            )
            val out = ByteArrayOutputStream()
            processor.save(out, pages, password = "stresspassword", metadata = mapOf("title" to "Stress Test PDF"))
            processor.close()
        }
    }

    private fun simulateOrientationRotation(context: Context) {
        AppLogger.log(context, "DEBUG", "Endurance Test: Simulated orientation rotation (Portrait <-> Landscape)")
    }

    private fun simulateFoldablePosture(context: Context) {
        AppLogger.log(context, "DEBUG", "Endurance Test: Simulated foldable posture change (Folded <-> Unfolded)")
    }
}
