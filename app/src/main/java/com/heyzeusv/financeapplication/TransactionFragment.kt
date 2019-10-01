package com.heyzeusv.financeapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.math.BigDecimal
import java.util.*

private const val TAG = "TransactionFragment"
private const val ARG_TRANSACTION_ID = "transaction_id"
private const val ARG_FAB_X = "fab_X"
private const val ARG_FAB_Y = "fab_Y"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0

class TransactionFragment : Fragment(), DatePickerFragment.Callbacks {

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
        Log.d(TAG, "args bundle transaction ID: $transactionId")
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

        view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {

            override fun onLayoutChange(v : View, left : Int, top : Int, right : Int,
                               bottom : Int, oldLeft : Int, oldTop : Int,
                               oldRight : Int, oldButton : Int) {

                v.removeOnLayoutChangeListener(this)
                val fabX : Int = arguments?.getInt(ARG_FAB_X) as Int
                val fabY : Int = arguments?.getInt(ARG_FAB_Y) as Int
                Log.d(TAG, "args bundle fabX: $fabX fabY: $fabY")
                val width = 1000
                val height = 1000
                val finalRadius = (Math.max(width, height) / 2 + Math.max(width - fabX, height - fabY)).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(v, fabX, fabY, 0.0F, 10000F)
                anim.duration = 500
                anim.start()

            }
        })

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
                val totalString = sequence.toString()
                transaction.total = BigDecimal(totalString.replace("$", ""))
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

    // will update date with the date selected from DatePickerFragment
    override fun onDateSelected(date : Date) {

        transaction.date = date
        updateUI()
    }

    companion object {

        // creates arguments bundle, creates a fragment instance,
        // and attaches the arguments to the fragment
        fun newInstance(transactionId : Int, fabX : Int, fabY : Int) : TransactionFragment {

            val args = Bundle().apply {

                putInt(ARG_TRANSACTION_ID, transactionId)
                putInt(ARG_FAB_X, fabX)
                putInt(ARG_FAB_Y, fabY)
            }

            return TransactionFragment().apply {

                arguments = args
            }
        }
    }
}