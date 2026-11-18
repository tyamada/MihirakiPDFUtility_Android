package com.takumayamada22.pdfutility

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.ui.*
import com.takumayamada22.pdfutility.util.PreferencesManager
import org.junit.Assert.*
import org.junit.Test

class ToolbarPreferencesTest {
    /** Dedicated preference file: never clears the app's real preferences. */
    private fun isolated(test: (Context, SharedPreferences) -> Unit) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "toolbar_test_" + java.util.UUID.randomUUID().toString()
        val prefs = base.getSharedPreferences(name, Context.MODE_PRIVATE)
        val context = object : ContextWrapper(base) {
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = prefs
        }
        try { test(context, prefs) } finally { base.deleteSharedPreferences(name) }
    }
    @Test fun freshInstallDefaultsToStandard() = isolated { context, _ ->
        assertEquals(ButtonDisplayType.ICON_AND_TEXT, PreferencesManager.getButtonDisplayType(context))
        assertEquals(ToolbarProfiles(), PreferencesManager.getToolbarProfiles(context, emptySet()))
    }
    @Test fun legacyEmptySelectionAndFormatArePreservedInBothOrientations() = isolated { context, prefs ->
        prefs.edit().putStringSet("visible_action_buttons", emptySet()).putString("button_display_type", "TEXT_ONLY").commit()
        val profiles = PreferencesManager.getToolbarProfiles(context, setOf(ActionButtonType.OPEN))
        assertNull(profiles.portrait.preset)
        assertTrue(profiles.portrait.buttons.isEmpty())
        assertEquals(profiles.portrait, profiles.landscape)
        assertEquals(ButtonDisplayType.TEXT_ONLY, PreferencesManager.getButtonDisplayType(context))
    }
    @Test fun legacyUnknownButtonNamesAreIgnored() = isolated { context, prefs ->
        prefs.edit().putStringSet("visible_action_buttons", setOf("SAVE", "REMOVED_ACTION")).commit()
        assertEquals(setOf(ActionButtonType.SAVE), PreferencesManager.getToolbarProfiles(context, emptySet()).portrait.buttons)
        assertEquals(ButtonDisplayType.ICON_ONLY, PreferencesManager.getButtonDisplayType(context))
    }
    @Test fun savedProfilesRoundTripWithoutOverwritingEachOther() = isolated { context, _ ->
        val profiles = ToolbarProfiles(ToolbarSelection(null, emptySet()), ToolbarSelection(ToolbarPreset.COMPACT))
        PreferencesManager.setToolbarOptions(context, ButtonDisplayType.ICON_ONLY, profiles)
        assertEquals(profiles, PreferencesManager.getToolbarProfiles(context, emptySet()))
        assertEquals(ButtonDisplayType.ICON_ONLY, PreferencesManager.getButtonDisplayType(context))
    }
}
