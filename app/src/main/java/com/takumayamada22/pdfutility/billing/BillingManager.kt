package com.takumayamada22.pdfutility.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PurchaseState {
    data object Idle : PurchaseState
    data object Cancelled : PurchaseState
    data class Error(val message: String) : PurchaseState
    data class Success(val tier: TipTier) : PurchaseState
}

class BillingManager(context: Context) : PurchasesUpdatedListener, AutoCloseable {
    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products = _products.asStateFlow()
    private val _purchase = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchase = _purchase.asStateFlow()
    
    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun connect() {
        Log.d("Billing", "Starting connection...")
        client.startConnection(
            object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Log.d("Billing", "Service disconnected")
                }
                override fun onBillingSetupFinished(result: BillingResult) {
                    Log.d("Billing", "Setup finished: ${result.responseCode} - ${result.debugMessage}")
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        query()
                    } else {
                        _purchase.value = PurchaseState.Error("Setup Error: ${result.debugMessage} (Code ${result.responseCode})")
                    }
                }
            },
        )
    }

    private fun query() {
        Log.d("Billing", "Querying products...")
        val productList = TipTier.entries.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        client.queryProductDetailsAsync(params) { billingResult, queryResult ->
            Log.d("Billing", "Query result code: ${billingResult.responseCode}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = queryResult
            } else {
                _purchase.value = PurchaseState.Error("Query Error: ${billingResult.debugMessage} (Code ${billingResult.responseCode})")
            }
        }
    }

    fun purchase(activity: Activity, details: ProductDetails) {
        val params = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()
        val result = client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchase.value = PurchaseState.Error(result.debugMessage)
        }
    }

    fun simulateSuccess(tier: TipTier) {
        _purchase.value = PurchaseState.Success(tier)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchase.value = PurchaseState.Cancelled
            return
        }
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchase.value = PurchaseState.Error(result.debugMessage)
            return
        }
        purchases.orEmpty().filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
            purchase.products.firstNotNullOfOrNull(TipTier::fromProductId)?.let { tier ->
                client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) { consumed: BillingResult, _: String? ->
                    _purchase.value = if (consumed.responseCode == BillingClient.BillingResponseCode.OK) {
                        PurchaseState.Success(tier)
                    } else {
                        PurchaseState.Error(consumed.debugMessage)
                    }
                }
            }
        }
    }

    override fun close() {
        client.endConnection()
    }
}
