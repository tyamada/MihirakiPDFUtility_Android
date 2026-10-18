package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.ButtonDisplayType
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
    text: String,
    displayType: ButtonDisplayType,
    tint: Color = LocalContentColor.current
) {
    when (displayType) {
        ButtonDisplayType.ICON_ONLY -> {
            IconButton(onClick = onClick, enabled = enabled) {
                CompositionLocalProvider(LocalContentColor provides tint) {
                    icon()
                }
            }
        }
        ButtonDisplayType.TEXT_ONLY -> {
            TextButton(onClick = onClick, enabled = enabled) {
                Text(text)
            }
        }
        ButtonDisplayType.ICON_AND_TEXT -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.padding(2.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = tint
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    icon()
                    Text(text)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PdfTopAppBar(
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
    onDeleteClick: () -> Unit
) {
    val displayType = uiState.buttonDisplayType

    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = uiState.fileName ?: stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (uiState.selectedIndices.isEmpty()) {
                    ActionButton(
                        onClick = onOpen,
                        icon = { Icon(Icons.Default.FileOpen, contentDescription = stringResource(R.string.action_open)) },
                        text = stringResource(R.string.action_open),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = onOpenFolder,
                        icon = { Icon(Icons.Default.FolderOpen, contentDescription = stringResource(R.string.menu_open_folder)) },
                        text = stringResource(R.string.menu_open_folder),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = onOpenSamplePdf,
                        icon = { Icon(Icons.Default.Download, contentDescription = stringResource(R.string.sample_pdf)) },
                        text = stringResource(R.string.sample_pdf),
                        displayType = displayType
                    )
                    if (uiState.pages.isNotEmpty()) {
                        ActionButton(
                            onClick = onSave,
                            icon = { Icon(Icons.Default.Save, contentDescription = stringResource(R.string.action_save)) },
                            text = stringResource(R.string.action_save),
                            displayType = displayType
                        )
                        ActionButton(
                            onClick = onAppend,
                            icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_pdf)) },
                            text = stringResource(R.string.action_add_pdf),
                            displayType = displayType
                        )
                        ActionButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo,
                            icon = { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.action_undo)) },
                            text = stringResource(R.string.action_undo),
                            displayType = displayType
                        )
                        ActionButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo,
                            icon = { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = stringResource(R.string.action_redo)) },
                            text = stringResource(R.string.action_redo),
                            displayType = displayType
                        )
                        ActionButton(
                            onClick = { viewModel.toggleMihirakiView() },
                            icon = {
                                Icon(
                                    Icons.Default.GridView,
                                    contentDescription = stringResource(R.string.action_toggle_view)
                                )
                            },
                            text = stringResource(R.string.action_toggle_view),
                            displayType = displayType
                        )
                        ActionButton(
                            onClick = { viewModel.openSettingsDialog(SettingsDialogType.PROPERTY) },
                            icon = { Icon(Icons.Default.Description, contentDescription = stringResource(R.string.action_property)) },
                            text = stringResource(R.string.action_property),
                            displayType = displayType
                        )
                        ActionButton(
                            onClick = { viewModel.setRightBinding() },
                            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = stringResource(R.string.action_right_binding)) },
                            text = stringResource(R.string.action_right_binding),
                            displayType = displayType
                        )
                        ActionButton(
                            onClick = { viewModel.setRightBindingWithCover() },
                            icon = { Icon(Icons.Default.AutoStories, contentDescription = stringResource(R.string.action_right_binding_cover)) },
                            text = stringResource(R.string.action_right_binding_cover),
                            displayType = displayType
                        )
                        if (uiState.isMihirakiView) {
                            ActionButton(
                                onClick = { viewModel.toggleRtl() },
                                icon = {
                                    Icon(
                                        if (uiState.isRtl) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = stringResource(R.string.action_toggle_rtl)
                                    )
                                },
                                text = stringResource(R.string.action_toggle_rtl),
                                displayType = displayType
                            )
                        }
                    }

                    Box {
                        ActionButton(
                            onClick = { viewModel.toggleSettingsMenu() },
                            icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.action_settings)) },
                            text = stringResource(R.string.action_settings),
                            displayType = displayType
                        )
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
                                text = { Text(stringResource(R.string.settings_options)) },
                                onClick = { viewModel.openSettingsDialog(SettingsDialogType.OPTIONS) }
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
                                text = { Text(stringResource(R.string.settings_diagnostics)) },
                                onClick = { viewModel.openSettingsDialog(SettingsDialogType.DIAGNOSTICS) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.support)) },
                                onClick = { viewModel.openSettingsDialog(SettingsDialogType.SUPPORT) }
                            )
                        }
                    }
                } else {
                    ActionButton(
                        onClick = { viewModel.selectAll() },
                        icon = { Icon(Icons.Default.SelectAll, contentDescription = stringResource(R.string.action_select_all)) },
                        text = stringResource(R.string.action_select_all),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = { viewModel.clearSelection() },
                        icon = { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear_selection)) },
                        text = stringResource(R.string.action_clear_selection),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = { viewModel.invertSelection() },
                        icon = { Icon(Icons.Default.Flip, contentDescription = stringResource(R.string.action_invert_selection)) },
                        text = stringResource(R.string.action_invert_selection),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = onExport,
                        icon = { Icon(Icons.Default.Output, contentDescription = stringResource(R.string.action_export_selected)) },
                        text = stringResource(R.string.action_export_selected),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = onDeleteClick,
                        icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete)) },
                        text = stringResource(R.string.action_delete),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = { viewModel.insertBlankAfterSelected() },
                        icon = { Icon(Icons.Default.AddBox, contentDescription = stringResource(R.string.action_insert_blank)) },
                        text = stringResource(R.string.action_insert_blank),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = onSplitClick,
                        icon = { Icon(Icons.Default.ContentCut, contentDescription = stringResource(R.string.action_split_pages)) },
                        text = stringResource(R.string.action_split_pages),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = onCropClick,
                        icon = { Icon(Icons.Default.Crop, contentDescription = stringResource(R.string.crop_title)) },
                        text = stringResource(R.string.crop_title),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = { viewModel.moveSelected(-1) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_move_up)) },
                        text = stringResource(R.string.action_move_up),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = { viewModel.moveSelected(1) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.action_move_down)) },
                        text = stringResource(R.string.action_move_down),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = { viewModel.rotateSelected(-90) },
                        icon = { Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = stringResource(R.string.action_rotate_left)) },
                        text = stringResource(R.string.action_rotate_left),
                        displayType = displayType
                    )
                    ActionButton(
                        onClick = { viewModel.rotateSelected(90) },
                        icon = { Icon(Icons.Default.RotateRight, contentDescription = stringResource(R.string.action_rotate_right)) },
                        text = stringResource(R.string.action_rotate_right),
                        displayType = displayType
                    )
                }
            }
        }
    }
}
