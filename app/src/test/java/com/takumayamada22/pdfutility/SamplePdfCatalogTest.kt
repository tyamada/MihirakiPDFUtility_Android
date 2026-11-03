package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import org.junit.Assert.*
import org.junit.Test

class SamplePdfCatalogTest {
    @Test fun v2HasUniqueVerifiedEpisodesAndPreservesAllExistingIds() {
        val items = SamplePdfCatalog.items
        val previous = setOf("ja", "en", "ko", "zh_cn", "zh_tw", "de", "fr", "es", "pt", "vi", "fil", "ne", "id", "it")
        val regions = previous + setOf("es_419", "pt_pt", "zh_hk")
        assertEquals(51, items.size)
        for (values in listOf(items.map { it.id }, items.map { it.fileName }, items.map { it.url })) {
            assertEquals(51, values.distinct().size)
        }
        assertEquals(regions, items.map { it.languageCode }.toSet())
        assertEquals(previous.flatMap { code -> (1..3).map { "ep${it}_$code" } }.toSet(),
            items.filter { it.languageCode in previous }.map { it.id }.toSet())
        items.groupBy { it.languageCode }.forEach { (code, episodes) ->
            assertEquals(setOf("ep1_$code", "ep2_$code", "ep3_$code"), episodes.map { it.id }.toSet())
        }
        items.forEach {
            assertEquals("https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/${it.fileName}", it.url)
            assertTrue(it.sha256.matches(Regex("[0-9a-f]{64}")))
            assertTrue(it.bytes > 0L)
            assertEquals(8, it.pageCount)
            assertFalse(it.title.isBlank())
        }
        assertEquals(listOf("tameshibu_episode1_3_fil.pdf", "tameshibu_episode2_3_fil.pdf", "tameshibu_episode3_2_tl.pdf"),
            items.filter { it.languageCode == "fil" }.sortedBy { it.id }.map { it.fileName })
        assertEquals("b84e714206e569318e31ee6255595649decdc57563933fb6d6453d96570de85a", items.single { it.id == "ep1_ja" }.sha256)
    }
}
