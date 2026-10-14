package com.takumayamada22.pdfutility.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.InputStream

class LoadPdfUseCase(
    private val processor: PdfProcessor,
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
}
