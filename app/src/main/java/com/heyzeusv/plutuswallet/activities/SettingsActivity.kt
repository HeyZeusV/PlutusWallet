package com.heyzeusv.plutuswallet.activities

import android.os.Bundle
import android.view.MenuItem
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.fragments.SettingsFragment

/**
 *  Activity that starts SettingsFragment
 */
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Create a new fragment transaction, adds fragment, and then commit it
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_set_container, SettingsFragment())
            .commit()

        // displays back button on ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return if (item!!.itemId == android.R.id.home) {
            // returns user to previous activity if they select back arrow
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}