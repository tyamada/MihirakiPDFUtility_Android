package com.takumayamada22.pdfutility.domain

import com.takumayamada22.pdfutility.ui.PageState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

class ExportPdfUseCase(
    private val processor: PdfProcessor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun save(
        outputStream: OutputStream,
        pages: List<PageState>,
        password: String? = null,
        metadata: Map<String, String>? = null
    ) = withContext(ioDispatcher) {
        processor.save(outputStream, pages, password, metadata)
    }
}
