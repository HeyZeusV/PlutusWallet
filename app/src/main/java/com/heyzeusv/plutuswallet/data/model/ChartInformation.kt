package com.heyzeusv.plutuswallet.data.model

/**
 *  Not exactly an entity, but more of a helper object to hold Graph data that is displayed on
 *  ChartCard Composable. [ctList] is the list of [CategoryTotals] which each represent a slice of
 *  the pie chart. [totalText] is the formatted text that displays the total of [ctList].
 */
data class ChartInformation(
    val ctList: List<CategoryTotals> = emptyList(),
    var totalText: String = ""
)