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
        SamplePdfLanguage("zh_tw", "繁體中文（台灣）", "Chinese (Traditional, Taiwan)"),
        SamplePdfLanguage("de", "Deutsch", "German"),
        SamplePdfLanguage("fr", "Français", "French"),
        SamplePdfLanguage("es", "Español (España)", "Spanish (Spain)"),
        SamplePdfLanguage("es_419", "Español (Latinoamérica)", "Spanish (Latin America)"),
        SamplePdfLanguage("it", "Italiano", "Italian"),
        SamplePdfLanguage("pt", "Português (Brasil)", "Portuguese (Brazil)"),
        SamplePdfLanguage("pt_pt", "Português (Portugal)", "Portuguese (Portugal)"),
        SamplePdfLanguage("zh_hk", "繁體中文（香港書面語）", "Chinese (Traditional, Hong Kong written standard)"),
        SamplePdfLanguage("vi", "Tiếng Việt", "Vietnamese"),
        SamplePdfLanguage("id", "Bahasa Indonesia", "Indonesian"),
        SamplePdfLanguage("fil", "Filipino", "Filipino"),
        SamplePdfLanguage("ne", "नेपाली", "Nepali")
    ).sortedBy { it.englishName }

    fun defaultCode(locale: java.util.Locale): String = when (locale.language) {
        "zh" -> when {
            locale.country in setOf("HK", "MO") -> "zh_hk"
            locale.country == "TW" || locale.script == "Hant" -> "zh_tw"
            else -> "zh_cn"
        }
        "es" -> if (locale.country in setOf("419", "AR", "BO", "BR", "BZ", "CL", "CO", "CR", "CU", "DO", "EC", "GT", "HN", "MX", "NI", "PA", "PE", "PR", "PY", "SV", "UY", "VE")) "es_419" else "es"
        "pt" -> if (locale.country == "PT") "pt_pt" else "pt"
        "tl", "fil" -> "fil"
        "in", "id" -> "id"
        else -> entries.firstOrNull { it.code == locale.language }?.code ?: "en"
    }

    fun englishName(code: String): String = entries.first { it.code == code }.englishName

    fun displayName(code: String): String = entries.first { it.code == code }.displayName
}
