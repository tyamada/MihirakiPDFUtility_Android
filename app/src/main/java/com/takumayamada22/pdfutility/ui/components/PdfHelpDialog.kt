package com.takumayamada22.pdfutility.ui.components

import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.PdfViewModel

internal data class HelpCategorySpec(
    val title: String,
    val items: List<String> = emptyList(),
    val content: @Composable (() -> Unit)? = null
)

@Composable
internal fun ToolbarCheatSheetSection() {
    val items = listOf(
        Triple(Icons.Default.FileOpen, stringResource(R.string.help_tool_open_title), stringResource(R.string.help_tool_open_desc)),
        Triple(Icons.Default.Save, stringResource(R.string.help_tool_save_title), stringResource(R.string.help_tool_save_desc)),
        Triple(Icons.Default.Add, stringResource(R.string.help_tool_add_title), stringResource(R.string.help_tool_add_desc)),
        Triple(Icons.Default.SelectAll, stringResource(R.string.help_tool_select_all_title), stringResource(R.string.help_tool_select_all_desc)),
        Triple(Icons.Default.Close, stringResource(R.string.help_tool_clear_title), stringResource(R.string.help_tool_clear_desc)),
        Triple(Icons.Default.Flip, stringResource(R.string.help_tool_invert_title), stringResource(R.string.help_tool_invert_desc)),
        Triple(Icons.AutoMirrored.Filled.RotateRight, stringResource(R.string.help_tool_rotate_title), stringResource(R.string.help_tool_rotate_desc)),
        Triple(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.help_tool_move_title), stringResource(R.string.help_tool_move_desc)),
        Triple(Icons.Default.ContentCut, stringResource(R.string.help_tool_split_title), stringResource(R.string.help_tool_split_desc)),
        Triple(Icons.Default.AddBox, stringResource(R.string.help_tool_insert_blank_title), stringResource(R.string.help_tool_insert_blank_desc)),
        Triple(Icons.Default.Delete, stringResource(R.string.help_tool_delete_title), stringResource(R.string.help_tool_delete_desc)),
        Triple(Icons.Default.Output, stringResource(R.string.help_tool_export_title), stringResource(R.string.help_tool_export_desc)),
        Triple(Icons.Default.GridView, stringResource(R.string.help_tool_toggle_view_title), stringResource(R.string.help_tool_toggle_view_desc)),
        Triple(Icons.AutoMirrored.Filled.MenuBook, stringResource(R.string.help_tool_rtl_title), stringResource(R.string.help_tool_rtl_desc)),
        Triple(Icons.Default.Description, stringResource(R.string.help_tool_properties_title), stringResource(R.string.help_tool_properties_desc)),
        Triple(Icons.Default.Settings, stringResource(R.string.help_tool_settings_title), stringResource(R.string.help_tool_settings_desc))
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.help_toolbar_cheat_sheet_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        items.forEach { (icon, title, desc) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun PdfHelpDialog(viewModel: PdfViewModel) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val categories = listOf(
        HelpCategorySpec(
            title = stringResource(R.string.help_category_getting_started),
            items = listOf(
                stringResource(R.string.help_item_overview),
                stringResource(R.string.help_item_open_save)
            )
        ),
        HelpCategorySpec(
            title = stringResource(R.string.help_category_menu_bar),
            items = listOf(
                stringResource(R.string.help_item_menu_bar),
                stringResource(R.string.help_item_mobile_bottom_sheet),
                stringResource(R.string.help_item_toolbar_customization)
            )
        ),
        HelpCategorySpec(
            title = stringResource(R.string.help_category_editing_features),
            items = listOf(
                stringResource(R.string.help_item_page_ops),
                stringResource(R.string.help_item_display_modes),
                stringResource(R.string.help_item_device_optimization)
            )
        ),
        HelpCategorySpec(
            title = stringResource(R.string.help_category_toolbar_cheat_sheet),
            content = { ToolbarCheatSheetSection() }
        ),
        HelpCategorySpec(
            title = stringResource(R.string.help_category_settings_guide),
            items = listOf(
                stringResource(R.string.help_item_properties),
                stringResource(R.string.help_item_security),
                stringResource(R.string.help_item_options),
                stringResource(R.string.help_item_support),
                stringResource(R.string.help_item_privacy_policy)
            )
        ),
        HelpCategorySpec(
            title = stringResource(R.string.help_category_diagnostics),
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
                                        category.content?.invoke()
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.closeSettingsDialog()
                            viewModel.showTutorialAgain()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.help_show_tutorial))
                    }
                    Button(
                        onClick = { viewModel.closeSettingsDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.action_close))
                    }
                }
            }
        }
    }
}
