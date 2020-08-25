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
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set

/**
 *  Shows different options users can change to better their experience
 */
class SettingsFragment : PreferenceFragmentCompat() {

    // SharedPreferences
    private lateinit var sharedPreferences : SharedPreferences

    // Preferences
    private lateinit var csPreference : ListPreference
    private lateinit var ssPreference : SwitchPreference
    private lateinit var tsPreference : ListPreference
    private lateinit var dpPreference : SwitchPreference
    private lateinit var dsPreference : ListPreference
    private lateinit var dfPreference : ListPreference
    private lateinit var lgPreference : ListPreference

    @SuppressLint("CommitPrefEdits")
    override fun onCreatePreferences(savedInstanceState : Bundle?, rootKey : String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // initialize SharedPreferences
        sharedPreferences = PreferenceHelper.sharedPrefs(requireActivity())

        // initialize Preferences
        csPreference = findPreference(Constants.KEY_CURRENCY_SYMBOL )!!
        ssPreference = findPreference(Constants.KEY_SYMBOL_SIDE     )!!
        tsPreference = findPreference(Constants.KEY_THOUSANDS_SYMBOL)!!
        dpPreference = findPreference(Constants.KEY_DECIMAL_PLACES  )!!
        dsPreference = findPreference(Constants.KEY_DECIMAL_SYMBOL  )!!
        dfPreference = findPreference(Constants.KEY_DATE_FORMAT     )!!
        lgPreference = findPreference(Constants.KEY_LANGUAGE        )!!
    }

    override fun onStart() {
        super.onStart()

        // Currency Symbol
        csPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _ : Preference, newValue : Any ->

             when (newValue) {

                 csPreference.value -> return@OnPreferenceChangeListener false
                 else -> {

                     // used to tell if data in Chart/TranList Fragments should be updated
                     sharedPreferences[Constants.KEY_CHART_CHANGE    ] = true
                     sharedPreferences[Constants.KEY_TRAN_LIST_CHANGE] = true
                     return@OnPreferenceChangeListener true
                 }
             }
        }

        // Symbol Side
        ssPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _ : Preference, _ : Any ->

            // used to tell if data in Chart/TranList Fragments should be updated
            sharedPreferences[Constants.KEY_CHART_CHANGE    ] = true
            sharedPreferences[Constants.KEY_TRAN_LIST_CHANGE] = true
            return@OnPreferenceChangeListener true
        }

        // Thousands Symbol
        tsPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _ : Preference, newValue : Any ->

            when (newValue) {

                // if newValue == decimal then launch AlertDialog
                dsPreference.value -> {

                    switchSymbolDialog()
                    return@OnPreferenceChangeListener false
                }
                else -> {

                    // used to tell if data in Chart/TranList Fragments should be updated
                    sharedPreferences[Constants.KEY_CHART_CHANGE    ] = true
                    sharedPreferences[Constants.KEY_TRAN_LIST_CHANGE] = true
                    return@OnPreferenceChangeListener true
                }
            }

        }

        // Decimal Places
        dpPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _ : Preference, _ : Any ->

            // asks the user if they do want to switch decimalPlacesPreference
            allowDecimalDialog(dpPreference.isChecked)
            return@OnPreferenceChangeListener false
        }

        // Decimal Symbol
        dsPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _ : Preference, newValue : Any ->

            when (newValue) {

                // if newValue == thousands then launch AlertDialog
                tsPreference.value -> {

                    switchSymbolDialog()
                    return@OnPreferenceChangeListener false
                }
                else -> {

                    // used to tell if data in Chart/TranList Fragments should be updated
                    sharedPreferences[Constants.KEY_CHART_CHANGE    ] = true
                    sharedPreferences[Constants.KEY_TRAN_LIST_CHANGE] = true
                    return@OnPreferenceChangeListener true
                }
            }
        }

        // Date Format
        dfPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _ : Preference, newValue : Any ->

            when (newValue) {

                dfPreference.value -> return@OnPreferenceChangeListener false
                else -> {

                    // used to tell if data in Chart/TranList Fragments should be updated
                    sharedPreferences[Constants.KEY_CHART_CHANGE    ] = true
                    sharedPreferences[Constants.KEY_TRAN_LIST_CHANGE] = true
                    return@OnPreferenceChangeListener true
                }
            }
        }

        // only works on API26 and higher
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            lgPreference.isVisible = false
        } else {

            // Language
            lgPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _ : Preference, languageCode : Any ->

                // checks if a different language was selected
                if (lgPreference.value != languageCode.toString()) {

                    // saving into SharedPreferences
                    sharedPreferences[Constants.KEY_LANGUAGE_CHANGED] = true
                    sharedPreferences[Constants.KEY_MANUAL_LANGUAGE ] = true

                    // destroys then restarts Activity in order to have updated language
                    requireActivity().recreate()
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
            // used to tell if data in Chart/TranList Fragments should be updated
            sharedPreferences[Constants.KEY_CHART_CHANGE    ] = true
            sharedPreferences[Constants.KEY_TRAN_LIST_CHANGE] = true
        }
        AlertDialogCreator.alertDialog(requireContext(),
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

            val newDecimal   : String = tsPreference.value
            val newThousands : String = dsPreference.value
            tsPreference.value = newThousands
            dsPreference.value = newDecimal
            sharedPreferences[Constants.KEY_CHART_CHANGE    ] = true
            sharedPreferences[Constants.KEY_TRAN_LIST_CHANGE] = true
        }

        AlertDialogCreator.alertDialog(requireContext(),
            getString(R.string.alert_dialog_duplicate_symbols),
            getString(R.string.alert_dialog_duplicate_symbols_warning),
            getString(R.string.alert_dialog_switch), posFun,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing)
    }
}