package com.takumayamada22.pdfutility.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.billing.BillingManager
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.domain.ThumbnailProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class SplitDirection {
    VERTICAL, HORIZONTAL
}

data class PageState(
    val id: String = UUID.randomUUID().toString(),
    val thumbnail: Bitmap?,
    val rotation: Int = 0,
    val originalIndex: Int,
    val isBlank: Boolean = false,
    val sourceUri: Uri? = null,
    val isSplit: Boolean = false,
    val splitPart: Int = 0, // 0: Left/Top, 1: Right/Bottom
    val splitDirection: SplitDirection = SplitDirection.VERTICAL,
    val cropMargin: Float = 0f // 0.0 to 0.45 (percent from each side)
)

enum class SettingsDialogType {
    PROPERTY, PASSWORD, VERSION, SUPPORT, HELP
}

data class PdfUiState(
    val pages: List<PageState> = emptyList(),
    val isLoading: Boolean = false,
    val fileName: String? = null,
    val isDirty: Boolean = false,
    val selectedIndices: Set<Int> = emptySet(),
    val isMihirakiView: Boolean = false,
    val isRtl: Boolean = false,
    val errorMessage: String? = null,
    val errorResId: Int? = null,
    val showPasswordDialog: Boolean = false,
    val pendingUri: Uri? = null,
    val isAppending: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val keywords: String = "",
    val savePassword: String = "",
    val showSettingsMenu: Boolean = false,
    val activeSettingsDialog: SettingsDialogType? = null,
    val previewPageIndex: Int? = null,
    val pdfVersion: String = "",
    val pageLayout: String = "SinglePage",
    val scrollDirection: String = "L2R",
    val showCover: Boolean = false
)

class PdfViewModel(application: Application) : AndroidViewModel(application) {
    private val processor = PdfProcessor()
    private val thumbnailProvider = ThumbnailProvider(application)
    val billing = BillingManager(application)
    
    private val _uiState = MutableStateFlow(PdfUiState())
    val uiState: StateFlow<PdfUiState> = _uiState

    private val undoStack = mutableListOf<List<PageState>>()
    private val redoStack = mutableListOf<List<PageState>>()
    private val maxHistorySize = 20

    private fun saveToHistory() {
        undoStack.add(_uiState.value.pages.toList())
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        updateHistoryFlags()
    }

    private fun updateHistoryFlags() {
        _uiState.value = _uiState.value.copy(
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentState = _uiState.value.pages.toList()
        redoStack.add(currentState)
        val previousState = undoStack.removeAt(undoStack.size - 1)
        _uiState.value = _uiState.value.copy(pages = previousState, isDirty = true)
        updateHistoryFlags()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentState = _uiState.value.pages.toList()
        undoStack.add(currentState)
        val nextState = redoStack.removeAt(redoStack.size - 1)
        _uiState.value = _uiState.value.copy(pages = nextState, isDirty = true)
        updateHistoryFlags()
    }

    fun loadPdf(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            val fileName = getFileName(uri)
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                fileName = fileName,
                errorMessage = null,
                errorResId = null,
                showPasswordDialog = false,
                isAppending = false
            )
            
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        processor.load(inputStream, password)
                        
                        val pageCount = thumbnailProvider.getPageCount(uri)
                        val pages = (0 until pageCount).map { index ->
                            PageState(
                                thumbnail = thumbnailProvider.getThumbnail(uri, index, 288),
                                originalIndex = index,
                                sourceUri = uri
                            )
                        }
                        
                        val metadata = processor.getMetadata()
                        
                        _uiState.value = _uiState.value.copy(
                            pages = pages,
                            isLoading = false,
                            isDirty = false,
                            pendingUri = null,
                            title = metadata["title"] ?: "",
                            author = metadata["author"] ?: "",
                            subject = metadata["subject"] ?: "",
                            keywords = metadata["keywords"] ?: "",
                            pdfVersion = metadata["version"] ?: "",
                            pageLayout = metadata["layout"] ?: "SinglePage",
                            scrollDirection = metadata["direction"] ?: "L2R",
                            showCover = metadata["layout"]?.let { it == "TwoColumnRight" || it == "TwoPageRight" } ?: false,
                            savePassword = ""
                        )
                        // Clear history on new load
                        undoStack.clear()
                        redoStack.clear()
                        updateHistoryFlags()
                    }
                } catch (e: InvalidPasswordException) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showPasswordDialog = true,
                        pendingUri = uri,
                        isAppending = false,
                        errorResId = if (password != null) R.string.error_incorrect_password else null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorResId = R.string.error_load_failed
                    )
                }
            }
        }
    }

    fun appendPdf(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                errorResId = null,
                showPasswordDialog = false,
                isAppending = true
            )

            withContext(Dispatchers.IO) {
                try {
                    val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        saveToHistory()
                        val startIndex = processor.append(inputStream, password) - thumbnailProvider.getPageCount(uri)
                        
                        val pageCount = thumbnailProvider.getPageCount(uri)
                        val newPages = (0 until pageCount).map { index ->
                            PageState(
                                thumbnail = thumbnailProvider.getThumbnail(uri, index, 288),
                                originalIndex = startIndex + index,
                                sourceUri = uri
                            )
                        }
                        
                        val currentPages = _uiState.value.pages.toMutableList()
                        currentPages.addAll(newPages)
                        
                        _uiState.value = _uiState.value.copy(
                            pages = currentPages,
                            isLoading = false,
                            isDirty = true,
                            pendingUri = null,
                            isAppending = false
                        )
                    }
                } catch (e: InvalidPasswordException) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showPasswordDialog = true,
                        pendingUri = uri,
                        isAppending = true,
                        errorResId = if (password != null) R.string.error_incorrect_password else null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorResId = R.string.error_append_failed
                    )
                }
            }
        }
    }

    fun handlePassword(password: String) {
        val uri = _uiState.value.pendingUri ?: return
        if (_uiState.value.isAppending) {
            appendPdf(uri, password)
        } else {
            loadPdf(uri, password)
        }
    }

    fun cancelPasswordDialog() {
        _uiState.value = _uiState.value.copy(showPasswordDialog = false, pendingUri = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, errorResId = null)
    }

    fun toggleSettingsMenu() {
        _uiState.value = _uiState.value.copy(showSettingsMenu = !_uiState.value.showSettingsMenu)
    }

    fun openSettingsDialog(type: SettingsDialogType) {
        _uiState.value = _uiState.value.copy(activeSettingsDialog = type, showSettingsMenu = false)
    }

    fun closeSettingsDialog() {
        _uiState.value = _uiState.value.copy(activeSettingsDialog = null)
    }

    fun openPreview(index: Int) {
        _uiState.value = _uiState.value.copy(previewPageIndex = index)
    }

    fun closePreview() {
        _uiState.value = _uiState.value.copy(previewPageIndex = null)
    }

    fun getHighResThumbnail(index: Int): Bitmap? {
        val page = _uiState.value.pages.getOrNull(index) ?: return null
        val uri = page.sourceUri ?: return null
        // 1024 or similar for high res
        return thumbnailProvider.getThumbnail(uri, page.originalIndex, 1024)
    }

    fun updateMetadata(
        title: String, 
        author: String, 
        subject: String, 
        keywords: String,
        pdfVersion: String,
        pageLayout: String,
        scrollDirection: String,
        showCover: Boolean
    ) {
        val verNum = pdfVersion.toFloatOrNull() ?: 1.4f
        val adjustedVersion = if ((pageLayout == "TwoPageLeft" || pageLayout == "TwoPageRight") && verNum < 1.5f) {
            "1.5"
        } else {
            pdfVersion
        }
        _uiState.value = _uiState.value.copy(
            title = title,
            author = author,
            subject = subject,
            keywords = keywords,
            pdfVersion = adjustedVersion,
            pageLayout = pageLayout,
            scrollDirection = scrollDirection,
            showCover = showCover,
            isDirty = true
        )
    }

    fun setSavePassword(password: String) {
        _uiState.value = _uiState.value.copy(savePassword = password)
    }

    fun rotateSelected(degrees: Int) {
        saveToHistory()
        val currentPages = _uiState.value.pages.toMutableList()
        val selected = _uiState.value.selectedIndices
        
        selected.forEach { index ->
            if (index in currentPages.indices) {
                val page = currentPages[index]
                val newRotation = (page.rotation + degrees) % 360
                val rotatedBitmap = page.thumbnail?.let { rotateBitmap(it, degrees.toFloat()) }
                currentPages[index] = page.copy(rotation = newRotation, thumbnail = rotatedBitmap)
            }
        }
        
        _uiState.value = _uiState.value.copy(pages = currentPages, isDirty = true)
    }

    fun deleteSelected() {
        saveToHistory()
        val currentPages = _uiState.value.pages.toMutableList()
        val selected = _uiState.value.selectedIndices.sortedDescending()
        
        selected.forEach { index ->
            if (index in currentPages.indices) {
                currentPages.removeAt(index)
            }
        }
        
        _uiState.value = _uiState.value.copy(
            pages = currentPages, 
            isDirty = true, 
            selectedIndices = emptySet()
        )
    }

    fun moveSelected(direction: Int) {
        saveToHistory()
        val currentPages = _uiState.value.pages.toMutableList()
        val selected = _uiState.value.selectedIndices.toSet()
        val newSelected = mutableSetOf<Int>()

        val indices = if (direction > 0) currentPages.indices.reversed() else currentPages.indices
        
        for (i in indices) {
            val target = i + direction
            if (selected.contains(i) && target in currentPages.indices && !selected.contains(target)) {
                val temp = currentPages[i]
                currentPages[i] = currentPages[target]
                currentPages[target] = temp
                newSelected.add(target)
            } else if (selected.contains(i)) {
                newSelected.add(i)
            }
        }
        
        _uiState.value = _uiState.value.copy(
            pages = currentPages,
            isDirty = true,
            selectedIndices = newSelected
        )
    }

    fun insertBlankAfterSelected() {
        saveToHistory()
        val currentPages = _uiState.value.pages.toMutableList()
        val selected = _uiState.value.selectedIndices.sortedDescending()
        
        selected.forEach { index ->
            if (index in currentPages.indices) {
                currentPages.add(index + 1, PageState(
                    thumbnail = null,
                    originalIndex = -1,
                    isBlank = true
                ))
            }
        }
        
        _uiState.value = _uiState.value.copy(pages = currentPages, isDirty = true)
    }

    fun toggleSelection(index: Int) {
        val currentSelected = _uiState.value.selectedIndices.toMutableSet()
        if (currentSelected.contains(index)) {
            currentSelected.remove(index)
        } else {
            currentSelected.add(index)
        }
        _uiState.value = _uiState.value.copy(selectedIndices = currentSelected)
    }

    fun selectAll() {
        val allIndices = _uiState.value.pages.indices.toSet()
        _uiState.value = _uiState.value.copy(selectedIndices = allIndices)
    }

    fun invertSelection() {
        val allIndices = _uiState.value.pages.indices.toSet()
        val currentSelected = _uiState.value.selectedIndices
        val inverted = allIndices - currentSelected
        _uiState.value = _uiState.value.copy(selectedIndices = inverted)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedIndices = emptySet())
    }

    fun toggleMihirakiView() {
        _uiState.value = _uiState.value.copy(isMihirakiView = !_uiState.value.isMihirakiView)
    }

    fun cropSelectedPages(percent: Float) {
        saveToHistory()
        val currentPages = _uiState.value.pages.toMutableList()
        val selected = _uiState.value.selectedIndices
        
        selected.forEach { index ->
            if (index in currentPages.indices) {
                currentPages[index] = currentPages[index].copy(cropMargin = percent)
            }
        }
        
        _uiState.value = _uiState.value.copy(pages = currentPages, isDirty = true)
    }

    fun splitSelectedPages(direction: SplitDirection = SplitDirection.VERTICAL) {
        saveToHistory()
        val currentPages = _uiState.value.pages.toMutableList()
        val selected = _uiState.value.selectedIndices.sortedDescending()
        val isRtl = _uiState.value.isRtl
        
        selected.forEach { index ->
            if (index in currentPages.indices) {
                val page = currentPages[index]
                if (!page.isBlank && !page.isSplit) {
                    val thumb = page.thumbnail
                    if (thumb != null) {
                        val w = thumb.width
                        val h = thumb.height
                        
                        val (part1Thumb, part2Thumb) = if (direction == SplitDirection.VERTICAL) {
                            Bitmap.createBitmap(thumb, 0, 0, w / 2, h) to
                            Bitmap.createBitmap(thumb, w / 2, 0, w / 2, h)
                        } else {
                            Bitmap.createBitmap(thumb, 0, 0, w, h / 2) to
                            Bitmap.createBitmap(thumb, 0, h / 2, w, h / 2)
                        }
                        
                        val page1 = page.copy(
                            id = UUID.randomUUID().toString(),
                            thumbnail = part1Thumb,
                            isSplit = true,
                            splitPart = 0,
                            splitDirection = direction
                        )
                        val page2 = page.copy(
                            id = UUID.randomUUID().toString(),
                            thumbnail = part2Thumb,
                            isSplit = true,
                            splitPart = 1,
                            splitDirection = direction
                        )
                        
                        currentPages.removeAt(index)
                        if (direction == SplitDirection.VERTICAL && isRtl) {
                            currentPages.add(index, page2)
                            currentPages.add(index + 1, page1)
                        } else {
                            currentPages.add(index, page1)
                            currentPages.add(index + 1, page2)
                        }
                    }
                }
            }
        }
        
        _uiState.value = _uiState.value.copy(
            pages = currentPages,
            isDirty = true,
            selectedIndices = emptySet()
        )
    }

    fun toggleRtl() {
        _uiState.value = _uiState.value.copy(isRtl = !_uiState.value.isRtl)
    }

    fun movePage(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _uiState.value.pages.indices || toIndex !in _uiState.value.pages.indices) return
        if (fromIndex == toIndex) return

        saveToHistory()
        val currentPages = _uiState.value.pages.toMutableList()
        val page = currentPages.removeAt(fromIndex)
        currentPages.add(toIndex, page)
        
        val newSelected = mutableSetOf<Int>()
        val selectedIds = _uiState.value.selectedIndices.mapNotNull { i -> 
            _uiState.value.pages.getOrNull(i)?.id 
        }.toSet()
        
        currentPages.forEachIndexed { index, p ->
            if (selectedIds.contains(p.id)) newSelected.add(index)
        }

        _uiState.value = _uiState.value.copy(
            pages = currentPages,
            isDirty = true,
            selectedIndices = newSelected
        )
    }

    fun savePdf(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            withContext(Dispatchers.IO) {
                try {
                    val currentState = _uiState.value
                    val metadata = mapOf(
                        "title" to currentState.title,
                        "author" to currentState.author,
                        "subject" to currentState.subject,
                        "keywords" to currentState.keywords,
                        "layout" to currentState.pageLayout,
                        "direction" to currentState.scrollDirection,
                        "version" to currentState.pdfVersion
                    )
                    val savePassword = password ?: currentState.savePassword.takeIf { it.isNotEmpty() }
                    
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        processor.save(outputStream, currentState.pages, savePassword, metadata)
                    }
                    _uiState.value = _uiState.value.copy(isDirty = false)
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun exportSelected(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            withContext(Dispatchers.IO) {
                try {
                    val currentState = _uiState.value
                    val selectedPages = currentState.selectedIndices.sorted().map { currentState.pages[it] }
                    val metadata = mapOf(
                        "title" to currentState.title,
                        "author" to currentState.author,
                        "subject" to currentState.subject,
                        "keywords" to currentState.keywords,
                        "layout" to currentState.pageLayout,
                        "direction" to currentState.scrollDirection,
                        "version" to currentState.pdfVersion
                    )
                    val savePassword = password ?: currentState.savePassword.takeIf { it.isNotEmpty() }

                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        processor.save(outputStream, selectedPages, savePassword, metadata)
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorResId = R.string.error_export_failed)
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        processor.close()
        thumbnailProvider.close()
        billing.close()
    }
}
