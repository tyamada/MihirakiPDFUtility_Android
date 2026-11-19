package com.takumayamada22.pdfutility

import android.app.Application
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import com.takumayamada22.pdfutility.badge.*
import com.takumayamada22.pdfutility.ui.components.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Clock
import java.time.ZoneOffset

/** JVM-only Android/Compose simulation. No device/emulator installation or TalkBack service. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w320dp-h480dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BadgeDialogLocalTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test @Config(qualifiers = "ja-w320dp-h480dp")
    fun japanesePortrait() = detailsCase(1f)
    @Test @Config(qualifiers = "ja-w320dp-h480dp")
    fun japanesePortraitLargeText() = detailsCase(2f)
    @Test @Config(qualifiers = "ja-w640dp-h280dp-land")
    fun japaneseLandscape() = detailsCase(1f)
    @Test @Config(qualifiers = "ja-w640dp-h280dp-land")
    fun japaneseLandscapeLargeText() = detailsCase(2f)
    @Test @Config(qualifiers = "en-rUS-w320dp-h480dp")
    fun englishPortrait() = detailsCase(1f)
    @Test @Config(qualifiers = "en-rUS-w320dp-h480dp")
    fun englishPortraitLargeText() = detailsCase(2f)
    @Test @Config(qualifiers = "en-rUS-w640dp-h280dp-land")
    fun englishLandscape() = detailsCase(1f)
    @Test @Config(qualifiers = "en-rUS-w640dp-h280dp-land")
    fun englishLandscapeLargeText() = detailsCase(2f)

    private fun detailsCase(scale: Float) {
        RuntimeEnvironment.setFontScale(scale)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val opened = mutableStateOf(false)
        val badge = BadgeCatalog.KOYOMI_BADGE
        compose.setContent {
            MaterialTheme {
                BadgeDetailsButton(badge) { opened.value = true }
                if (opened.value) BadgeDetailsDialog(badge) { opened.value = false }
            }
        }
        val context = compose.activity
        val openLabel = context.getString(R.string.badge_open_details, context.getString(badge.titleRes))
        compose.onNodeWithText(context.getString(badge.titleRes))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher("localized open action") { it.config[SemanticsActions.OnClick].label == openLabel })
            .performClick()
        val description = compose.onNodeWithText(context.getString(badge.descriptionRes))
        description.performScrollTo().assertIsDisplayed()
        val scroll = compose.onNode(hasScrollAction()).fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange]
        assertTrue("Full details must remain scrollable", scroll.maxValue() > 0f)
        for (node in listOf(description, compose.onNode(hasText(context.getString(badge.titleRes)) and hasAnyAncestor(isDialog())))) {
            node.performScrollTo().assertIsDisplayed()
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue(layouts.isNotEmpty())
            layouts.forEach { layout ->
                assertEquals(scale, layout.layoutInput.density.fontScale, 0.01f)
                assertFalse(layout.didOverflowHeight)
                for (line in 0 until layout.lineCount) {
                    assertFalse(layout.isLineEllipsized(line))
                    assertTrue(layout.getLineRight(line) <= layout.size.width + 1f)
                }
            }
        }
        val semanticTextOrder = compose.onAllNodes(hasAnyAncestor(isDialog()), useUnmergedTree = true)
            .fetchSemanticsNodes().filter { it.config.contains(SemanticsProperties.Text) }
            .map { node -> node.config[SemanticsProperties.Text].joinToString("") { it.text } }
        assertTrue(semanticTextOrder.indexOf(context.getString(badge.titleRes)) < semanticTextOrder.indexOf(context.getString(badge.descriptionRes)))
        assertTrue(semanticTextOrder.indexOf(context.getString(badge.descriptionRes)) < semanticTextOrder.indexOf(context.getString(R.string.action_close)))
        compose.onNodeWithText(context.getString(R.string.action_close))
            .assertIsDisplayed().assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).performClick()
        compose.runOnIdle { assertFalse(opened.value) }
    }

    @Test fun acquisitionNotificationDoesNotTimeOutAndClosingKeepsSavedBadge() {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences(BadgeManager.PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val badge = BadgeCatalog.KOYOMI_BADGE
        val owner = BadgeManager(SharedPreferencesBadgeStorage(prefs), Clock.fixed(badge.startInstant, ZoneOffset.UTC))
        assertEquals(listOf(badge), owner.checkAndClaimBadges())
        val visible = mutableStateOf(true)
        compose.setContent { MaterialTheme { if (visible.value) BadgeAcquiredDialog(badge) { visible.value = false } } }
        compose.mainClock.advanceTimeBy(10000)
        compose.onNodeWithText(context.getString(R.string.badge_acquired_heading)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.action_close)).performClick()
        assertEquals(setOf(badge.id), owner.acquiredBadgeIds.value)
        val recreated = BadgeManager(SharedPreferencesBadgeStorage(prefs), Clock.fixed(badge.endExclusive, ZoneOffset.UTC))
        assertEquals(setOf(badge.id), recreated.acquiredBadgeIds.value)
        assertTrue(recreated.checkAndClaimBadges().isEmpty())
    }
}
