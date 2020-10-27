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
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.databinding.FragmentCategoryBinding
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.utilities.adapters.CategoryAdapter
import com.heyzeusv.plutuswallet.utilities.adapters.CategoryListAdapter
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
    ): View? {

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

        catVM.editCategory.observe(viewLifecycleOwner, { category: Category? ->
            if (category != null) {
                val type: Int = if (category.type == "Expense") 0 else 1
                createDialog(
                    getString(R.string.alert_dialog_edit_category),
                    type, catVM::editCategoryName
                )
            }
        })

        catVM.existsCategory.observe(viewLifecycleOwner, { name: String? ->
            if (name != null) {
                val existBar: Snackbar = Snackbar.make(
                    binding.root, getString(R.string.snackbar_exists, name), Snackbar.LENGTH_SHORT
                )
                existBar.anchorView = binding.categoryCi
                existBar.show()
                catVM.existsCategory.value = null
            }
        })

        catVM.deleteCategory.observe(viewLifecycleOwner, { category: Category? ->
            if (category != null) {
                val type: Int = if (category.type == "Expense") 0 else 1
                val posFun = DialogInterface.OnClickListener { _, _ ->
                    catVM.catNames[type].remove(category.category)
                    catVM.catsUsed[type].remove(category.category)
                    catVM.deleteCategory(category)
                }

                AlertDialogCreator.alertDialog(
                    requireContext(), getString(R.string.alert_dialog_delete_category),
                    getString(R.string.alert_dialog_delete_warning, category.category),
                    getString(R.string.alert_dialog_yes), posFun,
                    getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing
                )
                catVM.deleteCategory.value = null
            }
        })

        // navigates user back to CFLFragment
        binding.accountTopBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        // handles menu selection
        binding.accountTopBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.category_new) {
                createDialog(
                    getString(R.string.alert_dialog_create_category),
                    binding.categoryVp.currentItem, catVM::insertNewCategory
                )
                true
            } else {
                false
            }
        }
    }

    /**
     *  Creates AlertDialog that allows user input for given [action]
     *  that performs [posFun] on [type] list on positive button click.
     */
    private fun createDialog(action: String, type: Int, posFun: (String, Int) -> Unit) {

        // inflates view that holds EditText
        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
        // the EditText to be used
        val input: EditText = viewInflated.findViewById(R.id.dialog_input)

        val posListener = DialogInterface.OnClickListener { _, _ ->
            posFun(input.text.toString(), type)
        }

        AlertDialogCreator.alertDialogInput(
            requireContext(),
            action, viewInflated,
            getString(R.string.alert_dialog_save), posListener,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing
        )
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
                catVM.catNames[pos] = catVM.getCatsByTypeAsync(type).await()
                catVM.catsUsed[pos] = catVM.getDistinctCatsByTypeAsync(type).await()
                adapter.submitList(catList)
            }
        } else {
            adapter.submitList(catList)
        }
    }
}