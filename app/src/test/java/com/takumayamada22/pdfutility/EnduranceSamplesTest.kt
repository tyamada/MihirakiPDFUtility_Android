package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.data.SamplePdfCache
import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.util.requireEnduranceSamples
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest

class EnduranceSamplesTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun verifiedFilipinoAliasIsReadFromTheDownloadCacheWithoutCopyingToTestdata() {
        val bytes = "%PDF-1.5\nsmall sample fixture\n%%EOF".toByteArray()
        val item = SamplePdfCatalog.items.single { it.id == "ep1_fil" }.copy(
            bytes = bytes.size.toLong(),
            sha256 = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 0xff) }
        )
        val root = temporary.newFolder()
        val cache = SamplePdfCache(root)
        val downloaded = cache.temporaryFile(item).apply { writeBytes(bytes) }
        cache.publish(item, downloaded)
        val verified = requireNotNull(cache.downloadedFile(item))
        assertTrue(verified.path.contains("verified/${item.sha256}/"))
        assertTrue(verified.name.endsWith("_fil.pdf"))
        assertEquals(listOf(verified), requireEnduranceSamples(listOf(item) to mapOf(item.id to verified)))
        assertFalse(File(root, "testdata").exists())
        // Corruption must fail preparation, rather than select an arbitrary tameshibu file.
        verified.writeBytes(bytes.copyOf().apply { this[10] = 0 })
        assertNull(cache.downloadedFile(item))
        try {
            requireEnduranceSamples(listOf(item) to mapOf(item.id to cache.downloadedFile(item)))
            fail("Missing verified sample must fail explicitly")
        } catch (expected: FileNotFoundException) {
            assertTrue(expected.message!!.contains("Download sample PDFs"))
        }
    }

    @Test fun sampleIdsControlSelectionAndUnknownOrMissingFilesDoNotPass() {
        val item = SamplePdfCatalog.items.first()
        val unknown = temporary.newFile("tameshibu_unrelated.pdf")
        try {
            requireEnduranceSamples(listOf(item) to mapOf("unknown" to unknown))
            fail("A filename alone must not establish a verified sample")
        } catch (_: FileNotFoundException) { }
        assertTrue(unknown.exists())
    }
}
