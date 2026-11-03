package com.takumayamada22.pdfutility

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.tom_roush.pdfbox.pdmodel.*
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RotationHistoryTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var vm: PdfViewModel
    @Before fun setup() {
        compose.runOnUiThread {
            vm = ViewModelProvider(compose.activity, PdfViewModel.provideFactory(compose.activity.application))[PdfViewModel::class.java]
            vm.dismissTutorial()
        }
        val fixture = File(compose.activity.cacheDir, "rotation-history-asymmetric.pdf")
        PDDocument().use { doc ->
            repeat(3) { index ->
                val page = PDPage(PDRectangle(200f, 300f)); doc.addPage(page)
                PDPageContentStream(doc, page).use { stream ->
                    stream.setNonStrokingColor(1f, 0f, 0f)
                    stream.addRect(10f + index * 5, 15f, 35f, 70f); stream.fill()
                    stream.setNonStrokingColor(0f, 0f, 1f)
                    stream.addRect(100f, 180f, 60f, 30f); stream.fill()
                }
            }
            doc.save(fixture)
        }
        compose.runOnUiThread { vm.loadPdf(Uri.fromFile(fixture)) }
        compose.waitUntil(15000) { vm.uiState.value.pages.size == 3 && !vm.uiState.value.isLoading }
    }
    private fun action(id: Int) {
        compose.onNodeWithContentDescription(compose.activity.getString(id), useUnmergedTree = true).performClick()
        compose.waitForIdle()
    }
    private fun thumbnails(): List<Bitmap> = vm.uiState.value.pages.map { requireNotNull(it.thumbnail) }
    private fun checkOriginal(expected: List<Bitmap>) {
        assertEquals(listOf(0, 0, 0), vm.uiState.value.pages.map { it.rotation })
        thumbnails().zip(expected).forEach { (actual, old) -> assertTrue("Undo must restore rendered pixels, not only angle metadata", actual.sameAs(old)) }
        assertEquals(setOf(0, 1), vm.uiState.value.selectedIndices)
    }
    private fun exercise(degrees: Int) {
        val original = thumbnails()
        val highRes = requireNotNull(vm.getHighResThumbnail(0))
        compose.runOnUiThread { vm.toggleSelection(0); vm.toggleSelection(1) }
        action(if (degrees < 0) R.string.action_rotate_left else R.string.action_rotate_right)
        val rotated = thumbnails()
        assertEquals(listOf(if (degrees < 0) 270 else 90, if (degrees < 0) 270 else 90, 0), vm.uiState.value.pages.map { it.rotation })
        assertEquals(original[0].height, rotated[0].width)
        assertTrue(original[2].sameAs(rotated[2]))
        assertEquals(highRes.height, requireNotNull(vm.getHighResThumbnail(0)).width)
        action(R.string.action_undo)
        checkOriginal(original)
        assertTrue(requireNotNull(vm.getHighResThumbnail(0)).sameAs(highRes))
        assertFalse(vm.uiState.value.canUndo); assertTrue(vm.uiState.value.canRedo)
        action(R.string.action_redo)
        thumbnails().zip(rotated).forEach { (actual, expected) -> assertTrue(actual.sameAs(expected)) }
        action(R.string.action_undo)
        checkOriginal(original)
    }
    @Test fun leftRotationUndoRedoRestoresPixelsAndSelection() = exercise(-90)
    @Test fun rightRotationUndoRedoRestoresPixelsAndSelection() = exercise(90)
    @Test fun deleteThenUndoRestoresRotatedPagesAndEarlierUndoRestoresOriginal() {
        val original = thumbnails()
        compose.runOnUiThread { vm.toggleSelection(0); vm.toggleSelection(1) }
        action(R.string.action_rotate_right)
        val rotated = thumbnails()
        compose.runOnUiThread { vm.deleteSelected() }
        compose.waitForIdle(); assertEquals(1, vm.uiState.value.pages.size)
        action(R.string.action_undo)
        assertEquals(3, vm.uiState.value.pages.size)
        thumbnails().zip(rotated).forEach { (actual, expected) -> assertTrue(actual.sameAs(expected)) }
        action(R.string.action_undo)
        checkOriginal(original)
    }
    @Test fun accessibilityActivationOpensPreviewAndCloseReturnsToEditor() {
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.page_number_description, 1))
            .performSemanticsAction(SemanticsActions.OnClick) { assertTrue(it()) }
        compose.onNodeWithText(compose.activity.getString(R.string.preview_page_counter, 1, 3)).assertIsDisplayed()
        assertEquals(0, vm.uiState.value.previewPageIndex)
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.action_close)).performClick()
        compose.runOnIdle { assertNull(vm.uiState.value.previewPageIndex) }
    }
}
