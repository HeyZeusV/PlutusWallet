package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.utilities.KEY_LANGUAGE_CHANGED
import com.heyzeusv.plutuswallet.utilities.KEY_MANUAL_LANGUAGE
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set

/**
 *  Shows different options users can change to better their experience
 */
class SettingsFragment : PreferenceFragmentCompat() {

    // SharedPreferences
    private lateinit var sharedPreferences : SharedPreferences

    // Preferences
    private lateinit var tsPreference : ListPreference
    private lateinit var dpPreference : SwitchPreference
    private lateinit var dsPreference : ListPreference
    private lateinit var lgPreference : ListPreference

    // used to tell if a new language is set or if the same is re-selected
    private lateinit var languageSet : String

    // used to tell which symbol is assigned
    private lateinit var thousands   : String
    private lateinit var decimal     : String

    @SuppressLint("CommitPrefEdits")
    override fun onCreatePreferences(savedInstanceState : Bundle?, rootKey : String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // initialize SharedPreferences
        sharedPreferences = PreferenceHelper.sharedPrefs(activity!!)

        // initialize Preferences
        tsPreference = findPreference("key_thousands_symbol")!!
        dpPreference = findPreference("key_decimal_places"  )!!
        dsPreference = findPreference("key_decimal_symbol"  )!!
        lgPreference = findPreference("key_language"        )!!

        // initialize variables with Preference values
        languageSet = lgPreference.value
        thousands   = tsPreference.value
        decimal     = dsPreference.value
    }

    override fun onStart() {
        super.onStart()

        // Thousands Symbol
        tsPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _ : Preference, newValue : Any ->

            when (newValue) {

                // if newValue == decimal then launch AlertDialog
                decimal -> {

                    switchSymbolDialog()
                    return@OnPreferenceChangeListener false
                }
                // else switch to newValue
                else -> {

                    thousands = newValue.toString()
                    return@OnPreferenceChangeListener true
                }
            }

        }

        // Decimal Places
        dpPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _ : Preference, _ : Any ->

            // asks the user if they do want to switch decimalPlacesPreference
            allowDecimalDialog(dpPreference.isChecked)
            return@OnPreferenceChangeListener false
        }

        // Decimal Symbol
        dsPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _ : Preference, newValue : Any ->

            when (newValue) {

                // if newValue == thousands then launch AlertDialog
                thousands -> {

                    switchSymbolDialog()
                    return@OnPreferenceChangeListener false
                }
                // else switch to newValue
                else -> {

                    decimal = newValue.toString()
                    return@OnPreferenceChangeListener true
                }
            }

        }

        // only works on API26 and higher
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            lgPreference.isVisible = false
        } else {

            // Language
            lgPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _ : Preference, languageCode : Any ->

                // checks if a different language was selected
                if (languageSet != languageCode.toString()) {

                    // saving into SharedPreferences
                    sharedPreferences[KEY_LANGUAGE_CHANGED] = true
                    sharedPreferences[KEY_MANUAL_LANGUAGE ] = true

                    // destroys then restarts Activity in order to have updated language
                    activity!!.recreate()
                }
                return@OnPreferenceChangeListener true
            }
        }
    }

    /**
     *  Launches AlertDialog asking user if they want to allow/disallow the use of decimal places.
     *
     *  @param warning true if user is turning off preference which sets an addition message
     */
    private fun allowDecimalDialog(warning : Boolean) {

        val message : String = if (warning) {

            getString(R.string.alert_dialog_decimal_place_warning)
        } else {

            ""
        }
        val posFun = DialogInterface.OnClickListener { _, _ ->

            dpPreference.isChecked = !dpPreference.isChecked
        }
        AlertDialogCreator.alertDialog(context!!,
            getString(R.string.alert_dialog_are_you_sure),
            message,
            getString(R.string.alert_dialog_switch), posFun,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing)
    }

    /**
     *  AlertDialog asking if the user wants to switch the thousands and decimal symbols
     */
    private fun switchSymbolDialog() {

        val posFun = DialogInterface.OnClickListener { _, _ ->

            tsPreference.value = decimal
            dsPreference.value = thousands
            decimal   = dsPreference.value
            thousands = tsPreference.value
        }

        AlertDialogCreator.alertDialog(context!!,
            getString(R.string.alert_dialog_duplicate_symbols),
            getString(R.string.alert_dialog_duplicate_symbols_warning),
            getString(R.string.alert_dialog_switch), posFun,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing)
    }
}