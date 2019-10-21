package com.heyzeusv.financeapplication

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment : DialogFragment() {

    /**
     * Required interface for hosting fragments
     * used to send date back to hosting fragment
     */
    interface Callbacks {

        fun onDateSelected(date : Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // used to receive the date the user selects, first parameter is unused
        val dateListener = DatePickerDialog.OnDateSetListener {
                _: DatePicker, year: Int, month: Int, day: Int ->

            // TransactionFragment needs Date type
            val resultDate : Date = GregorianCalendar(year, month, day).time

            // targetFragment stores fragment instance that started DatePickerFragment
            targetFragment?.let { fragment ->
                // passes new Date to CrimeFragment
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }

        val date     : Date     = arguments?.getSerializable(ARG_DATE) as Date
        val calendar : Calendar = Calendar.getInstance()
        // sets date if one existed already
        calendar.time = date
        val initialYear  : Int = calendar.get(Calendar.YEAR)
        val initialMonth : Int = calendar.get(Calendar.MONTH)
        val initialDay   : Int = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            // context object
            requireContext(),
            // date listener
            dateListener,
            // date initialized to
            initialYear,
            initialMonth,
            initialDay
        )
    }

    companion object {

        // creates arguments bundle, creates a fragment instance,
        // and attaches the arguments to the fragment
        fun newInstance(date : Date) : DatePickerFragment {

            val args : Bundle = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}