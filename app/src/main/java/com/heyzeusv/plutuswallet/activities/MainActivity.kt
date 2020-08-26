package com.heyzeusv.plutuswallet.activities

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.ActivityMainBinding
import com.heyzeusv.plutuswallet.fragments.TransactionFragment
import com.heyzeusv.plutuswallet.fragments.TransactionListFragment
import com.heyzeusv.plutuswallet.utilities.Constants
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set

/**
 *  Handles the loading and replacement of fragments into their containers, as well as
 *  starting Settings/About Activities.
 */
class MainActivity : BaseActivity(), TransactionListFragment.Callbacks {

    // DataBinding
     lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        // setting up DataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // disables swipe to open drawer
        binding.activityDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onStart() {
        super.onStart()

        // uses nav_graph to determine where each button goes from NavigationView
        binding.activityNavView.setupWithNavController(findNavController(R.id.fragment_container))
    }

    override fun onResume() {
        super.onResume()

        // loads if language changed
        val languageChanged : Boolean = sharedPreferences[Constants.KEY_LANGUAGE_CHANGED, false]!!
        if (languageChanged) {

            // saving into SharedPreferences
            sharedPreferences[Constants.KEY_LANGUAGE_CHANGED] = false

            // destroys then restarts Activity in order to have updated language
            recreate()
        }
    }

    override fun onBackPressed() {

        when {
            // close drawer if it is open
            binding.activityDrawer.isDrawerOpen(GravityCompat.START) ->
                binding.activityDrawer.closeDrawer(GravityCompat.START)
            else -> super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp() : Boolean =
        findNavController(R.id.fragment_container).navigateUp()

    /**
     *  Replaces TransactionListFragment, FilterFragment, and ChartFragment
     *  with TransactionFragment selected.
     *
     *  @param transactionId id of Transaction selected.
     *  @param fromFab       true if user clicked on FAB to create Transaction.
     */
    override fun onTransactionSelected(transactionId: Int, fromFab : Boolean) {

        val transactionFragment : TransactionFragment =
            TransactionFragment.newInstance(transactionId, fromFab)

        // fragment transaction with sliding animation
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(R.id.fragment_container, transactionFragment)
            .addToBackStack(null)
            .commit()

    }
}