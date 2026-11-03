package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.security.MessageDigest

class SamplePdfCacheTest {
    @get:Rule val temporary = TemporaryFolder()
    private val good = "%PDF-1.5\nverified new sample content\n%%EOF".toByteArray()
    private val old = "%PDF-1.5\nold downloaded sample copy\n%%EOF".toByteArray()
    private fun item(bytes: ByteArray = good) = SamplePdfCatalog.items.first().copy(
        fileName = "fixture.pdf", bytes = bytes.size.toLong(),
        sha256 = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 0xff) }
    )
    private fun manager(cache: SamplePdfCache, bytes: ByteArray = good) = SamplePdfDownloadManager(cache, SamplePdfTransfer { _, file, progress ->
        file.writeBytes(bytes); progress(1f)
    })
    private fun noParts(root: File) = assertFalse(root.walkTopDown().any { it.extension == "part" })

    @Test fun freshDownloadIsPublishedOnlyAfterVerification() = runBlocking {
        val root = temporary.newFolder(); val cache = SamplePdfCache(root); val entry = item()
        assertNull(cache.downloadedFile(entry))
        assertEquals(DownloadState.Success, manager(cache).download(entry).toList().last())
        val local = requireNotNull(cache.downloadedFile(entry))
        assertTrue(local.path.contains(entry.sha256)); assertEquals(entry.fileName, local.name)
        assertArrayEquals(good, local.readBytes()); noParts(root)
    }

    @Test fun oldSameFilenameIsNeverMistakenForRevisedPdfOrDeleted() = runBlocking {
        val root = temporary.newFolder(); val legacy = File(root, "fixture.pdf").apply { writeBytes(old) }
        val cache = SamplePdfCache(root); assertNull(cache.downloadedFile(item()))
        manager(cache).download(item()).toList()
        assertArrayEquals(old, legacy.readBytes()); assertArrayEquals(good, requireNotNull(cache.downloadedFile(item())).readBytes())
    }

    @Test fun unchangedVerifiedLegacyRemainsAvailableOfflineWithoutNetwork() = runBlocking {
        val root = temporary.newFolder(); val legacy = File(root, "fixture.pdf").apply { writeBytes(good) }
        val cache = SamplePdfCache(root)
        val offline = SamplePdfDownloadManager(cache, SamplePdfTransfer { _, _, _ -> throw IOException("Offline") })
        assertEquals(legacy, cache.downloadedFile(item()))
        assertEquals(DownloadState.Success, offline.download(item()).toList().last())
        assertTrue(legacy.isFile)
    }

    @Test fun offlineFailureCanBeRetriedWithoutLosingOldOrUnrelatedFiles() = runBlocking {
        val root = temporary.newFolder(); val legacy = File(root, "fixture.pdf").apply { writeBytes(old) }
        val user = File(root, "user-document.pdf").apply { writeBytes(good) }; val cache = SamplePdfCache(root)
        val offline = SamplePdfDownloadManager(cache, SamplePdfTransfer { _, file, _ -> file.writeText("partial"); throw IOException("Offline") })
        assertTrue(offline.download(item()).toList().last() is DownloadState.Error)
        assertNull(cache.downloadedFile(item())); noParts(root)
        assertArrayEquals(old, legacy.readBytes()); assertArrayEquals(good, user.readBytes())
        assertEquals(DownloadState.Success, manager(cache).download(item()).toList().last())
    }

    @Test fun sameLengthCorruptionIsRejectedAndRedownloadRepairsManagedCopy() = runBlocking {
        val root = temporary.newFolder(); val cache = SamplePdfCache(root); val entry = item()
        manager(cache).download(entry).toList()
        val local = requireNotNull(cache.downloadedFile(entry)); val timestamp = local.lastModified()
        val corrupt = good.copyOf().apply { this[15] = (this[15].toInt() xor 1).toByte() }
        local.writeBytes(corrupt); local.setLastModified(timestamp)
        assertNull(cache.downloadedFile(entry)) // Size and mtime alone must not certify a cached PDF.
        assertEquals(DownloadState.Success, manager(cache).download(entry).toList().last())
        assertArrayEquals(good, requireNotNull(cache.downloadedFile(entry)).readBytes())
    }

    @Test fun wrongHashOrTruncatedTransferNeverBecomesDownloaded() = runBlocking {
        for (bytes in listOf(good.copyOf().apply { this[15] = 0 }, good.copyOf(12), "<html>error</html>".toByteArray())) {
            val root = temporary.newFolder(); val cache = SamplePdfCache(root)
            val error = manager(cache, bytes).download(item()).toList().last()
            assertTrue(error is DownloadState.Error && error.integrityFailure)
            assertNull(cache.downloadedFile(item())); noParts(root)
        }
    }

    @Test fun explicitDeletePreservesLegacyAndDoesNotAutomaticallyResurrectIt() = runBlocking {
        val root = temporary.newFolder(); val legacy = File(root, "fixture.pdf").apply { writeBytes(good) }
        val cache = SamplePdfCache(root); assertNotNull(cache.downloadedFile(item()))
        assertTrue(cache.delete(item())); assertNull(SamplePdfCache(root).downloadedFile(item()))
        assertArrayEquals(good, legacy.readBytes())
        assertEquals(DownloadState.Success, manager(cache).download(item()).toList().last())
        assertNotNull(cache.downloadedFile(item())); assertArrayEquals(good, legacy.readBytes())
    }

    @Test fun changingExpectedHashRetainsPreviousManagedVersion() = runBlocking {
        val root = temporary.newFolder(); val cache = SamplePdfCache(root)
        manager(cache, old).download(item(old)).toList(); val previous = requireNotNull(cache.downloadedFile(item(old)))
        assertNull(cache.downloadedFile(item()))
        manager(cache).download(item()).toList()
        assertArrayEquals(old, previous.readBytes()); assertNotEquals(previous, cache.downloadedFile(item()))
    }

    @Test fun cancellationCleansOnlyTheCurrentTemporaryFile() = runBlocking {
        val root = temporary.newFolder(); val legacy = File(root, "fixture.pdf").apply { writeBytes(old) }
        val cache = SamplePdfCache(root)
        val cancelled = SamplePdfDownloadManager(cache, SamplePdfTransfer { _, file, _ -> file.writeText("partial"); throw CancellationException("cancel") })
        try { cancelled.download(item()).toList(); fail("Cancellation must propagate") } catch (_: CancellationException) { }
        noParts(root); assertArrayEquals(old, legacy.readBytes()); assertNull(cache.downloadedFile(item()))
    }
}
