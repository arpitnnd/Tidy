package com.tidy.app.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HistoryRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var historyFile: File
    private lateinit var repository: HistoryRepository

    @Before
    fun setUp() {
        historyFile = File(tempFolder.root, "history.json")
        repository = HistoryRepository(testFile = historyFile)
    }

    private fun sampleEntry(
        originalUrl: String = "https://example.com/page?utm_source=x",
        cleanedUrl: String = "https://example.com/page",
        removedParamsCount: Int = 1
    ) = HistoryEntry(
        originalUrl = originalUrl,
        cleanedUrl = cleanedUrl,
        removedParamsCount = removedParamsCount,
        domain = HistoryRepository.extractDomain(cleanedUrl)
    )

    @Test
    fun addEntryInsertsNewestFirst() = runTest {
        repository.addEntry(sampleEntry(originalUrl = "https://a.com/1", cleanedUrl = "https://a.com/1"))
        repository.addEntry(sampleEntry(originalUrl = "https://b.com/2", cleanedUrl = "https://b.com/2"))

        val history = repository.historyFlow.value
        assertEquals(2, history.size)
        assertEquals("https://b.com/2", history[0].cleanedUrl)
        assertEquals("https://a.com/1", history[1].cleanedUrl)
    }

    @Test
    fun clearAllEmptiesInMemoryAndPersistedState() = runTest {
        repository.addEntry(sampleEntry())
        repository.clearAll()

        assertTrue(repository.historyFlow.value.isEmpty())

        val reloaded = HistoryRepository(testFile = historyFile)
        reloaded.loadHistory()
        assertTrue(reloaded.historyFlow.value.isEmpty())
    }

    @Test
    fun persistsAcrossRepositoryInstances() = runTest {
        repository.addEntry(sampleEntry(originalUrl = "https://a.com/1?x=1", cleanedUrl = "https://a.com/1"))

        val reloaded = HistoryRepository(testFile = historyFile)
        reloaded.loadHistory()

        assertEquals(1, reloaded.historyFlow.value.size)
        assertEquals("https://a.com/1", reloaded.historyFlow.value[0].cleanedUrl)
    }

    @Test
    fun exportThenImportRoundTripsEntries() = runTest {
        repository.addEntry(sampleEntry(originalUrl = "https://a.com/1", cleanedUrl = "https://a.com/1"))
        repository.addEntry(sampleEntry(originalUrl = "https://b.com/2", cleanedUrl = "https://b.com/2"))
        val exported = repository.exportToJson()

        repository.clearAll()
        assertTrue(repository.historyFlow.value.isEmpty())

        val importedCount = repository.importFromJson(exported)

        assertEquals(2, importedCount)
        val urls = repository.historyFlow.value.map { it.cleanedUrl }.toSet()
        assertEquals(setOf("https://a.com/1", "https://b.com/2"), urls)
    }

    @Test
    fun importSkipsEntriesWithIdsAlreadyPresent() = runTest {
        repository.addEntry(sampleEntry())
        val exported = repository.exportToJson()

        val importedCount = repository.importFromJson(exported)

        assertEquals(0, importedCount)
        assertEquals(1, repository.historyFlow.value.size)
    }

    @Test
    fun importReturnsNegativeOneOnMalformedJsonAndLeavesHistoryUntouched() = runTest {
        repository.addEntry(sampleEntry())

        val result = repository.importFromJson("{ not valid json ")

        assertEquals(-1, result)
        assertEquals(1, repository.historyFlow.value.size)
    }

    @Test
    fun extractDomainStripsSchemePortPathQueryAndFragment() {
        assertEquals("example.com", HistoryRepository.extractDomain("https://example.com/path?x=1#frag"))
        assertEquals("example.com", HistoryRepository.extractDomain("http://EXAMPLE.com:8080/path"))
        assertEquals("m.example.com", HistoryRepository.extractDomain("https://m.example.com/"))
        assertEquals("example.com", HistoryRepository.extractDomain("example.com"))
    }
}
