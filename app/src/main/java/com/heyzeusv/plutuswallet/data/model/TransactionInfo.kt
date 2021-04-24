package com.heyzeusv.plutuswallet.data.model

import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *  Holds data to be sent to CFLViewModel that then gets passed to Repository to complete a query.
 *
 *  @param  account       boolean for account filter.
 *  @param  category      boolean for category filter.
 *  @param  date          boolean for date filter.
 *  @param  type          either "Expense" or "Income".
 *  @param  accountNames  account names to be searched.
 *  @param  categoryNames category names to be searched in table of type.
 *  @param  start         starting Date for date filter.
 *  @param  end           ending Date for date filter.
 */
data class TransactionInfo(
    val account: Boolean = false,
    val category: Boolean = false,
    val date: Boolean = false,
    val type: String = "",
    val accountNames: List<String> = listOf(""),
    val categoryNames: List<String> = listOf(""),
    val start: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
    val end: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
)