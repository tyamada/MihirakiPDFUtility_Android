package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel

@Composable
fun PasswordDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    if (uiState.showPasswordDialog) {
        var password by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.cancelPasswordDialog() },
            title = { Text(stringResource(R.string.dialog_password_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.dialog_password_desc))
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.dialog_password_label)) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.handlePassword(password)
                }) {
                    Text(stringResource(R.string.action_unlock))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelPasswordDialog() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    selectedCount: Int,
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_desc, selectedCount)) },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete))
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
