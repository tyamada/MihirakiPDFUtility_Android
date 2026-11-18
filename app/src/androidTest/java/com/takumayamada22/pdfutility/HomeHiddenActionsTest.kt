package com.takumayamada22.pdfutility
import android.app.Application
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.*
import com.takumayamada22.pdfutility.ui.components.PdfTopAppBar
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class HomeHiddenActionsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun hidingAllButtonsAndLabelsStillKeepsAppendInMenu() {
        val vm = PdfViewModel(compose.activity.application)
        val state = mutableStateOf(PdfUiState().copy(visibleActionButtons = emptySet(), buttonDisplayType = ButtonDisplayType.ICON_ONLY))
        var appended = 0
        compose.activity.runOnUiThread { compose.activity.setContent {
            MaterialTheme { Box(Modifier.width(320.dp)) {
                PdfTopAppBar(state.value, vm, {}, {}, {}, { appended++ }, {}, {}, {}, {}, {})
            } }
        } }
        val menu = compose.activity.getString(R.string.start_more_actions)
        val append = compose.activity.getString(R.string.action_add_pdf)
        compose.onNodeWithContentDescription(menu).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.action_save)).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText(append).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, appended) }
        compose.runOnIdle { state.value = state.value.copy(pages = listOf(PageState(thumbnail = null, originalIndex = 0))) }
        compose.onNodeWithContentDescription(menu).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.action_save)).performScrollTo().assertIsEnabled()
    }
}
