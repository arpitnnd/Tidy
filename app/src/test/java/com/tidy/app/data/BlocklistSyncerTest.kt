package com.tidy.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

class BlocklistSyncerTest {

    // In-memory fake DataStore for preferences to avoid background disk I/O in unit tests.
    private class FakeDataStore : DataStore<Preferences> {
        val stateFlow = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = stateFlow
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val new = transform(stateFlow.value)
            stateFlow.value = new
            return new
        }
    }

    private lateinit var settingsRepository: SettingsRepository
    private val context: Context = mock(Context::class.java)
    private val servers = mutableListOf<HttpServer>()

    @Before
    fun setUp() {
        settingsRepository = SettingsRepository(FakeDataStore())
    }

    @After
    fun tearDown() {
        servers.forEach { it.stop(0) }
        servers.clear()
    }

    private fun startServer(handler: (HttpExchange) -> Unit): String {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange -> handler(exchange) }
        server.start()
        servers.add(server)
        return "http://127.0.0.1:${server.address.port}/trackers.json"
    }

    @Test
    fun fetchesAndStoresBlocklistWhenCacheIsStale() = runTest {
        val body = """[{"name":"custom_tracker","description":"test tracker"}]"""
        val url = startServer { exchange ->
            exchange.responseHeaders.add("ETag", "\"v1\"")
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        BlocklistSyncer.sync(context, settingsRepository, url)

        assertEquals(body, settingsRepository.blocklistJson.first())
        assertEquals("\"v1\"", settingsRepository.blocklistEtag.first())
        assertTrue(settingsRepository.blocklistLastFetchTime.first() > 0)
    }

    @Test
    fun skipsNetworkCallWhenCacheIsFresh() = runTest {
        val hits = AtomicInteger(0)
        val url = startServer { exchange ->
            hits.incrementAndGet()
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }
        settingsRepository.setBlocklistLastFetchTime(System.currentTimeMillis())

        BlocklistSyncer.sync(context, settingsRepository, url)

        assertEquals(0, hits.get())
    }

    @Test
    fun leavesBlocklistUnchangedOnNotModified() = runTest {
        settingsRepository.setBlocklistJson(SettingsRepository.DEFAULT_BLOCKLIST_JSON)
        val url = startServer { exchange ->
            exchange.sendResponseHeaders(304, -1)
            exchange.close()
        }

        BlocklistSyncer.sync(context, settingsRepository, url)

        assertEquals(SettingsRepository.DEFAULT_BLOCKLIST_JSON, settingsRepository.blocklistJson.first())
        assertTrue(settingsRepository.blocklistLastFetchTime.first() > 0)
    }

    @Test
    fun ignoresMalformedResponseBody() = runTest {
        settingsRepository.setBlocklistJson(SettingsRepository.DEFAULT_BLOCKLIST_JSON)
        val url = startServer { exchange ->
            val bytes = "not valid json".toByteArray()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        BlocklistSyncer.sync(context, settingsRepository, url)

        assertEquals(SettingsRepository.DEFAULT_BLOCKLIST_JSON, settingsRepository.blocklistJson.first())
    }

    @Test
    fun ignoresEmptyTrackerArray() = runTest {
        settingsRepository.setBlocklistJson(SettingsRepository.DEFAULT_BLOCKLIST_JSON)
        val url = startServer { exchange ->
            val bytes = "[]".toByteArray()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        BlocklistSyncer.sync(context, settingsRepository, url)

        assertEquals(SettingsRepository.DEFAULT_BLOCKLIST_JSON, settingsRepository.blocklistJson.first())
    }

    @Test
    fun networkFailureLeavesStateUntouched() = runTest {
        // Nothing is listening on this port.
        BlocklistSyncer.sync(context, settingsRepository, "http://127.0.0.1:1/trackers.json")

        assertEquals(0L, settingsRepository.blocklistLastFetchTime.first())
        assertEquals(SettingsRepository.DEFAULT_BLOCKLIST_JSON, settingsRepository.blocklistJson.first())
    }
}
