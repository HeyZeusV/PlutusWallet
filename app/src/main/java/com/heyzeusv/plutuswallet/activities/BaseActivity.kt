package com.heyzeusv.plutuswallet.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.heyzeusv.plutuswallet.utilities.Constants
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import com.heyzeusv.plutuswallet.utilities.Utils

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
            val context: Context = Utils.changeLanguage(newBase, languageCode)

            super.attachBaseContext(context)
        } else {
            super.attachBaseContext(newBase)
        }
    }
}