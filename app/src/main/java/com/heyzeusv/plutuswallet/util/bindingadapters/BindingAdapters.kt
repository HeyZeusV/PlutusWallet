package com.heyzeusv.plutuswallet.util.bindingadapters

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.*
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.ItemViewChart
import com.heyzeusv.plutuswallet.ui.cfl.chart.ChartAdapter
import com.heyzeusv.plutuswallet.util.MaterialSpinnerAdapter
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
    if (ivc.fCategory && ivc.fType == type && !ivc.fCatName.contains("All")) {
        dataSet.selectionShift = 10f
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
    setCenterTextColor(ContextCompat.getColor(this.context, R.color.textColorPrimary))
    // true = display center text
    setDrawCenterText(true)
    // true = use percent values
    setUsePercentValues(true)
    val highlights: MutableList<Highlight> = mutableListOf()
    // highlights Category selected if it exists with current filters applied
    if (ivc.fCategory && ivc.fType == type && !ivc.fCatName.contains("All")) {
        for (cat: String in ivc.fCatName) {
            // finds position of Category selected in FilterFragment in ctList
            val position: Int = ivc.ctList.indexOfFirst { it.category == cat }
            // -1 = doesn't exist
            if (position != -1) highlights.add(Highlight(position.toFloat(), 0, 0))
        }
    }
    highlightValues(highlights.toTypedArray())
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

/**
 *  Creates ArrayAdapter with [entries] and attaches it to AutoCompleteTextView.
 */
@BindingAdapter("entries")
fun AutoCompleteTextView.setEntries(entries: List<String>?) {

    if (entries != null) {
        val arrayAdapter: MaterialSpinnerAdapter<String> =
            MaterialSpinnerAdapter(context, R.layout.material_spinner_item, entries)
        setAdapter(arrayAdapter)

    }
}