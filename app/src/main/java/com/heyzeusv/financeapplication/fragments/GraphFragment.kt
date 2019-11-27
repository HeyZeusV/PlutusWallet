 package com.heyzeusv.financeapplication.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
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
import com.heyzeusv.financeapplication.R
import com.heyzeusv.financeapplication.database.entities.CategoryTotals
import com.heyzeusv.financeapplication.viewmodels.GraphViewModel
import me.relex.circleindicator.CircleIndicator3
import java.math.BigDecimal
import java.util.Date

 private const val TAG               = "GraphFragment"
private const val ARG_CATEGORY      = "category"
private const val ARG_DATE          = "date"
private const val ARG_TYPE          = "type"
private const val ARG_CATEGORY_NAME = "category_name"
private const val ARG_START         = "start"
private const val ARG_END           = "end"

/**
 *   Creates and populates charts with Transaction data depending on filter applied.
 */
class GraphFragment : BaseFragment() {

    //views
    private lateinit var circleIndicator : CircleIndicator3
    private lateinit var graphViewPager  : ViewPager2

    // lists used to hold CategoryTotals and Category names
    private var emptyList        : List<CategoryTotals>              = emptyList()
    private var transactionLists : MutableList<List<CategoryTotals>> = mutableListOf(emptyList, emptyList)
    private var expenseNameList  : MutableList<String>               = mutableListOf()
    private var incomeNameList   : MutableList<String>               = mutableListOf()

    // the graph being displayed
    private var selectedGraph = 0

    // provides instance of ViewModel
    private val graphViewModel : GraphViewModel by lazy {
        ViewModelProviders.of(this).get(GraphViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_graph, container, false)

        circleIndicator = view.findViewById(R.id.graph_circle_indicator) as CircleIndicator3
        graphViewPager  = view.findViewById(R.id.graph_view_pager      ) as ViewPager2

        // clears previous lists
        transactionLists = mutableListOf(emptyList, emptyList)

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // loads in arguments, if any
        val date         : Boolean? = arguments?.getBoolean     (ARG_DATE)
        val start        : Date?    = arguments?.getSerializable(ARG_START)         as Date?
        val end          : Date?    = arguments?.getSerializable(ARG_END)           as Date?

        // LiveData of Expense Transactions
        val expenseTransactionListLiveData : LiveData<List<CategoryTotals>> =
            graphViewModel.filteredCategoryTotals(date, "Expense", start, end)
        // LiveData of Income Transactions
        val incomeTransactionListLiveData  : LiveData<List<CategoryTotals>> =
            graphViewModel.filteredCategoryTotals(date, "Income", start, end)

        // register an observer on LiveData instance and tie life to another component
        expenseTransactionListLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { expenseList : List<CategoryTotals>? ->
                // if not null
                expenseList?.let {
                    transactionLists[0] = calculateTotals("Expense", expenseList)
                    updateUI(transactionLists)
                }
            }
        )

        // register an observer on LiveData instance and tie life to another component
        incomeTransactionListLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { incomeList : List<CategoryTotals>? ->
                // if not null
                incomeList?.let {
                    transactionLists[1] = calculateTotals("Income", incomeList)
                    updateUI(transactionLists)
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        graphViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            /**
             *  Places selected page into selectedGraph
             *
             *  @param position the page that was selected
             */
            override fun onPageSelected(position : Int) {

                selectedGraph = position
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // notify that a setting might have been changed
        // that causes graphTotal to change
        graphViewPager.adapter?.notifyDataSetChanged()
    }

    /**
     *  Calculates the sum of all Totals of the same Category from given CategoryTotals list
     *
     *  @param  type              used to tell which Category name list to edit
     *  @param  categoryTotalList list holding CategoryTotals to be summed up
     *  @return list holding CategoryTotals with unique Category names totals summed up
     */
    private fun calculateTotals(type : String, categoryTotalList : List<CategoryTotals>) : List<CategoryTotals> {

        // list holding unique Category names
        val categoryList              : MutableList<String>         = mutableListOf()
        // list holding CategoryTotals with unique Category names totals summed up
        val updatedCategoryTotalsList : MutableList<CategoryTotals> = mutableListOf()

        // used to get list of unique Category names
        categoryTotalList.forEach {

            if (!categoryList.contains(it.category)) {

                categoryList.add(it.category)
            }
        }

        // used to get CategoryTotals, 1 for each Category obtained above
        categoryList.forEach { category : String ->

            val categoryTotal = CategoryTotals(category, BigDecimal(0))
            var total         = 0.0f
            // calculates sum of all Totals with the same Category of this CategoryTotals
            categoryTotalList.forEach {

                if (it.category == category) {

                    total += it.total.toFloat()
                }
            }
            categoryTotal.total = BigDecimal(total.toString())
            updatedCategoryTotalsList.add(categoryTotal)
        }

        // clears Category name lists and re-adds new values
        if (type == "Expense") {

            expenseNameList = categoryList
        } else {

            incomeNameList = categoryList
        }

        return updatedCategoryTotalsList
    }
    /**
     *  Ensures the UI is up to date with correct information.
     *
     *  @param transactionLists list of lists of CategoryTotals to be converted to graphs.
     */
    private fun updateUI(transactionLists : MutableList<List<CategoryTotals>>) {

        // creates GraphAdapter to set with ViewPager2
        graphViewPager .adapter = GraphAdapter(transactionLists)
        // sets up Dots Indicator with ViewPager2
        circleIndicator.setViewPager(graphViewPager)
        // when user deletes Transaction or returns to this screen,
        // this stops ViewPager2 from switching graphs
        graphViewPager .setCurrentItem(selectedGraph, false)

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

            val view : View = layoutInflater.inflate(R.layout.item_view_graph, parent, false)
            return GraphHolder(view)
        }

        override fun getItemCount() = transactionLists.size

        // populates given holder CategoryTotal from the given position in list
        override fun onBindViewHolder(holder : GraphHolder, position : Int) {

            val categoryTotals : List<CategoryTotals> = transactionLists[position]
            holder.bind(categoryTotals, position)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view.
     */
    private inner class GraphHolder(view : View) : RecyclerView.ViewHolder(view) {

        // views in the ItemView
        private val pieChart      : PieChart = itemView.findViewById(R.id.graph_pie)
        private val emptyTextView : TextView = itemView.findViewById(R.id.emptyTextView)
        private val graphTotal    : TextView = itemView.findViewById(R.id.graph_total)

        // load arguments if any
        val category     : Boolean? = arguments?.getBoolean(ARG_CATEGORY)
        val categoryName : String?  = arguments?.getString (ARG_CATEGORY_NAME)
        val typeStored   : String?  = arguments?.getString (ARG_TYPE)

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

                    // only adds values that are greater than 0
                    if (it.total.toFloat() != 0.0f) {

                        pieEntries.add(PieEntry(it.total.toFloat(), it.category))
                    }
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

        /**
         *  Initializes instance of GraphFragment.
         *
         *  Creates arguments Bundle, creates a Fragment instance, and attaches the
         *  arguments to the Fragment.
         *
         *  @param  category     boolean for category filter.
         *  @param  date         boolean for date filter.
         *  @param  type         either "Expense" or "Income".
         *  @param  categoryName category name to be searched in table of type.
         *  @param  start        starting Date for date filter.
         *  @param  end          ending Date for date filter.
         *  @return TransactionListFragment instance.
         */
        fun newInstance(category : Boolean, date : Boolean, type : String, categoryName : String, start : Date, end : Date) : GraphFragment {

            val args : Bundle = Bundle().apply {

                putBoolean     (ARG_CATEGORY     , category    )
                putBoolean     (ARG_DATE         , date        )
                putString      (ARG_TYPE         , type        )
                putString      (ARG_CATEGORY_NAME, categoryName)
                putSerializable(ARG_START        , start       )
                putSerializable(ARG_END          , end         )
            }

            return GraphFragment().apply {

                arguments = args
            }
        }
    }
}