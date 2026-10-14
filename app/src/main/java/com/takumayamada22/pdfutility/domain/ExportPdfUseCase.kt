package com.takumayamada22.pdfutility.domain

import com.takumayamada22.pdfutility.ui.PageState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.OutputStream

class ExportPdfUseCase(
    private val processor: PdfProcessor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun save(
        outputStream: OutputStream,
        pages: List<PageState>,
        password: String? = null,
        metadata: Map<String, String>? = null
    ) = runBlocking(ioDispatcher) {
        processor.save(outputStream, pages, password, metadata)
    }
}
