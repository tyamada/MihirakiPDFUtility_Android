package jp.mihiraki.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.mihiraki.pdfutility.ui.SplitDirection

@Composable
fun SplitDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (SplitDirection) -> Unit
) {
    if (show) {
        var direction by remember { mutableStateOf(SplitDirection.VERTICAL) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("ページ分割") },
            text = {
                Column {
                    Text("分割方向を選択してください。")
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = direction == SplitDirection.VERTICAL,
                            onClick = { direction = SplitDirection.VERTICAL }
                        )
                        Text("垂直分割 (左右)", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = direction == SplitDirection.HORIZONTAL,
                            onClick = { direction = SplitDirection.HORIZONTAL }
                        )
                        Text("水平分割 (上下)", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onConfirm(direction) }) {
                    Text("実行")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
fun CropDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    if (show) {
        var sliderValue by remember { mutableStateOf(0.1f) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("余白をカット") },
            text = {
                Column {
                    Text("各辺からカットする割合を指定してください。")
                    Spacer(Modifier.height(16.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..0.4f,
                        steps = 7
                    )
                    Text("${(sliderValue * 100).toInt()}% カット", modifier = Modifier.align(Alignment.End))
                }
            },
            confirmButton = {
                Button(onClick = { onConfirm(sliderValue) }) {
                    Text("実行")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル")
                }
            }
        )
    }
}
