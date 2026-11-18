package com.takumayamada22.pdfutility.billing

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Cache/callback regression tests. Never connect to Play or initiate a purchase. */
@RunWith(AndroidJUnit4::class)
class BillingManagerTest {
    private lateinit var base: Context
    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private lateinit var preferenceName: String

    @Before
    fun setUp() {
        base = ApplicationProvider.getApplicationContext()
        preferenceName = "billing-test-${UUID.randomUUID()}"
        preferences = base.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        context = object : ContextWrapper(base) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                if (name == "billing_prefs") preferences else super.getSharedPreferences(name, mode)
        }
    }

    @After
    fun tearDown() {
        // Only the unique preferences owned by this test; never the user's billing_prefs.
        base.deleteSharedPreferences(preferenceName)
    }

    @Test
    fun cachedSupportedPurchasesSurviveManagerRecreation() {
        val expected = setOf(TipTier.BRONZE, TipTier.GOLD)
        assertTrue(preferences.edit().putStringSet("purchased_tiers", expected.map { it.name }.toSet()).commit())
        repeat(2) {
            BillingManager(context).use { manager ->
                assertEquals(expected, manager.purchasedTiers.value)
                assertEquals(PurchaseState.Idle, manager.purchase.value)
            }
        }
    }

    @Test
    fun unknownCacheEntriesDoNotGrantPurchasesOrRewriteStoredData() {
        val saved = setOf(TipTier.SILVER.name, "UNKNOWN_TEST_TIER")
        assertTrue(preferences.edit().putStringSet("purchased_tiers", saved).commit())
        BillingManager(context).use { manager ->
            assertEquals(setOf(TipTier.SILVER), manager.purchasedTiers.value)
            assertEquals(saved, preferences.getStringSet("purchased_tiers", emptySet()))
        }
    }

    @Test
    fun emptyCacheStartsIdleWithoutGrantingPurchases() {
        BillingManager(context).use { manager ->
            assertTrue(manager.purchasedTiers.value.isEmpty())
            assertEquals(PurchaseState.Idle, manager.purchase.value)
        }
    }

    @Test
    fun cancelledAndFailedCallbacksPreserveCachedPurchases() {
        val saved = setOf(TipTier.BRONZE.name)
        assertTrue(preferences.edit().putStringSet("purchased_tiers", saved).commit())
        BillingManager(context).use { manager ->
            manager.onPurchasesUpdated(
                BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.USER_CANCELED).build(),
                null
            )
            assertEquals(PurchaseState.Cancelled, manager.purchase.value)
            assertEquals(setOf(TipTier.BRONZE), manager.purchasedTiers.value)
            manager.onPurchasesUpdated(
                BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.ERROR)
                    .setDebugMessage("test error").build(),
                null
            )
            assertEquals(PurchaseState.Error("test error"), manager.purchase.value)
            assertEquals(setOf(TipTier.BRONZE), manager.purchasedTiers.value)
            assertEquals(saved, preferences.getStringSet("purchased_tiers", emptySet()))
        }
    }
}
