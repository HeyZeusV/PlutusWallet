 package com.heyzeusv.financeapplication

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
import com.heyzeusv.financeapplication.utilities.BaseFragment
import me.relex.circleindicator.CircleIndicator3

 private const val TAG = "GraphFragment"

/**
 *   Creates and populates charts with Transaction data depending on filter applied.
 */
class GraphFragment : BaseFragment() {

    //views
    private lateinit var graphViewPager    : ViewPager2
    private lateinit var wormDotsIndicator : CircleIndicator3

    // lists used to hold CategoryTotals
    private var emptyList        : List<CategoryTotals>              = emptyList()
    private var transactionLists : MutableList<List<CategoryTotals>> = mutableListOf(emptyList, emptyList)

    // the graph being displayed
    private var selectedGraph = 0

    // provides instance of ViewModel
    private val graphViewModel : GraphViewModel by lazy {
        ViewModelProviders.of(this).get(GraphViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_graph, container, false)

        graphViewPager    = view.findViewById(R.id.graph_view_pager)       as ViewPager2
        wormDotsIndicator = view.findViewById(R.id.graph_circle_indicator) as CircleIndicator3

        // clears previous lists
        transactionLists = mutableListOf(emptyList, emptyList)

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LiveData of Expense Transactions
        val expenseTransactionListLiveData : LiveData<List<CategoryTotals>> =
            graphViewModel.categoryTotals("Expense")
        // LiveData of Income Transactions
        val incomeTransactionListLiveData  : LiveData<List<CategoryTotals>> =
            graphViewModel.categoryTotals("Income")

        // register an observer on LiveData instance and tie life to another component
        expenseTransactionListLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { expenseList ->
                // if not null
                expenseList?.let {
                    transactionLists[0] = expenseList
                    updateUI(transactionLists)
                }
            }
        )

        // register an observer on LiveData instance and tie life to another component
        incomeTransactionListLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { incomeList ->
                // if not null
                incomeList?.let {
                    transactionLists[1] = incomeList
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

    /**
     *  Ensures the UI is up to date with correct information.
     *
     *  @param transactionLists list of lists of CategoryTotals to be converted to graphs.
     */
    private fun updateUI(transactionLists : MutableList<List<CategoryTotals>>) {

        // creates GraphAdapter to set with ViewPager2
        graphViewPager   .adapter     = GraphAdapter(transactionLists)
        // sets up Dots Indicator with ViewPager2
        wormDotsIndicator.setViewPager(graphViewPager)
        // when user deletes Transaction or returns to this screen,
        // this stops ViewPager2 from switching graphs
        graphViewPager.setCurrentItem(selectedGraph, false)

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

        // sets the views with CategoryTotal data
        fun bind(categoryTotals : List<CategoryTotals>, type : Int) {

            // will display a message if there is no data to be displayed
            if (categoryTotals.isNotEmpty()) {

                emptyTextView.isVisible = false
                pieChart     .isVisible = true

                val typeName : String

                // list of values to be displayed in PieChart
                val pieEntries: MutableList<PieEntry> = mutableListOf()

                // adds values in categoryTotals list into list holding chart data
                categoryTotals.forEach {

                    pieEntries.add(PieEntry(it.total.toFloat(), it.category))
                }

                // PieDataSet set up
                val dataSet = PieDataSet(pieEntries, "Transactions")
                // distance between slices
                dataSet.sliceSpace     = 2.0f
                // size of highlighted area
                dataSet.selectionShift = 3.0f
                dataSet.valueTextSize  = 10f
                // sets up the colors and typeName depending on type
                if (type == 0) {

                    typeName = getString(R.string.expense)
                    context?.let {
                        dataSet.setColors(intArrayOf(R.color.expenseColor1, R.color.expenseColor2,
                            R.color.expenseColor3, R.color.expenseColor4), context)
                    }
                } else {

                    typeName = getString(R.string.income)
                    context?.let {
                        dataSet.setColors(intArrayOf(R.color.incomeColor1, R.color.incomeColor2,
                            R.color.incomeColor3, R.color.incomeColor4), context)
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
                pieChart.invalidate()
            } else {

                emptyTextView.isVisible = true
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