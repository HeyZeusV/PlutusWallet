package com.heyzeusv.plutuswallet.ui.cfl.tranlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.FragmentTransactionListBinding
import com.heyzeusv.plutuswallet.ui.base.BaseFragment

/**
 *  Will show list of Transactions depending on filters applied.
 */
class TransactionListFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentTransactionListBinding

    // provides instance of TransactionListViewModel
    private val listVM: TransactionListViewModel by viewModels()

    // RecyclerView Adapter/LayoutManager
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        listVM.setVals = setVals

        // setting up LayoutManager
        layoutManager = LinearLayoutManager(context)
        layoutManager.apply {
            reverseLayout = true
            stackFromEnd = true
        }

        // setting up DataBinding
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_transaction_list, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.listVM = listVM
        binding.tranlistRv.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        binding.tranlistRv.layoutManager = layoutManager

        return binding.root
    }
}