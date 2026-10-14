package com.takumayamada22.pdfutility.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class LoadPdfUseCase(
    private val processor: PdfProcessor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun load(inputStream: InputStream, password: String? = null): Int = withContext(ioDispatcher) {
        processor.load(inputStream, password)
    }

    suspend fun append(inputStream: InputStream, password: String? = null): Int = withContext(ioDispatcher) {
        processor.append(inputStream, password)
    }

    suspend fun getMetadata(): Map<String, String> = withContext(ioDispatcher) {
        processor.getMetadata()
    }
}
