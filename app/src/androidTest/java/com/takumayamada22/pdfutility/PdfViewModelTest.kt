package com.takumayamada22.pdfutility

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.PageState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class PdfViewModelTest {

    @Test
    fun testMoveSelectedPagesBatch() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = PdfViewModel(app)

        val initialPages = listOf(
            PageState(originalIndex = 0, id = "p0", thumbnail = null),
            PageState(originalIndex = 1, id = "p1", thumbnail = null),
            PageState(originalIndex = 2, id = "p2", thumbnail = null),
            PageState(originalIndex = 3, id = "p3", thumbnail = null),
            PageState(originalIndex = 4, id = "p4", thumbnail = null)
        )
        // Select pages at index 1 and 3 (p1 and p3)
        viewModel.setPagesForTest(initialPages, setOf(1, 3))

        // Move selected pages (dragging from index 1) to target index 4 (p4)
        viewModel.moveSelectedPages(1, 4)

        val resultingPages = viewModel.uiState.value.pages
        val resultingIds = resultingPages.map { it.id }

        // Verify that all 5 pages are present
        assertEquals(5, resultingPages.size)

        // p1 and p3 should move together as a block and remain adjacent
        val p1Index = resultingIds.indexOf("p1")
        val p3Index = resultingIds.indexOf("p3")
        assertTrue("p1 and p3 should be adjacent after batch move", p1Index != -1 && p3Index != -1 && abs(p1Index - p3Index) == 1)
        
        // Verify that selected indices are updated correctly in uiState
        val selectedIndices = viewModel.uiState.value.selectedIndices
        assertEquals(2, selectedIndices.size)
        assertTrue(selectedIndices.contains(p1Index))
        assertTrue(selectedIndices.contains(p3Index))
    }
}
