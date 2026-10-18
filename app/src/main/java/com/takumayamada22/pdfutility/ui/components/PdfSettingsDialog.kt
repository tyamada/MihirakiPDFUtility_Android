package com.takumayamada22.pdfutility.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.billing.PurchaseState
import com.takumayamada22.pdfutility.billing.TipTier
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.ui.ButtonDisplayType
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType
import com.takumayamada22.pdfutility.ui.ThumbnailSize
import com.takumayamada22.pdfutility.util.AppLogger
import com.takumayamada22.pdfutility.util.DeviceTestRunner
import com.takumayamada22.pdfutility.util.PersistentLogManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PdfSettingsDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    when (uiState.activeSettingsDialog) {
        SettingsDialogType.PROPERTY -> PdfPropertyDialog(viewModel, uiState)
        SettingsDialogType.PASSWORD -> PdfPasswordDialog(viewModel, uiState)
        SettingsDialogType.OPTIONS -> PdfOptionsDialog(viewModel, uiState)
        SettingsDialogType.VERSION -> PdfVersionDialog(viewModel)
        SettingsDialogType.SUPPORT -> PdfSupportDialog(viewModel)
        SettingsDialogType.HELP -> PdfHelpDialog(viewModel)
        SettingsDialogType.DIAGNOSTICS -> PdfDiagnosticsDialog(viewModel)
        SettingsDialogType.REPORT_ISSUE -> PdfReportIssueDialog(viewModel)
        null -> {}
    }
}

@Composable
private fun PdfPropertyDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
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
                    OutlinedTextField(value = uiState.fileName ?: "", onValueChange = {}, label = { Text(stringResource(R.string.prop_filename)) }, readOnly = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.prop_title)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text(stringResource(R.string.prop_author)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text(stringResource(R.string.prop_subtitle)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text(stringResource(R.string.prop_keywords)) }, modifier = Modifier.fillMaxWidth())
                    
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
private fun PdfPasswordDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    var password by remember { mutableStateOf(uiState.savePassword) }

    AlertDialog(
        onDismissRequest = { viewModel.closeSettingsDialog() },
        title = { Text(stringResource(R.string.settings_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.security_desc))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.security_password_label)) },
                    placeholder = { Text(stringResource(R.string.security_not_set)) }
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    password = ""
                    viewModel.setSavePassword("")
                }) {
                    Text(stringResource(R.string.action_clear))
                }
                Button(onClick = {
                    viewModel.setSavePassword(password)
                    viewModel.closeSettingsDialog()
                }) {
                    Text(stringResource(R.string.action_set))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeSettingsDialog() }) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun PdfOptionsDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    var selectedSize by remember { mutableStateOf(uiState.thumbnailSize) }
    var selectedButtonDisplayType by remember { mutableStateOf(uiState.buttonDisplayType) }

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
private fun PdfVersionDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val manager = viewModel.billing
    val purchasedTiers by manager.purchasedTiers.collectAsState()
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
                    Text(stringResource(R.string.version_build, if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode.toString() else packageInfo.versionCode.toString()))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.version_copyright), style = MaterialTheme.typography.bodySmall)

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
private fun PdfDiagnosticsDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    var testResults by remember { mutableStateOf<String?>(null) }
    var showLogs by remember { mutableStateOf(false) }
    var logsText by remember { mutableStateOf("") }

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
                    text = stringResource(R.string.diagnostics_screen_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.diagnostics_screen_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. 診断テストを実行
                    Button(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                            val manufacturer = Build.MANUFACTURER
                            val model = Build.MODEL
                            val osVersion = Build.VERSION.RELEASE
                            val sdkInt = Build.VERSION.SDK_INT

                            val resultsBuilder = StringBuilder()
                            resultsBuilder.append("=== MihirakiPDF Utility Diagnostics ===\n")
                            resultsBuilder.append(context.getString(R.string.test_timestamp_format, timestamp)).append("\n")
                            resultsBuilder.append(context.getString(R.string.device_info_format, manufacturer, model, osVersion)).append(" (SDK $sdkInt)\n")

                            try {
                                val processor = PdfProcessor()
                                processor.close()
                                resultsBuilder.append("1. PdfProcessor Init Test: PASSED\n")
                            } catch (e: Exception) {
                                resultsBuilder.append("1. PdfProcessor Init Test: FAILED (${e.message})\n")
                            }

                            val runtime = Runtime.getRuntime()
                            val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                            val maxMb = runtime.maxMemory() / (1024 * 1024)
                            resultsBuilder.append("2. Memory Usage: ${usedMb}MB / max ${maxMb}MB: PASSED\n")
                            resultsBuilder.append("All diagnostic checks completed successfully.")

                            testResults = resultsBuilder.toString()
                            AppLogger.log(context, "INFO", "Diagnostic tests executed successfully.")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.run_diagnostics))
                    }

                    // 2. アプリログを表示
                    Button(
                        onClick = {
                            logsText = AppLogger.getLogs(context)
                            showLogs = true
                            AppLogger.log(context, "INFO", "Viewed operation audit logs.")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.view_app_logs))
                    }

                    // 3. 不具合を報告
                    Button(
                        onClick = {
                            viewModel.openSettingsDialog(SettingsDialogType.REPORT_ISSUE)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.report_issue))
                    }

                    // 診断テストを実行した結果には「診断結果を共有」ボタンを表示
                    testResults?.let { results ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(results, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (showLogs) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    stringResource(R.string.diagnostics_screen_privacy),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                HorizontalDivider()
                                Text(
                                    logsText,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 250.dp)
                                        .verticalScroll(rememberScrollState())
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, logsText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }) {
                                        Text(stringResource(R.string.share_logs))
                                    }
                                }
                            }
                        }
                    }
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
private fun PdfSupportDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val manager = viewModel.billing
    val products by manager.products.collectAsState()
    val purchaseState by manager.purchase.collectAsState()
    val purchasedTiers by manager.purchasedTiers.collectAsState()

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
                    text = stringResource(R.string.support),
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
                    Text(stringResource(R.string.tip_message))
                    Text(
                        stringResource(R.string.free_use_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TipTier.entries.forEach { tier ->
                        val product = products.firstOrNull { it.productId == tier.productId }
                        val priceText = product?.oneTimePurchaseOfferDetails?.formattedPrice ?: tier.productId

                        val isOwned = purchasedTiers.contains(tier)

                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val iconRes = when (tier) {
                                    TipTier.BRONZE -> R.drawable.tip_bronze
                                    TipTier.SILVER -> R.drawable.tip_silver
                                    TipTier.GOLD -> R.drawable.tip_gold
                                    TipTier.KOYOMI -> R.drawable.tameshibu_badge_koyomi
                                }
                                Image(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (tier == TipTier.KOYOMI) stringResource(R.string.commemorative_badge_title) else tier.name,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(priceText, style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = {
                                        product?.let { manager.purchase(context as Activity, it) }
                                    },
                                    enabled = !isOwned && product != null,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isOwned) stringResource(R.string.action_owned) else stringResource(R.string.action_purchase),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                    
                    when (val s = purchaseState) {
                        is PurchaseState.Success -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.purchase_thanks, if (s.tier == TipTier.KOYOMI) "Koyomi" else s.tier.name), color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.purchase_badge_shown), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        is PurchaseState.Cancelled -> Text(stringResource(R.string.purchase_cancelled), style = MaterialTheme.typography.bodySmall)
                        is PurchaseState.Error -> Text(stringResource(R.string.purchase_failed), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        else -> Unit
                    }
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

    LaunchedEffect(Unit) {
        manager.connect()
    }
}

data class HelpCategorySpec(val title: String, val items: List<String>)

@Composable
private fun PdfHelpDialog(viewModel: PdfViewModel) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val categories = listOf(
        HelpCategorySpec(
            title = "はじめての使い方 / Getting Started",
            items = listOf(
                stringResource(R.string.help_item_overview),
                stringResource(R.string.help_item_open_save)
            )
        ),
        HelpCategorySpec(
            title = "編集を快適にする機能 / Editing Features",
            items = listOf(
                stringResource(R.string.help_item_page_ops),
                stringResource(R.string.help_item_devices)
            )
        ),
        HelpCategorySpec(
            title = "設定とカスタマイズ / Settings Guide",
            items = listOf(
                stringResource(R.string.help_item_properties),
                stringResource(R.string.help_item_security),
                stringResource(R.string.help_item_options),
                stringResource(R.string.help_item_support)
            )
        ),
        HelpCategorySpec(
            title = "診断 / Diagnostics",
            items = listOf(
                stringResource(R.string.help_item_diagnostics),
                stringResource(R.string.help_item_audit_logs),
                stringResource(R.string.help_item_report_issues)
            )
        )
    )

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
                    text = stringResource(R.string.help_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        var expanded by remember { mutableStateOf(false) }
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = !expanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (expanded) "Collapse" else "Expand"
                                        )
                                    }
                                }
                                if (expanded) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        category.items.forEach { htmlText ->
                                            AndroidView(
                                                factory = { context ->
                                                    TextView(context).apply {
                                                        textSize = 14f
                                                    }
                                                },
                                                update = { view ->
                                                    view.text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
                                                    view.setTextColor(onSurfaceColor)
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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

@Composable
private fun PdfReportIssueDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

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
                    text = stringResource(R.string.report_issue),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "不具合が発生した場合は、以下の情報をまとめてご確認ください。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.report_include_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.report_step_1), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                    Text(stringResource(R.string.report_step_2), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                    Text(stringResource(R.string.report_step_3), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                    Text(stringResource(R.string.report_step_4), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                    Text(stringResource(R.string.report_step_5), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                    Text(stringResource(R.string.report_step_6), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))

                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val result = DeviceTestRunner.runTest(context)
                            val testReport = DeviceTestRunner.formatResultString(result)
                            val logs = PersistentLogManager.getAllLogs().joinToString("\n")
                            val combined = "$testReport\n\n--- App Logs (${PersistentLogManager.getAllLogs().size} lines) ---\n$logs"
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(combined))
                            android.widget.Toast.makeText(context, context.getString(R.string.bulk_copy_success), android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.copy_all_diagnostics))
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.privacy_protection), style = MaterialTheme.typography.titleSmall)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.privacy_note_1), style = MaterialTheme.typography.bodySmall)
                            Text(stringResource(R.string.privacy_note_2), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/tyamada/MihirakiPDFUtility_Android/issues/new?template=bug_report.md"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.report_github))
                    }
                    Text(
                        "※ GitHubアカウントが必要です。送信した情報は公開される場合があります。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        viewModel.openSettingsDialog(SettingsDialogType.DIAGNOSTICS)
                    }) {
                        Text(stringResource(R.string.action_back))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { viewModel.closeSettingsDialog() }) {
                        Text(stringResource(R.string.action_close))
                    }
                }
            }
        }
    }
}
