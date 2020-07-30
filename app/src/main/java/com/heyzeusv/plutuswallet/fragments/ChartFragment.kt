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
import com.heyzeusv.plutuswallet.viewmodels.CFLViewModel
import com.heyzeusv.plutuswallet.viewmodels.ChartViewModel

private const val TAG = "PWChartFragment"

/**
 *   Creates and populates charts with Transaction data depending on filter applied.
 */
class ChartFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding : FragmentChartBinding

    // shared ViewModel
    private lateinit var cflViewModel : CFLViewModel

    // provides instance of ChartViewModel
    private val chartVM : ChartViewModel by lazy {
        ViewModelProviders.of(this).get(ChartViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?,
                              savedInstanceState : Bundle?) : View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false)
        binding.lifecycleOwner = activity
        binding.chartVM        = chartVM

        val view : View = binding.root

        // sending data that requires context to ViewModel
        chartVM.expense  = getString(R.string.type_expense)
        chartVM.income   = getString(R.string.type_income )
        chartVM.exColors = listOf(ContextCompat.getColor(context!!, R.color.colorExpense1),
                                  ContextCompat.getColor(context!!, R.color.colorExpense2),
                                  ContextCompat.getColor(context!!, R.color.colorExpense3),
                                  ContextCompat.getColor(context!!, R.color.colorExpense4))
        chartVM.inColors = listOf(ContextCompat.getColor(context!!, R.color.colorIncome1),
                                  ContextCompat.getColor(context!!, R.color.colorIncome2),
                                  ContextCompat.getColor(context!!, R.color.colorIncome3),
                                  ContextCompat.getColor(context!!, R.color.colorIncome4))

        // this ensures that this is same CFLViewModel as Filter/ListFragment use
        cflViewModel = activity!!.let {

            ViewModelProviders.of(it).get(CFLViewModel::class.java)
        }

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        cflViewModel.tInfoLiveData.observe(this, Observer { tInfo : TransactionInfo ->

            // LiveData of list of CategoryTotals
            val ctLiveData : LiveData<List<CategoryTotals>> =
                chartVM.filteredCategoryTotals(tInfo.account, tInfo.date, tInfo.accountName,
                    tInfo.start, tInfo.end)

            // register an observer on LiveData instance and tie life to this component
            // execute code whenever LiveData gets update
            ctLiveData.observe( this, Observer { ctList : List<CategoryTotals> ->

                // prepares list of ItemViewCharts that will be used to create PieCharts
                chartVM.prepareLists(ctList)
                chartVM.prepareTotals(tInfo.category, tInfo.categoryName, tInfo.type)
                prepareTotalTexts()
                chartVM.prepareIvgAdapter(tInfo.category, tInfo.categoryName, tInfo.type)

                // sets up Dots Indicator with ViewPager2
                binding.chartCircleIndicator.setViewPager(binding.chartViewPager)
            })
        })
    }

    override fun onResume() {
        super.onResume()

        // language change does not cause LiveData update... so this will update total strings
        prepareTotalTexts()
    }

    /**
     *  Translations require context, so rather than have ViewModel with context, use ChartFragment's
     *  context to translate strings depending on settings and then send them to ViewModel.
     */
    private fun prepareTotalTexts() {

        if (decimalPlaces) {

            if (symbolSide) {

                chartVM.exTotText = getString(R.string.chart_total,
                    currencySymbol, decimalFormatter.format(chartVM.exTotal))
                chartVM.inTotText = getString(R.string.chart_total,
                    currencySymbol, decimalFormatter.format(chartVM.inTotal))
            } else {

                chartVM.exTotText = getString(R.string.chart_total,
                    decimalFormatter.format(chartVM.exTotal), currencySymbol)
                chartVM.inTotText = getString(R.string.chart_total,
                    decimalFormatter.format(chartVM.inTotal), currencySymbol)
            }
        } else {

            if (symbolSide) {

                chartVM.exTotText = getString(R.string.chart_total,
                    currencySymbol, integerFormatter.format(chartVM.exTotal))
                chartVM.inTotText = getString(R.string.chart_total,
                    currencySymbol, integerFormatter.format(chartVM.inTotal))
            } else {

                chartVM.exTotText = getString(R.string.chart_total,
                    integerFormatter.format(chartVM.exTotal), currencySymbol)
                chartVM.inTotText = getString(R.string.chart_total,
                    integerFormatter.format(chartVM.inTotal), currencySymbol)
            }
        }
    }

    companion object {

        /**
         *  Initializes instance of ChartFragment
         */
        fun newInstance() : ChartFragment {

            return ChartFragment()
        }
    }
}