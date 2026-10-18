package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel

@Composable
internal fun PdfPasswordDialog(viewModel: PdfViewModel, uiState: PdfUiState) {
    var password by remember { mutableStateOf(uiState.savePassword) }

    AlertDialog(
        onDismissRequest = { viewModel.closeSettingsDialog() },
        title = { Text(stringResource(R.string.settings_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.security_desc))
                PdfSettingsTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.security_password_label)) },
                    placeholder = { Text(stringResource(R.string.security_not_set)) }
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    password = ""
                    viewModel.setSavePassword("")
                }) {
                    Text(stringResource(R.string.action_clear))
                }
                Button(onClick = {
                    viewModel.setSavePassword(password)
                    viewModel.closeSettingsDialog()
                }) {
                    Text(stringResource(R.string.action_set))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeSettingsDialog() }) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
