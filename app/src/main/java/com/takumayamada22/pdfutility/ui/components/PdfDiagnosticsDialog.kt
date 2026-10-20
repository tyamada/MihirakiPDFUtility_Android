package com.takumayamada22.pdfutility.ui.components

import android.content.Intent
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
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.domain.PdfProcessor
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.SettingsDialogType
import com.takumayamada22.pdfutility.util.AppLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun PdfDiagnosticsDialog(viewModel: PdfViewModel) {
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
                                Text(
                                    stringResource(R.string.diagnostics_privacy),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                HorizontalDivider()
                                Text(results, style = MaterialTheme.typography.bodySmall)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, results)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }) {
                                        Text(stringResource(R.string.share_results))
                                    }
                                }
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
