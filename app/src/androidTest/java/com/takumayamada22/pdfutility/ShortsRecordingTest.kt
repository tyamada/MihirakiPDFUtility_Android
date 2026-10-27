package com.takumayamada22.pdfutility

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.ui.*
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ShortsRecordingTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrument get() = InstrumentationRegistry.getInstrumentation()
    private val args get() = InstrumentationRegistry.getArguments()
    private lateinit var vm: PdfViewModel
    private val app get() = instrument.targetContext.applicationContext as Application
    private fun main(action: () -> Unit) = compose.runOnUiThread(action)

    private fun waitForReady(count: Int? = null) {
        compose.waitUntil(90000) { !vm.uiState.value.isLoading && vm.uiState.value.pages.isNotEmpty() && (count == null || vm.uiState.value.pages.size == count) }
        assertNull(vm.uiState.value.errorMessage)
        compose.waitForIdle()
    }

    private fun loadEnglishPdf(count: Int = 8): File {
        if (Build.VERSION.SDK_INT >= 33) {
            val localeManager = app.getSystemService(android.app.LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags("en-US")
        }

        val pdfFile = File(app.filesDir, "tameshibu_episode1_2_en.pdf")
        val tmpFile = File("/data/local/tmp/tameshibu_episode1_2_en.pdf")
        if (tmpFile.exists()) {
            tmpFile.copyTo(pdfFile, overwrite = true)
        } else if (!pdfFile.exists()) {
            val doc = PDDocument()
            repeat(count) { doc.addPage(PDPage()) }
            FileOutputStream(pdfFile).use { doc.save(it) }
            doc.close()
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
        return pdfFile
    }

    @Test
    fun recordScene() {
        val scene = args.getString("scene") ?: "scene_01"
        compose.activityRule.scenario.onActivity { activity ->
            vm = ViewModelProvider(activity, PdfViewModel.provideFactory(app))[PdfViewModel::class.java]
        }
        if (vm.uiState.value.showTutorial) {
            main { vm.dismissTutorial() }
            compose.waitForIdle()
        }
        loadEnglishPdf(8)

        val settingsDesc = compose.activity.getString(R.string.action_settings)
        val toggleViewText = compose.activity.getString(R.string.action_toggle_view)

        when (scene) {
            "scene_01" -> {
                // Scene 1: Thumbnail grid view -> tap 3 dots overflow menu, scroll menu, and tap "Toggle View" (~15s)
                Thread.sleep(2000)
                assertFalse(vm.uiState.value.isMihirakiView) // Starts in thumbnail grid view
                Thread.sleep(1500)

                try {
                    // Tap 3 dots overflow menu button at top right
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(1000)

                    // Scroll menu down if needed and click "Toggle View"
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
                Thread.sleep(8000) // Hold spread view to showcase two pages side-by-side
            }
            "scene_02" -> {
                // Scene 2: PDF loading & toolbar/menu interaction (~15s)
                Thread.sleep(2000)
                val extraFile = File(app.filesDir, "tameshibu_episode2_2_en.pdf")
                val tmpExtra = File("/data/local/tmp/tameshibu_episode2_2_en.pdf")
                if (tmpExtra.exists()) {
                    tmpExtra.copyTo(extraFile, overwrite = true)
                } else if (!extraFile.exists()) {
                    val doc = PDDocument()
                    repeat(4) { doc.addPage(PDPage()) }
                    FileOutputStream(extraFile).use { doc.save(it) }
                    doc.close()
                }
                val beforeCount = vm.uiState.value.pages.size
                main { vm.appendPdf(Uri.fromFile(extraFile)) }
                compose.waitUntil(90000) { !vm.uiState.value.isLoading && vm.uiState.value.pages.size > beforeCount }
                waitForReady()
                Thread.sleep(3000)
                main { vm.toggleSelection(0) }
                Thread.sleep(2000)
                main { vm.clearSelection() }
                Thread.sleep(4000)
            }
            "scene_03" -> {
                // Scene 3: Page editing (Rotate, Insert Blank, Delete) (~20s)
                Thread.sleep(2000)
                main { vm.toggleSelection(0) }
                Thread.sleep(1500)
                main { vm.rotateSelected(90) }
                Thread.sleep(2500)
                main { vm.insertBlankAfterSelected() }
                waitForReady()
                Thread.sleep(3000)
                main { vm.clearSelection(); vm.toggleSelection(2) }
                Thread.sleep(1500)
                main { vm.deleteSelected() }
                waitForReady()
                Thread.sleep(4000)
            }
            "scene_04" -> {
                // Scene 4: Display toggle (Thumbnail grid vs Spread) via 3 dots menu & options (~15s)
                Thread.sleep(2000)
                try {
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(800)
                    compose.onNodeWithText(toggleViewText, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                } catch (e: Exception) {
                    main { vm.toggleMihirakiView() }
                }
                Thread.sleep(2500)

                try {
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(800)
                    compose.onNodeWithText(toggleViewText, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                } catch (e: Exception) {
                    main { vm.toggleMihirakiView() }
                }
                Thread.sleep(2000)

                try {
                    compose.onNodeWithContentDescription(settingsDesc, useUnmergedTree = true).performClick()
                    compose.waitForIdle()
                    Thread.sleep(800)
                    compose.onNodeWithText(compose.activity.getString(R.string.settings_options), useUnmergedTree = true).performScrollTo().performClick()
                    compose.waitForIdle()
                } catch (e: Exception) {
                    main { vm.openSettingsDialog(SettingsDialogType.OPTIONS) }
                }
                Thread.sleep(3000)
                main { vm.closeSettingsDialog() }
                Thread.sleep(2000)
            }
            "scene_05" -> {
                // Scene 5: Save & Export (~15s)
                Thread.sleep(2000)
                main { vm.openSettingsDialog(SettingsDialogType.PROPERTY) }
                Thread.sleep(2000)
                main { vm.closeSettingsDialog() }
                Thread.sleep(1000)
                val saveFile = File(app.cacheDir, "shorts_export_en.pdf")
                main { vm.savePdf(Uri.fromFile(saveFile)) }
                waitForReady()
                Thread.sleep(5000)
            }
            else -> {
                Thread.sleep(5000)
            }
        }
    }
}
