package com.heyzeusv.plutuswallet.util

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

enum class DataListSelectedAction {
    CREATE,
    DELETE,
    EDIT
}

enum class SettingOptions(
    val titleId: Int,
    val valueArrayId: Int,
    val displayArrayId: Int,
    val key: Key
) {
    THEME(
        R.string.preferences_theme,
        R.array.theme_value_array,
        R.array.theme_display_array,
        Key.KEY_THEME
    ),
    CURRENCY_SYMBOL(
        R.string.preferences_currency_symbol,
        R.array.currency_symbol_value_array,
        R.array.currency_symbol_display_array,
        Key.KEY_CURRENCY_SYMBOL
    ),
    CURRENCY_SYMBOL_SIDE(
        R.string.preferences_currency_symbol_side,
        R.array.currency_symbol_side_value_array,
        R.array.currency_symbol_side_display_array,
        Key.KEY_CURRENCY_SYMBOL_SIDE
    ),
    THOUSANDS_SYMBOL(
        R.string.preferences_thousands_symbol,
        R.array.separator_symbol_value_array,
        R.array.separator_symbol_display_array,
        Key.KEY_THOUSANDS_SYMBOL
    ),
    DECIMAL_SYMBOL(
        R.string.preferences_decimal_symbol,
        R.array.separator_symbol_value_array,
        R.array.separator_symbol_display_array,
        Key.KEY_DECIMAL_SYMBOL
    ),
    DECIMAL_NUMBER(
        R.string.preferences_decimal_number,
        R.array.decimal_number_value_array,
        R.array.decimal_number_display_array,
        Key.KEY_DECIMAL_NUMBER
    ),
    DATE_FORMAT(
        R.string.preferences_date_format,
        R.array.date_format_value_array,
        R.array.date_format_display_array,
        Key.KEY_DATE_FORMAT
    ),
    LANGUAGE(
        R.string.preferences_language,
        R.array.language_value_array,
        R.array.language_display_array,
        Key.KEY_LANGUAGE
    )
}