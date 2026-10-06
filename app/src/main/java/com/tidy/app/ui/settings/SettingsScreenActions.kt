package com.tidy.app.ui.settings

import android.content.Context
import com.tidy.app.data.BlocklistSyncer
import com.tidy.app.data.ClipboardCleanTier
import com.tidy.app.data.ShareCleanTier

/** Actions supplied by the runtime host or a deterministic preview. */
interface SettingsScreenActions {
    fun refreshBlocklist(context: Context, onResult: (BlocklistSyncer.SyncResult) -> Unit) {}
    fun setCheckClipboardForLinks(enabled: Boolean) {}
    fun setClipboardCleanTier(tier: ClipboardCleanTier) {}
    fun setShareCleanTier(tier: ShareCleanTier) {}
    fun setCloseInsteadOfSharing(enabled: Boolean) {}
    fun onDomainInputChanged(value: String) {}
    fun onParamInputChanged(value: String) {}
    fun addDomain() {}
    fun removeDomain(domain: String) {}
    fun addParam() {}
    fun removeParam(param: String) {}
    fun setAutoExpandShortUrls(enabled: Boolean) {}
    fun setAutoRemoveMobileSubdomains(enabled: Boolean) {}
    fun setDropTrailingSlash(enabled: Boolean) {}
    fun setAutoCleanOnInput(enabled: Boolean) {}
    fun setAllowSystemBackup(enabled: Boolean) {}
    fun onNewParamWhitelistDomainChanged(value: String) {}
    fun onNewParamWhitelistParamChanged(value: String) {}
    fun addDomainWhitelistedParam() {}
    fun removeDomainWhitelistedParam(entry: String) {}
}
