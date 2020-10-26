package com.heyzeusv.plutuswallet.utilities.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.databinding.ItemViewAccountBinding
import com.heyzeusv.plutuswallet.viewmodels.AccountViewModel

/**
 *  Adapter for Transaction list.
 *  Has a reference to [AccountViewModel] to send actions back to it.
 */
class AccountAdapter(private val accountVM: AccountViewModel) :
    ListAdapter<Account, AccountAdapter.AccountHolder>(AccountDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder {

        val accountBinding: ItemViewAccountBinding = ItemViewAccountBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AccountHolder(accountBinding, accountVM)
    }

    // populates given holder with Account from the given position in list
    override fun onBindViewHolder(holder: AccountHolder, position: Int) {

        val account: Account = getItem(position)
        holder.bind(account)
    }

    class AccountHolder(
        private var binding: ItemViewAccountBinding,
        private var accountVM: AccountViewModel
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("StringFormatInvalid")
        fun bind(account: Account) {

            binding.account = account
            binding.accountVM = accountVM
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