package com.heyzeusv.plutuswallet.ui.cfl.tranlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.databinding.ItemViewTransactionBinding
import com.heyzeusv.plutuswallet.util.DateUtils

/**
 *  Adapter for Transaction list.
 *  Has a reference to [TransactionListViewModel] to send actions back to it.
 */
class TranListAdapter(private val listVM: TransactionListViewModel) :
    ListAdapter<ItemViewTransaction, TranListAdapter.TranListHolder>(TranListDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranListHolder {

        val itemViewBinding: ItemViewTransactionBinding = ItemViewTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TranListHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: TranListHolder, position: Int) {

        val ivt: ItemViewTransaction = getItem(position)
        holder.bind(ivt, listVM)
    }

    class TranListHolder(val binding: ItemViewTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ivt: ItemViewTransaction, listVM: TransactionListViewModel) {

            binding.ivt = ivt
            binding.listVM = listVM
            binding.executePendingBindings()

            // formats total correctly
            binding.ivtTotal.text = when {
                // currency symbol on left with decimal places
                listVM.setVals.decimalPlaces && listVM.setVals.symbolSide ->
                    itemView.context.getString(
                        R.string.total_number_symbol,
                        listVM.setVals.currencySymbol,
                        listVM.setVals.decimalFormatter.format(ivt.total)
                    )
                // currency symbol on right with decimal places
                listVM.setVals.decimalPlaces ->
                    itemView.context.getString(
                        R.string.total_number_symbol,
                        listVM.setVals.decimalFormatter.format(ivt.total),
                        listVM.setVals.currencySymbol
                    )
                // currency symbol on left without decimal places
                listVM.setVals.symbolSide ->
                    itemView.context.getString(
                        R.string.total_number_symbol,
                        listVM.setVals.currencySymbol,
                        listVM.setVals.integerFormatter.format(ivt.total)
                    )
                // currency symbol on right without decimal places
                else ->
                    itemView.context.getString(
                        R.string.total_number_symbol,
                        listVM.setVals.integerFormatter.format(ivt.total),
                        listVM.setVals.currencySymbol
                    )
            }
            // formats date correctly
            binding.ivtDate.text = DateUtils.formatString(ivt.date, listVM.setVals.dateFormat)
        }
    }
}

/**
 *  Callback for calculating the diff between two non-null items in a list.
 */
class TranListDiffUtil : DiffUtil.ItemCallback<ItemViewTransaction>() {

    override fun areItemsTheSame(
        oldItem: ItemViewTransaction,
        newItem: ItemViewTransaction
    ): Boolean {

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ItemViewTransaction,
        newItem: ItemViewTransaction
    ): Boolean {

        return oldItem == newItem
    }
}