package com.takumayamada22.pdfutility.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.billing.TipTier
import com.takumayamada22.pdfutility.ui.ActionButtonType
import com.takumayamada22.pdfutility.ui.ButtonDisplayType
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType
import com.takumayamada22.pdfutility.ui.ThumbnailSize

@Composable
internal fun PdfOptionsDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    var selectedSize by remember { mutableStateOf(uiState.thumbnailSize) }
    var selectedButtonDisplayType by remember { mutableStateOf(uiState.buttonDisplayType) }
    var visibleButtons by remember { mutableStateOf(uiState.visibleActionButtons) }

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
                    text = stringResource(R.string.settings_options),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, false)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.options_desc), style = MaterialTheme.typography.bodyMedium)
                    
                    ThumbnailSize.entries.forEach { size ->
                        val label = when (size) {
                            ThumbnailSize.S -> stringResource(R.string.options_size_s)
                            ThumbnailSize.M -> stringResource(R.string.options_size_m)
                            ThumbnailSize.L -> stringResource(R.string.options_size_l)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSize = size }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(stringResource(R.string.options_button_display_title), style = MaterialTheme.typography.titleSmall)

                    ButtonDisplayType.entries.forEach { type ->
                        val label = when (type) {
                            ButtonDisplayType.ICON_AND_TEXT -> stringResource(R.string.button_display_icon_and_text)
                            ButtonDisplayType.ICON_ONLY -> stringResource(R.string.button_display_icon_only)
                            ButtonDisplayType.TEXT_ONLY -> stringResource(R.string.button_display_text_only)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedButtonDisplayType = type }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedButtonDisplayType == type,
                                onClick = { selectedButtonDisplayType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(stringResource(R.string.options_visible_buttons_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.options_visible_buttons_desc), style = MaterialTheme.typography.bodyMedium)

                    ActionButtonType.entries.forEach { buttonType ->
                        val label = stringResource(buttonType.titleResId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    visibleButtons = if (visibleButtons.contains(buttonType)) {
                                        visibleButtons - buttonType
                                    } else {
                                        visibleButtons + buttonType
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = visibleButtons.contains(buttonType),
                                onCheckedChange = { checked ->
                                    visibleButtons = if (checked) {
                                        visibleButtons + buttonType
                                    } else {
                                        visibleButtons - buttonType
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
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
                        viewModel.setThumbnailSize(selectedSize)
                        viewModel.setButtonDisplayType(selectedButtonDisplayType)
                        viewModel.setVisibleActionButtons(visibleButtons)
                        viewModel.closeSettingsDialog()
                    }) {
                        Text(stringResource(R.string.action_apply))
                    }
                }
            }
        }
    }
}

@Composable
internal fun PdfVersionDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val manager = viewModel.billing
    val purchasedTiers by manager.purchasedTiers.collectAsState()
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.checkForUpdates()
    }
    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
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
                    text = stringResource(R.string.settings_version),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, false)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.version_app_name), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.version_number, packageInfo.versionName ?: "1.0.0"))
                    @Suppress("DEPRECATION")
                    Text(stringResource(R.string.version_build, if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode.toString() else packageInfo.versionCode.toString()))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.version_copyright), style = MaterialTheme.typography.bodySmall)

                    if (updateAvailable) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.update_app))
                        }
                    }

                    if (purchasedTiers.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.help_purchased_items),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            purchasedTiers.forEach { tier ->
                                val iconRes = when (tier) {
                                    TipTier.BRONZE -> R.drawable.tip_bronze
                                    TipTier.SILVER -> R.drawable.tip_silver
                                    TipTier.GOLD -> R.drawable.tip_gold
                                    TipTier.KOYOMI -> R.drawable.tameshibu_badge_koyomi
                                }
                                Image(
                                    painter = painterResource(iconRes),
                                    contentDescription = tier.name,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                    }

                    var showPromoDialog by remember { mutableStateOf(false) }

                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showPromoDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Text(stringResource(R.string.promo_code_help_title))
                        }
                    }

                    if (showPromoDialog) {
                        AlertDialog(
                            onDismissRequest = { showPromoDialog = false },
                            title = { Text(stringResource(R.string.promo_code_help_title)) },
                            text = {
                                Text(stringResource(R.string.promo_code_help_message), style = MaterialTheme.typography.bodyMedium)
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showPromoDialog = false
                                        viewModel.closeSettingsDialog()
                                        viewModel.openSettingsDialog(SettingsDialogType.SUPPORT)
                                    }
                                ) {
                                    Text(stringResource(R.string.go_to_support))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPromoDialog = false }) {
                                    Text(stringResource(R.string.action_cancel))
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.version_license_title), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = stringResource(R.string.version_license_text),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { viewModel.closeSettingsDialog() }) {
                        Text(stringResource(R.string.action_close))
                    }
                }
            }
        }
    }
}

@Composable
internal fun SegmentedButton(selected: Boolean, onClick: () -> Unit, label: String) {
    Button(
        onClick = onClick,
        colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
        modifier = Modifier.padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
