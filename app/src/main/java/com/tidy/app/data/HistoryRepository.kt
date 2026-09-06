package com.tidy.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File
import java.util.UUID

@Keep
@Serializable
data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val originalUrl: String,
    val cleanedUrl: String,
    val removedParamsCount: Int,
    val domain: String
)

@Keep
data class HistoryImportResult(
    val newEntriesCount: Int,
    val newTrackersBlocked: Int
)

class HistoryRepository(
    private val context: Context? = null,
    private val testFile: File? = null
) {
    private val historyFile = testFile ?: File(context!!.filesDir, "history.json")
    private val mutex = Mutex()

    private val _historyFlow = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val historyFlow: StateFlow<List<HistoryEntry>> = _historyFlow.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private var cachedEntries: List<HistoryEntry>? = null

    private fun getOrLoadEntries(): List<HistoryEntry> {
        val cached = cachedEntries
        if (cached != null) return cached
        val entries = readFromFile()
        cachedEntries = entries
        _historyFlow.value = entries
        return entries
    }

    suspend fun loadHistory() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                getOrLoadEntries()
            }
        }
    }

    suspend fun addEntry(entry: HistoryEntry) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val current = getOrLoadEntries().toMutableList()
                current.add(0, entry) // newest first
                cachedEntries = current
                writeToFile(current)
                _historyFlow.value = current
            }
        }
    }

    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val empty = emptyList<HistoryEntry>()
                cachedEntries = empty
                writeToFile(empty)
                _historyFlow.value = empty
            }
        }
    }

    suspend fun exportToJson(): String {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                json.encodeToString(getOrLoadEntries())
            }
        }
    }

    suspend fun getEntriesSnapshot(): List<HistoryEntry> {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                getOrLoadEntries()
            }
        }
    }

    suspend fun importFromJson(jsonStr: String): Int {
        return importFromJsonWithResult(jsonStr)?.newEntriesCount ?: -1
    }

    suspend fun importFromJsonWithResult(jsonStr: String): HistoryImportResult? {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    val imported = parseJson(jsonStr)
                    val current = getOrLoadEntries().toMutableList()
                    val existingIds = current.map { it.id }.toSet()
                    val newEntries = imported.filter { it.id !in existingIds }
                    if (newEntries.isNotEmpty()) {
                        current.addAll(0, newEntries)
                        current.sortByDescending { it.timestamp }
                        cachedEntries = current
                        writeToFile(current)
                        _historyFlow.value = current
                    }
                    HistoryImportResult(
                        newEntriesCount = newEntries.size,
                        newTrackersBlocked = newEntries.sumOf { it.removedParamsCount }
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    private fun readFromFile(): List<HistoryEntry> {
        if (!historyFile.exists()) return emptyList()
        return try {
            parseJson(historyFile.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseJson(jsonStr: String): List<HistoryEntry> {
        return when (val payload = json.parseToJsonElement(jsonStr)) {
            is JsonArray -> json.decodeFromJsonElement<List<HistoryEntry>>(payload)
            is JsonObject -> {
                val history = requireNotNull(payload["history"]) {
                    "Migration backup does not contain history"
                }
                json.decodeFromJsonElement<List<HistoryEntry>>(history)
            }

            else -> error("History import must be an array or migration backup")
        }
    }

    private fun writeToFile(entries: List<HistoryEntry>) {
        val jsonStr = json.encodeToString(entries)
        historyFile.writeText(jsonStr)
    }

    companion object {
        fun extractDomain(url: String): String {
            var temp = url
            val protoIndex = temp.indexOf("://")
            if (protoIndex != -1) temp = temp.substring(protoIndex + 3)
            val slashIndex = temp.indexOf('/')
            if (slashIndex != -1) temp = temp.substring(0, slashIndex)
            val qIndex = temp.indexOf('?')
            if (qIndex != -1) temp = temp.substring(0, qIndex)
            val hashIndex = temp.indexOf('#')
            if (hashIndex != -1) temp = temp.substring(0, hashIndex)
            val portIndex = temp.indexOf(':')
            if (portIndex != -1) temp = temp.substring(0, portIndex)
            return temp.trim().lowercase()
        }
    }
}
