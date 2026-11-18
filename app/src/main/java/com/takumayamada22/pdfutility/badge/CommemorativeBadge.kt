package com.takumayamada22.pdfutility.badge

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.takumayamada22.pdfutility.R
import java.time.LocalDateTime

data class CommemorativeBadge(
    val id: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime? = null
)

object BadgeCatalog {
    val KOYOMI_BADGE = CommemorativeBadge(
        id = "koyomi_commemorative",
        titleRes = R.string.commemorative_badge_title,
        iconRes = R.drawable.tameshibu_badge_koyomi,
        startDateTime = LocalDateTime.of(2026, 11, 1, 0, 0),
        endDateTime = null
    )

    val allBadges: List<CommemorativeBadge> = listOf(
        KOYOMI_BADGE
        // Future commemorative badges can be added here easily
    )
}
