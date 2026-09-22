package com.takumayamada22.pdfutility

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tom_roush.pdfbox.cos.COSDictionary
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PageLayout
import com.tom_roush.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.ui.PageState
import org.junit.Assert
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

    @Test
    fun testVersionUpgradeForTwoPageLayouts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val processor = PdfProcessor()
        val saveFile = File(context.cacheDir, "version_test.pdf")

        // Case: TwoPageRight should force version to 1.5+
        val pages = listOf(PageState(originalIndex = 0, thumbnail = null))
        val metadata = mapOf("layout" to "TwoPageRight")
        
        // We need a document loaded to call save
        val initialFile = File(context.cacheDir, "initial.pdf")
        val doc = PDDocument()
        doc.addPage(PDPage())
        doc.version = 1.4f
        doc.save(initialFile)
        doc.close()
        
        processor.load(initialFile.inputStream())
        
        val out = FileOutputStream(saveFile)
        processor.save(out, pages, metadata = metadata)
        out.close()
        
        // Verify version in saved file
        val verifier = PdfProcessor()
        verifier.load(saveFile.inputStream())
        val meta = verifier.getMetadata()
        val version = meta["version"]?.toFloat() ?: 0f
        
        Assert.assertTrue("Version should be 1.5 or higher for TwoPageRight", version >= 1.5f)
        verifier.close()
    }

    private data class TestSpec(
        val name: String,
        val layout: PageLayout,
        val direction: PDViewerPreferences.READING_DIRECTION
    )
}
