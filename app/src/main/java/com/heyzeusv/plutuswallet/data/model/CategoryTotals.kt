package com.heyzeusv.plutuswallet.data.model

import java.math.BigDecimal

/**
 *  Not exactly an entity, but more of a helper object which takes a [category] name and adds up
 *  all the totals of same Category as [total]. Since Categories can have the same name, but be of
 *  different type ("Expense" or "Income"), [type] is used to differentiate between the two.
 */
data class CategoryTotals(
    var category: String,
    var total: BigDecimal,
    var type: String
)