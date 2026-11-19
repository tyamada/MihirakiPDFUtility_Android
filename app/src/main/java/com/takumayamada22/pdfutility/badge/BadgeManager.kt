package com.takumayamada22.pdfutility.badge

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock

internal interface BadgeStorage {
    val lock: Any get() = this
    fun readIds(): Set<String>
    /** False must leave the previous IDs intact, including the process's in-memory view. */
    fun saveIds(ids: Set<String>): Boolean
}

internal class SharedPreferencesBadgeStorage(private val prefs: SharedPreferences) : BadgeStorage {
    override val lock: Any get() = prefs
    override fun readIds(): Set<String> = prefs.getStringSet(KEY, emptySet()).orEmpty().toSet()
    // Both the grant and rollback must finish synchronously; KTX edit does not return commit success.
    @SuppressLint("ApplySharedPref", "UseKtx")
    override fun saveIds(ids: Set<String>): Boolean {
        val previous = readIds()
        return try {
            if (prefs.edit().putStringSet(KEY, ids.toSet()).commit()) true
            else { prefs.edit().putStringSet(KEY, previous).commit(); false }
        } catch (_: RuntimeException) {
            // commit(false) may already change SharedPreferences' in-memory map. Roll it back.
            try { prefs.edit().putStringSet(KEY, previous).commit() } catch (_: RuntimeException) { }
            false
        }
    }
    companion object { const val KEY = "acquired_badge_ids" }
}

/** Free grants have their own existing store; no store account or billing transaction is used. */
class BadgeManager internal constructor(private val storage: BadgeStorage, private val clock: Clock) {
    constructor(context: Context) : this(SharedPreferencesBadgeStorage(
        context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)), Clock.systemUTC())

    private val _acquiredBadgeIds = MutableStateFlow(storage.readIds().toSet())
    val acquiredBadgeIds = _acquiredBadgeIds.asStateFlow()

    /** Call on IO. Only a newly and synchronously persisted grant may request a notification. */
    fun checkAndClaimBadges(): List<CommemorativeBadge> = synchronized(storage.lock) {
        val current = storage.readIds().toSet()
        val now = clock.instant()
        val newlyClaimed = BadgeCatalog.allBadges.filter { it.id !in current && it.canClaimAt(now) }
        if (newlyClaimed.isEmpty()) {
            _acquiredBadgeIds.value = current
            emptyList()
        } else {
            val updated = current + newlyClaimed.map { it.id }
            if (storage.saveIds(updated)) {
                _acquiredBadgeIds.value = updated
                newlyClaimed
            } else {
                _acquiredBadgeIds.value = current
                emptyList()
            }
        }
    }

    companion object { const val PREFERENCES = "commemorative_badge_prefs" }
}
