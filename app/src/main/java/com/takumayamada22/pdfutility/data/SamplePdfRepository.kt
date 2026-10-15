package com.takumayamada22.pdfutility.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class SamplePdfRepository(private val context: Context) {

    private val sampleDir: File
        get() {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MihirakiSamplePdfs")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun getLocalFile(fileName: String): File {
        return File(sampleDir, fileName)
    }

    fun isDownloaded(fileName: String): Boolean {
        val file = getLocalFile(fileName)
        val tempFile = File(sampleDir, "$fileName.tmp")
        if (file.exists() && file.length() == 0L) {
            file.delete()
        }
        return file.exists() && file.length() > 0 && !tempFile.exists()
    }

    fun deletePdf(fileName: String): Boolean {
        val file = getLocalFile(fileName)
        val tempFile = File(sampleDir, "$fileName.tmp")
        if (tempFile.exists()) tempFile.delete()
        return if (file.exists()) file.delete() else true
    }

    fun isOnCellularNetwork(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        val hasCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val hasWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        return hasCellular && !hasWifi
    }

    fun downloadPdf(item: SamplePdfItem): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0f))
        val targetFile = getLocalFile(item.fileName)
        val tempFile = File(sampleDir, "${item.fileName}.tmp")
        try {
            val url = URL(item.url)
            val connection = url.openConnection() as HttpsURLConnection
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                emit(DownloadState.Error("HTTP error: $responseCode"))
                if (tempFile.exists()) tempFile.delete()
                return@flow
            }

            val fileLength = connection.contentLength

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesCopied = 0L
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } >= 0) {
                        output.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead
                        if (fileLength > 0) {
                            val progress = bytesCopied.toFloat() / fileLength.toFloat()
                            emit(DownloadState.Downloading(progress.coerceIn(0f, 1f)))
                        }
                    }
                }
            }

            if (targetFile.exists()) {
                targetFile.delete()
            }
            if (tempFile.renameTo(targetFile)) {
                emit(DownloadState.Success)
            } else {
                emit(DownloadState.Error("Failed to save downloaded file"))
            }
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            emit(DownloadState.Error(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)
}
