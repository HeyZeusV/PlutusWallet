package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.heyzeusv.plutuswallet.R
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
            lgPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _ : Preference, newValue : Any ->

                // checks if a different language was selected
                if (languageSet != newValue.toString()) {

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

        // initialize and set up Builder
        val alertDialogBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
            // set title
            .setTitle(getString(R.string.alert_dialog_are_you_sure))
            // set positive button and its click listener
            .setPositiveButton(getString(R.string.alert_dialog_switch)) { _ : DialogInterface, _ : Int ->

                dpPreference.isChecked = !dpPreference.isChecked
            }
            // set negative button and its click listener
            .setNegativeButton(getString(R.string.alert_dialog_cancel)) { _ : DialogInterface, _ : Int -> }
        if (warning) {

            // set message
            alertDialogBuilder.setMessage(getString(R.string.alert_dialog_decimal_place_warning))
        }
        // make AlertDialog using Builder
        val decimalAlertDialog : AlertDialog = alertDialogBuilder.create()
        // display AlertDialog
        decimalAlertDialog.show()
    }

    /**
     *  AlertDialog asking if the user wants to switch the thousands and decimal symbols
     */
    private fun switchSymbolDialog() {

        // initialize and set up Builder
        val alertDialogBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
            // set title
            .setTitle("Duplicate Symbols")
            // set message
            .setMessage("The thousands and decimal separators cannot be the same... Would you like to switch them?")
            // set positive button and its click listener
            .setPositiveButton("Switch") { _ : DialogInterface, _ : Int ->

                tsPreference.value = decimal
                dsPreference.value = thousands
                decimal   = dsPreference.value
                thousands = tsPreference.value
            }
            // set negative button and its click listener
            .setNegativeButton("Cancel") { _ : DialogInterface, _ : Int -> }
        // make AlertDialog using builder
        val alertDialog : AlertDialog = alertDialogBuilder.create()
        // display AlertDialog
        alertDialog.show()
    }
}