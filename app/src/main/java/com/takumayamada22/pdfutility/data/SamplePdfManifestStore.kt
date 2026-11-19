package com.takumayamada22.pdfutility.data

import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.HttpsURLConnection

internal fun interface SamplePdfManifestFetch { fun fetch(): String }

internal class HttpsSamplePdfManifestFetch : SamplePdfManifestFetch {
    override fun fetch(): String {
        var url = URL(SamplePdfManifest.LATEST_URL)
        repeat(6) {
            SamplePdfEndpoints.requireRedirect(url.toString())
            val connection = url.openConnection() as HttpsURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            try {
                val status = connection.responseCode
                if (status in setOf(301, 302, 303, 307, 308)) {
                    url = URL(url, connection.getHeaderField("Location") ?: throw IOException("Missing redirect"))
                } else {
                    if (status != 200) throw IOException("Manifest HTTP $status")
                    require(connection.contentLengthLong <= SamplePdfManifest.MAX_BYTES)
                    val bytes = connection.inputStream.use { input ->
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            require(output.size() + count <= SamplePdfManifest.MAX_BYTES)
                            output.write(buffer, 0, count)
                        }
                        output.toByteArray()
                    }
                    return Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
                }
            } finally { connection.disconnect() }
        }
        throw IOException("Too many manifest redirects")
    }
}

/** Last-good metadata is atomically published, never a partial or unvalidated response.
 * Valid historical snapshots let previously downloaded, hash-verified editions remain usable
 * even when a future manifest changes their hash. No PDF is removed or fetched here.
 */
internal class SamplePdfManifestStore(
    private val directory: File,
    private val fetch: SamplePdfManifestFetch = HttpsSamplePdfManifestFetch()
) {
    private val lock = locks.getOrPut(directory.absolutePath) { Any() }
    private val current get() = File(directory, "current.json")
    private val snapshots get() = File(directory, "snapshots")

    fun read(): List<SamplePdfItem> = synchronized(lock) { parseFile(current) ?: SamplePdfCatalog.items }

    fun refresh(): List<SamplePdfItem> {
        return try {
            val text = fetch.fetch()
            val validated = SamplePdfManifest.parse(text)
            val hash = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
            synchronized(lock) {
                val snapshot = File(snapshots, "$hash.json")
                if (!snapshot.isFile) atomicWrite(snapshot, text)
                atomicWrite(current, text)
                validated
            }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) { read() }
    }

    fun previousCatalogs(): List<List<SamplePdfItem>> = synchronized(lock) {
        snapshots.listFiles()?.filter { it.isFile && it.name.matches(Regex("[0-9a-f]{64}\\.json")) }
            ?.sortedByDescending { it.lastModified() }?.mapNotNull(::parseFile).orEmpty() + listOf(SamplePdfCatalog.items)
    }

    private fun parseFile(file: File): List<SamplePdfItem>? = try {
        if (!file.isFile || file.length() > SamplePdfManifest.MAX_BYTES) null else SamplePdfManifest.parse(file.readText())
    } catch (_: Exception) { null }

    private fun atomicWrite(target: File, text: String) {
        val parent = requireNotNull(target.parentFile)
        if (!parent.isDirectory && !parent.mkdirs()) throw IOException("Manifest directory unavailable")
        val temporary = File.createTempFile("manifest-", ".tmp", parent)
        try {
            temporary.outputStream().use { output -> output.write(text.toByteArray(Charsets.UTF_8)); output.fd.sync() }
            Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally { temporary.delete() }
    }

    companion object { private val locks = ConcurrentHashMap<String, Any>() }
}

/** Resolve metadata and files together so full PDF hashes are not repeated on progress ticks. */
internal fun resolveSamplePdfCatalog(
    latest: List<SamplePdfItem>,
    previousCatalogs: () -> List<List<SamplePdfItem>>,
    downloadedFile: (SamplePdfItem) -> File?
): Pair<List<SamplePdfItem>, Map<String, File?>> {
    val previous by lazy { previousCatalogs().flatten().groupBy { it.id } }
    val files = mutableMapOf<String, File?>()
    val items = latest.map { incoming ->
        var file = downloadedFile(incoming)
        val selected = if (file != null) incoming else {
            previous[incoming.id].orEmpty().firstOrNull { old ->
                downloadedFile(old)?.also { file = it } != null
            } ?: incoming
        }
        files[selected.id] = file
        selected
    }
    return items to files
}
