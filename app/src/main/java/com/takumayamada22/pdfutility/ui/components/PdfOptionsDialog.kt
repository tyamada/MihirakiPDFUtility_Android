package com.takumayamada22.pdfutility.ui.components

import androidx.core.net.toUri
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.saveable.rememberSaveable
import com.takumayamada22.pdfutility.ui.ToolbarProfiles
import com.takumayamada22.pdfutility.ui.ToolbarSelection
import com.takumayamada22.pdfutility.ui.ToolbarPreset
import com.takumayamada22.pdfutility.ui.toolbarActionOrder
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    var selectedSize by rememberSaveable { mutableStateOf(uiState.thumbnailSize) }
    var selectedButtonDisplayType by rememberSaveable { mutableStateOf(uiState.buttonDisplayType) }
    val profilesSaver = androidx.compose.runtime.saveable.listSaver<ToolbarProfiles, String>(
        save = { p -> listOf(p.portrait.preset?.name ?: "CUSTOM", p.portrait.buttons.joinToString(",") { it.name },
            p.landscape.preset?.name ?: "CUSTOM", p.landscape.buttons.joinToString(",") { it.name }) },
        restore = { v ->
            fun selection(i: Int) = ToolbarSelection(ToolbarPreset.entries.firstOrNull { it.name == v[i] },
                ActionButtonType.entries.filter { it.name in v[i + 1].split(",") }.toSet())
            ToolbarProfiles(selection(0), selection(2))
        }
    )
    val legacy = ToolbarSelection(null, uiState.visibleActionButtons)
    var profiles by rememberSaveable(stateSaver = profilesSaver) {
        mutableStateOf(uiState.preference.toolbarProfiles ?: ToolbarProfiles(legacy, legacy))
    }
    var editLandscape by rememberSaveable { mutableStateOf(configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) }
    // The actual toolbar always resolves against its own measured width. Opposite orientation is a preview.
    val currentLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val previewWidthDp = if (editLandscape == currentLandscape) configuration.screenWidthDp else configuration.screenHeightDp
    val width = with(density) { (previewWidthDp.dp - 24.dp).roundToPx() }
    val selection = profiles.forOrientation(editLandscape)
    val visibleButtons = resolvedToolbarButtons(selection, selectedButtonDisplayType, width, previewWidthDp >= 840, uiState.updateAvailable)
    fun customize(buttons: Set<ActionButtonType>) {
        profiles = profiles.withSelection(editLandscape, ToolbarSelection(null, buttons))
    }
    androidx.compose.ui.window.Dialog(onDismissRequest = { viewModel.closeSettingsDialog() }) {
        Surface(Modifier.fillMaxWidth().wrapContentHeight().imePadding(), shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = AlertDialogDefaults.TonalElevation) {
            Column(Modifier.padding(24.dp).fillMaxWidth()) {
                Text(stringResource(R.string.settings_options), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Column(Modifier.weight(1f, false).verticalScroll(rememberScrollState()).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.toolbar_presets_title), style = MaterialTheme.typography.titleSmall)
                    ToolbarPreset.entries.forEach { preset ->
                        val title = when (preset) {
                            ToolbarPreset.STANDARD -> R.string.toolbar_standard
                            ToolbarPreset.SIMPLE -> R.string.toolbar_simple
                            ToolbarPreset.COMPACT -> R.string.toolbar_compact
                        }
                        val description = when (preset) {
                            ToolbarPreset.STANDARD -> R.string.toolbar_standard_desc
                            ToolbarPreset.SIMPLE -> R.string.toolbar_simple_desc
                            ToolbarPreset.COMPACT -> R.string.toolbar_compact_desc
                        }
                        OutlinedButton(onClick = {
                            selectedButtonDisplayType = preset.displayType
                            profiles = ToolbarProfiles(ToolbarSelection(preset), ToolbarSelection(preset))
                        }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(stringResource(title))
                                Text(stringResource(description), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    listOf(true, false).forEach { show ->
                        OutlinedButton(onClick = {
                            selectedButtonDisplayType = ButtonDisplayType.ICON_AND_TEXT
                            val all = ToolbarSelection(null, if (show) ActionButtonType.entries.toSet() else emptySet())
                            profiles = ToolbarProfiles(all, all)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(stringResource(if (show) R.string.toolbar_show_all else R.string.toolbar_hide_all))
                                Text(stringResource(R.string.toolbar_all_desc), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Text(stringResource(R.string.toolbar_auto_note), style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                    Text(stringResource(R.string.options_desc), style = MaterialTheme.typography.bodyMedium)
                    ThumbnailSize.entries.forEach { size ->
                        val label = when (size) { ThumbnailSize.S -> R.string.options_size_s; ThumbnailSize.M -> R.string.options_size_m; ThumbnailSize.L -> R.string.options_size_l }
                        Row(Modifier.fillMaxWidth().selectable(selectedSize == size, role = androidx.compose.ui.semantics.Role.RadioButton, onClick = { selectedSize = size }).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selectedSize == size, onClick = null)
                            Spacer(Modifier.width(8.dp)); Text(stringResource(label))
                        }
                    }
                    HorizontalDivider()
                    Text(stringResource(R.string.options_button_display_title), style = MaterialTheme.typography.titleSmall)
                    ButtonDisplayType.entries.forEach { type ->
                        val label = when (type) { ButtonDisplayType.ICON_AND_TEXT -> R.string.button_display_icon_and_text; ButtonDisplayType.ICON_ONLY -> R.string.button_display_icon_only; ButtonDisplayType.TEXT_ONLY -> R.string.button_display_text_only }
                        Row(Modifier.fillMaxWidth().selectable(selectedButtonDisplayType == type, role = androidx.compose.ui.semantics.Role.RadioButton, onClick = { selectedButtonDisplayType = type }).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selectedButtonDisplayType == type, onClick = null)
                            Spacer(Modifier.width(8.dp)); Text(stringResource(label))
                        }
                    }
                    HorizontalDivider()
                    Text(stringResource(R.string.options_visible_buttons_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.options_visible_buttons_desc), style = MaterialTheme.typography.bodyMedium)
                    listOf(false, true).forEach { landscape ->
                        Row(Modifier.fillMaxWidth().selectable(editLandscape == landscape, role = androidx.compose.ui.semantics.Role.RadioButton, onClick = { editLandscape = landscape }), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(editLandscape == landscape, onClick = null)
                            Text(stringResource(if (landscape) R.string.toolbar_landscape else R.string.toolbar_portrait))
                        }
                    }
                    Text(stringResource(R.string.toolbar_custom_note), style = MaterialTheme.typography.bodySmall)
                    toolbarActionOrder.forEach { buttonType ->
                        val checked = buttonType in visibleButtons
                        Row(Modifier.fillMaxWidth().toggleable(checked, role = androidx.compose.ui.semantics.Role.Checkbox, onValueChange = {
                            customize(if (it) visibleButtons + buttonType else visibleButtons - buttonType)
                        }).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp)); Text(stringResource(buttonType.titleResId), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.closeSettingsDialog() }) { Text(stringResource(R.string.action_cancel)) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        viewModel.setThumbnailSize(selectedSize)
                        viewModel.setToolbarOptions(selectedButtonDisplayType, profiles)
                        viewModel.closeSettingsDialog()
                    }) { Text(stringResource(R.string.action_apply)) }
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
    val badgeManager = remember { com.takumayamada22.pdfutility.badge.BadgeManager(context) }
    val acquiredBadgeIds by badgeManager.acquiredBadgeIds.collectAsState()
    val acquiredBadges = com.takumayamada22.pdfutility.badge.BadgeCatalog.allBadges.filter { acquiredBadgeIds.contains(it.id) }
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

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (updateAvailable) {
                                val intent = Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri()).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${context.packageName}".toUri())
                                    context.startActivity(webIntent)
                                }
                            } else {
                                viewModel.checkForUpdatesManual()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(if (updateAvailable) R.string.update_app else R.string.check_for_updates))
                    }

                    if (acquiredBadges.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.commemorative_badge_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            acquiredBadges.forEach { badge ->
                                Image(
                                    painter = painterResource(badge.iconRes),
                                    contentDescription = stringResource(badge.titleRes),
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
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
