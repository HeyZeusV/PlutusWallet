package com.heyzeusv.plutuswallet.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.prepareSettingValues
import java.util.Locale

/**
 *  Base Activity that all Activities will extend
 *
 *  Sets language on Activities based on language selected in Settings.
 */
abstract class BaseActivity : AppCompatActivity() {

    // cannot inject because injection occurs in onCreate()
    protected lateinit var sharedPref: SharedPreferences

    @SuppressLint("CommitPrefEdits")
    override fun attachBaseContext(newBase: Context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(newBase)
        prepareSettingValues(sharedPref)
        val manualChange: Boolean = sharedPref[Key.KEY_MANUAL_LANGUAGE, false]

        // sets to language selected in SettingsScreen else uses system language if available
        if (manualChange) {
            val language: String = sharedPref[Key.KEY_LANGUAGE, "en"]

            super.attachBaseContext(ContextWrapper(newBase.setAppLocale(language)))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    /**
     *  Extension function to set Locale to [languageCode].
     */
    fun Context.setAppLocale(languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        return createConfigurationContext(config)
    }
}