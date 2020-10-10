package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo
import com.heyzeusv.plutuswallet.databinding.FragmentTransactionListBinding
import com.heyzeusv.plutuswallet.databinding.ItemViewTransactionBinding
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.utilities.Constants
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set
import com.heyzeusv.plutuswallet.utilities.TranListDiffUtil
import com.heyzeusv.plutuswallet.utilities.SettingsUtils
import com.heyzeusv.plutuswallet.viewmodels.CFLViewModel
import com.heyzeusv.plutuswallet.viewmodels.TransactionListViewModel
import kotlinx.coroutines.launch

/**
 *  Will show list of Transactions depending on filters applied.
 */
class TransactionListFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentTransactionListBinding

    // provides instance of ViewModel
    private val listVM: TransactionListViewModel by lazy {
        ViewModelProvider(this).get(TransactionListViewModel::class.java)
    }

    // shared ViewModels
    private lateinit var cflVM: CFLViewModel

    // RecyclerView Adapter/LayoutManager
    private val tranListAdapter = TranListAdapter()
    lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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

        // this ensures that this is same CFLViewModel as Filter/ChartFragment use
        cflVM = requireActivity().let { ViewModelProvider(it).get(CFLViewModel::class.java) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cflVM.tInfoLiveData.observe(viewLifecycleOwner, { tInfo: TransactionInfo ->
            listVM.ivtList = listVM.filteredTransactionList(
                tInfo.account, tInfo.category, tInfo.date,
                tInfo.type, tInfo.accountName, tInfo.categoryName, tInfo.start, tInfo.end
            )

            listVM.ivtList.observe(viewLifecycleOwner, { transactions: List<ItemViewTransaction> ->
                // scrolls to saved position
                binding.tranlistRv.scrollToPosition(listVM.rvPosition)
                // will display empty string
                listVM.ivtEmpty.value = transactions.isEmpty()
                // update adapter with new list to check for any changes
                // and waits to be fully updated before running Runnable
                tranListAdapter.submitList(transactions) {
                    if (cflVM.filterChanged) {
                        binding.tranlistRv.smoothScrollToPosition(transactions.size)
                        cflVM.filterChanged = false
                    }
                }
            })
        })

        listVM.initializeTables()
    }

    override fun onResume() {
        super.onResume()

        // checks if there has been a change in settings, updates changes, and updates list
        if (sharedPref[Constants.KEY_TRAN_LIST_CHANGE, false]!!) {
            setVals = SettingsUtils.prepareSettingValues(sharedPref)
            tranListAdapter.notifyDataSetChanged()
            sharedPref[Constants.KEY_TRAN_LIST_CHANGE] = false
        }
        listVM.futureTransactions()
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     */
    inner class TranListAdapter :
        ListAdapter<ItemViewTransaction, TranListHolder>(TranListDiffUtil()) {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranListHolder {

            val itemViewBinding: ItemViewTransactionBinding = ItemViewTransactionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return TranListHolder(itemViewBinding)
        }

        // populates given holder with Transaction from the given position in list
        override fun onBindViewHolder(holder: TranListHolder, position: Int) {

            val ivt: ItemViewTransaction = getItem(position)
            holder.bind(ivt)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view using [binding] as its layout.
     */
    inner class TranListHolder(private var binding: ItemViewTransactionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        // sets the views with Transaction data
        fun bind(ivt: ItemViewTransaction) {

            binding.ivt = ivt
            binding.setVals = setVals
            binding.executePendingBindings()

            // formats the Total correctly
            when {
                setVals.decimalPlaces && setVals.symbolSide -> binding.ivtTotal.text =
                    getString(
                        R.string.total_number_symbol,
                        setVals.currencySymbol, setVals.decimalFormatter.format(ivt.total)
                    )
                setVals.decimalPlaces -> binding.ivtTotal.text =
                    getString(
                        R.string.total_number_symbol,
                        setVals.decimalFormatter.format(ivt.total), setVals.currencySymbol
                    )
                setVals.symbolSide -> binding.ivtTotal.text =
                    getString(
                        R.string.total_number_symbol,
                        setVals.currencySymbol, setVals.integerFormatter.format(ivt.total)
                    )
                else -> binding.ivtTotal.text =
                    getString(
                        R.string.total_number_symbol,
                        setVals.integerFormatter.format(ivt.total), setVals.currencySymbol
                    )
            }
        }

        override fun onClick(v: View?) {

            // the position that the user clicked on
            listVM.rvPosition = layoutManager.findFirstCompletelyVisibleItemPosition()

            // creates action with parameters
            val action: NavDirections =
                CFLFragmentDirections.actionTransaction(binding.ivt!!.id, false)
            // retrieves correct controller to send action to
            Navigation
                .findNavController(requireActivity(), R.id.fragment_container)
                .navigate(action)
        }

        /**
         *  Shows AlertDialog asking user if they want to delete Transaction.
         */
        @SuppressLint("StringFormatInvalid")
        override fun onLongClick(v: View?): Boolean {

            val posFun = DialogInterface.OnClickListener { _, _ ->
                launch {
                    listVM.deleteTransaction(listVM.getTransactionAsync(binding.ivt!!.id).await())
                    listVM.rvPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                }
            }

            AlertDialogCreator.alertDialog(
                context!!,
                getString(R.string.alert_dialog_delete_transaction),
                getString(R.string.alert_dialog_delete_warning, binding.ivt!!.title),
                getString(R.string.alert_dialog_yes), posFun,
                getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing
            )

            return true
        }
    }
}