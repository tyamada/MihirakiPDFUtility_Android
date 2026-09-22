package jp.mihiraki.pdfutility.ui.components

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.mihiraki.pdfutility.R
import jp.mihiraki.pdfutility.billing.PurchaseState
import jp.mihiraki.pdfutility.billing.TipTier
import jp.mihiraki.pdfutility.ui.PdfUiState
import jp.mihiraki.pdfutility.ui.PdfViewModel
import jp.mihiraki.pdfutility.ui.SettingsDialogType

@Composable
fun PdfSettingsDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    when (uiState.activeSettingsDialog) {
        SettingsDialogType.PROPERTY -> PdfPropertyDialog(viewModel, uiState)
        SettingsDialogType.PASSWORD -> PdfPasswordDialog(viewModel, uiState)
        SettingsDialogType.VERSION -> PdfVersionDialog(viewModel)
        SettingsDialogType.SUPPORT -> PdfSupportDialog(viewModel)
        null -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfPropertyDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    var title by remember { mutableStateOf(uiState.title) }
    var author by remember { mutableStateOf(uiState.author) }
    var subject by remember { mutableStateOf(uiState.subject) }
    var keywords by remember { mutableStateOf(uiState.keywords) }
    
    var pageLayout by remember { mutableStateOf(uiState.pageLayout) }
    var scrollDirection by remember { mutableStateOf(uiState.scrollDirection) }
    var showCover by remember { mutableStateOf(uiState.showCover) }

    val pageDisplayString = when (pageLayout) {
        "SinglePage" -> stringResource(R.string.display_single)
        "OneColumn" -> stringResource(R.string.display_one_column)
        "TwoColumnLeft" -> stringResource(R.string.display_two_column_left)
        "TwoColumnRight" -> stringResource(R.string.display_two_column_right)
        "TwoPageLeft" -> stringResource(R.string.display_two_page_left)
        "TwoPageRight" -> stringResource(R.string.display_two_page_right)
        else -> ""
    }

    AlertDialog(
        onDismissRequest = { viewModel.closeSettingsDialog() },
        title = { Text(stringResource(R.string.settings_property)) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = uiState.fileName ?: "", onValueChange = {}, label = { Text(stringResource(R.string.prop_filename)) }, readOnly = true)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.prop_title)) })
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text(stringResource(R.string.prop_author)) })
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text(stringResource(R.string.prop_subtitle)) })
                OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text(stringResource(R.string.prop_keywords)) })
                OutlinedTextField(value = stringResource(R.string.prop_pdf_version, uiState.pdfVersion), onValueChange = {}, label = { Text(stringResource(R.string.prop_pdf_version, "")) }, readOnly = true)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(stringResource(R.string.prop_display_settings), style = MaterialTheme.typography.titleSmall)
                
                var layoutExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = layoutExpanded,
                    onExpandedChange = { layoutExpanded = it }
                ) {
                    OutlinedTextField(
                        value = pageLayout,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.prop_page_layout)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = layoutExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = layoutExpanded,
                        onDismissRequest = { layoutExpanded = false }
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
                            DropdownMenuItem(
                                text = { Text(label) },
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

                Row(verticalAlignment = Alignment.CenterVertically) {
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.prop_scroll_direction), modifier = Modifier.weight(1f))
                    SegmentedButton(selected = scrollDirection == "L2R", onClick = { scrollDirection = "L2R" }, label = "L2R")
                    SegmentedButton(selected = scrollDirection == "R2L", onClick = { scrollDirection = "R2L" }, label = "R2L")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.prop_page_display) + ": ", style = MaterialTheme.typography.bodySmall)
                    Text(pageDisplayString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.updateMetadata(title, author, subject, keywords, pageLayout, scrollDirection, showCover)
                viewModel.closeSettingsDialog()
            }) {
                Text(stringResource(R.string.action_apply))
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
private fun PdfVersionDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }

    AlertDialog(
        onDismissRequest = { viewModel.closeSettingsDialog() },
        title = { Text(stringResource(R.string.settings_version)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.version_app_name), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.version_number, packageInfo.versionName ?: "1.0.0"))
                Text(stringResource(R.string.version_build, if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode.toString() else packageInfo.versionCode.toString()))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.version_copyright), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.closeSettingsDialog() }) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun PdfSupportDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val manager = viewModel.billing
    val products by manager.products.collectAsState()
    val purchaseState by manager.purchase.collectAsState()
    
    // In a real app, you might use BuildConfig.DEBUG, but here we'll assume simulation is okay for dev
    val isDebug = true 

    AlertDialog(
        onDismissRequest = { viewModel.closeSettingsDialog() },
        title = { Text(stringResource(R.string.support)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.tip_message))
                
                TipTier.entries.forEach { tier ->
                    val product = products.firstOrNull { it.productId == tier.productId }
                    val priceText = product?.oneTimePurchaseOfferDetails?.formattedPrice
                        ?: if (isDebug) {
                            when (tier) {
                                TipTier.BRONZE -> "¥100 (Mock)"
                                TipTier.SILVER -> "¥500 (Mock)"
                                TipTier.GOLD -> "¥1000 (Mock)"
                            }
                        } else tier.productId

                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(tier.badge, style = MaterialTheme.typography.headlineSmall)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tier.name, style = MaterialTheme.typography.labelLarge)
                                Text(priceText, style = MaterialTheme.typography.bodySmall)
                            }
                            Button(
                                onClick = {
                                    if (isDebug && product == null) {
                                        manager.simulateSuccess(tier)
                                    } else {
                                        product?.let { manager.purchase(context as Activity, it) }
                                    }
                                },
                                enabled = isDebug || product != null,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(stringResource(R.string.action_purchase), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                
                when (val s = purchaseState) {
                    is PurchaseState.Success -> Text(stringResource(R.string.purchase_thanks, s.tier.name), color = MaterialTheme.colorScheme.primary)
                    is PurchaseState.Cancelled -> Text(stringResource(R.string.purchase_cancelled), style = MaterialTheme.typography.bodySmall)
                    is PurchaseState.Error -> Text(stringResource(R.string.purchase_failed), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    else -> Unit
                }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.closeSettingsDialog() }) {
                Text(stringResource(R.string.action_close))
            }
        }
    )

    LaunchedEffect(Unit) {
        manager.connect()
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
