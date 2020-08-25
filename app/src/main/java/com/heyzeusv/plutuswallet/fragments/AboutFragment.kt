package com.heyzeusv.plutuswallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.FragmentAboutBinding

/**
 *  Displays information about Application such as version and external libraries used.
 */
class AboutFragment : Fragment() {

    // DataBinding
    private lateinit var binding : FragmentAboutBinding

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?,
                              savedInstanceState : Bundle?) : View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // navigates user back to CFLFragment
        binding.aboutTopBar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        // setting up click listeners that display/hide text
        // have to change some constraints in order to properly display text
        binding.aboutChangelogMb.setOnClickListener {

            when (binding.aboutChangelogSv.visibility) {
                View.GONE -> {
                    binding.aboutChangelogSv.visibility = View.VISIBLE
                    setNewConstraints(R.id.about_developer_tv, R.id.spacer5)
                }
                else -> {
                    binding.aboutChangelogSv.visibility = View.GONE
                    setNewConstraints(R.id.about_developer_tv, R.id.spacer4)
                }
            }
        }

        binding.aboutCiMb.setOnClickListener {

            when (binding.aboutCiSv.visibility) {
                View.GONE -> {
                    binding.aboutCiSv.visibility = View.VISIBLE
                    setNewConstraints(R.id.about_mpc_tv, R.id.spacer2)
                }
                else -> {
                    binding.aboutCiSv.visibility = View.GONE
                    setNewConstraints(R.id.about_mpc_tv, R.id.spacer1)
                }
            }
        }

        binding.aboutMpcMb.setOnClickListener {

            binding.aboutMpcSv.visibility = when (binding.aboutMpcSv.visibility) {
                View.GONE -> View.VISIBLE
                else      -> View.GONE
            }
        }
    }

    /**
     *  Changes the constraints of the given View Ids.
     *
     *  @param mainView TextView to be displayed
     *  @param spacer   Id of view used to give proper spacing of mainView.
     */
    private fun setNewConstraints(mainView : Int, spacer : Int) {

        val constraints = ConstraintSet()
        constraints.clone(binding.aboutConstraint)
        constraints.connect(mainView, ConstraintSet.TOP,
        spacer, ConstraintSet.BOTTOM, 0)
        constraints.applyTo(binding.aboutConstraint)
    }
}