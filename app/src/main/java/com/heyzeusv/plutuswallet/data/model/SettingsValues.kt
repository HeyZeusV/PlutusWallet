package com.heyzeusv.plutuswallet.data.model

import java.text.DateFormat
import java.text.DecimalFormat

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