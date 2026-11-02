package com.takumayamada22.pdfutility

import android.app.Application
import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.components.PdfSupportDialog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupportDialogAccessibilityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun largeFontAndShortWindowCanReachLastItemAndClose() {
        val activity = compose.activity
        val app = activity.application as Application
        val vm = ViewModelProvider(activity, PdfViewModel.provideFactory(app))[PdfViewModel::class.java]
        val config = Configuration(activity.resources.configuration).apply { screenHeightDp = 320 }
        compose.runOnUiThread {
            activity.setContent {
                CompositionLocalProvider(
                    LocalConfiguration provides config,
                    LocalDensity provides Density(activity.resources.displayMetrics.density, 2f)
                ) { MaterialTheme { PdfSupportDialog(vm) } }
            }
        }
        compose.onNodeWithText(activity.getString(R.string.commemorative_badge_title))
            .performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText(activity.getString(R.string.action_purchase)).assertCountEquals(4)
        compose.onNodeWithText(activity.getString(R.string.action_close))
            .performScrollTo().assertIsDisplayed().performClick()
    }
}
