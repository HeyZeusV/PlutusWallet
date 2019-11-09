package com.heyzeusv.financeapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
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

/**
 *  Used to apply filters and tell TransactionListFragment which Transaction list to load
 */
class FilterFragment : BaseFragment(), DatePickerFragment.Callbacks {

    /**
     *  Required interface for hosting fragments.
     *
     *  Defines work that the fragment needs done by hosting activity.
     */
    interface Callbacks {

        /**
         *  Tells Repository which Transaction list to return
         *
         *  Uses the values of category and date in order to determine which Transaction list is needed.
         *
         *  @param  category     boolean for category filter
         *  @param  date         boolean for date filter
         *  @param  type         either "Expense" or "Income"
         *  @param  categoryName category name to be searched in table of type
         *  @param  start        starting Date for date filter
         *  @param  end          ending Date for date filter
         *  @return LiveData object holding list of Transactions
         */
        fun onFilterApplied(category : Boolean, date : Boolean, type : String, categoryName : String, start : Date, end : Date)
    }

    private var callbacks : Callbacks? = null

    // views
    private lateinit var categoryCheckBox       : CheckBox
    private lateinit var dateCheckBox           : CheckBox
    private lateinit var typeButton             : MaterialButton
    private lateinit var startDateButton        : MaterialButton
    private lateinit var endDateButton          : MaterialButton
    private lateinit var applyButton            : MaterialButton
    private lateinit var expenseCategorySpinner : Spinner
    private lateinit var incomeCategorySpinner  : Spinner

    // booleans used to tell which DateButton was pressed
    private var startButton = false
    private var endButton   = false

    // used to tell if app is starting up
    private var startUp = true

    // used to set button time and to pass Dates to queries
    private var startDate = Date()
    private var endDate   = Date()

    // state of checkboxes
    private var categorySelected = false
    private var dateSelected     = false

    // lists containing Category names
    private var expenseCategoryNamesList : MutableList<String> = mutableListOf()
    private var incomeCategoryNamesList  : MutableList<String> = mutableListOf()

    // default strings used
    private var typeButtonText  : String = FinanceApplication.context!!.getString(R.string.expense)
    private var categoryName    : String = FinanceApplication.context!!.getString(R.string.all)
    private var applyButtonText : String = FinanceApplication.context!!.getString(R.string.reset)

    // provides instance of ViewModel
    private val filterViewModel : FilterViewModel by lazy {
        ViewModelProviders.of(this).get(FilterViewModel::class.java)
    }

    override fun onAttach(context : Context) {
        super.onAttach(context)

        // stashing context into callbacks property which is the activity instance hosting fragment
        callbacks = context as Callbacks?
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_filter, container, false)

        categoryCheckBox       = view.findViewById(R.id.filter_category_check)   as CheckBox
        dateCheckBox           = view.findViewById(R.id.filter_date_check)       as CheckBox
        typeButton             = view.findViewById(R.id.filter_type)             as MaterialButton
        startDateButton        = view.findViewById(R.id.filter_start_date)       as MaterialButton
        endDateButton          = view.findViewById(R.id.filter_end_date)         as MaterialButton
        applyButton            = view.findViewById(R.id.filter_apply)            as MaterialButton
        expenseCategorySpinner = view.findViewById(R.id.filter_expense_category) as Spinner
        incomeCategorySpinner  = view.findViewById(R.id.filter_income_category)  as Spinner

        // will only run when app is first started
        if (startUp) {

            resetTime()
        }

        // restores state of views
        typeButton            .isEnabled = categorySelected
        expenseCategorySpinner.isEnabled = categorySelected
        incomeCategorySpinner .isEnabled = categorySelected
        startDateButton       .isEnabled = dateSelected
        endDateButton         .isEnabled = dateSelected
        typeButton            .text      = typeButtonText
        endDateButton         .text      = DateFormat.getDateInstance(DateFormat.SHORT).format(endDate)
        startDateButton       .text      = DateFormat.getDateInstance(DateFormat.SHORT).format(startDate)
        applyButton           .text      = applyButtonText
        expenseCategorySpinner.isVisible = typeButtonText == getString(R.string.expense)
        incomeCategorySpinner .isVisible = typeButtonText == getString(R.string.income)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to another component
        filterViewModel.expenseCategoryNamesLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { expenseCategoryNames ->
                // if not null
                expenseCategoryNames?.let {
                    expenseCategoryNamesList = expenseCategoryNames.toMutableList()
                    // sorts list in alphabetical order
                    expenseCategoryNamesList.sort()
                    // Category to show all of one type
                    expenseCategoryNamesList.add(0, getString(R.string.all))
                    // sets up the categorySpinner
                    val categorySpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, expenseCategoryNamesList)
                    categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                    expenseCategorySpinner.adapter = categorySpinnerAdapter
                    // starts the spinner up to ExpenseCategory saved
                    expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(categoryName))
                }
            }
        )

        // register an observer on LiveData instance and tie life to another component
        filterViewModel.incomeCategoryNamesLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { incomeCategoryNames ->
                // if not null
                incomeCategoryNames?.let {
                    incomeCategoryNamesList = incomeCategoryNames.toMutableList()
                    // sorts list in alphabetical order
                    incomeCategoryNamesList.sort()
                    // Category to show all of one type
                    incomeCategoryNamesList.add(0, getString(R.string.all))
                    // sets up the categorySpinner
                    val categorySpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, incomeCategoryNamesList)
                    categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                    incomeCategorySpinner.adapter = categorySpinnerAdapter
                    // starts the spinner up to ExpenseCategory saved
                    incomeCategorySpinner.setSelection(incomeCategoryNamesList.indexOf(categoryName))
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        categoryCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->

                categorySelected                 = isChecked
                typeButton            .isEnabled = isChecked
                expenseCategorySpinner.isEnabled = isChecked
                incomeCategorySpinner .isEnabled = isChecked
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

        typeButton.setOnClickListener {

            if (typeButtonText == "Expense") {

                typeButtonText                   = getString(R.string.income)
                typeButton            .text      = typeButtonText
                expenseCategorySpinner.isVisible = false
                incomeCategorySpinner .isVisible = true
            } else {

                typeButtonText                   = getString(R.string.expense)
                typeButton            .text      = typeButtonText
                expenseCategorySpinner.isVisible = true
                incomeCategorySpinner .isVisible = false
            }
        }

        startDateButton.setOnClickListener {

            DatePickerFragment.newInstance(startDate).apply {

                // fragment that will be target and request code
                setTargetFragment(this@FilterFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@FilterFragment.requireFragmentManager(), DIALOG_DATE)
                startButton = true
            }
        }

        endDateButton.setOnClickListener {

            DatePickerFragment.newInstance(endDate).apply {

                // fragment that will be target and request code
                setTargetFragment(this@FilterFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@FilterFragment.requireFragmentManager(), DIALOG_DATE)
                endButton = true
            }
        }

        expenseCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                categoryName = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        incomeCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                categoryName = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        applyButton.setOnClickListener {

            // startDate must be before endDate else it displays Toast warning and doesn't apply filters
            if (startDate > endDate) {

                Toast.makeText(it.context, getString(R.string.date_warning), Toast.LENGTH_LONG).show()
            } else {

                // adds time to endDate to make it right before midnight of next day
                val endDateCorrected = Date(endDate.time + MIDNIGHT_MILLI)
                callbacks?.onFilterApplied(categorySelected, dateSelected, typeButtonText, categoryName, startDate, endDateCorrected)
                // if both filters are unchecked
                if (!categorySelected && !dateSelected) {

                    resetFilter()
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()

        // afterward you cannot access the activity or count on the activity continuing to exist
        callbacks = null
    }

    /**
     *  Sets the Date selected on dateButtons and saves the Date to be used later in a query.
     *
     *  @param date the date the user selected in DatePickerFragment
     */
    override fun onDateSelected(date: Date) {

        if (startButton) {

            startDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
            startDate            = date
            startButton          = false
        }
        if (endButton) {

            endDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
            endDate            = date
            endButton          = false
        }
    }

    /**
     *  Sets everything in FilterFragment as if app was first starting.
     */
    private fun resetFilter() {

        resetTime()
        expenseCategorySpinner.setSelection(0)
        incomeCategorySpinner .setSelection(0)
        typeButtonText                         = getString(R.string.expense)
        typeButton            .text            = typeButtonText
        endDateButton         .text            = DateFormat.getDateInstance(DateFormat.SHORT).format(endDate)
        startDateButton       .text            = DateFormat.getDateInstance(DateFormat.SHORT).format(startDate)
    }

    /**
     *  Sets the startDate to very start of current day and endDate to right before the next day.
     */
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

    /**
     *  Only thing to update at the moment is the applyButton text.
     */
    private fun updateUi() {

        if (!categorySelected && !dateSelected) {

            applyButtonText  = getString(R.string.reset)
            applyButton.text = applyButtonText
        } else {

            applyButtonText  = getString(R.string.apply)
            applyButton.text = applyButtonText
        }
    }

    companion object {

        /**
         *  Initializes instance of FilterFragment
         */
        fun newInstance() : FilterFragment {

            return FilterFragment()
        }
    }
}