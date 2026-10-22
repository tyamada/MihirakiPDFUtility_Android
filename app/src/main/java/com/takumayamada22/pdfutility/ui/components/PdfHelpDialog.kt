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
        Triple(Icons.Default.FileOpen, "開く / Open", "PDFファイルを読み込みます"),
        Triple(Icons.Default.Save, "保存 / Save", "すべてのページをPDFとして保存します"),
        Triple(Icons.Default.Add, "PDF追加 / Add PDF", "別のPDFを末尾に結合・追加します"),
        Triple(Icons.Default.SelectAll, "すべて選択 / Select All", "すべてのページを選択状態にします"),
        Triple(Icons.Default.Close, "選択解除 / Clear", "ページ選択をすべて解除します"),
        Triple(Icons.Default.Flip, "選択反転 / Invert", "選択状態を反転させます"),
        Triple(Icons.AutoMirrored.Filled.RotateRight, "回転 / Rotate", "選択したページを90度単位で回転します"),
        Triple(Icons.AutoMirrored.Filled.ArrowForward, "移動 / Move", "選択ページの順序を前後に移動します"),
        Triple(Icons.Default.ContentCut, "ページ分割 / Split", "見開きページを左右に分割します"),
        Triple(Icons.Default.AddBox, "白紙挿入 / Insert Blank", "選択した位置に空白ページを挿入します"),
        Triple(Icons.Default.Delete, "削除 / Delete", "選択したページを削除します"),
        Triple(Icons.Default.Output, "選択保存 / Export Selected", "選択したページのみを書き出します"),
        Triple(Icons.Default.GridView, "表示切替 / Toggle View", "グリッド表示と見開き表示を切り替えます"),
        Triple(Icons.AutoMirrored.Filled.MenuBook, "右開き切替 / RTL", "右開き(RTL)と左開き(LTR)を切り替えます"),
        Triple(Icons.Default.Description, "プロパティ / Properties", "タイトルやメタデータを編集します"),
        Triple(Icons.Default.Settings, "設定 / Settings", "パスワード保護やオプションを設定します")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("トップツールバーの主要なボタンとアイコンの一覧です。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                stringResource(R.string.help_item_display_modes),
                stringResource(R.string.help_item_device_optimization)
            )
        ),
        HelpCategorySpec(
            title = "ボタン・アイコン一覧 / Toolbar Cheat Sheet",
            content = { ToolbarCheatSheetSection() }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = {
                        viewModel.closeSettingsDialog()
                        viewModel.showTutorialAgain()
                    }) {
                        Text(stringResource(R.string.help_show_tutorial))
                    }
                    Button(onClick = { viewModel.closeSettingsDialog() }) {
                        Text(stringResource(R.string.action_close))
                    }
                }
            }
        }
    }
}
