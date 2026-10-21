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
import com.takumayamada22.pdfutility.util.DiagnosticTestManager
import com.takumayamada22.pdfutility.util.LongRunningTestManager
import com.takumayamada22.pdfutility.util.LongRunningTestProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun PdfDiagnosticsDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    var testResults by remember { mutableStateOf<String?>(null) }
    var showLogs by remember { mutableStateOf(false) }
    var logsText by remember { mutableStateOf("") }
    var enduranceMinutes by remember { mutableStateOf(3) }
    val enduranceProgress by viewModel.enduranceProgress.collectAsState()
    val enduranceStatusMessage by viewModel.enduranceStatusMessage.collectAsState()

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
                    // 1. 総合診断・ベンチマークテストを実行 (機能テスト、編集系機能テスト、負荷テスト、検索・パフォーマンス、デバイス情報含む)
                    Button(
                        onClick = {
                            val benchmarkResult = DiagnosticTestManager.runComprehensiveDiagnostics(context)
                            testResults = benchmarkResult.details
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.run_comprehensive_diagnostics))
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

                    // 3. 長時間ランニング耐久・ストレステスト
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("長時間ランニング耐久テスト (Long-Running Stress Test)", style = MaterialTheme.typography.titleSmall)
                            Text("テスト継続時間を選択し、マンガ全14本結合、1100ページ9回追加、編集ストレステスト、画面回転・折りたたみシミュレーションを連続実行します。", style = MaterialTheme.typography.bodySmall)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(3, 10, 30, 60).forEach { mins ->
                                    Button(
                                        onClick = { enduranceMinutes = mins },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (enduranceMinutes == mins) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("${mins}分")
                                    }
                                }
                            }

                            val isRunning = enduranceProgress?.isRunning == true
                            Button(
                                onClick = {
                                    if (isRunning) {
                                        viewModel.stopEnduranceTest()
                                    } else {
                                        viewModel.startEnduranceTest(enduranceMinutes)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isRunning) "長時間テストを停止 (Stop Test)" else "長時間テストを開始 (${enduranceMinutes}分)")
                            }

                            enduranceProgress?.let { prog ->
                                HorizontalDivider()
                                Text("進捗: 経過 ${prog.elapsedMinutes}分 / 目標 ${prog.targetMinutes}分", style = MaterialTheme.typography.bodySmall)
                                Text("イテレーション: #${prog.currentIteration}", style = MaterialTheme.typography.bodySmall)
                                Text("現在コース: ${prog.currentCourse}", style = MaterialTheme.typography.bodySmall)
                                Text("メモリ使用量: ${prog.memoryUsageMb} MB", style = MaterialTheme.typography.bodySmall)
                            }

                            enduranceStatusMessage?.let { msg ->
                                Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // 4. 不具合を報告
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
