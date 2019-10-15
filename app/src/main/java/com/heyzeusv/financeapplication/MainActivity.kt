package com.heyzeusv.financeapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.heyzeusv.financeapplication.utilities.BlankFragment

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), TransactionListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FragmentManager adds fragments to an activity
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_transaction_list_container)

        // would not be null if activity is destroyed and recreated
        // b/c FragmentManager saves list of frags
        if (currentFragment == null) {

            val transactionListFragment : TransactionListFragment = TransactionListFragment.newInstance()
            val filterFragment                                    = FilterFragment()
            // Create a new fragment transaction, add fragments,
            // and then commit it
            supportFragmentManager
                .beginTransaction()
                // container view ID (where fragment's view should appear)
                // fragment to be added
                .add(R.id.fragment_transaction_list_container, transactionListFragment)
                .add(R.id.fragment_filter_container, filterFragment)
                .commit()
        }
    }

    override fun onTransactionSelected(transactionId: Int, fabX : Int, fabY : Int, fromFab : Boolean) {

        Log.d(TAG, "MainActivity.onTransactionSelected: $transactionId")

        val transactionFragment = TransactionFragment.newInstance(transactionId, fabX, fabY, fromFab)
        val blankFragment = BlankFragment()
        val blankFragment2 = BlankFragment()
        supportFragmentManager
            .beginTransaction()
            // replace fragment hosted at location with new fragment provided
            // will add fragment even if there is none
            .add(R.id.fragment_transaction_container, transactionFragment)
            .replace(R.id.fragment_transaction_list_container, blankFragment)
            .replace(R.id.fragment_filter_container, blankFragment2)
            // pressing back button will go back to previous fragment (if any)
            .addToBackStack(null)
            .commit()
    }
}