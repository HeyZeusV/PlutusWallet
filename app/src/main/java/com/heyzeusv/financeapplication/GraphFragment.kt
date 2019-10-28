 package com.heyzeusv.financeapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.heyzeusv.financeapplication.utilities.BaseFragment
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

 private const val  TAG = "GraphFragment"

/**
 *   Creates and populates charts with Transaction data depending on filter applied.
 */
class GraphFragment : BaseFragment() {

    //views
    private lateinit var graphViewPager    : ViewPager2
    private lateinit var wormDotsIndicator : WormDotsIndicator

    // lists used to hold CategoryTotals
    private var emptyList        : List<CategoryTotals>              = emptyList()
    private var transactionLists : MutableList<List<CategoryTotals>> = mutableListOf(emptyList, emptyList)


    // provides instance of ViewModel
    private val graphViewModel : GraphViewModel by lazy {
        ViewModelProviders.of(this).get(GraphViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_graph, container, false)

        graphViewPager    = view.findViewById(R.id.graph_view_pager)          as ViewPager2
        wormDotsIndicator = view.findViewById(R.id.graph_worm_dots_indicator) as WormDotsIndicator

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

    /**
     *  Ensures the UI is up to date with correct information.
     *
     *  @param transactionLists list of lists of CategoryTotals to be converted to graphs.
     */
    private fun updateUI(transactionLists : MutableList<List<CategoryTotals>>) {

        // creates GraphAdapter to set with ViewPager2
        graphViewPager   .adapter = GraphAdapter(transactionLists)
        // sets up Dots Indicator with ViewPager2
        wormDotsIndicator.setViewPager2(graphViewPager)
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
        private val pieChart : PieChart = itemView.findViewById(R.id.graph_pie)

        // sets the views with CategoryTotal data
        fun bind(categoryTotals : List<CategoryTotals>, type : Int) {

            // list of values to be displayed in PieChart
            val pieEntries : MutableList<PieEntry> = mutableListOf()

            // adds values in categoryTotals list into list holding chart data
            categoryTotals.forEach() {

                pieEntries.add(PieEntry(it.total.toFloat(), it.category))
            }

            // will clean this up...
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
            pieChart.setTouchEnabled(false)
            pieChart.isDrawHoleEnabled = true
            pieChart.setUsePercentValues(true)
            pieChart.description = description
            pieChart.animateX(1000, Easing.EaseInBack)
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