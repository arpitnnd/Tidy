package com.tidy.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlCleanerTest {

    private val cleaner = UrlCleaner()

    @Test
    fun testCleanDefaultParams() {
        // "si" is deliberately not used here -- it's domain-scoped to Spotify/YouTube (see
        // testSiParameterScopedToSpotifyAndYouTube), so it wouldn't strip on example.com.
        val original =
            "https://example.com/page?utm_source=newsletter&utm_medium=email&fbclid=12345&igsh=abc"
        val result = cleaner.clean(original)

        assertEquals("https://example.com/page", result.cleanedUrl)
        assertEquals(4, result.removedParams.size)
        assertTrue(result.removedParams.contains("utm_source"))
        assertTrue(result.removedParams.contains("utm_medium"))
        assertTrue(result.removedParams.contains("fbclid"))
        assertTrue(result.removedParams.contains("igsh"))
    }

    @Test
    fun testCleanNewlyAddedTrackingParams() {
        val original =
            "https://example.com/page?gbraid=1&wbraid=2&twclid=3&ttclid=4&srsltid=5&li_fat_id=6&sc_cid=7&_hsenc=8&_hsmi=9"
        val result = cleaner.clean(original)

        assertEquals("https://example.com/page", result.cleanedUrl)
        assertEquals(9, result.removedParams.size)
        assertTrue(result.removedParams.contains("gbraid"))
        assertTrue(result.removedParams.contains("wbraid"))
        assertTrue(result.removedParams.contains("twclid"))
        assertTrue(result.removedParams.contains("ttclid"))
        assertTrue(result.removedParams.contains("srsltid"))
        assertTrue(result.removedParams.contains("li_fat_id"))
        assertTrue(result.removedParams.contains("sc_cid"))
        assertTrue(result.removedParams.contains("_hsenc"))
        assertTrue(result.removedParams.contains("_hsmi"))
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

    @Test
    fun testLinkedInRcmParameter() {
        val original = "https://www.linkedin.com/posts/warikoo_post-12345/?rcm=ACoAABqI9AYBS6KUpW_MZCyFkMyR_SvzhPYHZiY"
        val result = cleaner.clean(original)

        assertEquals("https://www.linkedin.com/posts/warikoo_post-12345/", result.cleanedUrl)
        assertEquals(1, result.removedParams.size)
        assertEquals("rcm", result.removedParams[0])
    }

    @Test
    fun testAmazonTrackingParametersPreservingProductVariant() {
        // "ref"/"ref_"/"social_share" are domain-scoped to Amazon's own domains (see
        // trackers.json) since they're too generic a set of query keys to strip safely
        // everywhere. "psc" (product variant) is preserved -- it's a legitimate
        // product-selection parameter, not a tracker.
        val original = "https://www.amazon.in/dp/1638778868?psc=1&ref=cm_sw_r_cso_cp_apan_ct_39JZ4QKXDZ6528XFDKDT&ref_=cm_sw_r_cso_cp_apan_ct_39JZ4QKXDZ6528XFDKDT&social_share=cm_sw_r_cso_cp_apan_ct_39JZ4QKXDZ6528XFDKDT"
        val result = cleaner.clean(original)

        assertEquals("https://www.amazon.in/dp/1638778868?psc=1", result.cleanedUrl)
        assertEquals(3, result.removedParams.size)
        assertTrue(result.removedParams.contains("ref"))
        assertTrue(result.removedParams.contains("ref_"))
        assertTrue(result.removedParams.contains("social_share"))
    }

    @Test
    fun testRefParameterIsNotStrippedOffAmazon() {
        // "ref" is a common, generic query key on many non-Amazon sites for legitimate,
        // non-tracking purposes, so it must not be stripped outside Amazon's own domains.
        val original = "https://example.com/page?ref=some-app-state&utm_source=newsletter"
        val result = cleaner.clean(original)

        assertEquals("https://example.com/page?ref=some-app-state", result.cleanedUrl)
        assertEquals(1, result.removedParams.size)
        assertEquals("utm_source", result.removedParams[0])
    }

    @Test
    fun testFeatureParameterScopedToYouTube() {
        // "feature" is domain-scoped to YouTube -- it's a plain English word used as a
        // functional query key elsewhere (feature flags, deep links), so it must not be
        // stripped off other sites.
        val onYouTube = cleaner.clean("https://youtu.be/dQw4w9WgXcQ?feature=share")
        assertEquals("https://youtu.be/dQw4w9WgXcQ", onYouTube.cleanedUrl)
        assertEquals(listOf("feature"), onYouTube.removedParams)

        val elsewhere = cleaner.clean("https://app.example.com/dashboard?feature=beta-editor")
        assertEquals("https://app.example.com/dashboard?feature=beta-editor", elsewhere.cleanedUrl)
        assertTrue(elsewhere.removedParams.isEmpty())
    }

    @Test
    fun testSiParameterScopedToSpotifyAndYouTube() {
        val onSpotify = cleaner.clean("https://open.spotify.com/track/123?si=abcd")
        assertEquals("https://open.spotify.com/track/123", onSpotify.cleanedUrl)
        assertEquals(listOf("si"), onSpotify.removedParams)

        val elsewhere = cleaner.clean("https://example.com/page?si=1")
        assertEquals("https://example.com/page?si=1", elsewhere.cleanedUrl)
        assertTrue(elsewhere.removedParams.isEmpty())
    }

    @Test
    fun testCampidParameterScopedToEbay() {
        val onEbay = cleaner.clean("https://www.ebay.com/itm/123?campid=5338722076")
        assertEquals("https://www.ebay.com/itm/123", onEbay.cleanedUrl)
        assertEquals(listOf("campid"), onEbay.removedParams)

        val elsewhere = cleaner.clean("https://example.com/campaign?campid=42")
        assertEquals("https://example.com/campaign?campid=42", elsewhere.cleanedUrl)
        assertTrue(elsewhere.removedParams.isEmpty())
    }

    @Test
    fun testDomainScopedTrackerEntry() {
        val scopedTrackers = listOf(
            TrackerEntry(name = "ref", description = "test", domains = listOf("amazon.com"))
        )

        val onDomain = cleaner.clean(
            urlStr = "https://www.amazon.com/dp/1?ref=abc",
            trackers = scopedTrackers
        )
        assertEquals("https://www.amazon.com/dp/1", onDomain.cleanedUrl)
        assertEquals(listOf("ref"), onDomain.removedParams)

        val offDomain = cleaner.clean(
            urlStr = "https://example.com/page?ref=abc",
            trackers = scopedTrackers
        )
        assertEquals("https://example.com/page?ref=abc", offDomain.cleanedUrl)
        assertEquals(emptyList<String>(), offDomain.removedParams)

        // A subdomain of the scoped domain is also covered, same as whitelistedDomains matching.
        val subdomain = cleaner.clean(
            urlStr = "https://smile.amazon.com/dp/1?ref=abc",
            trackers = scopedTrackers
        )
        assertEquals("https://smile.amazon.com/dp/1", subdomain.cleanedUrl)
        assertEquals(listOf("ref"), subdomain.removedParams)
    }

    @Test
    fun testParamPatternWildcardsAndDomainWildcards() {
        val trackers = listOf(
            TrackerEntry(name = "utm_*", description = "test"),
            TrackerEntry(name = "ref", description = "test", domains = listOf("amzn.*"))
        )

        // Wildcard parameter name utm_* matching utm_custom
        val utmCustom = cleaner.clean(
            urlStr = "https://example.com/page?utm_custom_id=99",
            trackers = trackers
        )
        assertEquals("https://example.com/page", utmCustom.cleanedUrl)
        assertEquals(listOf("utm_custom_id"), utmCustom.removedParams)

        // Wildcard domain amzn.* matching amzn.in
        val amznIn = cleaner.clean(
            urlStr = "https://amzn.in/d/123?ref=share",
            trackers = trackers
        )
        assertEquals("https://amzn.in/d/123", amznIn.cleanedUrl)
        assertEquals(listOf("ref"), amznIn.removedParams)
    }
}
