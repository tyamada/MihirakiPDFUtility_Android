package com.takumayamada22.pdfutility

import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.data.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.UUID

class SamplePdfManifestNetworkTest {
    @Test fun liveManifestPdfAndOfflineRestartKeepVerifiedFiles() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(context.cacheDir, "manifest-acceptance-${UUID.randomUUID()}")
        assertTrue(root.mkdirs())
        try {
            val live = HttpsSamplePdfManifestFetch().fetch()
            val catalogDir = File(root, "catalog")
            val store = SamplePdfManifestStore(catalogDir) { live }
            val items = store.refresh()
            assertEquals(51, items.size)
            assertEquals(SamplePdfCatalog.items.map { it.id }.toSet(), items.map { it.id }.toSet())
            val item = items.single { it.id == "ep1_fil" }
            assertTrue(item.url.endsWith("_tl.pdf"))
            assertTrue(item.fileName.endsWith("_fil.pdf"))
            val cache = SamplePdfCache(File(root, "pdfs"))
            val states = SamplePdfDownloadManager(cache).download(item).toList()
            assertEquals(DownloadState.Success, states.last())
            val downloaded = requireNotNull(cache.downloadedFile(item))
            assertEquals(item.bytes, downloaded.length())
            // Simulated offline transport after process/store recreation, not device network settings.
            val offlineStore = SamplePdfManifestStore(catalogDir) { throw IOException("offline") }
            assertEquals(items, offlineStore.refresh())
            val offlineDownload = SamplePdfDownloadManager(cache, SamplePdfTransfer { _, _, _ ->
                throw AssertionError("An already verified PDF must not be downloaded again")
            }).download(item).toList()
            assertEquals(DownloadState.Success, offlineDownload.last())
            val original = File(catalogDir, "current.json").readBytes()
            val invalid = JSONObject(live).put("schema_version", 999).toString()
            assertEquals(items, SamplePdfManifestStore(catalogDir) { invalid }.refresh())
            assertArrayEquals(original, File(catalogDir, "current.json").readBytes())
            assertEquals(downloaded, cache.downloadedFile(item))
        } finally { root.deleteRecursively() } // Only this test's UUID-owned private cache.
    }
}
