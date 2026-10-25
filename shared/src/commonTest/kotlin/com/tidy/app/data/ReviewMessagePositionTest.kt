package com.tidy.app.data

import org.junit.Test
import org.junit.Assert.assertEquals

class ReviewMessagePositionTest {
    @Test
    fun preservesAnEmailBeforeTheDetectedUrl() {
        val source = "Email user@example.com then visit example.com"
        val detected = UrlDetection.findFirstUrl(source)!!
        assertEquals("example.com", detected)
        assertEquals(
            "Email user@example.com then visit https://example.com",
            UrlDetection.spliceUrl(source, detected, UrlDetection.normalize(detected))
        )
    }
}
