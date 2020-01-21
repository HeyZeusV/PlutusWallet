package com.heyzeusv.plutuswallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.heyzeusv.plutuswallet.R

/**
 *  This Fragment will be used to nest Chart/Filter/List Fragments.
 */
class CFLFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filterFragment          : FilterFragment          = FilterFragment.newInstance()
        val chartFragment           : ChartFragment           = ChartFragment.newInstance()
        val transactionListFragment : TransactionListFragment = TransactionListFragment.newInstance()

        // starts fragment transaction, replaces fragments, and then commits it
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_filter_container          , filterFragment         )
            .replace(R.id.fragment_chart_container           , chartFragment          )
            .replace(R.id.fragment_transaction_list_container, transactionListFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        return inflater.inflate(R.layout.fragment_cfl, container, false)
    }

    companion object {

        /**
         *  Initializes instance of CFLFragment.
         */
        fun newInstance() : CFLFragment {

            return CFLFragment()
        }
    }
}