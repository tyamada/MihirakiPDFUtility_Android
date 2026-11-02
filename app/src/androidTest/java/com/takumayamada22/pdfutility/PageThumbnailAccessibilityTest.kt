package com.takumayamada22.pdfutility

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.PageState
import com.takumayamada22.pdfutility.ui.components.PageThumbnail
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageThumbnailAccessibilityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun accessibleSelectionAndPreviewActionsWork() {
        var selected by mutableStateOf(false)
        var previewed = false
        compose.runOnUiThread { compose.activity.setContent {
            MaterialTheme {
                PageThumbnail(PageState(originalIndex = 0, thumbnail = null), 1, selected, false,
                    onClick = { selected = !selected }, onDoubleTap = { previewed = true },
                    modifier = Modifier.size(120.dp, 180.dp))
            }
        } }
        val node = compose.onNodeWithContentDescription(compose.activity.getString(R.string.page_number_description, 1))
        node.assertIsNotSelected().assertHasClickAction()
        node.performSemanticsAction(SemanticsActions.OnClick) { assertTrue(it()) }
        node.assertIsSelected()
        val actions = node.fetchSemanticsNode().config[SemanticsActions.CustomActions]
        compose.runOnIdle {
            assertEquals(compose.activity.getString(R.string.page_preview_action), actions.single().label)
            assertTrue(actions.single().action())
        }
        assertTrue(previewed)
        node.performSemanticsAction(SemanticsActions.OnClick) { assertTrue(it()) }
        node.assertIsNotSelected()
    }
}
