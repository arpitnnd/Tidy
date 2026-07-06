package com.tidy.app.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.service.quicksettings.TileService
import android.widget.Toast
import com.tidy.app.TidyURLApp
import com.tidy.app.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TidyTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val text = item?.text?.toString()?.trim()
            if (text != null && (text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true) || (text.contains(".") && !text.contains(" ")))) {
                val formattedText = if (!text.startsWith("http://", ignoreCase = true) && !text.startsWith("https://", ignoreCase = true)) {
                    "https://$text"
                } else {
                    text
                }

                val settings = TidyURLApp.instance.settingsRepository
                val whitelist = runBlocking { settings.whitelistedDomains.first() }
                val customBlacklist = runBlocking { settings.blacklistedParams.first() }
                val domainParams = runBlocking { settings.domainWhitelistedParams.first() }
                val removeMobile = runBlocking { settings.autoRemoveMobileSubdomains.first() }

                val cleanResult = UrlCleaner().clean(
                    urlStr = formattedText,
                    whitelistedDomains = whitelist,
                    customBlacklistParams = customBlacklist,
                    domainWhitelistedParams = domainParams,
                    removeMobileSubdomains = removeMobile
                )
                if (cleanResult.removedParams.isNotEmpty()) {
                    runBlocking {
                        settings.setLastCleanedUrl(cleanResult.cleanedUrl)
                    }
                    val clip = ClipData.newPlainText(getString(R.string.main_cleaned_url), cleanResult.cleanedUrl)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(applicationContext, getString(R.string.toast_cleaned_copied), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, getString(R.string.toast_already_clean), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, getString(R.string.toast_no_url_in_clipboard), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext, getString(R.string.toast_clipboard_empty), Toast.LENGTH_SHORT).show()
        }
    }
}
