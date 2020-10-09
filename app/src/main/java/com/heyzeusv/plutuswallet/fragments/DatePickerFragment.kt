package com.heyzeusv.plutuswallet.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

private const val ARG_DATE = "date"

class DatePickerFragment : DialogFragment() {

    /**
     *  Required interface for hosting fragments.
     *
     *  Defines work that the fragment needs done by hosting activity.
     */
    interface Callbacks {

        /**
         *  Will update Transaction date with [date] selected from DatePickerFragment.
         */
        fun onDateSelected(date: Date)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        /**
         *  Listener for DateDialog.
         *
         *  Used to receive the date the user selects.
         */
        val dateListener =
            DatePickerDialog.OnDateSetListener { _, year: Int, month: Int, day: Int ->

                // turns date selected into Date type
                val resultDate: Date = GregorianCalendar(year, month, day).time

                // targetFragment stores fragment instance that started DatePickerFragment
                targetFragment?.let { fragment: Fragment ->
                    // passes Date selected to TransactionFragment
                    (fragment as Callbacks).onDateSelected(resultDate)
                }
            }

        // loads Date saved into Bundle
        val date: Date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar: Calendar = Calendar.getInstance()
        // sets calendar time to date
        calendar.time = date
        // variables used to initialize DateDialog
        val initialYear: Int = calendar.get(Calendar.YEAR)
        val initialMonth: Int = calendar.get(Calendar.MONTH)
        val initialDay: Int = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(), dateListener,
            initialYear, initialMonth, initialDay
        )
    }

    companion object {

        /**
         *  Initializes instance of DatePickerFragment with [date] passed.
         *
         *  Creates arguments Bundle, creates a Fragment instance, and attaches the
         *  arguments to the Fragment.
         */
        fun newInstance(date: Date): DatePickerFragment {

            val args: Bundle = Bundle().apply { putSerializable(ARG_DATE, date) }

            return DatePickerFragment().apply { arguments = args }
        }
    }
}