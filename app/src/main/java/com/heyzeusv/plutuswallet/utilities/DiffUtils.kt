package com.heyzeusv.plutuswallet.utilities

import androidx.recyclerview.widget.DiffUtil
import com.heyzeusv.plutuswallet.database.entities.Category

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