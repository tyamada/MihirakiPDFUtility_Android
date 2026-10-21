package com.takumayamada22.pdfutility.util

import android.app.Application
import android.net.Uri
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.ui.SettingsDialogType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class EnduranceTestExecutor(
    private val application: Application,
    private val viewModelActions: ViewModelActions
) {
    interface ViewModelActions {
        fun loadPdf(uri: Uri)
        fun appendPdf(uri: Uri)
        fun selectAll()
        fun rotateSelected(degrees: Int)
        fun cropSelectedPages(fraction: Float)
        fun toggleMihirakiView()
        fun toggleRtl()
        fun openSettingsDialog(type: SettingsDialogType)
        fun isProcessing(): Boolean
    }

    private val _enduranceProgress = MutableStateFlow<LongRunningTestProgress?>(null)
    val enduranceProgress: StateFlow<LongRunningTestProgress?> = _enduranceProgress.asStateFlow()

    private val _enduranceStatusMessage = MutableStateFlow<String?>(null)
    val enduranceStatusMessage: StateFlow<String?> = _enduranceStatusMessage.asStateFlow()

    private val isEnduranceRunning = AtomicBoolean(false)
    private var testJob: Job? = null

    val isRunning: Boolean get() = isEnduranceRunning.get()

    fun startEnduranceTest(durationMinutes: Int, selectedCourse: Int, scope: CoroutineScope) {
        if (isEnduranceRunning.get()) return
        isEnduranceRunning.set(true)
        _enduranceStatusMessage.value = "耐久テスト開始 (コース: $selectedCourse)..."

        testJob = scope.launch(Dispatchers.IO) {
            val targetMillis = durationMinutes * 60 * 1000L
            val startTime = System.currentTimeMillis()
            var iteration = 0
            val runtime = Runtime.getRuntime()

            try {
                AppLogger.log(application, "INFO", "Starting Endurance Test (Course: $selectedCourse) for $durationMinutes minutes.")

                while (isEnduranceRunning.get() && (System.currentTimeMillis() - startTime) < targetMillis) {
                    iteration++
                    val elapsedMs = System.currentTimeMillis() - startTime
                    val elapsedMin = (elapsedMs / (60 * 1000L)).toInt()

                    val progress = LongRunningTestProgress(
                        isRunning = true,
                        elapsedMinutes = elapsedMin,
                        targetMinutes = durationMinutes,
                        currentIteration = iteration,
                        currentCourse = "Course Mode: $selectedCourse (#$iteration)",
                        memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                        statusSummary = "Running Course $selectedCourse"
                    )
                    _enduranceProgress.value = progress

                    val testDataDir = File(application.getExternalFilesDir(null), "testdata")
                    val fallbackDir = File(application.filesDir, "testdata")
                    val activeDir = if (testDataDir.exists() && testDataDir.list()?.isNotEmpty() == true) testDataDir else fallbackDir

                    val filesToProcess = when (selectedCourse) {
                        1 -> {
                            val mangaFiles = activeDir.listFiles { file -> file.name.startsWith("tameshibu_") && file.name.endsWith(".pdf") }?.sortedBy { it.name } ?: emptyList()
                            if (mangaFiles.isEmpty()) {
                                throw java.io.FileNotFoundException("Required test files (tameshibu_*.pdf manga episode PDFs) not found in device testdata folder (${activeDir.absolutePath}). Please transfer test files from PC/Mac (testdata/) via ADB.")
                            }
                            mangaFiles
                        }
                        2 -> {
                            val loadFile = File(activeDir, "load_test_1100_pages.pdf")
                            if (!loadFile.exists()) {
                                throw java.io.FileNotFoundException("Required test file (load_test_1100_pages.pdf) not found in device testdata folder (${activeDir.absolutePath}). Please transfer test files from PC/Mac (testdata/) via ADB.")
                            }
                            listOf(loadFile)
                        }
                        else -> {
                            val f1 = TestPdfGenerator.createFunctionalTestPdf(application)
                            listOfNotNull(f1)
                        }
                    }

                    if (!isEnduranceRunning.get()) break

                    when (selectedCourse) {
                        1 -> {
                            val mergedFile = File(application.cacheDir, "merged_course1_${System.currentTimeMillis()}.pdf")
                            val processor = PdfProcessor()
                            try {
                                processor.load(filesToProcess[0].inputStream())
                                for (i in 1 until filesToProcess.size) {
                                    processor.append(filesToProcess[i].inputStream())
                                }
                                FileOutputStream(mergedFile).use { fos -> processor.save(fos) }
                            } finally {
                                processor.close()
                            }
                            withContext(Dispatchers.Main) {
                                if (!isEnduranceRunning.get()) return@withContext
                                if (mergedFile.exists()) {
                                    viewModelActions.loadPdf(Uri.fromFile(mergedFile))
                                }
                            }
                        }
                        2 -> {
                            val mergedFile = File(application.cacheDir, "merged_course2_${System.currentTimeMillis()}.pdf")
                            val processor = PdfProcessor()
                            try {
                                processor.load(filesToProcess[0].inputStream())
                                repeat(9) {
                                    processor.append(filesToProcess[0].inputStream())
                                }
                                FileOutputStream(mergedFile).use { fos -> processor.save(fos) }
                            } finally {
                                processor.close()
                            }
                            withContext(Dispatchers.Main) {
                                if (!isEnduranceRunning.get()) return@withContext
                                if (mergedFile.exists()) {
                                    viewModelActions.loadPdf(Uri.fromFile(mergedFile))
                                }
                            }
                        }
                        else -> {
                            withContext(Dispatchers.Main) {
                                if (!isEnduranceRunning.get()) return@withContext
                                val patternIndex = (iteration - 1) % 10
                                when (patternIndex) {
                                    0 -> { viewModelActions.loadPdf(Uri.fromFile(filesToProcess[0])) }
                                    1 -> { viewModelActions.loadPdf(Uri.fromFile(filesToProcess[0])); viewModelActions.selectAll(); viewModelActions.rotateSelected(90) }
                                    2 -> { viewModelActions.loadPdf(Uri.fromFile(filesToProcess[0])); viewModelActions.selectAll(); viewModelActions.cropSelectedPages(0.1f) }
                                    3 -> { viewModelActions.toggleMihirakiView(); viewModelActions.toggleRtl() }
                                    else -> { viewModelActions.loadPdf(Uri.fromFile(filesToProcess[0])) }
                                }
                            }
                        }
                    }

                    while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                        delay(100L)
                    }
                    delay(1000L)
                }

                val finalMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                val summary = "Endurance Test completed successfully. Final memory: ${finalMemory}MB."
                AppLogger.log(application, "INFO", summary)
                _enduranceProgress.value = null
                _enduranceStatusMessage.value = summary
                withContext(Dispatchers.Main) {
                    viewModelActions.openSettingsDialog(SettingsDialogType.DIAGNOSTICS)
                }
            } catch (e: Exception) {
                val errorMsg = "FATAL ERROR: ${e.message}"
                AppLogger.log(application, "FATAL", "$errorMsg\n${e.stackTraceToString()}")
                _enduranceProgress.value = null
                _enduranceStatusMessage.value = errorMsg
                isEnduranceRunning.set(false)
                withContext(Dispatchers.Main) {
                    viewModelActions.openSettingsDialog(SettingsDialogType.DIAGNOSTICS)
                }
            } finally {
                isEnduranceRunning.set(false)
                _enduranceProgress.value = null
                TestPdfGenerator.cleanupTestPdfs(application)
            }
        }
    }

    fun stopEnduranceTest() {
        isEnduranceRunning.set(false)
        testJob?.cancel()
        _enduranceProgress.value = null
        _enduranceStatusMessage.value = "テスト停止リクエスト送信中..."
    }
}
