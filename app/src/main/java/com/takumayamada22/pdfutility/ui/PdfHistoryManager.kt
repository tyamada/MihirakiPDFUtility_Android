package com.takumayamada22.pdfutility.ui

import android.net.Uri

data class PageSnapshot(
    val id: String,
    val originalIndex: Int,
    val rotation: Int = 0,
    val isBlank: Boolean = false,
    val sourceUri: Uri? = null,
    val isSplit: Boolean = false,
    val splitPart: Int = 0,
    val splitDirection: SplitDirection = SplitDirection.VERTICAL,
    val cropMargin: Float = 0f
)

data class HistoryState(
    val pageSnapshots: List<PageSnapshot>,
    val selectedIndices: Set<Int>
)

class PdfHistoryManager(private val maxHistorySize: Int = 20) {
    private val undoStack = mutableListOf<HistoryState>()
    private val redoStack = mutableListOf<HistoryState>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun saveState(snapshots: List<PageSnapshot>, selectedIndices: Set<Int>) {
        undoStack.add(HistoryState(snapshots, selectedIndices))
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
    }

    fun undo(currentState: HistoryState): HistoryState? {
        if (undoStack.isEmpty()) return null
        redoStack.add(currentState)
        if (redoStack.size > maxHistorySize) {
            redoStack.removeAt(0)
        }
        return undoStack.removeAt(undoStack.size - 1)
    }

    fun redo(currentState: HistoryState): HistoryState? {
        if (redoStack.isEmpty()) return null
        undoStack.add(currentState)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        return redoStack.removeAt(redoStack.size - 1)
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
