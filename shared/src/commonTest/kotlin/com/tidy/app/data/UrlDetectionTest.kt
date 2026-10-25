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
    fun rejectsEmailAddresses() {
        assertFalse(UrlDetection.looksLikeUrl("user@example.com"))
        assertFalse(UrlDetection.looksLikeUrl("first.last@company.co.uk"))
        assertFalse(UrlDetection.looksLikeUrl("support@service.io"))
        assertFalse(UrlDetection.looksLikeUrl("mailto:user@example.com"))
    }

    @Test
    fun acceptsBareDomainsWithAtInThePath() {
        assertTrue(UrlDetection.looksLikeUrl("medium.com/@someuser"))
        assertTrue(UrlDetection.looksLikeUrl("npmjs.com/package/@babel/core"))
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
    fun findsWrappedUrlsWithoutDroppingBalancedPathBrackets() {
        assertEquals(
            listOf("https://example.com/a_(b)?utm_source=x", "example.org/page"),
            UrlDetection.findAllUrls("See (https://example.com/a_(b)?utm_source=x), then <example.org/page>.")
        )
        assertEquals(
            "https://example.com/a_(b)",
            UrlDetection.findFirstUrl("https://example.com/a_(b)")
        )
    }

    @Test
    fun findsNestedWrappersAndPreservesBalancedPathBrackets() {
        for ((text, expected) in listOf(
            "See [(https://example.com/page)]" to "https://example.com/page",
            "See ([https://example.com/page])" to "https://example.com/page",
            "See (<https://example.com/page>)" to "https://example.com/page",
            "See [(https://example.com/a_(b)[c])], please." to "https://example.com/a_(b)[c]"
        )) {
            assertEquals(expected, UrlDetection.findFirstUrl(text))
            assertEquals(
                text.replace(expected, "https://clean.example/"),
                UrlDetection.spliceUrl(
                    text,
                    UrlDetection.findFirstUrl(text)!!,
                    "https://clean.example/"
                )
            )
        }
    }

    @Test(timeout = 2_000)
    fun longUnmatchedBracketSuffixDoesNotBlockDetection() {
        val url = "https://example.com/page"
        assertEquals(url, UrlDetection.findFirstUrl(url + ")".repeat(100_000)))
        assertEquals(url, UrlDetection.findFirstUrl(url + ")]".repeat(50_000)))
    }

    @Test
    fun splicingPreservesWhitespacePunctuationAndLaterUrls() {
        val source = "  See (example.com/p?utm_source=x),\nthen https://other.org/?fbclid=y!  "
        assertEquals(
            "  See (https://example.com/p),\nthen https://other.org/?fbclid=y!  ",
            UrlDetection.spliceUrl(
                source,
                UrlDetection.findFirstUrl(source)!!,
                "https://example.com/p"
            )
        )
    }

    @Test
    fun splicingUsesTheDetectedPositionAfterAnEmail() {
        val source = "Email user@example.com then visit [(example.com)], then example.com"
        assertEquals(
            "Email user@example.com then visit [(https://example.com)], then example.com",
            UrlDetection.spliceUrl(
                source,
                UrlDetection.findFirstUrl(source)!!,
                "https://example.com"
            )
        )
    }

    @Test
    fun splicingDoesNotReplacePartOfAnotherUrl() {
        val source = "https://other.org/example.com then example.com"
        assertEquals(
            "https://other.org/example.com then https://example.com",
            UrlDetection.spliceUrl(source, "example.com", "https://example.com")
        )
    }

    @Test
    fun splicingFallsBackWhenOnlyAnEmailContainsTheUrl() {
        assertEquals(
            "https://example.com",
            UrlDetection.spliceUrl("Email user@example.com", "example.com", "https://example.com")
        )
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
        val result = UrlDetection.spliceUrl(
            "no url here",
            "https://example.com",
            "https://example.com/clean"
        )
        assertEquals("https://example.com/clean", result)
    }
}
