package com.heyzeusv.plutuswallet.util

import android.view.View
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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

/**
 *  Adds only unique entries from [list].
 */
fun <T> MutableList<T>.addAllUnique(list: MutableList<T>) {

    for (entry: T in list) {
        if (!this.contains(entry)) add(entry)
    }
}

/**
 *  Checks all Chips in ChipGroup.
 */
fun ChipGroup.checkAll() {

    // retrieves ChipGroup child Chips
    val chipChildren: Sequence<View> = this.children
    // iterator for child Chips sequence
    val chipIterator: Iterator<View> = chipChildren.iterator()
    // check each child Chip
    chipIterator.forEach {
        (it as Chip).isChecked = true
    }
}