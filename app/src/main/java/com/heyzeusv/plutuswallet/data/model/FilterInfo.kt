package com.heyzeusv.plutuswallet.data.model

import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime

/**
 *  Holds data to be sent to CFLViewModel that then gets passed to Repository to complete a query.
 *  [account], [category], and [date] are Booleans used to determine which filters have been turned
 *  on/off. [accountNames] and [categoryNames] are the list of chips that user has selected to be
 *  displayed when filter is applied. [type] is used to determine "Expense" or "Income" Categories.
 *  When applying the Date filter, user must select a [start] date and an [end] date.
 */
data class FilterInfo(
    val account: Boolean = false,
    val accountNames: List<String> = listOf(""),
    val category: Boolean = false,
    val type: String = "",
    val categoryNames: List<String> = listOf(""),
    val date: Boolean = false,
    val start: ZonedDateTime = ZonedDateTime.of(1, 1, 1, 1, 1, 1, 1, systemDefault()),
    val end: ZonedDateTime = ZonedDateTime.of(1, 1, 1, 1, 1, 1, 2, systemDefault()),
)