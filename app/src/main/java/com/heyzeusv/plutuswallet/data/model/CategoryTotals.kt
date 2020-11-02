package com.heyzeusv.plutuswallet.data.model

import java.math.BigDecimal

/**
 *  Not exactly an entity, but more of a helper object to hold Category name and total
 *  of combined Transactions of said Category.
 *
 *  @param category the name of category.
 *  @param total    the total of all the Transactions of same Category.
 *  @param type     either "Expense" or "Income".
 */
data class CategoryTotals(
    var category: String,
    var total: BigDecimal,
    var type: String
)