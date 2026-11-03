package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.data.SamplePdfLanguages
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class SamplePdfLanguagesTest {
    @Test fun regionalFiltersHaveDistinctNamesAndEnglishAlphabeticalOrder() {
        assertEquals(listOf("zh_cn", "zh_hk", "zh_tw", "en", "fil", "fr", "de", "id", "it", "ja", "ko", "ne", "pt", "pt_pt", "es_419", "es", "vi"), SamplePdfCatalog.getAvailableLanguages())
        val names = SamplePdfLanguages.entries.map { it.englishName }
        assertEquals(names.sorted(), names)
        assertEquals(17, SamplePdfLanguages.entries.map { it.displayName }.distinct().size)
        for (pair in listOf("es" to "es_419", "pt" to "pt_pt", "zh_tw" to "zh_hk")) {
            assertNotEquals(SamplePdfLanguages.displayName(pair.first), SamplePdfLanguages.displayName(pair.second))
        }
        assertTrue(SamplePdfLanguages.displayName("zh_hk").contains("香港書面語"))
        assertFalse(SamplePdfLanguages.displayName("zh_hk").contains("Cantonese"))
        assertEquals(SamplePdfCatalog.items.map { it.languageCode }.toSet(), SamplePdfLanguages.entries.map { it.code }.toSet())
    }

    @Test fun deviceLocaleUsesTheCorrespondingRegionWithoutMergingVariants() {
        for ((tag, code) in mapOf("es-ES" to "es", "es-MX" to "es_419", "es-419" to "es_419", "pt-BR" to "pt", "pt-PT" to "pt_pt", "zh-Hant-TW" to "zh_tw", "zh-Hant-HK" to "zh_hk", "zh-CN" to "zh_cn", "fil-PH" to "fil", "tl-PH" to "fil", "id-ID" to "id", "ar-SA" to "en")) {
            assertEquals(tag, code, SamplePdfLanguages.defaultCode(Locale.forLanguageTag(tag)))
        }
    }
}
