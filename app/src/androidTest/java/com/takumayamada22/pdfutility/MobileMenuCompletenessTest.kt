package com.takumayamada22.pdfutility

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType
import com.takumayamada22.pdfutility.ui.components.PdfTopAppBar
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MobileMenuCompletenessTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun everyDesktopMenuOperationAndSupportIsReachableAtLargeFont() {
        val vm = PdfViewModel(compose.activity.application)
        val labels = listOf(
            R.string.action_open, R.string.menu_open_folder, R.string.sample_pdf,
            R.string.action_save, R.string.action_export_selected,
            R.string.action_undo, R.string.action_redo, R.string.action_select_all,
            R.string.action_clear_selection, R.string.action_invert_selection,
            R.string.action_delete, R.string.action_split_pages, R.string.crop_title,
            R.string.action_move_up, R.string.action_move_down,
            R.string.action_rotate_left, R.string.action_rotate_right,
            R.string.action_add_pdf, R.string.action_insert_blank,
            R.string.action_toggle_view, R.string.action_property,
            R.string.action_right_binding, R.string.action_right_binding_cover,
            R.string.action_toggle_rtl, R.string.settings_options, R.string.settings_password,
            R.string.settings_version, R.string.settings_help, R.string.privacy_policy_title,
            R.string.settings_diagnostics, R.string.support
        )
        compose.runOnUiThread { compose.activity.setContent {
            CompositionLocalProvider(LocalDensity provides Density(compose.activity.resources.displayMetrics.density, 2f)) {
                MaterialTheme {
                    PdfTopAppBar(PdfUiState(), vm, {}, {}, {}, {}, {}, {}, {}, {}, {})
                }
            }
        } }
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.action_settings)).performClick()
        labels.forEach { id ->
            compose.onNode(hasText(compose.activity.getString(id)) and hasClickAction()).performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithText(compose.activity.getString(R.string.support)).performClick()
        compose.runOnIdle { assertEquals(SettingsDialogType.SUPPORT, vm.uiState.value.activeSettingsDialog) }
    }
    @Test fun wideLayoutMenusKeepAllOperations() {
        val vm = PdfViewModel(compose.activity.application)
        val menus = listOf(
            R.string.menu_file to listOf(R.string.action_open, R.string.menu_open_folder, R.string.sample_pdf, R.string.action_save, R.string.action_export_selected),
            R.string.menu_edit to listOf(R.string.action_undo, R.string.action_redo, R.string.action_select_all, R.string.action_clear_selection, R.string.action_invert_selection, R.string.action_delete, R.string.action_split_pages, R.string.crop_title, R.string.action_move_up, R.string.action_move_down, R.string.action_rotate_left, R.string.action_rotate_right),
            R.string.menu_insert to listOf(R.string.action_add_pdf, R.string.action_insert_blank),
            R.string.menu_view to listOf(R.string.action_toggle_view),
            R.string.menu_options to listOf(R.string.action_property, R.string.action_right_binding, R.string.action_right_binding_cover, R.string.action_toggle_rtl, R.string.settings_options, R.string.settings_password),
            R.string.menu_help to listOf(R.string.settings_version, R.string.settings_help, R.string.settings_diagnostics, R.string.support, R.string.privacy_policy_title)
        )
        compose.runOnUiThread { compose.activity.setContent {
            // The dedicated 1080px emulator becomes 1080dp wide: exercises the actual wide-layout branch.
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                MaterialTheme { PdfTopAppBar(PdfUiState(), vm, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
            }
        } }
        menus.forEach { (menu, items) ->
            compose.onNodeWithText(compose.activity.getString(menu)).performScrollTo().performClick()
            items.forEach { id ->
                compose.onNode(hasText(compose.activity.getString(id)) and hasAnyAncestor(isPopup()))
                    .performScrollTo().assertIsDisplayed()
            }
            androidx.test.espresso.Espresso.pressBack()
            compose.waitForIdle()
        }
    }

}
