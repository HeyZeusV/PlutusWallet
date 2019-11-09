package com.heyzeusv.financeapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

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
        }
    }
}