package com.takumayamada22.pdfutility

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
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
            assertTrue(items.size >= 52)
            assertTrue(items.map { it.id }.toSet().containsAll(SamplePdfCatalog.items.map { it.id }))
            val alias = items.single { it.id == "ep1_fil" }
            assertTrue(alias.url.endsWith("_tl.pdf"))
            assertTrue(alias.fileName.endsWith("_fil.pdf"))
            val item = items.single { it.id == "ep4_ja" }
            assertEquals("https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261127-ep4-ja-v1/tameshibu_episode4_ja.pdf", item.url)
            assertEquals(99_981_486L, item.bytes)
            assertEquals("7c4bde39dc25800aa097854c6e47f31934abe12b3a05d97450fb73523af3c3dc", item.sha256)
            val cache = SamplePdfCache(File(root, "pdfs"))
            val states = SamplePdfDownloadManager(cache).download(item).toList()
            assertEquals(DownloadState.Success, states.last())
            val downloaded = requireNotNull(cache.downloadedFile(item))
            assertEquals(item.bytes, downloaded.length())
            ParcelFileDescriptor.open(downloaded, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                PdfRenderer(fd).use { renderer ->
                    assertEquals(8, renderer.pageCount)
                    renderer.openPage(0).use { page -> assertTrue(page.width > 0 && page.height > 0) }
                }
            }
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
