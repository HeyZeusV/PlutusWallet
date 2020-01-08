package com.heyzeusv.plutuswallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo
import com.heyzeusv.plutuswallet.utilities.Utils
import com.heyzeusv.plutuswallet.viewmodels.FGLViewModel
import com.heyzeusv.plutuswallet.viewmodels.FilterViewModel
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

private const val TAG            = "PWFilterFragment"
private const val DIALOG_DATE    = "DialogDate"
private const val REQUEST_DATE   = 0
private const val MIDNIGHT_MILLI = 86399999

/**
 *  Used to apply filters and tell TransactionListFragment which Transaction list to load
 */
class FilterFragment : BaseFragment(), DatePickerFragment.Callbacks {

    // views
    private lateinit var accountCheckBox        : CheckBox
    private lateinit var categoryCheckBox       : CheckBox
    private lateinit var dateCheckBox           : CheckBox
    private lateinit var typeButton             : MaterialButton
    private lateinit var startDateButton        : MaterialButton
    private lateinit var endDateButton          : MaterialButton
    private lateinit var applyButton            : MaterialButton
    private lateinit var accountSpinner         : Spinner
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
    private var accountSelected  = false
    private var categorySelected = false
    private var dateSelected     = false

    // lists containing Account/Category names
    private var accountNameList          : MutableList<String> = mutableListOf()
    private var expenseCategoryNamesList : MutableList<String> = mutableListOf()
    private var incomeCategoryNamesList  : MutableList<String> = mutableListOf()

    // strings for localization
    private lateinit var applyButtonText : String
    private lateinit var typeButtonText  : String
    private lateinit var accountName     : String
    private lateinit var categoryName    : String
    private lateinit var all             : String

    // provides instance of FilterViewModel
    private val filterViewModel : FilterViewModel by lazy {
        ViewModelProviders.of(this).get(FilterViewModel::class.java)
    }

    private lateinit var fglViewModel : FGLViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initializing strings for localization
        applyButtonText = getString(R.string.filter_apply)
        typeButtonText  = getString(R.string.type_expense)
        accountName     = ""
        categoryName    = getString(R.string.category_all)
        all             = getString(R.string.category_all)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_filter, container, false)

        // initialize views
        accountCheckBox        = view.findViewById(R.id.filter_account_check   ) as CheckBox
        categoryCheckBox       = view.findViewById(R.id.filter_category_check  ) as CheckBox
        dateCheckBox           = view.findViewById(R.id.filter_date_check      ) as CheckBox
        typeButton             = view.findViewById(R.id.filter_type            ) as MaterialButton
        startDateButton        = view.findViewById(R.id.filter_start_date      ) as MaterialButton
        endDateButton          = view.findViewById(R.id.filter_end_date        ) as MaterialButton
        applyButton            = view.findViewById(R.id.filter_apply           ) as MaterialButton
        accountSpinner         = view.findViewById(R.id.filter_account         ) as Spinner
        expenseCategorySpinner = view.findViewById(R.id.filter_expense_category) as Spinner
        incomeCategorySpinner  = view.findViewById(R.id.filter_income_category ) as Spinner

        // will only run when app is first started
        if (startUp) {

            resetTime()
        }

        updateUi(true)

        // this ensures that this is same FGLViewModel as Graph/ListFragment use
        fglViewModel = activity!!.let {

            ViewModelProviders.of(it).get(FGLViewModel::class.java)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch {

            // will get size of new Category table
            val categorySize : Int = filterViewModel.getCategorySizeAsync().await() ?: 0
            // if new table is empty, add all Categories from old table in order to be inserted
            // into new table
            if (categorySize == 0) {

                val categoryList : MutableList<Category> = mutableListOf()
                val existingExpense : List<String> = filterViewModel.getExpenseCategoryNamesAsync().await()
                existingExpense.forEach {

                    if (it != all) {

                        val category = Category(0, it, "Expense")
                        categoryList.add(category)
                    }
                }
                val existingIncome : List<String> = filterViewModel.getIncomeCategoryNamesAsync().await()
                existingIncome.forEach {

                    if (it != all) {

                        val category = Category(0, it, "Income")
                        categoryList.add(category)
                    }
                }
                filterViewModel.insertCategories(categoryList)
            }

            //retrieves list of Accounts from database
            accountNameList = filterViewModel.getDistinctAccountsAsync().await().toMutableList()
            // will create a list of existing accounts and adds them to new table
            val accountList : MutableList<Account> = mutableListOf()
            accountNameList.forEach {

                val account = Account(0, it)
                accountList.add(account)
            }
            filterViewModel.upsertAccounts(accountList)
            // sorts list in alphabetical order
            accountNameList.sort()
            // sets up the accountSpinner
            val accountSpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!, R.layout.spinner_item, accountNameList)
            accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            accountSpinner.adapter = accountSpinnerAdapter
            // sets the spinner up to Account saved
            accountSpinner.setSelection(if (accountNameList.indexOf(accountName) == -1) {

                0
            } else {

                accountNameList.indexOf(accountName)
            })

            // retrieves list of Expense Categories from database
            expenseCategoryNamesList = filterViewModel.getCategoriesByTypeAsync(
                "Expense").await().toMutableList()
            // Category to show all of one type
            expenseCategoryNamesList.add(0, all)
            // sets up the categorySpinner
            val expenseSpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!,
                R.layout.spinner_item, expenseCategoryNamesList)
            expenseSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            expenseCategorySpinner.adapter = expenseSpinnerAdapter
            // starts the spinner up to ExpenseCategory saved
            expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(categoryName))

            // retrieves list of Income Categories from database
            incomeCategoryNamesList = filterViewModel.getCategoriesByTypeAsync(
                "Income").await().toMutableList()
            // Category to show all of one type
            incomeCategoryNamesList.add(0, all)
            // sets up the categorySpinner
            val incomeSpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!,
                R.layout.spinner_item, incomeCategoryNamesList)
            incomeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            incomeCategorySpinner.adapter = incomeSpinnerAdapter
            // starts the spinner up to ExpenseCategory saved
            incomeCategorySpinner.setSelection(incomeCategoryNamesList.indexOf(categoryName))
        }
    }

    override fun onStart() {
        super.onStart()

        accountCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->

                accountSelected          = isChecked
                accountSpinner.isEnabled = isChecked
                updateUi(false)
            }
            // skips animation
            jumpDrawablesToCurrentState()
        }

        categoryCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->

                categorySelected                 = isChecked
                typeButton            .isEnabled = isChecked
                expenseCategorySpinner.isEnabled = isChecked
                incomeCategorySpinner .isEnabled = isChecked
                updateUi(false)
            }
            // skips animation
            jumpDrawablesToCurrentState()
        }

        dateCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->

                dateSelected              = isChecked
                startDateButton.isEnabled = isChecked
                endDateButton  .isEnabled = isChecked
                updateUi(false)
            }
            // skips animation
            jumpDrawablesToCurrentState()
        }

        typeButton.setOnClickListener {

            if (typeButtonText == getString(R.string.type_expense)) {

                typeButtonText                   = getString(R.string.type_income)
                typeButton            .text      = typeButtonText
                expenseCategorySpinner.isVisible = false
                incomeCategorySpinner .isVisible = true
            } else {

                typeButtonText                   = getString(R.string.type_expense)
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

        accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent : AdapterView<*>?, view : View?, position : Int, id : Long) {

                accountName = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
            if (startDate > endDate && dateSelected) {

                val dateBar : Snackbar = Snackbar.make(it,
                    getString(R.string.filter_date_warning), Snackbar.LENGTH_SHORT)
                dateBar.show()
            } else {

                // adds time to endDate to make it right before midnight of next day
                val endDateCorrected = Date(endDate.time + MIDNIGHT_MILLI)
                val type : String    = if (typeButtonText == getString(R.string.type_expense)) {

                    "Expense"
                } else {

                    "Income"
                }
                // updating MutableLiveData value in ViewModel
                val tInfo =
                    TransactionInfo(
                        accountSelected, categorySelected, dateSelected,
                        type, accountName, categoryName, startDate, endDateCorrected
                    )
                fglViewModel.updateTInfo(tInfo)
                // if both filters are unchecked
                if (!accountSelected && !categorySelected && !dateSelected) {

                    resetFilter()
                }
            }
        }
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
            endDate.time       = date.time
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
        typeButtonText                         = getString(R.string.type_expense)
        typeButton            .text            = typeButtonText
    }

    /**
     *  Sets the startDate to very start of current day and endDate to right before the next day.
     */
    private fun resetTime() {

        startDate    = Utils.startOfDay(Date())
        endDate.time = startDate.time
        startUp      = false
    }

    /**
     *  Restores views and updates Reset/Apply buttons.
     *
     *  @param views true = views need to be restored.
     */
    private fun updateUi(views : Boolean) {

        if (views) {

            // restores state of views
            accountSpinner        .isEnabled = accountSelected
            typeButton            .isEnabled = categorySelected
            expenseCategorySpinner.isEnabled = categorySelected
            incomeCategorySpinner .isEnabled = categorySelected
            startDateButton       .isEnabled = dateSelected
            endDateButton         .isEnabled = dateSelected
            applyButton           .text      = applyButtonText
            expenseCategorySpinner.isVisible = typeButtonText == getString(R.string.type_expense)
            incomeCategorySpinner .isVisible = typeButtonText == getString(R.string.type_income)
        }

        // changes visibility of buttons depending if there are filters applied
        if (!accountSelected && !categorySelected && !dateSelected) {

            applyButtonText  = getString(R.string.filter_reset)
            applyButton.text = applyButtonText
        } else {

            applyButtonText  = getString(R.string.filter_apply)
            applyButton.text = applyButtonText
        }

        // changes text on typeButton depending on categorySelected
        if (categorySelected) {

            typeButton.text = typeButtonText
        } else {

            typeButton.text = getString(R.string.filter_type)
        }

        // changes text on dateButtons depending on dateSelected
        if (dateSelected) {

            startDateButton.text = DateFormat.getDateInstance(DateFormat.SHORT).format(startDate)
            endDateButton  .text = DateFormat.getDateInstance(DateFormat.SHORT).format(endDate)
        } else {

            startDateButton.text = getString(R.string.filter_start)
            endDateButton  .text = getString(R.string.filter_end)
        }
    }

    companion object {

        /**
         *  Initializes instance of FilterFragment.
         */
        fun newInstance() : FilterFragment {

            return FilterFragment()
        }
    }
}