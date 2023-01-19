package com.heyzeusv.plutuswallet.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.FragmentCategoryBinding
import com.heyzeusv.plutuswallet.ui.base.BaseFragment

/**
 *  Shows all Categories depending on type in database and allows users to either
 *  edit them or delete them.
 */
class CategoryFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}