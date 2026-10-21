package com.takumayamada22.pdfutility

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
class UiEnduranceTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testConfigurableDurationUiEndurance() {
        val durationFile = File("/sdcard/Download/test_duration.txt")
        val durationMinutes = if (durationFile.exists()) {
            durationFile.readText().trim().toIntOrNull() ?: 3
        } else {
            3
        }

        val targetMillis = durationMinutes * 60 * 1000L
        val startTime = System.currentTimeMillis()
        var iteration = 0

        println("Starting UI-Emulated Endurance Test for $durationMinutes minutes...")

        while (System.currentTimeMillis() - startTime < targetMillis) {
            iteration++
            val pattern = ((iteration - 1) % 10) + 1
            composeTestRule.waitForIdle()

            // 1. Open Settings / Diagnostics dialog using language-independent testTags
            runCatching {
                composeTestRule.onNodeWithTag("settingsButton").performClick()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithTag("diagnosticsMenuItem").performClick()
                composeTestRule.waitForIdle()
            }

            // 2. Execute pattern-specific UI actions (Courses 1-5 modular combinations)
            when (pattern) {
                1 -> { // Pattern 1: Course 1 (Multi-File Merge) alone
                    runCatching {
                        composeTestRule.onNode(hasClickAction() and hasText("総合診断・ベンチマークテストを実行", ignoreCase = true), true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                2 -> { // Pattern 2: Course 2 (1100p Append) alone
                    runCatching {
                        composeTestRule.onNode(hasClickAction() and hasText("長時間テストを開始", ignoreCase = true), true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                3, 4 -> { // Pattern 3 & 4: Editing Stress
                    runCatching {
                        composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true).performClick()
                        composeTestRule.waitForIdle()
                        composeTestRule.onAllNodes(hasClickAction()).get(0).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
                5, 6, 7, 8, 9, 10 -> { // Other patterns
                    runCatching {
                        composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true).performClick()
                        composeTestRule.waitForIdle()
                    }
                }
            }

            // 3. Close dialogs / cleanup for next iteration
            runCatching {
                composeTestRule.onNode(hasText("閉じる", ignoreCase = true), true).performClick()
                composeTestRule.waitForIdle()
            }
        }
    }
}
