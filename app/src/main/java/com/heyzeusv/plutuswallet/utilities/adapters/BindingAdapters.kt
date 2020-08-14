package com.heyzeusv.plutuswallet.utilities.adapters

import android.graphics.Color
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.chip.ChipGroup
import com.heyzeusv.plutuswallet.database.entities.ItemViewChart
import com.heyzeusv.plutuswallet.database.entities.SettingsValues
import com.heyzeusv.plutuswallet.utilities.CurrencyEditText

/**
 *  DataBinding Custom Binding Adapters.
 *
 *  Will end up splitting into specific files if this gets too large...
 */
/**
 *  @param cAdapter ChartAdapter to be applied.
 */
@BindingAdapter("setAdapter")
fun ViewPager2.setChartAdapter(cAdapter : ChartAdapter?) {

    cAdapter?.let {

        adapter = cAdapter
    }
}

/**
 *  Sets up PieCharts used in ChartFragment.
 *
 *  @param ivc ItemViewChart object holding data needed to set up graph.
 */
@BindingAdapter("setUpChart")
fun PieChart.setUpChart(ivc : ItemViewChart) {

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
    val pData = PieData(dataSet)
    // makes values in form of percentages
    pData.setValueFormatter(PercentFormatter(this))

    // PieChart set up
    data = pData
    // displays translated type in center of chart
    centerText = ivc.typeTrans
    // don't want a description so make it blank
    description.text = ""
    // don't want legend so disable it
    legend.isEnabled = false
    // true = doughnut chart
    isDrawHoleEnabled = true
    // color of labels
    setEntryLabelColor(Color.BLACK)
    // size of Category labels
    setEntryLabelTextSize(14.5f)
    // size of center text
    setCenterTextSize(15f)
    // true = display center text
    setDrawCenterText(true)
    // true = use percent values
    setUsePercentValues(true)
    // highlights Category selected if it exists with current filters applied
    if (ivc.fCategory == true && ivc.fType == type && ivc.fCatName != "All") {

        // finds position of Category selected in FilterFragment in ctList
        val position : Int = ivc.ctList.indexOfFirst { it.category == ivc.fCatName }
        // -1 = doesn't exist
        if (position != -1) {

            highlightValue(position.toFloat(), 0)
        }
    }
    // refreshes PieChart
    invalidate()
}

/**
 *  @param bool View enabled state.
 */
@BindingAdapter("isEnabled")
fun View.setIsEnabled(bool : Boolean) {

    isEnabled = bool
}

/**
 *  @param id of chip selected
 */
@BindingAdapter("selectedChipId")
fun ChipGroup.setSelectedChipId(id : Int) {

    if (id != checkedChipId) check(id)
}

/**
 *  InverseListener for ChipGroup 2-way DataBinding.
 *
 *  @param inverseBindingListener listener that gets triggered when a chip is selected.
 */
@BindingAdapter("selectedChipIdAttrChanged")
fun ChipGroup.chipIdInverseBindingListener(inverseBindingListener : InverseBindingListener?) {

    when (inverseBindingListener) {
        null -> setOnCheckedChangeListener(null)
        else -> {

            setOnCheckedChangeListener { group : ChipGroup, _ : Int ->

                // ensures a chip is always selected
                for (i: Int in 0 until group.childCount) {

                    val chip : View = group.getChildAt(i)
                    chip.isClickable = chip.id != group.checkedChipId
                }
                inverseBindingListener.onChange()
            }
        }
    }
}

/**
 *  Gets called when InverseListener is triggered.
 *
 *  @return selected Chip.
 */
@InverseBindingAdapter(attribute = "selectedChipId", event = "selectedChipIdAttrChanged")
fun ChipGroup.getSelectedChipId() : Int {

    return checkedChipId
}

/**
 *  Sets filter that prevents user from typing decimal symbol when turned off in settings.
 *
 *  @param setVals holds Settings values.
 */
@BindingAdapter("filter")
fun CurrencyEditText.setFilter(setVals : SettingsValues) {

    // user selects no decimal places
    if (!setVals.decimalPlaces) {

        // filter that prevents user from typing decimalSymbol thus only integers
        filters += object : InputFilter {

            override fun filter(source : CharSequence?, start : Int, end : Int, dest : Spanned?,
                                dstart : Int, dend : Int) : CharSequence? {

                for (i : Int in start until end) {

                    if (source == setVals.decimalSymbol.toString()) return ""
                }
                return null
            }
        }
    }
}