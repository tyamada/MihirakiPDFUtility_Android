package jp.mihiraki.pdfutility

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tom_roush.pdfbox.cos.COSDictionary
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PageLayout
import com.tom_roush.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences
import jp.mihiraki.pdfutility.domain.PdfProcessor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class PdfSettingsTest {

    private fun createTestPdf(file: File, layout: PageLayout, direction: PDViewerPreferences.READING_DIRECTION) {
        val doc = PDDocument()
        doc.addPage(PDPage())
        doc.addPage(PDPage())
        doc.documentCatalog.pageLayout = layout
        
        val vp = PDViewerPreferences(COSDictionary())
        vp.setReadingDirection(direction)
        doc.documentCatalog.viewerPreferences = vp
        
        val out = FileOutputStream(file)
        doc.save(out)
        doc.close()
        out.close()
    }

    @Test
    fun testAllMetadataCases() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val processor = PdfProcessor()
        
        val testCases = listOf(
            // L2R Cases
            TestSpec("L2R_Cover.pdf", PageLayout.TWO_PAGE_RIGHT, PDViewerPreferences.READING_DIRECTION.L2R),
            TestSpec("L2R_NoCover.pdf", PageLayout.TWO_PAGE_LEFT, PDViewerPreferences.READING_DIRECTION.L2R),
            TestSpec("L2R_SinglePage.pdf", PageLayout.SINGLE_PAGE, PDViewerPreferences.READING_DIRECTION.L2R),
            TestSpec("L2R_OneColumn.pdf", PageLayout.ONE_COLUMN, PDViewerPreferences.READING_DIRECTION.L2R),
            TestSpec("L2R_TwoColumnLeft.pdf", PageLayout.TWO_COLUMN_LEFT, PDViewerPreferences.READING_DIRECTION.L2R),
            TestSpec("L2R_TwoColumnRight.pdf", PageLayout.TWO_COLUMN_RIGHT, PDViewerPreferences.READING_DIRECTION.L2R),
            TestSpec("L2R_TwoPageLeft.pdf", PageLayout.TWO_PAGE_LEFT, PDViewerPreferences.READING_DIRECTION.L2R),
            TestSpec("L2R_TwoPageRight.pdf", PageLayout.TWO_PAGE_RIGHT, PDViewerPreferences.READING_DIRECTION.L2R),
            
            // R2L Cases
            TestSpec("R2L_Cover.pdf", PageLayout.TWO_PAGE_RIGHT, PDViewerPreferences.READING_DIRECTION.R2L),
            TestSpec("R2L_NoCover.pdf", PageLayout.TWO_PAGE_LEFT, PDViewerPreferences.READING_DIRECTION.R2L),
            TestSpec("R2L_SinglePage.pdf", PageLayout.SINGLE_PAGE, PDViewerPreferences.READING_DIRECTION.R2L),
            TestSpec("R2L_OneColumn.pdf", PageLayout.ONE_COLUMN, PDViewerPreferences.READING_DIRECTION.R2L),
            TestSpec("R2L_TwoColumnLeft.pdf", PageLayout.TWO_COLUMN_LEFT, PDViewerPreferences.READING_DIRECTION.R2L),
            TestSpec("R2L_TwoColumnRight.pdf", PageLayout.TWO_COLUMN_RIGHT, PDViewerPreferences.READING_DIRECTION.R2L),
            TestSpec("R2L_TwoPageLeft.pdf", PageLayout.TWO_PAGE_LEFT, PDViewerPreferences.READING_DIRECTION.R2L),
            TestSpec("R2L_TwoPageRight.pdf", PageLayout.TWO_PAGE_RIGHT, PDViewerPreferences.READING_DIRECTION.R2L)
        )
        
        testCases.forEach { spec ->
            val file = File(context.cacheDir, spec.name)
            createTestPdf(file, spec.layout, spec.direction)
            
            processor.load(file.inputStream())
            val meta = processor.getMetadata()
            
            assertEquals("Layout mismatch for ${spec.name}", spec.layout.stringValue(), meta["layout"])
            assertEquals("Direction mismatch for ${spec.name}", spec.direction.name, meta["direction"])
            
            processor.close()
        }
    }

    private data class TestSpec(
        val name: String,
        val layout: PageLayout,
        val direction: PDViewerPreferences.READING_DIRECTION
    )
}
