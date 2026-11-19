package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.badge.*
import com.takumayamada22.pdfutility.billing.TipTier
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class BadgeManagerTest {
    private class Store(var ids: Set<String> = emptySet()) : BadgeStorage {
        var writes = 0
        var fail = false
        override fun readIds() = ids.toSet()
        override fun saveIds(ids: Set<String>): Boolean {
            writes++
            if (fail) return false
            this.ids = ids.toSet()
            return true
        }
    }
    private val badge = BadgeCatalog.KOYOMI_BADGE
    private fun manager(store: Store, at: Instant, zone: String = "UTC") = BadgeManager(store, Clock.fixed(at, ZoneId.of(zone)))

    @Test fun absoluteWindowIncludesStartAndExcludesExactDeadline() {
        assertEquals("2026-10-31T15:00:00Z", badge.startInstant.toString())
        assertEquals("2027-12-31T15:00:00Z", badge.endExclusive.toString())
        assertEquals(1830265200L, badge.endExclusive.epochSecond)
        assertFalse(badge.canClaimAt(badge.startInstant.minusNanos(1)))
        assertTrue(badge.canClaimAt(badge.startInstant))
        assertTrue(badge.canClaimAt(badge.endExclusive.minusNanos(1)))
        assertFalse(badge.canClaimAt(badge.endExclusive))
        assertFalse(badge.canClaimAt(badge.endExclusive.plusNanos(1)))
    }

    @Test fun clockZoneCannotShiftDeadline() {
        for (zone in listOf("UTC", "Asia/Tokyo", "America/Los_Angeles", "Pacific/Kiritimati")) {
            assertEquals(1, manager(Store(), badge.endExclusive.minusNanos(1), zone).checkAndClaimBadges().size)
            assertTrue(manager(Store(), badge.endExclusive, zone).checkAndClaimBadges().isEmpty())
        }
    }

    @Test fun newOrExistingUserGetsOneFreeOfflineGrantWithoutStoreMetadata() {
        for (existing in listOf(emptySet(), setOf("future_existing_badge"))) {
            val store = Store(existing)
            val owner = manager(store, badge.startInstant)
            assertEquals(listOf(badge), owner.checkAndClaimBadges())
            assertEquals(existing + badge.id, store.ids)
            assertEquals(store.ids, owner.acquiredBadgeIds.value)
            assertTrue(owner.checkAndClaimBadges().isEmpty())
            assertEquals(1, store.writes)
        }
    }

    @Test fun recreationAndExpiryKeepSavedIdsWithoutRenotification() {
        val first = Store(setOf("unknown_existing_id"))
        manager(first, badge.endExclusive.minusNanos(1)).checkAndClaimBadges()
        val recreatedStorage = Store(first.ids.toSet())
        val recreated = manager(recreatedStorage, badge.endExclusive.plusSeconds(10))
        assertTrue(recreated.checkAndClaimBadges().isEmpty())
        assertEquals(setOf("unknown_existing_id", badge.id), recreated.acquiredBadgeIds.value)
        assertEquals(0, recreatedStorage.writes)
    }

    @Test fun unsavedBadgeIsNotGrantedAtOrAfterDeadlineOrBeforeStart() {
        for (at in listOf(badge.startInstant.minusNanos(1), badge.endExclusive, badge.endExclusive.plusSeconds(1))) {
            val store = Store()
            assertTrue(manager(store, at).checkAndClaimBadges().isEmpty())
            assertTrue(store.ids.isEmpty())
            assertEquals(0, store.writes)
        }
    }

    @Test fun failedSaveNeverGrantsOrNotifiesAndCanRetryOnlyWhileEligible() {
        val store = Store(setOf("existing")); store.fail = true
        val owner = manager(store, badge.endExclusive.minusNanos(1))
        assertTrue(owner.checkAndClaimBadges().isEmpty())
        assertEquals(setOf("existing"), owner.acquiredBadgeIds.value)
        store.fail = false
        assertEquals(1, owner.checkAndClaimBadges().size)
        val failed = Store().also { it.fail = true }
        manager(failed, badge.endExclusive.minusNanos(1)).checkAndClaimBadges()
        failed.fail = false
        assertTrue(manager(failed, badge.endExclusive).checkAndClaimBadges().isEmpty())
        assertTrue(failed.ids.isEmpty())
    }

    @Test fun multipleManagersRereadSavedRightsInsteadOfDuplicatingGrant() {
        val store = Store()
        val first = manager(store, badge.startInstant)
        val second = manager(store, badge.startInstant)
        assertEquals(1, first.checkAndClaimBadges().size)
        assertTrue(second.checkAndClaimBadges().isEmpty())
        assertEquals(setOf(badge.id), second.acquiredBadgeIds.value)
        assertEquals(1, store.writes)
    }

    @Test fun freeBadgeNeverBecomesAPurchaseProductAndStorageKeysRemainStable() {
        assertEquals("koyomi_commemorative", badge.id)
        assertEquals("commemorative_badge_prefs", BadgeManager.PREFERENCES)
        assertEquals("acquired_badge_ids", SharedPreferencesBadgeStorage.KEY)
        assertEquals(setOf("tip_100", "tip_500", "tip_1000"), TipTier.entries.map { it.productId }.toSet())
    }

    @Test fun notificationWaitsForUnobstructedResumedHome() {
        for (resumed in listOf(false, true)) for (document in listOf(false, true))
            for (busy in listOf(false, true)) for (modal in listOf(false, true))
                assertEquals(resumed && !document && !busy && !modal,
                    canPresentBadgeNotification(resumed, document, busy, modal))
    }
}
