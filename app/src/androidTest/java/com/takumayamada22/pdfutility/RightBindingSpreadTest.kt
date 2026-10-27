package com.takumayamada22.pdfutility

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.PdfViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RightBindingSpreadTest {

    @Test
    fun testRightBindingPdfLoadingAndSpreadView() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = PdfViewModel(app)

        val rootDir = File(".")
        val testdataDir = if (File(rootDir, "testdata").isDirectory) {
            File(rootDir, "testdata")
        } else {
            File(app.filesDir, "../testdata")
        }

        val r2lLeftPdf = File(testdataDir, "R2L_TwoPageLeft.pdf")
        val r2lRightPdf = File(testdataDir, "R2L_TwoPageRight.pdf")

        if (r2lLeftPdf.exists()) {
            runBlocking {
                viewModel.loadPdf(Uri.fromFile(r2lLeftPdf))
            }
            val state = viewModel.uiState.value
            assertEquals("TwoPageLeft", state.pageLayout)
            assertEquals("R2L", state.scrollDirection)
            assertTrue("isRtl should be true for R2L PDF", state.isRtl)
            assertEquals(false, state.showCover)

            // Toggle Mihiraki view (display toggle)
            viewModel.toggleMihirakiView()
            assertTrue("isMihirakiView should be true", viewModel.uiState.value.isMihirakiView)
            assertTrue("isRtl should still be true after toggling view", viewModel.uiState.value.isRtl)
        }

        if (r2lRightPdf.exists()) {
            runBlocking {
                viewModel.loadPdf(Uri.fromFile(r2lRightPdf))
            }
            val state = viewModel.uiState.value
            assertEquals("TwoPageRight", state.pageLayout)
            assertEquals("R2L", state.scrollDirection)
            assertTrue("isRtl should be true for R2L PDF with cover", state.isRtl)
            assertEquals(true, state.showCover)

            // Toggle Mihiraki view
            viewModel.toggleMihirakiView()
            assertTrue("isMihirakiView should be true", viewModel.uiState.value.isMihirakiView)
            assertTrue("isRtl should still be true after toggling view", viewModel.uiState.value.isRtl)
        }
    }
}
