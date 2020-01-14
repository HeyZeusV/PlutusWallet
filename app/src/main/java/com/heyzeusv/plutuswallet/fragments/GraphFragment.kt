package com.heyzeusv.plutuswallet.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.CategoryTotals
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo
import com.heyzeusv.plutuswallet.databinding.FragmentGraphBinding
import com.heyzeusv.plutuswallet.databinding.ItemViewGraphBinding
import com.heyzeusv.plutuswallet.viewmodels.FGLViewModel
import com.heyzeusv.plutuswallet.viewmodels.GraphViewModel
import java.math.BigDecimal

private const val TAG = "PWGraphFragment"

/**
 *   Creates and populates charts with Transaction data depending on filter applied.
 */
class GraphFragment : BaseFragment() {

    // lists used to hold CategoryTotals and Category names
    private var transactionLists : MutableList<List<CategoryTotals>> = mutableListOf(emptyList(), emptyList())
    private var expenseNameList  : MutableList<String>               = mutableListOf()
    private var incomeNameList   : MutableList<String>               = mutableListOf()

    private lateinit var binding : FragmentGraphBinding

    // provides instance of GraphViewModel
    private val graphVM : GraphViewModel by lazy {
        ViewModelProviders.of(this).get(GraphViewModel::class.java)
    }

    private lateinit var fglViewModel : FGLViewModel

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_graph, container, false)
        binding.lifecycleOwner = activity
        binding.graphVM        = graphVM

        val view : View = binding.root

        binding.graphViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                /**
                 *  @param position page in focus.
                 */
                override fun onPageSelected(position : Int) {

                    graphVM.selectedGraph = position
                }
            })

        // this ensures that this is same FGLViewModel as Filter/ListFragment use
        fglViewModel = activity!!.let {

            ViewModelProviders.of(it).get(FGLViewModel::class.java)
        }

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        fglViewModel.tInfoLiveData.observe(this, Observer { tInfo : TransactionInfo ->

                // LiveData of Expense Transactions
                val expenseListLiveData : LiveData<List<CategoryTotals>> =
                    graphVM.filteredCategoryTotals(tInfo.account, tInfo.date, "Expense",
                        tInfo.accountName, tInfo.start, tInfo.end)
                // LiveData of Income Transactions
                val incomeListLiveData  : LiveData<List<CategoryTotals>> =
                    graphVM.filteredCategoryTotals(tInfo.account, tInfo.date, "Income",
                        tInfo.accountName, tInfo.start, tInfo.end)

                // register an observer on LiveData instance and tie life to this component
                // execute code whenever LiveData gets update
                expenseListLiveData.observe( this, Observer { eList : List<CategoryTotals> ->

                    graphVM.expenseNames = eList.map { it.category }
                    graphVM.expenseCatTotals = eList

                    Log.d(TAG, "HERE Expense")
                    transactionLists[0] = eList
                    updateUI(transactionLists)
                })

                // register an observer on LiveData instance and tie life to this component
                // execute code whenever LiveData gets updated
                incomeListLiveData.observe(this, Observer { iList : List<CategoryTotals> ->

                    graphVM.incomeNames = iList.map { it.category }
                    graphVM.incomeCatTotals = iList
                    Log.d(TAG, "HERE Income")
                    transactionLists[1] = iList
                    updateUI(transactionLists)

                })
            }
        )
    }

    /**
     *  Ensures the UI is up to date with correct information.
     *
     *  @param transactionLists list of lists of CategoryTotals to be converted to graphs.
     */
    private fun updateUI(transactionLists : MutableList<List<CategoryTotals>>) {

        // creates GraphAdapter to set with ViewPager2
        binding.graphViewPager.adapter = GraphAdapter(transactionLists)

        // sets up Dots Indicator with ViewPager2
        binding.graphCircleIndicator.setViewPager(binding.graphViewPager)

        // when user deletes Transaction, returns to this screen, or applies a filter
        // this stops ViewPager2 from switching graphs
        binding.graphViewPager.setCurrentItem(graphVM.selectedGraph, false)

    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     *
     *  @param transactionLists the list of lists of CategoryTotals.
     */
    private inner class GraphAdapter(var transactionLists : MutableList<List<CategoryTotals>>)
        : RecyclerView.Adapter<GraphHolder>() {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : GraphHolder {

            val itemBinding : ItemViewGraphBinding = ItemViewGraphBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return GraphHolder(itemBinding)
        }

        override fun getItemCount() : Int = transactionLists.size

        // populates given holder with list of CategoryTotal from the given position in list
        override fun onBindViewHolder(holder : GraphHolder, position : Int) {

            val categoryTotals : List<CategoryTotals> = transactionLists[position]
            holder.bind(categoryTotals, position)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view.
     */
    private inner class GraphHolder(private var binding : ItemViewGraphBinding)
        : RecyclerView.ViewHolder(binding.root) {


        // views in the ItemView
        private val pieChart      : PieChart = itemView.findViewById(R.id.graph_pie)
        private val emptyTextView : TextView = itemView.findViewById(R.id.emptyTextView)
        private val graphTotal    : TextView = itemView.findViewById(R.id.graph_total)

        // values from FGLViewModel
        val category     : Boolean? = fglViewModel.tInfoLiveData.value?.category
        val categoryName : String?  = fglViewModel.tInfoLiveData.value?.categoryName
        val typeStored   : String?  = fglViewModel.tInfoLiveData.value?.type

        // one most likely to be -1, but will still be checked before being used
        val expensePosition : Int = expenseNameList.indexOf(categoryName)
        val incomePosition  : Int = incomeNameList .indexOf(categoryName)

        // sets the views with CategoryTotal data
        fun bind(categoryTotals : List<CategoryTotals>, type : Int) {

            // will display a message if there is no data to be displayed
            if (categoryTotals.isNotEmpty()) {

                emptyTextView.isVisible = false
                pieChart     .isVisible = true

                val typeName : String
                var total = BigDecimal("0")

                // list of values to be displayed in PieChart
                val pieEntries: MutableList<PieEntry> = mutableListOf()

                // adds values in categoryTotals list into list holding chart data
                categoryTotals.forEach {

                    pieEntries.add(PieEntry(it.total.toFloat(), it.category))
                    // adds to total
                    if (category != true || categoryName == getString(R.string.category_all)) {

                        total += it.total
                    } else {

                        // if a Category is selected in FilterFragment
                        if (it.category == categoryName) {

                            total = it.total
                        }
                    }
                }

                // sets text based on decimalPlaces and symbolSide
                graphTotal.text = if (decimalPlaces) {

                    if (symbolSide) {

                        getString(R.string.graph_total, currencySymbol, decimalFormatter.format(total))
                    } else {

                        getString(R.string.graph_total, decimalFormatter.format(total), currencySymbol)
                    }
                } else {

                    if (symbolSide) {

                        getString(R.string.graph_total, currencySymbol, integerFormatter.format(total))
                    } else {

                        getString(R.string.graph_total, integerFormatter.format(total), currencySymbol)
                    }
                }

                // PieDataSet set up
                val dataSet = PieDataSet(pieEntries, "Transactions")
                // distance between slices
                dataSet.sliceSpace     = 2.5f
                // size of highlighted area
                dataSet.selectionShift = 0.0f
                dataSet.valueTextSize  = 10f
                // sets up the colors and typeName depending on type
                if (type == 0) {

                    typeName = getString(R.string.type_expense)
                    context?.let {
                        dataSet.setColors(intArrayOf(R.color.colorExpense1, R.color.colorExpense2,
                                                     R.color.colorExpense3, R.color.colorExpense4), context)
                    }
                    category?.let {
                        if (category) {

                            if (expensePosition != -1) {

                                // size of highlighted area
                                dataSet.selectionShift = 7.5f
                            } else {

                                dataSet.selectionShift = 0.0f
                            }
                        }
                    }
                } else {

                    typeName = getString(R.string.type_income)
                    context?.let {
                        dataSet.setColors(intArrayOf(R.color.colorIncome1, R.color.colorIncome2,
                                                     R.color.colorIncome3, R.color.colorIncome4), context)
                    }
                    category?.let {
                        if (category) {

                            if (incomePosition != -1) {

                                // size of highlighted area
                                dataSet.selectionShift = 7.5f
                            } else {

                                dataSet.selectionShift = 0.0f
                            }
                        }
                    }
                }

                // PieData set up
                val data = PieData(dataSet)
                // makes value in form of percentages
                data.setValueFormatter(PercentFormatter(pieChart))

                // Description set up
                val description  = Description()
                // don't want a description so make it blank
                description.text = ""

                // PieChart set up
                // attach data
                pieChart.data              = data
                // displays type in center of chart
                pieChart.centerText        = typeName
                // attach description
                pieChart.description       = description
                // don't want legend so disable it
                pieChart.legend.isEnabled  = false
                // true = doughnut chart
                pieChart.isDrawHoleEnabled = true
                // color of label text
                pieChart.setEntryLabelColor(Color.BLACK)
                pieChart.setCenterTextSize(15f)
                pieChart.setDrawCenterText(true)
                pieChart.setUsePercentValues(true)
                // highlights the category being searched on its respective PieChart
                category?.let {


                    if (it && typeStored == getString(R.string.type_expense) && typeName == getString(R.string.type_expense)) {

                        if (expensePosition != -1) {

                            pieChart.highlightValue(expensePosition.toFloat(), 0)
                        }
                    }
                    if (it && typeStored == getString(R.string.type_income) && typeName == getString(R.string.type_income)) {

                        if (incomePosition != -1) {

                            pieChart.highlightValue(incomePosition.toFloat(), 0)
                        }
                    }
                }
                pieChart.invalidate()
            } else {

                emptyTextView.isVisible = true
                graphTotal   .isVisible = false
                pieChart     .isVisible = false
            }
        }
    }

    companion object {

        /**
         *  Initializes instance of GraphFragment
         */
        fun newInstance() : GraphFragment {

            return GraphFragment()
        }
    }
}