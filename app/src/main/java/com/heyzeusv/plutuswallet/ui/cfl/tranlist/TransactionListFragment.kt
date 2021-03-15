package com.heyzeusv.plutuswallet.ui.cfl.tranlist

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.TransactionInfo
import com.heyzeusv.plutuswallet.databinding.FragmentTransactionListBinding
import com.heyzeusv.plutuswallet.ui.base.BaseFragment
import com.heyzeusv.plutuswallet.ui.cfl.CFLFragmentDirections
import com.heyzeusv.plutuswallet.util.AlertDialogCreator
import com.heyzeusv.plutuswallet.util.EventObserver
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.PreferenceHelper.set
import com.heyzeusv.plutuswallet.util.SettingsUtils
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel
import kotlinx.coroutines.launch

/**
 *  Will show list of Transactions depending on filters applied.
 */
class TransactionListFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentTransactionListBinding

    // provides instance of TransactionListViewModel
    private val listVM: TransactionListViewModel by viewModels()

    // shared ViewModels
    private val cflVM: CFLViewModel by activityViewModels()

    // RecyclerView Adapter/LayoutManager
    private lateinit var tranListAdapter: TranListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        listVM.setVals = setVals
        tranListAdapter = TranListAdapter(listVM)

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
        binding.tranlistRv.adapter = tranListAdapter
        binding.tranlistRv.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        binding.tranlistRv.layoutManager = layoutManager

        return binding.root
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cflVM.tInfoLiveData.observe(viewLifecycleOwner, { tInfo: TransactionInfo ->
            listVM.ivtList = listVM.filteredTransactionList(
                tInfo.account, tInfo.category, tInfo.date,
                tInfo.type, tInfo.accountNames, tInfo.categoryNames, tInfo.start, tInfo.end
            )

            listVM.ivtList.observe(viewLifecycleOwner, { transactions: List<ItemViewTransaction> ->
                // update adapter with new list to check for any changes
                // and waits to be fully updated before running Runnable
                tranListAdapter.submitList(transactions) {
                    if (cflVM.filterChanged) {
                        binding.tranlistRv.smoothScrollToPosition(transactions.size)
                        cflVM.filterChanged = false
                    }
                }
                // scrolls to saved position
                binding.tranlistRv.scrollToPosition(listVM.rvPosition)
                // will display empty string
                listVM.ivtEmpty.value = transactions.isEmpty()
            })
        })

        listVM.openTranEvent.observe(viewLifecycleOwner, EventObserver { tranId: Int ->
            // the position that the user clicked on
            listVM.rvPosition = layoutManager.findLastCompletelyVisibleItemPosition()
            // creates action with parameters
            val action: NavDirections =
                CFLFragmentDirections.actionTransaction(tranId, false)
            // retrieves correct controller to send action to
            Navigation
                .findNavController(requireActivity(), R.id.fragment_container)
                .navigate(action)

        })

        listVM.deleteTranEvent.observe(
            viewLifecycleOwner,
            EventObserver { ivt: ItemViewTransaction ->
                val posFun = DialogInterface.OnClickListener { _, _ ->
                    launch {
                        listVM.deleteTranPosFun(ivt)
                    }
                }

                AlertDialogCreator.alertDialog(
                    requireContext(),
                    requireContext().getString(R.string.alert_dialog_delete_transaction),
                    requireContext().getString(R.string.alert_dialog_delete_warning, ivt.title),
                    requireContext().getString(R.string.alert_dialog_yes), posFun,
                    requireContext().getString(R.string.alert_dialog_no),
                    AlertDialogCreator.doNothing
                )
                // the position that the user clicked on
                listVM.rvPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            })

        listVM.initializeTables()
    }

    override fun onResume() {
        super.onResume()

        // checks if there has been a change in settings, updates changes, and updates list
        if (sharedPref[Key.KEY_TRAN_LIST_CHANGED, false]) {
            setVals = SettingsUtils.prepareSettingValues(sharedPref)
            listVM.setVals = setVals
            tranListAdapter.notifyDataSetChanged()
            sharedPref[Key.KEY_TRAN_LIST_CHANGED] = false
        }
        listVM.futureTransactions()
    }
}