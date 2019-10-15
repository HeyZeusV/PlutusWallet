package com.heyzeusv.financeapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import com.heyzeusv.financeapplication.utilities.BaseFragment

class FilterFragment : BaseFragment() {

    // views
    private lateinit var categorySpinner : Spinner
    private lateinit var startDateButton : Button
    private lateinit var endDateButton   : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_filter, container, false)

        categorySpinner = view.findViewById(R.id.filter_category)   as Spinner
        startDateButton = view.findViewById(R.id.filter_start_date) as Button
        endDateButton   = view.findViewById(R.id.filter_end_date)   as Button

        return view
    }

}