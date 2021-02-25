package com.heyzeusv.plutuswallet.ui.category

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.databinding.FragmentCategoryBinding
import com.heyzeusv.plutuswallet.ui.base.BaseFragment
import com.heyzeusv.plutuswallet.util.AlertDialogCreator
import com.heyzeusv.plutuswallet.util.EventObserver
import kotlinx.coroutines.launch

/**
 *  Shows all Categories depending on type in database and allows users to either
 *  edit them or delete them.
 */
class CategoryFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentCategoryBinding

    // adapters used by ViewPager
    private lateinit var catListAdapter: CategoryListAdapter

    // adapters used by RecyclerViews
    private lateinit var expenseAdapter: CategoryAdapter
    private lateinit var incomeAdapter: CategoryAdapter

    // instance of CategoryViewModel
    private val catVM: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // initialize adapters here due to requiring ViewModel
        catListAdapter = CategoryListAdapter(catVM)
        expenseAdapter = CategoryAdapter(catVM)
        incomeAdapter = CategoryAdapter(catVM)
        catVM.expenseAdapter = expenseAdapter
        catVM.incomeAdapter = incomeAdapter

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.categoryVp.adapter = catListAdapter
        // need 2 tabs on ViewPager; "Expense" and "Income"
        catListAdapter.submitList(listOf(emptyList(), emptyList()))
        // sets up DotsIndicator with ViewPager2
        binding.categoryCi.setViewPager(binding.categoryVp)

        return binding.root
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        catVM.expenseCatsLD.observe(viewLifecycleOwner, { list: List<Category> ->
            updateAdapters(list, expenseAdapter, "Expense", 0)
        })

        catVM.incomeCatsLD.observe(viewLifecycleOwner, { list: List<Category> ->
            updateAdapters(list, incomeAdapter, "Income", 1)
        })

        catVM.editCategoryEvent.observe(viewLifecycleOwner, EventObserver { category: Category ->
            val type: Int = if (category.type == "Expense") 0 else 1
            val alertDialogView = createAlertDialogView()
            AlertDialogCreator.alertDialogInput(
                requireContext(), alertDialogView,
                getString(R.string.alert_dialog_edit_category),
                getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                null, null, null, null, null,
                category, type, catVM::editCategoryName
            )
        })

        catVM.existsCategoryEvent.observe(viewLifecycleOwner, EventObserver { name: String ->
            val existBar: Snackbar = Snackbar.make(
                binding.root, getString(R.string.snackbar_exists, name), Snackbar.LENGTH_SHORT
            )
            existBar.anchorView = binding.categoryAnchor
            existBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSnackbarText))
            existBar.show()

        })

        catVM.deleteCategoryEvent.observe(viewLifecycleOwner, EventObserver { category: Category ->
            val type: Int = if (category.type == "Expense") 0 else 1
            val posFun = DialogInterface.OnClickListener { _, _ ->
                catVM.deleteCategoryPosFun(category, type)
            }

            AlertDialogCreator.alertDialog(
                requireContext(), getString(R.string.alert_dialog_delete_category),
                getString(R.string.alert_dialog_delete_warning, category.category),
                getString(R.string.alert_dialog_yes), posFun,
                getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing
            )
        })

        // navigates user back to CFLFragment
        binding.categoryTopBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        // handles menu selection
        binding.categoryTopBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.category_new) {
                val newCategory = Category(0, "", "Expense")
                val alertDialogView = createAlertDialogView()
                AlertDialogCreator.alertDialogInput(
                    requireContext(), alertDialogView,
                    getString(R.string.alert_dialog_create_category),
                    getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                    null, null, null, null, null,
                    newCategory, binding.categoryVp.currentItem, catVM::insertNewCategory
                )
                true
            } else {
                false
            }
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

        // only need to retrieve all names/in use lists once
        if (catVM.catNames[pos].isEmpty()) {
            launch {
                catVM.initNamesUsedLists(type, pos)
                adapter.submitList(catList)
            }
        } else {
            adapter.submitList(catList)
        }
    }
}