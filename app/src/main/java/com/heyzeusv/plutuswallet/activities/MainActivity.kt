package com.heyzeusv.plutuswallet.activities

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.ActivityMainBinding
import com.heyzeusv.plutuswallet.utilities.Key
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.get
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper.set
import dagger.hilt.android.AndroidEntryPoint

/**
 *  Handles the loading and replacement of fragments into their containers, as well as
 *  starting Settings/About Activities.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity() {

    // DataBinding
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
        val languageChanged: Boolean = sharedPref[Key.KEY_LANGUAGE_CHANGED, false]
        if (languageChanged) {
            // saving into SharedPreferences
            sharedPref[Key.KEY_LANGUAGE_CHANGED] = false
            // destroys then restarts Activity in order to have updated language
            recreate()
        }
    }

    override fun onBackPressed() {

        if (binding.activityDrawer.isDrawerOpen(GravityCompat.START)) {
            // close drawer if it is open
            binding.activityDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}