package com.heyzeusv.plutuswallet.utilities

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Configuration
import com.heyzeusv.plutuswallet.database.entities.SettingsValues
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
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
         *  @param  num       BigDecimal to be formatted.
         *  @param  thousands thousands symbol
         *  @return the formatted string.
         */
        fun formatInteger(num : BigDecimal, thousands : Char) : String {

            val customSymbols = DecimalFormatSymbols(Locale.US)
            customSymbols.groupingSeparator = thousands
            // every three numbers, a thousands symbol will be added
            val formatter = DecimalFormat("#,###", customSymbols)
            formatter.roundingMode = RoundingMode.HALF_UP
            return formatter.format(num)
        }

        /**
         *  Will format string with thousands and decimal separators.
         *
         *  @param  num       BigDecimal to be formatted.
         *  @param  thousands thousands symbol
         *  @param  decimal   decimal symbol
         *  @return the formatted string.
         */
        fun formatDecimal(num : BigDecimal, thousands : Char, decimal : Char) : String {

            val customSymbols = DecimalFormatSymbols(Locale.US)
            customSymbols.groupingSeparator = thousands
            customSymbols.decimalSeparator = decimal
            // every three numbers, a thousands symbol will be added
            val formatter = DecimalFormat("#,##0.00", customSymbols)
            formatter.roundingMode = RoundingMode.HALF_UP
            return formatter.format(num)
        }

        /**
         *  Used to get Currency symbol to be displayed depending on users selection.
         *
         *  @param  symbolKey taken from SettingsFragment.
         *  @return the symbol to be used.
         */
        private fun getCurrencySymbol(symbolKey : String) : String {

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
        private fun getDateFormat(dateFormat : String) : Int {

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

        /**
         *  Retrieves settings values and either passes them or uses them prepare formatters.
         *
         *  @param  sp SharedPreferences used to retrieve values.
         *  @return data class that holds settings values/formatters.
         */
        fun prepareSettingValues(sp : SharedPreferences) : SettingsValues {

            // retrieving SharedPreferences values
            val currencySymbolKey  : String  = sp[Constants.KEY_CURRENCY_SYMBOL , "dollar"]!!
            val dateFormatKey      : String  = sp[Constants.KEY_DATE_FORMAT     , "0"     ]!!
            val decimalSymbolKey   : String  = sp[Constants.KEY_DECIMAL_SYMBOL  , "period"]!!
            val thousandsSymbolKey : String  = sp[Constants.KEY_THOUSANDS_SYMBOL, "comma" ]!!
            val decimalPlaces      : Boolean = sp[Constants.KEY_DECIMAL_PLACES  , true]!!
            val symbolSide         : Boolean = sp[Constants.KEY_SYMBOL_SIDE     , true]!!

            // converting keys to values
            val currencySymbol  : String = getCurrencySymbol (currencySymbolKey )
            val dateFormat      : Int    = getDateFormat     (dateFormatKey     )
            val decimalSymbol   : Char   = getSeparatorSymbol(decimalSymbolKey  )
            val thousandsSymbol : Char   = getSeparatorSymbol(thousandsSymbolKey)

            // set up decimal/thousands symbol
            val customSymbols = DecimalFormatSymbols(Locale.US)
            customSymbols.decimalSeparator  = decimalSymbol
            customSymbols.groupingSeparator = thousandsSymbol

            // remaking formatters with new symbols
            val decimalFormatter = DecimalFormat("#,##0.00", customSymbols)
            val integerFormatter = DecimalFormat("#,###"   , customSymbols)

            return SettingsValues(currencySymbol, symbolSide, thousandsSymbol, decimalPlaces,
                decimalSymbol, dateFormat, decimalFormatter, integerFormatter)
        }
    }
}
