package com.heyzeusv.plutuswallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.CategoryTotals
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo
import com.heyzeusv.plutuswallet.databinding.FragmentChartBinding
import com.heyzeusv.plutuswallet.viewmodels.FGLViewModel
import com.heyzeusv.plutuswallet.viewmodels.GraphViewModel

private const val TAG = "PWGraphFragment"

/**
 *   Creates and populates charts with Transaction data depending on filter applied.
 */
class GraphFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding : FragmentChartBinding

    // shared ViewModel
    private lateinit var fglViewModel : FGLViewModel

    // provides instance of GraphViewModel
    private val graphVM : GraphViewModel by lazy {
        ViewModelProviders.of(this).get(GraphViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?,
                              savedInstanceState : Bundle?) : View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false)
        binding.lifecycleOwner = activity
        binding.graphVM        = graphVM

        // sending data that requires context to graphVM
        graphVM.expense  = getString(R.string.type_expense)
        graphVM.income   = getString(R.string.type_income )
        graphVM.exColors = listOf(ContextCompat.getColor(context!!, R.color.colorExpense1),
                                  ContextCompat.getColor(context!!, R.color.colorExpense2),
                                  ContextCompat.getColor(context!!, R.color.colorExpense3),
                                  ContextCompat.getColor(context!!, R.color.colorExpense4))
        graphVM.inColors = listOf(ContextCompat.getColor(context!!, R.color.colorIncome1),
                                  ContextCompat.getColor(context!!, R.color.colorIncome2),
                                  ContextCompat.getColor(context!!, R.color.colorIncome3),
                                  ContextCompat.getColor(context!!, R.color.colorIncome4))

        val view : View = binding.root

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

                // LiveData of list of CategoryTotals
                val ctLiveData : LiveData<List<CategoryTotals>> =
                    graphVM.filteredCategoryTotals(tInfo.account, tInfo.date, tInfo.accountName,
                        tInfo.start, tInfo.end)

                // register an observer on LiveData instance and tie life to this component
                // execute code whenever LiveData gets update
                ctLiveData.observe( this, Observer { ctList : List<CategoryTotals> ->

                    // prepares list of ItemViewGraphs that will be used to create PieCharts
                    graphVM.prepareLists(ctList)
                    graphVM.prepareTotals(tInfo.category, tInfo.categoryName, tInfo.type)
                    prepareTotalTexts()
                    graphVM.prepareIvgAdapter(tInfo.category, tInfo.categoryName, tInfo.type)

                    // sets up Dots Indicator with ViewPager2
                    binding.graphCircleIndicator.setViewPager(binding.graphViewPager)
                })
            }
        )
    }

    override fun onResume() {
        super.onResume()

        // language change does not cause LiveData update... so this will update total strings
        prepareTotalTexts()
    }

    /**
     *  Translations require context, so rather than have ViewModel with context, use GraphFragment's
     *  context to translate strings depending on settings and then send them to ViewModel.
     */
    private fun prepareTotalTexts() {

        if (decimalPlaces) {

            if (symbolSide) {

                graphVM.exTotText = getString(R.string.graph_total,
                    currencySymbol, decimalFormatter.format(graphVM.exTotal))
                graphVM.inTotText = getString(R.string.graph_total,
                    currencySymbol, decimalFormatter.format(graphVM.inTotal))
            } else {

                graphVM.exTotText = getString(R.string.graph_total,
                    decimalFormatter.format(graphVM.exTotal), currencySymbol)
                graphVM.inTotText = getString(R.string.graph_total,
                    decimalFormatter.format(graphVM.inTotal), currencySymbol)
            }
        } else {

            if (symbolSide) {

                graphVM.exTotText = getString(R.string.graph_total,
                    currencySymbol, integerFormatter.format(graphVM.exTotal))
                graphVM.inTotText = getString(R.string.graph_total,
                    currencySymbol, integerFormatter.format(graphVM.inTotal))
            } else {

                graphVM.exTotText = getString(R.string.graph_total,
                    integerFormatter.format(graphVM.exTotal), currencySymbol)
                graphVM.inTotText = getString(R.string.graph_total,
                    integerFormatter.format(graphVM.inTotal), currencySymbol)
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