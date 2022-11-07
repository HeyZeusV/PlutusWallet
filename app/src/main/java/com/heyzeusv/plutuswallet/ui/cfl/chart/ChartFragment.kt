package com.heyzeusv.plutuswallet.ui.cfl.chart

import android.annotation.SuppressLint
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
import com.heyzeusv.plutuswallet.data.model.FilterInfo
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
            ContextCompat.getColor(requireContext(), R.color.colorExpenseOne),
            ContextCompat.getColor(requireContext(), R.color.colorExpenseTwo),
            ContextCompat.getColor(requireContext(), R.color.colorExpenseThree),
            ContextCompat.getColor(requireContext(), R.color.colorExpenseFour)
        )
        val inColors: List<Int> = listOf(
            ContextCompat.getColor(requireContext(), R.color.colorIncomeOne),
            ContextCompat.getColor(requireContext(), R.color.colorIncomeThree),
            ContextCompat.getColor(requireContext(), R.color.colorIncomeTwo),
            ContextCompat.getColor(requireContext(), R.color.colorIncomeFour)
        )

        cflVM.tInfoLiveData.observe(viewLifecycleOwner, { tInfo: FilterInfo ->
            // LiveData of list of CategoryTotals
            val ctLiveData: LiveData<List<CategoryTotals>> =
                chartVM.filteredCategoryTotals(
                    tInfo.account, tInfo.date, tInfo.accountNames, tInfo.start, tInfo.end
                )

            ctLiveData.observe(viewLifecycleOwner, { ctList: List<CategoryTotals> ->
                // prepares list of ItemViewCharts that will be used to create PieCharts
                chartVM.prepareLists(ctList, tInfo.category, tInfo.type)
                chartVM.prepareTotals(tInfo.category, tInfo.categoryNames, tInfo.type)
                prepareTotalTexts()
                chartVM.prepareIvcAdapter(
                    tInfo.category, tInfo.categoryNames, tInfo.type,
                    getString(R.string.type_expense), getString(R.string.type_income),
                    exColors, inColors
                )

                // sets up Dots Indicator with ViewPager2
                binding.chartCi.setViewPager(binding.chartVp)
            })
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        chartVM.adapter?.let {
            // checks if there has been a change in settings, updates changes, and updates list
            if (sharedPref[Key.KEY_CHART_CHANGED, false]
                && it.currentList.size == 2
            ) {
                setVals = SettingsUtils.prepareSettingValues(sharedPref)
                prepareTotalTexts()
                it.currentList[0].totalText = chartVM.exTotText
                it.currentList[1].totalText = chartVM.inTotText
                it.notifyDataSetChanged()
                sharedPref[Key.KEY_CHART_CHANGED] = false
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
                    R.string.chart_amount,
                    setVals.currencySymbol, setVals.decimalFormatter.format(chartVM.exTotal)
                )
                chartVM.inTotText = getString(
                    R.string.chart_amount,
                    setVals.currencySymbol, setVals.decimalFormatter.format(chartVM.inTotal)
                )
            }
            setVals.decimalPlaces -> {
                chartVM.exTotText = getString(
                    R.string.chart_amount,
                    setVals.decimalFormatter.format(chartVM.exTotal), setVals.currencySymbol
                )
                chartVM.inTotText = getString(
                    R.string.chart_amount,
                    setVals.decimalFormatter.format(chartVM.inTotal), setVals.currencySymbol
                )
            }
            setVals.symbolSide -> {
                chartVM.exTotText = getString(
                    R.string.chart_amount,
                    setVals.currencySymbol, setVals.integerFormatter.format(chartVM.exTotal)
                )
                chartVM.inTotText = getString(
                    R.string.chart_amount,
                    setVals.currencySymbol, setVals.integerFormatter.format(chartVM.inTotal)
                )
            }
            else -> {
                chartVM.exTotText = getString(
                    R.string.chart_amount,
                    setVals.integerFormatter.format(chartVM.exTotal), setVals.currencySymbol
                )
                chartVM.inTotText = getString(
                    R.string.chart_amount,
                    setVals.integerFormatter.format(chartVM.inTotal), setVals.currencySymbol
                )
            }
        }
    }
}