package com.heyzeusv.plutuswallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.FragmentCflBinding
import com.heyzeusv.plutuswallet.viewmodels.CFLViewModel

/**
 *  This Fragment will be used to nest Chart/Filter/List Fragments.
 */
class CFLFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding : FragmentCflBinding

    // shared ViewModels
    private lateinit var cflVM : CFLViewModel

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?,
                              savedInstanceState : Bundle?) : View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cfl, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        // this ensures that this is same CFLViewModel as Filter/ChartFragment use
        cflVM = requireActivity().let {

            ViewModelProvider(it).get(CFLViewModel::class.java)
        }

        // opens NavigationDrawer
        binding.cflTopBar.setNavigationOnClickListener {

            requireActivity().findViewById<DrawerLayout>(R.id.activity_drawer)
                .openDrawer(GravityCompat.START)
        }

        // handles menu selection
        binding.cflTopBar.setOnMenuItemClickListener { item : MenuItem ->

            when (item.itemId) {
                R.id.cfl_new_tran -> {

                    // creates action with parameters
                    val action : NavDirections =
                        CFLFragmentDirections.actionTransaction(-1, true)
                    findNavController().navigate(action)
                    // scroll back to top of list
                    cflVM.filterChanged = true
                    true
                }
                else -> false
            }
        }

        return binding.root
    }
}