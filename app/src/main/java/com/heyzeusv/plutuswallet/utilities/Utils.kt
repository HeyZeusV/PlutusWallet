package com.heyzeusv.plutuswallet.utilities

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

/**
 *  Functions used in multiple classes
 */
class Utils {

    companion object {

        /**
         *  Changes locale depending on users selection.
         *
         *  @param  context      used to change configuration.
         *  @param  languageCode string containing language code to be used. eg "en", "es".
         *  @return holds context that contains updated locale.
         */
        fun changeLanguage(context : Context, languageCode : String) : ContextWrapper {

            val config     : Configuration = context.resources.configuration
            val newContext : Context
            val newLocale                  = Locale(languageCode)

            // sets locale for JVM
            Locale.setDefault(newLocale)
            // sets locale for context
            config.setLocale(newLocale)
            // new context needed since parameters are final
            newContext = context.createConfigurationContext(config)

            return ContextWrapper(newContext)
        }

        /**
         *  Will format string with thousands separators.
         *
         *  @param  string    string to be formatted.
         *  @param  thousands thousands symbol
         *  @return the formatted string.
         */
        fun formatInteger(string : String, thousands : Char) : String {

            val customSymbols = DecimalFormatSymbols(Locale.US)
            customSymbols.groupingSeparator = thousands
            val parsed = BigDecimal(string)
            // every three numbers, a thousands symbol will be added
            val formatter = DecimalFormat("#,###", customSymbols)
            return formatter.format(parsed)
        }

        /**
         *  Will format string with thousands and decimal separators.
         *
         *  @param  string    the string to be formatted.
         *  @param  thousands thousands symbol
         *  @param  decimal   decimal symbol
         *  @return the formatted string.
         */
        fun formatDecimal(string : String, thousands : Char, decimal : Char) : String {

            val customSymbols = DecimalFormatSymbols(Locale.US)
            customSymbols.groupingSeparator = thousands
            customSymbols.decimalSeparator = decimal
            // adds leading zero if user only inputs decimal
            if (string == customSymbols.decimalSeparator.toString()) {

                return "0" + customSymbols.decimalSeparator.toString()
            }
            val parsed = BigDecimal(string.replace(("[" + customSymbols.decimalSeparator +"]").toRegex(), "."))
            // every three numbers, a thousands symbol will be added
            val formatter = DecimalFormat("#,##0." + getDecimalPattern(string, customSymbols), customSymbols)
            formatter.roundingMode = RoundingMode.DOWN
            return formatter.format(parsed)
        }

        /**
         *  It will return suitable pattern for format decimal.
         *  For example: 10.2 -> return 0 | 10.23 -> return 00, | 10.235 -> return 000
         *
         *  @param  string        used for formatter
         *  @param  customSymbols contains decimal symbol
         *  @return returns pattern to be used after decimal symbol
         */
        private fun getDecimalPattern(string : String, customSymbols : DecimalFormatSymbols) : String {

            // returns number of characters after decimal point
            val decimalCount : Int = string.length - string.indexOf(customSymbols.decimalSeparator) - 1
            val decimalPattern     = StringBuilder()
            var i = 0
            while (i < decimalCount && i < 2) {

                decimalPattern.append("0")
                i++
            }
            return decimalPattern.toString()
        }

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
                else     -> "฿"
            }
        }

        /**
         *  Used to get which format to display the Date in
         *
         *  @param  dateFormat taken from Settings Fragment.
         *  @return int used DateFormat: 0 = FULL, 1 = LONG, 2 = MEDIUM, 3 = SHORT
         */
        fun getDateFormat(dateFormat : String) : Int {

            return when (dateFormat) {

                "0"  -> 0
                "1"  -> 1
                "2"  -> 2
                else -> 3
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
                "hyphen" -> '-'
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
            calendar.set(Calendar.MINUTE     , 0)
            calendar.set(Calendar.SECOND     , 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar.time
        }
    }
}
