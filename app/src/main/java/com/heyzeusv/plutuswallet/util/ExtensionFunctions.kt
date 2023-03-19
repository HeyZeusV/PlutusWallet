package com.heyzeusv.plutuswallet.util

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import java.math.BigDecimal
import java.time.ZonedDateTime

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
 *  Navigates app to [route]
 */
fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        // pressing back from any screen would pop back stack to Overview
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
        // only 1 copy of a destination is ever created
        launchSingleTop = true
        // previous data and state is saved
        restoreState = true
    }

/**
 *  Used whenever navigating to TransactionScreen, opens the Transaction screen while passing
 *  [tranId] as argument to determine which Transaction to open
 */
fun NavHostController.navigateToTransactionWithId(tranId: Int) {
    this.navigateSingleTopTo("${TransactionDestination.route}/$tranId")
}

/**
 *  Combines isAfter() and isEqual() into one
 */
fun ZonedDateTime.isAfterEqual(other: ZonedDateTime): Boolean {
    return isAfter(other) || isEqual(other)
}

/**
 *  Combines isBefore() and isEqual() into one
 */
fun ZonedDateTime.isBeforeEqual(other: ZonedDateTime): Boolean {
    return isBefore(other) || isEqual(other)
}