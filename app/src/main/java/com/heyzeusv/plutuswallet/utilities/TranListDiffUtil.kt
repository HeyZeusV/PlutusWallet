package com.heyzeusv.plutuswallet.utilities

import androidx.recyclerview.widget.DiffUtil
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction

/**
 *  Utility class used by TranListAdapter that allows it to update its list
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