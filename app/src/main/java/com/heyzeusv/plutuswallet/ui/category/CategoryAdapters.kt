package com.heyzeusv.plutuswallet.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.databinding.ItemViewCategoryBinding
import com.heyzeusv.plutuswallet.databinding.ItemViewCatlistBinding

/**
 *  Adapter for the 2 lists of Category types shown by ViewPager.
 *  Has a reference to [CategoryViewModel] to receive data from.
 */
class CategoryListAdapter :
    ListAdapter<List<Category>, CategoryListAdapter.CategoryListHolder>(CatListDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListHolder {

        val itemViewBinding: ItemViewCatlistBinding = ItemViewCatlistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryListHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: CategoryListHolder, position: Int) {

        holder.bind()
    }

    /**
     *  ViewHolder stores a reference to an item's view using [binding] as its layout.
     */
    class CategoryListHolder(private var binding: ItemViewCatlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {

            binding.executePendingBindings()
        }
    }
}

/**
 *  Adapter for Category list.
 *  Has a reference to [CategoryViewModel] to send actions back to it.
 */
class CategoryAdapter :
    ListAdapter<Category, CategoryAdapter.CategoryHolder>(CategoryDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {

        val itemViewBinding: ItemViewCategoryBinding = ItemViewCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {

        holder.bind()
    }

    /**
     *  ViewHolder stores a reference to an item's view using [binding] as its layout.
     */
    class CategoryHolder(private var binding: ItemViewCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.executePendingBindings()
        }
    }
}


/**
 *  Callback for calculating the diff between two non-null items in a list.
 */
class CatListDiffUtil : DiffUtil.ItemCallback<List<Category>>() {

    override fun areContentsTheSame(oldItem: List<Category>, newItem: List<Category>): Boolean {

        return oldItem[0].type == newItem[0].type
    }

    override fun areItemsTheSame(oldItem: List<Category>, newItem: List<Category>): Boolean {

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