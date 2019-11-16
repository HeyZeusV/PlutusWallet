package com.heyzeusv.financeapplication

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.heyzeusv.financeapplication.utilities.BaseActivity

private const val TAG                  = "SettingsFragment"
private const val KEY_LANGUAGE_CHANGED = "key_language_changed"
private const val KEY_MANUAL_LANGUAGE  = "key_manual_language"

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
            val sp     : SharedPreferences        = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor : SharedPreferences.Editor = sp.edit()

            // Preferences that need Listeners
            val dpPreference : SwitchPreference? = findPreference("key_decimal_places")
            val lgPreference : ListPreference?   = findPreference("key_language")

            // used to tell if a new language is set or if the same is re-selected
            val languageSet : String = lgPreference!!.value

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

            // only works on API26 and higher
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                lgPreference.isVisible = false
            } else {

                lgPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, newValue ->

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