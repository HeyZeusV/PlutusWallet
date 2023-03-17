package com.heyzeusv.plutuswallet.util

import android.content.SharedPreferences
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 *  Functions used in multiple classes focused on Settings.
 */

/**
 *  Returns currency symbol by using [symbolKey] from settings.
 */
fun getCurrencySymbol(symbolKey: String): String {
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
    val currencySymbolKey: String = sharedPref[Key.KEY_CURRENCY_SYMBOL, "dollar"]
    val dateFormatKey: String = sharedPref[Key.KEY_DATE_FORMAT, "0"]
    val decimalSymbolKey: String = sharedPref[Key.KEY_DECIMAL_SYMBOL, "period"]
    val thousandsSymbolKey: String = sharedPref[Key.KEY_THOUSANDS_SYMBOL, "comma"]
    val decimalNumber: String = sharedPref[Key.KEY_DECIMAL_NUMBER, "yes"]
    val currencySymbolSide: String = sharedPref[Key.KEY_CURRENCY_SYMBOL_SIDE, "left"]

    // converting keys to values
    val currencySymbol: String = getCurrencySymbol(currencySymbolKey)
    val dateFormatValue: Int = dateFormatKey.toInt()
    val decimalSymbol: Char = getSeparatorSymbol(decimalSymbolKey)
    val thousandsSymbol: Char = getSeparatorSymbol(thousandsSymbolKey)

    // set up decimal/thousands symbol
    val customSymbols = DecimalFormatSymbols(Locale.US)
    customSymbols.decimalSeparator = decimalSymbol
    customSymbols.groupingSeparator = thousandsSymbol

    // remaking formatters with new symbols
    val decimalFormatter = DecimalFormat("#,##0.00", customSymbols)
        .apply { roundingMode = RoundingMode.HALF_UP }
    val integerFormatter = DecimalFormat("#,###", customSymbols)
        .apply { roundingMode = RoundingMode.HALF_UP }

    val dateFormat = retrieveDateFormat(dateFormatValue)

    return SettingsValues(
        currencySymbol, currencySymbolSide, thousandsSymbol, decimalSymbol,
        decimalNumber, decimalFormatter, integerFormatter, dateFormat
    )
}