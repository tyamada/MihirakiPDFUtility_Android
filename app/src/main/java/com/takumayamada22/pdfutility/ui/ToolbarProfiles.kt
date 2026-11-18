package com.takumayamada22.pdfutility.ui

/** Null preset means an explicit selection, including an intentionally empty selection. */
enum class ToolbarPreset(val rows: Int, val displayType: ButtonDisplayType) {
    STANDARD(2, ButtonDisplayType.ICON_AND_TEXT),
    SIMPLE(1, ButtonDisplayType.ICON_AND_TEXT),
    COMPACT(2, ButtonDisplayType.ICON_ONLY)
}

data class ToolbarSelection(
    val preset: ToolbarPreset? = ToolbarPreset.STANDARD,
    val buttons: Set<ActionButtonType> = emptySet()
)

data class ToolbarProfiles(
    val portrait: ToolbarSelection = ToolbarSelection(),
    val landscape: ToolbarSelection = ToolbarSelection()
) {
    fun forOrientation(landscape: Boolean) = if (landscape) this.landscape else portrait
    fun withSelection(landscape: Boolean, selection: ToolbarSelection) =
        if (landscape) copy(landscape = selection) else copy(portrait = selection)
}

/** Matches the existing visual order, which is different from enum declaration order. */
val toolbarActionOrder = listOf(
    ActionButtonType.OPEN, ActionButtonType.OPEN_FOLDER, ActionButtonType.SAMPLE_PDF,
    ActionButtonType.SAVE, ActionButtonType.EXPORT, ActionButtonType.UNDO, ActionButtonType.REDO,
    ActionButtonType.SELECT_ALL, ActionButtonType.CLEAR_SELECTION, ActionButtonType.INVERT_SELECTION,
    ActionButtonType.DELETE, ActionButtonType.SPLIT, ActionButtonType.CROP,
    ActionButtonType.MOVE_UP, ActionButtonType.MOVE_DOWN, ActionButtonType.ROTATE_LEFT,
    ActionButtonType.ROTATE_RIGHT, ActionButtonType.APPEND, ActionButtonType.INSERT_BLANK,
    ActionButtonType.TOGGLE_VIEW, ActionButtonType.PROPERTY, ActionButtonType.RIGHT_BINDING,
    ActionButtonType.RIGHT_BINDING_COVER, ActionButtonType.TOGGLE_RTL
)

/** Largest leading prefix that fits, reserving trailing mandatory controls in the same rows. */
fun fittingToolbarButtons(widths: List<Int>, availableWidth: Int, rows: Int, trailingWidths: List<Int> = emptyList()): Int {
    if (availableWidth <= 0 || rows <= 0) return 0
    fun fits(items: List<Int>): Boolean {
        var line = 1
        var used = 0
        for (width in items) {
            if (width > availableWidth || width <= 0) return false
            if (used + width > availableWidth) { line++; used = 0 }
            if (line > rows) return false
            used += width
        }
        return true
    }
    return (widths.size downTo 0).firstOrNull { fits(widths.take(it) + trailingWidths) } ?: 0
}
