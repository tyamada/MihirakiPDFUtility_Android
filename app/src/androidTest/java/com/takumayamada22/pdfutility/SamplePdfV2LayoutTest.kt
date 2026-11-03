package com.takumayamada22.pdfutility

import android.net.Uri
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.ui.PdfViewModel
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SamplePdfV2LayoutTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun regionalCoversPageSequenceAndLtrRtlSpreadsAreDisplayed() {
        val path = InstrumentationRegistry.getArguments().getString("samplePdfFixtureDir")
        assumeTrue("Requires public v2 fixtures", path != null)
        val vm = ViewModelProvider(compose.activity, PdfViewModel.provideFactory(compose.activity.application))[PdfViewModel::class.java]
        fun capture(file: File) {
            val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
        val output = File(compose.activity.getExternalFilesDir(null), "sample-v2-ui").apply { mkdirs() }
        compose.runOnUiThread { vm.dismissTutorial() }
        for (code in listOf("es_419", "pt_pt", "zh_hk")) {
            val item = SamplePdfCatalog.items.single { it.id == "ep1_$code" }
            compose.runOnUiThread { vm.loadPdf(Uri.fromFile(File(requireNotNull(path), item.fileName))) }
            compose.waitUntil(30_000) { vm.uiState.value.pages.size == 8 && !vm.uiState.value.isLoading }
            compose.waitForIdle()
            if (vm.uiState.value.showCcByDialog) {
                compose.onNodeWithText(compose.activity.getString(R.string.action_cancel)).performClick()
                compose.waitForIdle()
            }
            assertFalse(vm.uiState.value.showCcByDialog)
            assertFalse(vm.uiState.value.ccByAcknowledged)
            val copiedText = PDDocument.load(File(requireNotNull(path), item.fileName).inputStream(), MemoryUsageSetting.setupTempFileOnly()).use { document ->
                PDFTextStripper().apply { startPage = 1; endPage = 1 }.getText(document)
            }
            compose.runOnUiThread {
                val clipboard = compose.activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Sample v2 QA", copiedText))
                assertEquals(copiedText, clipboard.primaryClip?.getItemAt(0)?.coerceToText(compose.activity).toString())
            }
            assertEquals((0..7).toList(), vm.uiState.value.pages.map { it.originalIndex })
            compose.runOnUiThread {
                vm.setRightBindingWithCover()
                if (code != "zh_hk") vm.toggleRtl()
            }
            compose.waitForIdle()
            val page2 = compose.onNodeWithContentDescription(compose.activity.getString(R.string.page_number_description, 2))
                .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val page3 = compose.onNodeWithContentDescription(compose.activity.getString(R.string.page_number_description, 3))
                .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            assertEquals(code, code == "zh_hk", page2.center.x > page3.center.x)
            capture(File(output, "$code-spread.png"))
            compose.runOnUiThread { vm.openPreview(0) }
            compose.onNodeWithText(compose.activity.getString(R.string.preview_page_counter, 1, 8)).assertIsDisplayed()
            capture(File(output, "$code-cover.png"))
            compose.runOnUiThread { vm.closePreview() }
        }
    }
}
