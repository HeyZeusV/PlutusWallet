package com.heyzeusv.financeapplication.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.heyzeusv.financeapplication.utilities.Utils
import java.util.*

// SharedPreferences Keys
const val KEY_LANGUAGE         = "key_language"
const val KEY_MANUAL_LANGUAGE  = "key_manual_language"
const val KEY_LANGUAGE_CHANGED = "key_language_changed"

/**
 *  Base Activity that all Activities will extend
 *
 *  Sets language on Activities based on language selected in Settings.
 */
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var sharedPreferences : SharedPreferences
    protected lateinit var editor            : SharedPreferences.Editor

    // array of languages supported
    private val languages : Array<String> = arrayOf("en", "es", "de", "hi", "ja", "ko", "th")

    @SuppressLint("CommitPrefEdits")
    override fun attachBaseContext(newBase: Context?) {

        // SharedPreference/Editor will be available to all Activities that inherit this class
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(newBase)
        editor            = sharedPreferences.edit()

        val manualChange : Boolean = sharedPreferences.getBoolean(KEY_MANUAL_LANGUAGE, false)

        // API 24 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            // retrieves system Locale
            val systemLocale : Locale = Resources.getSystem().configuration.locales[0]

            // if system Locale is supported and user hasn't manually selected a language from settings
            // use the system Locale language. Creates pre made categories in system language
            if (languages.contains(systemLocale.language) && !manualChange) {

                super.attachBaseContext(newBase)

            // API 26 or higher, can't get manual language change to work API 25 and below, but user
            // can change system language and app will change
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manualChange) {

                // retrieves language selected
                val languageCode : String  = sharedPreferences.getString(KEY_LANGUAGE, "en")!!
                // sets context with language
                val context : Context = Utils.changeLanguage(newBase!!, languageCode)

                super.attachBaseContext(context)

            } else {

                super.attachBaseContext(newBase)
            }
        } else {

            super.attachBaseContext(newBase)
        }
    }
}