package com.takumayamada22.pdfutility

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.takumayamada22.pdfutility.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Uses real Activity configuration changes, not CompositionLocal configuration overrides. */
class ToolbarRotationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun model() = ViewModelProvider(compose.activity,
        PdfViewModel.provideFactory(compose.activity.application))[PdfViewModel::class.java]

    @Test fun activityRotationAndDraftSettingsSurviveRecreation() {
        val activity = compose.activity
        val initialOrientation = activity.requestedOrientation
        try {
            compose.runOnIdle {
                val vm = model()
                vm.dismissTutorial(true)
                vm.setToolbarOptions(ButtonDisplayType.ICON_AND_TEXT, ToolbarProfiles(
                    ToolbarSelection(ToolbarPreset.STANDARD), ToolbarSelection(ToolbarPreset.SIMPLE)))
                val document = PdfDocument()
                val page = document.startPage(PdfDocument.PageInfo.Builder(200, 200, 1).create())
                page.canvas.drawText("Toolbar rotation fixture", 10f, 50f, android.graphics.Paint())
                document.finishPage(page)
                val pdf = File(activity.filesDir, "toolbar-rotation-fixture.pdf")
                pdf.outputStream().use { document.writeTo(it) }; document.close()
                vm.loadPdf(Uri.fromFile(pdf))
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            compose.waitUntil(15000) { model().uiState.value.pages.size == 1 }
            for ((orientation, expected, limit) in listOf(
                Triple(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT, 2),
                Triple(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_LANDSCAPE, 1),
                Triple(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT, 2))) {
                compose.runOnIdle { compose.activity.requestedOrientation = orientation }
                compose.waitUntil(15000) { compose.activity.resources.configuration.orientation == expected }
                compose.waitForIdle()
                val nodes = compose.onAllNodesWithTag("toolbarActionButton").fetchSemanticsNodes()
                assertTrue(nodes.isNotEmpty())
                assertTrue(nodes.map { kotlin.math.round(it.boundsInRoot.top) }.distinct().size <= limit)
                assertEquals(1, model().uiState.value.pages.size)
            }
            compose.runOnIdle { model().openSettingsDialog(SettingsDialogType.OPTIONS) }
            compose.onNodeWithText(compose.activity.getString(R.string.toolbar_compact)).performScrollTo().performClick()
            compose.runOnIdle { compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            compose.waitUntil(15000) { compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            compose.onNodeWithText(compose.activity.getString(R.string.action_apply)).performClick()
            compose.runOnIdle {
                val saved = model().uiState.value.preference.toolbarProfiles!!
                assertEquals(ToolbarPreset.COMPACT, saved.portrait.preset)
                assertEquals(ToolbarPreset.COMPACT, saved.landscape.preset)
            }
        } finally {
            compose.runOnIdle { compose.activity.requestedOrientation = initialOrientation }
        }
    }
}
