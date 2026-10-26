package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType

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
    val isBusy = uiState.isLoading || uiState.isAppending

    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isDesktopOrLarge = maxWidth >= 840.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isDesktopOrLarge) {
                    PdfMenuBar(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpen = onOpen,
                        onOpenFolder = onOpenFolder,
                        onOpenSamplePdf = onOpenSamplePdf,
                        onAppend = onAppend,
                        onSave = onSave,
                        onExport = onExport,
                        onSplitClick = onSplitClick,
                        onCropClick = onCropClick,
                        onDeleteClick = onDeleteClick
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

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

                    if (!isDesktopOrLarge) {
                        MobileOverflowMenu(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpen = onOpen,
                            onOpenFolder = onOpenFolder,
                            onOpenSamplePdf = onOpenSamplePdf,
                            onAppend = onAppend,
                            onSave = onSave,
                            onExport = onExport,
                            onSplitClick = onSplitClick,
                            onCropClick = onCropClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    PdfActionButtons(
                        uiState = uiState,
                        viewModel = viewModel,
                        isBusy = isBusy,
                        displayType = displayType,
                        onOpen = onOpen,
                        onOpenFolder = onOpenFolder,
                        onOpenSamplePdf = onOpenSamplePdf,
                        onAppend = onAppend,
                        onSave = onSave,
                        onExport = onExport,
                        onSplitClick = onSplitClick,
                        onCropClick = onCropClick,
                        onDeleteClick = onDeleteClick
                    )

                    if (isDesktopOrLarge) {
                        // Settings button is always visible in desktop mode
                        Box(modifier = Modifier.testTag("settingsButton")) {
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
                                    onClick = { viewModel.openSettingsDialog(SettingsDialogType.DIAGNOSTICS) },
                                    modifier = Modifier.testTag("diagnosticsMenuItem")
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.support)) },
                                    onClick = { viewModel.openSettingsDialog(SettingsDialogType.SUPPORT) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.privacy_policy_title)) },
                                    onClick = { viewModel.openSettingsDialog(SettingsDialogType.PRIVACY_POLICY) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
