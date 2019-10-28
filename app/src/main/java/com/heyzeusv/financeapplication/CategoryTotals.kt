package com.heyzeusv.financeapplication

import java.math.BigDecimal

/**
 *  Not exactly an entity, but more of a helper object to hold Category name and total
 *  of combined Transactions of said Category.
 *
 *  @param category the name of category.
 *  @param total    the total of all the Transactions of same Category.
 */
data class CategoryTotals(
    val category : String,
    val total    : BigDecimal
)