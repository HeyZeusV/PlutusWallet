package com.heyzeusv.plutuswallet.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.fragments.BlankFragment
import com.heyzeusv.plutuswallet.fragments.FilterFragment
import com.heyzeusv.plutuswallet.fragments.GraphFragment
import com.heyzeusv.plutuswallet.fragments.TransactionFragment
import com.heyzeusv.plutuswallet.fragments.TransactionListFragment
import com.heyzeusv.plutuswallet.utilities.KEY_LANGUAGE_CHANGED
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set

private const val TAG = "MainActivity"

/**
 *  Handles the loading and replacement of fragments into their containers, as well as
 *  starting Settings/About Activities.
 */
class MainActivity : BaseActivity(), TransactionListFragment.Callbacks {

    // views
    private lateinit var drawerLayout   : DrawerLayout
    private lateinit var fab            : FloatingActionButton
    private lateinit var menuButton     : MaterialButton
    private lateinit var navigationView : NavigationView

    // position of FAB, depends on device
    private var fabX = 0
    private var fabY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize views
        drawerLayout   = findViewById(R.id.activity_drawer         )
        fab            = findViewById(R.id.activity_fab            )
        menuButton     = findViewById(R.id.fragment_settings       )
        navigationView = findViewById(R.id.activity_navigation_view)

        // disables swipe to open drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        // FragmentManager adds fragments to an activity
        val currentFragment : Fragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_transaction_list_container)

        // would not be null if activity is destroyed and recreated
        // because FragmentManager saves list of fragments
        if (currentFragment == null) {

            val transactionListFragment : TransactionListFragment = TransactionListFragment.newInstance()
            val filterFragment          : FilterFragment          = FilterFragment         .newInstance()
            val graphFragment           : GraphFragment           = GraphFragment          .newInstance()

            // Create a new fragment transaction, adds fragments, and then commit it
            supportFragmentManager
                .beginTransaction()
                // container view ID (where fragment's view should appear)
                // fragment to be added
                .add(R.id.fragment_transaction_list_container, transactionListFragment)
                .add(R.id.fragment_filter_container          , filterFragment         )
                .add(R.id.fragment_graph_container           , graphFragment          )
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()

        // Listener for NavigationDrawer
        navigationView.setNavigationItemSelectedListener {

            return@setNavigationItemSelectedListener when (it.itemId) {

                // starts SettingsActivity
                R.id.settings -> {

                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // starts AboutActivity
                else -> {

                    val aboutIntent = Intent(this, AboutActivity::class.java)
                    startActivity(aboutIntent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }

        // clicking the menuButton will open drawer
        menuButton.setOnClickListener {

            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onResume() {
        super.onResume()

        // loads if language changed
        val languageChanged : Boolean = sharedPreferences[KEY_LANGUAGE_CHANGED, false]!!
        if (languageChanged) {

            // saving into SharedPreferences
            sharedPreferences[KEY_LANGUAGE_CHANGED] = false

            // destroys then restarts Activity in order to have updated language
            recreate()
        }
    }

    override fun onBackPressed() {

        // close drawer if open else do regular behavior
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

            drawerLayout.closeDrawer(GravityCompat.START)
        } else {

            menuButton.visibility = View.VISIBLE
            super.onBackPressed()
        }
    }

    /**
     *  Replaces TransactionListFragment, FilterFragment, and GraphFragment with TransactionFragment selected.
     *
     *  @param transactionId id of Transaction selected.
     *  @param fromFab       true if user clicked on FAB to create Transaction.
     */
    override fun onTransactionSelected(transactionId: Int, fromFab : Boolean) {

        getFabLocation()

        val transactionFragment : TransactionFragment = TransactionFragment.newInstance(transactionId, fabX, fabY, fromFab)
        val blankFragment                             = BlankFragment()
        val blankFragment2                            = BlankFragment()
        val blankFragment3                            = BlankFragment()

        // Create a new fragment transaction, adds fragments,
        // and then commit it
        supportFragmentManager
            .beginTransaction()
            // replace fragment hosted at location with new fragment provided
            // will add fragment even if there is none
            .add(R.id.fragment_transaction_container         , transactionFragment)
            .replace(R.id.fragment_transaction_list_container, blankFragment      )
            .replace(R.id.fragment_filter_container          , blankFragment2     )
            .replace(R.id.fragment_graph_container           , blankFragment3     )
            // pressing back button will go back to previous fragment (if any)
            .addToBackStack(null)
            .commit()

        menuButton.visibility = View.INVISIBLE
    }

    /**
     *  Gets location of FAB button in order to start animation from correct location.
     */
    private fun getFabLocation() {

        val fabLocationArray = IntArray(2)
        fab.getLocationOnScreen(fabLocationArray)
        fabX = fabLocationArray[0] + fab.width / 2
        fabY = fabLocationArray[1] - fab.height
    }
}