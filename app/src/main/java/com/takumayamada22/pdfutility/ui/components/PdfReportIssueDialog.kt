package com.takumayamada22.pdfutility.ui.components

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType
import com.takumayamada22.pdfutility.util.DeviceTestRunner
import com.takumayamada22.pdfutility.util.DiagnosticTestManager
import com.takumayamada22.pdfutility.util.PersistentLogManager

@Composable
internal fun PdfReportIssueDialog(viewModel: PdfViewModel) {
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
                            val benchmarkResult = DiagnosticTestManager.runComprehensiveDiagnostics(context)
                            val logs = PersistentLogManager.getAllLogs().joinToString("\n")
                            val combined = "$testReport\n\n--- Comprehensive Diagnostics & Benchmarks ---\n${benchmarkResult.details}\n\n--- App Logs (${PersistentLogManager.getAllLogs().size} lines) ---\n$logs"
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
                            val intent = Intent(Intent.ACTION_VIEW, "https://github.com/tyamada/MihirakiPDFUtility_Android/issues/new?template=bug_report.md".toUri())
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
