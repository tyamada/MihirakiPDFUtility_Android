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
    fun testUiEmulatedEnduranceLoop() {
        // Emulate user interactions on the UI for multiple iterations to test UI rendering pipeline and system load
        val iterationCount = 5

        for (i in 1..iterationCount) {
            // 1. Open Sample PDF catalog or Settings dialog via UI click
            composeTestRule.waitForIdle()

            // Click settings or diagnostics menu if visible, or sample PDF button
            runCatching {
                composeTestRule.onNodeWithContentDescription("Settings", ignoreCase = true).performClick()
                composeTestRule.waitForIdle()
                
                // Click Diagnostics in settings
                composeTestRule.onNodeWithText("診断", ignoreCase = true).performClick()
                composeTestRule.waitForIdle()

                // Close dialog
                composeTestRule.onNodeWithText("閉じる", ignoreCase = true).performClick()
                composeTestRule.waitForIdle()
            }

            // 2. Test view mode toggle via UI
            runCatching {
                composeTestRule.onNodeWithContentDescription("Mihiraki", ignoreCase = true).performClick()
                composeTestRule.waitForIdle()
            }

            // 3. Test selection & grid interaction
            runCatching {
                composeTestRule.onAllNodes(hasClickAction()).get(0).performClick()
                composeTestRule.waitForIdle()
            }
        }
    }
}
