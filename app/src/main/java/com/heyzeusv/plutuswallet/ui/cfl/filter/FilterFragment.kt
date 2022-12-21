package com.heyzeusv.plutuswallet.ui.cfl.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.FragmentFilterBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 *  Used to apply filters and tell TransactionListFragment which Transaction list to load
 */
@AndroidEntryPoint
class FilterFragment : Fragment() {

    // DataBinding
    private lateinit var binding: FragmentFilterBinding

    // provides instance of FilterViewModel
    private val filterVM: FilterViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false)
        binding.lifecycleOwner = activity
        binding.filterVM = filterVM

        return binding.root
    }

}