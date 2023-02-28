package com.heyzeusv.plutuswallet.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.heyzeusv.plutuswallet.R

/**
 *  Enum to ensure that type from Transaction object is always either Expense or Income. [type] is
 *  English string version of Expense/Income which gets saved to Database. [stringId] is the
 *  string resource id of the translated version of Expense/Income which gets displayed to user.
 */
enum class TransactionType(val type: String, val stringId: Int) {
    EXPENSE("Expense", R.string.type_expense),
    INCOME("Income", R.string.type_income);

    // used to quickly switch between Expense/Income
    fun opposite(): TransactionType = if (this == EXPENSE) INCOME else EXPENSE
}

/**
 *  Enum for the available options when user opens Drawer. [icon] is the visual representation of
 *  item that is displayed next to label. [labelId] is the translated string resource id of item.
 *  [route] is used by Navigation to determine where item leads user to.
 */
enum class PWDrawerItems(val icon: ImageVector, val labelId: Int, val route: String) {
    ACCOUNTS(Icons.Filled.AccountBalance, R.string.accounts, AccountsDestination.route),
    CATEGORIES(Icons.Filled.Category, R.string.categories, CategoriesDestination.route),
    SETTINGS(Icons.Filled.Settings, R.string.settings, SettingsDestination.route),
    ABOUT(Icons.Filled.PermDeviceInformation, R.string.about, AboutDestination.route)
}

/**
 *  Enum for the possible states of Filter when user presses "Apply" button. [stringId] is the
 *  string resource id of translated string that is displayed by Snackbar when state is an error.
 */
enum class FilterState(val stringId: Int) {
    VALID(R.string.blank_string),
    NO_SELECTED_ACCOUNT(R.string.filter_no_selected_account),
    NO_SELECTED_CATEGORY(R.string.filter_no_selected_category),
    NO_SELECTED_DATE(R.string.filter_no_selected_dates),
    INVALID_DATE_RANGE(R.string.filter_invalid_date_range)
}

/**
 *  Enum for the possible actions on the Chips in Filter.
 */
enum class FilterChipAction {
    ADD,
    REMOVE
}

/**
 *  Enum for the possible actions possible on List screen items.
 */
enum class ListItemAction {
    CREATE,
    DELETE,
    EDIT
}

/**
 *  Enum for the available settings in the Settings screen. [titleId] is the string resource id for
 *  the name of the setting. [valueArrayId] is the string array resource id for the values that get
 *  saved to SharedPreferences after user selects an option. [displayArrayId] is the string array
 *  resource id for the values that get shown to user. Can be seen as [valueArrayId] are the keys
 *  while [displayArrayId] are the values in a map. [key] is [Key] object which holds the
 *  SharedPreferences key for the setting.
 */
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