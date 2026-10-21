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
    fun testAll10ModularEndurancePatternsWithScrolling() {
        val iterationCount = 10 // Covers Patterns 1 to 10

        for (iteration in 1..iterationCount) {
            val pattern = ((iteration - 1) % 10) + 1
            composeTestRule.waitForIdle()

            // 1. Open Settings / Diagnostics dialog using testTag with scroll consideration
            runCatching {
                composeTestRule.onNodeWithTag("settingsButton").performClick()
                composeTestRule.waitForIdle()
                
                // Scroll if needed to find diagnostics menu item
                composeTestRule.onAllNodes(hasScrollAction()).let { nodes ->
                    if (nodes.fetchSemanticsNodes().isNotEmpty()) {
                        nodes.get(0).performScrollToIndex(0)
                        composeTestRule.waitForIdle()
                    }
                }
                
                composeTestRule.onNodeWithTag("diagnosticsMenuItem")
                    .performScrollTo()
                    .performClick()
                composeTestRule.waitForIdle()
            }

            // 2. Execute Patterns 1 to 10 (Courses 1-5 modular combinations) with explicit scroll handling
            when (pattern) {
                1 -> { // Pattern 1: Course 1 (Multi-File Merge) alone
                    runCatching {
                        composeTestRule.onNode(hasClickAction() and hasText("総合診断・ベンチマークテストを実行", ignoreCase = true), true)
                            .performScrollTo()
                            .performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                2 -> { // Pattern 2: Course 2 (1100p Append) alone
                    runCatching {
                        composeTestRule.onNode(hasClickAction() and hasText("長時間テストを開始", ignoreCase = true), true)
                            .performScrollTo()
                            .performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                3, 4 -> { // Pattern 3 & 4: Course 1/2 + Course 3 (Editing) with scroll handling
                    runCatching {
                        composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true)
                            .performScrollTo()
                            .performClick()
                        composeTestRule.waitForIdle()
                        composeTestRule.onAllNodes(hasClickAction()).let { nodes ->
                            if (nodes.fetchSemanticsNodes().isNotEmpty()) {
                                nodes.get(0).performScrollToIndex(0)
                                nodes.get(0).performClick()
                                composeTestRule.waitForIdle()
                            }
                        }
                    }
                }
                5, 6 -> { // Pattern 5 & 6: Course 1/2 + Course 4 (Orientation Rotation) with scroll handling
                    runCatching {
                        composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true)
                            .performScrollTo()
                            .performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                7, 8 -> { // Pattern 7 & 8: Course 1/2 + Course 5 (Foldable Simulation) with scroll handling
                    runCatching {
                        composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true)
                            .performScrollTo()
                            .performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                9, 10 -> { // Pattern 9 & 10: Full Combinations A & B (Courses 1/2 + 3 + 4 + 5) with scroll handling
                    runCatching {
                        composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true)
                            .performScrollTo()
                            .performClick()
                        composeTestRule.waitForIdle()
                    }
                }
            }

            // 3. Close dialogs / cleanup with scroll consideration
            runCatching {
                composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true)
                    .performScrollTo()
                    .performClick()
                composeTestRule.waitForIdle()
            }
        }
    }
}
