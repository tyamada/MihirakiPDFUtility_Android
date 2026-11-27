package com.takumayamada22.pdfutility

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.data.SamplePdfRepository
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
        // This instrumentation test exercises the live catalog, not the frozen 51-book bundle.
        val repository = SamplePdfRepository(compose.activity.application)
        repository.refreshCatalog()
        val catalog = repository.getCatalog()
        assertTrue("Latest catalog must include Japanese episode 4", catalog.any { it.id == "ep4_ja" })
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
            compose.waitUntil(10000) { vm.uiState.value.selectedLanguage == code && vm.uiState.value.items.size == catalog.count { it.languageCode == code } }
            compose.onNodeWithTag("sample-language-filter").assertTextContains(label)
            assertEquals(catalog.filter { it.languageCode == code }.map { it.id }.toSet(), vm.uiState.value.items.map { it.item.id }.toSet())
        }
        compose.onNodeWithTag("sample-language-filter").performClick()
        compose.onNodeWithTag("sample-language-ALL").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.uiState.value.selectedLanguage == "ALL" && vm.uiState.value.items.size == catalog.size }
        assertEquals(catalog.toSet(), vm.uiState.value.items.map { it.item }.toSet())
        val originalOrder = vm.uiState.value.sortOrder
        for (order in listOf(SamplePdfSortOrder.DESCENDING, SamplePdfSortOrder.ASCENDING)) {
            if (vm.uiState.value.sortOrder != order) compose.onNodeWithTag("btn_toggle_order").performClick()
            compose.waitUntil(10000) { vm.uiState.value.sortOrder == order }
            val expected = catalog.sortedWith(order.comparator()).map { it.id }
            assertEquals(expected, vm.uiState.value.items.map { it.item.id })
        }
        if (vm.uiState.value.sortOrder != originalOrder) compose.onNodeWithTag("btn_toggle_order").performClick()
    }
}
