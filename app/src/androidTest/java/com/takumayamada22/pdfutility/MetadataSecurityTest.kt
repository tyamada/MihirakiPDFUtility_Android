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
class MetadataSecurityTest {
    @Test
    fun testMetadataAndEncryption() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val processor = PdfProcessor()
        
        // Create initial PDF
        val tempFile = File(context.cacheDir, "meta_test.pdf")
        val doc = PDDocument()
        doc.addPage(PDPage())
        doc.save(tempFile)
        doc.close()
        
        processor.load(tempFile.inputStream())
        
        // Update metadata and set password
        val metadata = mapOf(
            "title" to "Test Title",
            "author" to "Test Author",
            "subject" to "Test Subject",
            "keywords" to "test, pdf"
        )
        val password = "secret_password"
        
        val saveFile = File(context.cacheDir, "saved_secure.pdf")
        val out = FileOutputStream(saveFile)
        val pages = listOf(PageState(originalIndex = 0, thumbnail = null))
        
        processor.save(out, pages, password = password, metadata = metadata)
        out.close()
        
        // Verify metadata and encryption
        val verifier = PdfProcessor()
        
        try {
            verifier.load(saveFile.inputStream())
            org.junit.Assert.fail("Encrypted input must request a password")
        } catch (_: com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException) {
            // Authentication errors must be propagated, not converted to an empty PDF.
        }

        // Load with correct password
        val countWithPass = verifier.load(saveFile.inputStream(), password = password)
        assertEquals(1, countWithPass)
        
        val savedMeta = verifier.getMetadata()
        assertEquals("Test Title", savedMeta["title"])
        assertEquals("Test Author", savedMeta["author"])
        
        verifier.close()
    }
}
