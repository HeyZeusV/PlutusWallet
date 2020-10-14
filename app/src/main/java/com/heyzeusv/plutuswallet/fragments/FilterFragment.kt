package com.heyzeusv.plutuswallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo
import com.heyzeusv.plutuswallet.databinding.FragmentFilterBinding
import com.heyzeusv.plutuswallet.viewmodels.CFLViewModel
import com.heyzeusv.plutuswallet.viewmodels.FilterViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val MIDNIGHT_MILLI = 86399999

/**
 *  Used to apply filters and tell TransactionListFragment which Transaction list to load
 */
@AndroidEntryPoint
class FilterFragment : Fragment(), DatePickerFragment.Callbacks {

    // DataBinding
    private lateinit var binding: FragmentFilterBinding

    // booleans used to tell which DateButton was pressed
    private var startButton = false
    private var endButton = false

    // provides instance of FilterViewModel
    private val filterVM: FilterViewModel by viewModels()
    // shared ViewModel
    private val cflVM: CFLViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false)
        binding.lifecycleOwner = activity
        binding.filterVM = filterVM

        // sending data that requires context to ViewModel
        filterVM.all = getString(R.string.category_all)
        filterVM.apply = getString(R.string.filter_apply)
        filterVM.end = getString(R.string.filter_end)
        filterVM.expense = getString(R.string.type_expense)
        filterVM.income = getString(R.string.type_income)
        filterVM.reset = getString(R.string.filter_reset)
        filterVM.start = getString(R.string.filter_start)
        filterVM.type = getString(R.string.filter_type)

        // preparing data/listeners
        filterVM.prepareSpinners()
        prepareListeners()

        return binding.root
    }

    /**
     *  Sets the [date] selected on dateButtons and saves the Date to be used later in a query.
     */
    override fun onDateSelected(date: Date) {

        if (startButton) {
            filterVM.startDate.value = date
            startButton = false
        }
        if (endButton) {
            // adds time to endDate to make it right before midnight of next day
            filterVM.endDate.value = Date(date.time + MIDNIGHT_MILLI)
            endButton = false
        }
    }

    /**
     *  Sets up OnClickListeners for MaterialButtons and sets them to MutableLiveData in ViewModel.
     *
     *  I wanted all these to be in ViewModel, but several required context. I know that
     *  AndroidViewModel allows the use of context within ViewModel, but I decided against using it.
     *  I did decide to create all OnClickListeners here rather than some here and others in
     *  ViewModel.
     */
    private fun prepareListeners() {

        // sets up typeButton with initial value
        if (filterVM.typeSelected.value == "") filterVM.typeSelected.value = filterVM.expense

        // will change which Category Spinner will be displayed
        filterVM.typeOnClick.value = View.OnClickListener {
            if (filterVM.typeSelected.value == filterVM.expense) {
                filterVM.typeSelected.value = filterVM.income
                filterVM.typeVisible.value = false
            } else {
                filterVM.typeSelected.value = filterVM.expense
                filterVM.typeVisible.value = true
            }
        }

        // starts up DatePickerFragment
        filterVM.startOnClick.value = View.OnClickListener {
            DatePickerFragment.newInstance(filterVM.startDate.value!!).apply {
                // fragment that will be target and request code
                setTargetFragment(this@FilterFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@FilterFragment.parentFragmentManager, DIALOG_DATE)
                startButton = true
            }
        }

        // starts up DatePickerFragment
        filterVM.endOnClick.value = View.OnClickListener {
            DatePickerFragment.newInstance(filterVM.endDate.value!!).apply {
                // fragment that will be target and request code
                setTargetFragment(this@FilterFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@FilterFragment.parentFragmentManager, DIALOG_DATE)
                endButton = true
            }
        }

        filterVM.actionOnClick.value = View.OnClickListener {
            // startDate must be before endDate else it displays warning and doesn't apply filters
            if (filterVM.startDate.value!! > filterVM.endDate.value!!
                && filterVM.dateCheck.value!!
            ) {
                val dateBar: Snackbar = Snackbar.make(
                    it,
                    getString(R.string.filter_date_warning), Snackbar.LENGTH_SHORT
                )
                dateBar.show()
            } else {
                var cat: String
                val type: String
                // sets type and category applied
                if (filterVM.typeSelected.value == getString(R.string.type_expense)) {
                    type = "Expense"
                    cat = filterVM.exCategory.value!!
                } else {
                    type = "Income"
                    cat = filterVM.inCategory.value!!
                }

                // translates "All"
                if (cat == getString(R.string.category_all)) cat = "All"

                // updating MutableLiveData value in ViewModel
                val tInfo = TransactionInfo(
                    filterVM.accCheck.value!!, filterVM.catCheck.value!!, filterVM.dateCheck.value!!,
                    type, filterVM.account.value!!, cat,
                    filterVM.startDate.value!!, filterVM.endDate.value!!
                )

                // updates MutableLiveData, causing Chart/ListFragment refresh
                cflVM.updateTInfo(tInfo)
                // if all filters are unchecked
                if (!filterVM.accCheck.value!!
                    && !filterVM.catCheck.value!!
                    && !filterVM.dateCheck.value!!
                ) {
                    filterVM.resetFilter()
                }
                cflVM.filterChanged = true
            }
        }
    }
}