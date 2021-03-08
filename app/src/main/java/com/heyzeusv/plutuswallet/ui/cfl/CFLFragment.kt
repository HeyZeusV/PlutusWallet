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
import androidx.transition.TransitionManager
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

    var filterShown = false

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
                    val constraints = ConstraintSet()
                    constraints.clone(binding.cflConstraint)
                    filterShown = if (filterShown) {
                        constraints.connect(
                            R.id.fragment_filter_container, ConstraintSet.BOTTOM,
                            R.id.cfl_constraint, ConstraintSet.TOP, 0
                        )
                        !filterShown
                    } else {
                        constraints.connect(
                            R.id.fragment_filter_container, ConstraintSet.BOTTOM,
                            R.id.cfl_filter_anchor, ConstraintSet.TOP, 0
                        )
                        !filterShown
                    }
                    constraints.applyTo(binding.cflConstraint)
                    TransitionManager.beginDelayedTransition(binding.cflConstraint)
                    true
                }
                else -> false
            }
        }

        return binding.root
    }
}