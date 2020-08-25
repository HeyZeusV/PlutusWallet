package com.heyzeusv.plutuswallet.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.ActivityMainBinding
import com.heyzeusv.plutuswallet.fragments.AboutFragment
import com.heyzeusv.plutuswallet.fragments.AccountFragment
import com.heyzeusv.plutuswallet.fragments.CategoryFragment
import com.heyzeusv.plutuswallet.fragments.CFLFragment
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

        // FragmentManager adds fragments to an activity
        val currentFragment : Fragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_tran_container)

        // would not be null if activity is destroyed and recreated
        // because FragmentManager saves list of fragments
        if (currentFragment == null) {

            val cflFragment : CFLFragment = CFLFragment.newInstance()

            // Create a new fragment transaction, adds fragments, and then commit it
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_tran_container, cflFragment)
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()

        // Listener for NavigationDrawer
        binding.activityNavView.setNavigationItemSelectedListener {

            return@setNavigationItemSelectedListener when (it.itemId) {

                // starts AccountFragment
                R.id.menu_accounts -> {

                    // instance of AccountFragment
                    val accountFragment : AccountFragment = AccountFragment.newInstance()

                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_tran_container, accountFragment)
                        .addToBackStack(null)
                        .commit()

                    binding.activityDrawer.closeDrawer(GravityCompat.START)
                    true
                }
                // starts CategoryFragment
                R.id.menu_categories -> {

                    // instance of CategoryFragment
                    val categoryFragment : CategoryFragment = CategoryFragment.newInstance()

                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_tran_container, categoryFragment)
                        .addToBackStack(null)
                        .commit()

                    binding.activityDrawer.closeDrawer(GravityCompat.START)
                    true
                }
                // starts SettingsActivity
                R.id.menu_set -> {

                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                    binding.activityDrawer.closeDrawer(GravityCompat.START)
                    true
                }
                // starts AboutFragment
                // R.id.about
                else -> {

                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                            R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.fragment_tran_container, AboutFragment())
                        .addToBackStack(null)
                        .commit()

                    binding.activityDrawer.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
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
            .replace(R.id.fragment_tran_container, transactionFragment)
            .addToBackStack(null)
            .commit()

    }
}