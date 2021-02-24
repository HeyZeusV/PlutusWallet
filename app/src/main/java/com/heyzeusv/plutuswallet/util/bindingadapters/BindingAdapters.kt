package com.heyzeusv.plutuswallet.util.bindingadapters

import android.text.InputFilter
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
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
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.ItemViewChart
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.ui.cfl.chart.ChartAdapter
import com.heyzeusv.plutuswallet.ui.transaction.CurrencyEditText
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 *  DataBinding Custom Binding Adapters.
 *
 *  Will end up splitting into specific files if this gets too large...
 */
/**
 *  Sets [cAdapter] as ViewPager2 adapter
 */
@BindingAdapter("setAdapter")
fun ViewPager2.setChartAdapter(cAdapter: ChartAdapter?) {
    cAdapter?.let { adapter = cAdapter }
}

/**
 *  Sets up PieCharts used in ChartFragment using [ivc] data.
 */
@BindingAdapter("setUpChart")
fun PieChart.setUpChart(ivc: ItemViewChart) {

    // no chart to create if ctList is empty
    if (ivc.ctList.isEmpty()) return

    // either "Expense" or "Income"
    val type: String = ivc.ctList[0].type

    // list of values to be displayed in PieChart
    val pieEntries: List<PieEntry> = ivc.ctList.map { PieEntry(it.total.toFloat(), it.category) }

    // PieDataSet set up
    val dataSet = PieDataSet(pieEntries, "Transactions")
    // distance between slices
    dataSet.sliceSpace = 2.5f
    // size of percent value
    dataSet.valueTextSize = 13f
    // color of percent value
    dataSet.valueTextColor = ContextCompat.getColor(this.context, R.color.colorChartText)
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
    setEntryLabelColor(ContextCompat.getColor(this.context, R.color.colorChartText))
    // size of Category labels
    setEntryLabelTextSize(14.5f)
    // color of center hole
    setHoleColor(ContextCompat.getColor(this.context, R.color.colorChartHole))
    // size of center text
    setCenterTextSize(15f)
    // color of center text
    setCenterTextColor(ContextCompat.getColor(this.context, R.color.colorText))
    // true = display center text
    setDrawCenterText(true)
    // true = use percent values
    setUsePercentValues(true)
    // highlights Category selected if it exists with current filters applied
    if (ivc.fCategory == true && ivc.fType == type && ivc.fCatName != "All") {
        // finds position of Category selected in FilterFragment in ctList
        val position: Int = ivc.ctList.indexOfFirst { it.category == ivc.fCatName }
        // -1 = doesn't exist
        if (position != -1) highlightValue(position.toFloat(), 0)

    }
    // refreshes PieChart
    invalidate()
}

/**
 *  Views state depending on [enabled]
 */
@BindingAdapter("isEnabled")
fun View.setIsEnabled(enabled: Boolean) {

    isEnabled = enabled
}

/**
 *  Selects Chip with [id] in ChipGroup
 */
@BindingAdapter("selectedChipId")
fun ChipGroup.setSelectedChipId(id: Int) {

    if (id != checkedChipId) check(id)
}

/**
 *  InverseListener for ChipGroup 2-way DataBinding.
 *  [inverseBindingListener] gets triggered when a chip is selected.
 */
@BindingAdapter("selectedChipIdAttrChanged")
fun ChipGroup.chipIdInverseBindingListener(inverseBindingListener: InverseBindingListener?) {

    if (inverseBindingListener == null) {
        setOnCheckedChangeListener(null)
    } else {
        setOnCheckedChangeListener { group: ChipGroup, _ ->
            // ensures a chip is always selected
            for (i: Int in 0 until group.childCount) {
                val chip: View = group.getChildAt(i)
                chip.isClickable = chip.id != group.checkedChipId
            }
            inverseBindingListener.onChange()
        }
    }
}

/**
 *  Returns selected Chip when InverseListener is triggered.
 */
@InverseBindingAdapter(attribute = "selectedChipId", event = "selectedChipIdAttrChanged")
fun ChipGroup.getSelectedChipId(): Int = checkedChipId

/**
 *  Sets filter that prevents user from typing decimal symbol when turned off in [setVals].
 */
@BindingAdapter("filter")
fun CurrencyEditText.setFilter(setVals: SettingsValues) {

    // user selects no decimal places
    if (!setVals.decimalPlaces) {
        // filter that prevents user from typing decimalSymbol thus only integers
        filters += object : InputFilter {

            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {

                for (i: Int in start until end) {
                    if (source == setVals.decimalSymbol.toString()) return ""
                }
                return null
            }
        }
    }
}

/**
 *  Changes given text into a clickable [link].
 */
@BindingAdapter("link")
fun TextView.setLink(link: String) {

    // changes HTML string into link
    text = HtmlCompat.fromHtml(link, HtmlCompat.FROM_HTML_MODE_LEGACY)
    // makes link clickable
    movementMethod = LinkMovementMethod.getInstance()
}

/**
 *  Loads in the [file] given and displays its text in TextView.
 */
@BindingAdapter("file")
fun TextView.loadFile(file: String) {

    var fileText = ""
    var reader: BufferedReader? = null

    try {
        // open file and read through it
        reader = BufferedReader(InputStreamReader(context.assets.open(file)))
        fileText = reader.readLines().joinToString("\n")
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            // close reader
            reader?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // sets text to content from file
        text = fileText
    }
}

/**
 *  Sets TextView as [selected] in order to cause text to scroll if it is too long.
 */
@BindingAdapter("selected")
fun TextView.selected(selected: Boolean) {

    this.isSelected = selected
}