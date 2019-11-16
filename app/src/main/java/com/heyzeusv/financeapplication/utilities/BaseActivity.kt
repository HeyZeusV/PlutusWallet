package com.heyzeusv.financeapplication.utilities

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity

const val KEY_LANGUAGE = "key_language"

/**
 *  Base Activity that all Activities will extend
 *
 *  Sets language on Activities based on language selected in Settings.
 */
abstract class BaseActivity : AppCompatActivity() {

    lateinit var sharedPreferences : SharedPreferences

    override fun attachBaseContext(newBase: Context?) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(newBase)

        // couldn't get it to work below API26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            // retrieves language selected
            val languageCode : String  = sharedPreferences.getString(KEY_LANGUAGE, "en")!!
            // sets context with language
            val context      : Context = Utils.changeLanguage(newBase!!, languageCode)

            super.attachBaseContext(context)
        } else {

            super.attachBaseContext(newBase)
        }
    }
}