package com.heyzeusv.plutuswallet.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.MobileAds
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.billingrepo.localdb.AugmentedSkuDetails
import com.heyzeusv.plutuswallet.fragments.AccountFragment
import com.heyzeusv.plutuswallet.fragments.CategoryFragment
import com.heyzeusv.plutuswallet.fragments.FGLFragment
import com.heyzeusv.plutuswallet.fragments.TransactionFragment
import com.heyzeusv.plutuswallet.fragments.TransactionListFragment
import com.heyzeusv.plutuswallet.utilities.KEY_LANGUAGE_CHANGED
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set
import com.heyzeusv.plutuswallet.viewmodels.BillingViewModel

private const val TAG = "MainActivity"

/**
 *  Handles the loading and replacement of fragments into their containers, as well as
 *  starting Settings/About Activities.
 */
class MainActivity : BaseActivity(), TransactionListFragment.Callbacks {

    // views
    private lateinit var drawerLayout   : DrawerLayout
    private lateinit var fab            : FloatingActionButton
    private lateinit var backButton     : MaterialButton
    private lateinit var menuButton     : MaterialButton
    private lateinit var navigationView : NavigationView

    // ViewModel
    private lateinit var billingViewModel : BillingViewModel

    // details for NoAds IAP
    private lateinit var noAdsDetails : AugmentedSkuDetails

    // position of FAB, depends on device
    private var fabX = 0
    private var fabY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize ads
        MobileAds.initialize(this) {}

        // initialize views
        drawerLayout   = findViewById(R.id.activity_drawer         )
        fab            = findViewById(R.id.activity_fab            )
        backButton     = findViewById(R.id.activity_back           )
        menuButton     = findViewById(R.id.activity_settings       )
        navigationView = findViewById(R.id.activity_navigation_view)

        // disables swipe to open drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        // FragmentManager adds fragments to an activity
        val currentFragment : Fragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_transaction_container)

        // would not be null if activity is destroyed and recreated
        // because FragmentManager saves list of fragments
        if (currentFragment == null) {

            val fglFragment : FGLFragment = FGLFragment.newInstance()

            // Create a new fragment transaction, adds fragments, and then commit it
            supportFragmentManager
                .beginTransaction()
                // container view ID (where fragment's view should appear)
                // fragment to be added
                .add(R.id.fragment_transaction_container, fglFragment)
                .commit()
        }

        // getting instance of BillingViewModel
        billingViewModel = ViewModelProviders.of(this).get(BillingViewModel::class.java)
        // observing List of SKUDetails LiveData
        billingViewModel.inappSkuDetailsListLiveData.observe(this, Observer {

            it?.let {

                if (it.isNotEmpty()) {

                    noAdsDetails = it[0]
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()

        // Listener for NavigationDrawer
        navigationView.setNavigationItemSelectedListener {

            return@setNavigationItemSelectedListener when (it.itemId) {

                // starts AccountFragment
                R.id.accounts -> {

                    backButton.visibility = View.VISIBLE
                    menuButton.visibility = View.INVISIBLE

                    // instance of AccountFragment
                    val accountFragment : AccountFragment = AccountFragment.newInstance()

                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_transaction_container, accountFragment)
                        .addToBackStack(null)
                        .commit()

                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                // starts CategoryFragment
                R.id.categories -> {

                    backButton.visibility = View.VISIBLE
                    menuButton.visibility = View.INVISIBLE

                    // instance of CategoryFragment
                    val categoryFragment : CategoryFragment = CategoryFragment.newInstance()

                    supportFragmentManager
                        .beginTransaction()
                        // replace fragment hosted at location with new fragment provided
                        // will add fragment even if there is none
                        .replace(R.id.fragment_transaction_container, categoryFragment)
                        // pressing back button will go back to previous fragment (if any)
                        .addToBackStack(null)
                        .commit()

                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // starts SettingsActivity
                R.id.settings -> {

                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(settingsIntent)

                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // starts AboutActivity
                R.id.about -> {

                    val aboutIntent = Intent(this, AboutActivity::class.java)
                    startActivity(aboutIntent)

                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // starts NoAds IAP
                else -> {

                    billingViewModel.makePurchase(this, noAdsDetails)
                    true
                }
            }
        }

        // clicking the menuButton will open drawer
        menuButton.setOnClickListener {

            drawerLayout.openDrawer(GravityCompat.START)
        }

        backButton.setOnClickListener {

            onBackPressed()
        }

        billingViewModel.noAdsLiveData.observe(this, Observer {

            it?.let {

                // makes No Ads option visible depending if user is entitled to it
                val navMenu : Menu = navigationView.menu
                navMenu.findItem(R.id.no_ads).isVisible = !it.entitled
            }
        })
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
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {

                drawerLayout.closeDrawer(GravityCompat.START)
            }
            menuButton.visibility == View.INVISIBLE -> {

                backButton.visibility = View.INVISIBLE
                menuButton.visibility = View.VISIBLE
                super.onBackPressed()
            }
            else -> {

                // moves menuButton back into view
                ObjectAnimator.ofFloat(menuButton, "translationX", 0f).apply {

                    duration = 400
                    start()
                }
                super.onBackPressed()
            }
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

        // Create a new fragment transaction, adds fragments,
        // and then commit it
        supportFragmentManager
            .beginTransaction()
            // animations for switching out fragments
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            // replace fragment hosted at location with new fragment provided
            // will add fragment even if there is none
            .replace(R.id.fragment_transaction_container, transactionFragment)
            // pressing back button will go back to previous fragment (if any)
            .addToBackStack(null)
            .commit()

        // moves menuButton off screen
        ObjectAnimator.ofFloat(menuButton, "translationX", -200f).apply {

            duration = 400
            start()
        }
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