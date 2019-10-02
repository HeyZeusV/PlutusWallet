package com.heyzeusv.financeapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), TransactionListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FragmentManager adds fragments to an activity
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        // would not be null if activity is destroyed and recreated
        // b/c FragmentManager saves list of frags
        if (currentFragment == null) {

            val fragment = TransactionListFragment.newInstance()
            // Create a new fragment transaction, include one add operation in it,
            // and then commit it
            supportFragmentManager
                .beginTransaction()
                // container view ID (where fragment's view should appear and
                // unique ID for frag in FragmentManager's list, CrimeFragment
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onTransactionSelected(transactionId: Int, fabX : Int, fabY : Int, fromFab : Boolean) {

        Log.d(TAG, "MainActivity.onTransactionSelected: $transactionId")

        val fragment = TransactionFragment.newInstance(transactionId, fabX, fabY, fromFab)
        supportFragmentManager
            .beginTransaction()
            // replace fragment hosted at location with new fragment provided
            // will add fragment even if there is none
            .replace(R.id.fragment_container, fragment)
            // pressing back button will go back to previous fragment (if any)
            .addToBackStack(null)
            .commit()
    }
}