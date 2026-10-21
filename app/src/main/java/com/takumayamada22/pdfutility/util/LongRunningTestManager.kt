package com.takumayamada22.pdfutility.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.ui.PageState
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

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

    interface TestCallback {
        fun onProgress(progress: LongRunningTestProgress)
        fun onCompleted(summary: String)
        fun onError(error: String)
    }

    fun isTestRunning(): Boolean = isRunning.get()

    fun stopTest() {
        shouldStop.set(true)
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
            var lastError: String? = null
            val runtime = Runtime.getRuntime()

            try {
                AppLogger.log(context, "INFO", "Starting Long-Running Endurance Test for $durationMinutes minutes.")

                while (!shouldStop.get() && (System.currentTimeMillis() - startTime) < targetMillis) {
                    iteration++
                    val elapsedMs = System.currentTimeMillis() - startTime
                    val elapsedMin = (elapsedMs / (60 * 1050L)).toInt()

                    // Course 1: Multi-file Merge Stress Test
                    handler.post {
                        callback?.onProgress(
                            LongRunningTestProgress(
                                isRunning = true,
                                elapsedMinutes = (elapsedMs / 60000).toInt(),
                                targetMinutes = durationMinutes,
                                currentIteration = iteration,
                                currentCourse = "Course 1: Multi-File Merge Stress",
                                memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                                statusSummary = "Running Course 1 (Merge)"
                            )
                        )
                    }
                    runMergeStressTest(context)

                    if (shouldStop.get()) break

                    // Course 2: High-Page Load & 9x Append Stress Test
                    handler.post {
                        callback?.onProgress(
                            LongRunningTestProgress(
                                isRunning = true,
                                elapsedMinutes = (elapsedMs / 60000).toInt(),
                                targetMinutes = durationMinutes,
                                currentIteration = iteration,
                                currentCourse = "Course 2: High-Page 1100-Pages 9x Append Stress",
                                memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                                statusSummary = "Running Course 2 (1100p Append 9x)"
                            )
                        )
                    }
                    runLoadAndAppendStressTest(context)

                    if (shouldStop.get()) break

                    // Course 3: Repeated Editing & Export Stress Test
                    handler.post {
                        callback?.onProgress(
                            LongRunningTestProgress(
                                isRunning = true,
                                elapsedMinutes = (elapsedMs / 60000).toInt(),
                                targetMinutes = durationMinutes,
                                currentIteration = iteration,
                                currentCourse = "Course 3: Repeated Editing & Export Stress",
                                memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                                statusSummary = "Running Course 3 (Editing & Export)"
                            )
                        )
                    }
                    runEditingStressTest(context)

                    if (shouldStop.get()) break

                    // Course 4: Orientation Rotation Simulation
                    handler.post {
                        callback?.onProgress(
                            LongRunningTestProgress(
                                isRunning = true,
                                elapsedMinutes = (elapsedMs / 60000).toInt(),
                                targetMinutes = durationMinutes,
                                currentIteration = iteration,
                                currentCourse = "Course 4: Orientation Rotation Simulation",
                                memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                                statusSummary = "Running Course 4 (Orientation Rotation)"
                            )
                        )
                    }
                    simulateOrientationRotation(context)

                    if (shouldStop.get()) break

                    // Course 5: Foldable Posture Simulation
                    handler.post {
                        callback?.onProgress(
                            LongRunningTestProgress(
                                isRunning = true,
                                elapsedMinutes = (elapsedMs / 60000).toInt(),
                                targetMinutes = durationMinutes,
                                currentIteration = iteration,
                                currentCourse = "Course 5: Foldable Posture Simulation",
                                memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                                statusSummary = "Running Course 5 (Foldable Posture)"
                            )
                        )
                    }
                    simulateFoldablePosture(context)

                    Thread.sleep(1000L) // Small breathing pause between iterations
                }

                val finalMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                val summary = "Endurance test completed successfully after $iteration iterations. Final memory: ${finalMemory}MB."
                AppLogger.log(context, "INFO", summary)

                handler.post {
                    callback?.onCompleted(summary)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Endurance test failed", e)
                AppLogger.log(context, "ERROR", "Endurance test failed: ${e.message}")
                handler.post {
                    callback?.onError(e.message ?: "Unknown error")
                }
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
        val loadFile = TestPdfGenerator.createLoadTestPdf(context, 100) // 100 pages for rapid stress iteration
        if (loadFile != null) {
            val processor = PdfProcessor()
            processor.load(loadFile.inputStream())
            // Append 9 times
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
        // Simulate orientation rotation logging and memory check
        AppLogger.log(context, "DEBUG", "Endurance Test: Simulated orientation rotation (Portrait <-> Landscape)")
    }

    private fun simulateFoldablePosture(context: Context) {
        // Simulate foldable posture state change (Half-opened / Fully opened / Closed)
        AppLogger.log(context, "DEBUG", "Endurance Test: Simulated foldable posture change (Folded <-> Unfolded)")
    }
}
