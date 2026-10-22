package com.takumayamada22.pdfutility.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferencesManager {
    private const val PREF_NAME = "mihiraki_pdf_prefs"
    private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted(context: Context) {
        getPrefs(context).edit(commit = false) {
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
        }
    }
}
