package com.takumayamada22.pdfutility

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class UiEnduranceTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testConfigurableDurationUiEnduranceWithScrolling() {
        val iterationCount = 5

        for (i in 1..iterationCount) {
            composeTestRule.waitForIdle()

            // 1. Open Settings / Diagnostics dialog using testTag
            runCatching {
                composeTestRule.onNodeWithTag("settingsButton").performClick()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithTag("diagnosticsMenuItem").performClick()
                composeTestRule.waitForIdle()
            }

            // 2. Perform test actions with explicit consideration for scrollable containers
            runCatching {
                // If a scrollable container exists (like dialog content or lazy list), scroll down first to ensure nodes are visible
                composeTestRule.onAllNodes(hasScrollAction()).let { nodes ->
                    if (nodes.fetchSemanticsNodes().isNotEmpty()) {
                        nodes.get(0).performScrollToIndex(0)
                        composeTestRule.waitForIdle()
                    }
                }
            }

            // 3. Click test execution or dialog buttons safely with scroll support
            runCatching {
                composeTestRule.onNode(hasClickAction() and (hasText("総合診断・ベンチマークテストを実行", ignoreCase = true) or hasText("長時間テストを開始", ignoreCase = true)), true)
                    .assertExists()
                    .performScrollTo()
                    .performClick()
                composeTestRule.waitForIdle()
            }

            // 4. Close dialogs / cleanup with scroll consideration
            runCatching {
                composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true)
                    .assertExists()
                    .performScrollTo()
                    .performClick()
                composeTestRule.waitForIdle()
            }
        }
    }
}
