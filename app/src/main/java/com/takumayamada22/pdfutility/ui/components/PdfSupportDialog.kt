package com.takumayamada22.pdfutility.ui.components

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.billing.PurchaseState
import com.takumayamada22.pdfutility.billing.TipTier
import com.takumayamada22.pdfutility.ui.PdfViewModel

@Composable
internal fun PdfSupportDialog(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val manager = viewModel.billing
    val products by manager.products.collectAsState()
    val purchaseState by manager.purchase.collectAsState()
    val purchasedTiers by manager.purchasedTiers.collectAsState()

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
                    text = stringResource(R.string.support),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.tip_message))
                    Text(
                        stringResource(R.string.free_use_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TipTier.entries.forEach { tier ->
                        val product = products.firstOrNull { it.productId == tier.productId }
                        val priceText = product?.oneTimePurchaseOfferDetails?.formattedPrice ?: tier.productId

                        val isOwned = purchasedTiers.contains(tier)

                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val iconRes = when (tier) {
                                    TipTier.BRONZE -> R.drawable.tip_bronze
                                    TipTier.SILVER -> R.drawable.tip_silver
                                    TipTier.GOLD -> R.drawable.tip_gold
                                    TipTier.KOYOMI -> R.drawable.tameshibu_badge_koyomi
                                }
                                Image(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (tier == TipTier.KOYOMI) stringResource(R.string.commemorative_badge_title) else tier.name,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(priceText, style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = {
                                        product?.let { manager.purchase(context as Activity, it) }
                                    },
                                    enabled = !isOwned && product != null,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isOwned) stringResource(R.string.action_owned) else stringResource(R.string.action_purchase),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                    
                    when (val s = purchaseState) {
                        is PurchaseState.Success -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.purchase_thanks, if (s.tier == TipTier.KOYOMI) "Koyomi" else s.tier.name), color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.purchase_badge_shown), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        is PurchaseState.Cancelled -> Text(stringResource(R.string.purchase_cancelled), style = MaterialTheme.typography.bodySmall)
                        is PurchaseState.Error -> Text(stringResource(R.string.purchase_failed), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        else -> Unit
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

    LaunchedEffect(Unit) {
        manager.connect()
    }
}
