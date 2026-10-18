package com.takumayamada22.pdfutility

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.ui.*
import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest

/** Test APK only: operates the real Activity and PDF engine; no fabricated UI state. */
@RunWith(AndroidJUnit4::class)
class StoreScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrument get() = InstrumentationRegistry.getInstrumentation()
    private val args get() = InstrumentationRegistry.getArguments()
    private lateinit var vm: PdfViewModel
    private lateinit var folder: File
    private fun state() = vm.uiState.value
    private fun main(action: () -> Unit) = compose.runOnUiThread(action)
    private fun ready(count: Int? = null) {
        compose.waitUntil(90000) { !state().isLoading && state().pages.isNotEmpty() && (count == null || state().pages.size == count) }
        assertNull("PDF processing error", state().errorResId)
        assertNull(state().errorMessage)
        assertTrue(state().pages.all { it.thumbnail != null })
        compose.waitForIdle()
    }
    private fun load(file: File, count: Int) {
        main { vm.clearSelection(); vm.loadPdf(Uri.fromFile(file)) }
        compose.waitUntil(90000) { !state().isLoading && state().fileName == file.name && state().pages.size == count }
        ready(count)
    }
    private fun click(id: Int) {
        val label = compose.activity.getString(id)
        compose.onNodeWithContentDescription(label, useUnmergedTree = true).performClick()
        compose.waitForIdle()
    }
    private fun saveAndVerify(name: String, count: Int, password: String? = null) {
        val output = File(folder, "$name.pdf")
        main { vm.savePdf(Uri.fromFile(output), password) }
        compose.waitUntil(90000) { output.exists() && output.length() > 100 && !state().isLoading }
        assertNull(state().errorResId)
        PDDocument.load(output, password ?: "").use { assertEquals(count, it.numberOfPages) }
    }

    @Test fun captureStoreScreenshots() {
        val lang = args.getString("captureLanguage")!!
        val scene = args.getString("captureScene")!!
        val token = args.getString("captureId")!!
        val app = instrument.targetContext.applicationContext as Application
        folder = File(app.filesDir, "store_capture").apply { mkdirs() }
        compose.activityRule.scenario.onActivity {
            vm = ViewModelProvider(it, PdfViewModel.provideFactory(app))[PdfViewModel::class.java]
        }
        ready()
        val locale = compose.activity.resources.configuration.locales[0].toLanguageTag()
        assertEquals(args.getString("captureLocale"), locale)
        val version = app.packageManager.getPackageInfo(app.packageName, 0)
        assertEquals(args.getString("captureVersionCode")!!.toLong(), version.longVersionCode)
        compose.waitUntil(15000) { (compose.activity.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) == (args.getString("captureOrientation") == "portrait") }
        val portrait = compose.activity.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
        assertEquals(args.getString("captureOrientation") == "portrait", portrait)
        val source = File(folder, "$lang.pdf")
        val digest = MessageDigest.getInstance("SHA-256").digest(source.readBytes()).joinToString("") { "%02x".format(it) }
        assertEquals(args.getString("capturePdfSha256"), digest)
        load(source, 8)
        // Choose supported user preferences. The phone toolbar uses the app's icon option
        // so the actual page content remains legible on the small display.
        main { vm.setButtonDisplayType(if (portrait) ButtonDisplayType.ICON_ONLY else ButtonDisplayType.ICON_AND_TEXT) }
        main { vm.setThumbnailSize(ThumbnailSize.M) }
        compose.waitUntil(90000) { state().pages.all { it.thumbnail != null && it.thumbnail!!.width >= 190 } }
        compose.waitForIdle()
        val checks = JSONObject()
        when (scene) {
            "01_page_grid" -> Unit
            "02_spread" -> {
                click(R.string.action_right_binding_cover)
                assertTrue(state().isMihirakiView)
                assertTrue(state().isRtl)
                assertTrue(state().showCover)
                // Scroll past the standalone cover to the first pair of story pages.
                compose.onAllNodes(hasScrollAction()).onFirst().performScrollToIndex(1)
            }
            "03_page_edit" -> {
                val before = state().pages.map { it.id }
                main { vm.movePage(1, 3); vm.movePage(1, 3); vm.toggleSelection(2); vm.toggleSelection(3) }
                assertEquals(setOf(2,3), state().selectedIndices)
                assertEquals(before[1], state().pages[2].id)
                assertEquals(before[2], state().pages[3].id)
                saveAndVerify("${token}_reordered", 8)
                checks.put("reorderedAndSaved", true)
            }
            "04_split" -> {
                load(File(folder, "${lang}_spread.pdf"), 1)
                main { vm.toggleSelection(0) }
                click(R.string.action_split_pages)
                compose.onNodeWithText(compose.activity.getString(R.string.split_vertical)).assertIsDisplayed()
                compose.onNodeWithText(compose.activity.getString(R.string.split_horizontal)).assertIsDisplayed()
                // First verify the operation and saved PDF, then restore the input/dialog for capture.
                compose.onNodeWithText(compose.activity.getString(R.string.action_run)).performClick()
                ready(2)
                assertTrue(state().pages.all { it.isSplit })
                saveAndVerify("${token}_split", 2)
                checks.put("splitAndSaved", true)
                load(File(folder, "${lang}_spread.pdf"), 1)
                main { vm.toggleSelection(0) }
                click(R.string.action_split_pages)
            }
            "05_insert_pdf" -> {
                val append = File(folder, "${lang}_append.pdf")
                val extra = PDDocument.load(append).use { it.numberOfPages }
                main { vm.appendPdf(Uri.fromFile(append)) }
                ready(8 + extra)
                assertEquals(append.toURI().path, state().pages.last().sourceUri?.path)
                saveAndVerify("${token}_combined", 8 + extra)
                // Show the boundary where episode 2 follows episode 1.
                compose.onAllNodes(hasScrollAction()).onFirst().performScrollToIndex(6)
                checks.put("appendedAndSaved", true).put("appendedPageCount", extra)
            }
            "06_properties" -> {
                click(R.string.action_property)
                assertEquals(SettingsDialogType.PROPERTY, state().activeSettingsDialog)
                compose.onNodeWithText(compose.activity.getString(R.string.prop_title)).assertExists()
            }
            "07_security" -> {
                // Verify an encrypted save can be reopened with the demonstration password.
                saveAndVerify("${token}_encrypted", 8, "StoreDemo2026")
                PDDocument.load(File(folder, "${token}_encrypted.pdf"), "StoreDemo2026").use { assertTrue(it.isEncrypted) }
                click(R.string.action_settings)
                compose.onNodeWithText(compose.activity.getString(R.string.settings_password)).performClick()
                assertEquals(SettingsDialogType.PASSWORD, state().activeSettingsDialog)
                checks.put("encryptedSaveVerified", true)
            }
            else -> error("Unsupported scene: $scene")
        }
        ready()
        // Allow the physical display to settle after scrolling and dialog animations.
        Thread.sleep(1200)
        instrument.waitForIdleSync()
        val bitmap = instrument.uiAutomation.takeScreenshot() ?: error("No screenshot")
        File(folder, "$token.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val config = compose.activity.resources.configuration
        val info = JSONObject().put("captureId", token).put("scene", scene).put("language", lang)
            .put("locale", locale).put("package", app.packageName).put("versionCode", version.longVersionCode)
            .put("versionName", version.versionName).put("pdfSha256", digest).put("portrait", portrait)
            .put("pageCount", state().pages.size).put("selectedIndices", JSONArray(state().selectedIndices.toList()))
            .put("pageOrder", JSONArray(state().pages.map { it.originalIndex + 1 }))
            .put("api", Build.VERSION.SDK_INT).put("densityDpi", config.densityDpi)
            .put("screenWidthDp", config.screenWidthDp).put("screenHeightDp", config.screenHeightDp)
            .put("checks", checks).put("setup", "Real MainActivity; PDF loaded through production ViewModel API; toolbar/dialog actions through Compose input")
        File(folder, "$token.json").writeText(info.toString(2))
        bitmap.recycle()
    }
}
