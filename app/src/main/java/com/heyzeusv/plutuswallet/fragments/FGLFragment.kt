package com.heyzeusv.plutuswallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.heyzeusv.plutuswallet.R

/**
 *  This Fragment will be used to nest Filter/Graph/List Fragments.
 */
class FGLFragment : BaseFragment() {

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        return inflater.inflate(R.layout.fragment_fgl, container, false)
    }

    override fun onStart() {
        super.onStart()

        val filterFragment          : FilterFragment          = FilterFragment.newInstance()
        val graphFragment           : GraphFragment           = GraphFragment.newInstance()
        val transactionListFragment : TransactionListFragment = TransactionListFragment.newInstance()

        childFragmentManager.beginTransaction()
            .add(R.id.fragment_filter_container          , filterFragment         )
            .add(R.id.fragment_graph_container           , graphFragment          )
            .add(R.id.fragment_transaction_list_container, transactionListFragment)
            .commit()
    }

    companion object {

        /**
         *  Initializes instance of FGLFragment.
         */
        fun newInstance() : FGLFragment {

            return FGLFragment()
        }
    }
}