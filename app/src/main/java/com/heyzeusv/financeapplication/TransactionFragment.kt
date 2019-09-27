package com.heyzeusv.financeapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.math.BigDecimal
import java.text.DateFormat

private const val TAG = "TransactionFragment"
private const val ARG_TRANSACTION_ID = "transaction_id"

class TransactionFragment : Fragment() {

    // views
    private lateinit var transaction       : Transaction
    private lateinit var titleField        : EditText
    private lateinit var totalField        : EditText
    private lateinit var memoField         : EditText
    private lateinit var dateButton        : Button
    private lateinit var repeatingCheckBox : CheckBox
    private lateinit var categorySpinner   : Spinner
    private lateinit var frequencySpinner  : Spinner

    // provides instance of ViewModel
    private val transactionDetailViewModel : TransactionDetailViewModel by lazy {
        ViewModelProviders.of(this).get(TransactionDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transaction = Transaction()

        // retrieves arguments passed on (if any)
        val transactionId : Int = arguments?.getInt(ARG_TRANSACTION_ID) as Int
        // Log.d(TAG, "args bundle transaction ID: $transactionId`")
        transactionDetailViewModel.loadTransaction(transactionId)
    }

    override fun onCreateView(
        inflater : LayoutInflater,
        container : ViewGroup?,
        savedInstanceState : Bundle?
    ) : View? {

        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        
        titleField        = view.findViewById(R.id.transaction_title)     as EditText
        totalField        = view.findViewById(R.id.transaction_total)     as EditText
        memoField         = view.findViewById(R.id.transaction_memo)      as EditText
        dateButton        = view.findViewById(R.id.transaction_date)      as Button
        repeatingCheckBox = view.findViewById(R.id.transaction_repeating) as CheckBox
        categorySpinner   = view.findViewById(R.id.transaction_category)  as Spinner
        frequencySpinner  = view.findViewById(R.id.transaction_frequency) as Spinner

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            })
    }

    override fun onStart() {
        super.onStart()

        // placed in onStart due to being triggered when view state is restored
        val titleWatcher = object : TextWatcher {

            // not needed so blank
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

            // sequence is user's input which title is changed to
            override fun onTextChanged(
                sequence : CharSequence?,
                start    : Int,
                before   : Int,
                count    : Int
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
                sequence : CharSequence?,
                start    : Int,
                before   : Int,
                count    : Int
            ) {
                transaction.total = BigDecimal(sequence.toString())
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
                sequence : CharSequence?,
                start    : Int,
                before   : Int,
                count    : Int
            ) {
                transaction.memo = sequence.toString()
            }

            // not needed so blank
            override fun afterTextChanged(sequence: Editable?) {}
        }

        titleField.addTextChangedListener(titleWatcher)
        totalField.addTextChangedListener(totalWatcher)
        memoField .addTextChangedListener(memoWatcher)

        // OnClickListener not affected by state restoration,
        // but nice to have listeners in one place
        dateButton.apply {

            text = DateFormat.getDateInstance(DateFormat.FULL).format(transaction.date)
            isEnabled = false
        }
        repeatingCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->
                transaction.repeating = isChecked
            }
        }
    }

    override fun onStop() {
        super.onStop()

        transactionDetailViewModel.saveTransaction(transaction)
    }

    private fun updateUI() {

        titleField.setText(transaction.title)
        memoField .setText(transaction.memo)
        totalField.setText(String.format("$%.2f", transaction.total))
        dateButton.text = transaction.date.toString()
        repeatingCheckBox.apply {
            isChecked = transaction.repeating
            // skips animation
            jumpDrawablesToCurrentState()
        }
    }

    companion object {

        // creates arguments bundle, creates a fragment instance,
        // and attaches the arguments to the fragment
        fun newInstance(transactionId : Int) : TransactionFragment {

            val args = Bundle().apply {

                putInt(ARG_TRANSACTION_ID, transactionId)
            }

            return TransactionFragment().apply {

                arguments = args
            }
        }
    }
}