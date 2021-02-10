package com.heyzeusv.plutuswallet.ui.cfl.chart

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
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.TransactionInfo
import com.heyzeusv.plutuswallet.databinding.FragmentChartBinding
import com.heyzeusv.plutuswallet.ui.base.BaseFragment
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.PreferenceHelper.set
import com.heyzeusv.plutuswallet.util.SettingsUtils
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel

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
    ): View {

        chartVM.adapter = ChartAdapter().apply { submitList(chartVM.ivcList) }
        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false)
        binding.lifecycleOwner = activity
        binding.chartVM = chartVM

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exColors: List<Int> = listOf(
            ContextCompat.getColor(requireContext(), R.color.income_green),
            ContextCompat.getColor(requireContext(), R.color.income_teal),
            ContextCompat.getColor(requireContext(), R.color.income_blue),
            ContextCompat.getColor(requireContext(), R.color.income_purple)
        )
        val inColors: List<Int> = listOf(
            ContextCompat.getColor(requireContext(), R.color.expense_red),
            ContextCompat.getColor(requireContext(), R.color.expense_yellow),
            ContextCompat.getColor(requireContext(), R.color.expense_pink),
            ContextCompat.getColor(requireContext(), R.color.expense_orange)
        )

        cflVM.tInfoLiveData.observe(viewLifecycleOwner, { tInfo: TransactionInfo ->
            // LiveData of list of CategoryTotals
            val ctLiveData: LiveData<List<CategoryTotals>> =
                chartVM.filteredCategoryTotals(
                    tInfo.account, tInfo.date, tInfo.accountName, tInfo.start, tInfo.end
                )

            ctLiveData.observe(viewLifecycleOwner, { ctList: List<CategoryTotals> ->
                // prepares list of ItemViewCharts that will be used to create PieCharts
                chartVM.prepareLists(ctList, tInfo.category, tInfo.type)
                chartVM.prepareTotals(tInfo.category, tInfo.categoryName, tInfo.type)
                prepareTotalTexts()
                chartVM.prepareIvgAdapter(
                    tInfo.category, tInfo.categoryName, tInfo.type,
                    getString(R.string.type_expense), getString(R.string.type_income),
                    exColors, inColors
                )

                // sets up Dots Indicator with ViewPager2
                binding.chartCi.setViewPager(binding.chartVp)
            })
        })
    }

    override fun onResume() {
        super.onResume()

        chartVM.adapter?.let {
            // checks if there has been a change in settings, updates changes, and updates list
            if (sharedPref[Key.KEY_CHART_CHANGE, false]
                && it.currentList.size == 2
            ) {
                setVals = SettingsUtils.prepareSettingValues(sharedPref)
                prepareTotalTexts()
                it.currentList[0].totalText = chartVM.exTotText
                it.currentList[1].totalText = chartVM.inTotText
                it.notifyDataSetChanged()
                sharedPref[Key.KEY_CHART_CHANGE] = false
            }
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