package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R

/** An empty document is a starting point, not an editing toolbar. No saved state is changed here. */
@Composable
internal fun PdfStartScreen(
    isBusy: Boolean,
    onOpen: () -> Unit,
    onOpenFolder: () -> Unit,
    onSample: () -> Unit,
    onAppend: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = useWideStartLayout(maxWidth.value, maxHeight.value, LocalDensity.current.fontScale)
        val content = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
        if (wide) {
            Row(content, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StartPrimaryAction(isBusy, onOpen)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StartSecondaryActions(isBusy, onOpenFolder, onSample, onAppend)
                }
            }
        } else {
            Column(content, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StartPrimaryAction(isBusy, onOpen)
                StartSecondaryActions(isBusy, onOpenFolder, onSample, onAppend)
            }
        }
    }
}

/** Use available space, including font scaling, rather than device/orientation names. */
internal fun useWideStartLayout(widthDp: Float, heightDp: Float, fontScale: Float): Boolean =
    widthDp / fontScale.coerceAtLeast(1f) >= 600f && widthDp > heightDp

@Composable
private fun StartPrimaryAction(isBusy: Boolean, onOpen: () -> Unit) {
    Text(stringResource(R.string.start_title), style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.semantics { heading() })
    Text(stringResource(R.string.start_description), style = MaterialTheme.typography.bodyLarge)
    Button(onClick = onOpen, enabled = !isBusy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(stringResource(R.string.start_open_pdf))
    }
}

@Composable
private fun StartSecondaryActions(isBusy: Boolean, onOpenFolder: () -> Unit, onSample: () -> Unit, onAppend: () -> Unit) {
    OutlinedButton(onClick = onOpenFolder, enabled = !isBusy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(stringResource(R.string.menu_open_folder))
    }
    OutlinedButton(onClick = onSample, enabled = !isBusy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(stringResource(R.string.sample_pdf))
    }
    OutlinedButton(onClick = onAppend, enabled = !isBusy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(stringResource(R.string.action_add_pdf))
    }
}
