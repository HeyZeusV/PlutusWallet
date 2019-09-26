package com.heyzeusv.financeapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import java.math.BigDecimal

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transaction = Transaction()
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
        memoField.addTextChangedListener(memoWatcher)

        // OnClickListener not affected by state restoration,
        // but nice to have listeners in one place
        dateButton.apply {

            text = transaction.date.toString()
            isEnabled = false
        }
        repeatingCheckBox.apply {

            setOnCheckedChangeListener { _, isChecked ->
                transaction.repeating = isChecked
            }
        }
    }
}