package com.heyzeusv.plutuswallet.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.heyzeusv.plutuswallet.utilities.Constants
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import java.util.Locale

/**
 *  Base Activity that all Activities will extend
 *
 *  Sets language on Activities based on language selected in Settings.
 */
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var sharedPref: SharedPreferences

    @SuppressLint("CommitPrefEdits")
    override fun attachBaseContext(newBase: Context?) {

        // SharedPreference/Editor will be available to all Activities that inherit this class
        sharedPref = PreferenceHelper.sharedPrefs(newBase!!)
        val manualChange: Boolean = sharedPref[Constants.KEY_MANUAL_LANGUAGE, false]!!

        // API 26 or higher, can't get manual language change to work API 25 and below, but user
        // can change system language and app will change
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manualChange) {
            // retrieves language selected
            val languageCode: String = sharedPref[Constants.KEY_LANGUAGE, "en"]!!
            // sets context with language
            val context: Context = changeLanguage(newBase, languageCode)

            super.attachBaseContext(context)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    /**
     *  Uses app [context] to return a new Context containing language selected
     *  using [languageCode].
     */
    fun changeLanguage(context: Context, languageCode: String): ContextWrapper {

        val config: Configuration = context.resources.configuration
        val newContext: Context
        val newLocale = Locale(languageCode)
        // sets locale for JVM
        Locale.setDefault(newLocale)
        // sets locale for context
        config.setLocale(newLocale)
        // new context needed since parameters are final
        newContext = context.createConfigurationContext(config)

        return ContextWrapper(newContext)
    }
}