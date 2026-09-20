package jp.mihiraki.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.mihiraki.pdfutility.ui.PdfUiState
import jp.mihiraki.pdfutility.ui.PdfViewModel

@Composable
fun PasswordDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    if (uiState.showPasswordDialog) {
        var password by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.cancelPasswordDialog() },
            title = { Text("Password Required") },
            text = {
                Column {
                    Text("This PDF is password protected.")
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.handlePassword(password)
                }) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelPasswordDialog() }) {
                    Text("Cancel")
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
            title = { Text("Delete Pages") },
            text = { Text("Are you sure you want to delete the $selectedCount selected pages?") },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
