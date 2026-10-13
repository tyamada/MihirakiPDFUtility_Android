package com.takumayamada22.pdfutility.domain

import java.io.InputStream

class LoadPdfUseCase(private val processor: PdfProcessor) {
    fun load(inputStream: InputStream, password: String? = null): Int {
        return processor.load(inputStream, password)
    }

    fun append(inputStream: InputStream, password: String? = null): Int {
        return processor.append(inputStream, password)
    }

    fun getMetadata(): Map<String, String> {
        return processor.getMetadata()
    }
}
