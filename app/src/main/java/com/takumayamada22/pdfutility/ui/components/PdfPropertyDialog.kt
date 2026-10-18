package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel

@Composable
internal fun PdfPropertyDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    var title by remember { mutableStateOf(uiState.title) }
    var author by remember { mutableStateOf(uiState.author) }
    var subject by remember { mutableStateOf(uiState.subject) }
    var keywords by remember { mutableStateOf(uiState.keywords) }
    var pdfVersion by remember { mutableStateOf(uiState.pdfVersion.ifEmpty { "1.4" }) }
    
    var pageLayout by remember { mutableStateOf(uiState.pageLayout) }
    var scrollDirection by remember { mutableStateOf(uiState.scrollDirection) }
    var showCover by remember { mutableStateOf(uiState.showCover) }

    var showVersionDialog by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }

    val pageDisplayString = when (pageLayout) {
        "SinglePage" -> stringResource(R.string.display_single)
        "OneColumn" -> stringResource(R.string.display_one_column)
        "TwoColumnLeft" -> stringResource(R.string.display_two_column_left)
        "TwoColumnRight" -> stringResource(R.string.display_two_column_right)
        "TwoPageLeft" -> stringResource(R.string.display_two_page_left)
        "TwoPageRight" -> stringResource(R.string.display_two_page_right)
        else -> ""
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = { viewModel.closeSettingsDialog() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .imePadding(),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.settings_property),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PdfSettingsTextField(value = uiState.fileName ?: "", onValueChange = {}, label = { Text(stringResource(R.string.prop_filename)) }, readOnly = true)
                    PdfSettingsTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.prop_title)) })
                    PdfSettingsTextField(value = author, onValueChange = { author = it }, label = { Text(stringResource(R.string.prop_author)) })
                    PdfSettingsTextField(value = subject, onValueChange = { subject = it }, label = { Text(stringResource(R.string.prop_subtitle)) })
                    PdfSettingsTextField(value = keywords, onValueChange = { keywords = it }, label = { Text(stringResource(R.string.prop_keywords)) })
                    
                    OutlinedCard(
                        onClick = { showVersionDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.prop_pdf_version_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(pdfVersion, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(stringResource(R.string.prop_display_settings), style = MaterialTheme.typography.titleSmall)
                    
                    OutlinedCard(
                        onClick = { showLayoutDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.prop_page_layout), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                when(pageLayout) {
                                    "SinglePage" -> stringResource(R.string.layout_single)
                                    "OneColumn" -> stringResource(R.string.layout_one_column)
                                    "TwoColumnLeft" -> stringResource(R.string.layout_two_column_left)
                                    "TwoColumnRight" -> stringResource(R.string.layout_two_column_right)
                                    "TwoPageLeft" -> stringResource(R.string.layout_two_page_left)
                                    "TwoPageRight" -> stringResource(R.string.layout_two_page_right)
                                    else -> pageLayout
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.prop_show_cover), modifier = Modifier.weight(1f))
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

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.prop_scroll_direction), modifier = Modifier.weight(1f))
                        SegmentedButton(selected = scrollDirection == "L2R", onClick = { scrollDirection = "L2R" }, label = "L2R")
                        SegmentedButton(selected = scrollDirection == "R2L", onClick = { scrollDirection = "R2L" }, label = "R2L")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.prop_page_display) + ": ", style = MaterialTheme.typography.bodySmall)
                        Text(pageDisplayString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.closeSettingsDialog() }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        viewModel.updateMetadata(title, author, subject, keywords, pdfVersion, pageLayout, scrollDirection, showCover)
                        viewModel.closeSettingsDialog()
                    }) {
                        Text(stringResource(R.string.action_apply))
                    }
                }
            }
        }
    }

    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            title = { Text(stringResource(R.string.prop_pdf_version_label)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1.3", "1.4", "1.5", "1.6", "1.7").forEach { ver ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pdfVersion = ver
                                    showVersionDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = pdfVersion == ver,
                                onClick = {
                                    pdfVersion = ver
                                    showVersionDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(ver, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showLayoutDialog) {
        AlertDialog(
            onDismissRequest = { showLayoutDialog = false },
            title = { Text(stringResource(R.string.prop_page_layout)) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 350.dp)
                ) {
                    listOf("SinglePage", "OneColumn", "TwoColumnLeft", "TwoColumnRight", "TwoPageLeft", "TwoPageRight").forEach { layout ->
                        val label = when(layout) {
                            "SinglePage" -> stringResource(R.string.layout_single)
                            "OneColumn" -> stringResource(R.string.layout_one_column)
                            "TwoColumnLeft" -> stringResource(R.string.layout_two_column_left)
                            "TwoColumnRight" -> stringResource(R.string.layout_two_column_right)
                            "TwoPageLeft" -> stringResource(R.string.layout_two_page_left)
                            "TwoPageRight" -> stringResource(R.string.layout_two_page_right)
                            else -> layout
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pageLayout = layout
                                    if (layout == "TwoColumnRight" || layout == "TwoPageRight") {
                                        showCover = true
                                    } else if (layout == "TwoColumnLeft" || layout == "TwoPageLeft") {
                                        showCover = false
                                    }
                                    if (layout == "TwoPageLeft" || layout == "TwoPageRight") {
                                        val verNum = pdfVersion.toFloatOrNull() ?: 1.4f
                                        if (verNum < 1.5f) {
                                            pdfVersion = "1.5"
                                        }
                                    }
                                    showLayoutDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = pageLayout == layout,
                                onClick = {
                                    pageLayout = layout
                                    if (layout == "TwoColumnRight" || layout == "TwoPageRight") {
                                        showCover = true
                                    } else if (layout == "TwoColumnLeft" || layout == "TwoPageLeft") {
                                        showCover = false
                                    }
                                    if (layout == "TwoPageLeft" || layout == "TwoPageRight") {
                                        val verNum = pdfVersion.toFloatOrNull() ?: 1.4f
                                        if (verNum < 1.5f) {
                                            pdfVersion = "1.5"
                                        }
                                    }
                                    showLayoutDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLayoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
internal fun PdfSettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}
