package com.takumayamada22.pdfutility

import android.app.Application
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.ui.*
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class AllEditingButtonsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrument get() = InstrumentationRegistry.getInstrumentation()
    private lateinit var vm: PdfViewModel
    private val app get() = instrument.targetContext.applicationContext as Application
    private fun main(action: () -> Unit) = compose.runOnIdle(action)

    @Before
    fun setUp() {
        compose.activityRule.scenario.onActivity { activity ->
            vm = ViewModelProvider(activity, PdfViewModel.provideFactory(app))[PdfViewModel::class.java]
        }
        val testFile = File(app.cacheDir, "chapter1_test.pdf")
        val doc = PDDocument()
        repeat(8) { doc.addPage(PDPage()) }
        val out = FileOutputStream(testFile)
        doc.save(out)
        doc.close()
        out.close()

        main {
            vm.clearSelection()
            vm.loadPdf(Uri.fromFile(testFile))
        }
        waitForReady(8)
    }

    private fun waitForReady(count: Int? = null) {
        compose.waitUntil(90000) { !vm.uiState.value.isLoading && vm.uiState.value.pages.isNotEmpty() && (count == null || vm.uiState.value.pages.size == count) }
        assertNull(vm.uiState.value.errorMessage)
        compose.waitForIdle()
    }

    private fun clickAction(id: Int) {
        val label = compose.activity.getString(id)
        compose.onNodeWithContentDescription(label, useUnmergedTree = true).performClick()
        compose.waitForIdle()
    }

    @Test
    fun testAllEditingButtonsAndFeatures() {
        val state = { vm.uiState.value }

        // --- Phase 1: Unselected state buttons (Save, Undo, Redo, View, Property, Binding, Settings) ---
        clickAction(R.string.action_toggle_view)
        assertTrue(state().isMihirakiView)

        clickAction(R.string.action_toggle_rtl)
        assertTrue(state().isRtl)

        clickAction(R.string.action_right_binding_cover)
        assertTrue(state().showCover)

        clickAction(R.string.action_property)
        assertEquals(SettingsDialogType.PROPERTY, state().activeSettingsDialog)
        compose.onNodeWithText(compose.activity.getString(R.string.prop_title)).assertExists()
        main { vm.closeSettingsDialog() }

        clickAction(R.string.action_settings)
        compose.onNodeWithText(compose.activity.getString(R.string.settings_password)).performClick()
        assertEquals(SettingsDialogType.PASSWORD, state().activeSettingsDialog)
        main { vm.closeSettingsDialog() }

        // --- Phase 2: Selected state buttons (Select All, Invert, Clear, Rotate, Move, Split, Insert, Delete) ---
        // Select page 0 to enter selection mode
        main { vm.toggleSelection(0) }
        assertFalse(state().selectedIndices.isEmpty())

        clickAction(R.string.action_select_all)
        assertEquals(state().pages.size, state().selectedIndices.size)

        clickAction(R.string.action_invert_selection)
        assertTrue(state().selectedIndices.isEmpty())

        // Re-select page 0
        main { vm.toggleSelection(0) }

        // Page Rotation
        val initialRotation0 = state().pages[0].rotation
        clickAction(R.string.action_rotate_right)
        assertEquals((initialRotation0 + 90) % 360, state().pages[0].rotation)

        clickAction(R.string.action_rotate_left)
        assertEquals(initialRotation0, state().pages[0].rotation)

        // Page Movement
        val page0Id = state().pages[0].id
        val page1Id = state().pages[1].id
        clickAction(R.string.action_move_down)
        assertEquals(page1Id, state().pages[0].id)
        assertEquals(page0Id, state().pages[1].id)

        clickAction(R.string.action_move_up)
        assertEquals(page0Id, state().pages[0].id)

        // Insert Blank Page
        val countBeforeInsert = state().pages.size
        clickAction(R.string.action_insert_blank)
        assertEquals(countBeforeInsert + 1, state().pages.size)

        // Split Pages
        main { vm.clearSelection(); vm.toggleSelection(0) }
        clickAction(R.string.action_split_pages)
        compose.onNodeWithText(compose.activity.getString(R.string.split_vertical)).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.action_run)).performClick()
        waitForReady()

        // Delete Pages
        main { vm.clearSelection(); vm.toggleSelection(0) }
        clickAction(R.string.action_delete)
        compose.onNodeWithText(compose.activity.getString(R.string.dialog_delete_title)).assertExists()
        compose.onAllNodesWithText(compose.activity.getString(R.string.action_delete)).get(1).performClick()
        waitForReady()

        // Clear Selection
        main { vm.toggleSelection(0) }
        clickAction(R.string.action_clear_selection)
        assertTrue(state().selectedIndices.isEmpty())

        // --- Phase 3: Append & Save ---
        val extraFile = File(app.cacheDir, "extra_append.pdf")
        val docExtra = PDDocument()
        docExtra.addPage(PDPage())
        val outExtra = FileOutputStream(extraFile)
        docExtra.save(outExtra)
        docExtra.close()
        outExtra.close()

        main {
            vm.appendPdf(Uri.fromFile(extraFile))
        }
        waitForReady()

        val saveFile = File(app.cacheDir, "final_saved.pdf")
        main {
            vm.savePdf(Uri.fromFile(saveFile))
        }
        waitForReady()
        assertTrue(saveFile.exists())
    }
}
