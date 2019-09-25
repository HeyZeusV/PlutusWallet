package com.heyzeusv.financeapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FragmentManager adds fragments to an activity
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        // would not be null if activity is destroyed and recreated
        // b/c FragMana saves list of frags
        if (currentFragment == null) {

            val fragment = TransactionFragment()
            // Create a new fragment transaction, include one add operation in it,
            // and then commit it
            supportFragmentManager
                .beginTransaction()
                // container view ID (where fragment's view should appear and
                // unique ID for frag in FragMana's list, CrimeFragment
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
