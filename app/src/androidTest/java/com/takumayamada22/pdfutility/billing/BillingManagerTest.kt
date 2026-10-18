package com.takumayamada22.pdfutility.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BillingManagerTest {

    @Test
    fun testClaimKoyomiBadgeAndPersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val billingManager = BillingManager(context)

        billingManager.claimKoyomiBadge()

        assertTrue(billingManager.purchasedTiers.value.contains(TipTier.KOYOMI))

        billingManager.close()
    }
}
