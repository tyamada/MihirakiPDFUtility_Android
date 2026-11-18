package com.takumayamada22.pdfutility.badge

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class BadgeManager(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("commemorative_badge_prefs", Context.MODE_PRIVATE)

    private val _acquiredBadgeIds = MutableStateFlow(loadAcquiredBadgeIds())
    val acquiredBadgeIds = _acquiredBadgeIds.asStateFlow()

    private fun loadAcquiredBadgeIds(): Set<String> {
        return prefs.getStringSet("acquired_badge_ids", emptySet()) ?: emptySet()
    }

    private fun saveAcquiredBadgeIds(ids: Set<String>) {
        prefs.edit { putStringSet("acquired_badge_ids", ids) }
    }

    fun checkAndClaimBadges(): List<CommemorativeBadge> {
        val now = LocalDateTime.now()
        val currentAcquired = _acquiredBadgeIds.value.toMutableSet()
        val newlyClaimed = mutableListOf<CommemorativeBadge>()

        for (badge in BadgeCatalog.allBadges) {
            if (!currentAcquired.contains(badge.id)) {
                val isAfterStart = !now.isBefore(badge.startDateTime)
                val isBeforeEnd = badge.endDateTime == null || !now.isAfter(badge.endDateTime)

                if (isAfterStart && isBeforeEnd) {
                    currentAcquired.add(badge.id)
                    newlyClaimed.add(badge)
                }
            }
        }

        if (newlyClaimed.isNotEmpty()) {
            _acquiredBadgeIds.value = currentAcquired
            saveAcquiredBadgeIds(currentAcquired)
        }
        return newlyClaimed
    }
}
