package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.databinding.FragmentCategoryBinding
import com.heyzeusv.plutuswallet.databinding.ItemViewCategoryBinding
import com.heyzeusv.plutuswallet.databinding.ItemViewCatlistBinding
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.utilities.CatListDiffUtil
import com.heyzeusv.plutuswallet.utilities.CategoryDiffUtil
import com.heyzeusv.plutuswallet.viewmodels.CategoryViewModel
import kotlinx.coroutines.launch

/**
 *  Shows all Categories depending on type in database and allows users to either
 *  edit them or delete them.
 */
class CategoryFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentCategoryBinding

    // adapters used by ViewPager
    private val catListAdapter = CategoryListAdapter()

    // adapters used by RecyclerViews
    private val expenseAdapter = CategoryAdapter()
    private val incomeAdapter = CategoryAdapter()

    // instance of CategoryViewModel
    private val catVM: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.categoryVp.adapter = catListAdapter
        binding.categoryCi.setViewPager(binding.categoryVp)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        catVM.expenseCatsLD.observe(viewLifecycleOwner, {
            updateAdapters(it, expenseAdapter, "Expense", 0)
        })

        catVM.incomeCatsLD.observe(viewLifecycleOwner, {
            updateAdapters(it, incomeAdapter, "Income", 1)
        })

        binding.categoryVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                catVM.listShown = position
            }
        })

        // navigates user back to CFLFragment
        binding.accountTopBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        // handles menu selection
        binding.accountTopBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.category_new) {
                createDialog()
                true
            } else {
                false
            }
        }
    }

    /**
     *  Creates AlertDialog when user clicks New Category button in TopBar.
     */
    private fun createDialog() {

        // inflates view that holds EditText
        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
        // the EditText to be used
        val input: EditText = viewInflated.findViewById(R.id.dialog_input)

        val posListener = DialogInterface.OnClickListener { _, _ ->
            insertCategory(input.text.toString())
        }

        AlertDialogCreator.alertDialogInput(
            requireContext(),
            getString(R.string.category_create), viewInflated,
            getString(R.string.alert_dialog_save), posListener,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing
        )
    }

    /**
     *  Checks if Category exists in type list, if not then creates new Category with given [name]
     *  and type.
     */
    private fun insertCategory(name : String) {

        if (catVM.catNames[catVM.listShown].contains(name)) {
            val existBar: Snackbar = Snackbar.make(
                binding.root,
                getString(R.string.snackbar_exists, name), Snackbar.LENGTH_SHORT
            )
            existBar.anchorView = binding.categoryCi
            existBar.show()
        } else {
            // creates and inserts new Category with name and type depending on which list is shown
            val category = Category(0, name, if (catVM.listShown == 0) "Expense" else "Income")
            catVM.insertCategory(category)
        }
    }

    /**
     *  Ensures adapters are up to date with correct information.
     *  Ensures [adapter] is updated with correct [catList]
     *  using [type] and [pos] with either being "Expense"(0) or "Income"(1).
     */
    private fun updateAdapters(
        catList: List<Category>,
        adapter: CategoryAdapter,
        type: String,
        pos: Int
    ) {

        catVM.catLists[pos] = catList
        // tells ViewPager adapter there might be a change in data
        catListAdapter.submitList(catVM.catLists)

        // coroutine ensures that lists used by CatViewHolders are ready before updating adapter
        launch {
            catVM.catNames[pos] = catVM.getCatsByTypeAsync(type).await()
            catVM.catsUsed[pos] = catVM.getDistinctCatsByTypeAsync(type).await()
            adapter.submitList(catList)
        }

        // sets up DotsIndicator with ViewPager2
        binding.categoryCi.setViewPager(binding.categoryVp)
        // ensures ViewPager is showing correct list
        binding.categoryVp.setCurrentItem(catVM.listShown, false)
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     */
    private inner class CategoryListAdapter
        : ListAdapter<List<Category>, CategoryListHolder>(CatListDiffUtil()) {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListHolder {

            val itemViewBinding: ItemViewCatlistBinding = ItemViewCatlistBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return CategoryListHolder(itemViewBinding)
        }

        override fun onBindViewHolder(holder: CategoryListHolder, position: Int) {

            holder.bind(position)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view using [binding] as its layout.
     */
    private inner class CategoryListHolder(private var binding: ItemViewCatlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(type: Int) {

            binding.type = type
            binding.executePendingBindings()
            if (type == 0) {
                binding.ivclRv.adapter = expenseAdapter
            } else {
                binding.ivclRv.adapter = incomeAdapter
            }
            binding.ivclRv.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
            binding.ivclRv.layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     */
    private inner class CategoryAdapter :
        ListAdapter<Category, CategoryHolder>(CategoryDiffUtil()) {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {

            val itemViewBinding: ItemViewCategoryBinding = ItemViewCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return CategoryHolder(itemViewBinding)
        }

        // populates given holder with Category from the given position in CategoryList
        override fun onBindViewHolder(holder: CategoryHolder, position: Int) {

            val category: Category = getItem(position)
            holder.bind(category)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view using [binding] as its layout.
     */
    private inner class CategoryHolder(private var binding: ItemViewCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("StringFormatInvalid")
        fun bind(category: Category) {

            val type: Int = if (category.type == "Expense") 0 else 1

            // enables delete button if Category is not in use and if there is more than 1 Category
            if (!catVM.catsUsed[type].contains(category.category)
                && catVM.catLists[type].size > 1
            ) {
                binding.ivcatDelete.isEnabled = true

                // AlertDialog to ensure user does want to delete Category
                binding.ivcatDelete.setOnClickListener {
                    val posFun = DialogInterface.OnClickListener { _, _ ->
                        catVM.deleteCategory(category)
                    }

                    AlertDialogCreator.alertDialog(
                        context!!,
                        getString(R.string.alert_dialog_delete_category),
                        getString(R.string.alert_dialog_delete_warning, category.category),
                        getString(R.string.alert_dialog_yes), posFun,
                        getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing
                    )
                }
            }

            // AlertDialog with EditText that allows input for new name
            binding.ivcatEdit.setOnClickListener {

                // inflates view that holds EditText
                val viewInflated: View = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
                // the EditText to be used
                val input: EditText = viewInflated.findViewById(R.id.dialog_input)
                val posFun = DialogInterface.OnClickListener { _, _ ->
                    editCategory(input.text.toString(), category, type)
                }

                AlertDialogCreator.alertDialogInput(
                    context!!,
                    getString(R.string.alert_dialog_edit_category), viewInflated,
                    getString(R.string.alert_dialog_save), posFun,
                    getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing
                )
            }

            binding.category = category
            binding.executePendingBindings()
        }

        /**
         *  Checks if [updatedName] exists already in [type] Category table
         *  before updating [category]
         */
        private fun editCategory(updatedName: String, category: Category, type: Int) {

            // if exists, Snackbar appears telling user so, else, updates Category
            if (catVM.catNames[type].contains(updatedName)) {
                val existBar: Snackbar = Snackbar.make(
                    view!!,
                    getString(R.string.snackbar_exists, updatedName), Snackbar.LENGTH_SHORT
                )
                existBar.anchorView = this@CategoryFragment.binding.categoryCi
                existBar.show()
            } else {
                category.category = updatedName
                binding.category = category
                catVM.updateCategory(category)
            }
        }
    }
}