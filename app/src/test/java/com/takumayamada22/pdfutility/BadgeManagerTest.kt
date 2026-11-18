package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.badge.BadgeCatalog
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class BadgeManagerTest {
    @Test
    fun testBadgeCatalogHasKoyomiBadge() {
        val koyomi = BadgeCatalog.KOYOMI_BADGE
        assertEquals("koyomi_commemorative", koyomi.id)
        assertNotNull(koyomi.startDateTime)
        assertNull(koyomi.endDateTime)
    }

    @Test
    fun testBadgeValidityPeriod() {
        val badge = BadgeCatalog.KOYOMI_BADGE
        val beforeStart = LocalDateTime.of(2026, 10, 31, 23, 59)
        val atStart = LocalDateTime.of(2026, 11, 1, 0, 0)
        val afterStart = LocalDateTime.of(2026, 11, 2, 12, 0)

        assertFalse(!beforeStart.isBefore(badge.startDateTime))
        assertTrue(!atStart.isBefore(badge.startDateTime))
        assertTrue(!afterStart.isBefore(badge.startDateTime))
    }
}
