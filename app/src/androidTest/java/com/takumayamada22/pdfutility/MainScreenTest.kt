package com.takumayamada22.pdfutility

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.MainScreen
import com.takumayamada22.pdfutility.ui.PdfViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMainScreenInitialState() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = PdfViewModel(app)
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("PDFを開く").assertExists()
    }
}
