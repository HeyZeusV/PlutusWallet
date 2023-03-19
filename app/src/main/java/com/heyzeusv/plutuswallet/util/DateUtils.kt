package com.heyzeusv.plutuswallet.util

import android.app.DatePickerDialog
import android.view.View
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 *  Functions used in multiple classes focused on Dates.
 */

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
                year, month + 1, day, 0, 0, 0, 0, ZoneId.systemDefault()
            )
            onDateSelected(date)
        }

    return DatePickerDialog(view.context, dateListener, initYear, initMonth, initDay)
}