package com.heyzeusv.financeapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.heyzeusv.financeapplication.utilities.BlankFragment
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), TransactionListFragment.Callbacks, FilterFragment.Callbacks {

    private lateinit var fab : FloatingActionButton

    private var fabX = 0
    private var fabY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab = findViewById(R.id.activity_fab)

        // FragmentManager adds fragments to an activity
        val currentFragment : Fragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_transaction_list_container)

        // would not be null if activity is destroyed and recreated
        // b/c FragmentManager saves list of frags
        if (currentFragment == null) {

            val transactionListFragment : TransactionListFragment = TransactionListFragment.newInstance()
            val filterFragment          : FilterFragment          = FilterFragment         .newInstance()
            val graphFragment           : GraphFragment           = GraphFragment          .newInstance()

            // Create a new fragment transaction, add fragments,
            // and then commit it
            supportFragmentManager
                .beginTransaction()
                // container view ID (where fragment's view should appear)
                // fragment to be added
                .add(R.id.fragment_transaction_list_container, transactionListFragment)
                .add(R.id.fragment_filter_container          , filterFragment)
                .add(R.id.fragment_graph_container           , graphFragment)
                .commit()
        }

    }

    // replaces TransactionListFragment with new TransactionListFragment with filters applied
    override fun onFilterApplied(category : Boolean, date : Boolean, categoryName : String, start : Date, end : Date) {

        Log.d(TAG, "onFilterApplied: Category: $category, Date: $date")

        val filteredTransactionListFragment : TransactionListFragment =
            TransactionListFragment.newInstance(category, date, categoryName, start, end)

        // Create a new fragment transaction, add fragments,
        // and then commit it
        supportFragmentManager
            .beginTransaction()
            // replace fragment hosted at location with new fragment provided
            // will add fragment even if there is none
            .replace(R.id.fragment_transaction_list_container, filteredTransactionListFragment)
            // pressing back button will go back to previous fragment (if any)
            .addToBackStack(null)
            .commit()
    }

    // replaces TransactionListFragment and FilterFragment with TransactionFragment selected
    override fun onTransactionSelected(transactionId: Int, fromFab : Boolean) {

        Log.d(TAG, "onTransactionSelected: $transactionId")
        getFabLocation()

        val transactionFragment : TransactionFragment = TransactionFragment.newInstance(transactionId, fabX, fabY, fromFab)
        val blankFragment                             = BlankFragment()
        val blankFragment2                            = BlankFragment()
        val blankFragment3                            = BlankFragment()

        // Create a new fragment transaction, add fragments,
        // and then commit it
        supportFragmentManager
            .beginTransaction()
            // replace fragment hosted at location with new fragment provided
            // will add fragment even if there is none
            .add(R.id.fragment_transaction_container         , transactionFragment)
            .replace(R.id.fragment_transaction_list_container, blankFragment)
            .replace(R.id.fragment_filter_container          , blankFragment2)
            .replace(R.id.fragment_graph_container           , blankFragment3)
            // pressing back button will go back to previous fragment (if any)
            .addToBackStack(null)
            .commit()
    }

    // gets location of FAB button in order to start animation from correct location
    private fun getFabLocation() {

        val fabLocationArray = IntArray(2)
        fab.getLocationOnScreen(fabLocationArray)
        fabX = fabLocationArray[0] + fab.width / 2
        fabY = fabLocationArray[1] - fab.height
        Log.d(TAG, "fabX: $fabX fabY: $fabY")
    }
}