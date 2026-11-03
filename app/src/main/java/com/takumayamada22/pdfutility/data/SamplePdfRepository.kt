package com.takumayamada22.pdfutility.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import java.io.File

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String, val integrityFailure: Boolean = false) : DownloadState()
}

class SamplePdfRepository(private val context: Context) {
    private val sampleDirectory = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: File(context.filesDir, "Documents"),
        "MihirakiSamplePdfs"
    )
    private val cache = SamplePdfCache(sampleDirectory)
    private val downloads = SamplePdfDownloadManager(cache)

    fun getDownloadedFile(item: SamplePdfItem): File? = cache.downloadedFile(item)
    fun isDownloaded(item: SamplePdfItem): Boolean = getDownloadedFile(item) != null
    fun deletePdf(item: SamplePdfItem): Boolean = cache.delete(item)
    fun downloadPdf(item: SamplePdfItem) = downloads.download(item)

    fun isOnCellularNetwork(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
