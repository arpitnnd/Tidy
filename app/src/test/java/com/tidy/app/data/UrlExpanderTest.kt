package com.tidy.app.data

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

class UrlExpanderTest {

    private val servers = mutableListOf<HttpServer>()

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
        return "http://127.0.0.1:${server.address.port}/"
    }

    // isShortUrl --------------------------------------------------------

    @Test
    fun recognizesKnownShortenerDomains() {
        assertTrue(UrlExpander.isShortUrl("https://bit.ly/abc123"))
        assertTrue(UrlExpander.isShortUrl("http://tinyurl.com/xyz"))
        assertTrue(UrlExpander.isShortUrl("t.co/abc"))
        assertTrue(UrlExpander.isShortUrl("https://cutt.ly/abc"))
    }

    @Test
    fun recognizesSubdomainsOfKnownShorteners() {
        assertTrue(UrlExpander.isShortUrl("https://www.bit.ly/abc"))
    }

    @Test
    fun rejectsUnknownDomains() {
        assertFalse(UrlExpander.isShortUrl("https://example.com/page"))
        assertFalse(UrlExpander.isShortUrl("https://notbit.ly.evil.com/abc"))
    }

    @Test
    fun isCaseInsensitiveOnHost() {
        assertTrue(UrlExpander.isShortUrl("https://BIT.LY/AbC"))
    }

    // resolve -------------------------------------------------------------

    @Test
    fun resolveReturnsSameUrlWhenTargetRespondsOk() = runTest {
        val url = startServer { exchange ->
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }

        assertEquals(url, UrlExpander.resolve(url))
    }

    @Test
    fun resolveFollowsASingleRedirect() = runTest {
        val target = startServer { exchange ->
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }
        val redirector = startServer { exchange ->
            exchange.responseHeaders.add("Location", target)
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }

        assertEquals(target, UrlExpander.resolve(redirector))
    }

    @Test
    fun resolveFollowsAChainOfRedirects() = runTest {
        val target = startServer { exchange ->
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }
        val hop2 = startServer { exchange ->
            exchange.responseHeaders.add("Location", target)
            exchange.sendResponseHeaders(301, -1)
            exchange.close()
        }
        val hop1 = startServer { exchange ->
            exchange.responseHeaders.add("Location", hop2)
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }

        assertEquals(target, UrlExpander.resolve(hop1))
    }

    @Test
    fun resolveStopsAfterFiveHopsOnRedirectLoop() = runTest {
        val hits = AtomicInteger(0)
        lateinit var selfUrl: String
        selfUrl = startServer { exchange ->
            hits.incrementAndGet()
            exchange.responseHeaders.add("Location", "${selfUrl}?n=${hits.get()}")
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }

        UrlExpander.resolve(selfUrl)

        assertEquals(5, hits.get())
    }

    @Test
    fun resolveAddsHttpsSchemeWhenMissing() = runTest {
        // No server is listening, so the https attempt fails fast and resolve()
        // falls back to returning the already-prefixed URL instead of throwing.
        val result = UrlExpander.resolve("127.0.0.1:1")

        assertEquals("https://127.0.0.1:1", result)
    }

    @Test
    fun resolveCachesResultForRepeatedCalls() = runTest {
        val hits = AtomicInteger(0)
        val url = startServer { exchange ->
            hits.incrementAndGet()
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }

        UrlExpander.resolve(url)
        UrlExpander.resolve(url)

        assertEquals(1, hits.get())
    }

    @Test
    fun resolveReturnsInputUnchangedOnConnectionFailure() = runTest {
        // Nothing is listening on this port.
        val unreachable = "http://127.0.0.1:1/"

        assertEquals(unreachable, UrlExpander.resolve(unreachable))
    }
}
