package com.heyzeusv.plutuswallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.CategoryTotals
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo
import com.heyzeusv.plutuswallet.databinding.FragmentChartBinding
import com.heyzeusv.plutuswallet.utilities.Constants
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set
import com.heyzeusv.plutuswallet.utilities.SettingsUtils
import com.heyzeusv.plutuswallet.viewmodels.CFLViewModel
import com.heyzeusv.plutuswallet.viewmodels.ChartViewModel

/**
 *   Creates and populates charts with Transaction data depending on filter applied.
 */
class ChartFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentChartBinding

    // provides instance of ChartViewModel
    private val chartVM: ChartViewModel by viewModels()
    // shared ViewModel
    private val cflVM: CFLViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false)
        binding.lifecycleOwner = activity
        binding.chartVM = chartVM

        // sending data that requires context to ViewModel
        chartVM.expense = getString(R.string.type_expense)
        chartVM.income = getString(R.string.type_income)
        chartVM.exColors = listOf(
            ContextCompat.getColor(requireContext(), R.color.colorExpense1),
            ContextCompat.getColor(requireContext(), R.color.colorExpense2),
            ContextCompat.getColor(requireContext(), R.color.colorExpense3),
            ContextCompat.getColor(requireContext(), R.color.colorExpense4)
        )
        chartVM.inColors = listOf(
            ContextCompat.getColor(requireContext(), R.color.colorIncome1),
            ContextCompat.getColor(requireContext(), R.color.colorIncome2),
            ContextCompat.getColor(requireContext(), R.color.colorIncome3),
            ContextCompat.getColor(requireContext(), R.color.colorIncome4)
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cflVM.tInfoLiveData.observe(viewLifecycleOwner, { tInfo: TransactionInfo ->
            // LiveData of list of CategoryTotals
            val ctLiveData: LiveData<List<CategoryTotals>> =
                chartVM.filteredCategoryTotals(
                    tInfo.account, tInfo.date, tInfo.accountName, tInfo.start, tInfo.end
                )

            ctLiveData.observe(viewLifecycleOwner, { ctList: List<CategoryTotals> ->
                // prepares list of ItemViewCharts that will be used to create PieCharts
                chartVM.prepareLists(ctList)
                chartVM.prepareTotals(tInfo.category, tInfo.categoryName, tInfo.type)
                prepareTotalTexts()
                chartVM.prepareIvgAdapter(tInfo.category, tInfo.categoryName, tInfo.type)

                // sets up Dots Indicator with ViewPager2
                binding.chartCi.setViewPager(binding.chartVp)
            })
        })
    }

    override fun onResume() {
        super.onResume()

        // checks if there has been a change in settings, updates changes, and updates list
        if (sharedPref[Constants.KEY_CHART_CHANGE, false]
            && chartVM.adapter.currentList.size == 2
        ) {
            setVals = SettingsUtils.prepareSettingValues(sharedPref)
            prepareTotalTexts()
            chartVM.adapter.currentList[0].totalText = chartVM.exTotText
            chartVM.adapter.currentList[1].totalText = chartVM.inTotText
            chartVM.adapter.notifyDataSetChanged()
            sharedPref[Constants.KEY_CHART_CHANGE] = false
        }
    }

    /**
     *  Translations require context, so rather than have ViewModel with context, use ChartFragment's
     *  context to translate strings depending on settings and then send them to ViewModel.
     */
    private fun prepareTotalTexts() {

        when {

            setVals.decimalPlaces && setVals.symbolSide -> {
                chartVM.exTotText = getString(
                    R.string.chart_total,
                    setVals.currencySymbol, setVals.decimalFormatter.format(chartVM.exTotal)
                )
                chartVM.inTotText = getString(
                    R.string.chart_total,
                    setVals.currencySymbol, setVals.decimalFormatter.format(chartVM.inTotal)
                )
            }
            setVals.decimalPlaces -> {
                chartVM.exTotText = getString(
                    R.string.chart_total,
                    setVals.decimalFormatter.format(chartVM.exTotal), setVals.currencySymbol
                )
                chartVM.inTotText = getString(
                    R.string.chart_total,
                    setVals.decimalFormatter.format(chartVM.inTotal), setVals.currencySymbol
                )
            }
            setVals.symbolSide -> {
                chartVM.exTotText = getString(
                    R.string.chart_total,
                    setVals.currencySymbol, setVals.integerFormatter.format(chartVM.exTotal)
                )
                chartVM.inTotText = getString(
                    R.string.chart_total,
                    setVals.currencySymbol, setVals.integerFormatter.format(chartVM.inTotal)
                )
            }
            else -> {
                chartVM.exTotText = getString(
                    R.string.chart_total,
                    setVals.integerFormatter.format(chartVM.exTotal), setVals.currencySymbol
                )
                chartVM.inTotText = getString(
                    R.string.chart_total,
                    setVals.integerFormatter.format(chartVM.inTotal), setVals.currencySymbol
                )
            }
        }
    }
}