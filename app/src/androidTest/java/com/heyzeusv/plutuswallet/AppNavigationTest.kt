package com.heyzeusv.plutuswallet

import android.view.Gravity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withIndex
import com.heyzeusv.plutuswallet.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 *  Testing all the navigation within app
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class AppNavigationTest {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var repeatRule = RepeatRule()

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    fun init() {

        // populate @Inject fields in test class
        hiltRule.inject()

        // start scenario before every test
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun close() {

        // close scenario after every test
        activityScenario.close()
    }

    @Test
    fun openDrawerUsingMenuIcon() {

        // check Drawer starts out closed
        onView(withId(R.id.activity_drawer)).check(matches(isClosed(Gravity.LEFT)))

        // open Drawer
        onView(withContentDescription(R.string.cfl_drawer_description)).perform(click())

        // check that Drawer is open
        onView(withId(R.id.activity_drawer)).check(matches(isOpen(Gravity.LEFT)))
    }

    @Test
    fun navigateToNewTransactionAndBack() {

        // click on New Transaction button in CFLFragment
        onView(withId(R.id.cfl_new_tran)).perform(click())

        // check that TransactionFragment is displayed
        onView(withId(R.id.tran_layout)).check(matches(isDisplayed()))

        // navigate back
        onView(withContentDescription(R.string.navigate_back)).perform(click())

        // check that CFLFragment is displayed
        onView(withId(R.id.cfl_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToExistingTransactionAndBack() {

        // click on existing Transaction from TransactionListFragment
        onView(withIndex(withId(R.id.ivt_layout), 2)).perform(click())

        // check that TransactionFragment is displayed
        onView(withId(R.id.tran_layout)).check(matches(isDisplayed()))

        // navigate back
        onView(withContentDescription(R.string.navigate_back)).perform(click())

        // check that CFLFragment is displayed
        onView(withId(R.id.cfl_layout)).check(matches(isDisplayed()))
    }

    @Test
    @RepeatTest(100)
    fun navigateToAccountsAndBack() {

        // check Drawer starts closed then open it
        onView(withId(R.id.activity_drawer))
            .check(matches(isClosed(Gravity.LEFT)))
            .perform(open())

        // navigate to AccountFragment
        onView(withId(R.id.activity_nav_view))
            .perform(navigateTo(R.id.accountFragment))

        /**
         *  Hate to do this, but can't stop drawer closing animation nor does espresso wait for it
         *  to finish before continuing. So what happens is that drawer covers the back button and
         *  it is pressed correctly even though we don't get an error saying View isn't visible,
         *  but instead the app just does not navigate back...
         *  Will look into implementing IdlingResource for it...
         */
        Thread.sleep(500)

        // check that AccountFragment is displayed
        onView(withId(R.id.account_layout)).check(matches(isDisplayed()))

        // navigate back
        onView(withContentDescription(R.string.navigate_back)).perform(click())

        // check that CFLFragment is displayed
        onView(withId(R.id.cfl_layout)).check(matches(isDisplayed()))
    }

    @Test
    @RepeatTest(100)
    fun navigateToCategoriesAndBack() {

        // check Drawer starts closed then open it
        onView(withId(R.id.activity_drawer))
            .check(matches(isClosed(Gravity.LEFT)))
            .perform(open())

        // navigate to CategoryFragment
        onView(withId(R.id.activity_nav_view))
            .perform(navigateTo(R.id.categoryFragment))

        /**
         *  Hate to do this, but can't stop drawer closing animation nor does espresso wait for it
         *  to finish before continuing. So what happens is that drawer covers the back button and
         *  it is pressed correctly even though we don't get an error saying View isn't visible,
         *  but instead the app just does not navigate back...
         *  Will look into implementing IdlingResource for it...
         */
        Thread.sleep(500)

        // check that CategoryFragment is displayed
        onView(withId(R.id.category_layout)).check(matches(isDisplayed()))

        // navigate back
        onView(withContentDescription(R.string.navigate_back)).perform(click())

        // check that CFLFragment is displayed
        onView(withId(R.id.cfl_layout)).check(matches(isDisplayed()))
    }

    @Test
    @RepeatTest(100)
    fun navigateToAboutAndBack() {

        // check Drawer starts closed then open it
        onView(withId(R.id.activity_drawer))
            .check(matches(isClosed(Gravity.LEFT)))
            .perform(open())

        // navigate to AboutFragment
        onView(withId(R.id.activity_nav_view))
            .perform(navigateTo(R.id.aboutFragment))

        /**
         *  Hate to do this, but can't stop drawer closing animation nor does espresso wait for it
         *  to finish before continuing. So what happens is that drawer covers the back button and
         *  it is pressed correctly even though we don't get an error saying View isn't visible,
         *  but instead the app just does not navigate back...
         *  Will look into implementing IdlingResource for it...
         */
        Thread.sleep(500)

        // check that AboutFragment is displayed
        onView(withId(R.id.about_layout)).check(matches(isDisplayed()))

        // navigate back
        onView(withContentDescription(R.string.navigate_back)).perform(click())

        // check that CFLFragment is displayed
        onView(withId(R.id.cfl_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToSettingsAndBack() {

        // check Drawer starts closed then open it
        onView(withId(R.id.activity_drawer))
            .check(matches(isClosed(Gravity.LEFT)))
            .perform(open())

        // navigate to SettingsActivity
        onView(withId(R.id.activity_nav_view))
            .perform(navigateTo(R.id.actionSettings))

        // check that SettingsActivity is displayed
        onView(withId(R.id.settings_layout)).check(matches(isDisplayed()))

        // navigate back
        pressBack()

        // check that CFLFragment is displayed
        onView(withId(R.id.cfl_layout)).check(matches(isDisplayed()))
    }
}