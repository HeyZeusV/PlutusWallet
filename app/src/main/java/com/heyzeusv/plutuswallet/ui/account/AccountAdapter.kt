package com.heyzeusv.plutuswallet.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.databinding.ItemViewAccountBinding

/**
 *  Adapter for Transaction list.
 *  Has a reference to [AccountViewModel] to send actions back to it.
 */
class AccountAdapter() :
    ListAdapter<Account, AccountAdapter.AccountHolder>(AccountDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder {

        val accountBinding: ItemViewAccountBinding = ItemViewAccountBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AccountHolder(accountBinding)
    }

    override fun onBindViewHolder(holder: AccountHolder, position: Int) {

        holder.bind()
    }

    class AccountHolder(private var binding: ItemViewAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.executePendingBindings()
        }
    }
}

/**
 *  Callback for calculating the diff between two non-null items in a list.
 */
class AccountDiffUtil : DiffUtil.ItemCallback<Account>() {

    override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {

        return oldItem == newItem
    }
}