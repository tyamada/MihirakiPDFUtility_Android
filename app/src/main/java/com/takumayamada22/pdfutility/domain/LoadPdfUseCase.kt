package com.takumayamada22.pdfutility.domain

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.io.OutputStream

class LoadPdfUseCase(
    internal val processor: PdfProcessor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun load(inputStream: InputStream, password: String? = null): Int = runBlocking(ioDispatcher) {
        processor.load(inputStream, password)
    }

    fun append(inputStream: InputStream, password: String? = null): Int = runBlocking(ioDispatcher) {
        processor.append(inputStream, password)
    }

    fun getMetadata(): Map<String, String> = runBlocking(ioDispatcher) {
        processor.getMetadata()
    }

    fun createPdfFromImages(imageUris: List<Uri>, context: Context): Int = runBlocking(ioDispatcher) {
        processor.createPdfFromImages(imageUris, context)
    }

    fun save(outputStream: OutputStream) = runBlocking(ioDispatcher) {
        processor.save(outputStream)
    }
}
