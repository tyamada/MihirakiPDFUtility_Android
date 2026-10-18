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
import com.takumayamada22.pdfutility.domain.ExportPdfUseCase
import com.takumayamada22.pdfutility.domain.ImageFileItem
import com.takumayamada22.pdfutility.domain.ImageSortField
import com.takumayamada22.pdfutility.domain.ImageSortOrder
import com.takumayamada22.pdfutility.domain.LoadPdfUseCase
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.domain.ThumbnailProvider
import com.takumayamada22.pdfutility.util.AppLogger
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.io.File
import java.io.FileOutputStream

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

enum class ThumbnailSize { S, M, L }

enum class ButtonDisplayType { ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY }

enum class SettingsDialogType {
    PROPERTY, PASSWORD, OPTIONS, VERSION, SUPPORT, HELP, DIAGNOSTICS, REPORT_ISSUE
}

data class PdfDocumentState(
    val pages: List<PageState> = emptyList(),
    val isLoading: Boolean = false,
    val fileName: String? = null,
    val isDirty: Boolean = false,
    val isAppending: Boolean = false,
    val pdfVersion: String = "",
    val pageLayout: String = "SinglePage",
    val scrollDirection: String = "L2R",
    val showCover: Boolean = false,
    val isSamplePdf: Boolean = false,
    val ccByAcknowledged: Boolean = false
)

data class PdfSelectionState(
    val selectedIndices: Set<Int> = emptySet(),
    val previewPageIndex: Int? = null
)

data class PdfMetadataState(
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val keywords: String = "",
    val savePassword: String = ""
)

data class PdfDialogState(
    val errorMessage: String? = null,
    val errorResId: Int? = null,
    val showPasswordDialog: Boolean = false,
    val pendingUri: Uri? = null,
    val showSettingsMenu: Boolean = false,
    val activeSettingsDialog: SettingsDialogType? = null,
    val showCcByDialog: Boolean = false,
    val showSamplePdfScreen: Boolean = false
)

data class PdfHistoryState(
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

data class PdfPreferenceState(
    val isMihirakiView: Boolean = false,
    val isRtl: Boolean = false,
    val thumbnailSize: ThumbnailSize = ThumbnailSize.M,
    val buttonDisplayType: ButtonDisplayType = ButtonDisplayType.ICON_AND_TEXT
)

data class FolderImageState(
    val folderUri: Uri? = null,
    val images: List<ImageFileItem> = emptyList(),
    val sortField: ImageSortField = ImageSortField.NAME,
    val sortOrder: ImageSortOrder = ImageSortOrder.ASCENDING,
    val showFolderImageDialog: Boolean = false,
    val isLoadingFolder: Boolean = false
)

data class PdfUiState(
    val document: PdfDocumentState = PdfDocumentState(),
    val selection: PdfSelectionState = PdfSelectionState(),
    val metadata: PdfMetadataState = PdfMetadataState(),
    val dialog: PdfDialogState = PdfDialogState(),
    val history: PdfHistoryState = PdfHistoryState(),
    val preference: PdfPreferenceState = PdfPreferenceState(),
    val folderImageState: FolderImageState = FolderImageState()
) {
    val pages: List<PageState> get() = document.pages
    val isLoading: Boolean get() = document.isLoading
    val fileName: String? get() = document.fileName
    val isDirty: Boolean get() = document.isDirty
    val isAppending: Boolean get() = document.isAppending
    val pdfVersion: String get() = document.pdfVersion
    val pageLayout: String get() = document.pageLayout
    val scrollDirection: String get() = document.scrollDirection
    val showCover: Boolean get() = document.showCover
    val isSamplePdf: Boolean get() = document.isSamplePdf
    val ccByAcknowledged: Boolean get() = document.ccByAcknowledged

    val selectedIndices: Set<Int> get() = selection.selectedIndices
    val previewPageIndex: Int? get() = selection.previewPageIndex

    val title: String get() = metadata.title
    val author: String get() = metadata.author
    val subject: String get() = metadata.subject
    val keywords: String get() = metadata.keywords
    val savePassword: String get() = metadata.savePassword

    val errorMessage: String? get() = dialog.errorMessage
    val errorResId: Int? get() = dialog.errorResId
    val showPasswordDialog: Boolean get() = dialog.showPasswordDialog
    val pendingUri: Uri? get() = dialog.pendingUri
    val showSettingsMenu: Boolean get() = dialog.showSettingsMenu
    val activeSettingsDialog: SettingsDialogType? get() = dialog.activeSettingsDialog
    val showCcByDialog: Boolean get() = dialog.showCcByDialog
    val showSamplePdfScreen: Boolean get() = dialog.showSamplePdfScreen

    val canUndo: Boolean get() = history.canUndo
    val canRedo: Boolean get() = history.canRedo

    val isMihirakiView: Boolean get() = preference.isMihirakiView
    val isRtl: Boolean get() = preference.isRtl
    val thumbnailSize: ThumbnailSize get() = preference.thumbnailSize
    val buttonDisplayType: ButtonDisplayType get() = preference.buttonDisplayType

    private object Unspecified

    fun copy(
        pages: List<PageState>? = null,
        isLoading: Boolean? = null,
        fileName: Any? = Unspecified,
        isDirty: Boolean? = null,
        isAppending: Boolean? = null,
        pdfVersion: String? = null,
        pageLayout: String? = null,
        scrollDirection: String? = null,
        showCover: Boolean? = null,
        isSamplePdf: Boolean? = null,
        ccByAcknowledged: Boolean? = null,
        selectedIndices: Set<Int>? = null,
        previewPageIndex: Any? = Unspecified,
        title: String? = null,
        author: String? = null,
        subject: String? = null,
        keywords: String? = null,
        savePassword: String? = null,
        errorMessage: Any? = Unspecified,
        errorResId: Any? = Unspecified,
        showPasswordDialog: Boolean? = null,
        pendingUri: Any? = Unspecified,
        showSettingsMenu: Boolean? = null,
        activeSettingsDialog: Any? = Unspecified,
        showCcByDialog: Boolean? = null,
        showSamplePdfScreen: Boolean? = null,
        canUndo: Boolean? = null,
        canRedo: Boolean? = null,
        isMihirakiView: Boolean? = null,
        isRtl: Boolean? = null,
        thumbnailSize: ThumbnailSize? = null,
        buttonDisplayType: ButtonDisplayType? = null
    ): PdfUiState {
        return PdfUiState(
            document = document.copy(
                pages = pages ?: document.pages,
                isLoading = isLoading ?: document.isLoading,
                fileName = if (fileName === Unspecified) document.fileName else fileName as String?,
                isDirty = isDirty ?: document.isDirty,
                isAppending = isAppending ?: document.isAppending,
                pdfVersion = pdfVersion ?: document.pdfVersion,
                pageLayout = pageLayout ?: document.pageLayout,
                scrollDirection = scrollDirection ?: document.scrollDirection,
                showCover = showCover ?: document.showCover,
                isSamplePdf = isSamplePdf ?: document.isSamplePdf,
                ccByAcknowledged = ccByAcknowledged ?: document.ccByAcknowledged
            ),
            selection = selection.copy(
                selectedIndices = selectedIndices ?: selection.selectedIndices,
                previewPageIndex = if (previewPageIndex === Unspecified) selection.previewPageIndex else previewPageIndex as Int?
            ),
            metadata = metadata.copy(
                title = title ?: metadata.title,
                author = author ?: metadata.author,
                subject = subject ?: metadata.subject,
                keywords = keywords ?: metadata.keywords,
                savePassword = savePassword ?: metadata.savePassword
            ),
            dialog = dialog.copy(
                errorMessage = if (errorMessage === Unspecified) dialog.errorMessage else errorMessage as String?,
                errorResId = if (errorResId === Unspecified) dialog.errorResId else errorResId as Int?,
                showPasswordDialog = showPasswordDialog ?: dialog.showPasswordDialog,
                pendingUri = if (pendingUri === Unspecified) dialog.pendingUri else pendingUri as Uri?,
                showSettingsMenu = showSettingsMenu ?: dialog.showSettingsMenu,
                activeSettingsDialog = if (activeSettingsDialog === Unspecified) dialog.activeSettingsDialog else activeSettingsDialog as SettingsDialogType?,
                showCcByDialog = showCcByDialog ?: dialog.showCcByDialog,
                showSamplePdfScreen = showSamplePdfScreen ?: dialog.showSamplePdfScreen
            ),
            history = history.copy(
                canUndo = canUndo ?: history.canUndo,
                canRedo = canRedo ?: history.canRedo
            ),
            preference = preference.copy(
                isMihirakiView = isMihirakiView ?: preference.isMihirakiView,
                isRtl = isRtl ?: preference.isRtl,
                thumbnailSize = thumbnailSize ?: preference.thumbnailSize,
                buttonDisplayType = buttonDisplayType ?: preference.buttonDisplayType
            )
        )
    }
}

class PdfViewModel(
    application: Application,
    private val loadPdfUseCase: LoadPdfUseCase = LoadPdfUseCase(PdfProcessor()),
    private val exportPdfUseCase: ExportPdfUseCase = ExportPdfUseCase(PdfProcessor()),
    private val thumbnailProvider: ThumbnailProvider = ThumbnailProvider(application),
    val billing: BillingManager = BillingManager(application)
) : AndroidViewModel(application) {
    companion object {
        fun provideFactory(
            application: Application,
            loadPdfUseCase: LoadPdfUseCase = LoadPdfUseCase(PdfProcessor()),
            exportPdfUseCase: ExportPdfUseCase = ExportPdfUseCase(PdfProcessor()),
            thumbnailProvider: ThumbnailProvider = ThumbnailProvider(application),
            billing: BillingManager = BillingManager(application)
        ): androidx.lifecycle.ViewModelProvider.Factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PdfViewModel::class.java)) {
                    return PdfViewModel(
                        application,
                        loadPdfUseCase,
                        exportPdfUseCase,
                        thumbnailProvider,
                        billing
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    
    private val metrics = application.resources.displayMetrics
    private val minDimensionPx = minOf(metrics.widthPixels, metrics.heightPixels)
    private val defaultThumbnailSize = if (minDimensionPx < 1280) ThumbnailSize.S else ThumbnailSize.M

    private val _uiState = MutableStateFlow(PdfUiState(preference = PdfPreferenceState(thumbnailSize = defaultThumbnailSize)))
    val uiState: StateFlow<PdfUiState> = _uiState

    init {
        loadSamplePdf()
    }

    private fun getThumbnailWidth(size: ThumbnailSize): Int {
        return when (size) {
            ThumbnailSize.S -> 100
            ThumbnailSize.M -> 200
            ThumbnailSize.L -> 400
        }
    }

    fun setThumbnailSize(size: ThumbnailSize) {
        if (_uiState.value.thumbnailSize == size) return
        _uiState.value = _uiState.value.copy(thumbnailSize = size)
        val width = getThumbnailWidth(size)
        viewModelScope.launch {
            val currentPages = _uiState.value.pages
            val updatedPages = withContext(Dispatchers.IO) {
                currentPages.map { page ->
                    val uri = page.sourceUri
                    val thumb = if (uri != null) {
                        thumbnailProvider.getThumbnail(uri, page.originalIndex, width)
                    } else page.thumbnail
                    page.copy(thumbnail = thumb)
                }
            }
            _uiState.value = _uiState.value.copy(pages = updatedPages)
        }
    }

    fun setButtonDisplayType(type: ButtonDisplayType) {
        if (_uiState.value.buttonDisplayType == type) return
        _uiState.value = _uiState.value.copy(buttonDisplayType = type)
    }

    private var pendingAction: (() -> Unit)? = null

    fun openSamplePdf() {
        openSamplePdfScreen()
    }

    fun openSamplePdfScreen() {
        _uiState.value = _uiState.value.copy(showSamplePdfScreen = true)
    }

    fun closeSamplePdfScreen() {
        _uiState.value = _uiState.value.copy(showSamplePdfScreen = false)
    }

    fun openSamplePdfUri(uri: Uri) {
        closeSamplePdfScreen()
        loadPdf(uri)
        _uiState.value = _uiState.value.copy(isSamplePdf = true, ccByAcknowledged = false)
    }

    fun dismissCcByDialog() {
        pendingAction = null
        _uiState.value = _uiState.value.copy(showCcByDialog = false)
    }

    fun confirmCcByDialog() {
        _uiState.value = _uiState.value.copy(showCcByDialog = false, ccByAcknowledged = true)
        val action = pendingAction
        pendingAction = null
        action?.invoke()
    }

    private fun withSampleCheck(action: () -> Unit) {
        if (_uiState.value.isSamplePdf && !_uiState.value.ccByAcknowledged) {
            pendingAction = action
            _uiState.value = _uiState.value.copy(showCcByDialog = true)
        } else {
            action()
        }
    }

    private fun loadSamplePdf() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, fileName = "sample.pdf", isSamplePdf = true, ccByAcknowledged = false)
            val count = withContext(Dispatchers.IO) {
                try {
                    val inputStream = getApplication<Application>().assets.open("sample.pdf")
                    loadPdfUseCase.load(inputStream)
                } catch (e: Exception) {
                    0
                }
            }
            if (count > 0) {
                AppLogger.log(getApplication(), "INFO", "Sample PDF loaded successfully, pages: $count")
                val cacheFile = File(getApplication<Application>().cacheDir, "sample.pdf")
                withContext(Dispatchers.IO) {
                    try {
                        getApplication<Application>().assets.open("sample.pdf").use { input ->
                            FileOutputStream(cacheFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } catch (e: Exception) {}
                }
                val uri = Uri.fromFile(cacheFile)
                val pages = (0 until count).map { index ->
                    PageState(
                        originalIndex = index,
                        thumbnail = thumbnailProvider.getThumbnail(uri, index, getThumbnailWidth(_uiState.value.thumbnailSize)),
                        sourceUri = uri
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pages = pages,
                    fileName = "sample.pdf",
                    isSamplePdf = true,
                    ccByAcknowledged = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private data class PageSnapshot(
        val id: String,
        val originalIndex: Int,
        val rotation: Int,
        val isBlank: Boolean,
        val sourceUri: Uri?,
        val isSplit: Boolean,
        val splitPart: Int,
        val splitDirection: SplitDirection,
        val cropMargin: Float
    )

    private data class HistoryState(
        val pageSnapshots: List<PageSnapshot>,
        val selectedIndices: Set<Int>
    )

    private val undoStack = mutableListOf<HistoryState>()
    private val redoStack = mutableListOf<HistoryState>()
    private val maxHistorySize = 20

    private fun saveToHistory() {
        val snapshots = _uiState.value.pages.map { page ->
            PageSnapshot(
                id = page.id,
                originalIndex = page.originalIndex,
                rotation = page.rotation,
                isBlank = page.isBlank,
                sourceUri = page.sourceUri,
                isSplit = page.isSplit,
                splitPart = page.splitPart,
                splitDirection = page.splitDirection,
                cropMargin = page.cropMargin
            )
        }
        undoStack.add(HistoryState(snapshots, _uiState.value.selectedIndices.toSet()))
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
        val currentSnapshots = _uiState.value.pages.map { page ->
            PageSnapshot(
                id = page.id,
                originalIndex = page.originalIndex,
                rotation = page.rotation,
                isBlank = page.isBlank,
                sourceUri = page.sourceUri,
                isSplit = page.isSplit,
                splitPart = page.splitPart,
                splitDirection = page.splitDirection,
                cropMargin = page.cropMargin
            )
        }
        val currentState = HistoryState(currentSnapshots, _uiState.value.selectedIndices.toSet())
        redoStack.add(currentState)
        val previousState = undoStack.removeAt(undoStack.size - 1)
        restoreState(previousState)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentSnapshots = _uiState.value.pages.map { page ->
            PageSnapshot(
                id = page.id,
                originalIndex = page.originalIndex,
                rotation = page.rotation,
                isBlank = page.isBlank,
                sourceUri = page.sourceUri,
                isSplit = page.isSplit,
                splitPart = page.splitPart,
                splitDirection = page.splitDirection,
                cropMargin = page.cropMargin
            )
        }
        val currentState = HistoryState(currentSnapshots, _uiState.value.selectedIndices.toSet())
        undoStack.add(currentState)
        val nextState = redoStack.removeAt(redoStack.size - 1)
        restoreState(nextState)
    }

    private fun restoreState(historyState: HistoryState) {
        val currentPagesMap = _uiState.value.pages.associateBy { it.id }
        val restoredPages = historyState.pageSnapshots.map { snapshot ->
            val existing = currentPagesMap[snapshot.id]
            if (existing != null) {
                existing.copy(
                    rotation = snapshot.rotation,
                    originalIndex = snapshot.originalIndex,
                    isBlank = snapshot.isBlank,
                    sourceUri = snapshot.sourceUri,
                    isSplit = snapshot.isSplit,
                    splitPart = snapshot.splitPart,
                    splitDirection = snapshot.splitDirection,
                    cropMargin = snapshot.cropMargin
                )
            } else {
                PageState(
                    id = snapshot.id,
                    thumbnail = snapshot.sourceUri?.let { uri ->
                        thumbnailProvider.getThumbnail(uri, snapshot.originalIndex, getThumbnailWidth(_uiState.value.thumbnailSize))
                    },
                    rotation = snapshot.rotation,
                    originalIndex = snapshot.originalIndex,
                    isBlank = snapshot.isBlank,
                    sourceUri = snapshot.sourceUri,
                    isSplit = snapshot.isSplit,
                    splitPart = snapshot.splitPart,
                    splitDirection = snapshot.splitDirection,
                    cropMargin = snapshot.cropMargin
                )
            }
        }
        _uiState.value = _uiState.value.copy(
            pages = restoredPages,
            selectedIndices = historyState.selectedIndices,
            isDirty = true
        )
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
                        loadPdfUseCase.load(inputStream, password)
                        
                        val pageCount = thumbnailProvider.getPageCount(uri)
                        val pages = (0 until pageCount).map { index ->
                            PageState(
                                thumbnail = thumbnailProvider.getThumbnail(uri, index, getThumbnailWidth(_uiState.value.thumbnailSize)),
                                originalIndex = index,
                                sourceUri = uri
                            )
                        }
                        
                        val metadata = loadPdfUseCase.getMetadata()
                        
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
                            savePassword = "",
                            isSamplePdf = false,
                            ccByAcknowledged = false
                        )
                        AppLogger.log(getApplication(), "INFO", "PDF loaded successfully, pages: ${pageCount}")
                        // Clear history on new load
                        undoStack.clear()
                        redoStack.clear()
                        updateHistoryFlags()
                    }
                } catch (e: InvalidPasswordException) {
                    AppLogger.log(getApplication(), "WARN", "PDF load failed: Invalid password")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showPasswordDialog = true,
                        pendingUri = uri,
                        isAppending = false,
                        errorResId = if (password != null) R.string.error_incorrect_password else null
                    )
                } catch (e: Exception) {
                    AppLogger.log(getApplication(), "ERROR", "PDF load failed: ${e.message}")
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
                        val startIndex = loadPdfUseCase.append(inputStream, password) - thumbnailProvider.getPageCount(uri)
                        
                        val pageCount = thumbnailProvider.getPageCount(uri)
                        val newPages = (0 until pageCount).map { index ->
                            PageState(
                                thumbnail = thumbnailProvider.getThumbnail(uri, index, getThumbnailWidth(_uiState.value.thumbnailSize)),
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
                    AppLogger.log(getApplication(), "WARN", "PDF load failed: Invalid password")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showPasswordDialog = true,
                        pendingUri = uri,
                        isAppending = true,
                        errorResId = if (password != null) R.string.error_incorrect_password else null
                    )
                } catch (e: Exception) {
                    AppLogger.log(getApplication(), "ERROR", "PDF load failed: ${e.message}")
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
        if (type == SettingsDialogType.VERSION || type == SettingsDialogType.SUPPORT) {
            billing.queryExistingPurchases()
        }
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
        withSampleCheck {
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
    }

    fun setRightBinding() {
        val verNum = _uiState.value.pdfVersion.toFloatOrNull() ?: 1.4f
        val adjustedVersion = if (verNum < 1.5f) "1.5" else _uiState.value.pdfVersion
        _uiState.value = _uiState.value.copy(
            pageLayout = "TwoPageLeft",
            scrollDirection = "R2L",
            showCover = false,
            pdfVersion = adjustedVersion,
            isMihirakiView = true,
            isRtl = true,
            isDirty = true
        )
    }

    fun setRightBindingWithCover() {
        val verNum = _uiState.value.pdfVersion.toFloatOrNull() ?: 1.4f
        val adjustedVersion = if (verNum < 1.5f) "1.5" else _uiState.value.pdfVersion
        _uiState.value = _uiState.value.copy(
            pageLayout = "TwoPageRight",
            scrollDirection = "R2L",
            showCover = true,
            pdfVersion = adjustedVersion,
            isMihirakiView = true,
            isRtl = true,
            isDirty = true
        )
    }

    fun setSavePassword(password: String) {
        _uiState.value = _uiState.value.copy(savePassword = password)
    }

    fun rotateSelected(degrees: Int) {
        withSampleCheck {
            saveToHistory()
            AppLogger.log(getApplication(), "INFO", "Rotated selected pages by $degrees degrees")
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
    }

    fun deleteSelected() {
        withSampleCheck {
            saveToHistory()
            AppLogger.log(getApplication(), "INFO", "Deleted selected pages")
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
    }

    fun moveSelected(direction: Int) {
        withSampleCheck {
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
    }

    fun insertBlankAfterSelected() {
        withSampleCheck {
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
        withSampleCheck {
            saveToHistory()
            AppLogger.log(getApplication(), "INFO", "Cropped selected pages with margin: $percent")
            val currentPages = _uiState.value.pages.toMutableList()
            val selected = _uiState.value.selectedIndices
            
            selected.forEach { index ->
                if (index in currentPages.indices) {
                    currentPages[index] = currentPages[index].copy(cropMargin = percent)
                }
            }
            
            _uiState.value = _uiState.value.copy(pages = currentPages, isDirty = true)
        }
    }

    fun splitSelectedPages(direction: SplitDirection = SplitDirection.VERTICAL) {
        withSampleCheck {
            saveToHistory()
            AppLogger.log(getApplication(), "INFO", "Split selected pages with direction: $direction")
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
    }

    fun toggleRtl() {
        _uiState.value = _uiState.value.copy(isRtl = !_uiState.value.isRtl)
    }

    fun movePage(fromIndex: Int, toIndex: Int) {
        withSampleCheck {
            if (fromIndex !in _uiState.value.pages.indices || toIndex !in _uiState.value.pages.indices) return@withSampleCheck
            if (fromIndex == toIndex) return@withSampleCheck

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
            updateHistoryFlags()
        }
    }

    internal fun setPagesForTest(pages: List<PageState>, selected: Set<Int> = emptySet()) {
        _uiState.value = _uiState.value.copy(pages = pages, selectedIndices = selected)
    }

    fun moveSelectedPages(fromIndex: Int, toIndex: Int) {
        withSampleCheck {
            val currentState = _uiState.value
            val selectedIndices = currentState.selectedIndices.sorted()
            if (selectedIndices.isEmpty() || fromIndex !in currentState.pages.indices || toIndex !in currentState.pages.indices) {
                movePage(fromIndex, toIndex)
                return@withSampleCheck
            }

            if (!selectedIndices.contains(fromIndex) || selectedIndices.size <= 1) {
                movePage(fromIndex, toIndex)
                return@withSampleCheck
            }

            saveToHistory()
            val oldPages = currentState.pages
            val selectedItems = selectedIndices.map { oldPages[it] }
            val selectedIds = selectedItems.map { it.id }.toSet()

            val remainingPages = oldPages.filterNot { selectedIds.contains(it.id) }.toMutableList()

            val targetPage = oldPages.getOrNull(toIndex)
            var insertPos = 0
            if (targetPage != null && !selectedIds.contains(targetPage.id)) {
                val foundIdx = remainingPages.indexOfFirst { it.id == targetPage.id }
                if (foundIdx != -1) {
                    insertPos = if (toIndex > fromIndex) foundIdx + 1 else foundIdx
                } else {
                    insertPos = remainingPages.size
                }
            } else {
                var foundIdx = -1
                for (i in toIndex until oldPages.size) {
                    val p = oldPages[i]
                    if (!selectedIds.contains(p.id)) {
                        foundIdx = remainingPages.indexOfFirst { it.id == p.id }
                        if (foundIdx != -1) break
                    }
                }
                if (foundIdx == -1) {
                    for (i in toIndex downTo 0) {
                        val p = oldPages[i]
                        if (!selectedIds.contains(p.id)) {
                            foundIdx = remainingPages.indexOfFirst { it.id == p.id }
                            if (foundIdx != -1) break
                        }
                    }
                }
                insertPos = if (foundIdx != -1) {
                    if (toIndex > fromIndex) foundIdx + 1 else foundIdx
                } else {
                    remainingPages.size
                }
            }

            remainingPages.addAll(insertPos.coerceIn(0, remainingPages.size), selectedItems)

            val newSelected = mutableSetOf<Int>()
            remainingPages.forEachIndexed { index, page ->
                if (selectedIds.contains(page.id)) {
                    newSelected.add(index)
                }
            }

            _uiState.value = currentState.copy(
                pages = remainingPages,
                isDirty = true,
                selectedIndices = newSelected
            )
            updateHistoryFlags()
        }
    }

    fun savePdf(uri: Uri, password: String? = null) {
        withSampleCheck {
            savePdfInternal(uri, password)
        }
    }

    private fun savePdfInternal(uri: Uri, password: String? = null) {
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
                        exportPdfUseCase.save(outputStream, currentState.pages, savePassword, metadata)
                    }
                    AppLogger.log(getApplication(), "INFO", "PDF saved successfully")
                    _uiState.value = _uiState.value.copy(isDirty = false)
                } catch (e: Exception) {
                    AppLogger.log(getApplication(), "ERROR", "PDF save failed: ${e.message}")
                    _uiState.value = _uiState.value.copy(errorResId = R.string.error_save_failed)
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun exportSelected(uri: Uri, password: String? = null) {
        withSampleCheck {
            exportSelectedInternal(uri, password)
        }
    }

    private fun exportSelectedInternal(uri: Uri, password: String? = null) {
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
                        exportPdfUseCase.save(outputStream, selectedPages, savePassword, metadata)
                    }
                    AppLogger.log(getApplication(), "INFO", "PDF export successfully")
                } catch (e: Exception) {
                    AppLogger.log(getApplication(), "ERROR", "PDF export failed: ${e.message}")
                    _uiState.value = _uiState.value.copy(errorResId = R.string.error_export_failed)
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun loadImagesFromFolder(folderUri: Uri, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                folderImageState = _uiState.value.folderImageState.copy(
                    folderUri = folderUri,
                    isLoadingFolder = true
                )
            )
            val images = mutableListOf<ImageFileItem>()
            try {
                val docFile = DocumentFile.fromTreeUri(context, folderUri)
                docFile?.listFiles()?.forEach { file ->
                    if (file.isFile && file.canRead()) {
                        val name = file.name ?: ""
                        val mimeType = file.type ?: ""
                        val isImage = mimeType.startsWith("image/") ||
                            name.endsWith(".jpg", true) ||
                            name.endsWith(".jpeg", true) ||
                            name.endsWith(".png", true) ||
                            name.endsWith(".webp", true) ||
                            name.endsWith(".bmp", true)
                        if (isImage) {
                            val uri = file.uri
                            val size = file.length()
                            val lastModified = file.lastModified()
                            val creationTime = lastModified

                            images.add(
                                ImageFileItem(
                                    uri = uri,
                                    name = name,
                                    size = size,
                                    creationTime = creationTime,
                                    lastModifiedTime = lastModified
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.log(context, "ERROR", "Failed to load images from folder: ${e.message}")
            }

            val sortedImages = sortImages(images, ImageSortField.NAME, ImageSortOrder.ASCENDING)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    folderImageState = _uiState.value.folderImageState.copy(
                        images = sortedImages,
                        sortField = ImageSortField.NAME,
                        sortOrder = ImageSortOrder.ASCENDING,
                        showFolderImageDialog = true,
                        isLoadingFolder = false
                    )
                )
            }
        }
    }

    fun setImageSortOptions(field: ImageSortField, order: ImageSortOrder) {
        val current = _uiState.value.folderImageState
        val sorted = sortImages(current.images, field, order)
        _uiState.value = _uiState.value.copy(
            folderImageState = current.copy(
                images = sorted,
                sortField = field,
                sortOrder = order
            )
        )
    }

    fun dismissFolderImageDialog() {
        _uiState.value = _uiState.value.copy(
            folderImageState = _uiState.value.folderImageState.copy(showFolderImageDialog = false)
        )
    }

    fun createPdfFromFolderImages(context: android.content.Context) {
        val imageUris = _uiState.value.folderImageState.images.map { it.uri }
        if (imageUris.isEmpty()) {
            dismissFolderImageDialog()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                document = _uiState.value.document.copy(isLoading = true),
                folderImageState = _uiState.value.folderImageState.copy(showFolderImageDialog = false)
            )
            try {
                loadPdfUseCase.createPdfFromImages(imageUris, context)
                
                val tempFile = File(context.cacheDir, "folder_images_${System.currentTimeMillis()}.pdf")
                FileOutputStream(tempFile).use { fos ->
                    loadPdfUseCase.save(fos)
                }
                val tempUri = Uri.fromFile(tempFile)

                val pageCount = thumbnailProvider.getPageCount(tempUri)
                val pages = (0 until pageCount).map { index ->
                    PageState(
                        thumbnail = thumbnailProvider.getThumbnail(tempUri, index, getThumbnailWidth(_uiState.value.thumbnailSize)),
                        originalIndex = index,
                        sourceUri = tempUri
                    )
                }

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        document = PdfDocumentState(
                            pages = pages,
                            isLoading = false,
                            fileName = "FolderImages.pdf",
                            isDirty = true
                        )
                    )
                    AppLogger.log(context, "INFO", "Successfully created PDF from folder images, pages: $pageCount")
                    undoStack.clear()
                    redoStack.clear()
                    updateHistoryFlags()
                }
            } catch (e: Exception) {
                AppLogger.log(context, "ERROR", "Failed to create PDF from folder images: ${e.message}")
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        document = _uiState.value.document.copy(isLoading = false),
                        dialog = _uiState.value.dialog.copy(errorMessage = "Failed to create PDF from images: ${e.message}")
                    )
                }
            }
        }
    }

    private fun sortImages(images: List<ImageFileItem>, field: ImageSortField, order: ImageSortOrder): List<ImageFileItem> {
        val comparator = when (field) {
            ImageSortField.NAME -> compareBy<ImageFileItem> { it.name.lowercase() }
            ImageSortField.CREATION_DATE -> compareBy { it.creationTime }
            ImageSortField.MODIFIED_DATE -> compareBy { it.lastModifiedTime }
        }
        return if (order == ImageSortOrder.ASCENDING) {
            images.sortedWith(comparator)
        } else {
            images.sortedWith(comparator.reversed())
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
        thumbnailProvider.close()
        billing.close()
    }
}
