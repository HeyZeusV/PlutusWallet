package com.heyzeusv.plutuswallet.ui.transaction

import com.heyzeusv.plutuswallet.R

enum class TransactionType(val type: String, val stringId: Int) {
    EXPENSE("Expense", R.string.type_expense),
    INCOME("Income", R.string.type_income);

    fun opposite(): TransactionType = if (this == EXPENSE) INCOME else EXPENSE
}

enum class FilterState(val stringId: Int) {
    VALID(R.string.blank_string),
    NO_SELECTED_ACCOUNT(R.string.filter_no_selected_account),
    NO_SELECTED_CATEGORY(R.string.filter_no_selected_category),
    NO_SELECTED_DATE(R.string.filter_no_selected_dates),
    INVALID_DATE_RANGE(R.string.filter_date_warning)
}

enum class FilterSelectedAction {
    ADD,
    REMOVE
}