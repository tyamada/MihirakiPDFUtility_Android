package jp.mihiraki.pdfutility.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import jp.mihiraki.pdfutility.ui.PdfUiState
import jp.mihiraki.pdfutility.ui.PdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfTopAppBar(
    uiState: PdfUiState,
    viewModel: PdfViewModel,
    onOpen: () -> Unit,
    onAppend: () -> Unit,
    onSave: () -> Unit,
    onExport: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = uiState.fileName ?: "Mihiraki PDF Utility",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            IconButton(onClick = { viewModel.undo() }, enabled = uiState.canUndo) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
            }
            IconButton(onClick = { viewModel.redo() }, enabled = uiState.canRedo) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
            }

            if (uiState.selectedIndices.isEmpty()) {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Default.FileOpen, contentDescription = "Open")
                }
                if (uiState.pages.isNotEmpty()) {
                    IconButton(onClick = onAppend) {
                        Icon(Icons.Default.Add, contentDescription = "Add PDF")
                    }
                    if (uiState.isMihirakiView) {
                        IconButton(onClick = { viewModel.toggleRtl() }) {
                            Icon(
                                if (uiState.isRtl) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Toggle RTL"
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.toggleMihirakiView() }) {
                        Icon(
                            if (uiState.isMihirakiView) Icons.Default.GridView else Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Toggle View"
                        )
                    }
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            } else {
                IconButton(onClick = { viewModel.selectAll() }) {
                    Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                }
                IconButton(onClick = { viewModel.invertSelection() }) {
                    Icon(Icons.Default.Flip, contentDescription = "Invert Selection")
                }
                IconButton(onClick = onExport) {
                    Icon(Icons.Default.Output, contentDescription = "Export Selected")
                }
                IconButton(onClick = { viewModel.splitSelectedPages() }) {
                    Icon(Icons.Default.ContentCut, contentDescription = "Split Pages")
                }
                IconButton(onClick = { viewModel.rotateSelected(90) }) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate Right")
                }
                IconButton(onClick = { viewModel.insertBlankAfterSelected() }) {
                    Icon(Icons.Default.AddBox, contentDescription = "Insert Blank")
                }
                IconButton(onClick = { viewModel.moveSelected(-1) }) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                }
                IconButton(onClick = { viewModel.moveSelected(1) }) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
                IconButton(onClick = { viewModel.clearSelection() }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                }
            }
        }
    )
}
