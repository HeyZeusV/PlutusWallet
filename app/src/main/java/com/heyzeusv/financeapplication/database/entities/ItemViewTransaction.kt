package com.heyzeusv.financeapplication.database.entities

import java.math.BigDecimal
import java.util.Date

/**
 *  Not exactly an entity, but more of a helper object to hold Transaction data that
 *  is displayed on TransactionListFragment since not all Transaction data is displayed.
 *
 *  @param id       unique id of Transaction.
 *  @param title    title of Transaction.
 *  @param date     Date of Transaction.
 *  @param total    the total amount of Transaction.
 *  @param type     either "Expense" or "Income".
 *  @param category the name of category selected.
 */
class ItemViewTransaction(
    val id       : Int,
    val title    : String,
    val date     : Date,
    val total    : BigDecimal,
    val type     : String,
    val category : String)