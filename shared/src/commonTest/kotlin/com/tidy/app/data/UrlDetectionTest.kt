package com.tidy.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlDetectionTest {

    @Test
    fun acceptsSchemeUrls() {
        assertTrue(UrlDetection.looksLikeUrl("https://example.com/page?utm_source=x"))
        assertTrue(UrlDetection.looksLikeUrl("http://example.com"))
        assertTrue(UrlDetection.looksLikeUrl("  https://example.com  "))
    }

    @Test
    fun acceptsBareDomainsWithoutScheme() {
        assertTrue(UrlDetection.looksLikeUrl("example.com"))
        assertTrue(UrlDetection.looksLikeUrl("example.com/path?x=1"))
        assertTrue(UrlDetection.looksLikeUrl("sub.example.co.uk/page"))
        assertTrue(UrlDetection.looksLikeUrl("bit.ly/abc"))
    }

    @Test
    fun rejectsPlainText() {
        assertFalse(UrlDetection.looksLikeUrl(""))
        assertFalse(UrlDetection.looksLikeUrl("hello world"))
        assertFalse(UrlDetection.looksLikeUrl("no dots here"))
        assertFalse(UrlDetection.looksLikeUrl("version 1.2"))
        assertFalse(UrlDetection.looksLikeUrl("1.5"))
        assertFalse(UrlDetection.looksLikeUrl("file.txt."))
    }

    @Test
    fun findsUrlsInsideFreeFormText() {
        assertEquals(
            "https://example.com/a?fbclid=1",
            UrlDetection.findFirstUrl("Check this out: https://example.com/a?fbclid=1 so cool")
        )
        assertEquals("example.com/a", UrlDetection.findFirstUrl("visit example.com/a, thanks"))
        assertNull(UrlDetection.findFirstUrl("nothing to see here"))
        assertNull(UrlDetection.findFirstUrl(null))
    }

    @Test
    fun findsAllUrls() {
        val urls = UrlDetection.findAllUrls("https://a.com/1\nhttps://b.com/2 and c.org")
        assertEquals(listOf("https://a.com/1", "https://b.com/2", "c.org"), urls)
    }

    @Test
    fun normalizeAddsSchemeOnlyWhenMissing() {
        assertEquals("https://example.com", UrlDetection.normalize("example.com"))
        assertEquals("https://example.com", UrlDetection.normalize("  example.com "))
        assertEquals("http://example.com", UrlDetection.normalize("http://example.com"))
        assertEquals("https://example.com", UrlDetection.normalize("https://example.com"))
    }

    @Test
    fun spliceUrlReplacesOnlyTheUrlKeepingSurroundingText() {
        val text = "here's the file https://wetransfer.com/x?utm_source=y enjoy"
        val result = UrlDetection.spliceUrl(
            text,
            "https://wetransfer.com/x?utm_source=y",
            "https://wetransfer.com/x"
        )
        assertEquals("here's the file https://wetransfer.com/x enjoy", result)
    }

    @Test
    fun spliceUrlFallsBackToJustTheNewUrlWhenOldUrlNotFound() {
        val result = UrlDetection.spliceUrl("no url here", "https://example.com", "https://example.com/clean")
        assertEquals("https://example.com/clean", result)
    }
}
