package jp.mihiraki.pdfutility.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import jp.mihiraki.pdfutility.domain.PdfProcessor
import jp.mihiraki.pdfutility.domain.ThumbnailProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class PageState(
    val id: String = UUID.randomUUID().toString(),
    val thumbnail: Bitmap?,
    val rotation: Int = 0,
    val originalIndex: Int,
    val isBlank: Boolean = false,
    val sourceUri: Uri? = null
)

data class PdfUiState(
    val pages: List<PageState> = emptyList(),
    val isLoading: Boolean = false,
    val fileName: String? = null,
    val isDirty: Boolean = false,
    val selectedIndices: Set<Int> = emptySet(),
    val isMihirakiView: Boolean = false,
    val isRtl: Boolean = false,
    val errorMessage: String? = null,
    val showPasswordDialog: Boolean = false,
    val pendingUri: Uri? = null,
    val isAppending: Boolean = false
)

class PdfViewModel(application: Application) : AndroidViewModel(application) {
    private val processor = PdfProcessor()
    private val thumbnailProvider = ThumbnailProvider(application)
    
    private val _uiState = MutableStateFlow(PdfUiState())
    val uiState: StateFlow<PdfUiState> = _uiState

    fun loadPdf(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                fileName = uri.lastPathSegment,
                errorMessage = null,
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
                        _uiState.value = _uiState.value.copy(
                            pages = pages,
                            isLoading = false,
                            isDirty = false,
                            pendingUri = null
                        )
                    }
                } catch (e: InvalidPasswordException) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showPasswordDialog = true,
                        pendingUri = uri,
                        isAppending = false,
                        errorMessage = if (password != null) "Incorrect password" else null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load PDF"
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
                showPasswordDialog = false,
                isAppending = true
            )

            withContext(Dispatchers.IO) {
                try {
                    val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                    if (inputStream != null) {
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
                        errorMessage = if (password != null) "Incorrect password" else null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to append PDF"
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
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun rotateSelected(degrees: Int) {
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

    fun toggleRtl() {
        _uiState.value = _uiState.value.copy(isRtl = !_uiState.value.isRtl)
    }

    fun movePage(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _uiState.value.pages.indices || toIndex !in _uiState.value.pages.indices) return
        if (fromIndex == toIndex) return

        val currentPages = _uiState.value.pages.toMutableList()
        val page = currentPages.removeAt(fromIndex)
        currentPages.add(toIndex, page)
        
        // Update selection indices to match new positions
        val currentSelected = _uiState.value.selectedIndices
        val newSelected = mutableSetOf<Int>()
        
        currentPages.indices.forEach { index ->
            // This is a bit complex if we want to track the EXACT page objects.
            // But since they have UUIDs, we can use that.
        }
        
        // Simpler way: just map the IDs
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
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        processor.save(outputStream, _uiState.value.pages, password)
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

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    override fun onCleared() {
        super.onCleared()
        processor.close()
        thumbnailProvider.close()
    }
}
