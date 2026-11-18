package com.takumayamada22.pdfutility.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.takumayamada22.pdfutility.data.SamplePdfSortOrder
import com.takumayamada22.pdfutility.ui.ButtonDisplayType
import com.takumayamada22.pdfutility.ui.ToolbarProfiles
import com.takumayamada22.pdfutility.ui.ToolbarSelection
import com.takumayamada22.pdfutility.ui.ToolbarPreset
import com.takumayamada22.pdfutility.ui.ActionButtonType

object PreferencesManager {
    private const val PREF_NAME = "mihiraki_pdf_prefs"
    private const val KEY_DONT_SHOW_TUTORIAL = "dont_show_tutorial"
    private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    private const val KEY_SAMPLE_PDF_SORT_ORDER = "sample_pdf_sort_order"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun shouldShowTutorial(context: Context): Boolean {
        val prefs = getPrefs(context)
        return !prefs.getBoolean(KEY_DONT_SHOW_TUTORIAL, false)
    }

    fun setTutorialDismissed(context: Context, dontShowAgain: Boolean) {
        getPrefs(context).edit(commit = false) {
            putBoolean(KEY_DONT_SHOW_TUTORIAL, dontShowAgain)
            if (dontShowAgain) {
                putBoolean(KEY_IS_FIRST_LAUNCH, false)
            }
        }
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted(context: Context) {
        setTutorialDismissed(context, true)
    }

    fun getSamplePdfSortOrder(context: Context): SamplePdfSortOrder {
        val value = getPrefs(context).getString(KEY_SAMPLE_PDF_SORT_ORDER, SamplePdfSortOrder.DESCENDING.name)
        return try {
            SamplePdfSortOrder.valueOf(value ?: SamplePdfSortOrder.DESCENDING.name)
        } catch (e: Exception) {
            SamplePdfSortOrder.DESCENDING
        }
    }

    fun setSamplePdfSortOrder(context: Context, order: SamplePdfSortOrder) {
        getPrefs(context).edit(commit = false) {
            putString(KEY_SAMPLE_PDF_SORT_ORDER, order.name)
        }
    }
    fun getButtonDisplayType(context: Context): ButtonDisplayType =
        enumValues<ButtonDisplayType>().firstOrNull {
            it.name == getPrefs(context).getString("button_display_type", null)
        } ?: if (getPrefs(context).contains("visible_action_buttons")) ButtonDisplayType.ICON_ONLY else ButtonDisplayType.ICON_AND_TEXT

    fun setButtonDisplayType(context: Context, type: ButtonDisplayType) {
        getPrefs(context).edit { putString("button_display_type", type.name) }
    }

    fun getVisibleActionButtons(context: Context, defaults: Set<ActionButtonType>): Set<ActionButtonType> {
        val names = getPrefs(context).getStringSet("visible_action_buttons", null) ?: return defaults
        // An explicitly empty selection is valid; ignore obsolete/unknown enum names.
        return enumValues<ActionButtonType>().filter { it.name in names }.toSet()
    }

    fun setVisibleActionButtons(context: Context, buttons: Set<ActionButtonType>) {
        getPrefs(context).edit {
            putStringSet("visible_action_buttons", buttons.map { it.name }.toSet())
            remove("toolbar_portrait_preset"); remove("toolbar_landscape_preset")
            putStringSet("toolbar_portrait_buttons", buttons.map { it.name }.toSet())
            putStringSet("toolbar_landscape_buttons", buttons.map { it.name }.toSet())
        }
    }

    fun getToolbarProfiles(context: Context, defaults: Set<ActionButtonType>): ToolbarProfiles {
        val prefs = getPrefs(context)
        val legacy = getVisibleActionButtons(context, defaults)
        val existing = prefs.contains("visible_action_buttons") || prefs.contains("button_display_type")
        fun read(key: String): ToolbarSelection {
            val names = prefs.getStringSet("${key}_buttons", null)
            val preset = prefs.getString("${key}_preset", null)
            if (names == null && preset == null) return if (existing) ToolbarSelection(null, legacy) else ToolbarSelection()
            return ToolbarSelection(ToolbarPreset.entries.firstOrNull { it.name == preset },
                ActionButtonType.entries.filter { it.name in (names ?: emptySet()) }.toSet())
        }
        return ToolbarProfiles(read("toolbar_portrait"), read("toolbar_landscape"))
    }

    fun setToolbarOptions(context: Context, type: ButtonDisplayType, profiles: ToolbarProfiles) {
        getPrefs(context).edit {
            putString("button_display_type", type.name)
            for ((key, selection) in listOf("toolbar_portrait" to profiles.portrait, "toolbar_landscape" to profiles.landscape)) {
                putString("${key}_preset", selection.preset?.name ?: "CUSTOM")
                putStringSet("${key}_buttons", selection.buttons.map { it.name }.toSet())
            }
        }
    }
}
