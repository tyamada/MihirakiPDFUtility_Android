package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobileBottomSheetMenu(
    uiState: PdfUiState,
    viewModel: PdfViewModel,
    onOpen: () -> Unit,
    onOpenFolder: () -> Unit,
    onOpenSamplePdf: () -> Unit,
    onAppend: () -> Unit,
    onSave: () -> Unit,
    onExport: () -> Unit,
    onSplitClick: () -> Unit,
    onCropClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var sheetVisible by remember { mutableStateOf(value = false) }
    val sheetState = rememberModalBottomSheetState()

    Box {
        IconButton(onClick = { sheetVisible = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_settings))
        }
    }

    if (sheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { sheetVisible = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                // File Section
                MenuSectionHeader(stringResource(R.string.menu_file))
                MenuItemRow(Icons.Default.FileOpen, stringResource(R.string.action_open), true) { sheetVisible = false; onOpen() }
                MenuItemRow(Icons.Default.FolderOpen, stringResource(R.string.menu_open_folder), true) { sheetVisible = false; onOpenFolder() }
                MenuItemRow(Icons.Default.Download, stringResource(R.string.sample_pdf), true) { sheetVisible = false; onOpenSamplePdf() }
                MenuItemRow(Icons.Default.Save, stringResource(R.string.action_save), uiState.pages.isNotEmpty()) { sheetVisible = false; onSave() }
                MenuItemRow(Icons.Default.Output, stringResource(R.string.action_export_selected), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; onExport() }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Edit Section
                MenuSectionHeader(stringResource(R.string.menu_edit))
                MenuItemRow(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.action_undo), uiState.canUndo) { sheetVisible = false; viewModel.undo() }
                MenuItemRow(Icons.AutoMirrored.Filled.Redo, stringResource(R.string.action_redo), uiState.canRedo) { sheetVisible = false; viewModel.redo() }
                MenuItemRow(Icons.Default.SelectAll, stringResource(R.string.action_select_all), uiState.pages.isNotEmpty()) { sheetVisible = false; viewModel.selectAll() }
                MenuItemRow(Icons.Default.Close, stringResource(R.string.action_clear_selection), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; viewModel.clearSelection() }
                MenuItemRow(Icons.Default.Flip, stringResource(R.string.action_invert_selection), uiState.pages.isNotEmpty()) { sheetVisible = false; viewModel.invertSelection() }
                MenuItemRow(Icons.Default.Delete, stringResource(R.string.action_delete), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; onDeleteClick() }
                MenuItemRow(Icons.Default.ContentCut, stringResource(R.string.action_split_pages), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; onSplitClick() }
                MenuItemRow(Icons.Default.Crop, stringResource(R.string.crop_title), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; onCropClick() }
                MenuItemRow(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_move_up), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; viewModel.moveSelected(-1) }
                MenuItemRow(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.action_move_down), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; viewModel.moveSelected(1) }
                MenuItemRow(Icons.AutoMirrored.Filled.RotateLeft, stringResource(R.string.action_rotate_left), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; viewModel.rotateSelected(-90) }
                MenuItemRow(Icons.AutoMirrored.Filled.RotateRight, stringResource(R.string.action_rotate_right), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; viewModel.rotateSelected(90) }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Insert Section
                MenuSectionHeader(stringResource(R.string.menu_insert))
                MenuItemRow(Icons.Default.Add, stringResource(R.string.action_add_pdf), true) { sheetVisible = false; onAppend() }
                MenuItemRow(Icons.Default.AddBox, stringResource(R.string.action_insert_blank), uiState.selectedIndices.isNotEmpty()) { sheetVisible = false; viewModel.insertBlankAfterSelected() }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // View & Options Section
                MenuSectionHeader("${stringResource(R.string.menu_view)} / ${stringResource(R.string.menu_options)}")
                MenuItemRow(Icons.Default.GridView, stringResource(R.string.action_toggle_view), uiState.pages.isNotEmpty()) { sheetVisible = false; viewModel.toggleMihirakiView() }
                MenuItemRow(Icons.Default.Description, stringResource(R.string.action_property), uiState.pages.isNotEmpty()) { sheetVisible = false; viewModel.openSettingsDialog(SettingsDialogType.PROPERTY) }
                MenuItemRow(Icons.Default.Settings, stringResource(R.string.settings_options), true) { sheetVisible = false; viewModel.openSettingsDialog(SettingsDialogType.OPTIONS) }
                MenuItemRow(Icons.Default.Lock, stringResource(R.string.settings_password), uiState.pages.isNotEmpty()) { sheetVisible = false; viewModel.openSettingsDialog(SettingsDialogType.PASSWORD) }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Help Section
                MenuSectionHeader(stringResource(R.string.menu_help))
                MenuItemRow(Icons.Default.Info, stringResource(R.string.settings_version), true) { sheetVisible = false; viewModel.openSettingsDialog(SettingsDialogType.VERSION) }
                MenuItemRow(Icons.AutoMirrored.Filled.Help, stringResource(R.string.settings_help), true) { sheetVisible = false; viewModel.openSettingsDialog(SettingsDialogType.HELP) }
                MenuItemRow(Icons.Default.Security, stringResource(R.string.privacy_policy_title), true) { sheetVisible = false; viewModel.openSettingsDialog(SettingsDialogType.PRIVACY_POLICY) }
                MenuItemRow(Icons.Default.BugReport, stringResource(R.string.settings_diagnostics), true) { sheetVisible = false; viewModel.openSettingsDialog(SettingsDialogType.DIAGNOSTICS) }
                MenuItemRow(Icons.Default.Favorite, stringResource(R.string.support), true) { sheetVisible = false; viewModel.openSettingsDialog(SettingsDialogType.SUPPORT) }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MenuSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.38f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        )
    }
}
