package com.heyzeusv.plutuswallet.ui.transaction

import com.heyzeusv.plutuswallet.R

enum class TransactionType(val type: String, val stringId: Int) {
    EXPENSE("Expense", R.string.type_expense),
    INCOME("Income", R.string.type_income);

    fun opposite(): TransactionType = if (this == EXPENSE) INCOME else EXPENSE
}