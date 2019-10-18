package com.heyzeusv.financeapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.heyzeusv.financeapplication.utilities.BaseFragment
import java.text.DateFormat
import java.util.*

private const val TAG            = "FilterFragment"
private const val DIALOG_DATE    = "DialogDate"
private const val REQUEST_DATE   = 0
private const val MIDNIGHT_MILLI = 86399999

class FilterFragment : BaseFragment(), DatePickerFragment.Callbacks {

    interface Callbacks {

        fun onFilterApplied(category : Boolean, date : Boolean, categoryName : String, start : Date, end : Date)
    }

    private var callbacks : Callbacks? = null

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

    // used to tell if app is starting up
    private var startUp = true

    // used to set button time and to pass Dates to queries
    private var startDate = Date()
    private var endDate   = Date()

    // state of checkboxes
    private var categorySelected = false
    private var dateSelected     = false

    private var categoryNamesList : MutableList<String> = mutableListOf()

    // default strings used
    private var categoryName    : String = "Education"
    private var applyButtonText : String = "Reset"

    // provides instance of ViewModel
    private val filterViewModel : FilterViewModel by lazy {
        ViewModelProviders.of(this).get(FilterViewModel::class.java)
    }

    // called when fragment is attached to activity
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // stashing context into callbacks property which is the activity instance hosting fragment
        callbacks = context as Callbacks?
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_filter, container, false)

        categoryCheckBox = view.findViewById(R.id.filter_category_check) as CheckBox
        dateCheckBox     = view.findViewById(R.id.filter_date_check)     as CheckBox
        startDateButton  = view.findViewById(R.id.filter_start_date)     as MaterialButton
        endDateButton    = view.findViewById(R.id.filter_end_date)       as MaterialButton
        applyButton      = view.findViewById(R.id.filter_apply)          as MaterialButton
        categorySpinner  = view.findViewById(R.id.filter_category)       as Spinner

        // will only run when app is first started
        if (startUp) {

            resetTime()
        }

        // restores state of views
        categorySpinner.isEnabled = categorySelected
        startDateButton.isEnabled = dateSelected
        endDateButton  .isEnabled = dateSelected
        endDateButton  .text = DateFormat.getDateInstance(DateFormat.SHORT).format(endDate)
        startDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(startDate)
        applyButton    .text = applyButtonText

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
                    categoryNamesList = categoryNames.toMutableList()
                    categoryNamesList.sort()
                    // sets up the categorySpinner
                    val categorySpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, categoryNamesList)
                    categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                    categorySpinner.adapter = categorySpinnerAdapter
                    // starts the spinner up to ExpenseCategory saved
                    categorySpinner.setSelection(categoryNamesList.indexOf(categoryName))
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
                updateUi()
            }
            // skips animation
            jumpDrawablesToCurrentState()
        }

        dateCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->

                dateSelected              = isChecked
                startDateButton.isEnabled = isChecked
                endDateButton  .isEnabled = isChecked
                updateUi()
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

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                categoryName = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        applyButton.setOnClickListener {

            // startDate must be before endDate
            if (startDate > endDate) {

                Toast.makeText(it.context, "End date is before start date!!", Toast.LENGTH_LONG).show()
            } else {

                // adds time to endDate to make it right before midnight of next day
                val endDateCorrected = Date(endDate.time + MIDNIGHT_MILLI)
                callbacks?.onFilterApplied(categorySelected, dateSelected, categoryName, startDate, endDateCorrected)
                // if both filters are unchecked
                if (!categorySelected && !dateSelected) {

                    resetFilter()
                }
            }
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

    // sets everything in filter as if app was first starting
    private fun resetFilter() {

        resetTime()
        categorySpinner.setSelection(0)
        endDateButton  .text = DateFormat.getDateInstance(DateFormat.SHORT).format(endDate)
        startDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(startDate)
    }

    // sets the startDate to very start of current day and endDate to right before the next day
    private fun resetTime() {

        val date = GregorianCalendar()
        date.set(Calendar.HOUR_OF_DAY, 0)
        date.set(Calendar.MINUTE     , 0)
        date.set(Calendar.SECOND     , 0)
        date.set(Calendar.MILLISECOND, 0)
        startDate.time = date.timeInMillis
        endDate  .time = startDate.time
        startUp = false
    }

    // only thing to update at the moment is the applyButton text
    private fun updateUi() {

        if (!categorySelected && !dateSelected) {

            applyButtonText  = getString(R.string.filter_reset)
            applyButton.text = applyButtonText
        } else {

            applyButtonText  = getString(R.string.filter_apply)
            applyButton.text = applyButtonText
        }
    }

    companion object {

        // can be called by activities to get instance of fragment
        fun newInstance() : FilterFragment {

            return FilterFragment()
        }
    }
}