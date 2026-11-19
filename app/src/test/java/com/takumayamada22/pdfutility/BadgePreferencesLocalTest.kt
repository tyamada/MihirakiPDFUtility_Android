package com.takumayamada22.pdfutility

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.takumayamada22.pdfutility.badge.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BadgePreferencesLocalTest {
    @Test fun realSharedPreferencesSurviveManagerRecreationAndKeepUnrelatedRights() {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences(BadgeManager.PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit().clear().putStringSet(SharedPreferencesBadgeStorage.KEY, setOf("existing_unknown")).commit()
        val billing = context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
        billing.edit().putString("unrelated_purchase_record", "preserve")
            .putStringSet("purchased_tiers", setOf("BRONZE", "SILVER")).commit()
        val badge = BadgeCatalog.KOYOMI_BADGE
        val manager = BadgeManager(SharedPreferencesBadgeStorage(prefs), Clock.fixed(badge.endExclusive.minusNanos(1), ZoneOffset.UTC))
        assertEquals(listOf(badge), manager.checkAndClaimBadges())
        val next = BadgeManager(SharedPreferencesBadgeStorage(context.getSharedPreferences(BadgeManager.PREFERENCES, Context.MODE_PRIVATE)),
            Clock.fixed(badge.endExclusive, ZoneOffset.UTC))
        assertEquals(setOf("existing_unknown", badge.id), next.acquiredBadgeIds.value)
        assertTrue(next.checkAndClaimBadges().isEmpty())
        assertEquals("preserve", billing.getString("unrelated_purchase_record", null))
        assertEquals(setOf("BRONZE", "SILVER"), billing.getStringSet("purchased_tiers", emptySet()))
    }

    @Test fun commitFailureRollsBackTheInMemoryMapAndDoesNotNotify() {
        var values = setOf("existing")
        var nextValues = values
        var failNextCommit = true
        val editor = java.lang.reflect.Proxy.newProxyInstance(SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java)) { proxy, method, args ->
            when (method.name) {
                "putStringSet" -> { @Suppress("UNCHECKED_CAST")
                    nextValues = (args!![1] as Set<String>).toSet(); proxy }
                "commit" -> { values = nextValues; if (failNextCommit) { failNextCommit = false; false } else true }
                else -> null
            }
        } as SharedPreferences.Editor
        val prefs = java.lang.reflect.Proxy.newProxyInstance(SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)) { _, method, _ ->
            when (method.name) { "getStringSet" -> values; "edit" -> editor; else -> null }
        } as SharedPreferences
        val owner = BadgeManager(SharedPreferencesBadgeStorage(prefs), Clock.fixed(BadgeCatalog.KOYOMI_BADGE.startInstant, ZoneOffset.UTC))
        assertTrue(owner.checkAndClaimBadges().isEmpty())
        assertEquals(setOf("existing"), values)
        assertEquals(values, owner.acquiredBadgeIds.value)
        assertEquals(1, owner.checkAndClaimBadges().size)
    }
}
