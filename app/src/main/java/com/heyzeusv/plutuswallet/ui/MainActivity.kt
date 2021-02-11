package com.heyzeusv.plutuswallet.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.ActivityMainBinding
import com.heyzeusv.plutuswallet.ui.base.BaseActivity
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.PreferenceHelper.set
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

        AppCompatDelegate.setDefaultNightMode(sharedPref[Key.KEY_THEME, "-1"].toInt())

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

        // loads if view mode changed
        val themeChanged: Boolean = sharedPref[Key.KEY_THEME_CHANGED, false]
        if (themeChanged) {
            sharedPref[Key.KEY_THEME_CHANGED] = false
            // destroys then restarts Activity in order to have updated theme
            recreate()
        }

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