package com.heyzeusv.plutuswallet.ui.transaction

import com.heyzeusv.plutuswallet.R
import kotlinx.coroutines.flow.MutableStateFlow

enum class TransactionTextFields(val labelId: Int, val helperId: Int, val length: Int) {
    TITLE(R.string.transaction_title, R.string.transaction_title_hint, R.integer.maxLengthTitle),
    MEMO(R.string.transaction_memo, R.string.transaction_memo_hint, R.integer.maxLengthMemo),
}

enum class TransactionDropMenus(val labelId: Int, val createNewId: Int, val alertTitleId: Int) {
    ACCOUNT(R.string.transaction_account, R.string.account_create, R.string.alert_dialog_create_account),
    EXPENSE(R.string.transaction_category, R.string.category_create, R.string.alert_dialog_create_category),
    INCOME(R.string.transaction_category, R.string.category_create, R.string.alert_dialog_create_category),
    PERIOD(R.string.transaction_period, 0, 0)
}

enum class TransactionNumberFields(val labelId: Int, val length: Int) {
    TOTAL_DECIMAL(R.string.transaction_total, R.integer.maxLengthTotalDecimal),
    TOTAL_INTEGER(R.string.transaction_total, R.integer.maxLengthTotalInteger),
    FREQUENCY(R.string.transaction_frequency, R.integer.maxLengthFrequency)
}

enum class TransactionChips(val labelId: Int, val icon: Boolean) {
    EXPENSE(R.string.type_expense, false),
    INCOME(R.string.type_income, false),
    REPEAT(R.string.transaction_repeat, true)
}

enum class TransactionType(val type: String, val boolValue: MutableStateFlow<Boolean>) {
    EXPENSE("Expense", MutableStateFlow(false)),
    INCOME("Income", MutableStateFlow(true))
}