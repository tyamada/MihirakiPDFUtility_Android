package com.takumayamada22.pdfutility

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType
import com.takumayamada22.pdfutility.ui.components.PdfSettingsDialog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfSettingsDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testVersionDialogDisplay() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = PdfViewModel(app)
        viewModel.openSettingsDialog(SettingsDialogType.VERSION)
        val uiState = viewModel.uiState.value

        composeTestRule.setContent {
            PdfSettingsDialog(viewModel = viewModel, uiState = uiState)
        }

        composeTestRule.onNodeWithText("バージョン").assertExists()
    }
}
