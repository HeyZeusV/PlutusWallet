package com.heyzeusv.plutuswallet.utilities

import androidx.recyclerview.widget.DiffUtil
import com.heyzeusv.plutuswallet.database.entities.ItemViewChart
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction

/**
 *  Utility classes used by ListAdapters that allows it to update its list
 *  more efficiently and quickly.
 */
class TranListDiffUtil : DiffUtil.ItemCallback<ItemViewTransaction>() {

    override fun areItemsTheSame(
        oldItem : ItemViewTransaction, newItem : ItemViewTransaction) : Boolean {

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem : ItemViewTransaction, newItem : ItemViewTransaction ) : Boolean {

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