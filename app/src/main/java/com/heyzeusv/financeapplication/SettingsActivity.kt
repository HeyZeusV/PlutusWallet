package com.heyzeusv.financeapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "SettingsFragment"

/**
 *  Activity that starts SettingsFragment
 */
class SettingsActivity : AppCompatActivity() {

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

            val dpPreference : SwitchPreference? = findPreference("key_decimal_places")
            dpPreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->

                // asks the user if they do want to switch decimalPlacesPreference
                if (dpPreference.isChecked) {

                    // initialize and set up Builder
                    val adBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                        .setTitle(getString(R.string.alert_dialog_are_you_sure))
                        .setMessage(getString(R.string.alert_dialog_decimal_place_warning))
                        .setPositiveButton(getString(R.string.alert_dialog_switch)) { _, _ ->

                        dpPreference.isChecked = false
                    }
                        .setNegativeButton(getString(R.string.alert_dialog_cancel)) { _, _ -> }
                    // make AlertDialog using Builder
                    val decimalAlertDialog : AlertDialog = adBuilder.create()
                    // display AlertDialog
                    decimalAlertDialog.show()
                } else {

                    // initialize and set up Builder
                    val adBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.alert_dialog_are_you_sure)
                        .setPositiveButton(getString(R.string.alert_dialog_switch)) { _, _ ->

                        dpPreference.isChecked = true
                    }
                        .setNegativeButton(getString(R.string.alert_dialog_cancel)) { _, _ -> }
                    // make AlertDialog using Builder
                    val decimalAlertDialog : AlertDialog = adBuilder.create()
                    // display AlertDialog
                    decimalAlertDialog.show()
                }
                return@OnPreferenceChangeListener false
            }
        }
    }
}