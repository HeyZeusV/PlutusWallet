package com.heyzeusv.plutuswallet.utilities

import android.app.DatePickerDialog
import android.view.View
import com.heyzeusv.plutuswallet.database.entities.SettingsValues
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

object DateUtils {

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

}