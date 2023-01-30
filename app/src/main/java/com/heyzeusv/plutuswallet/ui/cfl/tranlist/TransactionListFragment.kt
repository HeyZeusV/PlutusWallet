package com.heyzeusv.plutuswallet.ui.cfl.tranlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.FragmentTransactionListBinding
import com.heyzeusv.plutuswallet.ui.base.BaseFragment
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.PreferenceHelper.set
import com.heyzeusv.plutuswallet.util.SettingsUtils
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel

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



    override fun onResume() {
        super.onResume()

        // checks if there has been a change in settings, updates changes, and updates list
        if (sharedPref[Key.KEY_TRAN_LIST_CHANGED, false]) {
            setVals = SettingsUtils.prepareSettingValues(sharedPref)
//            listVM.setVals = setVals
            sharedPref[Key.KEY_TRAN_LIST_CHANGED] = false
        }
        listVM.futureTransactions()
    }
}