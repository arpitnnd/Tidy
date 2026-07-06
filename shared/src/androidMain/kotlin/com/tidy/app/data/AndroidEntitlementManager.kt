package com.tidy.app.data

import android.app.Activity

interface AndroidEntitlementManager : EntitlementManager {
    fun init()
    fun launchPurchaseFlow(activity: Activity, onError: (String) -> Unit)
}
