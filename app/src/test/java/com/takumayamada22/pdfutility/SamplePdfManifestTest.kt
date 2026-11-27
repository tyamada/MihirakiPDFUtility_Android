package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.data.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.security.MessageDigest

class SamplePdfManifestTest {
    private fun fixture() = requireNotNull(javaClass.getResource("/sample-pdf-manifest-v1.json")).readText()
    private fun changed(change: (JSONObject) -> Unit): String = JSONObject(fixture()).also(change).toString()
    private fun rejected(text: String) {
        try { SamplePdfManifest.parse(text); fail("Invalid manifest accepted") } catch (_: IllegalArgumentException) {} catch (_: org.json.JSONException) {}
    }

    @Test fun publishedFixturePreservesAllIdentitiesAndFilipinoCacheAliases() {
        val items = SamplePdfManifest.parse(fixture())
        assertEquals(51, items.size)
        for (item in items) assertEquals(SamplePdfCatalog.items.single { it.id == item.id }, item)
        for (episode in 1..2) {
            val item = items.single { it.id == "ep${episode}_fil" }
            assertTrue(item.url.endsWith("_tl.pdf"))
            assertTrue(item.fileName.endsWith("_fil.pdf"))
            assertEquals("fil", item.languageCode)
        }
    }

    @Test fun japaneseOnlyEpisodeFourRefreshesOldCatalogAndSurvivesRestart() {
        val dir = Files.createTempDirectory("episode-four-refresh").toFile()
        try {
            var response = fixture()
            val store = SamplePdfManifestStore(dir) { response }
            assertEquals(51, store.refresh().size)
            // Root compatibility URLs deliberately remain on the original 51-book release.
            response = changed { root ->
                val entries = root.getJSONArray("entries")
                val entry = JSONObject(entries.getJSONObject(0).toString())
                    .put("sample_id", "ep4_ja").put("episode", 4)
                    .put("language_key", "ja").put("locale", "ja-JP")
                    .put("filename", "tameshibu_episode4_ja.pdf")
                    .put("title", "ためし部 第4話 風と、なかよく。")
                    .put("bytes", 99_981_486L).put("pages", 8)
                    .put("sha256", "7c4bde39dc25800aa097854c6e47f31934abe12b3a05d97450fb73523af3c3dc")
                    .put("new_download_url", "https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261127-ep4-ja-v1/tameshibu_episode4_ja.pdf")
                entries.put(entry)
                root.put("pdf_count", entries.length())
            }
            val items = store.refresh()
            assertEquals(52, items.size)
            assertEquals(4, items.count { it.languageCode == "ja" })
            assertEquals(3, items.count { it.languageCode == "en" })
            assertTrue(items.single { it.id == "ep4_ja" }.url.contains("manga-pdf-20261127-ep4-ja-v1"))
            assertEquals("ep4_ja", items.sortedWith(SamplePdfSortOrder.DESCENDING.comparator()).first().id)
            val restarted = SamplePdfManifestStore(dir) { throw IOException("offline") }
            assertEquals(items, restarted.refresh())
            assertEquals(2, File(dir, "snapshots").listFiles()!!.size)
        } finally { dir.deleteRecursively() }
    }

    @Test fun invalidWholeListsAndSchemaAreRejected() {
        listOf("{", "{}", "{unquoted: 1}", "{\"a\":1,}", "/* comment */" + fixture(), "[".repeat(40) + "]".repeat(40), fixture().replaceFirst("\"schema_version\": 1", "\"schema_version\": 1, \"schema_version\": 1"), fixture() + "garbage", changed { it.put("schema_version", 2) },
            changed { it.put("schema_version", "1") }, changed { it.put("repository", "other/repo") }, changed { it.put("status", "draft") },
            changed { it.put("pdf_count", 52) }, changed { it.getJSONArray("entries").remove(0) },
            changed { val a = it.getJSONArray("entries"); a.put(1, a.getJSONObject(0)) },
            " ".repeat(SamplePdfManifest.MAX_BYTES + 1)).forEach(::rejected)
    }

    @Test fun invalidFieldsAndUntrustedPdfUrlsAreRejected() {
        val bad = listOf("new_download_url" to "http://github.com/tyamada/manga-prompt-kit/releases/download/v1/a.pdf",
            "new_download_url" to "https://github.com/other/repo/releases/download/v1/a.pdf",
            "new_download_url" to "https://github.com/tyamada/manga-prompt-kit/releases/latest/download/a.pdf",
            "new_download_url" to "https://user@github.com/tyamada/manga-prompt-kit/releases/download/v1/a.pdf",
            "new_download_url" to "https://github.com/tyamada/manga-prompt-kit/releases/download/v1/a.pdf?x=1",
            "filename" to "../a.pdf", "sha256" to "bad", "bytes" to 0, "bytes" to 1.5,
            "bytes" to "1024", "pages" to 0, "language_key" to "tl", "locale" to "unknown",
            "sample_id" to "ep9_ja", "license" to "unknown", "title" to "bad\nlabel")
        for ((key, value) in bad) rejected(changed { it.getJSONArray("entries").getJSONObject(0).put(key, value) })
    }

    @Test fun lastGoodListSurvivesNetworkFailureInvalidResponsesAndProcessRestart() {
        val dir = Files.createTempDirectory("sample-manifest-test").toFile()
        try {
            var response = fixture()
            var offline = false
            val store = SamplePdfManifestStore(dir) { if (offline) throw IOException("offline") else response }
            val good = store.refresh()
            val saved = File(dir, "current.json").readBytes()
            response = "invalid"
            assertEquals(good, store.refresh())
            assertArrayEquals(saved, File(dir, "current.json").readBytes())
            offline = true
            assertEquals(good, store.refresh())
            assertEquals(good, SamplePdfManifestStore(dir) { throw IOException("offline") }.read())
            assertEquals(1, File(dir, "snapshots").listFiles()!!.size)
        } finally { dir.deleteRecursively() }
    }

    @Test fun missingOrCorruptCachedListFallsBackToBundledList() {
        val dir = Files.createTempDirectory("sample-manifest-test").toFile()
        try {
            val store = SamplePdfManifestStore(dir) { throw IOException("offline") }
            assertEquals(SamplePdfCatalog.items, store.refresh())
            File(dir, "current.json").writeText("corrupt")
            assertEquals(SamplePdfCatalog.items, store.read())
        } finally { dir.deleteRecursively() }
    }

    @Test fun validNewEditionKeepsPreviouslyVerifiedDownloadOffline() {
        val dir = Files.createTempDirectory("sample-edition-test").toFile()
        try {
            val payload = "%PDF-verified old edition".toByteArray()
            val hash = MessageDigest.getInstance("SHA-256").digest(payload).joinToString("") { "%02x".format(it.toInt() and 255) }
            var response = changed { it.getJSONArray("entries").getJSONObject(0).put("bytes", payload.size).put("sha256", hash) }
            val store = SamplePdfManifestStore(File(dir, "catalog")) { response }
            val old = store.refresh().first()
            val pdfs = File(dir, "pdfs").also { it.mkdirs() }
            val oldFile = File(pdfs, old.fileName).also { it.writeBytes(payload) }
            val cache = SamplePdfCache(pdfs)
            response = changed { it.getJSONArray("entries").getJSONObject(0).put("sha256", "a".repeat(64)) }
            val latest = store.refresh()
            val restarted = SamplePdfManifestStore(File(dir, "catalog")) { throw IOException("offline") }
            val (items, files) = resolveSamplePdfCatalog(restarted.read(), { restarted.previousCatalogs() }, cache::downloadedFile)
            assertNotEquals(old.sha256, latest.first().sha256)
            assertEquals(old, items.first())
            assertEquals(oldFile, files[old.id])
            assertArrayEquals(payload, oldFile.readBytes())
        } finally { dir.deleteRecursively() }
    }

    @Test fun metadataRefreshDoesNotFetchPdfAndRedirectsRejectUntrustedHosts() {
        for (url in listOf("http://github.com/a", "https://evil.example/a", "https://github.com/other/repo/a", "https://github.com:444/tyamada/manga-prompt-kit/releases/download/a")) {
            try { SamplePdfEndpoints.requireRedirect(url); fail(url) } catch (_: IllegalArgumentException) {} catch (_: IllegalStateException) {}
        }
        SamplePdfEndpoints.requireRedirect("https://release-assets.githubusercontent.com/github-production-release-asset/a?signature=public-test")
    }
    @Test fun slowNetworkDoesNotBlockReadingOfflineCatalog() {
        val dir = Files.createTempDirectory("sample-slow-network-test").toFile()
        val started = java.util.concurrent.CountDownLatch(1)
        val release = java.util.concurrent.CountDownLatch(1)
        val executor = java.util.concurrent.Executors.newFixedThreadPool(2)
        try {
            val store = SamplePdfManifestStore(dir) { started.countDown(); release.await(); fixture() }
            val refresh = executor.submit<List<SamplePdfItem>> { store.refresh() }
            assertTrue(started.await(5, java.util.concurrent.TimeUnit.SECONDS))
            val cached = executor.submit<List<SamplePdfItem>> { store.read() }
            assertEquals(SamplePdfCatalog.items, cached.get(2, java.util.concurrent.TimeUnit.SECONDS))
            release.countDown()
            assertEquals(51, refresh.get(5, java.util.concurrent.TimeUnit.SECONDS).size)
        } finally { release.countDown(); executor.shutdownNow(); dir.deleteRecursively() }
    }

}
