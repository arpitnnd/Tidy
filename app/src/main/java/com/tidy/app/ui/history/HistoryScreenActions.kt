package com.tidy.app.ui.history

/** Actions supplied by the runtime host or a deterministic preview. */
interface HistoryScreenActions {
    fun showClearConfirmation() {}
    fun dismissClearConfirmation() {}
    fun clearHistory(onResult: (Boolean) -> Unit = {}) {}
    fun undoClearHistory(onResult: (Boolean) -> Unit = {}) {}
    fun discardClearUndo() {}
    suspend fun getExportJson(): String = "[]"
    fun importHistory(json: String, onResult: (Int) -> Unit) {}
    fun addDomainWhitelistedParam(domain: String, param: String) {}
}
