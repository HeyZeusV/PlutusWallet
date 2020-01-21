package com.heyzeusv.plutuswallet.database.entities

import java.util.Date

/**
 *  Holds data to be sent to CFLViewModel that then gets passed to Repository to complete a query.
 *
 *  @param  account      boolean for account filter.
 *  @param  category     boolean for category filter.
 *  @param  date         boolean for date filter.
 *  @param  type         either "Expense" or "Income".
 *  @param  accountName  account name to be searched.
 *  @param  categoryName category name to be searched in table of type.
 *  @param  start        starting Date for date filter.
 *  @param  end          ending Date for date filter.
 */
class TransactionInfo(
    val account      : Boolean?,
    val category     : Boolean?,
    val date         : Boolean?,
    val type         : String?,
    val accountName  : String?,
    val categoryName : String?,
    val start        : Date?,
    val end          : Date?
)