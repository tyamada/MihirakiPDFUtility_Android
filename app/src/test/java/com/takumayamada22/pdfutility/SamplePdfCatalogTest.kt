package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class SamplePdfCatalogTest {
    @Test fun catalogHasEpisodesForEveryLanguageAndMatchingLocalCopies() {
        val root = listOf(File("."), File("..")).first { File(it, "docs").isDirectory }
        val items = SamplePdfCatalog.items
        assertEquals(30, items.size)
        assertEquals(items.size, items.map { it.id }.distinct().size)
        assertEquals(items.size, items.map { it.fileName }.distinct().size)
        assertEquals(setOf("ja", "en", "ko", "zh_cn", "zh_tw", "de", "fr", "es", "pt", "vi", "fil", "ne", "id", "it"), items.map { it.languageCode }.toSet())
        items.groupBy { it.languageCode }.forEach { (language, episodes) ->
            val expectedSize = if (language == "en" || language == "ja") 3 else 2
            assertEquals(language, expectedSize, episodes.size)
        }
        items.forEach { item ->
            assertEquals("https://tyamada.github.io/MihirakiPDFViewer_Android/sample/${item.fileName}", item.url)
            val sourceFile = File(root, "testdata/${item.fileName}")
            val published = File(root, "docs/sample/${item.fileName}")
            val source = if (sourceFile.isFile) sourceFile else published
            assertTrue("Missing source: $source", source.isFile && source.length() > 0)
            assertTrue("Missing distribution copy: $published", published.isFile && published.length() > 0)
            val sha = MessageDigest.getInstance("SHA-256")
            assertArrayEquals(item.fileName, sha.digest(source.readBytes()), sha.digest(published.readBytes()))
        }
    }
}
