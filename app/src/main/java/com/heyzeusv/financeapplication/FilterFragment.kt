package com.heyzeusv.financeapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.heyzeusv.financeapplication.utilities.BaseFragment
import java.text.DateFormat
import java.util.*

private const val TAG           = "FilterFragment"
private const val DIALOG_DATE   = "DialogDate"
private const val REQUEST_DATE  = 0
private const val ONE_DAY_MILLI = 86400000

class FilterFragment : BaseFragment(), DatePickerFragment.Callbacks {

    // views
    private lateinit var categoryCheckBox : CheckBox
    private lateinit var dateCheckBox     : CheckBox
    private lateinit var startDateButton  : MaterialButton
    private lateinit var endDateButton    : MaterialButton
    private lateinit var applyButton      : MaterialButton
    private lateinit var categorySpinner  : Spinner

    // booleans used to tell which DateButton was pressed
    private var start = false
    private var end   = false

    // used to set button time and to pass Dates to queries
    private var startDate = Date()
    private var endDate   = Date(startDate.time + ONE_DAY_MILLI)

    // state of checkboxes
    private var categorySelected = false
    private var dateSelected     = false

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

        categoryCheckBox = view.findViewById(R.id.filter_category_check) as CheckBox
        dateCheckBox     = view.findViewById(R.id.filter_date_check)     as CheckBox
        startDateButton  = view.findViewById(R.id.filter_start_date)     as MaterialButton
        endDateButton    = view.findViewById(R.id.filter_end_date)       as MaterialButton
        applyButton      = view.findViewById(R.id.filter_apply)          as MaterialButton
        categorySpinner  = view.findViewById(R.id.filter_category)       as Spinner

        // restores state of views
        categorySpinner.isEnabled = categorySelected
        startDateButton.isEnabled = dateSelected
        endDateButton  .isEnabled = dateSelected
        applyButton    .isEnabled = categorySelected or dateSelected
        endDateButton  .text = DateFormat.getDateInstance(DateFormat.SHORT).format(endDate)
        startDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(startDate)


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

        categoryCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->

                categorySelected          = isChecked
                categorySpinner.isEnabled = isChecked
                applyButton    .isEnabled = isChecked or dateSelected
            }
            // skips animation
            jumpDrawablesToCurrentState()
        }

        dateCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->

                dateSelected              = isChecked
                startDateButton.isEnabled = isChecked
                endDateButton  .isEnabled = isChecked
                applyButton    .isEnabled = isChecked or categorySelected
            }
            // skips animation
            jumpDrawablesToCurrentState()
        }

        startDateButton.setOnClickListener {

            DatePickerFragment.newInstance(startDate).apply {

                // fragment that will be target and request code
                setTargetFragment(this@FilterFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@FilterFragment.requireFragmentManager(), DIALOG_DATE)
                start = true
            }
        }

        endDateButton.setOnClickListener {

            DatePickerFragment.newInstance(endDate).apply {

                // fragment that will be target and request code
                setTargetFragment(this@FilterFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@FilterFragment.requireFragmentManager(), DIALOG_DATE)
                end = true
            }
        }

        applyButton.setOnClickListener {

            if (startDate > endDate) {

                Toast.makeText(it.context, "End date is before start date!!", Toast.LENGTH_LONG).show()            }
        }
    }

    // sets the Date selected on dateButtons and saves the Date to be used later in a query
    override fun onDateSelected(date: Date) {

        if (start) {

            startDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
            startDate            = date
            start                = false
        }
        if (end) {

            endDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
            endDate            = date
            end                = false
        }
    }
}