package com.takumayamada22.pdfutility.data

enum class SamplePdfSortOrder {
    ASCENDING,
    DESCENDING;

    fun comparator(): Comparator<SamplePdfItem> = compareBy<SamplePdfItem> {
        val episode = it.id.removePrefix("ep").takeWhile(Char::isDigit).toIntOrNull() ?: 1
        if (this == ASCENDING) episode else -episode
    }.thenBy { it.languageDisplayName }
}
