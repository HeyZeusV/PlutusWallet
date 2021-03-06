package com.heyzeusv.plutuswallet.data.model

/**
 *  Not exactly an entity, but more of a helper object to hold Graph data that is displayed on
 *  ChartFragment.
 *
 *  @param ctList     data to be show in PieChart.
 *  @param typeTrans  translated text that will appear in center of PieChart.
 *  @param totalText  translated text that will display formatted Total.
 *  @param colorArray colors used by PieChart when showing data.
 *  @param fType      either "Expense" or "Income" selected from filter.
 *  @param fCategory  boolean stating if Category filter is applied.
 *  @param fCatName   string of Category selected from filter.
 */
data class ItemViewChart(
    val ctList: List<CategoryTotals> = emptyList(),
    val typeTrans: String = "",
    var totalText: String = "",
    val colorArray: List<Int> = emptyList(),
    val fCategory: Boolean = false,
    val fCatName: List<String> = listOf(""),
    val fType: String = ""
)