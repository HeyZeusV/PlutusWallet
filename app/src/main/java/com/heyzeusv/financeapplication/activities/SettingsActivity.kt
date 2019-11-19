package com.heyzeusv.financeapplication.activities

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.heyzeusv.financeapplication.R

private const val TAG = "SettingsFragment"

/**
 *  Activity that starts SettingsFragment
 */
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        // Create a new fragment transaction, adds fragment, and then commit it
        supportFragmentManager
            .beginTransaction()
            // container view ID (where fragment's view should appear)
            // fragment to be added
            .replace(R.id.settings, SettingsFragment())
            .commit()

        // displays back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item : MenuItem?) : Boolean {

        when (item?.itemId) {

            // returns user to previous activity if they select back arrow
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState : Bundle?, rootKey : String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // SharedPreferences
            val sharedPreferences : SharedPreferences        = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor            : SharedPreferences.Editor = sharedPreferences.edit()

            // Preferences
            val tsPreference : ListPreference?   = findPreference("key_thousands_symbol")
            val dpPreference : SwitchPreference? = findPreference("key_decimal_places"  )
            val dsPreference : ListPreference?   = findPreference("key_decimal_symbol"  )
            val lgPreference : ListPreference?   = findPreference("key_language"        )

            // used to tell if a new language is set or if the same is re-selected
            val languageSet : String  = lgPreference!!.value
            val thousands   : String  = tsPreference!!.value
            val decimalOn   : Boolean = dpPreference!!.isChecked
            val decimal     : String  = dsPreference!!.value

//            tsPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _ : Preference, newValue : Any ->
//
//                when (newValue) {
//
//                    newValue != decimal -> return@OnPreferenceChangeListener true
//                    newValue == decimal && decimalOn -> {
//
//                        val alertDialogBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
//                            .setTitle("Duplicate Symbols")
//                            .setMessage("")
//                        return@OnPreferenceChangeListener false
//                    }
//                }
//
//            }

            dpPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _ : Preference, _ : Any ->

                // asks the user if they do want to switch decimalPlacesPreference
                if (dpPreference.isChecked) {

                    // initialize and set up Builder
                    val adBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                        .setTitle(getString(R.string.alert_dialog_are_you_sure))
                        .setMessage(getString(R.string.alert_dialog_decimal_place_warning))
                        .setPositiveButton(getString(R.string.alert_dialog_switch)) { _ : DialogInterface, _ : Int ->

                        dpPreference.isChecked = false
                    }
                        .setNegativeButton(getString(R.string.alert_dialog_cancel)) { _ : DialogInterface, _ : Int -> }
                    // make AlertDialog using Builder
                    val decimalAlertDialog : AlertDialog = adBuilder.create()
                    // display AlertDialog
                    decimalAlertDialog.show()
                } else {

                    // initialize and set up Builder
                    val adBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.alert_dialog_are_you_sure)
                        .setPositiveButton(getString(R.string.alert_dialog_switch)) { _ : DialogInterface, _ : Int ->

                        dpPreference.isChecked = true
                    }
                        .setNegativeButton(getString(R.string.alert_dialog_cancel)) { _ : DialogInterface, _ : Int -> }
                    // make AlertDialog using Builder
                    val decimalAlertDialog : AlertDialog = adBuilder.create()
                    // display AlertDialog
                    decimalAlertDialog.show()
                }
                return@OnPreferenceChangeListener false
            }

            // only works on API26 and higher
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                lgPreference.isVisible = false
            } else {

                lgPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_ : Preference, newValue : Any ->

                    // checks if a different language was selected
                    if (languageSet != newValue.toString()) {

                        // saving into SharedPreferences
                        editor.putBoolean(KEY_LANGUAGE_CHANGED, true)
                        editor.putBoolean(KEY_MANUAL_LANGUAGE,  true)
                        editor.apply()

                        // destroys then restarts Activity in order to have updated language
                        activity!!.recreate()
                    }
                    return@OnPreferenceChangeListener true
                }
            }
        }
    }
}