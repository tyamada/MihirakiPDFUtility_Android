package jp.mihiraki.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.mihiraki.pdfutility.ui.PdfUiState
import jp.mihiraki.pdfutility.ui.PdfViewModel

@Composable
fun PdfSettingsDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    if (uiState.showSettingsDialog) {
        var title by remember { mutableStateOf(uiState.title) }
        var author by remember { mutableStateOf(uiState.author) }
        var subject by remember { mutableStateOf(uiState.subject) }
        var keywords by remember { mutableStateOf(uiState.keywords) }
        var savePassword by remember { mutableStateOf(uiState.savePassword) }

        AlertDialog(
            onDismissRequest = { viewModel.toggleSettingsDialog() },
            title = { Text("PDF Settings") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        fullWidth = true
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author") },
                        fullWidth = true
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        fullWidth = true
                    )
                    OutlinedTextField(
                        value = keywords,
                        onValueChange = { keywords = it },
                        label = { Text("Keywords") },
                        fullWidth = true
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text("Security", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = savePassword,
                        onValueChange = { savePassword = it },
                        label = { Text("Encryption Password") },
                        placeholder = { Text("Leave empty for no password") },
                        fullWidth = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateMetadata(title, author, subject, keywords)
                    viewModel.setSavePassword(savePassword)
                    viewModel.toggleSettingsDialog()
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleSettingsDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper to avoid duplicate code, though Material3 TextField doesn't have 'fullWidth' param directly like this
@Composable
private fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    placeholder: (@Composable () -> Unit)? = null,
    fullWidth: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        modifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier,
        singleLine = true
    )
}
