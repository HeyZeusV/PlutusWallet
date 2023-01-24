package com.heyzeusv.plutuswallet.ui.transaction

import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.util.Key

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

enum class DataListSelectedAction {
    CREATE,
    DELETE,
    EDIT
}

enum class SettingOptions(val titleId: Int, val keyId: Int, val valueId: Int, val key: Key, isBool: Boolean = false) {
    THEME(
        R.string.preferences_theme,
        R.array.theme_key_array,
        R.array.theme_array,
        Key.KEY_THEME
    ),
    CURRENCY_SYMBOL(
        R.string.preferences_currency_symbol,
        R.array.currency_symbol_key_array,
        R.array.currency_symbol_array,
        Key.KEY_CURRENCY_SYMBOL
    ),
    SYMBOL_SIDE(
        R.string.preferences_symbol_side,
        R.array.symbol_side_key_array,
        R.array.symbol_side_array,
        Key.KEY_SYMBOL_SIDE,
        true
    ),
    THOUSANDS_SYMBOL(
        R.string.preferences_thousands_symbol,
        R.array.separator_symbol_key_array,
        R.array.separator_symbol_array,
        Key.KEY_THOUSANDS_SYMBOL
    ),
    DECIMAL_SYMBOL(
        R.string.preferences_decimal_symbol,
        R.array.separator_symbol_key_array,
        R.array.separator_symbol_array,
        Key.KEY_DECIMAL_SYMBOL
    ),
    NUMBER_DECIMAL(
        R.string.preferences_number_decimal,
        R.array.number_decimal_key_array,
        R.array.number_decimal_array,
        Key.KEY_DECIMAL_PLACES,
        true
    ),
    DATE_FORMAT(
        R.string.preferences_date_format,
        R.array.date_format_key_array,
        R.array.date_format_array,
        Key.KEY_DATE_FORMAT
    ),
    LANGUAGE(
        R.string.preferences_language,
        R.array.language_key_array,
        R.array.language_array,
        Key.KEY_LANGUAGE
    )
}