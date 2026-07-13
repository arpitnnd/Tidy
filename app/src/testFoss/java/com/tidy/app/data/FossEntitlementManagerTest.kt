package com.tidy.app.data

import android.app.Activity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.fail
import org.junit.Test

/**
 * Regression guard for the bug where isPlusUnlocked was hardcoded true, unlocking Tidy+
 * features (Material You, share automation) in the foss build for free. The foss flavour
 * has no purchase flow, so entitlement must always report false.
 */
class FossEntitlementManagerTest {

    private val manager = FossEntitlementManager()

    @Test
    fun isPlusUnlocked_isAlwaysFalse() {
        assertFalse(manager.isPlusUnlocked.value)
    }

    @Test
    fun isPurchasePending_isAlwaysFalse() {
        assertFalse(manager.isPurchasePending.value)
    }

    @Test
    fun launchPurchaseFlow_isANoOp() {
        manager.launchPurchaseFlow(Activity()) { fail("onError should never be invoked in foss") }
        // No exception, no callback -- confirms the no-op didn't touch the activity or report an error.
        assertEquals(false, manager.isPlusUnlocked.value)
    }
}
