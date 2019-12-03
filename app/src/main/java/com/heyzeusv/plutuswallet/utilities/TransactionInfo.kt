package com.heyzeusv.plutuswallet.utilities

import java.util.Date

/**
 *  Holds data to be sent to FGLViewModel that then gets past to Repository
 *  to complete a query.
 *
 *  @param  category     boolean for category filter
 *  @param  date         boolean for date filter
 *  @param  type         either "Expense" or "Income"
 *  @param  categoryName category name to be searched in table of type
 *  @param  start        starting Date for date filter
 *  @param  end          ending Date for date filter
 */
class TransactionInfo(
    val category     : Boolean?,
    val date         : Boolean?,
    val type         : String?,
    val categoryName : String?,
    val start        : Date?,
    val end          : Date?
)