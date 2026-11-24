package com.takumayamada22.pdfutility.ui

/** Suggest a PDF name with exactly one extension for both Save entry points. */
internal fun suggestEditedPdfFileName(sourceName: String?): String {
    var baseName = sourceName ?: "document"
    while (baseName.endsWith(".pdf", ignoreCase = true)) {
        baseName = baseName.dropLast(4)
    }
    return "edited_${baseName.ifBlank { "document" }}.pdf"
}
