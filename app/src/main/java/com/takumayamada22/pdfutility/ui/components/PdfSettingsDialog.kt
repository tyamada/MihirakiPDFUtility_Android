package com.takumayamada22.pdfutility.ui.components

import androidx.compose.runtime.Composable
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType

@Composable
fun PdfSettingsDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    when (uiState.activeSettingsDialog) {
        SettingsDialogType.PROPERTY -> PdfPropertyDialog(viewModel, uiState)
        SettingsDialogType.PASSWORD -> PdfPasswordDialog(viewModel, uiState)
        SettingsDialogType.OPTIONS -> PdfOptionsDialog(viewModel, uiState)
        SettingsDialogType.VERSION -> PdfVersionDialog(viewModel)
        SettingsDialogType.SUPPORT -> PdfSupportDialog(viewModel)
        SettingsDialogType.HELP -> PdfHelpDialog(viewModel)
        SettingsDialogType.DIAGNOSTICS -> PdfDiagnosticsDialog(viewModel)
        SettingsDialogType.REPORT_ISSUE -> PdfReportIssueDialog(viewModel)
        null -> {}
    }
}
