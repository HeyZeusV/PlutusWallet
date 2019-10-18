 package com.heyzeusv.financeapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.heyzeusv.financeapplication.utilities.BaseFragment

private const val  TAG = "GraphFragment"

class GraphFragment : BaseFragment() {

    private lateinit var pieChart : PieChart

    private var pieEntries : MutableList<PieEntry> = mutableListOf()

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_graph, container, false)

        pieChart = view.findViewById(R.id.graph_pie)

        pieEntries.add(PieEntry(25.0f, "Test1"))
        pieEntries.add(PieEntry(25.0f, "Test2"))
        pieEntries.add(PieEntry(50.0f, "Test3"))
        pieEntries.add(PieEntry(0.0f, "Test4"))

        val dataSet = PieDataSet(pieEntries, "Testing")
        dataSet.sliceSpace = 2.0f
        dataSet.selectionShift = 5.0f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        pieChart.data = data
        val description = Description()
        description.text = ""
        val legend = pieChart.legend
        legend.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.description = description
        pieChart.animateX(1000, Easing.EaseInBack)


        return view
    }

    companion object {

        fun newInstance() : GraphFragment {

            return GraphFragment()
        }
    }
}