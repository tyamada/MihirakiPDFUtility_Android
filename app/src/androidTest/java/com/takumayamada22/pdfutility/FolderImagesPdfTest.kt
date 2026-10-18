package com.takumayamada22.pdfutility

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.domain.PdfProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FolderImagesPdfTest {

    @Test
    fun testCreatePdfFromFolderImages() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val rootDir = File(".")
        val sampleImagesDir = File(rootDir, "testdata/sample_images")
        
        val testImagesDir = File(context.cacheDir, "sample_images")
        testImagesDir.mkdirs()
        
        val sourceDir = if (sampleImagesDir.isDirectory) sampleImagesDir else File(context.filesDir, "sample_images")
        val imageFiles = sourceDir.listFiles { file -> file.extension.lowercase() in listOf("jpg", "jpeg", "png") }?.sortedBy { it.name }
            ?: emptyList()
            
        if (imageFiles.isEmpty()) {
            repeat(8) { i ->
                val numStr = String.format("%02d", i + 1)
                val f = File(testImagesDir, "page_$numStr.jpg")
                f.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
            }
        }
        
        val targetImages = (sourceDir.listFiles()?.filter { it.extension.lowercase() in listOf("jpg", "jpeg", "png") } ?: emptyList())
            .ifEmpty { testImagesDir.listFiles()?.filter { it.extension.lowercase() in listOf("jpg", "jpeg", "png") } ?: emptyList() }
            
        assertTrue("Should have test images", targetImages.isNotEmpty())
        
        val uris = targetImages.sortedBy { it.name }.map { Uri.fromFile(it) }
        
        val processor = PdfProcessor()
        val pageCount = processor.createPdfFromImages(uris, context)
        assertEquals(uris.size, pageCount)
        processor.close()
    }
}
