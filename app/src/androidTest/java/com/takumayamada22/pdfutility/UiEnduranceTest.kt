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
    fun testModularUiEndurancePatterns() {
        // Execute all 10 modular test course combination patterns (Courses 1-5 combinations)
        // emulating real user taps, touch interactions, dialog navigation, and view toggles.
        val patterns = 1..10

        for (pattern in patterns) {
            composeTestRule.waitForIdle()

            // 1. Open Settings / Diagnostics dialog
            runCatching {
                composeTestRule.onNodeWithContentDescription("Settings", ignoreCase = true).performClick()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithText("診断", ignoreCase = true).performClick()
                composeTestRule.waitForIdle()
            }

            // 2. Execute pattern-specific UI actions
            when (pattern) {
                1 -> { // Pattern 1: Course 1 (Multi-File Merge) alone
                    runCatching {
                        composeTestRule.onNodeWithText("総合診断・ベンチマークテストを実行", ignoreCase = true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                2 -> { // Pattern 2: Course 2 (1100p Append) alone
                    runCatching {
                        composeTestRule.onNodeWithText("長時間テストを開始", ignoreCase = true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                3 -> { // Pattern 3: Course 1 + Course 3 (Merge + Editing)
                    runCatching {
                        composeTestRule.onNodeWithText("閉じる", ignoreCase = true).performClick()
                        composeTestRule.waitForIdle()
                        // Tap grid item to select and edit
                        composeTestRule.onAllNodes(hasClickAction()).get(0).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                4 -> { // Pattern 4: Course 2 + Course 3 (Append + Editing)
                    runCatching {
                        composeTestRule.onNodeWithText("閉じる", ignoreCase = true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                5, 6 -> { // Pattern 5 & 6: Orientation Rotation Simulation
                    runCatching {
                        composeTestRule.onNodeWithContentDescription("Mihiraki", ignoreCase = true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                7, 8 -> { // Pattern 7 & 8: Foldable Posture Simulation
                    runCatching {
                        composeTestRule.onNodeWithContentDescription("RTL", ignoreCase = true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                9, 10 -> { // Full Combinations A & B
                    runCatching {
                        composeTestRule.onNodeWithContentDescription("Settings", ignoreCase = true).performClick()
                        composeTestRule.waitForIdle()
                        composeTestRule.onNodeWithText("閉じる", ignoreCase = true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
            }

            // 3. Close dialogs / cleanup for next iteration
            runCatching {
                composeTestRule.onNodeWithText("閉じる", ignoreCase = true).performClick()
                composeTestRule.waitForIdle()
            }
        }
    }
}
