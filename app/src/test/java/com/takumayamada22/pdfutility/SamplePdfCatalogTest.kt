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
        assertEquals(42, items.size)
        assertEquals(items.size, items.map { it.id }.distinct().size)
        assertEquals(items.size, items.map { it.fileName }.distinct().size)
        assertEquals(setOf("ja", "en", "ko", "zh_cn", "zh_tw", "de", "fr", "es", "pt", "vi", "fil", "ne", "id", "it"), items.map { it.languageCode }.toSet())
        items.groupBy { it.languageCode }.forEach { (language, episodes) ->
            val expectedSize = 3
            assertEquals(language, expectedSize, episodes.size)
        }
        // Committed checksums remain independent of ignored local testdata in CI.
        val checksums = File(root, "docs/sample/SHA256SUMS").readLines().associate {
            val parts = it.split("  ", limit = 2)
            parts[1] to parts[0]
        }
        assertEquals(items.map { it.fileName }.toSet(), checksums.keys)
        items.forEach { item ->
            assertEquals("https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v1/${item.fileName}", item.url)
            val published = File(root, "docs/sample/${item.fileName}")
            assertTrue("Missing distribution copy: $published", published.isFile && published.length() > 0)
            val actual = MessageDigest.getInstance("SHA-256").digest(published.readBytes())
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
            assertEquals(item.fileName, checksums[item.fileName], actual)
        }
    }
}
