package com.heyzeusv.plutuswallet.util

import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.time.format.FormatStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class DateUtilsTest {

    val initialDate: ZonedDateTime = ZonedDateTime.of(1980, 1, 1, 0, 0, 0, 0, systemDefault())

    @Test
    @DisplayName("Create a future date from different combinations of period and frequency values.")
    fun createFutureDateTest() {
        val dayPeriod = createFutureDate(initialDate, 0, 25)
        val expectedDayPeriod = ZonedDateTime.of(1980, 1, 26, 0, 0, 0, 0, systemDefault())
        assertEquals(expectedDayPeriod, dayPeriod)

        val weekPeriod = createFutureDate(initialDate, 1, 5)
        val expectedWeekPeriod = ZonedDateTime.of(1980, 2, 5, 0, 0, 0, 0, systemDefault())
        assertEquals(expectedWeekPeriod, weekPeriod)

        val monthPeriod = createFutureDate(initialDate, 2, 6)
        val expectedMonthPeriod = ZonedDateTime.of(1980, 7, 1, 0, 0, 0, 0, systemDefault())
        assertEquals(expectedMonthPeriod, monthPeriod)

        val yearPeriod = createFutureDate(initialDate, 3, 2)
        val expectedYearPeriod = ZonedDateTime.of(1982, 1, 1, 0, 0, 0, 0, systemDefault())
        assertEquals(expectedYearPeriod, yearPeriod)
    }

    @Test
    @DisplayName("Format given ZonedDateTime using given FormatStyle.")
    fun formatDateTest() {
        val short = formatDate(initialDate, FormatStyle.SHORT)
        val expectedShort = "1/1/80"
        assertEquals(expectedShort, short)

        val medium = formatDate(initialDate, FormatStyle.MEDIUM)
        val expectedMedium = "Jan 1, 1980"
        assertEquals(expectedMedium, medium)

        val long = formatDate(initialDate, FormatStyle.LONG)
        val expectedLong = "January 1, 1980"
        assertEquals(expectedLong, long)

        val full = formatDate(initialDate, FormatStyle.FULL)
        val expectedFull = "Tuesday, January 1, 1980"
        assertEquals(expectedFull, full)
    }

    @Test
    @DisplayName("Given Int return correct FormatStyle")
    fun retrieveDateFormatTest() {
        val short = retrieveDateFormat(0)
        val expectedShort = FormatStyle.SHORT
        assertEquals(expectedShort, short)

        val medium = retrieveDateFormat(1)
        val expectedMedium = FormatStyle.MEDIUM
        assertEquals(expectedMedium, medium)

        val long = retrieveDateFormat(2)
        val expectedLong = FormatStyle.LONG
        assertEquals(expectedLong, long)

        val full = retrieveDateFormat(3)
        val expectedFull = FormatStyle.FULL
        assertEquals(expectedFull, full)
    }

    @Test
    @DisplayName("Given ZonedDateTime return ZonedDateTime that begins at very start of day.")
    fun startOfDayTest() {
        val date = ZonedDateTime.of(1980, 1, 1, 15, 25, 45, 2, systemDefault())
        val startOfDayDate = startOfDay(date)
        val expectedDate = ZonedDateTime.of(1980, 1, 1, 0, 0, 0, 0, systemDefault())
        assertEquals(expectedDate, startOfDayDate)
    }

    @Test
    @DisplayName("Given ZonedDateTime return ZonedDateTime at very end of the day.")
    fun endOfDayTest() {
        val date = ZonedDateTime.of(1980, 1, 1, 15, 25, 45, 2, systemDefault())
        val endOfDayDate = endOfDay(date)
        val expectedDate = ZonedDateTime.of(1980, 1, 1, 23, 59, 59, 0, systemDefault())
        assertEquals(expectedDate, endOfDayDate)
    }
}