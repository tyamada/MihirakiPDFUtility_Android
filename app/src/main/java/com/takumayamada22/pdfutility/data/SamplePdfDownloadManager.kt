package com.takumayamada22.pdfutility.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.HttpsURLConnection

internal fun interface SamplePdfTransfer {
    suspend fun fetch(item: SamplePdfItem, target: File, progress: suspend (Float) -> Unit)
}

internal class HttpsSamplePdfTransfer : SamplePdfTransfer {
    override suspend fun fetch(item: SamplePdfItem, target: File, progress: suspend (Float) -> Unit) {
        var url = URL(item.url)
        for (redirect in 0..5) {
            require(url.protocol == "https") { "Only HTTPS sample downloads are supported" }
            val connection = url.openConnection() as HttpsURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 20_000
            connection.readTimeout = 60_000
            try {
                val status = connection.responseCode
                if (status in setOf(301, 302, 303, 307, 308)) {
                    val location = connection.getHeaderField("Location") ?: throw IOException("Missing redirect")
                    url = URL(url, location)
                    continue
                }
                if (status != HttpsURLConnection.HTTP_OK) throw IOException("HTTP $status")
                connection.inputStream.use { input ->
                    target.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var copied = 0L
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val count = input.read(buffer)
                            if (count < 0) break
                            copied += count
                            if (copied > item.bytes) throw SamplePdfIntegrityException()
                            output.write(buffer, 0, count)
                            progress((copied.toFloat() / item.bytes).coerceIn(0f, 1f))
                        }
                    }
                }
                return
            } finally { connection.disconnect() }
        }
        throw IOException("Too many HTTPS redirects")
    }
}

internal class SamplePdfDownloadManager(
    private val cache: SamplePdfCache,
    private val transfer: SamplePdfTransfer = HttpsSamplePdfTransfer()
) {
    private val locks = ConcurrentHashMap<String, Mutex>()

    fun download(item: SamplePdfItem): Flow<DownloadState> = flow {
        locks.getOrPut(item.sha256 + "/" + item.fileName) { Mutex() }.withLock {
            emit(DownloadState.Downloading(0f))
            var temporary: File? = null
            try {
                if (cache.downloadedFile(item) == null) {
                    temporary = cache.temporaryFile(item)
                    transfer.fetch(item, temporary) { emit(DownloadState.Downloading(it)) }
                    cache.publish(item, temporary)
                }
                emit(DownloadState.Success)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                emit(DownloadState.Error(error.message ?: "Download failed", error is SamplePdfIntegrityException))
            } finally {
                temporary?.delete() // Only this attempt's temporary file; never legacy/user PDFs.
            }
        }
    }.flowOn(Dispatchers.IO)
}
