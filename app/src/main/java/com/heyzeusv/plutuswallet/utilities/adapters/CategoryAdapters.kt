package com.heyzeusv.plutuswallet.utilities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.databinding.ItemViewCategoryBinding
import com.heyzeusv.plutuswallet.databinding.ItemViewCatlistBinding
import com.heyzeusv.plutuswallet.viewmodels.CategoryViewModel

/**
 *  Adapter for the 2 lists of Category types shown by ViewPager.
 *  Has a reference to [CategoryViewModel] to receive data from.
 */
class CategoryListAdapter(private val catVM: CategoryViewModel) :
    ListAdapter<List<Category>, CategoryListAdapter.CategoryListHolder>(CatListDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListHolder {

        val itemViewBinding: ItemViewCatlistBinding = ItemViewCatlistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryListHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: CategoryListHolder, position: Int) {

        holder.bind(position, catVM)
    }

    /**
     *  ViewHolder stores a reference to an item's view using [binding] as its layout.
     */
    class CategoryListHolder(private var binding: ItemViewCatlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(type: Int, catVM: CategoryViewModel) {

            binding.type = type
            binding.executePendingBindings()
            // sets up adapter depending on type
            if (type == 0) {
                binding.ivclRv.adapter = catVM.expenseAdapter
            } else {
                binding.ivclRv.adapter = catVM.incomeAdapter
            }
            binding.ivclRv.addItemDecoration(
                DividerItemDecoration(binding.root.context, DividerItemDecoration.VERTICAL)
            )
            binding.ivclRv.layoutManager = LinearLayoutManager(binding.root.context)
        }
    }
}

/**
 *  Adapter for Category list.
 *  Has a reference to [CategoryViewModel] to send actions back to it.
 */
class CategoryAdapter(private val catVM: CategoryViewModel) :
    ListAdapter<Category, CategoryAdapter.CategoryHolder>(CategoryDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {

        val itemViewBinding: ItemViewCategoryBinding = ItemViewCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {

        val category: Category = getItem(position)
        holder.bind(category, catVM)
    }

    /**
     *  ViewHolder stores a reference to an item's view using [binding] as its layout.
     */
    class CategoryHolder(private var binding: ItemViewCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, catVM: CategoryViewModel) {

            binding.category = category
            binding.type = if (category.type == "Expense") 0 else 1
            binding.catVM = catVM
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