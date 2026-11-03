package com.takumayamada22.pdfutility.data

data class SamplePdfLanguage(val code: String, val nativeName: String, val englishName: String) {
    val displayName: String get() = if (nativeName == englishName) nativeName else "$nativeName ($englishName)"
}

/** Native and English names are language identities, independent of the UI locale. */
object SamplePdfLanguages {
    val entries = listOf(
        SamplePdfLanguage("en", "English", "English"),
        SamplePdfLanguage("ja", "日本語", "Japanese"),
        SamplePdfLanguage("ko", "한국어", "Korean"),
        SamplePdfLanguage("zh_cn", "简体中文", "Chinese (Simplified)"),
        SamplePdfLanguage("zh_tw", "繁體中文", "Chinese (Traditional)"),
        SamplePdfLanguage("de", "Deutsch", "German"),
        SamplePdfLanguage("fr", "Français", "French"),
        SamplePdfLanguage("es", "Español", "Spanish"),
        SamplePdfLanguage("it", "Italiano", "Italian"),
        SamplePdfLanguage("pt", "Português", "Portuguese"),
        SamplePdfLanguage("vi", "Tiếng Việt", "Vietnamese"),
        SamplePdfLanguage("id", "Bahasa Indonesia", "Indonesian"),
        SamplePdfLanguage("fil", "Filipino", "Filipino"),
        SamplePdfLanguage("ne", "नेपाली", "Nepali")
    ).sortedBy { it.englishName }

    fun displayName(code: String): String = entries.first { it.code == code }.displayName
}
