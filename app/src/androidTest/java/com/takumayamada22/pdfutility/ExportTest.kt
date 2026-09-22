package com.takumayamada22.pdfutility

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.ui.PageState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ExportTest {
    @Test
    fun testExportLogic() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val processor = PdfProcessor()
        
        // Create a dummy PDF with 3 pages for testing
        val tempFile = File(context.cacheDir, "test.pdf")
        val out = FileOutputStream(tempFile)
        val doc = PDDocument()
        doc.addPage(PDPage())
        doc.addPage(PDPage())
        doc.addPage(PDPage())
        doc.save(out)
        doc.close()
        out.close()
        
        // Load it into processor
        processor.load(tempFile.inputStream())
        assertEquals(3, processor.getPageCount())
        
        // Export only pages 0 and 2
        val exportFile = File(context.cacheDir, "export.pdf")
        val exportOut = FileOutputStream(exportFile)
        val selectedPages = listOf(
            PageState(originalIndex = 0, thumbnail = null),
            PageState(originalIndex = 2, thumbnail = null)
        )
        
        processor.save(exportOut, selectedPages)
        exportOut.close()
        
        // Verify exported file
        val verifier = PdfProcessor()
        verifier.load(exportFile.inputStream())
        assertEquals(2, verifier.getPageCount())
        verifier.close()
    }
}
