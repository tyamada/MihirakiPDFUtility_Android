package com.takumayamada22.pdfutility

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.data.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SamplePdfV2NetworkTest {
    @Test fun realHttpsDownloadOfflineReuseCorruptionAndRetryPreserveOlderPdf() = runBlocking {
        assumeTrue("Opt in to live HTTPS tests with sampleV2Network=true", InstrumentationRegistry.getArguments().getString("sampleV2Network") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "v2-network-qa-${UUID.randomUUID()}").apply { mkdirs() }
        val item = SamplePdfCatalog.items.single { it.id == "ep3_zh_hk" }
        val oldBytes = "%PDF-1.5\nolder user copy".toByteArray()
        val legacy = File(directory, item.fileName).apply { writeBytes(oldBytes) }
        val cache = SamplePdfCache(directory)
        val live = SamplePdfDownloadManager(cache)
        val offline = SamplePdfDownloadManager(cache, SamplePdfTransfer { _, _, _ -> throw IOException("Simulated offline") })
        val report = JSONObject().put("id", item.id).put("url", item.url)
        try {
            assertNull(cache.downloadedFile(item))
            assertEquals(DownloadState.Success, live.download(item).toList().last())
            val local = requireNotNull(cache.downloadedFile(item))
            assertTrue(cache.matches(local, item)); report.put("new_download", true)
            assertEquals(DownloadState.Success, offline.download(item).toList().last())
            report.put("offline_verified_reuse", true)
            RandomAccessFile(local, "rw").use { it.seek(10); val byte = it.readByte(); it.seek(10); it.writeByte(byte.toInt() xor 1) }
            assertNull(cache.downloadedFile(item))
            assertTrue(offline.download(item).toList().last() is DownloadState.Error)
            assertTrue(local.isFile); report.put("corruption_rejected_without_deletion", true)
            assertEquals(DownloadState.Success, live.download(item).toList().last())
            assertTrue(cache.matches(requireNotNull(cache.downloadedFile(item)), item))
            assertArrayEquals(oldBytes, legacy.readBytes())
            report.put("retry_verified", true).put("legacy_preserved", true).put("sha256", item.sha256)
        } finally {
            val output = File(context.getExternalFilesDir(null), "sample-v2-network.json")
            output.writeText(report.toString(2))
            directory.deleteRecursively() // Only this test's uniquely named synthetic cache.
        }
        Unit
    }
}
