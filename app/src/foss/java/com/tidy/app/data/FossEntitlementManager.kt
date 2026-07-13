package com.tidy.app.data

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FossEntitlementManager : AndroidEntitlementManager {
    private val _isPlusUnlocked = MutableStateFlow(false)
    override val isPlusUnlocked: StateFlow<Boolean> = _isPlusUnlocked.asStateFlow()

    private val _isPurchasePending = MutableStateFlow(false)
    override val isPurchasePending: StateFlow<Boolean> = _isPurchasePending.asStateFlow()

    override fun init() {
        // No-op for FOSS
    }

    override fun launchPurchaseFlow(activity: Activity, onError: (String) -> Unit) {
        // No-op for FOSS
    }
}
