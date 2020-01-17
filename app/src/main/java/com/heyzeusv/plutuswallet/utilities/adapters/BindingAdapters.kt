package com.heyzeusv.plutuswallet.utilities.adapters

import android.graphics.Color
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.heyzeusv.plutuswallet.database.entities.ItemViewChart

/**
 *  DataBinding Custom Binding Adapters.
 *
 *  Will end up splitting into specific files if this gets too large...
 */
/**
 *  Sets up PieCharts used in GraphFragment.
 *
 *  @param view PieChart to be affected.
 *  @param ivc  ItemViewChart object holding data needed to set up graph.
 */
@BindingAdapter("android:setUpChart")
fun setUpChart(view : PieChart, ivc : ItemViewChart) {

    // no chart to create if ctList is empty
    if (ivc.ctList.isEmpty()) return

    // either "Expense" or "Income"
    val type : String = ivc.ctList[0].type

    // list of values to be displayed in PieChart
    val pieEntries : List<PieEntry> = ivc.ctList.map { PieEntry(it.total.toFloat(), it.category) }

    // PieDataSet set up
    val dataSet = PieDataSet(pieEntries, "Transactions")
    // distance between slices
    dataSet.sliceSpace     = 2.5f
    // size of percent value
    dataSet.valueTextSize  = 13f
    // colors used for slices
    dataSet.colors = ivc.colorArray
    // size of highlighted area
    if (ivc.fCategory == true && ivc.fType == type && ivc.fCatName != "All") {

        dataSet.selectionShift = 7.5f
    } else {

        dataSet.selectionShift = 0.0f
    }

    // PieData set up
    val data = PieData(dataSet)
    // makes values in form of percentages
    data.setValueFormatter(PercentFormatter(view))

    //Description set up
    val description =  Description()
    // don't want a description so make it blank
    description.text = ""

    // PieChart set up
    view.data = data
    // displays translated type in center of chart
    view.centerText = ivc.typeTrans
    // attach description
    view.description = description
    // don't want legend so disable it
    view.legend.isEnabled = false
    // true = doughnut chart
    view.isDrawHoleEnabled = true
    // color of labels
    view.setEntryLabelColor(Color.BLACK)
    // size of Category labels
    view.setEntryLabelTextSize(14.5f)
    // size of center text
    view.setCenterTextSize(15f)
    // true = display center text
    view.setDrawCenterText(true)
    // true = use percent values
    view.setUsePercentValues(true)
    // highlights Category selected if it exists with current filters applied
    if (ivc.fCategory == true && ivc.fType == type && ivc.fCatName != "All") {

        // finds position of Category selected in FilterFragment in ctList
        val position : Int = ivc.ctList.indexOfFirst { it.category == ivc.fCatName }
        // -1 = doesn't exist
        if (position != -1) {

            view.highlightValue(position.toFloat(), 0)
        }
    }
    // refreshes PieChart
    view.invalidate()
}

/**
 *  Sets up ViewPager2 adapter in GraphFragment.
 *
 *  @param view    ViewPager2 to be affected.
 *  @param adapter adapter to be applied.
 */
@BindingAdapter("android:setAdapter")
fun setAdapter(view : ViewPager2, adapter : ChartAdapter?) {

    adapter?.let {

        view.adapter = adapter
    }
}