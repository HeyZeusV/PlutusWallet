package com.heyzeusv.plutuswallet.util

import android.app.DatePickerDialog
import android.view.View
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

/**
 *  Functions used in multiple classes focused on Dates.
 */

/**
 *   Returns FutureDate set at the beginning of the day by calculating
 *   ([frequency] * [period]) + [date].
 */
fun createFutureDate(date: ZonedDateTime, period: Int, frequency: Int): ZonedDateTime {
    return date.apply {
        // 0 = Day, 1 = Week, 2 = Month, 3 = Year
        when (period) {
            0 -> plusDays(frequency.toLong())
            1 -> plusWeeks(frequency.toLong())
            2 -> plusMonths(frequency.toLong())
            else -> plusYears(frequency.toLong())
        }
    }
}

/**
 *  Returns formatted [date] String using [format] to determine version.
 */
fun formatString(date: ZonedDateTime, format: Int): String {
    return date.format(
        DateTimeFormatter.ofLocalizedDate(
            when (format) {
                0 -> FormatStyle.SHORT
                1 -> FormatStyle.MEDIUM
                2 -> FormatStyle.LONG
                else -> FormatStyle.FULL
            }
        )
    )
}


/**
 *  Returns ZonedDateTime object starting at the beginning of the day using [date].
 */
fun startOfDay(date: ZonedDateTime): ZonedDateTime {
    val startDay: ZonedDateTime = date

    return startDay.apply {
        minusNanos(date.nano.toLong())
        minusSeconds(date.second.toLong())
        minusMinutes(date.minute.toLong())
        minusHours(date.hour.toLong())
    }
}

/**
 *  Return ZonedDateTime object at 1 second before midnight using [date].
 */
fun endOfDay(date: ZonedDateTime): ZonedDateTime {
    val endDay: ZonedDateTime = startOfDay(date)
    return endDay.apply {
        plusDays(1L)

        minusSeconds(date.second.toLong() + 1L)
    }
}


/**
 *  Returns Date object starting at the beginning of the day using [date].
 */
fun startOfDay(date: Date): Date {

    val calendar = GregorianCalendar()
    calendar.timeInMillis = date.time
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.time
}

/**
 *  Creates DatePickerDialog using [view]'s Context with [initDate] selected.
 *  [onDateSelected] is a function passed from ViewModel which determines what
 *  to do with the Date user selects.
 */
fun datePickerDialog(
    view: View,
    initDate: Date,
    onDateSelected: (Date) -> Unit
): DatePickerDialog {

    // set up Calendar with initial Date
    val calendar: Calendar = Calendar.getInstance()
    calendar.time = initDate
    // variables used to initialize DateDialog
    val initYear: Int = calendar.get(Calendar.YEAR)
    val initMonth: Int = calendar.get(Calendar.MONTH)
    val initDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
    // retrieves date selected in DateDialog and passes to function from ViewModel
    val dateListener =
        DatePickerDialog.OnDateSetListener { _, year: Int, month: Int, day: Int ->

            val date: Date = GregorianCalendar(year, month, day).time
            onDateSelected(date)
        }

    return DatePickerDialog(view.context, dateListener, initYear, initMonth, initDay)
}

fun datePickerDialog(
    view: View,
    initDate: ZonedDateTime,
    onDateSelected: (Date) -> Unit
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
            onDateSelected(Date())
        }

    return DatePickerDialog(view.context, dateListener, initYear, initMonth, initDay)
}