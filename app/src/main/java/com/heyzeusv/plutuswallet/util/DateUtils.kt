package com.heyzeusv.plutuswallet.util

import android.app.DatePickerDialog
import android.view.View
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 *  Function used in multiple classes focused on Dates.
 */
object DateUtils {

    /**
     *  Returns FutureDate set at the beginning of the day by calculating
     *  ([frequency] * [period]) + [date].
     */
    fun createFutureDate(date: ZonedDateTime, period: Int, frequency: Int): ZonedDateTime {

        return date.apply {
            // 0 = Day, 1 = Week, 2 = Month, 3 = Year
            when (period) {
                0 -> plusDays(frequency.toLong())
                1 -> plusWeeks(frequency.toLong())
                2 -> plusMonths(frequency.toLong())
                else -> plusMonths(frequency.toLong())
            }
        }
    }

    /**
     *  Returns formatted [date] string using [format] to determine version.
     */
    fun formatString(date: ZonedDateTime, format: Int): String {

        return date.format(DateTimeFormatter.ofLocalizedDate(when(format) {
            0 -> FormatStyle.SHORT
            1 -> FormatStyle.MEDIUM
            2 -> FormatStyle.LONG
            else -> FormatStyle.FULL
        }))
    }

    /**
     *  Returns ZonedDateTime object starting at the beginning of the day using [date].
     */
    fun startOfDay(date: ZonedDateTime): ZonedDateTime {

        date.minusNanos(date.nano.toLong())
        date.minusSeconds(date.second.toLong())
        date.minusMinutes(date.minute.toLong())
        date.minusHours(date.hour.toLong())

        return date
    }

    /**
     *  Return ZonedDateTime object at 1 second before midnight using [date].
     */
    fun endOfDay(date: ZonedDateTime): ZonedDateTime {

        date.plusDays(1L)

        date.minusNanos(date.nano.toLong())
        date.minusSeconds(date.second.toLong() + 1L)
        date.minusMinutes(date.minute.toLong())
        date.minusHours(date.hour.toLong())

        return date
    }

    /**
     *  Creates DatePickerDialog using [view]'s Context with [initDate] selected.
     *  [onDateSelected] is a function passed from ViewModel which determines what
     *  to do with the Date user selects.
     */
    fun datePickerDialog(
        view: View,
        initDate: ZonedDateTime,
        onDateSelected: (ZonedDateTime) -> Unit
    ): DatePickerDialog {

        // variables used to initialize DateDialog
        val initYear: Int = initDate.year
        val initMonth: Int = initDate.monthValue
        val initDay: Int = initDate.dayOfMonth

        // retrieves date selected in DateDialog and passes to function from ViewModel
        val dateListener =
            DatePickerDialog.OnDateSetListener { _, year: Int, month: Int, day: Int ->

                val date: ZonedDateTime = ZonedDateTime.of(
                    year, month, day, 0, 0, 0, 0, ZoneId.systemDefault()
                )
                onDateSelected(date)
            }

        return DatePickerDialog(view.context, dateListener, initYear, initMonth, initDay)
    }
}