package com.heyzeusv.plutuswallet.util

import android.content.SharedPreferences

/**
 *  Helper class for SharedPreferences using extension functions.
 */
object PreferenceHelper {

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor: SharedPreferences.Editor = this.edit()
        operation(editor)
        editor.apply()
    }

    /**
     *  Puts a key value pair in shared prefs if doesn't exists, otherwise updates value on
     *  given [key].
     */
    operator fun SharedPreferences.set(key: Key, value: Any) {

        when (value) {
            is String -> edit { it.putString(key.key, value) }
            is Int -> edit { it.putInt(key.key, value) }
            is Boolean -> edit { it.putBoolean(key.key, value) }
            is Float -> edit { it.putFloat(key.key, value) }
            is Long -> edit { it.putLong(key.key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    /**
     * Finds value on given [key].
     * T is the type of value
     * [defaultValue] will be used if no value is found for key.
     */
    inline operator fun <reified T : Any> SharedPreferences.get(
        key: Key,
        defaultValue: T
    ): T {

        return when (T::class) {
            String::class -> getString(key.key, defaultValue as String) as T
            Int::class -> getInt(key.key, defaultValue as Int) as T
            Boolean::class -> getBoolean(key.key, defaultValue as Boolean) as T
            Float::class -> getFloat(key.key, defaultValue as Float) as T
            Long::class -> getLong(key.key, defaultValue as Long) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }
}

// SharedPreferences Keys
enum class Key(val key: String) {
    KEY_CURRENCY_SYMBOL_SIDE("key_currency_symbol_side"),
    KEY_CURRENCY_SYMBOL("key_currency_symbol"),
    KEY_DATE_FORMAT("key_date_format"),
    KEY_DECIMAL_NUMBER("key_decimal_number"),
    KEY_DECIMAL_SYMBOL("key_decimal_symbol"),
    KEY_LANGUAGE("key_language"),
    KEY_MANUAL_LANGUAGE("key_manual_language"),
    KEY_THEME("key_theme"),
    KEY_THOUSANDS_SYMBOL("key_thousands_symbol"),
    KEY_VIEW("key_view")
}