package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.badge.CommemorativeBadge

/** Manual dismissal in every accessibility mode. No timer, focus request, or grant mutation. */
@Composable
internal fun BadgeAcquiredDialog(badge: CommemorativeBadge, onDismiss: () -> Unit) =
    BadgeInformationDialog(badge, true, onDismiss)

@Composable
internal fun BadgeDetailsDialog(badge: CommemorativeBadge, onDismiss: () -> Unit) =
    BadgeInformationDialog(badge, false, onDismiss)

@Composable
internal fun BadgeDetailsButton(badge: CommemorativeBadge, onOpen: () -> Unit) {
    val label = stringResource(R.string.badge_open_details, stringResource(badge.titleRes))
    OutlinedCard(Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = label, onClick = onOpen)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(badge.iconRes), contentDescription = null, modifier = Modifier.size(64.dp))
            Text(stringResource(badge.titleRes), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun BadgeInformationDialog(badge: CommemorativeBadge, acquired: Boolean, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = AlertDialogDefaults.TonalElevation,
            modifier = Modifier.fillMaxWidth().heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.85f).imePadding()) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.weight(1f, false).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (acquired) Text(stringResource(R.string.badge_acquired_heading),
                        Modifier.fillMaxWidth().semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(badge.titleRes), Modifier.fillMaxWidth().semantics { heading() },
                        style = MaterialTheme.typography.titleLarge)
                    Image(painterResource(badge.iconRes), contentDescription = null,
                        modifier = Modifier.sizeIn(maxWidth = 192.dp, maxHeight = 192.dp).fillMaxWidth().aspectRatio(1f))
                    Text(stringResource(badge.descriptionRes), Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    }
}
