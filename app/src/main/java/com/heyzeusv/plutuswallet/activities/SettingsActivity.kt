package com.heyzeusv.plutuswallet.activities

import android.os.Bundle
import android.view.MenuItem
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.fragments.SettingsFragment

/**
 *  Activity that starts SettingsFragment
 */
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Create a new fragment transaction, adds fragment, and then commit it
        supportFragmentManager
            .beginTransaction()
            // container view ID (where fragment's view should appear)
            // fragment to be added
            .replace(R.id.fragment_set_container, SettingsFragment())
            .commit()

        // displays back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item : MenuItem?) : Boolean {

        when (item!!.itemId) {

            // returns user to previous activity if they select back arrow
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}