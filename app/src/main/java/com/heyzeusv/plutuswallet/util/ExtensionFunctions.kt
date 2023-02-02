package com.heyzeusv.plutuswallet.util

import com.heyzeusv.plutuswallet.data.model.SettingsValues
import java.math.BigDecimal

/**
 *  Returns formatted String which includes currency, thousands, and decimal symbols depending
 *  on [setVals] values.
 */
fun BigDecimal.prepareTotalText(setVals: SettingsValues): String {
    val total = this
    setVals.apply {
        return when {
            // currency symbol on left with decimal places
            decimalNumber == "yes" && currencySymbolSide == "left" -> {
                "$currencySymbol${decimalFormatter.format(total)}"
            }
            // currency symbol on right with decimal places
            decimalNumber == "yes" -> "${decimalFormatter.format(total)}$currencySymbol"
            // currency symbol on left without decimal places
            currencySymbolSide == "left" -> "$currencySymbol${integerFormatter.format(total)}"
            // currency symbol on right without decimal places
            else -> "${integerFormatter.format(total)}$currencySymbol"
        }
    }
}

/**
 *  Replace first instance of [old] with [new].
 */
fun <T> MutableList<T>.replace(old: T, new: T) {

    this[indexOf(old)] = new
}