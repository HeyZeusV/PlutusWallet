package com.heyzeusv.financeapplication

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.financeapplication.utilities.BaseFragment
import com.heyzeusv.financeapplication.utilities.CurrencyEditText
import com.heyzeusv.financeapplication.utilities.Utils
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.util.*
import kotlin.math.hypot

private const val TAG                 = "TransactionFragment"
private const val ARG_TRANSACTION_ID  = "transaction_id"
private const val ARG_FAB_X           = "fab_X"
private const val ARG_FAB_Y           = "fab_Y"
private const val ARG_FROM_FAB        = "from_fab"
private const val DIALOG_DATE         = "DialogDate"
private const val REQUEST_DATE        = 0
private const val KEY_MAX_ID          = "key_max_id"
private const val KEY_CURRENCY_SYMBOL = "key_currency_symbol"
private const val KEY_DECIMAL_PLACES  = "key_decimal_places"
private const val KEY_SYMBOL_SIDE     = "key_symbol_side"

/**
 *  Shows all the information in database of one fragment and allows users to edit any field and save changes.
 */
class TransactionFragment : BaseFragment(), DatePickerFragment.Callbacks {

    // SharedPreferences
    private lateinit var sp     : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

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
    private lateinit var expenseCategorySpinner : Spinner
    private lateinit var incomeCategorySpinner  : Spinner
    private lateinit var frequencyPeriodSpinner : Spinner
    private lateinit var frequencyText          : TextView
    private lateinit var symbolLeftText         : TextView
    private lateinit var symbolRightText        : TextView
    private lateinit var transaction            : Transaction

    // array holding values for frequency spinner
    private var frequencyArray : Array<String> = arrayOf(FinanceApplication.context!!.getString(R.string.days), FinanceApplication.context!!.getString(R.string.weeks), FinanceApplication.context!!.getString(R.string.months), FinanceApplication.context!!.getString(R.string.years))

    // used with categories
    private var expenseCategoryNamesList : MutableList<String> = mutableListOf()
    private var incomeCategoryNamesList  : MutableList<String> = mutableListOf()
    private var newCategoryName                                = ""
    private var madeNewCategory                                = false

    // used for SharedPreferences
    private var maxId         : Int     = 0
    private var decimalPlaces : Boolean = true
    private var symbolSide    : Boolean = true
    private var symbolKey     : String  = "dollar"

    // used to determine whether to insert a new transaction or updated existing
    private var newTransaction = false

    // false = Expense, true = Income
    private var typeSelected = false

    // used to tell if date has been edited for re-repeating Transactions
    private var dateChanged    = false
    private var transLoaded    = false
    private var oldDate : Date = Utils.startOfDay()

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
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_transaction, container, false)

        repeatingCheckBox      = view.findViewById(R.id.transaction_repeating)        as CheckBox
        expenseChip            = view.findViewById(R.id.transaction_expense_chip)     as Chip
        incomeChip             = view.findViewById(R.id.transaction_income_chip)      as Chip
        typeChipGroup          = view.findViewById(R.id.transaction_type_chips)       as ChipGroup
        transactionLayout      = view.findViewById(R.id.transaction_constraint)       as ConstraintLayout
        totalField             = view.findViewById(R.id.transaction_total)            as CurrencyEditText
        titleField             = view.findViewById(R.id.transaction_title)            as EditText
        memoField              = view.findViewById(R.id.transaction_memo)             as EditText
        frequencyField         = view.findViewById(R.id.transaction_frequency)        as EditText
        saveFab                = view.findViewById(R.id.transaction_save_fab)         as FloatingActionButton
        dateButton             = view.findViewById(R.id.transaction_date)             as MaterialButton
        expenseCategorySpinner = view.findViewById(R.id.transaction_expense_category) as Spinner
        incomeCategorySpinner  = view.findViewById(R.id.transaction_income_category)  as Spinner
        frequencyPeriodSpinner = view.findViewById(R.id.transaction_frequency_period) as Spinner
        frequencyText          = view.findViewById(R.id.frequencyTextView)            as TextView
        symbolLeftText         = view.findViewById(R.id.symbolLeftTextView)           as TextView
        symbolRightText        = view.findViewById(R.id.symbolRightTextView)          as TextView

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

        // retrieves any saved preferences
        sp            = PreferenceManager.getDefaultSharedPreferences(activity)
        decimalPlaces = sp.getBoolean(KEY_DECIMAL_PLACES, true)
        symbolSide    = sp.getBoolean(KEY_SYMBOL_SIDE, true)
        symbolKey     = sp.getString (KEY_CURRENCY_SYMBOL, "dollar")!!
        maxId         = sp.getInt    (KEY_MAX_ID, 0)

        // retrieves symbol to be used according to settings
        val symbol : String = Utils.getCurrencySymbol(symbolKey)
        symbolLeftText  .text = symbol
        symbolRightText .text = symbol

        // if symbol on right side
        if (!symbolSide) {

            // used to change constraints
            val totalConstraintSet = ConstraintSet()
            totalConstraintSet.clone(transactionLayout)
            totalConstraintSet.connect(R.id.transaction_total, ConstraintSet.START,
                                       R.id.totalTextView, ConstraintSet.END, 0)
            totalConstraintSet.connect(R.id.transaction_total, ConstraintSet.END,
                                       R.id.symbolRightTextView, ConstraintSet.START, 0)
            totalConstraintSet.applyTo(transactionLayout)
            // text starts to right
            totalField     .textDirection = View.TEXT_DIRECTION_RTL
            symbolLeftText .isVisible     = false
            symbolRightText.isVisible     = true
        }

        // user selects no decimal places
        if (!decimalPlaces) {

            // filter that prevents user from typing '.' thus only integers
            val filter = object:InputFilter {

                override fun filter(source : CharSequence?, start : Int, end : Int, dest : Spanned?, dstart : Int, dend : Int) : CharSequence? {

                    for (i in start until end) {

                        if (source != null && source == ".") {

                            return ""
                        }
                    }
                    return null
                }
            }
            totalField.filters = arrayOf(filter)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fromFab : Boolean = arguments?.getBoolean(ARG_FROM_FAB) as Boolean

        // register an observer on LiveData instance and tie life to another component
        transactionDetailViewModel.transactionLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { transaction ->
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

        // register an observer on LiveData instance and tie life to another component
        transactionDetailViewModel.expenseCategoryNamesLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { expenseCategoryNames ->
                // if not null
                expenseCategoryNames?.let {
                    expenseCategoryNamesList = expenseCategoryNames.toMutableList()
                    // sorts list in alphabetical order
                    expenseCategoryNamesList.sort()
                    // "Create New Category" will always be at bottom of the list
                    expenseCategoryNamesList.add(getString(R.string.create_category))
                    // sets up the categorySpinner
                    val categorySpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, expenseCategoryNamesList)
                    categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                    expenseCategorySpinner.adapter = categorySpinnerAdapter
                    // if user made a new category, then sets the categorySpinner to new one
                    // else starts the spinner up to ExpenseCategory saved
                    if (madeNewCategory) {

                        expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(newCategoryName))
                    } else {

                        expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(transaction.category))
                    }
                }
            }
        )

        // register an observer on LiveData instance and tie life to another component
        transactionDetailViewModel.incomeCategoryNamesLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { incomeCategoryNames ->
                // if not null
                incomeCategoryNames?.let {
                    incomeCategoryNamesList = incomeCategoryNames.toMutableList()
                    // sorts list in alphabetical order
                    incomeCategoryNamesList.sort()
                    // "Create New Category" will always be at bottom of the list
                    incomeCategoryNamesList.add(getString(R.string.create_category))
                    // sets up the categorySpinner
                    val categorySpinnerAdapter : ArrayAdapter<String> = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, incomeCategoryNamesList)
                    categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                    incomeCategorySpinner.adapter = categorySpinnerAdapter
                    // if user made a new category, then sets the categorySpinner to new one
                    // else starts the spinner up to IncomeCategory saved
                    if (madeNewCategory) {

                        incomeCategorySpinner.setSelection(incomeCategoryNamesList.indexOf(newCategoryName))
                    } else {

                        incomeCategorySpinner.setSelection(incomeCategoryNamesList.indexOf(transaction.category))
                    }
                }
            }
        )

        // only occurs if user wants to create new Transaction
        if (fromFab) {

            maxId += 1
            transaction.id = maxId

            // used for saveFab
            newTransaction = true
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

        expenseCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent : AdapterView<*>?, view : View?, position : Int, id : Long) {

                // only creates AlertDialog if user selects "Create New Category"
                if (parent?.getItemAtPosition(position) == getString(R.string.create_category)) {

                    newCategoryDialog(expenseCategorySpinner)
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
                if (parent?.getItemAtPosition(position) == getString(R.string.create_category)) {

                    newCategoryDialog(incomeCategorySpinner)
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

                transaction.title = getString(R.string.transaction_number) + transaction.id
            }

            // frequency must always be at least 1
            if (transaction.frequency < 1) {

                transaction.frequency = 1
            }

            // sets up Total and totalField
            when {
                totalField.text.toString() != "" -> {

                    // converts the totalField into BigDecimal
                    transaction.total = BigDecimal(
                        totalField.text.toString()
                            .replace(",", "")
                    )
                }
                decimalPlaces -> {

                    transaction.total = BigDecimal(0.00)
                    totalField.setText("0.00")
                }
                else -> {

                    transaction.total = BigDecimal(0)
                    totalField.setText("0")
                }
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

                    // initialize instance of builder
                    val alertDialogBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                        // set title
                        .setTitle(getString(R.string.future_transaction))
                        // set message
                        .setMessage(getString(R.string.future_transaction_warning))
                        // set positive button and click listener
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->

                        transaction.futureTCreated = false
                        launch {

                            transactionDetailViewModel.updateTransaction(transaction)
                        }
                    }
                        // set negative button and click listener
                        .setNegativeButton(getString(R.string.no)) { _, _ ->

                        launch {

                            transactionDetailViewModel.updateTransaction(transaction)
                        }
                    }
                    // make AlertDialog using builder
                    val alertDialog : AlertDialog = alertDialogBuilder.create()
                    // display AlertDialog
                    alertDialog.show()
                // will insert Transaction if it is new, else updates existing
                } else if (newTransaction) {

                    transactionDetailViewModel.insertTransaction(transaction)
                    newTransaction = false
                    // saves maxId into SharedPreferences
                    editor = sp.edit()
                    editor.putInt(KEY_MAX_ID, maxId)
                    editor.apply()
                } else {

                    transactionDetailViewModel.updateTransaction(transaction)
                }
            }
            Log.d(TAG, "$transaction")
            updateUI()
            val savedBar : Snackbar = Snackbar.make(it, getString(R.string.snackbar_saved), Snackbar.LENGTH_SHORT)
            savedBar.anchorView = it
            savedBar.show()
        }
    }

    /**
     *  Will update Transaction date with date selected from DatePickerFragment.
     *
     *  @param date the date selected by the user.
     */
    override fun onDateSelected(date : Date) {

        transaction.date = date
        dateChanged = transaction.date != oldDate
        updateUI()
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

        // reset hour, minutes, seconds and millis to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    @SuppressLint("DefaultLocale")
    @ExperimentalStdlibApi
    /**
     *  Inserts new Category into database or selects it in categorySpinner if it exists already.
     *
     *  @param input the category to be inserted/selected.
     */
    private fun insertCategory(input : String) {

        // makes first letter of every word capital and every other letter lower case
        val name : String = input.split(" ").joinToString(" ") {it.toLowerCase(Locale.US).capitalize(Locale.US)}

        if (!typeSelected) {

            // -1 means it doesn't exist
            if (expenseCategoryNamesList.indexOf(name) == -1) {

                // creates new Category with name
                val newCategory = ExpenseCategory(name)
                launch {

                    transactionDetailViewModel.insertExpenseCategory(newCategory)
                }
                newCategoryName = name
                madeNewCategory = true
            } else {

                expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(name))
            }
        } else if (typeSelected) {

            // -1 means it doesn't exist
            if (incomeCategoryNamesList.indexOf(name) == -1) {

                // creates new Category with name
                val newCategory = IncomeCategory(name)
                launch {

                    transactionDetailViewModel.insertIncomeCategory(newCategory)
                }
                newCategoryName = name
                madeNewCategory = true
            } else {

                incomeCategorySpinner.setSelection(incomeCategoryNamesList.indexOf(name))
            }
        }
    }

    @ExperimentalStdlibApi
    /**
     *  AlertDialog to create new Category.
     *
     *  @param categorySpinner the Spinner that that called this function.
     */
    private fun newCategoryDialog(categorySpinner : Spinner) {

        // initialize instance of Builder
        val builder = MaterialAlertDialogBuilder(context)
            // set title of AlertDialog
            .setTitle(getString(R.string.create_category))
        // inflates view that holds EditText
        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_new_category, view as ViewGroup, false)
        // the EditText to be used
        val input: EditText = viewInflated.findViewById(R.id.category_Input)
        // sets the view
        builder.setView(viewInflated)
            // set positive button and its click listener
            .setPositiveButton(getString(R.string.save)) { _, _ ->

            insertCategory(input.text.toString())
        }
            // set negative button and its click listener
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->

            // users shouldn't be able to save on "Create New Category",
            // this prevents that
            categorySpinner.setSelection(0)
        }
        // make the AlertDialog using the builder
        val categoryAlertDialog : AlertDialog = builder.create()
        // display AlertDialog
        categoryAlertDialog.show()
    }

    /**
     *  Ensures that the UI is up to date with all the correct information on Transaction.
     */
    private fun updateUI() {

        titleField    .setText(transaction.title)
        memoField     .setText(transaction.memo)
        frequencyField.setText(transaction.frequency.toString())
        dateButton.text = DateFormat.getDateInstance(DateFormat.FULL).format(this.transaction.date)

        if (decimalPlaces) {

            totalField.setText(getString(R.string.total_number, String.format(transaction.total.toString())))
        } else {

            totalField.setText(getString(R.string.total_number, String.format(transaction.total.setScale(0, RoundingMode.HALF_UP).toString())))
        }

        if (transaction.type == "Income") {

            typeSelected = true
            expenseCategorySpinner.isVisible = false
            incomeCategorySpinner .isVisible = true
        } else {

            typeSelected = false
            expenseCategorySpinner.isVisible = true
            incomeCategorySpinner .isVisible = false
            // weird bug that only affected expenseChip where it was able to be
            // deselected when Transaction is first started, this fixes it
            expenseChip.isClickable = false
            incomeChip .isClickable = true
        }

        if (!typeSelected) {

            expenseChip.isChecked = true
            incomeChip .isChecked = false
        } else {

            expenseChip.isChecked = false
            incomeChip .isChecked = true
        }

        if (!typeSelected) {

            expenseCategorySpinner.setSelection(expenseCategoryNamesList.indexOf(transaction.category))
        } else {

            incomeCategorySpinner.setSelection(incomeCategoryNamesList.indexOf(transaction.category))
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
                putInt    (ARG_FAB_X         , fabX)
                putInt    (ARG_FAB_Y         , fabY)
                putBoolean(ARG_FROM_FAB      , fromFab)
            }

            return TransactionFragment().apply {

                arguments = args
            }
        }
    }
}