package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.billing.TipTier
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                        IconButton(onClick = { viewModel.openSettingsDialog(SettingsDialogType.PROPERTY) }) {
                            Icon(Icons.Default.Description, contentDescription = stringResource(R.string.action_property))
                        }
                        IconButton(onClick = { viewModel.setRightBinding() }) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = stringResource(R.string.action_right_binding))
                        }
                        IconButton(onClick = { viewModel.setRightBindingWithCover() }) {
                            Icon(Icons.Default.AutoStories, contentDescription = stringResource(R.string.action_right_binding_cover))
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
                        purchasedTiers.contains(TipTier.GOLD) -> Color(0xFFFFD700)
                        purchasedTiers.contains(TipTier.SILVER) -> Color(0xFFC0C0C0)
                        purchasedTiers.contains(TipTier.BRONZE) -> Color(0xFFCD7F32)
                        else -> Color.Black
                    }
                    IconButton(onClick = { viewModel.openSettingsDialog(SettingsDialogType.SUPPORT) }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.support),
                            tint = heartColor
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
        }
    }
}
