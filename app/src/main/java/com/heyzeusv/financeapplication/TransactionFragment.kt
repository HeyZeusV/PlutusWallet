package com.heyzeusv.financeapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.heyzeusv.financeapplication.utilities.BaseFragment
import com.heyzeusv.financeapplication.utilities.CurrencyEditText
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DateFormat
import java.util.*
import kotlin.math.hypot

private const val TAG                = "TransactionFragment"
private const val ARG_TRANSACTION_ID = "transaction_id"
private const val ARG_FAB_X          = "fab_X"
private const val ARG_FAB_Y          = "fab_Y"
private const val ARG_FROM_FAB       = "from_fab"
private const val DIALOG_DATE        = "DialogDate"
private const val REQUEST_DATE       = 0

class TransactionFragment : BaseFragment(), DatePickerFragment.Callbacks {

    // views
    private lateinit var transaction            : Transaction
    private lateinit var titleField             : EditText
    private lateinit var memoField              : EditText
    private lateinit var frequencyField         : EditText
    private lateinit var totalField             : CurrencyEditText
    private lateinit var dateButton             : Button
    private lateinit var repeatingCheckBox      : CheckBox
    private lateinit var categorySpinner        : Spinner
    private lateinit var frequencyPeriodSpinner : Spinner
    private lateinit var frequencyText          : TextView
    private lateinit var saveFab                : FloatingActionButton


    // arrays holding values for frequency spinner
    private var frequencyArray = arrayOf("Day(s)", "Week(s)", "Month(s)", "Year(s)")

    // used with categories
    private var categoryNamesList = mutableListOf<String>()
    private var newCategoryName = ""
    private var madeNewCategory = false

    // used to determine whether to insert a new transaction or updated existing
    private var newTransaction = false

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

    override fun onCreateView(
        inflater : LayoutInflater,
        container : ViewGroup?,
        savedInstanceState : Bundle?
    ) : View? {

        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        
        titleField             = view.findViewById(R.id.transaction_title)            as EditText
        memoField              = view.findViewById(R.id.transaction_memo)             as EditText
        frequencyField         = view.findViewById(R.id.transaction_frequency)        as EditText
        totalField             = view.findViewById(R.id.transaction_total)            as CurrencyEditText
        dateButton             = view.findViewById(R.id.transaction_date)             as Button
        repeatingCheckBox      = view.findViewById(R.id.transaction_repeating)        as CheckBox
        categorySpinner        = view.findViewById(R.id.transaction_category)         as Spinner
        frequencyPeriodSpinner = view.findViewById(R.id.transaction_frequency_period) as Spinner
        frequencyText          = view.findViewById(R.id.frequencyTextView)            as TextView
        saveFab                = view.findViewById(R.id.transaction_save_fab)         as FloatingActionButton

        // set up for the frequencyPeriodSpinner
        val frequencyPeriodSpinnerAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, frequencyArray)
        frequencyPeriodSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        frequencyPeriodSpinner.adapter = frequencyPeriodSpinnerAdapter

        // checks to see how user arrived to TransactionFragment
        val fromFab : Boolean = arguments?.getBoolean(ARG_FROM_FAB) as Boolean
        if (fromFab) {

            // animation that plays on user presses FAB
            view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {

                override fun onLayoutChange(
                    v: View, left: Int, top: Int, right: Int,
                    bottom: Int, oldLeft: Int, oldTop: Int,
                    oldRight: Int, oldButton: Int
                ) {

                    v.removeOnLayoutChangeListener(this)
                    val fabX: Int = arguments?.getInt(ARG_FAB_X) as Int
                    val fabY: Int = arguments?.getInt(ARG_FAB_Y) as Int
                    val finalRadius = hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
                    val anim = ViewAnimationUtils.createCircularReveal(
                        v, fabX, fabY, 10.0F, finalRadius)
                    anim.duration = 1000
                    anim.start()
                    updateUI()
                }
            })
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
                    updateUI()
                }
            }
        )

        // register an observer on LiveData instance and tie life to another component
        transactionDetailViewModel.categoryNamesLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { categoryNames ->
                // if not null
                categoryNames?.let {
                    categoryNamesList = categoryNames.toMutableList()
                    // "Create New Category will always be at bottom of the list
                    categoryNamesList.remove("Create New Category")
                    categoryNamesList.sort()
                    categoryNamesList.add("Create New Category")
                    // sets up the categorySpinner
                    val categorySpinnerAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, categoryNamesList)
                    categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                    categorySpinner.adapter = categorySpinnerAdapter
                    // if user made a new category, then sets the categorySpinner to new one
                    // else starts the spinner up to Category saved
                    if (madeNewCategory) {

                        categorySpinner.setSelection(categoryNamesList.indexOf(newCategoryName))
                    } else {
                        categorySpinner.setSelection(categoryNamesList.indexOf(transaction.category))
                    }
                }
            }
        )

        // only occurs if user wants to create new Transaction
        if (fromFab) {

            launch {

                // returns the highest id in Transaction table
                var maxId = transactionDetailViewModel.getMaxIdAsync().await()
                // should only run if Transaction table is empty
                if (maxId == null) {

                    maxId = 0
                }
                // id is primary key, so have to increase it
                transaction.id = maxId.plus(1)
            }
            // used for saveFab
            newTransaction = true
        }
    }

    @ExperimentalStdlibApi
    override fun onStart() {
        super.onStart()

        // placed in onStart due to being triggered when view state is restored
        val titleWatcher = object : TextWatcher {

            // not needed so blank
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

            // sequence is user's input which title is changed to
            override fun onTextChanged(
                sequence : CharSequence?, start : Int, before : Int, count : Int) {

                transaction.title = sequence.toString()
            }

            // not needed so blank
            override fun afterTextChanged(sequence: Editable?) {}
        }

        // placed in onStart due to being triggered when view state is restored
        val memoWatcher = object : TextWatcher {

            // not needed so blank
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

            // sequence is user's input which memo is changed to
            override fun onTextChanged(
                sequence : CharSequence?, start : Int, before : Int, count : Int) {

                transaction.memo = sequence.toString()
            }

            // not needed so blank
            override fun afterTextChanged(sequence: Editable?) {}
        }

        // placed in onStart due to being triggered when view state is restored
        val frequencyWatcher = object : TextWatcher {

            // not needed so blank
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

            // sequence is user's input which memo is changed to
            override fun onTextChanged(
                sequence : CharSequence?, start : Int, before : Int, count : Int) {

                try {
                    transaction.frequency = Integer.parseInt(sequence.toString())
                } catch (e : NumberFormatException) {

                    transaction.frequency = 0
                }
            }

            // not needed so blank
            override fun afterTextChanged(sequence: Editable?) {}
        }

        titleField    .addTextChangedListener(titleWatcher)
        memoField     .addTextChangedListener(memoWatcher)
        frequencyField.addTextChangedListener(frequencyWatcher)

        // OnClickListener not affected by state restoration,
        // but nice to have listeners in one place
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(transaction.date).apply {
                // fragment that will be target and request code
                setTargetFragment(this@TransactionFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@TransactionFragment.requireFragmentManager(), DIALOG_DATE)
            }
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
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                // only creates AlertDialog if user selects "Create New Category"
                if (parent?.getItemAtPosition(position) == "Create New Category") {

                    // initialize instance of Builder
                    val builder = AlertDialog.Builder(context)
                    // set title of AlertDialog
                    builder.setTitle("Create new category")
                    // inflates view that holds EditText
                    val viewInflated: View = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_new_category, getView() as ViewGroup, false)
                    // the EditText to be used
                    val input: EditText = viewInflated.findViewById(R.id.category_Input)
                    // sets the view
                    builder.setView(viewInflated)
                    // set positive button and its click listener
                    builder.setPositiveButton("Save") { _, _ ->

                        insertCategory(input.text.toString())
                    }
                    // set negative button and its click listener
                    builder.setNegativeButton("Cancel") { _, _ ->

                        // users shouldn't be able to save on "Create New Category",
                        // this prevents that
                        categorySpinner.setSelection(0)
                    }
                    // make the AlertDialog using the builder
                    val categoryAlertDialog: AlertDialog = builder.create()
                    // display AlertDialog
                    categoryAlertDialog.show()
                }
                // updates the category to selected one
                transaction.category = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        frequencyPeriodSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    transaction.period = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        saveFab.setOnClickListener {

            // gives Transactions simple titles if user doesn't enter any
            if (transaction.title == "") {

                transaction.title = "Transaction #" + transaction.id
            }

            // frequency must always be at least 1
            if (transaction.frequency < 1) {

                transaction.frequency = 1
            }

            try {
                transaction.total = BigDecimal(
                    totalField.text.toString()
                        .replace("$", "").replace(",", "")
                )
            } catch (e : java.lang.NumberFormatException) {

                transaction.total = BigDecimal("0.00")
            }

            if (transaction.repeating) {

                createFutureTransaction()
            } else {

                deleteFutureTransaction()
            }

            launch {

                // will insert Transaction if it was new, else update existing
                if (newTransaction) {

                    transactionDetailViewModel.insertTransaction(transaction)
                    newTransaction = false
                } else {

                    transactionDetailViewModel.updateTransaction(transaction)
                }
            }

            updateUI()
        }
    }

    private fun updateUI() {

        titleField    .setText(transaction.title)
        memoField     .setText(transaction.memo)
        totalField    .setText("$" + String.format(transaction.total.toString()))
        frequencyField.setText(transaction.frequency.toString())
        dateButton.text = DateFormat.getDateInstance(DateFormat.FULL).format(this.transaction.date)
        categorySpinner.setSelection(categoryNamesList.indexOf(transaction.category))
        repeatingCheckBox.apply {
            isChecked = transaction.repeating
            // skips animation
            jumpDrawablesToCurrentState()
        }
        frequencyPeriodSpinner.setSelection(transaction.period)
    }

    // will update date with the date selected from DatePickerFragment
    override fun onDateSelected(date : Date) {

        transaction.date = date
        updateUI()
    }

    // creates or updates FutureTransaction
    private fun createFutureTransaction() {

        val futureDate : Date = createFutureDate()
        launch {

            var futureTransaction : FutureTransaction? =
                transactionDetailViewModel.getFutureTransactionAsync(transaction.id).await()

            // means current Transaction has no FutureTransaction created for it
            if (futureTransaction == null) {

                futureTransaction = FutureTransaction(transaction.id, transaction.id, futureDate)
                transactionDetailViewModel.insertFutureTransaction(futureTransaction)
            // there is a FutureTransaction for current Transaction so update new futureDate
            } else {

                futureTransaction.futureDate = futureDate
                transactionDetailViewModel.updateFutureTransaction(futureTransaction)
            }
        }
    }

    // adds frequency * period to the date on Transaction
    private fun createFutureDate() : Date {

        val calendar : Calendar = Calendar.getInstance()
        // set to Transaction date rather than current time due to Users being able
        // to select a Date in the past or future
        calendar.time = transaction.date

        //0 = Day(s), 1 = Week(s), 2 = Month(s), 3 = Year(s)
        when (transaction.period) {

            0 -> calendar.add(Calendar.DAY_OF_MONTH, transaction.frequency)
            1 -> calendar.add(Calendar.WEEK_OF_YEAR, transaction.frequency)
            2 -> calendar.add(Calendar.MONTH       , transaction.frequency)
            3 -> calendar.add(Calendar.YEAR        , transaction.frequency)
        }
        return calendar.time
    }

    // deletes FutureTransaction for this Transaction, if any
    private fun deleteFutureTransaction() {

        launch {

            val futureTransaction : FutureTransaction? =
                transactionDetailViewModel.getFutureTransactionAsync(transaction.id).await()

            if (futureTransaction != null) {

                transactionDetailViewModel.deleteFutureTransaction(futureTransaction)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @ExperimentalStdlibApi
    // inserts new Category into database or selects it in categorySpinner if it exists already
    private fun insertCategory(input : String) {

        // makes first letter of every word capital and every other letter lower case
        val name = input.split(" ").joinToString(" ") {it.toLowerCase(Locale.US).capitalize(Locale.US)  }

        // -1 means it doesn't exist
        if (categoryNamesList.indexOf(name) == -1) {

            val newCategory = Category(name)
            launch {

                transactionDetailViewModel.insertCategory(newCategory)
            }
            newCategoryName = name
            madeNewCategory = true
        } else {

            categorySpinner.setSelection(categoryNamesList.indexOf(name))
        }
    }

    companion object {

        // creates arguments bundle, creates a fragment instance,
        // and attaches the arguments to the fragment
        fun newInstance(transactionId : Int, fabX : Int, fabY : Int, fromFab : Boolean) : TransactionFragment {

            val args = Bundle().apply {

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