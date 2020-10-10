package com.heyzeusv.plutuswallet.utilities

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class DateUtils {

    companion object {

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
    }
}