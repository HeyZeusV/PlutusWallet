package com.heyzeusv.plutuswallet.ui.cfl.filter

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.FragmentFilterBinding
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.EventObserver
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel
import com.heyzeusv.plutuswallet.util.addAllUnique
import com.heyzeusv.plutuswallet.util.checkAll
import dagger.hilt.android.AndroidEntryPoint

/**
 *  Used to apply filters and tell TransactionListFragment which Transaction list to load
 */
@AndroidEntryPoint
class FilterFragment : Fragment() {

    // DataBinding
    private lateinit var binding: FragmentFilterBinding

    // provides instance of FilterViewModel
    private val filterVM: FilterViewModel by viewModels()

    // shared ViewModel
    private val cflVM: CFLViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        filterVM.all = getString(R.string.category_all)

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false)
        binding.lifecycleOwner = activity
        binding.filterVM = filterVM

        // preparing data/listeners
        filterVM.prepareChipData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // used by SnackBars
        val snackBarAnchor: CoordinatorLayout? =
            parentFragment?.view?.rootView?.findViewById(R.id.cfl_anchor)

        binding.filterAccount.setOnClickListener {
            filterVM.accFilter.value = !filterVM.accFilter.value!!
            binding.filterAccountMotion.apply {
                if (filterVM.accFilter.value!!) transitionToEnd() else transitionToStart()
            }
        }

        binding.filterCategory.setOnClickListener {
            filterVM.catFilter.value = !filterVM.catFilter.value!!
            if (filterVM.catFilter.value!!) {
                binding.filterCategoryMotion.transitionToEnd()
                // after a short delay, scroll down to show all children
                binding.filterScroll.postDelayed({
                    binding.filterScroll.smoothScrollBy(0, if (filterVM.typeVisible.value!!) {
                        binding.filterExpenseChips.bottom
                    } else {
                        binding.filterIncomeChips.bottom
                    })
                }, 450)
            } else {
                binding.filterCategoryMotion.transitionToStart()
            }
        }

        binding.filterDate.setOnClickListener {
            filterVM.dateFilterOld.value = !filterVM.dateFilterOld.value!!
            if (filterVM.dateFilterOld.value!!) {
                binding.filterDateMotion.transitionToEnd()
                // after a short delay, scroll down to show all children
                binding.filterScroll.postDelayed({
                    binding.filterScroll.smoothScrollBy(0, binding.filterScroll.bottom)
                }, 450)
            } else {
                binding.filterDateMotion.transitionToStart()
            }
        }

        filterVM.cflChange.observe(viewLifecycleOwner, EventObserver { change: Boolean ->
            // updates MutableLiveData, causing Chart/ListFragment refresh
            cflVM.updateTInfo(filterVM.cflTInfo)
            cflVM.filterChanged = change
        })

        filterVM.noChipEvent.observe(viewLifecycleOwner, EventObserver {
            val chipBar: Snackbar = Snackbar.make(
                binding.root, if (it) {
                    getString(R.string.filter_no_chip_account)
                } else {
                    getString(R.string.filter_no_chip_category)
                }, Snackbar.LENGTH_SHORT
            )
            chipBar.anchorView = snackBarAnchor
            chipBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSnackbarText))
            chipBar.show()
        })

        filterVM.dateErrorEvent.observe(viewLifecycleOwner, EventObserver {
            val dateBar: Snackbar = Snackbar.make(
                binding.root, getString(R.string.filter_date_warning), Snackbar.LENGTH_SHORT
            )
            dateBar.anchorView = snackBarAnchor
            dateBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSnackbarText))
            dateBar.show()
        })

        filterVM.selectDateEvent.observe(viewLifecycleOwner, EventObserver { dateId: Int ->
            val dateDialog: DatePickerDialog = if (dateId == R.id.filter_start_date) {
                DateUtils.datePickerDialog(
                    binding.root, filterVM.startDate, filterVM::startDateSelected
                )
            } else {
                DateUtils.datePickerDialog(
                    binding.root, filterVM.endDate, filterVM::endDateSelected
                )
            }
            dateDialog.show()
        })

        filterVM.resetEvent.observe(viewLifecycleOwner, EventObserver  { reset: Boolean  ->
            if (reset) {
                binding.filterAccountChips.clearCheck()
                binding.filterExpenseChips.clearCheck()
                binding.filterIncomeChips.clearCheck()
            }
        })

        filterVM.accList.observe(viewLifecycleOwner, { accounts: MutableList<String> ->
            // don't attempt to create Chip if list hasn't been loaded
            if (accounts.size > 0) {
                createChips(binding.filterAccountChips, accounts, filterVM.accSelectedChips)
            }
        })

        filterVM.exCatList.observe(viewLifecycleOwner, {categories: MutableList<String> ->
            // don't attempt to create Chip if list hasn't been loaded
            if (categories.size > 0) {
                createChips(binding.filterExpenseChips, categories, filterVM.exCatSelectedChips)
            }
        })

        filterVM.inCatList.observe(viewLifecycleOwner, {categories: MutableList<String> ->
            // don't attempt to create Chip if list hasn't been loaded
            if (categories.size > 0) {
                createChips(binding.filterIncomeChips, categories, filterVM.inCatSelectedChips)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // opens filters back up if they were open before user left screen
        if (filterVM.accFilter.value!!) binding.filterAccountMotion.transitionToEnd()
        if (filterVM.catFilter.value!!) binding.filterCategoryMotion.transitionToEnd()
        if (filterVM.dateFilterOld.value!!) binding.filterDateMotion.transitionToEnd()
    }

    /**
     *  Creates Chips using [entryList] and adds them to [parent] ChipGroup. When Chips are
     *  checked/unchecked, they will be added/removed from given [selectedList].
     */
    private fun createChips(
        parent: ChipGroup,
        entryList: MutableList<String>,
        selectedList: MutableList<String>
    ) {

        parent.apply {
            // remove previous children so there won't be repeats
            if (this.childCount > 0) this.removeAllViews()
            // go through list and create Chip for each category
            entryList.forEachIndexed { index: Int, entry: String ->
                val chip: Chip =
                    LayoutInflater.from(context).inflate(R.layout.chip, this, false) as Chip
                chip.id = index
                // adds empty space to be used as padding as Chip if too short
                chip.text = when {
                    entry.length < 2 -> "   $entry   "
                    entry.length < 4 -> "  $entry  "
                    else -> entry
                }
                // checks Chip if entry is in selectedList
                chip.isChecked = selectedList.contains(entry)
                // adds/removes from selected list
                chip.setOnCheckedChangeListener { _, checked: Boolean ->
                    selectedList.apply {
                        // translated "All" Chip only in category ChipGroups
                        if (entry == getString(R.string.category_all)) {
                            // add all entries to list and check all Chips
                            if (checked) {
                                addAllUnique(entryList)
                                parent.checkAll()
                            // remove all entries from list and uncheck all Chips
                            } else {
                                clear()
                                parent.clearCheck()
                            }
                        } else {
                            if (checked) add(entry) else remove(entry)
                        }
                    }
                }
                // add to ChipGroup
                this.addView(chip)
            }
        }
    }
}