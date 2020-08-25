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

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        val filterFragment          : FilterFragment          = FilterFragment.newInstance()
        val chartFragment           : ChartFragment           = ChartFragment.newInstance()
        val transactionListFragment : TransactionListFragment = TransactionListFragment.newInstance()

        // starts fragment transaction, replaces fragments, and then commits it
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_filter_container  , filterFragment         )
            .replace(R.id.fragment_chart_container   , chartFragment          )
            .replace(R.id.fragment_tranlist_container, transactionListFragment)
            .addToBackStack(null)
            .commit()
    }

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

                    // scroll back to top of list
                    cflVM.filterChanged = true
                    // show new TransactionFragment
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                            R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_tran_container,
                            TransactionFragment.newInstance(-1, true))
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }
        }

        return binding.root
    }

    companion object {

        /**
         *  Initializes instance of CFLFragment.
         */
        fun newInstance() : CFLFragment {

            return CFLFragment()
        }
    }
}