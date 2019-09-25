package com.heyzeusv.financeapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

private const val TAG = "TransactionListFragment"

class TransactionListFragment : Fragment() {

    // provides instance of ViewModel
    private val transactionListViewModel : TransactionListViewModel by lazy {
        ViewModelProviders.of(this).get(TransactionListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // logs number of transactions
        Log.d(TAG, "Total crimes: ${transactionListViewModel.transactions.size}")
    }

    companion object {

        // can be called by activities to get instance of fragment
        fun newInstance() : TransactionListFragment {

            return TransactionListFragment()
        }
    }
}