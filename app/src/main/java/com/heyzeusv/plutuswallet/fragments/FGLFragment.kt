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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filterFragment          : FilterFragment          = FilterFragment.newInstance()
        val graphFragment           : GraphFragment           = GraphFragment.newInstance()
        val transactionListFragment : TransactionListFragment = TransactionListFragment.newInstance()

        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_filter_container          , filterFragment         )
            .replace(R.id.fragment_graph_container           , graphFragment          )
            .replace(R.id.fragment_transaction_list_container, transactionListFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        return inflater.inflate(R.layout.fragment_fgl, container, false)
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