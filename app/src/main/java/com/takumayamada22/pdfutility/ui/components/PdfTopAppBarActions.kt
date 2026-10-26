package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.ActionButtonType
import com.takumayamada22.pdfutility.ui.ButtonDisplayType
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType

@Composable
internal fun ActionButton(
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
                colors = ButtonDefaults.buttonColors(contentColor = tint)
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

@Composable
internal fun PdfMenuBar(
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
    var fileMenuExpanded by remember { mutableStateOf(false) }
    var editMenuExpanded by remember { mutableStateOf(false) }
    var insertMenuExpanded by remember { mutableStateOf(false) }
    var viewMenuExpanded by remember { mutableStateOf(false) }
    var optionsMenuExpanded by remember { mutableStateOf(false) }
    var helpMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File Menu
        Box {
            TextButton(onClick = { fileMenuExpanded = true }) {
                Text(stringResource(R.string.menu_file))
            }
            DropdownMenu(expanded = fileMenuExpanded, onDismissRequest = { fileMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_open)) },
                    onClick = { fileMenuExpanded = false; onOpen() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_open_folder)) },
                    onClick = { fileMenuExpanded = false; onOpenFolder() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.sample_pdf)) },
                    onClick = { fileMenuExpanded = false; onOpenSamplePdf() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_save)) },
                    onClick = { fileMenuExpanded = false; onSave() },
                    enabled = uiState.pages.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_export_selected)) },
                    onClick = { fileMenuExpanded = false; onExport() },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
            }
        }

        // Edit Menu
        Box {
            TextButton(onClick = { editMenuExpanded = true }) {
                Text(stringResource(R.string.menu_edit))
            }
            DropdownMenu(expanded = editMenuExpanded, onDismissRequest = { editMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_undo)) },
                    onClick = { editMenuExpanded = false; viewModel.undo() },
                    enabled = uiState.canUndo
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_redo)) },
                    onClick = { editMenuExpanded = false; viewModel.redo() },
                    enabled = uiState.canRedo
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_select_all)) },
                    onClick = { editMenuExpanded = false; viewModel.selectAll() },
                    enabled = uiState.pages.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_clear_selection)) },
                    onClick = { editMenuExpanded = false; viewModel.clearSelection() },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_invert_selection)) },
                    onClick = { editMenuExpanded = false; viewModel.invertSelection() },
                    enabled = uiState.pages.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_delete)) },
                    onClick = { editMenuExpanded = false; onDeleteClick() },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_split_pages)) },
                    onClick = { editMenuExpanded = false; onSplitClick() },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.crop_title)) },
                    onClick = { editMenuExpanded = false; onCropClick() },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_move_up)) },
                    onClick = { editMenuExpanded = false; viewModel.moveSelected(-1) },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_move_down)) },
                    onClick = { editMenuExpanded = false; viewModel.moveSelected(1) },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_rotate_left)) },
                    onClick = { editMenuExpanded = false; viewModel.rotateSelected(-90) },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_rotate_right)) },
                    onClick = { editMenuExpanded = false; viewModel.rotateSelected(90) },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
            }
        }

        // Insert Menu
        Box {
            TextButton(onClick = { insertMenuExpanded = true }) {
                Text(stringResource(R.string.menu_insert))
            }
            DropdownMenu(expanded = insertMenuExpanded, onDismissRequest = { insertMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_add_pdf)) },
                    onClick = { insertMenuExpanded = false; onAppend() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_insert_blank)) },
                    onClick = { insertMenuExpanded = false; viewModel.insertBlankAfterSelected() },
                    enabled = uiState.selectedIndices.isNotEmpty()
                )
            }
        }

        // View Menu
        Box {
            TextButton(onClick = { viewMenuExpanded = true }) {
                Text(stringResource(R.string.menu_view))
            }
            DropdownMenu(expanded = viewMenuExpanded, onDismissRequest = { viewMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_toggle_view)) },
                    onClick = { viewMenuExpanded = false; viewModel.toggleMihirakiView() },
                    enabled = uiState.pages.isNotEmpty()
                )
            }
        }

        // Options Menu
        Box {
            TextButton(onClick = { optionsMenuExpanded = true }) {
                Text(stringResource(R.string.menu_options))
            }
            DropdownMenu(expanded = optionsMenuExpanded, onDismissRequest = { optionsMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_property)) },
                    onClick = { optionsMenuExpanded = false; viewModel.openSettingsDialog(SettingsDialogType.PROPERTY) },
                    enabled = uiState.pages.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_right_binding)) },
                    onClick = { optionsMenuExpanded = false; viewModel.setRightBinding() },
                    enabled = uiState.pages.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_right_binding_cover)) },
                    onClick = { optionsMenuExpanded = false; viewModel.setRightBindingWithCover() },
                    enabled = uiState.pages.isNotEmpty()
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_toggle_rtl)) },
                    onClick = { optionsMenuExpanded = false; viewModel.toggleRtl() },
                    enabled = uiState.pages.isNotEmpty() && uiState.isMihirakiView
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_options)) },
                    onClick = { optionsMenuExpanded = false; viewModel.openSettingsDialog(SettingsDialogType.OPTIONS) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_password)) },
                    onClick = { optionsMenuExpanded = false; viewModel.openSettingsDialog(SettingsDialogType.PASSWORD) },
                    enabled = uiState.pages.isNotEmpty()
                )
            }
        }

        // Help Menu
        Box {
            TextButton(onClick = { helpMenuExpanded = true }) {
                Text(stringResource(R.string.menu_help))
            }
            DropdownMenu(expanded = helpMenuExpanded, onDismissRequest = { helpMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_version)) },
                    onClick = { helpMenuExpanded = false; viewModel.openSettingsDialog(SettingsDialogType.VERSION) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_help)) },
                    onClick = { helpMenuExpanded = false; viewModel.openSettingsDialog(SettingsDialogType.HELP) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_diagnostics)) },
                    onClick = { helpMenuExpanded = false; viewModel.openSettingsDialog(SettingsDialogType.DIAGNOSTICS) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.support)) },
                    onClick = { helpMenuExpanded = false; viewModel.openSettingsDialog(SettingsDialogType.SUPPORT) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.privacy_policy_title)) },
                    onClick = { helpMenuExpanded = false; viewModel.openSettingsDialog(SettingsDialogType.PRIVACY_POLICY) }
                )
            }
        }
    }
}

@Composable
internal fun PdfActionButtons(
    uiState: PdfUiState,
    viewModel: PdfViewModel,
    isBusy: Boolean,
    displayType: ButtonDisplayType,
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
    val visible = uiState.visibleActionButtons

    // 1. OPEN
    if (visible.contains(ActionButtonType.OPEN)) {
        ActionButton(
            onClick = onOpen,
            enabled = !isBusy,
            icon = { Icon(Icons.Default.FileOpen, contentDescription = stringResource(R.string.action_open)) },
            text = stringResource(R.string.action_open),
            displayType = displayType
        )
    }
    // 2. OPEN_FOLDER
    if (visible.contains(ActionButtonType.OPEN_FOLDER)) {
        ActionButton(
            onClick = onOpenFolder,
            enabled = !isBusy,
            icon = { Icon(Icons.Default.FolderOpen, contentDescription = stringResource(R.string.menu_open_folder)) },
            text = stringResource(R.string.menu_open_folder),
            displayType = displayType
        )
    }
    // 3. SAMPLE_PDF
    if (visible.contains(ActionButtonType.SAMPLE_PDF)) {
        ActionButton(
            onClick = onOpenSamplePdf,
            enabled = !isBusy,
            icon = { Icon(Icons.Default.Download, contentDescription = stringResource(R.string.sample_pdf)) },
            text = stringResource(R.string.sample_pdf),
            displayType = displayType
        )
    }
    // 4. SAVE
    if (visible.contains(ActionButtonType.SAVE)) {
        ActionButton(
            onClick = onSave,
            enabled = !isBusy && uiState.pages.isNotEmpty(),
            icon = { Icon(Icons.Default.Save, contentDescription = stringResource(R.string.action_save)) },
            text = stringResource(R.string.action_save),
            displayType = displayType
        )
    }
    // 5. EXPORT
    if (visible.contains(ActionButtonType.EXPORT)) {
        ActionButton(
            onClick = onExport,
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.Default.Output, contentDescription = stringResource(R.string.action_export_selected)) },
            text = stringResource(R.string.action_export_selected),
            displayType = displayType
        )
    }
    // 6. UNDO
    if (visible.contains(ActionButtonType.UNDO)) {
        ActionButton(
            onClick = { viewModel.undo() },
            enabled = !isBusy && uiState.canUndo,
            icon = { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.action_undo)) },
            text = stringResource(R.string.action_undo),
            displayType = displayType
        )
    }
    // 7. REDO
    if (visible.contains(ActionButtonType.REDO)) {
        ActionButton(
            onClick = { viewModel.redo() },
            enabled = !isBusy && uiState.canRedo,
            icon = { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = stringResource(R.string.action_redo)) },
            text = stringResource(R.string.action_redo),
            displayType = displayType
        )
    }
    // 8. SELECT_ALL
    if (visible.contains(ActionButtonType.SELECT_ALL)) {
        ActionButton(
            onClick = { viewModel.selectAll() },
            enabled = !isBusy && uiState.pages.isNotEmpty(),
            icon = { Icon(Icons.Default.SelectAll, contentDescription = stringResource(R.string.action_select_all)) },
            text = stringResource(R.string.action_select_all),
            displayType = displayType
        )
    }
    // 9. CLEAR_SELECTION
    if (visible.contains(ActionButtonType.CLEAR_SELECTION)) {
        ActionButton(
            onClick = { viewModel.clearSelection() },
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear_selection)) },
            text = stringResource(R.string.action_clear_selection),
            displayType = displayType
        )
    }
    // 10. INVERT_SELECTION
    if (visible.contains(ActionButtonType.INVERT_SELECTION)) {
        ActionButton(
            onClick = { viewModel.invertSelection() },
            enabled = !isBusy && uiState.pages.isNotEmpty(),
            icon = { Icon(Icons.Default.Flip, contentDescription = stringResource(R.string.action_invert_selection)) },
            text = stringResource(R.string.action_invert_selection),
            displayType = displayType
        )
    }
    // 11. DELETE
    if (visible.contains(ActionButtonType.DELETE)) {
        ActionButton(
            onClick = onDeleteClick,
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete)) },
            text = stringResource(R.string.action_delete),
            displayType = displayType
        )
    }
    // 12. SPLIT
    if (visible.contains(ActionButtonType.SPLIT)) {
        ActionButton(
            onClick = onSplitClick,
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.Default.ContentCut, contentDescription = stringResource(R.string.action_split_pages)) },
            text = stringResource(R.string.action_split_pages),
            displayType = displayType
        )
    }
    // 13. CROP
    if (visible.contains(ActionButtonType.CROP)) {
        ActionButton(
            onClick = onCropClick,
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.Default.Crop, contentDescription = stringResource(R.string.crop_title)) },
            text = stringResource(R.string.crop_title),
            displayType = displayType
        )
    }
    // 14. MOVE_UP
    if (visible.contains(ActionButtonType.MOVE_UP)) {
        ActionButton(
            onClick = { viewModel.moveSelected(-1) },
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_move_up)) },
            text = stringResource(R.string.action_move_up),
            displayType = displayType
        )
    }
    // 15. MOVE_DOWN
    if (visible.contains(ActionButtonType.MOVE_DOWN)) {
        ActionButton(
            onClick = { viewModel.moveSelected(1) },
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.action_move_down)) },
            text = stringResource(R.string.action_move_down),
            displayType = displayType
        )
    }
    // 16. ROTATE_LEFT
    if (visible.contains(ActionButtonType.ROTATE_LEFT)) {
        ActionButton(
            onClick = { viewModel.rotateSelected(-90) },
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = stringResource(R.string.action_rotate_left)) },
            text = stringResource(R.string.action_rotate_left),
            displayType = displayType
        )
    }
    // 17. ROTATE_RIGHT
    if (visible.contains(ActionButtonType.ROTATE_RIGHT)) {
        ActionButton(
            onClick = { viewModel.rotateSelected(90) },
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.Default.RotateRight, contentDescription = stringResource(R.string.action_rotate_right)) },
            text = stringResource(R.string.action_rotate_right),
            displayType = displayType
        )
    }
    // 18. APPEND
    if (visible.contains(ActionButtonType.APPEND)) {
        ActionButton(
            onClick = onAppend,
            enabled = !isBusy,
            icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_pdf)) },
            text = stringResource(R.string.action_add_pdf),
            displayType = displayType
        )
    }
    // 19. INSERT_BLANK
    if (visible.contains(ActionButtonType.INSERT_BLANK)) {
        ActionButton(
            onClick = { viewModel.insertBlankAfterSelected() },
            enabled = !isBusy && uiState.selectedIndices.isNotEmpty(),
            icon = { Icon(Icons.Default.AddBox, contentDescription = stringResource(R.string.action_insert_blank)) },
            text = stringResource(R.string.action_insert_blank),
            displayType = displayType
        )
    }
    // 20. TOGGLE_VIEW
    if (visible.contains(ActionButtonType.TOGGLE_VIEW)) {
        ActionButton(
            onClick = { viewModel.toggleMihirakiView() },
            enabled = !isBusy && uiState.pages.isNotEmpty(),
            icon = { Icon(Icons.Default.GridView, contentDescription = stringResource(R.string.action_toggle_view)) },
            text = stringResource(R.string.action_toggle_view),
            displayType = displayType
        )
    }
    // 21. PROPERTY
    if (visible.contains(ActionButtonType.PROPERTY)) {
        ActionButton(
            onClick = { viewModel.openSettingsDialog(SettingsDialogType.PROPERTY) },
            enabled = !isBusy && uiState.pages.isNotEmpty(),
            icon = { Icon(Icons.Default.Description, contentDescription = stringResource(R.string.action_property)) },
            text = stringResource(R.string.action_property),
            displayType = displayType
        )
    }
    // 22. RIGHT_BINDING
    if (visible.contains(ActionButtonType.RIGHT_BINDING)) {
        ActionButton(
            onClick = { viewModel.setRightBinding() },
            enabled = !isBusy && uiState.pages.isNotEmpty(),
            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = stringResource(R.string.action_right_binding)) },
            text = stringResource(R.string.action_right_binding),
            displayType = displayType
        )
    }
    // 23. RIGHT_BINDING_COVER
    if (visible.contains(ActionButtonType.RIGHT_BINDING_COVER)) {
        ActionButton(
            onClick = { viewModel.setRightBindingWithCover() },
            enabled = !isBusy && uiState.pages.isNotEmpty(),
            icon = { Icon(Icons.Default.AutoStories, contentDescription = stringResource(R.string.action_right_binding_cover)) },
            text = stringResource(R.string.action_right_binding_cover),
            displayType = displayType
        )
    }
    // 24. TOGGLE_RTL
    if (visible.contains(ActionButtonType.TOGGLE_RTL)) {
        ActionButton(
            onClick = { viewModel.toggleRtl() },
            enabled = !isBusy && uiState.pages.isNotEmpty() && uiState.isMihirakiView,
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

@Composable
internal fun MobileOverflowMenu(
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
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_settings))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("--- ${stringResource(R.string.menu_file)} ---") }, onClick = {}, enabled = false)
            DropdownMenuItem(text = { Text(stringResource(R.string.action_open)) }, onClick = { expanded = false; onOpen() })
            DropdownMenuItem(text = { Text(stringResource(R.string.menu_open_folder)) }, onClick = { expanded = false; onOpenFolder() })
            DropdownMenuItem(text = { Text(stringResource(R.string.sample_pdf)) }, onClick = { expanded = false; onOpenSamplePdf() })
            DropdownMenuItem(text = { Text(stringResource(R.string.action_save)) }, onClick = { expanded = false; onSave() }, enabled = uiState.pages.isNotEmpty())
            DropdownMenuItem(text = { Text(stringResource(R.string.action_export_selected)) }, onClick = { expanded = false; onExport() }, enabled = uiState.selectedIndices.isNotEmpty())

            HorizontalDivider()

            DropdownMenuItem(text = { Text("--- ${stringResource(R.string.menu_edit)} ---") }, onClick = {}, enabled = false)
            DropdownMenuItem(text = { Text(stringResource(R.string.action_undo)) }, onClick = { expanded = false; viewModel.undo() }, enabled = uiState.canUndo)
            DropdownMenuItem(text = { Text(stringResource(R.string.action_redo)) }, onClick = { expanded = false; viewModel.redo() }, enabled = uiState.canRedo)
            DropdownMenuItem(text = { Text(stringResource(R.string.action_select_all)) }, onClick = { expanded = false; viewModel.selectAll() }, enabled = uiState.pages.isNotEmpty())
            DropdownMenuItem(text = { Text(stringResource(R.string.action_clear_selection)) }, onClick = { expanded = false; viewModel.clearSelection() }, enabled = uiState.selectedIndices.isNotEmpty())
            DropdownMenuItem(text = { Text(stringResource(R.string.action_invert_selection)) }, onClick = { expanded = false; viewModel.invertSelection() }, enabled = uiState.pages.isNotEmpty())
            DropdownMenuItem(text = { Text(stringResource(R.string.action_delete)) }, onClick = { expanded = false; onDeleteClick() }, enabled = uiState.selectedIndices.isNotEmpty())
            DropdownMenuItem(text = { Text(stringResource(R.string.action_split_pages)) }, onClick = { expanded = false; onSplitClick() }, enabled = uiState.selectedIndices.isNotEmpty())
            DropdownMenuItem(text = { Text(stringResource(R.string.crop_title)) }, onClick = { expanded = false; onCropClick() }, enabled = uiState.selectedIndices.isNotEmpty())

            HorizontalDivider()

            DropdownMenuItem(text = { Text("--- ${stringResource(R.string.menu_insert)} ---") }, onClick = {}, enabled = false)
            DropdownMenuItem(text = { Text(stringResource(R.string.action_add_pdf)) }, onClick = { expanded = false; onAppend() })
            DropdownMenuItem(text = { Text(stringResource(R.string.action_insert_blank)) }, onClick = { expanded = false; viewModel.insertBlankAfterSelected() }, enabled = uiState.selectedIndices.isNotEmpty())

            HorizontalDivider()

            DropdownMenuItem(text = { Text("--- ${stringResource(R.string.menu_view)} / ${stringResource(R.string.menu_options)} ---") }, onClick = {}, enabled = false)
            DropdownMenuItem(text = { Text(stringResource(R.string.action_toggle_view)) }, onClick = { expanded = false; viewModel.toggleMihirakiView() }, enabled = uiState.pages.isNotEmpty())
            DropdownMenuItem(text = { Text(stringResource(R.string.action_property)) }, onClick = { expanded = false; viewModel.openSettingsDialog(SettingsDialogType.PROPERTY) }, enabled = uiState.pages.isNotEmpty())
            DropdownMenuItem(text = { Text(stringResource(R.string.settings_options)) }, onClick = { expanded = false; viewModel.openSettingsDialog(SettingsDialogType.OPTIONS) })
            DropdownMenuItem(text = { Text(stringResource(R.string.settings_password)) }, onClick = { expanded = false; viewModel.openSettingsDialog(SettingsDialogType.PASSWORD) }, enabled = uiState.pages.isNotEmpty())

            HorizontalDivider()

            DropdownMenuItem(text = { Text("--- ${stringResource(R.string.menu_help)} ---") }, onClick = {}, enabled = false)
            DropdownMenuItem(text = { Text(stringResource(R.string.settings_version)) }, onClick = { expanded = false; viewModel.openSettingsDialog(SettingsDialogType.VERSION) })
            DropdownMenuItem(text = { Text(stringResource(R.string.settings_help)) }, onClick = { expanded = false; viewModel.openSettingsDialog(SettingsDialogType.HELP) })
            DropdownMenuItem(text = { Text(stringResource(R.string.privacy_policy_title)) }, onClick = { expanded = false; viewModel.openSettingsDialog(SettingsDialogType.PRIVACY_POLICY) })
            DropdownMenuItem(text = { Text(stringResource(R.string.settings_diagnostics)) }, onClick = { expanded = false; viewModel.openSettingsDialog(SettingsDialogType.DIAGNOSTICS) })
            DropdownMenuItem(text = { Text(stringResource(R.string.support)) }, onClick = { expanded = false; viewModel.openSettingsDialog(SettingsDialogType.SUPPORT) })
        }
    }
}
