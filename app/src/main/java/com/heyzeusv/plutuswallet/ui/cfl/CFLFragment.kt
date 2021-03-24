package com.heyzeusv.plutuswallet.ui.cfl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.FragmentCflBinding

/**
 *  This Fragment will be used to nest Chart/Filter/List Fragments.
 */
class CFLFragment : Fragment() {

    // DataBinding
    private lateinit var binding: FragmentCflBinding

    // shared ViewModels
    private val cflVM: CFLViewModel by activityViewModels()

    // constraint set used by filter animation
    private val constraints = ConstraintSet()

    private var filterShown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cfl, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        // opens NavigationDrawer
        binding.cflTopBar.setNavigationOnClickListener {
            requireActivity()
                .findViewById<DrawerLayout>(R.id.activity_drawer)
                .openDrawer(GravityCompat.START)
        }

        constraints.clone(binding.cflConstraint)

        // clicking outside the filter area will close it
        binding.cflFilterMask.setOnClickListener {
            binding.cflConstraint.transitionToStart()
            filterShown = false
        }

        // handles menu selection
        binding.cflTopBar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.cfl_new_tran -> {
                    // creates action with parameters
                    val action: NavDirections =
                        CFLFragmentDirections.actionTransaction()
                    findNavController().navigate(action)
                    // scroll back to top of list
                    cflVM.filterChanged = true
                    true
                }
                R.id.cfl_edit_filter -> {
                    filterShown = if (filterShown) {
                        binding.cflConstraint.transitionToStart()
                        false
                    } else {
                        binding.cflConstraint.transitionToEnd()
                        true
                    }
                    true
                }
                else -> false
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cflVM.tInfoLiveData.observe(viewLifecycleOwner, {
            if (filterShown) {
                binding.cflConstraint.transitionToStart()
                filterShown = false
            }
        })
    }

    override fun onPause() {
        super.onPause()

        // closes filter if it is open and user moves away from Overview page
        if (filterShown) binding.cflConstraint.transitionToStart()
    }
}