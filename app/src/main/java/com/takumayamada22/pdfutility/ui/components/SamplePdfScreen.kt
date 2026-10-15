package com.takumayamada22.pdfutility.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.data.SamplePdfItemState
import com.takumayamada22.pdfutility.ui.SamplePdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamplePdfScreen(
    viewModel: SamplePdfViewModel,
    back: () -> Unit,
    onOpenPdf: (Uri) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val allLabel = stringResource(R.string.all_languages)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sample_pdfs)) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Language Dropdown Filter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedLabel = when (uiState.selectedLanguage) {
                        "ALL" -> allLabel
                        "en" -> "English"
                        "ja" -> "Japanese (日本語)"
                        "ko" -> "Korean (한국어)"
                        "zh_cn" -> "Chinese (Simplified) (中国語(簡体字))"
                        "zh_tw" -> "Chinese (Traditional) (中国語(繁体字))"
                        "de" -> "German (Deutsch)"
                        "fr" -> "French (Français)"
                        else -> uiState.selectedLanguage.uppercase()
                    }
                    OutlinedTextField(
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.language_filter)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        uiState.availableLanguages.forEach { lang ->
                            val labelText = when (lang) {
                                "ALL" -> allLabel
                                "en" -> "English"
                                "ja" -> "Japanese (日本語)"
                                "ko" -> "Korean (한국어)"
                                "zh_cn" -> "Chinese (Simplified) (中国語(簡体字))"
                                "zh_tw" -> "Chinese (Traditional) (中国語(繁体字))"
                                "de" -> "German (Deutsch)"
                                "fr" -> "French (Français)"
                                else -> lang.uppercase()
                            }
                            DropdownMenuItem(
                                text = { Text(labelText) },
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // PDF List & Attribution Footer
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.items, key = { it.item.id }) { itemState ->
                    SamplePdfCard(
                        itemState = itemState,
                        onDownload = { viewModel.requestDownload(itemState.item) },
                        onOpen = {
                            itemState.localFile?.let { file ->
                                onOpenPdf(Uri.fromFile(file))
                            }
                        },
                        onDelete = { viewModel.deletePdf(itemState.item) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "© 2026 Komairo Biyori / CC BY 4.0",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Parts of this work are provided under the Creative Commons Attribution 4.0 International (CC BY 4.0) license.\n" +
                                "Attribution: \"The Try-It Club\" by Komairo Biyori.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    // Cellular Network Confirmation Dialog
    uiState.cellularDialogItem?.let { item ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissCellularDialog() },
            title = { Text(stringResource(R.string.cellular_data_warning)) },
            text = { Text(stringResource(R.string.cellular_data_message, item.title, item.fileSize)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCellularDownload(item) }) {
                    Text(stringResource(R.string.sample_pdf_download))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCellularDialog() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun SamplePdfCard(
    itemState: SamplePdfItemState,
    onDownload: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = itemState.item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${itemState.item.languageDisplayName} • ${itemState.item.fileSize}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (itemState.isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { itemState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.sample_pdf_downloading_progress, (itemState.progress * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            itemState.errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.error_prefix, err),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (itemState.isDownloaded && !itemState.isDownloading) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.sample_pdf_delete))
                    }

                    Button(onClick = onOpen, modifier = Modifier.testTag("btn_open")) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.sample_pdf_open))
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        enabled = !itemState.isDownloading,
                        modifier = Modifier.testTag("btn_download")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (itemState.isDownloading) stringResource(R.string.sample_pdf_downloading) else stringResource(R.string.sample_pdf_download))
                    }
                }
            }
        }
    }
}
