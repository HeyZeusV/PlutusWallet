package com.heyzeusv.plutuswallet.data.model

import java.text.DecimalFormat

data class SettingsValues(
    var currencySymbol: String = "$",
    var symbolSide: Boolean = true,
    var thousandsSymbol: Char = ',',
    var decimalPlaces: Boolean = true,
    var decimalSymbol: Char = '.',
    var dateFormat: Int = 0,
    var decimalFormatter: DecimalFormat = DecimalFormat("#,##0.00"),
    var integerFormatter: DecimalFormat = DecimalFormat("#,###"),
    var decimalNumber: String = "yes",
    var currencySymbolSide: String = "left"
)