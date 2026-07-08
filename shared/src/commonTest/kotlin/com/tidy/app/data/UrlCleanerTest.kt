package com.tidy.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlCleanerTest {

    private val cleaner = UrlCleaner()

    @Test
    fun testCleanDefaultParams() {
        val original =
            "https://example.com/page?utm_source=newsletter&utm_medium=email&fbclid=12345&si=abc"
        val result = cleaner.clean(original)

        assertEquals("https://example.com/page", result.cleanedUrl)
        assertEquals(4, result.removedParams.size)
        assertTrue(result.removedParams.contains("utm_source"))
        assertTrue(result.removedParams.contains("utm_medium"))
        assertTrue(result.removedParams.contains("fbclid"))
        assertTrue(result.removedParams.contains("si"))
    }

    @Test
    fun testPreserveOtherParams() {
        val original = "https://example.com/search?q=kmp&utm_source=google&page=2"
        val result = cleaner.clean(original)

        assertEquals("https://example.com/search?q=kmp&page=2", result.cleanedUrl)
        assertEquals(1, result.removedParams.size)
        assertEquals("utm_source", result.removedParams[0])
    }

    @Test
    fun testWhitelistedDomain() {
        val original = "https://safe.google.com/search?q=android&utm_source=chrome"
        val result = cleaner.clean(
            urlStr = original,
            whitelistedDomains = setOf("google.com")
        )

        assertEquals(original, result.cleanedUrl)
        assertEquals(0, result.removedParams.size)
    }

    @Test
    fun testCustomBlacklistParams() {
        val original = "https://example.com/product?id=99&custom_tracker=xyz&utm_source=facebook"
        val result = cleaner.clean(
            urlStr = original,
            customBlacklistParams = setOf("custom_tracker")
        )

        assertEquals("https://example.com/product?id=99", result.cleanedUrl)
        assertEquals(2, result.removedParams.size)
        assertTrue(result.removedParams.contains("custom_tracker"))
        assertTrue(result.removedParams.contains("utm_source"))
    }

    @Test
    fun testRemoveMobileSubdomains() {
        val urlM = "https://m.wikipedia.org/wiki/Main_Page?utm_source=twitter"
        val resultM = cleaner.clean(urlStr = urlM, removeMobileSubdomains = true)
        assertEquals("https://wikipedia.org/wiki/Main_Page", resultM.cleanedUrl)

        val urlMobile = "https://mobile.twitter.com/profile?utm_medium=feed"
        val resultMobile = cleaner.clean(urlStr = urlMobile, removeMobileSubdomains = true)
        assertEquals("https://twitter.com/profile", resultMobile.cleanedUrl)

        // Verify m.me is preserved since "me" doesn't have a dot
        val urlMe = "https://m.me/username?utm_source=share"
        val resultMe = cleaner.clean(urlStr = urlMe, removeMobileSubdomains = true)
        assertEquals("https://m.me/username", resultMe.cleanedUrl)
    }
}
