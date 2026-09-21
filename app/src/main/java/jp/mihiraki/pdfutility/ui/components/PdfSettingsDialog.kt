package jp.mihiraki.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.mihiraki.pdfutility.ui.PdfUiState
import jp.mihiraki.pdfutility.ui.PdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfSettingsDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    if (uiState.showSettingsDialog) {
        var title by remember { mutableStateOf(uiState.title) }
        var author by remember { mutableStateOf(uiState.author) }
        var subject by remember { mutableStateOf(uiState.subject) }
        var keywords by remember { mutableStateOf(uiState.keywords) }
        var savePassword by remember { mutableStateOf(uiState.savePassword) }
        
        var pageLayout by remember { mutableStateOf(uiState.pageLayout) }
        var scrollDirection by remember { mutableStateOf(uiState.scrollDirection) }
        var showCover by remember { mutableStateOf(uiState.showCover) }

        val pageDisplayString = remember(pageLayout) {
            when (pageLayout) {
                "SinglePage" -> "単一ページ表示"
                "OneColumn" -> "単一ページ表示、スクロール有効"
                "TwoColumnLeft" -> "見開きページ表示、スクロール有効"
                "TwoColumnRight" -> "見開きページ表示、表紙を表示、スクロール有効"
                "TwoPageLeft" -> "見開きページ表示"
                "TwoPageRight" -> "見開きページ表示、表紙を表示"
                else -> ""
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.toggleSettingsDialog() },
            title = { Text("PDF Settings") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Detailed Info", style = MaterialTheme.typography.titleSmall)
                    Text("PDF Version: ${uiState.pdfVersion}", style = MaterialTheme.typography.bodySmall)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ページを表示: ", style = MaterialTheme.typography.bodySmall)
                        Text(pageDisplayString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") }
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author") }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text("Viewer Preferences", style = MaterialTheme.typography.titleSmall)
                    
                    // Page Layout Dropdown
                    var layoutExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = layoutExpanded,
                        onExpandedChange = { layoutExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = pageLayout,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Page Layout") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = layoutExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = layoutExpanded,
                            onDismissRequest = { layoutExpanded = false }
                        ) {
                            listOf("SinglePage", "OneColumn", "TwoColumnLeft", "TwoColumnRight", "TwoPageLeft", "TwoPageRight").forEach { layout ->
                                DropdownMenuItem(
                                    text = { Text(layout) },
                                    onClick = {
                                        pageLayout = layout
                                        if (layout == "TwoColumnRight" || layout == "TwoPageRight") {
                                            showCover = true
                                        } else if (layout == "TwoColumnLeft" || layout == "TwoPageLeft") {
                                            showCover = false
                                        }
                                        layoutExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Show Cover Switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("表紙を表示: ", modifier = Modifier.weight(1f))
                        Switch(
                            checked = showCover,
                            onCheckedChange = { isChecked ->
                                showCover = isChecked
                                if (isChecked) {
                                    if (pageLayout == "TwoColumnLeft") pageLayout = "TwoColumnRight"
                                    if (pageLayout == "TwoPageLeft") pageLayout = "TwoPageRight"
                                } else {
                                    if (pageLayout == "TwoColumnRight") pageLayout = "TwoColumnLeft"
                                    if (pageLayout == "TwoPageRight") pageLayout = "TwoPageLeft"
                                }
                            }
                        )
                    }

                    // Reading Direction
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Direction: ", modifier = Modifier.weight(1f))
                        SegmentedButton(
                            selected = scrollDirection == "L2R",
                            onClick = { scrollDirection = "L2R" },
                            label = "L2R"
                        )
                        SegmentedButton(
                            selected = scrollDirection == "R2L",
                            onClick = { scrollDirection = "R2L" },
                            label = "R2L"
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text("Security", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = savePassword,
                        onValueChange = { savePassword = it },
                        label = { Text("Encryption Password") },
                        placeholder = { Text("Leave empty for no password") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateMetadata(title, author, subject, keywords, pageLayout, scrollDirection, showCover)
                    viewModel.setSavePassword(savePassword)
                    viewModel.toggleSettingsDialog()
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleSettingsDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SegmentedButton(selected: Boolean, onClick: () -> Unit, label: String) {
    Button(
        onClick = onClick,
        colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
        modifier = Modifier.padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    placeholder: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        modifier = modifier,
        singleLine = true
    )
}
