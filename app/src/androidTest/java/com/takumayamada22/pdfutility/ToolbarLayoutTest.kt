package com.takumayamada22.pdfutility

import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.ui.*
import com.takumayamada22.pdfutility.ui.components.PdfTopAppBar
import com.takumayamada22.pdfutility.ui.components.PdfOptionsDialog
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ToolbarLayoutTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private data class Case(val language: String, val scale: Float, val preset: ToolbarPreset, val landscape: Boolean)

    @Test fun japaneseAndEnglishAutomaticLayoutsFitWithLargeText() {
        val current = mutableStateOf(Case("ja", 1f, ToolbarPreset.STANDARD, false))
        val vm = PdfViewModel(compose.activity.application)
        compose.activity.runOnUiThread { compose.activity.setContent {
            val case = current.value
            val base = LocalContext.current
            val configuration = Configuration(base.resources.configuration).apply {
                setLocales(LocaleList.forLanguageTags(case.language))
                orientation = if (case.landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
            }
            val context = base.createConfigurationContext(configuration)
            val density = LocalDensity.current
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides configuration,
                LocalDensity provides Density(density.density, case.scale)) {
                MaterialTheme {
                    val selection = ToolbarSelection(case.preset)
                    val state = PdfUiState(preference = PdfPreferenceState(buttonDisplayType = case.preset.displayType,
                        toolbarProfiles = ToolbarProfiles(selection, selection))).copy(
                        pages = listOf(PageState(thumbnail = null, originalIndex = 0)))
                    Box(Modifier.width(320.dp)) {
                        PdfTopAppBar(state, vm, {}, {}, {}, {}, {}, {}, {}, {}, {})
                    }
                }
            }
        } }
        for (lang in listOf("ja", "en")) for (scale in listOf(1f, 2f))
            for (preset in ToolbarPreset.entries) for (landscape in listOf(false, true)) {
                val case = Case(lang, scale, preset, landscape)
                compose.runOnIdle { current.value = case }
                compose.waitForIdle()
                val nodes = compose.onAllNodesWithTag("toolbarActionButton").fetchSemanticsNodes()
                assertTrue("No buttons for $case", nodes.isNotEmpty())
                val rows = nodes.map { kotlin.math.round(it.boundsInRoot.top) }.distinct().size
                assertTrue("$case: $rows rows exceeds ${preset.rows}", rows <= preset.rows)
                val ordered = nodes.sortedWith(compareBy({ it.boundsInRoot.top }, { it.boundsInRoot.left }))
                ordered.zipWithNext().forEach { (a, b) ->
                    if (kotlin.math.abs(a.boundsInRoot.top - b.boundsInRoot.top) < 1f)
                        assertTrue("Overlapping buttons: $case", a.boundsInRoot.right <= b.boundsInRoot.left + 1f)
                }
            }
    }
    @Test fun presetsApplyTogetherAndCancelPreservesSavedSettings() {
        val vm = PdfViewModel(compose.activity.application)
        val initial = ToolbarProfiles(ToolbarSelection(ToolbarPreset.SIMPLE), ToolbarSelection(ToolbarPreset.SIMPLE))
        vm.setToolbarOptions(ButtonDisplayType.ICON_AND_TEXT, initial)
        vm.openSettingsDialog(SettingsDialogType.OPTIONS)
        compose.activity.runOnUiThread { compose.activity.setContent {
            val state by vm.uiState.collectAsState()
            MaterialTheme {
                if (state.activeSettingsDialog == SettingsDialogType.OPTIONS) PdfOptionsDialog(vm, state)
            }
        } }
        fun text(id: Int) = compose.activity.getString(id)
        compose.waitForIdle()
        val screenshot = compose.onNode(isDialog()).captureToImage().asAndroidBitmap()
        java.io.File(compose.activity.getExternalFilesDir(null), "toolbar-options.png").outputStream().use {
            screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText(text(R.string.toolbar_hide_all)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.action_cancel)).performClick()
        compose.runOnIdle { assertEquals(initial, vm.uiState.value.preference.toolbarProfiles) }
        val choices = listOf(
            R.string.toolbar_standard to ToolbarPreset.STANDARD,
            R.string.toolbar_simple to ToolbarPreset.SIMPLE,
            R.string.toolbar_compact to ToolbarPreset.COMPACT
        )
        for ((id, preset) in choices) {
            compose.runOnIdle { vm.openSettingsDialog(SettingsDialogType.OPTIONS) }
            compose.onNodeWithText(text(id)).performScrollTo().performClick()
            compose.onNodeWithText(text(R.string.action_apply)).performClick()
            compose.runOnIdle {
                assertEquals(preset.displayType, vm.uiState.value.buttonDisplayType)
                assertEquals(preset, vm.uiState.value.preference.toolbarProfiles!!.portrait.preset)
                assertEquals(preset, vm.uiState.value.preference.toolbarProfiles!!.landscape.preset)
            }
        }
        for (show in listOf(true, false)) {
            compose.runOnIdle { vm.openSettingsDialog(SettingsDialogType.OPTIONS) }
            compose.onNodeWithText(text(if (show) R.string.toolbar_show_all else R.string.toolbar_hide_all)).performScrollTo().performClick()
            compose.onNodeWithText(text(R.string.action_apply)).performClick()
            compose.runOnIdle {
                val profiles = vm.uiState.value.preference.toolbarProfiles!!
                assertNull(profiles.portrait.preset)
                assertEquals(if (show) ActionButtonType.entries.toSet() else emptySet<ActionButtonType>(), profiles.portrait.buttons)
                assertEquals(profiles.portrait, profiles.landscape)
                assertEquals(ButtonDisplayType.ICON_AND_TEXT, vm.uiState.value.buttonDisplayType)
                val restored = PdfViewModel(compose.activity.application).uiState.value
                assertEquals(profiles, restored.preference.toolbarProfiles)
            }
        }
        compose.runOnIdle { vm.openSettingsDialog(SettingsDialogType.OPTIONS) }
        compose.onNodeWithText(text(R.string.toolbar_portrait)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.action_save)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.action_apply)).performClick()
        compose.runOnIdle {
            val profiles = vm.uiState.value.preference.toolbarProfiles!!
            assertEquals(setOf(ActionButtonType.SAVE), profiles.portrait.buttons)
            assertTrue(profiles.landscape.buttons.isEmpty())
        }
    }
}
