package com.takumayamada22.pdfutility.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.*

/** Button and fitting calculation share identical widths, density and font measurement. */
@Composable
internal fun toolbarButtonWidth(text: String, type: ButtonDisplayType): Int {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val textWidth = measurer.measure(AnnotatedString(text), style = MaterialTheme.typography.labelLarge, maxLines = 1).size.width
    return with(density) {
        when (type) {
            ButtonDisplayType.ICON_ONLY -> 48.dp.roundToPx()
            ButtonDisplayType.ICON_AND_TEXT -> maxOf(58.dp.roundToPx(), textWidth + 44.dp.roundToPx()) + 4.dp.roundToPx()
            ButtonDisplayType.TEXT_ONLY -> maxOf(58.dp.roundToPx(), textWidth + 24.dp.roundToPx())
        }
    }
}

@Composable
internal fun resolvedToolbarButtons(selection: ToolbarSelection, type: ButtonDisplayType, width: Int,
    desktop: Boolean, updateAvailable: Boolean): Set<ActionButtonType> {
    val preset = selection.preset ?: return selection.buttons
    val widths = toolbarActionOrder.map { toolbarButtonWidth(stringResource(it.titleResId), type) }
    val trailing = buildList {
        if (updateAvailable) add(toolbarButtonWidth(stringResource(R.string.update_app), type))
        if (desktop) add(toolbarButtonWidth(stringResource(R.string.action_settings), type))
    }
    return toolbarActionOrder.take(fittingToolbarButtons(widths, width, preset.rows, trailing)).toSet()
}
