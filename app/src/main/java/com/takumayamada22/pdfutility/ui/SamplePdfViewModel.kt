package com.takumayamada22.pdfutility.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.takumayamada22.pdfutility.data.DownloadState
import com.takumayamada22.pdfutility.data.SamplePdfCatalog
import com.takumayamada22.pdfutility.data.SamplePdfItem
import com.takumayamada22.pdfutility.data.SamplePdfItemState
import com.takumayamada22.pdfutility.data.SamplePdfRepository
import com.takumayamada22.pdfutility.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class SamplePdfUiState(
    val items: List<SamplePdfItemState> = emptyList(),
    val selectedLanguage: String = "en",
    val availableLanguages: List<String> = emptyList(),
    val cellularDialogItem: SamplePdfItem? = null
)

class SamplePdfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SamplePdfRepository(application)

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    private val _selectedLanguage = MutableStateFlow(getDefaultLanguage())
    private val _cellularDialogItem = MutableStateFlow<SamplePdfItem?>(null)
    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<SamplePdfUiState> = combine(
        _downloadStates,
        _selectedLanguage,
        _cellularDialogItem,
        _refreshTrigger
    ) { states, lang, dialogItem, _ ->
        val allLangs = SamplePdfCatalog.items.map { it.languageCode }.distinct()
        val filteredItems = SamplePdfCatalog.items.map { item ->
            val dlState = states[item.id] ?: DownloadState.Idle
            val isDownloaded = repository.isDownloaded(item.fileName)
            val isDownloading = dlState is DownloadState.Downloading
            val progress = (dlState as? DownloadState.Downloading)?.progress ?: 0f
            val err = (dlState as? DownloadState.Error)?.message
            val localFile = if (isDownloaded) repository.getLocalFile(item.fileName) else null

            SamplePdfItemState(
                item = item,
                isDownloaded = isDownloaded,
                isDownloading = isDownloading,
                progress = progress,
                errorMessage = err,
                localFile = localFile
            )
        }.filter { lang == "ALL" || it.item.languageCode == lang }

        SamplePdfUiState(
            items = filteredItems,
            selectedLanguage = lang,
            availableLanguages = listOf("ALL") + allLangs,
            cellularDialogItem = dialogItem
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SamplePdfUiState()
    )

    private fun getDefaultLanguage(): String {
        val deviceLang = Locale.getDefault().language
        val available = SamplePdfCatalog.items.map { it.languageCode }.distinct()
        return if (available.contains(deviceLang)) deviceLang else "en"
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
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

    fun deletePdf(item: SamplePdfItem) {
        viewModelScope.launch {
            repository.deletePdf(item.fileName)
            AppLogger.log(getApplication(), "INFO", "Deleted sample PDF: ${item.title} (${item.fileName})")
            _downloadStates.value = _downloadStates.value - item.id
            _refreshTrigger.value++
        }
    }
}
