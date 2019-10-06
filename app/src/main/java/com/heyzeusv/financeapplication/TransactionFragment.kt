package com.heyzeusv.financeapplication

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.math.BigDecimal
import java.text.DateFormat
import java.util.*
import kotlin.math.hypot

private const val TAG = "TransactionFragment"
private const val ARG_TRANSACTION_ID = "transaction_id"
private const val ARG_FAB_X = "fab_X"
private const val ARG_FAB_Y = "fab_Y"
private const val ARG_FROM_FAB = "from_fab"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0

class TransactionFragment : Fragment(), DatePickerFragment.Callbacks {

    // views
    private lateinit var transaction            : Transaction
    private lateinit var titleField             : EditText
    private lateinit var totalField             : EditText
    private lateinit var memoField              : EditText
    private lateinit var frequencyField         : EditText
    private lateinit var dateButton             : Button
    private lateinit var repeatingCheckBox      : CheckBox
    private lateinit var categorySpinner        : Spinner
    private lateinit var frequencyPeriodSpinner : Spinner
    private lateinit var frequencyText          : TextView

    // arrays holding values for frequency spinners
    private var dayArray       = arrayOf("1", "2", "3", "4", "5", "6")
    private var weekArray      = arrayOf("1", "2", "3")
    private var monthArray     = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11")
    private var yearArray      = arrayOf("1", "2", "3", "4", "5")
    private var frequencyArray = arrayOf("Day(s)", "Week(s)", "Month(s)", "Year(s)")

    private var categoryNamesList = listOf<String>()

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
        totalField             = view.findViewById(R.id.transaction_total)            as EditText
        memoField              = view.findViewById(R.id.transaction_memo)             as EditText
        frequencyField         = view.findViewById(R.id.transaction_frequency)        as EditText
        dateButton             = view.findViewById(R.id.transaction_date)             as Button
        repeatingCheckBox      = view.findViewById(R.id.transaction_repeating)        as CheckBox
        categorySpinner        = view.findViewById(R.id.transaction_category)         as Spinner
        frequencyPeriodSpinner = view.findViewById(R.id.transaction_frequency_period) as Spinner
        frequencyText          = view.findViewById(R.id.frequencyTextView)            as TextView

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
                    Log.d(TAG, "args bundle fabX: $fabX fabY: $fabY")
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
        transactionDetailViewModel.transactionMaxIdLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { maxId ->
                // if not null
                maxId?.let {
                    // runs only when user creates a new Transaction
                    if (fromFab) {
                        transaction.id = maxId
                        Log.d(TAG, "Transaction ID onViewCreated: ${transaction.id}")
                    }
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
                    // sets up the categorySpinner
                    categoryNamesList = categoryNames
                    val categorySpinnerAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, categoryNames)
                    categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                    categorySpinner.adapter = categorySpinnerAdapter
                    // starts the spinner up to Category saved, if any
                    categorySpinner.setSelection(categoryNames.indexOf(transaction.category))
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        // placed in onStart due to being triggered when view state is restored
        val titleWatcher = object : TextWatcher {

            // not needed so blank
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

            // sequence is user's input which title is changed to
            override fun onTextChanged(
                sequence : CharSequence?, start : Int, before : Int, count : Int
            ) {
                transaction.title = sequence.toString()
            }

            // not needed so blank
            override fun afterTextChanged(sequence: Editable?) {}
        }

        // placed in onStart due to being triggered when view state is restored
        val totalWatcher = object : TextWatcher {

            // not needed so blank
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

            // sequence is user's input which total is changed to
            override fun onTextChanged(
                sequence : CharSequence?, start : Int, before : Int, count : Int
            ) {
                try {
                    transaction.total = BigDecimal(sequence.toString())
                } catch (e : NumberFormatException) {

                    transaction.total = BigDecimal("0.00")
                }
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
                sequence : CharSequence?, start : Int, before : Int, count : Int
            ) {
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
                sequence : CharSequence?, start : Int, before : Int, count : Int
            ) {
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
        totalField    .addTextChangedListener(totalWatcher)
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
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                Log.d(TAG, "CategorySpinner: ${parent?.getItemAtPosition(position)}")
                // updates the category to selected one
                transaction.category = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        frequencyPeriodSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                    transaction.period = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onPause() {
        super.onPause()

        // gives Transactions simple titles if user doesn't enter any
        if (transaction.title == "") {

            transaction.title = "Transaction #" + transaction.id
        }
        transactionDetailViewModel.saveTransaction(transaction)
    }

    private fun updateUI() {

        titleField    .setText(transaction.title)
        memoField     .setText(transaction.memo)
        totalField    .setText(String.format(transaction.total.toString()))
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