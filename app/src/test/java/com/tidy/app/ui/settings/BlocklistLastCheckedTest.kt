package com.tidy.app.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class BlocklistLastCheckedTest {

    @Test
    fun crossingMidnightCountsAsThePreviousDay() {
        val timeZone = TimeZone.getTimeZone("UTC")
        val checkedAt = timestamp(2026, Calendar.SEPTEMBER, 3, 23, 55, timeZone)
        val now = timestamp(2026, Calendar.SEPTEMBER, 4, 0, 5, timeZone)

        assertEquals(1, calendarDaysSince(checkedAt, now, timeZone))
    }

    @Test
    fun daylightSavingChangesDoNotChangeCalendarDayCount() {
        val timeZone = TimeZone.getTimeZone("America/New_York")
        val checkedAt = timestamp(2026, Calendar.MARCH, 8, 0, 30, timeZone)
        val now = timestamp(2026, Calendar.MARCH, 9, 0, 15, timeZone)

        assertEquals(1, calendarDaysSince(checkedAt, now, timeZone))
    }

    private fun timestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        timeZone: TimeZone
    ): Long = GregorianCalendar(timeZone).apply {
        clear()
        set(year, month, day, hour, minute)
    }.timeInMillis
}
