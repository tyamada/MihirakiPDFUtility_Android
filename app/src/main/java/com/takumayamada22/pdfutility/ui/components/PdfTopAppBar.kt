package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.billing.TipTier
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfTopAppBar(
    uiState: PdfUiState,
    viewModel: PdfViewModel,
    onOpen: () -> Unit,
    onAppend: () -> Unit,
    onSave: () -> Unit,
    onExport: () -> Unit,
    onSplitClick: () -> Unit,
    onCropClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = uiState.fileName ?: stringResource(R.string.app_name),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            IconButton(onClick = { viewModel.undo() }, enabled = uiState.canUndo) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.action_undo))
            }
            IconButton(onClick = { viewModel.redo() }, enabled = uiState.canRedo) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = stringResource(R.string.action_redo))
            }

            if (uiState.selectedIndices.isEmpty()) {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Default.FileOpen, contentDescription = stringResource(R.string.action_open))
                }
                if (uiState.pages.isNotEmpty()) {
                    IconButton(onClick = onAppend) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_pdf))
                    }
                    if (uiState.isMihirakiView) {
                        IconButton(onClick = { viewModel.toggleRtl() }) {
                            Icon(
                                if (uiState.isRtl) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.action_toggle_rtl)
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.toggleMihirakiView() }) {
                        Icon(
                            if (uiState.isMihirakiView) Icons.Default.GridView else Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = stringResource(R.string.action_toggle_view)
                        )
                    }
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.action_save))
                    }
                }

                val purchasedTiers by viewModel.billing.purchasedTiers.collectAsState()
                val heartColor = when {
                    purchasedTiers.contains(TipTier.GOLD) -> Color(0xFFFFD700) // Gold (1,000 yen)
                    purchasedTiers.contains(TipTier.SILVER) -> Color(0xFFC0C0C0) // Silver (500 yen)
                    purchasedTiers.contains(TipTier.BRONZE) -> Color(0xFFCD7F32) // Bronze (100 yen)
                    else -> Color.Black
                }
                IconButton(onClick = { viewModel.openSettingsDialog(SettingsDialogType.SUPPORT) }) {
                    Text(
                        text = "❤️",
                        color = heartColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Box {
                    IconButton(onClick = { viewModel.toggleSettingsMenu() }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                    DropdownMenu(
                        expanded = uiState.showSettingsMenu,
                        onDismissRequest = { viewModel.toggleSettingsMenu() }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_property)) },
                            onClick = { viewModel.openSettingsDialog(SettingsDialogType.PROPERTY) },
                            enabled = uiState.pages.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_password)) },
                            onClick = { viewModel.openSettingsDialog(SettingsDialogType.PASSWORD) },
                            enabled = uiState.pages.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_version)) },
                            onClick = { viewModel.openSettingsDialog(SettingsDialogType.VERSION) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_help)) },
                            onClick = { viewModel.openSettingsDialog(SettingsDialogType.HELP) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.support)) },
                            onClick = { viewModel.openSettingsDialog(SettingsDialogType.SUPPORT) }
                        )
                    }
                }
            } else {
                IconButton(onClick = { viewModel.selectAll() }) {
                    Icon(Icons.Default.SelectAll, contentDescription = stringResource(R.string.action_select_all))
                }
                IconButton(onClick = { viewModel.invertSelection() }) {
                    Icon(Icons.Default.Flip, contentDescription = stringResource(R.string.action_invert_selection))
                }
                IconButton(onClick = onExport) {
                    Icon(Icons.Default.Output, contentDescription = stringResource(R.string.action_export_selected))
                }
                IconButton(onClick = onSplitClick) {
                    Icon(Icons.Default.ContentCut, contentDescription = stringResource(R.string.action_split_pages))
                }
                IconButton(onClick = onCropClick) {
                    Icon(Icons.Default.Crop, contentDescription = stringResource(R.string.crop_title))
                }
                IconButton(onClick = { viewModel.rotateSelected(90) }) {
                    Icon(Icons.Default.RotateRight, contentDescription = stringResource(R.string.action_rotate_right))
                }
                IconButton(onClick = { viewModel.insertBlankAfterSelected() }) {
                    Icon(Icons.Default.AddBox, contentDescription = stringResource(R.string.action_insert_blank))
                }
                IconButton(onClick = { viewModel.moveSelected(-1) }) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.action_move_up))
                }
                IconButton(onClick = { viewModel.moveSelected(1) }) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.action_move_down))
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                }
                IconButton(onClick = { viewModel.clearSelection() }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear_selection))
                }
            }
        }
    )
}
