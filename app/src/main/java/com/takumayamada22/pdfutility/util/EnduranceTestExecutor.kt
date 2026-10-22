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

data class LayoutState(
    val isMihirakiView: Boolean,
    val isRtl: Boolean
)

class EnduranceTestExecutor(
    private val application: Application,
    private val viewModelActions: ViewModelActions
) {
    interface ViewModelActions {
        fun loadPdf(uri: Uri)
        fun appendPdf(uri: Uri)
        fun selectAll()
        fun clearSelection()
        fun invertSelection()
        fun rotateSelected(degrees: Int)
        fun cropSelectedPages(fraction: Float)
        fun insertBlankAfterSelected()
        fun moveSelected(offset: Int)
        fun toggleMihirakiView()
        fun toggleRtl()
        fun ensureSinglePageView()
        fun setOrientation(orientationCode: Int)
        fun getInitialOrientationCode(): Int
        fun isFoldableDevice(): Boolean
        fun getLayoutState(): LayoutState
        fun setLayoutState(state: LayoutState)
        fun undo()
        fun redo()
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

    private suspend fun executePattern(courseNum: Int, filesToProcess: List<File>) {
        // Initial state setup: Ensure non-spread (single page view) and clear all selection before each pattern starts
        withContext(Dispatchers.Main) {
            if (!isEnduranceRunning.get()) return@withContext
            viewModelActions.clearSelection()
            viewModelActions.ensureSinglePageView()
        }
        delay(500L)

        when (courseNum) {
            1 -> { // Course 1: Merge ALL sample manga files (all 14 files) via incremental batch merging with live progress
                withContext(Dispatchers.Main) {
                    if (!isEnduranceRunning.get()) return@withContext
                    _enduranceStatusMessage.value = "コース1: マンガ全14冊の結合を開始..."
                }
                val mergedFile = File(application.cacheDir, "merged_course1_${System.currentTimeMillis()}.pdf")
                val batchSize = 4
                var currentMerged: File? = null

                try {
                    var startIndex = 0
                    while (startIndex < filesToProcess.size) {
                        val endIndex = minOf(startIndex + batchSize, filesToProcess.size)
                        val batch = filesToProcess.subList(startIndex, endIndex)
                        
                        withContext(Dispatchers.Main) {
                            if (!isEnduranceRunning.get()) return@withContext
                            _enduranceStatusMessage.value = "コース1: マンガファイル結合中 (${startIndex + 1}〜${endIndex} / ${filesToProcess.size})..."
                        }

                        val batchFile = File(application.cacheDir, "batch_${System.currentTimeMillis()}_${startIndex}.pdf")
                        val processor = PdfProcessor()
                        try {
                            if (currentMerged != null && currentMerged.exists()) {
                                processor.load(currentMerged.inputStream())
                                for (file in batch) {
                                    processor.append(file.inputStream())
                                }
                            } else {
                                processor.load(batch[0].inputStream())
                                for (i in 1 until batch.size) {
                                    processor.append(batch[i].inputStream())
                                }
                            }
                            FileOutputStream(batchFile).use { fos -> processor.save(fos) }
                        } finally {
                            processor.close()
                        }

                        if (currentMerged != null && currentMerged.exists()) {
                            currentMerged.delete()
                        }
                        currentMerged = batchFile
                        startIndex = endIndex
                        System.gc()
                    }

                    if (currentMerged != null && currentMerged.exists()) {
                        currentMerged.renameTo(mergedFile)
                    }
                } catch (e: Exception) {
                    AppLogger.log(application, "ERROR", "Batch merge failed: ${e.message}")
                    throw e
                }

                withContext(Dispatchers.Main) {
                    if (!isEnduranceRunning.get()) return@withContext
                    if (mergedFile.exists()) {
                        _enduranceStatusMessage.value = "コース1: 結合完了。PDFを読み込み中..."
                        viewModelActions.loadPdf(Uri.fromFile(mergedFile))
                    }
                }
            }
            2 -> { // Course 2: load_test_1100_pages.pdf appended 9 times via incremental batching
                val mergedFile = File(application.cacheDir, "merged_course2_${System.currentTimeMillis()}.pdf")
                var currentMerged: File? = null
                try {
                    var remaining = 9
                    while (remaining > 0) {
                        val count = minOf(3, remaining)
                        val batchFile = File(application.cacheDir, "batch_c2_${System.currentTimeMillis()}.pdf")
                        val processor = PdfProcessor()
                        try {
                            if (currentMerged != null && currentMerged.exists()) {
                                processor.load(currentMerged.inputStream())
                                repeat(count) {
                                    processor.append(filesToProcess[0].inputStream())
                                }
                            } else {
                                processor.load(filesToProcess[0].inputStream())
                                repeat(count) {
                                    processor.append(filesToProcess[0].inputStream())
                                }
                            }
                            FileOutputStream(batchFile).use { fos -> processor.save(fos) }
                        } finally {
                            processor.close()
                        }
                        if (currentMerged != null && currentMerged.exists()) {
                            currentMerged.delete()
                        }
                        currentMerged = batchFile
                        remaining -= count
                        System.gc()
                    }
                    if (currentMerged != null && currentMerged.exists()) {
                        currentMerged.renameTo(mergedFile)
                    }
                } catch (e: Exception) {
                    AppLogger.log(application, "ERROR", "Course 2 batch append failed: ${e.message}")
                    throw e
                }
                withContext(Dispatchers.Main) {
                    if (!isEnduranceRunning.get()) return@withContext
                    if (mergedFile.exists()) {
                        viewModelActions.loadPdf(Uri.fromFile(mergedFile))
                    }
                }
            }
            3, 4 -> { // Pattern 3 & 4: Base (Load -> SelectAll -> Crop) + 100 Random Edit Operations (>= 1.0s interval)
                withContext(Dispatchers.Main) {
                    if (!isEnduranceRunning.get()) return@withContext
                    AppLogger.log(application, "INFO", "Pattern $courseNum: Starting base sequence (Load -> SelectAll -> Crop)")
                    viewModelActions.loadPdf(Uri.fromFile(filesToProcess[0]))
                }
                while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                    delay(100L)
                }
                delay(1000L)

                withContext(Dispatchers.Main) {
                    if (!isEnduranceRunning.get()) return@withContext
                    viewModelActions.selectAll()
                    viewModelActions.cropSelectedPages(0.1f)
                }
                while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                    delay(100L)
                }
                delay(1000L)

                val random = java.util.Random()
                for (opCount in 1..100) {
                    if (!isEnduranceRunning.get()) break
                    val actionChoice = random.nextInt(10)
                    AppLogger.log(application, "DEBUG", "Pattern $courseNum: Random edit operation #$opCount/100")

                    withContext(Dispatchers.Main) {
                        if (!isEnduranceRunning.get()) return@withContext
                        when (actionChoice) {
                            0 -> viewModelActions.rotateSelected(90)
                            1 -> viewModelActions.rotateSelected(180)
                            2 -> viewModelActions.rotateSelected(-90)
                            3 -> viewModelActions.insertBlankAfterSelected()
                            4 -> viewModelActions.moveSelected(if (random.nextBoolean()) 1 else -1)
                            5 -> viewModelActions.invertSelection()
                            6 -> { viewModelActions.clearSelection(); viewModelActions.selectAll() }
                            7 -> viewModelActions.toggleMihirakiView()
                            8 -> viewModelActions.toggleRtl()
                            9 -> { if (random.nextBoolean()) viewModelActions.undo() else viewModelActions.redo() }
                        }
                    }

                    while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                        delay(100L)
                    }
                    delay(1000L) // >= 1.0 second interval after each operation
                }
            }
            5, 6 -> { // Pattern 5 & 6: Clockwise 90deg x 4 then Counter-clockwise 90deg x 4 set repeated 25 times at >= 1.0s intervals
                withContext(Dispatchers.Main) {
                    if (!isEnduranceRunning.get()) return@withContext
                    viewModelActions.loadPdf(Uri.fromFile(filesToProcess[0]))
                }
                while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                    delay(100L)
                }
                delay(1000L)

                val clockwiseSequence = listOf(0, 1, 2, 3, 0)
                val counterClockwiseSequence = listOf(0, 3, 2, 1, 0)
                val fullSet = clockwiseSequence + counterClockwiseSequence // 10 steps

                for (setIndex in 1..25) {
                    if (!isEnduranceRunning.get()) break
                    AppLogger.log(application, "DEBUG", "Pattern $courseNum: Rotation set #$setIndex/25")
                    for (orientCode in fullSet) {
                        if (!isEnduranceRunning.get()) break
                        withContext(Dispatchers.Main) {
                            if (!isEnduranceRunning.get()) return@withContext
                            viewModelActions.setOrientation(orientCode)
                        }
                        while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                            delay(100L)
                        }
                        delay(1000L) // >= 1.0s interval
                    }
                }
            }
            7, 8 -> { // Pattern 7 & 8: Foldable posture simulation (only if device is foldable) with layout restore
                if (!viewModelActions.isFoldableDevice()) {
                    val skipMsg = "Pattern $courseNum skipped: Device is not a foldable device."
                    AppLogger.log(application, "INFO", skipMsg)
                    withContext(Dispatchers.Main) {
                        if (!isEnduranceRunning.get()) return@withContext
                        _enduranceStatusMessage.value = skipMsg
                    }
                    delay(2000L)
                    return
                }

                val initialLayout = viewModelActions.getLayoutState()
                try {
                    withContext(Dispatchers.Main) {
                        if (!isEnduranceRunning.get()) return@withContext
                        AppLogger.log(application, "INFO", "Pattern $courseNum: Starting foldable posture simulation")
                        viewModelActions.loadPdf(Uri.fromFile(filesToProcess[0]))
                    }
                    while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                        delay(100L)
                    }
                    delay(1000L)

                    val random = java.util.Random()
                    for (opCount in 1..100) {
                        if (!isEnduranceRunning.get()) break
                        val orientCode = random.nextInt(4)
                        AppLogger.log(application, "DEBUG", "Pattern $courseNum: Foldable posture change #$opCount/100")
                        withContext(Dispatchers.Main) {
                            if (!isEnduranceRunning.get()) return@withContext
                            viewModelActions.setOrientation(orientCode)
                        }
                        while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                            delay(100L)
                        }
                        delay(1000L)
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        viewModelActions.setLayoutState(initialLayout)
                    }
                }
            }
            9, 10 -> { // Pattern 9 & 10: After Course 1/2, 300 ops if foldable, else 200 ops
                if (courseNum == 9) {
                    val mergedFile = File(application.cacheDir, "merged_p9_${System.currentTimeMillis()}.pdf")
                    val batchSize = 4
                    var currentMerged: File? = null
                    try {
                        var startIndex = 0
                        while (startIndex < filesToProcess.size) {
                            val endIndex = minOf(startIndex + batchSize, filesToProcess.size)
                            val batch = filesToProcess.subList(startIndex, endIndex)
                            val batchFile = File(application.cacheDir, "batch_p9_${System.currentTimeMillis()}_${startIndex}.pdf")
                            val processor = PdfProcessor()
                            try {
                                if (currentMerged != null && currentMerged.exists()) {
                                    processor.load(currentMerged.inputStream())
                                    for (file in batch) { processor.append(file.inputStream()) }
                                } else {
                                    processor.load(batch[0].inputStream())
                                    for (i in 1 until batch.size) { processor.append(batch[i].inputStream()) }
                                }
                                FileOutputStream(batchFile).use { fos -> processor.save(fos) }
                            } finally {
                                processor.close()
                            }
                            if (currentMerged != null && currentMerged.exists()) currentMerged.delete()
                            currentMerged = batchFile
                            startIndex = endIndex
                            System.gc()
                        }
                        if (currentMerged != null && currentMerged.exists()) currentMerged.renameTo(mergedFile)
                    } catch (e: Exception) {
                        AppLogger.log(application, "ERROR", "Pattern 9 merge failed: ${e.message}")
                        throw e
                    }
                    withContext(Dispatchers.Main) {
                        if (!isEnduranceRunning.get()) return@withContext
                        if (mergedFile.exists()) viewModelActions.loadPdf(Uri.fromFile(mergedFile))
                    }
                } else {
                    val mergedFile = File(application.cacheDir, "merged_p10_${System.currentTimeMillis()}.pdf")
                    var currentMerged: File? = null
                    try {
                        var remaining = 9
                        while (remaining > 0) {
                            val count = minOf(3, remaining)
                            val batchFile = File(application.cacheDir, "batch_p10_${System.currentTimeMillis()}.pdf")
                            val processor = PdfProcessor()
                            try {
                                if (currentMerged != null && currentMerged.exists()) {
                                    processor.load(currentMerged.inputStream())
                                    repeat(count) { processor.append(filesToProcess[0].inputStream()) }
                                } else {
                                    processor.load(filesToProcess[0].inputStream())
                                    repeat(count) { processor.append(filesToProcess[0].inputStream()) }
                                }
                                FileOutputStream(batchFile).use { fos -> processor.save(fos) }
                            } finally {
                                processor.close()
                            }
                            if (currentMerged != null && currentMerged.exists()) currentMerged.delete()
                            currentMerged = batchFile
                            remaining -= count
                            System.gc()
                        }
                        if (currentMerged != null && currentMerged.exists()) currentMerged.renameTo(mergedFile)
                    } catch (e: Exception) {
                        AppLogger.log(application, "ERROR", "Pattern 10 append failed: ${e.message}")
                        throw e
                    }
                    withContext(Dispatchers.Main) {
                        if (!isEnduranceRunning.get()) return@withContext
                        if (mergedFile.exists()) viewModelActions.loadPdf(Uri.fromFile(mergedFile))
                    }
                }

                while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                    delay(100L)
                }
                delay(1000L)

                val isFoldable = viewModelActions.isFoldableDevice()
                val totalOps = if (isFoldable) 300 else 200
                AppLogger.log(application, "INFO", "Pattern $courseNum: Running $totalOps consecutive operations (Foldable=$isFoldable)")

                val random = java.util.Random()
                repeat(totalOps) {
                    if (!isEnduranceRunning.get()) return@repeat
                    withContext(Dispatchers.Main) {
                        if (!isEnduranceRunning.get()) return@withContext
                        val choice = random.nextInt(if (isFoldable) 3 else 2)
                        when (choice) {
                            0 -> {
                                val editChoice = random.nextInt(10)
                                when (editChoice) {
                                    0 -> viewModelActions.rotateSelected(90)
                                    1 -> viewModelActions.rotateSelected(180)
                                    2 -> viewModelActions.rotateSelected(-90)
                                    3 -> viewModelActions.insertBlankAfterSelected()
                                    4 -> viewModelActions.moveSelected(if (random.nextBoolean()) 1 else -1)
                                    5 -> viewModelActions.invertSelection()
                                    6 -> { viewModelActions.clearSelection(); viewModelActions.selectAll() }
                                    7 -> viewModelActions.toggleMihirakiView()
                                    8 -> viewModelActions.toggleRtl()
                                    9 -> { if (random.nextBoolean()) viewModelActions.undo() else viewModelActions.redo() }
                                }
                            }
                            1 -> viewModelActions.setOrientation(random.nextInt(4))
                            2 -> viewModelActions.setOrientation(random.nextInt(4))
                        }
                    }
                    while (isEnduranceRunning.get() && viewModelActions.isProcessing()) {
                        delay(100L)
                    }
                    delay(1000L)
                }
            }
        }
    }

    fun startEnduranceTest(durationMinutes: Int, selectedCourse: Int, scope: CoroutineScope) {
        if (isEnduranceRunning.get()) return
        isEnduranceRunning.set(true)
        _enduranceStatusMessage.value = "耐久テスト開始 (コース: $selectedCourse)..."

        testJob = scope.launch(Dispatchers.IO) {
            val targetMillis = durationMinutes * 60 * 1000L
            val startTime = System.currentTimeMillis()
            var iteration = 0
            val runtime = Runtime.getRuntime()
            val originalOrientation = viewModelActions.getInitialOrientationCode()

            try {
                AppLogger.log(application, "INFO", "Starting Endurance Test (Course: $selectedCourse) for $durationMinutes minutes.")

                while (isEnduranceRunning.get() && (System.currentTimeMillis() - startTime) < targetMillis) {
                    iteration++
                    val elapsedMs = System.currentTimeMillis() - startTime
                    val elapsedMin = (elapsedMs / (60 * 1000L)).toInt()

                    val courseNum = if (selectedCourse in 1..10) selectedCourse else ((iteration - 1) % 10) + 1
                    val courseLabel = if (selectedCourse == 0) "All (Pattern $courseNum)" else "Course $selectedCourse (Pattern $courseNum)"

                    val progress = LongRunningTestProgress(
                        isRunning = true,
                        elapsedMinutes = elapsedMin,
                        targetMinutes = durationMinutes,
                        currentIteration = iteration,
                        currentCourse = courseLabel,
                        memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                        statusSummary = "Running Pattern $courseNum [Mode: $selectedCourse]"
                    )
                    _enduranceProgress.value = progress

                    val testDataDir = File(application.getExternalFilesDir(null), "testdata")
                    val fallbackDir = File(application.filesDir, "testdata")
                    val activeDir = if (testDataDir.exists() && testDataDir.list()?.isNotEmpty() == true) testDataDir else fallbackDir

                    val filesToProcess = when (courseNum) {
                        1, 3, 4, 5, 6, 7, 8, 9 -> {
                            val mangaFiles = activeDir.listFiles { file -> file.name.startsWith("tameshibu_") && file.name.endsWith(".pdf") }?.sortedBy { it.name } ?: emptyList()
                            if (mangaFiles.isEmpty()) {
                                throw java.io.FileNotFoundException("Required test files (tameshibu_*.pdf manga episode PDFs) not found in device testdata folder (${activeDir.absolutePath}). Please transfer test files from PC/Mac (testdata/) via ADB.")
                            }
                            mangaFiles
                        }
                        2, 10 -> {
                            val loadFile = File(activeDir, "load_test_1100_pages.pdf")
                            if (!loadFile.exists()) {
                                throw java.io.FileNotFoundException("Required test file (load_test_1100_pages.pdf) not found in device testdata folder (${activeDir.absolutePath}). Please transfer test files from PC/Mac (testdata/) via ADB.")
                            }
                            listOf(loadFile)
                        }
                        else -> {
                            throw java.io.FileNotFoundException("Required test files not found.")
                        }
                    }

                    if (!isEnduranceRunning.get()) break

                    executePattern(courseNum, filesToProcess)

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
                    viewModelActions.setOrientation(originalOrientation)
                    viewModelActions.openSettingsDialog(SettingsDialogType.DIAGNOSTICS)
                }
            } catch (e: Exception) {
                val errorMsg = "FATAL ERROR: ${e.message}"
                AppLogger.log(application, "FATAL", "$errorMsg\n${e.stackTraceToString()}")
                _enduranceProgress.value = null
                _enduranceStatusMessage.value = errorMsg
                isEnduranceRunning.set(false)
                withContext(Dispatchers.Main) {
                    viewModelActions.setOrientation(originalOrientation)
                    viewModelActions.openSettingsDialog(SettingsDialogType.DIAGNOSTICS)
                }
            } finally {
                isEnduranceRunning.set(false)
                _enduranceProgress.value = null
                withContext(Dispatchers.Main) {
                    viewModelActions.setOrientation(originalOrientation)
                }
                TestPdfGenerator.cleanupTestPdfs(application)
            }
        }
    }

    fun stopEnduranceTest() {
        isEnduranceRunning.set(false)
        testJob?.cancel()
        _enduranceProgress.value = null
        _enduranceStatusMessage.value = "テスト停止リクエスト送信中..."
        AppLogger.log(application, "INFO", "Endurance test was interrupted/stopped by user.")
        val originalOrientation = viewModelActions.getInitialOrientationCode()
        CoroutineScope(Dispatchers.Main).launch {
            viewModelActions.setOrientation(originalOrientation)
            viewModelActions.openSettingsDialog(SettingsDialogType.DIAGNOSTICS)
        }
    }
}
