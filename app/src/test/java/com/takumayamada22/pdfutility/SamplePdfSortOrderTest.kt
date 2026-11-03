package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.data.SamplePdfSortOrder
import org.junit.Assert.*
import org.junit.Test

class SamplePdfSortOrderTest {
    @Test fun latestFirstAndFirstEpisodeFirstPreserveCatalogAndLanguageTies() {
        for (order in SamplePdfSortOrder.entries) {
            val sorted = SamplePdfCatalog.items.reversed().sortedWith(order.comparator())
            val episodes = sorted.map { it.id.removePrefix("ep").takeWhile(Char::isDigit).toInt() }
            assertEquals(if (order == SamplePdfSortOrder.ASCENDING) episodes.sorted() else episodes.sortedDescending(), episodes)
            assertEquals(SamplePdfCatalog.items.toSet(), sorted.toSet())
            sorted.groupBy { it.id.substringBefore('_') }.values.forEach {
                assertEquals(SamplePdfCatalog.getAvailableLanguages(), it.map { item -> item.languageCode })
            }
            val japanese = sorted.filter { it.languageCode == "ja" }
            assertEquals(if (order == SamplePdfSortOrder.ASCENDING) listOf("ep1_ja", "ep2_ja", "ep3_ja") else listOf("ep3_ja", "ep2_ja", "ep1_ja"), japanese.map { it.id })
        }
    }
}
