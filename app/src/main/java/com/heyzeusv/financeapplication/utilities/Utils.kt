package com.heyzeusv.financeapplication.utilities

import java.util.*

/**
 *  Functions used in multiple classes
 */
class Utils {

    companion object {

        /**
         *  Used to get Currency symbol to be displayed depending on users selection.
         *
         *  @param  symbolKey taken from SettingsFragment.
         *  @return the symbol to be used.
         */
        fun getCurrencySymbol(symbolKey : String) : String {

            return when (symbolKey) {

                "dollar" -> "$"
                "euro"   -> "€"
                "pound"  -> "£"
                "yen"    -> "¥"
                "rupee"  -> "₹"
                "won"    -> "₩"
                "baht"   -> "฿"
                else     -> "$"
            }
        }

        /**
         *  Used to get Separator symbol to be displayed depending on users selection.
         *
         *  @param  symbolKey taken from SettingsFragment.
         *  @return the symbol to be used.
         */
        fun getSeparatorSymbol(symbolKey : String) : Char {

            return when (symbolKey) {

                "comma"  -> ','
                "period" -> '.'
                else     -> ' '
            }
        }

        /**
         *  Sets time right that the beginning at the day.
         *
         *  @param  date the date object to be changed.
         *  @return Date object with time at start of day.
         */
        fun startOfDay(date : Date) : Date {

            val calendar = GregorianCalendar()
            calendar.timeInMillis = date.time
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE,      0)
            calendar.set(Calendar.SECOND,      0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar.time
        }
    }
}
