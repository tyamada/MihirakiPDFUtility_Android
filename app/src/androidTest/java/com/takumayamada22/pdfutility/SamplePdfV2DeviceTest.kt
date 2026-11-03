package com.takumayamada22.pdfutility

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.domain.ThumbnailProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale

/** Optional public fixtures are supplied to the emulator, never committed as PDFs. */
@RunWith(AndroidJUnit4::class)
class SamplePdfV2DeviceTest {
    @Test fun all51PdfsRenderInPageOrderAndExposeSearchableTextOnAndroid() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val fixturePath = InstrumentationRegistry.getArguments().getString("samplePdfFixtureDir")
        assumeTrue("Supply verified public v2 PDFs with samplePdfFixtureDir", fixturePath != null)
        val fixtures = File(requireNotNull(fixturePath))
        val output = File(context.getExternalFilesDir(null), "sample-v2-qa").apply { mkdirs() }
        PDFBoxResourceLoader.init(context)
        val queries = mapOf("ja" to "ためし部", "ko" to "해봄부", "zh_cn" to "试试社", "zh_tw" to "試試社", "zh_hk" to "試試社", "de" to "PROBIERCLUB", "en" to "CLUB", "es" to "CLUB", "es_419" to "CLUB", "fr" to "CLUB", "id" to "KLUB", "it" to "CLUB", "ne" to "प्रयोग", "pt" to "CLUBE", "pt_pt" to "CLUBE", "fil" to "CLUB", "vi" to "CÂU")
        val results = JSONArray()
        val issues = mutableListOf<String>()
        for (item in SamplePdfCatalog.items) {
            val row = JSONObject().put("id", item.id).put("file", item.fileName)
            val provider = ThumbnailProvider(context)
            try {
                val file = File(fixtures, item.fileName)
                assertTrue("Missing public fixture ${item.fileName}", file.isFile)
                val uri = Uri.fromFile(file)
                assertEquals(item.pageCount, provider.getPageCount(uri))
                val query = requireNotNull(queries[item.languageCode])
                row.put("query", query).put("query_page", 1)
                val textCounts = JSONArray()
                PDDocument.load(file.inputStream(), MemoryUsageSetting.setupTempFileOnly()).use { document ->
                    assertEquals(item.pageCount, document.numberOfPages)
                    for (page in 0 until item.pageCount) {
                        val bitmap = requireNotNull(provider.getThumbnail(uri, page, 360))
                        File(output, "${item.id}-page-${page + 1}.png").outputStream().use {
                            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                        }
                        val text = PDFTextStripper().apply { startPage = page + 1; endPage = page + 1 }.getText(document)
                        assertTrue("No extractable text: ${item.fileName} page ${page + 1}", text.isNotBlank())
                        textCounts.put(text.trim().length)
                        if (page == 0) {
                            row.put("cover_text", text).put("query_found", text.uppercase(Locale.ROOT).contains(query.uppercase(Locale.ROOT)))
                            assertTrue("Search query '$query' missing: ${item.fileName} page 1", text.uppercase(Locale.ROOT).contains(query.uppercase(Locale.ROOT)))
                        }
                    }
                }
                row.put("text_chars_per_page", textCounts).put("passed", true)
            } catch (error: Throwable) {
                row.put("passed", false).put("error", error.toString())
                issues += "${item.fileName}: ${error.message}"
            } finally { provider.close() }
            results.put(row)
        }
        File(output, "android-pdf-qa.json").writeText(results.toString(2))
        assertTrue(issues.joinToString("\n"), issues.isEmpty())
    }
}
