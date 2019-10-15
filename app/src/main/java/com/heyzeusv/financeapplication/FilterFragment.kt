package com.heyzeusv.financeapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.heyzeusv.financeapplication.utilities.BaseFragment
import java.text.DateFormat
import java.util.*

private const val DIALOG_DATE  = "DialogDate"
private const val REQUEST_DATE = 0

class FilterFragment : BaseFragment(), DatePickerFragment.Callbacks {

    // views
    private lateinit var categorySpinner : Spinner
    private lateinit var startDateButton : MaterialButton
    private lateinit var endDateButton   : MaterialButton

    private var start = false
    private var end   = false

    // provides instance of ViewModel
    private val filterViewModel : FilterViewModel by lazy {
        ViewModelProviders.of(this).get(FilterViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_filter, container, false)

        categorySpinner = view.findViewById(R.id.filter_category)   as Spinner
        startDateButton = view.findViewById(R.id.filter_start_date) as MaterialButton
        endDateButton   = view.findViewById(R.id.filter_end_date)   as MaterialButton

        val calendar : Calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        endDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.time)
        startDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(Date())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to another component
        filterViewModel.categoryNamesLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { categoryNames ->
                // if not null
                categoryNames?.let {
                    val categoryNamesList : MutableList<String> = categoryNames.toMutableList()
                    categoryNamesList.sort()
                    categoryNamesList.add(0, "All")
                    // sets up the categorySpinner
                    val categorySpinnerAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, categoryNamesList)
                    categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                    categorySpinner.adapter = categorySpinnerAdapter
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        startDateButton.setOnClickListener {

            DatePickerFragment.newInstance(Date()).apply {

                setTargetFragment(this@FilterFragment, REQUEST_DATE)
                show(this@FilterFragment.requireFragmentManager(), DIALOG_DATE)
                start = true
            }
        }

        endDateButton.setOnClickListener {

            DatePickerFragment.newInstance(Date()).apply {

                setTargetFragment(this@FilterFragment, REQUEST_DATE)
                show(this@FilterFragment.requireFragmentManager(), DIALOG_DATE)
                end = true
            }
        }
    }

    override fun onDateSelected(date: Date) {

        if (start) {

            startDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
            start                = false
        }
        if (end) {

            endDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
            end                = false
        }
    }

}