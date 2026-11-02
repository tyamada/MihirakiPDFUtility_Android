package com.takumayamada22.pdfutility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FolderImagesPdfTest {
    @Test fun testCreatePdfFromFolderImages() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dir = File(context.cacheDir, "valid_image_fixture").apply { mkdirs() }
        val images = listOf(Color.RED, Color.GREEN, Color.BLUE).mapIndexed { i, color ->
            val file = File(dir, "page_$i.jpg")
            val bitmap = Bitmap.createBitmap(80 + i * 10, 120, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(color)
            file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)) }
            bitmap.recycle()
            file
        }
        val processor = PdfProcessor()
        try {
            assertEquals(3, processor.createPdfFromImages(images.map { Uri.fromFile(it) }, context))
            val pdf = File(dir, "images.pdf")
            pdf.outputStream().use { processor.save(it) }
            PDDocument.load(pdf).use { reopened ->
                assertEquals(3, reopened.numberOfPages)
                repeat(3) { assertTrue(reopened.getPage(it).resources.xObjectNames.iterator().hasNext()) }
            }
        } finally { processor.close() }
    }
}
