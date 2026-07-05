package com.example.urlcleanapp.data

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OssEntitlementManager : AndroidEntitlementManager {
    private val _isPlusUnlocked = MutableStateFlow(true)
    override val isPlusUnlocked: StateFlow<Boolean> = _isPlusUnlocked.asStateFlow()

    private val _isPurchasePending = MutableStateFlow(false)
    override val isPurchasePending: StateFlow<Boolean> = _isPurchasePending.asStateFlow()

    override fun init() {
        // No-op for OSS
    }

    override fun launchPurchaseFlow(activity: Activity, onError: (String) -> Unit) {
        // No-op for OSS
    }
}
