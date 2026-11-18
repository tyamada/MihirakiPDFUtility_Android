package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.ui.*
import org.junit.Assert.*
import org.junit.Test

class ToolbarProfilesTest {
    @Test fun fittingUsesActualWrappingAndReservesMandatoryControls() {
        assertEquals(2, fittingToolbarButtons(listOf(90, 70, 120), 200, 1))
        assertEquals(1, fittingToolbarButtons(listOf(90, 70, 120), 200, 1, listOf(60)))
        assertEquals(3, fittingToolbarButtons(listOf(90, 70, 120), 200, 2, listOf(60)))
        assertEquals(2, fittingToolbarButtons(listOf(140, 140, 40), 200, 2, listOf(60)))
    }
    @Test fun narrowWindowsCannotForceAnOversizedButtonIntoTheRow() {
        assertEquals(0, fittingToolbarButtons(listOf(180, 40), 100, 2))
        assertEquals(0, fittingToolbarButtons(listOf(48), 0, 2))
        assertEquals(0, fittingToolbarButtons(listOf(48), 100, 0))
        assertEquals(0, fittingToolbarButtons(listOf(48), 100, 1, listOf(120)))
    }
    @Test fun oneAndTwoRowsRespondToWidthChanges() {
        val widths = List(24) { 48 }
        assertEquals(6, fittingToolbarButtons(widths, 300, 1))
        assertEquals(12, fittingToolbarButtons(widths, 300, 2))
        assertEquals(8, fittingToolbarButtons(widths, 200, 2))
        assertEquals(24, fittingToolbarButtons(widths, 1200, 2))
    }
    @Test fun manualPortraitSelectionKeepsLandscapeAutomatic() {
        val profiles = ToolbarProfiles().withSelection(false, ToolbarSelection(null, setOf(ActionButtonType.SAVE)))
        assertEquals(setOf(ActionButtonType.SAVE), profiles.forOrientation(false).buttons)
        assertNull(profiles.forOrientation(false).preset)
        assertEquals(ToolbarPreset.STANDARD, profiles.forOrientation(true).preset)
    }
    @Test fun emptyCustomSelectionIsDifferentFromAutomaticDefault() {
        assertNull(ToolbarSelection(null, emptySet()).preset)
        assertEquals(ToolbarPreset.STANDARD, ToolbarProfiles().portrait.preset)
        assertEquals(2, ToolbarPreset.STANDARD.rows)
        assertEquals(ButtonDisplayType.ICON_AND_TEXT, ToolbarPreset.STANDARD.displayType)
        assertEquals(1, ToolbarPreset.SIMPLE.rows)
        assertEquals(ButtonDisplayType.ICON_ONLY, ToolbarPreset.COMPACT.displayType)
    }
    @Test fun orderContainsEveryActionExactlyOnce() {
        assertEquals(ActionButtonType.entries.toSet(), toolbarActionOrder.toSet())
        assertEquals(ActionButtonType.entries.size, toolbarActionOrder.size)
    }
}
