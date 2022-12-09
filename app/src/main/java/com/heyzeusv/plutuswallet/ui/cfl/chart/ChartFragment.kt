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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false)
        binding.lifecycleOwner = activity


        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
//
//        chartVM.adapter?.let {
//            // checks if there has been a change in settings, updates changes, and updates list
//            if (sharedPref[Key.KEY_CHART_CHANGED, false]
//                && it.currentList.size == 2
//            ) {
//                setVals = SettingsUtils.prepareSettingValues(sharedPref)
//                prepareTotalTexts()
//                it.currentList[0].totalText = chartVM.exTotText
//                it.currentList[1].totalText = chartVM.inTotText
//                it.notifyDataSetChanged()
//                sharedPref[Key.KEY_CHART_CHANGED] = false
//            }
//        }
    }
}