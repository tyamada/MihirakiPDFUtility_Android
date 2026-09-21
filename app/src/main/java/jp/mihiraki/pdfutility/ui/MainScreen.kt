package jp.mihiraki.pdfutility.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.mihiraki.pdfutility.R
import jp.mihiraki.pdfutility.ui.components.DeleteConfirmationDialog
import jp.mihiraki.pdfutility.ui.components.PasswordDialog
import jp.mihiraki.pdfutility.ui.components.PdfPageGrid
import jp.mihiraki.pdfutility.ui.components.PdfPagePreview
import jp.mihiraki.pdfutility.ui.components.PdfSettingsDialog
import jp.mihiraki.pdfutility.ui.components.PdfTopAppBar
import jp.mihiraki.pdfutility.ui.components.SplitDialog
import jp.mihiraki.pdfutility.ui.components.CropDialog

@Composable
fun MainScreen(viewModel: PdfViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }
    var showCropDialog by remember { mutableStateOf(false) }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.loadPdf(it) }
    }
    val appendLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.appendPdf(it) }
    }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { viewModel.savePdf(it) }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { viewModel.exportSelected(it) }
    }

    val errorText = uiState.errorResId?.let { stringResource(it) } ?: uiState.errorMessage

    LaunchedEffect(errorText) {
        errorText?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.onKeyEvent { event ->
            if (uiState.previewPageIndex != null) {
                if (event.type == KeyEventType.KeyDown && event.key == Key.Back) {
                    viewModel.closePreview()
                    true
                } else false
            } else if (event.type == KeyEventType.KeyDown && event.isCtrlPressed) {
                when (event.key) {
                    Key.O -> {
                        openLauncher.launch("application/pdf")
                        true
                    }
                    Key.S -> {
                        saveLauncher.launch("edited_${uiState.fileName ?: "document"}.pdf")
                        true
                    }
                    Key.A -> {
                        viewModel.selectAll()
                        true
                    }
                    Key.Z -> {
                        if (event.isShiftPressed) viewModel.redo() else viewModel.undo()
                        true
                    }
                    Key.Y -> {
                        viewModel.redo()
                        true
                    }
                    else -> false
                }
            } else if (event.type == KeyEventType.KeyDown && event.key == Key.Delete) {
                if (uiState.selectedIndices.isNotEmpty()) {
                    showDeleteConfirmation = true
                }
                true
            } else {
                false
            }
        },
        topBar = {
            PdfTopAppBar(
                uiState = uiState,
                viewModel = viewModel,
                onOpen = { openLauncher.launch("application/pdf") },
                onAppend = { appendLauncher.launch("application/pdf") },
                onSave = { saveLauncher.launch("edited_${uiState.fileName ?: "document"}.pdf") },
                onExport = { exportLauncher.launch("selected_pages.pdf") },
                onSplitClick = { showSplitDialog = true },
                onCropClick = { showCropDialog = true },
                onDeleteClick = { showDeleteConfirmation = true }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            PdfPageGrid(uiState = uiState, viewModel = viewModel)
        }
    }

    if (uiState.previewPageIndex != null) {
        PdfPagePreview(uiState = uiState, viewModel = viewModel)
    }

    PasswordDialog(viewModel = viewModel, uiState = uiState)
    
    PdfSettingsDialog(viewModel = viewModel, uiState = uiState)

    SplitDialog(
        show = showSplitDialog,
        onDismiss = { showSplitDialog = false },
        onConfirm = { direction ->
            viewModel.splitSelectedPages(direction)
            showSplitDialog = false
        }
    )

    CropDialog(
        show = showCropDialog,
        onDismiss = { showCropDialog = false },
        onConfirm = { percent ->
            viewModel.cropSelectedPages(percent)
            showCropDialog = false
        }
    )

    DeleteConfirmationDialog(
        show = showDeleteConfirmation,
        onDismiss = { showDeleteConfirmation = false },
        selectedCount = uiState.selectedIndices.size,
        onConfirm = {
            viewModel.deleteSelected()
            showDeleteConfirmation = false
        }
    )
}
