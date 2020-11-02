package com.heyzeusv.plutuswallet.ui.cfl.filter

import android.app.DatePickerDialog
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
import com.heyzeusv.plutuswallet.databinding.FragmentFilterBinding
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.EventObserver
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel
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
    ): View? {

        filterVM.all = getString(R.string.category_all)

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false)
        binding.lifecycleOwner = activity
        binding.filterVM = filterVM

        // preparing data/listeners
        filterVM.prepareSpinners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterVM.cflChange.observe(viewLifecycleOwner, EventObserver { change: Boolean ->
            // updates MutableLiveData, causing Chart/ListFragment refresh
            cflVM.updateTInfo(filterVM.cflTInfo)
            cflVM.filterChanged = change
        })

        filterVM.dateErrorEvent.observe(viewLifecycleOwner, EventObserver {
            val dateBar: Snackbar = Snackbar.make(
                binding.root, getString(R.string.filter_date_warning), Snackbar.LENGTH_SHORT
            )
            dateBar.show()
        })

        filterVM.selectDateEvent.observe(viewLifecycleOwner, EventObserver { dateId: Int ->
            val dateDialog: DatePickerDialog = if (dateId == R.id.filter_start_date) {
                DateUtils.datePickerDialog(
                    binding.root, filterVM.startDate.value!!, filterVM::startDateSelected
                )
            } else {
                DateUtils.datePickerDialog(
                    binding.root, filterVM.endDate.value!!, filterVM::endDateSelected
                )
            }
            dateDialog.show()
        })
    }
}