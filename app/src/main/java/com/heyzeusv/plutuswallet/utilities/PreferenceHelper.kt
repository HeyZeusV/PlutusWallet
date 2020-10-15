package com.heyzeusv.plutuswallet.utilities

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
     *  Puts a key value pair in shared prefs if doesn't exists, otherwise updates value on given [key].
     */
    operator fun SharedPreferences.set(key: String, value: Any) {

        when (value) {
            is String -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    /**
     * Finds value on given [key].
     * T is the type of value
     * [defaultValue] will be used if no value is found for key.
     */
    inline operator fun <reified T : Any> SharedPreferences.get(
        key: String,
        defaultValue: T
    ): T {

        return when (T::class) {
            String::class -> getString(key, defaultValue as String) as T
            Int::class -> getInt(key, defaultValue as Int) as T
            Boolean::class -> getBoolean(key, defaultValue as Boolean) as T
            Float::class -> getFloat(key, defaultValue as Float) as T
            Long::class -> getLong(key, defaultValue as Long) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }
}