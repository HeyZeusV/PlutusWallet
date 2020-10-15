package com.heyzeusv.plutuswallet.utilities

import android.content.SharedPreferences
import com.heyzeusv.plutuswallet.database.entities.SettingsValues
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 *  Functions used in multiple classes
 */
class SettingsUtils {

    companion object {

        /**
         *  Returns currency symbol by using [symbolKey] from settings.
         */
        private fun getCurrencySymbol(symbolKey: String): String {

            return when (symbolKey) {
                "dollar" -> "$"
                "euro" -> "€"
                "pound" -> "£"
                "yen" -> "¥"
                "rupee" -> "₹"
                "won" -> "₩"
                else -> "฿"
            }
        }

        /**
         *  Returns DateFormat: 0 = FULL, 1 = LONG, 2 = MEDIUM, 3 = SHORT using [dateFormat] key.
         */
        private fun getDateFormat(dateFormat: String): Int {

            return when (dateFormat) {
                "0" -> 0
                "1" -> 1
                "2" -> 2
                else -> 3
            }
        }

        /**
         *  Returns separator symbol by using [symbolKey] from settings.
         */
        fun getSeparatorSymbol(symbolKey: String): Char {

            return when (symbolKey) {
                "comma" -> ','
                "period" -> '.'
                "hyphen" -> '-'
                else -> ' '
            }
        }

        /**
         *  Returns SettingsValues containing settings/formatters using [sharedPref].
         */
        fun prepareSettingValues(sharedPref: SharedPreferences): SettingsValues {

            // retrieving SharedPreferences values
            val currencySymbolKey: String = sharedPref[Constants.KEY_CURRENCY_SYMBOL, "dollar"]
            val dateFormatKey: String = sharedPref[Constants.KEY_DATE_FORMAT, "0"]
            val decimalSymbolKey: String = sharedPref[Constants.KEY_DECIMAL_SYMBOL, "period"]
            val thousandsSymbolKey: String = sharedPref[Constants.KEY_THOUSANDS_SYMBOL, "comma"]
            val decimalPlaces: Boolean = sharedPref[Constants.KEY_DECIMAL_PLACES, true]
            val symbolSide: Boolean = sharedPref[Constants.KEY_SYMBOL_SIDE, true]

            // converting keys to values
            val currencySymbol: String = getCurrencySymbol(currencySymbolKey)
            val dateFormat: Int = getDateFormat(dateFormatKey)
            val decimalSymbol: Char = getSeparatorSymbol(decimalSymbolKey)
            val thousandsSymbol: Char = getSeparatorSymbol(thousandsSymbolKey)

            // set up decimal/thousands symbol
            val customSymbols = DecimalFormatSymbols(Locale.US)
            customSymbols.decimalSeparator = decimalSymbol
            customSymbols.groupingSeparator = thousandsSymbol

            // remaking formatters with new symbols
            val decimalFormatter = DecimalFormat("#,##0.00", customSymbols)
            val integerFormatter = DecimalFormat("#,###", customSymbols)

            return SettingsValues(
                currencySymbol, symbolSide, thousandsSymbol, decimalPlaces,
                decimalSymbol, dateFormat, decimalFormatter, integerFormatter
            )
        }
    }
}
