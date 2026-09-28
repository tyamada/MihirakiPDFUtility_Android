package com.takumayamada22.pdfutility

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.domain.ThumbnailProvider
import com.takumayamada22.pdfutility.ui.PageState
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class LoadTest {

    private fun createLargeTestPdf(context: Context, pageCount: Int): File {
        val file = File(context.cacheDir, "large_load_test_${pageCount}_pages.pdf")
        val doc = PDDocument()
        repeat(pageCount) {
            doc.addPage(PDPage())
        }
        val out = FileOutputStream(file)
        doc.save(out)
        doc.close()
        out.close()
        return file
    }

    @Test
    fun testLargePdfLoadingPerformance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pageCount = 100
        val testFile = createLargeTestPdf(context, pageCount)

        val processor = PdfProcessor()
        var loadedPages = 0
        val loadTime = measureTimeMillis {
            loadedPages = processor.load(testFile.inputStream())
        }

        Log.d("LoadTest", "Loaded $loadedPages pages in ${loadTime}ms")
        assertEquals(pageCount, loadedPages)
        assertTrue("Load time should be reasonable (< 5000ms for $pageCount pages)", loadTime < 5000)

        // Log memory usage
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        Log.d("LoadTest", "Current memory usage after loading: ${usedMemory}MB")

        processor.close()
        testFile.delete()
    }

    @Test
    fun testThumbnailGenerationPerformance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pageCount = 20
        val testFile = createLargeTestPdf(context, pageCount)
        val uri = Uri.fromFile(testFile)

        val thumbnailProvider = ThumbnailProvider(context)
        val thumbTime = measureTimeMillis {
            for (i in 0 until pageCount) {
                val bitmap = thumbnailProvider.getThumbnail(uri, i, 200)
                assertTrue("Thumbnail should not be null for page $i", bitmap != null)
            }
        }

        Log.d("LoadTest", "Generated $pageCount thumbnails in ${thumbTime}ms")
        assertTrue("Thumbnail generation should be fast (< 3000ms for $pageCount pages)", thumbTime < 3000)

        thumbnailProvider.close()
        testFile.delete()
    }

    @Test
    fun testBulkPageOperationsAndSavingStressTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pageCount = 50
        val testFile = createLargeTestPdf(context, pageCount)

        val processor = PdfProcessor()
        processor.load(testFile.inputStream())

        val saveFile = File(context.cacheDir, "stress_saved.pdf")
        val out = FileOutputStream(saveFile)

        // Create a shuffled / stress page state list (reorder, repeat, blank pages)
        val stressPages = mutableListOf<PageState>()
        repeat(100) { i ->
            val origIndex = i % pageCount
            stressPages.add(PageState(originalIndex = origIndex, thumbnail = null))
        }

        val saveTime = measureTimeMillis {
            processor.save(out, stressPages)
        }
        out.close()

        Log.d("LoadTest", "Stress saved 100 pages from $pageCount source pages in ${saveTime}ms")
        assertTrue("Save time should be reasonable (< 5000ms)", saveTime < 5000)

        // Verify saved result
        val verifier = PdfProcessor()
        val verifiedCount = verifier.load(saveFile.inputStream())
        assertEquals(100, verifiedCount)
        verifier.close()

        processor.close()
        testFile.delete()
        saveFile.delete()
    }
}
