package com.takumayamada22.pdfutility

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest
import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.data.SamplePdfItem

@RunWith(AndroidJUnit4::class)
class ShortsRecordingTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrument get() = InstrumentationRegistry.getInstrumentation()
    private val args get() = InstrumentationRegistry.getArguments()
    private lateinit var vm: PdfViewModel
    private val app get() = instrument.targetContext.applicationContext as Application
    private fun main(action: () -> Unit) = compose.runOnUiThread(action)

    private fun waitForReady(count: Int? = null) {
        Log.d("ShortsTest", "waitForReady: starting (target count = $count)")
        compose.waitUntil(90000) { !vm.uiState.value.isLoading && vm.uiState.value.pages.isNotEmpty() && (count == null || vm.uiState.value.pages.size == count) }
        assertNull(vm.uiState.value.errorMessage)
        compose.waitForIdle()
        Log.d("ShortsTest", "waitForReady: completed (page count = ${vm.uiState.value.pages.size})")
    }

    private fun clickAction(id: Int) {
        val label = compose.activity.getString(id)
        Log.d("ShortsTest", "clickAction: clicking label = $label")
        try {
            compose.onNodeWithContentDescription(label, useUnmergedTree = true).performClick()
        } catch (e: Exception) {
            Log.w("ShortsTest", "clickAction: contentDescription '$label' not found, trying text search")
            compose.onNodeWithText(label, useUnmergedTree = true).performClick()
        }
        compose.waitForIdle()
    }

    private fun preparedRecordingSample(id: String): File {
        val item = SamplePdfCatalog.items.single { it.id == id }
        val inputPath = requireNotNull(args.getString("sampleInputDir")) {
            "Pass -e sampleInputDir <device-directory> with verified ep1_en and ep2_en PDFs."
        }
        val inputDir = File(inputPath)
        require(inputDir.isAbsolute && inputDir.isDirectory) {
            "sampleInputDir must be an existing absolute device directory"
        }
        val source = File(inputDir, item.fileName)
        require(verifiedRecordingSample(source, item)) {
            "Missing or invalid recording input ${item.fileName}; prepare the fixed-manifest " +
                "sample ID $id and verify its byte size and SHA-256 before recording."
        }
        val cacheDir = File(app.cacheDir, "shorts_samples/${item.sha256}")
        check(cacheDir.isDirectory || cacheDir.mkdirs()) { "Recording cache unavailable" }
        val target = File(cacheDir, item.fileName)
        source.copyTo(target, overwrite = true)
        check(verifiedRecordingSample(target, item)) { "Recording input changed while copying" }
        return target
    }

    private fun verifiedRecordingSample(file: File, item: SamplePdfItem): Boolean {
        if (!file.isFile || file.length() != item.bytes) return false
        return file.inputStream().use { input ->
            val header = ByteArray(5)
            if (input.read(header) != 5 || !header.contentEquals("%PDF-".toByteArray())) return false
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(header)
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
            digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) } == item.sha256
        }
    }

    private fun loadEnglishPdf(count: Int = 8): File {
        Log.d("ShortsTest", "loadEnglishPdf: loading PDF with count = $count")
        if (Build.VERSION.SDK_INT >= 33) {
            val localeManager = app.getSystemService(android.app.LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags("en-US")
        }

        // This is real-manga recording, not a synthetic PDF unit test. Missing
        // prepared inputs must fail rather than produce a blank "successful" video.
        val pdfFile = preparedRecordingSample("ep1_en")
        preparedRecordingSample("ep2_en")
        check(SamplePdfCatalog.items.single { it.id == "ep1_en" }.pageCount == count) {
            "Recording scene page count differs from the fixed sample catalog"
        }

        main {
            vm.clearSelection()
            vm.loadPdf(Uri.fromFile(pdfFile))
        }
        waitForReady(count)
        if (vm.uiState.value.showTutorial) {
            main { vm.dismissTutorial() }
            compose.waitForIdle()
        }
        if (vm.uiState.value.showCcByDialog) {
            main { vm.confirmCcByDialog() }
            compose.waitForIdle()
        }
        Log.d("ShortsTest", "loadEnglishPdf: completed successfully")
        return pdfFile
    }

    @Test
    fun recordScene() {
        val scene = args.getString("scene") ?: "scene_01"
        Log.d("ShortsTest", "recordScene: starting scene = $scene")
        compose.activityRule.scenario.onActivity { activity ->
            vm = ViewModelProvider(activity, PdfViewModel.provideFactory(app))[PdfViewModel::class.java]
        }
        if (vm.uiState.value.showTutorial) {
            main { vm.dismissTutorial() }
            compose.waitForIdle()
        }

        val settingsDesc = compose.activity.getString(R.string.action_settings)
        val toggleViewText = compose.activity.getString(R.string.action_toggle_view)

        when (scene) {
            "scene_01" -> {
                loadEnglishPdf(8)
                Thread.sleep(3000)
                assertFalse(vm.uiState.value.isMihirakiView)
                Thread.sleep(1500)

                try {
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(1000)

                    try {
                        compose.onNode(hasText(toggleViewText), useUnmergedTree = true).performScrollTo().performClick()
                    } catch (e: Exception) {
                        compose.onNodeWithText(toggleViewText, useUnmergedTree = true).performClick()
                    }
                    compose.waitForIdle()
                } catch (e: Exception) {
                    main { vm.toggleMihirakiView() }
                }

                assertTrue(vm.uiState.value.isMihirakiView)
                Thread.sleep(15000)
            }
            "scene_02" -> {
                main { vm.clearSelection() }
                Thread.sleep(2500)

                if (vm.uiState.value.showTutorial) {
                    main { vm.dismissTutorial() }
                    compose.waitForIdle()
                }
                if (vm.uiState.value.showCcByDialog) {
                    main { vm.confirmCcByDialog() }
                    compose.waitForIdle()
                }
                Thread.sleep(1500)

                loadEnglishPdf(8)
                Thread.sleep(2500)

                try {
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(2500)
                } catch (e: Exception) {}

                Thread.sleep(6000)
            }
            "scene_03" -> {
                Log.d("ShortsTest", "Scene 3: Starting loadEnglishPdf")
                loadEnglishPdf(8)
                Thread.sleep(3000) // Clear initial view of thumbnail grid

                Log.d("ShortsTest", "Scene 3: Step 1 - Selecting page 0")
                main { vm.clearSelection(); vm.toggleSelection(0) }
                Thread.sleep(2500)
                assertTrue("Page 0 should be selected", vm.uiState.value.selectedIndices.contains(0))
                
                Log.d("ShortsTest", "Scene 3: Step 2 - Rotating page 90 degrees")
                main { vm.rotateSelected(90) }
                assertEquals("Page 0 rotation should be 90", 90, vm.uiState.value.pages[0].rotation)
                Thread.sleep(5000) // Distinct pause highlighting rotation result alone
                
                Log.d("ShortsTest", "Scene 3: Step 3 - Inserting blank page after page 0")
                main { vm.clearSelection(); vm.toggleSelection(0) }
                Thread.sleep(2000)
                main { vm.insertBlankAfterSelected() }
                waitForReady()
                assertEquals("Page count should be 9 after inserting blank page", 9, vm.uiState.value.pages.size)
                assertTrue("Inserted page should be blank", vm.uiState.value.pages[1].isBlank)
                Thread.sleep(5500) // Distinct pause highlighting blank page insertion result alone
                
                Log.d("ShortsTest", "Scene 3: Step 4 - Deleting page 2 with confirmation dialog")
                main { vm.clearSelection(); vm.toggleSelection(2) }
                Thread.sleep(2500)
                
                clickAction(R.string.action_delete)
                compose.onNodeWithText(compose.activity.getString(R.string.dialog_delete_title)).assertExists()
                compose.waitForIdle()
                Thread.sleep(1500) // Display confirmation dialog clearly on screen for the user
                
                main { vm.deleteSelected() }
                waitForReady()
                assertEquals("Page count should be 8 after deletion", 8, vm.uiState.value.pages.size)
                Thread.sleep(7000) // Hold final edited result steady showing page successfully removed!
                Log.d("ShortsTest", "Scene 3: Completed successfully")
            }
            "scene_04" -> {
                loadEnglishPdf(8)
                Thread.sleep(3000)
                try {
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(1000)
                    compose.onNodeWithText(toggleViewText, useUnmergedTree = true).performScrollTo().performClick()
                    compose.waitForIdle()
                } catch (e: Exception) {
                    main { vm.toggleMihirakiView() }
                }
                Thread.sleep(3000)

                try {
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(1000)
                    compose.onNodeWithText(toggleViewText, useUnmergedTree = true).performScrollTo().performClick()
                    compose.waitForIdle()
                } catch (e: Exception) {
                    main { vm.toggleMihirakiView() }
                }
                Thread.sleep(2500)

                try {
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(1000)
                    compose.onNodeWithText(compose.activity.getString(R.string.settings_options), useUnmergedTree = true).performScrollTo().performClick()
                    compose.waitForIdle()
                } catch (e: Exception) {
                    main { vm.openSettingsDialog(SettingsDialogType.OPTIONS) }
                }
                Thread.sleep(2000)
                
                try {
                    compose.onAllNodes(hasScrollAction())[1].performTouchInput {
                        swipeUp()
                    }
                } catch (e: Exception) {
                    try {
                        compose.onAllNodes(hasScrollAction())[0].performTouchInput {
                            swipeUp()
                        }
                    } catch (e2: Exception) {}
                }
                Thread.sleep(8000)
                
                main { vm.closeSettingsDialog() }
                Thread.sleep(3000)
            }
            "scene_05" -> {
                loadEnglishPdf(8)
                Thread.sleep(3000)
                
                try {
                    clickAction(R.string.action_save)
                } catch (e: Exception) {
                    try {
                        compose.onNodeWithText(compose.activity.getString(R.string.action_save), useUnmergedTree = true).performClick()
                    } catch (e: Exception) {
                        val saveFile = File(app.cacheDir, "shorts_export_en.pdf")
                        main { vm.savePdf(Uri.fromFile(saveFile)) }
                    }
                }
                waitForReady()
                Thread.sleep(9000)
            }
            else -> {
                Thread.sleep(5000)
            }
        }
    }
}
