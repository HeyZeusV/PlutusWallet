package com.heyzeusv.financeapplication.utilities

import java.util.*

class Utils {

    companion object {

        /**
         *  Used to get Currency symbol to be displayed depending on users selection.
         *
         *  @param  symbolKey taken from SettingsFragment.
         *  @return the symbol to be used.
         */
        fun getSymbol(symbolKey : String) : String {

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
         *  Sets time right that the beginning at the day.
         *
         *  @return Date object with time at start of day.
         */
        fun startOfDay() : Date {

            val date = GregorianCalendar()
            date.set(Calendar.HOUR_OF_DAY, 0)
            date.set(Calendar.MINUTE, 0)
            date.set(Calendar.SECOND, 0)
            date.set(Calendar.MILLISECOND, 0)

            return date.time
        }
    }
}
