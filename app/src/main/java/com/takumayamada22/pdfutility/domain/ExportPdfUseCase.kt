package com.takumayamada22.pdfutility.domain

import com.takumayamada22.pdfutility.ui.PageState
import java.io.OutputStream

class ExportPdfUseCase(private val processor: PdfProcessor) {
    fun save(
        outputStream: OutputStream,
        pages: List<PageState>,
        password: String? = null,
        metadata: Map<String, String>? = null
    ) {
        processor.save(outputStream, pages, password, metadata)
    }
}

