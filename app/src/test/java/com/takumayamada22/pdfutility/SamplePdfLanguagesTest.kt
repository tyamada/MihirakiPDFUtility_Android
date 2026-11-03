package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.data.SamplePdfLanguages
import org.junit.Assert.*
import org.junit.Test

class SamplePdfLanguagesTest {
    @Test fun filterNamesAndEnglishAlphabeticalOrderCoverEveryCatalogLanguage() {
        assertEquals(listOf("zh_cn", "zh_tw", "en", "fil", "fr", "de", "id", "it", "ja", "ko", "ne", "pt", "es", "vi"), SamplePdfCatalog.getAvailableLanguages())
        assertEquals("Español (Spanish)", SamplePdfLanguages.displayName("es"))
        assertEquals("Italiano (Italian)", SamplePdfLanguages.displayName("it"))
        assertEquals("简体中文 (Chinese (Simplified))", SamplePdfLanguages.displayName("zh_cn"))
        assertEquals("繁體中文 (Chinese (Traditional))", SamplePdfLanguages.displayName("zh_tw"))
        SamplePdfLanguages.entries.forEach {
            assertTrue(it.displayName.contains(it.nativeName))
            assertTrue(it.displayName.contains(it.englishName))
            assertNotEquals(it.code.uppercase(), it.displayName)
        }
        assertEquals(SamplePdfCatalog.items.map { it.languageCode }.toSet(), SamplePdfLanguages.entries.map { it.code }.toSet())
    }
}
