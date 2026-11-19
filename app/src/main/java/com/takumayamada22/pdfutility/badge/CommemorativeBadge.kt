package com.takumayamada22.pdfutility.badge

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.takumayamada22.pdfutility.R
import java.time.Instant

data class CommemorativeBadge(
    val id: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val descriptionRes: Int,
    val startInstant: Instant,
    val endExclusive: Instant
) {
    fun canClaimAt(now: Instant): Boolean = !now.isBefore(startInstant) && now.isBefore(endExclusive)
}

object BadgeCatalog {
    val KOYOMI_BADGE = CommemorativeBadge(
        id = "koyomi_commemorative",
        titleRes = R.string.commemorative_badge_title,
        iconRes = R.drawable.tameshibu_badge_koyomi,
        descriptionRes = R.string.koyomi_badge_description,
        startInstant = Instant.parse("2026-10-31T15:00:00Z"), // 2026-11-01 00:00 JST
        endExclusive = Instant.ofEpochSecond(1830265200L) // 2028-01-01 00:00 JST
    )
    val allBadges: List<CommemorativeBadge> = listOf(KOYOMI_BADGE)
}

/** Manual-dismiss dialog only on the unobstructed start screen, never over reading content. */
internal fun canPresentBadgeNotification(resumed: Boolean, hasDocument: Boolean, busy: Boolean, modalOpen: Boolean): Boolean =
    resumed && !hasDocument && !busy && !modalOpen
