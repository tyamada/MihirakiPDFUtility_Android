package jp.mihiraki.pdfutility.domain

import jp.mihiraki.pdfutility.ui.PageState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage

class PdfProcessorTest {

    @Test
    fun testPageOperations() {
        val processor = PdfProcessor()
        
        // Create a dummy PDF in memory
        val dummyDoc = PDDocument()
        dummyDoc.addPage(PDPage())
        dummyDoc.addPage(PDPage())
        val out = ByteArrayOutputStream()
        dummyDoc.save(out)
        dummyDoc.close()
        
        val input = ByteArrayInputStream(out.toByteArray())
        val count = processor.load(input)
        assertEquals(2, count)
        
        // Test save with reorder/deletion logic (mocked PageState)
        val pages = listOf(
            PageState(originalIndex = 1, thumbnail = null),
            PageState(originalIndex = 0, thumbnail = null),
            PageState(originalIndex = -1, isBlank = true, thumbnail = null)
        )
        
        val saveOut = ByteArrayOutputStream()
        processor.save(saveOut, pages)
        
        val resultDoc = PDDocument.load(ByteArrayInputStream(saveOut.toByteArray()))
        assertEquals(3, resultDoc.numberOfPages)
        resultDoc.close()
    }
}
