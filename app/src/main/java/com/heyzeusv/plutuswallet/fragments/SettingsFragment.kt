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
import com.heyzeusv.plutuswallet.utilities.Constants
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 *  Shows different options users can change to better their experience
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    // SharedPreferences
    @Inject lateinit var sharedPref: SharedPreferences

    // Preferences
    private lateinit var csPref: ListPreference
    private lateinit var ssPref: SwitchPreference
    private lateinit var tsPref: ListPreference
    private lateinit var dpPref: SwitchPreference
    private lateinit var dsPref: ListPreference
    private lateinit var dfPref: ListPreference
    private lateinit var lgPref: ListPreference

    @SuppressLint("CommitPrefEdits")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // initialize Preferences
        csPref = findPreference(Constants.KEY_CURRENCY_SYMBOL)!!
        ssPref = findPreference(Constants.KEY_SYMBOL_SIDE)!!
        tsPref = findPreference(Constants.KEY_THOUSANDS_SYMBOL)!!
        dpPref = findPreference(Constants.KEY_DECIMAL_PLACES)!!
        dsPref = findPreference(Constants.KEY_DECIMAL_SYMBOL)!!
        dfPref = findPreference(Constants.KEY_DATE_FORMAT)!!
        lgPref = findPreference(Constants.KEY_LANGUAGE)!!
    }

    override fun onStart() {
        super.onStart()

        // Currency Symbol
        csPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue: Any ->
                if (newValue == csPref.value) {
                    return@OnPreferenceChangeListener false
                } else {
                    // used to tell if data in Chart/TranList Fragments should be updated
                    sharedPref[Constants.KEY_CHART_CHANGE] = true
                    sharedPref[Constants.KEY_TRAN_LIST_CHANGE] = true
                    return@OnPreferenceChangeListener true
                }
            }

        // Symbol Side
        ssPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                // used to tell if data in Chart/TranList Fragments should be updated
                sharedPref[Constants.KEY_CHART_CHANGE] = true
                sharedPref[Constants.KEY_TRAN_LIST_CHANGE] = true
                return@OnPreferenceChangeListener true
            }

        // Thousands Symbol
        tsPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue: Any ->
                if (newValue == dsPref.value) {
                    // if newValue == decimal then launch AlertDialog
                    switchSymbolDialog()
                    return@OnPreferenceChangeListener false
                } else {
                    // used to tell if data in Chart/TranList Fragments should be updated
                    sharedPref[Constants.KEY_CHART_CHANGE] = true
                    sharedPref[Constants.KEY_TRAN_LIST_CHANGE] = true
                    return@OnPreferenceChangeListener true
                }
            }

        // Decimal Places
        dpPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                // asks the user if they do want to switch decimalPlacesPreference
                allowDecimalDialog(dpPref.isChecked)
                return@OnPreferenceChangeListener false
            }

        // Decimal Symbol
        dsPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue: Any ->

                if (newValue == tsPref.value) {
                    // if newValue == thousands then launch AlertDialog
                    switchSymbolDialog()
                    return@OnPreferenceChangeListener false
                } else {
                    // used to tell if data in Chart/TranList Fragments should be updated
                    sharedPref[Constants.KEY_CHART_CHANGE] = true
                    sharedPref[Constants.KEY_TRAN_LIST_CHANGE] = true
                    return@OnPreferenceChangeListener true
                }
            }

        // Date Format
        dfPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue: Any ->
                if (newValue == dfPref.value) {
                    return@OnPreferenceChangeListener false
                } else {
                    // used to tell if data in Chart/TranList Fragments should be updated
                    sharedPref[Constants.KEY_CHART_CHANGE] = true
                    sharedPref[Constants.KEY_TRAN_LIST_CHANGE] = true
                    return@OnPreferenceChangeListener true
                }
            }

        // Language selection only works on API26 and higher
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            lgPref.isVisible = false
        } else {
            // Language
            lgPref.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, languageCode: Any ->
                    // checks if a different language was selected
                    if (lgPref.value != languageCode.toString()) {
                        // saving into SharedPreferences
                        sharedPref[Constants.KEY_LANGUAGE_CHANGED] = true
                        sharedPref[Constants.KEY_MANUAL_LANGUAGE] = true
                        // destroys then restarts Activity in order to have updated language
                        requireActivity().recreate()
                    }
                    return@OnPreferenceChangeListener true
                }
        }
    }

    /**
     *  Launches AlertDialog asking user if they want to allow/disallow the use of decimal places.
     *  [warning] is true if user is turning of preference, showing an addition message.
     */
    private fun allowDecimalDialog(warning: Boolean) {

        val message: String = if (warning) {
            getString(R.string.alert_dialog_decimal_place_warning)
        } else {
            ""
        }
        val posFun = DialogInterface.OnClickListener { _, _ ->
            dpPref.isChecked = !dpPref.isChecked
            // used to tell if data in Chart/TranList Fragments should be updated
            sharedPref[Constants.KEY_CHART_CHANGE] = true
            sharedPref[Constants.KEY_TRAN_LIST_CHANGE] = true
        }
        AlertDialogCreator.alertDialog(
            requireContext(),
            getString(R.string.alert_dialog_are_you_sure), message,
            getString(R.string.alert_dialog_switch), posFun,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing
        )
    }

    /**
     *  AlertDialog asking if the user wants to switch the thousands and decimal symbols
     */
    private fun switchSymbolDialog() {

        val posFun = DialogInterface.OnClickListener { _, _ ->
            val newDecimal: String = tsPref.value
            val newThousands: String = dsPref.value
            tsPref.value = newThousands
            dsPref.value = newDecimal
            sharedPref[Constants.KEY_CHART_CHANGE] = true
            sharedPref[Constants.KEY_TRAN_LIST_CHANGE] = true
        }

        AlertDialogCreator.alertDialog(
            requireContext(),
            getString(R.string.alert_dialog_duplicate_symbols),
            getString(R.string.alert_dialog_duplicate_symbols_warning),
            getString(R.string.alert_dialog_switch), posFun,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing
        )
    }
}