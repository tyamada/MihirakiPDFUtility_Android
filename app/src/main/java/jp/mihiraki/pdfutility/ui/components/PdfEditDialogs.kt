package jp.mihiraki.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.mihiraki.pdfutility.R
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
            title = { Text(stringResource(R.string.split_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.split_desc))
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = direction == SplitDirection.VERTICAL,
                            onClick = { direction = SplitDirection.VERTICAL }
                        )
                        Text(stringResource(R.string.split_vertical), modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = direction == SplitDirection.HORIZONTAL,
                            onClick = { direction = SplitDirection.HORIZONTAL }
                        )
                        Text(stringResource(R.string.split_horizontal), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onConfirm(direction) }) {
                    Text(stringResource(R.string.action_run))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
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
            title = { Text(stringResource(R.string.crop_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.crop_desc))
                    Spacer(Modifier.height(16.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..0.4f,
                        steps = 7
                    )
                    Text(stringResource(R.string.crop_percent, (sliderValue * 100).toInt()), modifier = Modifier.align(Alignment.End))
                }
            },
            confirmButton = {
                Button(onClick = { onConfirm(sliderValue) }) {
                    Text(stringResource(R.string.action_run))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
