package com.tidy.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlReplacementTest {
    @Test
    fun replacementsUseOriginalPositionsAndDoNotReprocessOutput() {
        val seen = mutableListOf<String>()
        val output = UrlDetection.replaceUrls("a@example.com (example.com). example.com") {
            seen.add(it)
            "https://new.example/longer"
        }
        assertEquals(listOf("example.com", "example.com"), seen)
        assertEquals(
            "a@example.com (https://new.example/longer). https://new.example/longer",
            output
        )
    }

    @Test
    fun emptyAndNonLinkTextRemainUnchanged() {
        assertEquals("", UrlDetection.replaceUrls("") { error("No link expected") })
        assertEquals(
            "No links here",
            UrlDetection.replaceUrls("No links here") { error("No link expected") })
    }
}
