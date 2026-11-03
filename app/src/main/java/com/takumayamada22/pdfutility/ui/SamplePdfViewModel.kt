package com.takumayamada22.pdfutility.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.takumayamada22.pdfutility.data.DownloadState
import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.data.SamplePdfItem
import com.takumayamada22.pdfutility.data.SamplePdfItemState
import com.takumayamada22.pdfutility.data.SamplePdfRepository
import com.takumayamada22.pdfutility.data.SamplePdfSortOrder
import com.takumayamada22.pdfutility.util.AppLogger
import com.takumayamada22.pdfutility.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import com.takumayamada22.pdfutility.data.SamplePdfLanguages
import com.takumayamada22.pdfutility.R
import java.util.Locale

data class SamplePdfUiState(
    val items: List<SamplePdfItemState> = emptyList(),
    val selectedLanguage: String = "en",
    val availableLanguages: List<String> = emptyList(),
    val cellularDialogItem: SamplePdfItem? = null,
    val sortOrder: SamplePdfSortOrder = SamplePdfSortOrder.DESCENDING
)

class SamplePdfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SamplePdfRepository(application)

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    private val _selectedLanguage = MutableStateFlow(getDefaultLanguage())
    private val _cellularDialogItem = MutableStateFlow<SamplePdfItem?>(null)
    private val _sortOrder = MutableStateFlow(PreferencesManager.getSamplePdfSortOrder(application))
    private val _refreshTrigger = MutableStateFlow(0)

    // Full hashes are checked on refresh, not on each download progress tick.
    private val verifiedFiles = _refreshTrigger.map {
        SamplePdfCatalog.items.associate { item -> item.id to repository.getDownloadedFile(item) }
    }.flowOn(Dispatchers.IO)

    val uiState: StateFlow<SamplePdfUiState> = combine(
        _downloadStates,
        _selectedLanguage,
        _cellularDialogItem,
        _sortOrder,
        verifiedFiles
    ) { states, lang, dialogItem, sortOrder, files ->
        val allLangs = SamplePdfCatalog.getAvailableLanguages()
        val mappedItems = SamplePdfCatalog.items.map { item ->
            val dlState = states[item.id] ?: DownloadState.Idle
            val localFile = files[item.id]
            val isDownloaded = localFile != null
            val isDownloading = dlState is DownloadState.Downloading
            val progress = (dlState as? DownloadState.Downloading)?.progress ?: 0f
            val err = (dlState as? DownloadState.Error)?.let {
                if (it.integrityFailure) getApplication<Application>().getString(R.string.sample_pdf_integrity_error) else it.message
            }

            SamplePdfItemState(
                item = item,
                isDownloaded = isDownloaded,
                isDownloading = isDownloading,
                progress = progress,
                errorMessage = err,
                localFile = localFile
            )
        }.filter { lang == "ALL" || it.item.languageCode == lang }

        val sortedItems = mappedItems.sortedWith { a, b ->
            sortOrder.comparator().compare(a.item, b.item)
        }

        SamplePdfUiState(
            items = sortedItems,
            selectedLanguage = lang,
            availableLanguages = listOf("ALL") + allLangs,
            cellularDialogItem = dialogItem,
            sortOrder = sortOrder
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SamplePdfUiState()
    )

    private fun getDefaultLanguage(): String {
        return SamplePdfLanguages.defaultCode(Locale.getDefault())
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun toggleSortOrder() {
        val newOrder = if (_sortOrder.value == SamplePdfSortOrder.ASCENDING) {
            SamplePdfSortOrder.DESCENDING
        } else {
            SamplePdfSortOrder.ASCENDING
        }
        _sortOrder.value = newOrder
        PreferencesManager.setSamplePdfSortOrder(getApplication(), newOrder)
    }

    fun requestDownload(item: SamplePdfItem) {
        if (repository.isOnCellularNetwork()) {
            _cellularDialogItem.value = item
        } else {
            startDownload(item)
        }
    }

    fun confirmCellularDownload(item: SamplePdfItem) {
        _cellularDialogItem.value = null
        startDownload(item)
    }

    fun dismissCellularDialog() {
        _cellularDialogItem.value = null
    }

    fun startDownload(item: SamplePdfItem) {
        AppLogger.log(getApplication(), "INFO", "Started downloading sample PDF: ${item.title}")
        viewModelScope.launch {
            repository.downloadPdf(item).collect { state ->
                _downloadStates.value = _downloadStates.value + (item.id to state)
                when (state) {
                    is DownloadState.Success -> {
                        AppLogger.log(getApplication(), "INFO", "Successfully downloaded sample PDF: ${item.title} (${item.fileName})")
                        _refreshTrigger.value++
                    }
                    is DownloadState.Error -> {
                        AppLogger.log(getApplication(), "ERROR", "Failed to download sample PDF ${item.title}: ${state.message}")
                        _refreshTrigger.value++
                    }
                    else -> {}
                }
            }
        }
    }

    fun openDownloadedPdf(item: SamplePdfItem, onOpen: (File) -> Unit) {
        viewModelScope.launch {
            // Recheck immediately before opening, even if the list was already shown.
            val file = withContext(Dispatchers.IO) { repository.getDownloadedFile(item) }
            if (file != null) {
                onOpen(file)
            } else {
                _downloadStates.value = _downloadStates.value + (item.id to DownloadState.Error(
                    getApplication<Application>().getString(R.string.sample_pdf_integrity_error), true
                ))
                _refreshTrigger.value++
            }
        }
    }

    fun deletePdf(item: SamplePdfItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePdf(item)
            AppLogger.log(getApplication(), "INFO", "Deleted sample PDF: ${item.title} (${item.fileName})")
            _downloadStates.value = _downloadStates.value - item.id
            _refreshTrigger.value++
        }
    }
}
