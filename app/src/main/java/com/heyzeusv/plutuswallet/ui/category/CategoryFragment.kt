package com.heyzeusv.plutuswallet.ui.category

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
            createDialog(
                getString(R.string.alert_dialog_edit_category), category,
                type, catVM::editCategoryName
            )
        })

        catVM.existsCategoryEvent.observe(viewLifecycleOwner, EventObserver { name: String ->
            val existBar: Snackbar = Snackbar.make(
                binding.root, getString(R.string.snackbar_exists, name), Snackbar.LENGTH_SHORT
            )
            existBar.anchorView = binding.categoryCi
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
                val category = Category(0, "", "Expense")
                createDialog(
                    getString(R.string.alert_dialog_create_category), category,
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
     *  that performs [posFun] on [type] list on positive button click on [category].
     */
    private fun createDialog(action: String, category: Category, type: Int, posFun: (Category, String, Int) -> Unit) {

        // inflates view that holds EditText
        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
        // the EditText to be used
        val input: EditText = viewInflated.findViewById(R.id.dialog_input)

        val posListener = DialogInterface.OnClickListener { _, _ ->
            posFun(category, input.text.toString(), type)
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
                catVM.initNamesUsedLists(type, pos)
                adapter.submitList(catList)
            }
        } else {
            adapter.submitList(catList)
        }
    }
}