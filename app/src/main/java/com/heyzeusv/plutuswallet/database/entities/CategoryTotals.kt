package com.heyzeusv.plutuswallet.database.entities

import java.math.BigDecimal

/**
 *  Not exactly an entity, but more of a helper object to hold Category name and total
 *  of combined Transactions of said Category.
 *
 *  @param category the name of category.
 *  @param total    the total of all the Transactions of same Category.
 */
data class CategoryTotals(
    var category : String,
    var total    : BigDecimal
)