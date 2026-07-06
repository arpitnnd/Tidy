package com.tidy.app.data

import kotlinx.coroutines.flow.StateFlow

interface EntitlementManager {
    val isPlusUnlocked: StateFlow<Boolean>
    val isPurchasePending: StateFlow<Boolean>
}
