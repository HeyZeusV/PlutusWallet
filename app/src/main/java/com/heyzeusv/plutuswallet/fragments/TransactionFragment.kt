package com.heyzeusv.plutuswallet.fragments

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.Transaction
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.utilities.CurrencyEditText
import com.heyzeusv.plutuswallet.utilities.Utils
import com.heyzeusv.plutuswallet.viewmodels.TransactionDetailViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Date
import java.util.Calendar
import kotlin.math.hypot

private const val TAG                 = "PWTransactionFragment"
private const val ARG_TRANSACTION_ID  = "transaction_id"
private const val ARG_FAB_X           = "fab_X"
private const val ARG_FAB_Y           = "fab_Y"
private const val ARG_FROM_FAB        = "from_fab"
private const val DIALOG_DATE         = "DialogDate"
private const val REQUEST_DATE        = 0

/**
 *  Shows all the information in database of one Transaction and allows users to
 *  edit any field and save changes.
 */
class TransactionFragment : BaseFragment(), DatePickerFragment.Callbacks {

    // views
    private lateinit var repeatingCheckBox      : CheckBox
    private lateinit var expenseChip            : Chip
    private lateinit var incomeChip             : Chip
    private lateinit var typeChipGroup          : ChipGroup
    private lateinit var transactionLayout      : ConstraintLayout
    private lateinit var totalField             : CurrencyEditText
    private lateinit var titleField             : EditText
    private lateinit var memoField              : EditText
    private lateinit var frequencyField         : EditText
    private lateinit var saveFab                : FloatingActionButton
    private lateinit var dateButton             : MaterialButton
    private lateinit var accountSpinner         : Spinner
    private lateinit var expenseCategorySpinner : Spinner
    private lateinit var incomeCategorySpinner  : Spinner
    private lateinit var frequencyPeriodSpinner : Spinner
    private lateinit var frequencyText          : TextView
    private lateinit var symbolLeftText         : TextView
    private lateinit var symbolRightText        : TextView
    private lateinit var transaction            : Transaction

    // array holding values for frequency spinner
    private lateinit var frequencyArray : Array<String>

    // used with accounts/categories
    private var accountNameList          : MutableList<String> = mutableListOf()
    private var expenseCategoryNamesList : MutableList<String> = mutableListOf()
    private var incomeCategoryNamesList  : MutableList<String> = mutableListOf()

    private var maxId              : Int     = 0

    // used to determine whether to insert a new transaction or updated existing
    private var newTransaction = false

    // false = Expense, true = Income
    private var typeSelected = false

    // used to tell if date has been edited for re-repeating Transactions
    private var dateChanged    = false
    private var transLoaded    = false
    private var oldDate : Date = Utils.startOfDay(Date())

    // adapters to be used on Spinners
    private var accountSpinnerAdapter : ArrayAdapter<String>? = null
    private var expenseSpinnerAdapter : ArrayAdapter<String>? = null
    private var incomeSpinnerAdapter  : ArrayAdapter<String>? = null

    // provides instance of ViewModel
    private val transactionDetailViewModel : TransactionDetailViewModel by lazy {
        ViewModelProviders.of(this).get(TransactionDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transaction = Transaction()

        // retrieves arguments passed on (if any)
        val transactionId : Int = arguments?.getInt(ARG_TRANSACTION_ID) as Int
        transactionDetailViewModel.loadTransaction(transactionId)

        // initialize array with Resource strings for localization
        frequencyArray = arrayOf(getString(R.string.period_days), getString(R.string.period_weeks), getString(R.string.period_months), getString(R.string.period_years))
    }

    @SuppressLint("RtlHardcoded")
    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_transaction, container, false)

        // initialize views
        repeatingCheckBox      = view.findViewById(R.id.transaction_repeating       ) as CheckBox
        expenseChip            = view.findViewById(R.id.transaction_expense_chip    ) as Chip
        incomeChip             = view.findViewById(R.id.transaction_income_chip     ) as Chip
        typeChipGroup          = view.findViewById(R.id.transaction_type_chips      ) as ChipGroup
        transactionLayout      = view.findViewById(R.id.transaction_constraint      ) as ConstraintLayout
        totalField             = view.findViewById(R.id.transaction_total           ) as CurrencyEditText
        titleField             = view.findViewById(R.id.transaction_title           ) as EditText
        memoField              = view.findViewById(R.id.transaction_memo            ) as EditText
        frequencyField         = view.findViewById(R.id.transaction_frequency       ) as EditText
        saveFab                = view.findViewById(R.id.transaction_save_fab        ) as FloatingActionButton
        dateButton             = view.findViewById(R.id.transaction_date            ) as MaterialButton
        accountSpinner         = view.findViewById(R.id.transaction_account         ) as Spinner
        expenseCategorySpinner = view.findViewById(R.id.transaction_expense_category) as Spinner
        incomeCategorySpinner  = view.findViewById(R.id.transaction_income_category ) as Spinner
        frequencyPeriodSpinner = view.findViewById(R.id.transaction_frequency_period) as Spinner
        frequencyText          = view.findViewById(R.id.frequencyTextView           ) as TextView
        symbolLeftText         = view.findViewById(R.id.symbolLeftTextView          ) as TextView
        symbolRightText        = view.findViewById(R.id.symbolRightTextView         ) as TextView

        // set up for the frequencyPeriodSpinner
        val frequencyPeriodSpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!, R.layout.spinner_item, frequencyArray)
        frequencyPeriodSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        frequencyPeriodSpinner       .adapter = frequencyPeriodSpinnerAdapter

        // checks to see how user arrived to TransactionFragment
        val fromFab : Boolean = arguments?.getBoolean(ARG_FROM_FAB) as Boolean
        if (fromFab) {

            // animation that plays on user presses FAB
            view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {

                override fun onLayoutChange(v : View, left : Int, top : Int, right : Int, bottom : Int, oldLeft : Int, oldTop : Int, oldRight : Int, oldButton : Int) {

                    v.removeOnLayoutChangeListener(this)
                    val fabX        : Int      = arguments?.getInt(ARG_FAB_X) as Int
                    val fabY        : Int      = arguments?.getInt(ARG_FAB_Y) as Int
                    val finalRadius : Float    = hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
                    // view, initialX, initialY, startingRadius, endRadius
                    val anim        : Animator = ViewAnimationUtils.createCircularReveal(v, fabX, fabY, 10.0F, finalRadius)
                    // time to complete animation
                    anim.duration              = 1000
                    anim.start()
                    updateUI()
                }
            })
        }

        // if symbol on right side
        if (!symbolSide) {

            // used to change constraints
            val totalConstraintSet = ConstraintSet()
            totalConstraintSet.clone(transactionLayout)
            totalConstraintSet.connect(
                R.id.transaction_total, ConstraintSet.START,
                R.id.totalTextView    , ConstraintSet.END  , 0)
            totalConstraintSet.connect(
                R.id.transaction_total  , ConstraintSet.END  ,
                R.id.symbolRightTextView, ConstraintSet.START, 0)
            totalConstraintSet.applyTo(transactionLayout)
            // text starts to right
            totalField     .gravity    = Gravity.RIGHT
            symbolLeftText .isVisible  = false
            symbolRightText.isVisible  = true
        }

        // user selects no decimal places
        if (!decimalPlaces) {

            // filter that prevents user from typing decimalSymbol thus only integers
            val filter = object : InputFilter {

                override fun filter(source : CharSequence?, start : Int, end : Int, dest : Spanned?, dstart : Int, dend : Int) : CharSequence? {

                    for (i : Int in start until end) {

                        if (source != null && source == decimalSymbol.toString()) {

                            return ""
                        }
                    }
                    return null
                }
            }
            totalField.filters += filter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fromFab : Boolean = arguments?.getBoolean(ARG_FROM_FAB) as Boolean

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        transactionDetailViewModel.transactionLiveData.observe(this, Observer { transaction : Transaction? ->
                // if not null
                transaction?.let {
                    this.transaction = transaction
                    // only saves oldDate once rather than at every update
                    if (!transLoaded) {

                        oldDate = transaction.date
                    }
                    updateUI()
                    transLoaded = true
                }
            }
        )

        launch {

            //retrieves list of Accounts from database
            accountNameList = transactionDetailViewModel.getAccountsAsync().await().toMutableList()
            // sorts list in alphabetical order
            accountNameList.sort()
            // "Create New Account will always be at bottom of list
            accountNameList.add(getString(R.string.account_create))
            // sets up the accountSpinner
            accountSpinnerAdapter = ArrayAdapter(context!!, R.layout.spinner_item, accountNameList)
            accountSpinnerAdapter!!.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            accountSpinner         .adapter = accountSpinnerAdapter
            // sets selection of spinner
            accountSpinner.setSelection(if (transaction.account == "") {

                0
            } else {

                accountNameList.indexOf(transaction.account)
            })

            // retrieves list of Expense Categories from database
            expenseCategoryNamesList = transactionDetailViewModel.getCategoriesByTypeAsync("Expense").await().toMutableList()
            // "Create New Category" will always be at bottom of the list
            expenseCategoryNamesList.add(getString(R.string.category_create))
            // sets up the categorySpinner
            expenseSpinnerAdapter = ArrayAdapter(context!!, R.layout.spinner_item, expenseCategoryNamesList)
            expenseSpinnerAdapter!!.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            expenseCategorySpinner .adapter = expenseSpinnerAdapter

            // retrieves list of Income Categories from database
            incomeCategoryNamesList = transactionDetailViewModel.getCategoriesByTypeAsync("Income").await().toMutableList()
            // "Create New Category" will always be at bottom of the list
            incomeCategoryNamesList.add(getString(R.string.category_create))
            // sets up the categorySpinner
            incomeSpinnerAdapter = ArrayAdapter(context!!, R.layout.spinner_item, incomeCategoryNamesList)
            incomeSpinnerAdapter!!.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            incomeCategorySpinner .adapter = incomeSpinnerAdapter

            // sets up correct Spinner to Category that is stored in Transaction (if any)
            if (transaction.type == "Expense") {

                // is -1 when user had language other than English at first run
                if (expenseCategoryNamesList.indexOf(transaction.category) != -1) {

                    expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(transaction.category))
                } else {

                    expenseCategorySpinner.setSelection(0)
                }
            } else {

                // is -1 when user had language other than English at first run
                if (incomeCategoryNamesList.indexOf(transaction.category) != -1) {

                    incomeCategorySpinner.setSelection(incomeCategoryNamesList.indexOf(transaction.category))
                } else {

                    incomeCategorySpinner.setSelection(0)
                }
            }

            // retrieves maxId or 0 if null
            maxId = transactionDetailViewModel.getMaxIdAsync().await() ?: 0

            // only occurs if user wants to create new Transaction
            if (fromFab) {

                maxId += 1
                transaction.id = maxId

                // used for saveFab
                newTransaction = true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @ExperimentalStdlibApi
    override fun onStart() {
        super.onStart()

        // placed in onStart due to being triggered when view state is restored
        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(sequence : CharSequence?, start : Int, count : Int, after : Int) {}

            override fun onTextChanged(
                sequence : CharSequence?, start : Int, before : Int, count : Int) {

                transaction.title = sequence.toString()
            }

            override fun afterTextChanged(sequence : Editable?) {}
        }

        // placed in onStart due to being triggered when view state is restored
        val memoWatcher = object : TextWatcher {

            override fun beforeTextChanged(sequence : CharSequence?, start : Int, count : Int, after : Int) {}

            override fun onTextChanged(
                sequence : CharSequence?, start : Int, before : Int, count : Int) {

                transaction.memo = sequence.toString()
            }

            override fun afterTextChanged(sequence : Editable?) {}
        }

        // placed in onStart due to being triggered when view state is restored
        val frequencyWatcher = object : TextWatcher {

            override fun beforeTextChanged(sequence : CharSequence?, start : Int, count : Int, after : Int) {}

            override fun onTextChanged(
                sequence : CharSequence?, start : Int, before : Int, count : Int) {

                try {

                    transaction.frequency = Integer.parseInt(sequence.toString())
                } catch (e : NumberFormatException) {

                    transaction.frequency = 1
                }
            }

            override fun afterTextChanged(sequence : Editable?) {}
        }

        titleField    .addTextChangedListener(titleWatcher)
        memoField     .addTextChangedListener(memoWatcher)
        frequencyField.addTextChangedListener(frequencyWatcher)

        // OnClickListener not affected by state restoration, but nice to have listeners in one place
        dateButton.setOnClickListener {

            DatePickerFragment.newInstance(transaction.date).apply {

                // fragment that will be target and request code
                setTargetFragment(this@TransactionFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@TransactionFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        typeChipGroup.setOnCheckedChangeListener { group, checkedId ->

            // prevents no chips being selected
            for (i : Int in 0 until group.childCount) {

                val chip : View = group.getChildAt(i)
                chip.isClickable = chip.id != group.checkedChipId
            }

            when (checkedId) {

                R.id.transaction_expense_chip -> {

                    typeSelected                     = false
                    expenseCategorySpinner.isVisible = true
                    incomeCategorySpinner .isVisible = false
                    transaction           .type      = "Expense"
                }
                R.id.transaction_income_chip -> {

                    typeSelected                     = true
                    expenseCategorySpinner.isVisible = false
                    incomeCategorySpinner .isVisible = true
                    transaction           .type      = "Income"
                }
            }
        }

        accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent : AdapterView<*>?, view : View?, position : Int, id : Long) {

                if (position == accountNameList.size - 1) {

                    createDialog(accountSpinner, 0)
                }
                // updates account to selected one
                transaction.account = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        expenseCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent : AdapterView<*>?, view : View?, position : Int, id : Long) {

                // only creates AlertDialog if user selects "Create New Category"
                if (position == expenseCategoryNamesList.size - 1) {

                    createDialog(expenseCategorySpinner, 1)
                }
                // updates the category to selected one
                transaction.category = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent : AdapterView<*>?) {}
        }

        incomeCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent : AdapterView<*>?, view : View?, position : Int, id : Long) {

                // only creates AlertDialog if user selects "Create New Category"
                if (position == incomeCategoryNamesList.size - 1) {

                    createDialog(incomeCategorySpinner, 1)
                }
                // updates the category to selected one
                transaction.category = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent : AdapterView<*>?) {}
        }

        repeatingCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->
                transaction.repeating = isChecked
                if (isChecked) {

                    frequencyText         .isVisible = true
                    frequencyField        .isVisible = true
                    frequencyPeriodSpinner.isVisible = true
                } else {

                    frequencyText         .isVisible = false
                    frequencyField        .isVisible = false
                    frequencyPeriodSpinner.isVisible = false
                }
            }
        }

        frequencyPeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent : AdapterView<*>?, view : View?, position : Int, id : Long) {

                transaction.period = position
            }

            override fun onNothingSelected(parent : AdapterView<*>?) {}
        }

        saveFab.setOnClickListener {

            // gives Transaction simple title if user doesn't enter any
            if (transaction.title.trim().isEmpty()) {

                transaction.title = getString(R.string.transaction_empty_title) + transaction.id
            }

            // frequency must always be at least 1
            if (transaction.frequency < 1) {

                transaction.frequency = 1
            }

            // sets up Total and totalField
            if (totalField.text.toString() != "") {

                // need to save to separate string or else format will be ruined after saving
                var savedTotal : String = totalField.text.toString()
                savedTotal = savedTotal.replace(thousandsSymbol.toString(), "" )
                    .replace(decimalSymbol  .toString(), ".")
                // converts the totalField into BigDecimal
                transaction.total = BigDecimal(savedTotal)
            }


            // deals with either creating, updating, or deleting a future Transaction
            if (transaction.repeating) {

                transaction.futureDate = createFutureDate()
            } else {

                // essentially 'deletes' it since Long.MAX_VALUE is a VERY long time in the future
                transaction.futureDate = Date(Long.MAX_VALUE)
            }

            launch {

                // AlertDialog that asks user if they want Transaction to repeat again
                if (transaction.futureTCreated && dateChanged && transaction.repeating) {

                    val posFun = DialogInterface.OnClickListener { _, _ ->

                        transaction.futureTCreated = false
                        updateTransaction(transaction)
                        createSnackbar(it)
                    }
                    val negFun = DialogInterface.OnClickListener { _, _ ->

                        dateChanged = false
                        updateTransaction(transaction)
                        createSnackbar(it)
                    }

                    AlertDialogCreator.alertDialog(context!!,
                        getString(R.string.alert_dialog_future_transaction),
                        getString(R.string.alert_dialog_future_transaction_warning),
                        getString(R.string.alert_dialog_yes), posFun,
                        getString(R.string.alert_dialog_no), negFun)

                // will insert Transaction if it is new, else updates existing
                } else if (newTransaction) {

                    transactionDetailViewModel.insertTransaction(transaction)
                    newTransaction = false
                    createSnackbar(it)
                } else {

                    transactionDetailViewModel.updateTransaction(transaction)
                    createSnackbar(it)
                }
            }
            updateUI()
        }
    }

    /**
     *  Creates AlertDialog when user selects "Create New ..."
     *
     *  @param spinner Spinner to be applied to.
     *  @param type    0 = Account, 1 = Category.
     */
    @ExperimentalStdlibApi
    private fun createDialog(spinner : Spinner, type : Int) {

        // inflates view that holds EditText
        val viewInflated : View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
        // the EditText to be used
        val input : EditText = viewInflated.findViewById(R.id.dialog_input)

        val title : String = when (type) {

            0    -> getString(R.string.account_create)
            else -> getString(R.string.category_create)
        }

        // Listeners
        val cancelListener = DialogInterface.OnCancelListener {

            spinner.setSelection(0)
        }
        val negListener = DialogInterface.OnClickListener { _, _ ->

            spinner.setSelection(0)
        }
        val posListener = DialogInterface.OnClickListener { _, _ ->

            when (type) {

                0    -> insertAccount(input.text.toString())
                else -> insertCategory(input.text.toString())
            }
        }

        AlertDialogCreator.alertDialogInputCancelable(context!!,
            title,
            viewInflated,
            getString(R.string.alert_dialog_save), posListener,
            getString(R.string.alert_dialog_cancel), negListener,
            cancelListener)
    }

    override fun onResume() {
        super.onResume()

        // symbol side setting might have changed
        symbolLeftText .text = currencySymbol
        symbolRightText.text = currencySymbol
    }

    /**
     *  Adds frequency * period to the date on Transaction.
     */
    private fun createFutureDate() : Date {

        val calendar : Calendar = Calendar.getInstance()
        // set to Transaction date rather than current time due to Users being able
        // to select a Date in the past or future
        calendar.time = transaction.date

        // 0 = Day, 1 = Week, 2 = Month, 3 = Year
        when (transaction.period) {

            0 -> calendar.add(Calendar.DAY_OF_MONTH, transaction.frequency)
            1 -> calendar.add(Calendar.WEEK_OF_YEAR, transaction.frequency)
            2 -> calendar.add(Calendar.MONTH       , transaction.frequency)
            3 -> calendar.add(Calendar.YEAR        , transaction.frequency)
        }

        return Utils.startOfDay(calendar.time)
    }

    /**
     *  Snackbar alerting user that Transaction has been saved.
     *
     *  @param view the view that Snackbar will be anchored to.
     */
    private fun createSnackbar(view : View) {

        val savedBar : Snackbar = Snackbar.make(view,
            getString(R.string.snackbar_saved), Snackbar.LENGTH_SHORT)
        savedBar.anchorView = view
        savedBar.show()
    }

    /**
     *  Inserts new Account into list or selects it in categorySpinner if it exists already.
     *
     *  @param name the Account to be inserted/selected.
     */
    @ExperimentalStdlibApi
    private fun insertAccount(name : String) {


        // -1 means it doesn't exist
        if (accountNameList.indexOf(name) == -1) {

            // creates new Account with name
            launch {

                val account = Account(0, name)
                transactionDetailViewModel.insertAccount(account)
            }

            // adds new Account to list, sorts list, ensures "Create New Account" appears at bottom,
            // updates SpinnerAdapter, and sets Spinner to new Account
            accountNameList.remove(getString(R.string.account_create))
            accountNameList.add(name)
            accountNameList.sort()
            accountNameList.add(getString(R.string.account_create))
            accountSpinnerAdapter!!.notifyDataSetChanged()
        }

        accountSpinner.setSelection(accountNameList.indexOf(name))

        transaction.account = name
    }

    /**
     *  Inserts new Category into database or selects it in categorySpinner if it exists already.
     *
     *  @param name the category to be inserted/selected.
     */
    @ExperimentalStdlibApi
    private fun insertCategory(name : String) {

        if (!typeSelected) {

            // -1 means it doesn't exist
            if (expenseCategoryNamesList.indexOf(name) == -1) {

                // creates new Category with name
                launch {

                    val category = Category(0, name, "Expense")
                    transactionDetailViewModel.insertCategory(category)
                }
                // adds new Category to list, sorts list, ensures "Create New Category" appears at bottom,
                // updates SpinnerAdapter, and sets Spinner to new Category
                expenseCategoryNamesList.remove(getString(R.string.category_create))
                expenseCategoryNamesList.add(name)
                expenseCategoryNamesList.sort()
                expenseCategoryNamesList.add(getString(R.string.category_create))
                expenseSpinnerAdapter!!.notifyDataSetChanged()
            }
            expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(name))

        } else if (typeSelected) {

            // -1 means it doesn't exist
            if (incomeCategoryNamesList.indexOf(name) == -1) {

                // creates new Category with name
                launch {

                    val category = Category(0, name, "Income")
                    transactionDetailViewModel.insertCategory(category)
                }
                // adds new Category to list, sorts list, ensures "Create New Category" appears at bottom,
                // updates SpinnerAdapter, and sets Spinner to new Category
                incomeCategoryNamesList.remove(getString(R.string.category_create))
                incomeCategoryNamesList.add(name)
                incomeCategoryNamesList.sort()
                incomeCategoryNamesList.add(getString(R.string.category_create))
                incomeSpinnerAdapter!!.notifyDataSetChanged()
            }
            incomeCategorySpinner.setSelection(incomeCategoryNamesList.indexOf(name))
        }
        transaction.category = name
    }

    /**
     *  Will update Transaction date with date selected from DatePickerFragment.
     *
     *  @param date the date selected by the user.
     */
    override fun onDateSelected(date : Date) {

        transaction.date = date
        dateButton .text = DateFormat.getDateInstance(dateFormat).format(this.transaction.date)
        dateChanged      = transaction.date != oldDate
    }

    /**
     *  Used in AlertDialog that appears after user switches date of Transaction
     *  that has been repeated. It appears CoRoutines do not work directly in AlertDialog.
     *
     *  @param transaction The Transaction to be updated.
     */
    private fun updateTransaction(transaction : Transaction) {

        launch {

            transactionDetailViewModel.updateTransaction(transaction)
        }
    }

    /**
     *  Ensures that the UI is up to date with all the correct information on Transaction.
     */
    private fun updateUI() {

        titleField    .setText(transaction.title)
        memoField     .setText(transaction.memo)
        frequencyField.setText(transaction.frequency.toString())
        dateButton.text = DateFormat.getDateInstance(dateFormat).format(this.transaction.date)

        // formats Total depending on decimalPlaces and if decimal separator is present
        totalField.setText(getString(R.string.total_number, if (decimalPlaces && transaction.total.toString().contains(".")) {

            Utils.formatDecimal(transaction.total.toString(), thousandsSymbol, decimalSymbol)
        } else {

            Utils.formatInteger(transaction.total.toString(), thousandsSymbol)
        }))

        accountSpinner.setSelection(accountNameList.indexOf(transaction.account))

        if (transaction.type == "Income") {

            typeSelected                     = true
            expenseCategorySpinner.isVisible = false
            incomeCategorySpinner .isVisible = true
            expenseChip           .isChecked = false
            incomeChip            .isChecked = true
            incomeCategorySpinner .setSelection(incomeCategoryNamesList.indexOf(transaction.category))
        } else {

            typeSelected                     = false
            expenseCategorySpinner.isVisible = true
            incomeCategorySpinner .isVisible = false
            expenseChip           .isChecked = true
            incomeChip            .isChecked = false
            expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(transaction.category))
            // weird bug that only affected expenseChip where it was able to be
            // deselected when Transaction is first started, this fixes it
            expenseChip.isClickable = false
            incomeChip .isClickable = true
        }

        repeatingCheckBox.apply {
            isChecked = transaction.repeating
            // skips animation
            jumpDrawablesToCurrentState()
        }
        frequencyPeriodSpinner.setSelection(transaction.period)
    }

    companion object {

        /**
         *  Initializes instance of TransactionFragment.
         *
         *  Creates arguments Bundle, creates a Fragment instance, and attaches the
         *  arguments to the Fragment.
         *
         *  @param transactionId id of Transaction selected.
         *  @param fabX          the x position of FAB
         *  @param fabY          the y position of FAB
         *  @param fromFab       true if user clicked on FAB to create Transaction.
         */
        fun newInstance(transactionId : Int, fabX : Int, fabY : Int, fromFab : Boolean) : TransactionFragment {

            val args : Bundle = Bundle().apply {

                putInt    (ARG_TRANSACTION_ID, transactionId)
                putInt    (ARG_FAB_X         , fabX         )
                putInt    (ARG_FAB_Y         , fabY         )
                putBoolean(ARG_FROM_FAB      , fromFab      )
            }

            return TransactionFragment().apply {

                arguments = args
            }
        }
    }
}