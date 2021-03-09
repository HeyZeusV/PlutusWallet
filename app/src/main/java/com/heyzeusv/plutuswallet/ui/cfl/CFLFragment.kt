package com.heyzeusv.plutuswallet.ui.cfl

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
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
            filterInteraction()
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
                    if (filterShown) {
                        filterInteraction()
                    } else {
                        filterInteraction()
                    }
                    true
                }
                else -> false
            }
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()

        // closes filter if it is open and user moves away from Overview page
        if (filterShown) filterInteraction()
    }

    /**
     *  Either opens or closes filter depending on filterShown.
     */
    private fun filterInteraction() {

        // setting the new constraints for FilterFragment
        constraints.connect(
            R.id.fragment_filter_container, ConstraintSet.BOTTOM,
            if (filterShown) R.id.cfl_constraint else R.id.cfl_filter_anchor, ConstraintSet.TOP, 0
        )
        // raises mask in order to appear above everything except ToolBar and FilterFragment
        binding.cflFilterMask.elevation = 1f
        binding.cflFilterMask.animate().apply {
            interpolator = LinearInterpolator()
            // if filter is shown then after animation lower mask elevation to be behind everything
            setListener(if (filterShown) {
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.cflFilterMask.elevation = -1f
                    }
                }
            } else {
                null
            })
            duration = 400
            // alpha change depending on if filter is shown
            alpha(if (filterShown) 0f else .547f)
            start()
        }

        // transition is only used to make filter animation last the same as mask alpha animation
        val transition = AutoTransition()
        transition.duration = 400

        // apply constraints and start transition
        constraints.applyTo(binding.cflConstraint)
        TransitionManager.beginDelayedTransition(binding.cflConstraint, transition)
        filterShown = !filterShown
    }
}