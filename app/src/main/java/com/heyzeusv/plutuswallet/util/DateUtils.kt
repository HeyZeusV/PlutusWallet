package com.heyzeusv.plutuswallet.util

import android.app.DatePickerDialog
import android.view.View
import java.time.DayOfWeek
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 *  Functions/Classes used in multiple classes focused on Dates.
 */
/**
 *  Class used to ensure that we always get a [start] and [end] [ZonedDateTime] to pass to the
 *  filter when the Date filter IS NOT applied.
 */
data class ViewDates(
    val start: ZonedDateTime,
    val end: ZonedDateTime
)

/**
 *   Returns FutureDate set at the beginning of the day by calculating
 *   ([frequency] * [period]) + [date].
 */
fun createFutureDate(date: ZonedDateTime, period: Int, frequency: Int): ZonedDateTime {
    return when (period) {
        0 -> date.plusDays(frequency.toLong())
        1 -> date.plusWeeks(frequency.toLong())
        2 -> date.plusMonths(frequency.toLong())
        else -> date.plusYears(frequency.toLong())
    }
}

/**
 *  Returns formatted [date] String using [format] to determine version.
 */
fun formatDate(date: ZonedDateTime, format: FormatStyle): String {
    return date.format(DateTimeFormatter.ofLocalizedDate(format))
}

/**
 *  Returns [FormatStyle] used by ZonedDateTime by converting passed [intFormat]
 */
fun retrieveDateFormat(intFormat: Int): FormatStyle {
    return when (intFormat) {
        0 -> FormatStyle.SHORT
        1 -> FormatStyle.MEDIUM
        2 -> FormatStyle.LONG
        else -> FormatStyle.FULL
    }
}

/**
 *  Returns ZonedDateTime object starting at the beginning of the day using [date].
 */
fun startOfDay(date: ZonedDateTime): ZonedDateTime {
    return date.minusHours(date.hour.toLong())
        .minusMinutes(date.minute.toLong())
        .minusSeconds(date.second.toLong())
        .minusNanos(date.nano.toLong())
}

/**
 *  Return ZonedDateTime object at 1 second before midnight using [date].
 */
fun endOfDay(date: ZonedDateTime): ZonedDateTime {
    return startOfDay(date).plusDays(1L).minusSeconds(1L)
}

/**
 *  Based on [view], calculates the start and end dates using [date], and returns them in a
 *  [ViewDates] data class.
 */
fun calculateViewDates(date: ZonedDateTime, view: Views): ViewDates {
    val year = date.year
    val month = date.monthValue

    val start: ZonedDateTime
    val end: ZonedDateTime
    when (view) {
        Views.YEARLY -> {
            start = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, systemDefault())
            end = ZonedDateTime.of(year + 1, 1, 1, 0, 0, 0, 0, systemDefault()).minusSeconds(1L)
        }
        Views.MONTHLY -> {
            start = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, systemDefault())
            end = ZonedDateTime.of(year, month + 1, 1, 0, 0, 0, 0, systemDefault()).minusSeconds(1L)
        }
        Views.WEEKLY -> {
            val day = date.dayOfWeek
            if (day == DayOfWeek.SUNDAY) {
                start = startOfDay(date)
                end = endOfDay(start).plusDays(6L)
            } else {
                val daysToSaturday = 6 - day.value
                start = startOfDay(date).minusDays(day.value.toLong())
                end =  endOfDay(date).plusDays(daysToSaturday.toLong())
            }
        }
        Views.DAILY -> {
            start = startOfDay(date)
            end = endOfDay(date)
        }
    }

    return ViewDates(start, end)
}

fun datePickerDialog(
    view: View,
    initDate: ZonedDateTime,
    onDateSelected: (ZonedDateTime) -> Unit
): DatePickerDialog {

    // variables used to initialize DateDialog
    val initYear: Int = initDate.year
    val initMonth: Int = initDate.monthValue - 1
    val initDay: Int = initDate.dayOfMonth
    // retrieves date selected in DateDialog and passes to function from ViewModel
    val dateListener =
        DatePickerDialog.OnDateSetListener { _, year: Int, month: Int, day: Int ->
            val date: ZonedDateTime = ZonedDateTime.of(
                year, month + 1, day, 0, 0, 0, 0, systemDefault()
            )
            onDateSelected(date)
        }

    return DatePickerDialog(view.context, dateListener, initYear, initMonth, initDay)
}