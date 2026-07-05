package com.example.urlcleanapp.data

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface EntitlementManager {
    val isPlusUnlocked: StateFlow<Boolean>
    val isPurchasePending: StateFlow<Boolean>

    fun init()
    fun launchPurchaseFlow(activity: Activity, onError: (String) -> Unit)
}
