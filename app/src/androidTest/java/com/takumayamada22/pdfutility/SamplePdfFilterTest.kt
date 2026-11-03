package com.takumayamada22.pdfutility

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.data.SamplePdfLanguages
import com.takumayamada22.pdfutility.data.SamplePdfSortOrder
import com.takumayamada22.pdfutility.ui.SamplePdfViewModel
import com.takumayamada22.pdfutility.ui.components.SamplePdfScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SamplePdfFilterTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun nativeNamesAreDisplayedAndAllIncludesEveryMangaEpisode() {
        val vm = SamplePdfViewModel(compose.activity.application)
        compose.runOnUiThread { compose.activity.setContent {
            MaterialTheme { SamplePdfScreen(vm, back = {}, onOpenPdf = {}) }
        } }
        compose.waitUntil(10000) { vm.uiState.value.availableLanguages.size == 18 }
        assertEquals(listOf("ALL") + SamplePdfCatalog.getAvailableLanguages(), vm.uiState.value.availableLanguages)
        for ((code, label) in SamplePdfLanguages.entries.map { it.code to it.displayName }) {
            compose.onNodeWithTag("sample-language-filter").performClick()
            compose.onNodeWithTag("sample-language-$code").performScrollTo()
                .assertTextContains(label).performClick()
            compose.waitUntil(10000) { vm.uiState.value.selectedLanguage == code && vm.uiState.value.items.size == 3 }
            compose.onNodeWithTag("sample-language-filter").assertTextContains(label)
            assertEquals(setOf("ep1_$code", "ep2_$code", "ep3_$code"), vm.uiState.value.items.map { it.item.id }.toSet())
        }
        compose.onNodeWithTag("sample-language-filter").performClick()
        compose.onNodeWithTag("sample-language-ALL").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.uiState.value.selectedLanguage == "ALL" && vm.uiState.value.items.size == 51 }
        assertEquals(SamplePdfCatalog.items.toSet(), vm.uiState.value.items.map { it.item }.toSet())
        vm.uiState.value.items.groupBy { it.item.languageCode }.forEach { (_, items) -> assertEquals(3, items.size) }
        val originalOrder = vm.uiState.value.sortOrder
        for (order in listOf(SamplePdfSortOrder.DESCENDING, SamplePdfSortOrder.ASCENDING)) {
            if (vm.uiState.value.sortOrder != order) compose.onNodeWithTag("btn_toggle_order").performClick()
            compose.waitUntil(10000) { vm.uiState.value.sortOrder == order }
            val episodes = if (order == SamplePdfSortOrder.DESCENDING) listOf(3, 2, 1) else listOf(1, 2, 3)
            val expected = episodes.flatMap { episode -> SamplePdfCatalog.getAvailableLanguages().map { "ep${episode}_$it" } }
            assertEquals(expected, vm.uiState.value.items.map { it.item.id })
        }
        if (vm.uiState.value.sortOrder != originalOrder) compose.onNodeWithTag("btn_toggle_order").performClick()
    }
}
