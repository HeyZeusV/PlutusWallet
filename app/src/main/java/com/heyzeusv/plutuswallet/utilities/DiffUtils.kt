package com.heyzeusv.plutuswallet.utilities

import androidx.recyclerview.widget.DiffUtil
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.ItemViewChart
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction

/**
 *  Utility classes used by ListAdapters that allows it to update its list
 *  more efficiently and quickly.
 */
class AccountDiffUtil : DiffUtil.ItemCallback<Account>() {

    override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {

        return oldItem == newItem
    }
}

class CategoryDiffUtil : DiffUtil.ItemCallback<Category>() {

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {

        return oldItem.id == newItem.id
    }

    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {

        return oldItem == newItem
    }
}

class CatListDiffUtil : DiffUtil.ItemCallback<List<Category>>() {

    override fun areContentsTheSame(oldItem: List<Category>, newItem: List<Category>): Boolean {

        return oldItem[0].type == newItem[0].type
    }

    override fun areItemsTheSame(oldItem: List<Category>, newItem: List<Category>): Boolean {

        return oldItem == newItem
    }
}

class ChartDiffUtil : DiffUtil.ItemCallback<ItemViewChart>() {

    override fun areItemsTheSame(oldItem: ItemViewChart, newItem: ItemViewChart): Boolean {

        return oldItem.fType == newItem.fType
    }

    override fun areContentsTheSame(oldItem: ItemViewChart, newItem: ItemViewChart): Boolean {

        return oldItem == newItem
    }
}

class TranListDiffUtil : DiffUtil.ItemCallback<ItemViewTransaction>() {

    override fun areItemsTheSame(
        oldItem: ItemViewTransaction, newItem: ItemViewTransaction
    ): Boolean {

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ItemViewTransaction, newItem: ItemViewTransaction
    ): Boolean {

        return oldItem == newItem
    }
}