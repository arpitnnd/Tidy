package com.tidy.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorTextTest {

    @Test
    fun returnsShortMessageUnchanged() {
        assertEquals("Permission denied", errorDetail("Permission denied"))
    }

    @Test
    fun truncatesLongMessageWithEllipsis() {
        val long = "x".repeat(200)
        val result = errorDetail(long, maxLength = 80)
        assertEquals(81, result.length)
        assertEquals("x".repeat(80) + "…", result)
    }

    @Test
    fun returnsEmptyStringForNullMessage() {
        assertEquals("", errorDetail(null))
    }
}
