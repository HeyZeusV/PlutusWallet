package com.heyzeusv.plutuswallet.data.model

import java.text.DateFormat
import java.text.DecimalFormat

/**
 *  Data class which holds most Settings that user can change in SettingsScreen Composable. These
 *  values are collecting in StateFlows in order to keep the UI updated when user changes any
 *  Setting. [currencySymbol], [thousandsSymbol], and [decimalSymbol] are the various symbols that
 *  are displayed whenever a total is shown; they are passed along to [decimalFormatter] and
 *  [integerFormatter] which are used to retrieve a formatted total String. [currencySymbolSide]
 *  determines if [currencySymbol] should appear on the left or right of total. [decimalNumber]
 *  determines if totals should be decimal or integer numbers. [dateFormatter] is used to retrieve
 *  a formatted date String.
 */
data class SettingsValues(
    var currencySymbol: String = "$",
    var currencySymbolSide: String = "left",
    var thousandsSymbol: Char = ',',
    var decimalSymbol: Char = '.',
    var decimalNumber: String = "yes",
    var decimalFormatter: DecimalFormat = DecimalFormat("#,##0.00"),
    var integerFormatter: DecimalFormat = DecimalFormat("#,###"),
    var dateFormatter: DateFormat = DateFormat.getDateInstance(0)
)