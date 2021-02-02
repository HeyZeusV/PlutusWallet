package com.heyzeusv.plutuswallet.ui.about

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.heyzeusv.plutuswallet.R
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
class AboutFragmentTest {

    @Before
    fun init() {

        // display AboutFragment
        launchFragmentInContainer<AboutFragment>(Bundle(), R.style.AppTheme)
    }

    @Test
    fun buttonsAndLinksEnabledContentHidden() {

        // checks that buttons are enables and content is hidden
        // checks to make sure links are clickable
        onView(withId(R.id.about_changelog_mb)).check(matches(isEnabled()))
        onView(withId(R.id.about_changelog_sv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_changelog_tv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_email_tv)).check(matches(isClickable()))
        onView(withId(R.id.about_scrollView)).perform(swipeUp())
        onView(withId(R.id.about_ci_git_tv)).check(matches(isClickable()))
        onView(withId(R.id.about_ci_mb)).check(matches(isEnabled()))
        onView(withId(R.id.about_ci_sv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_ci_license_tv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_mpc_git_tv)).check(matches(isClickable()))
        onView(withId(R.id.about_mpc_mb)).check(matches(isEnabled()))
        onView(withId(R.id.about_mpc_sv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_mpc_license_tv)).check(matches(not(isDisplayed())))
    }

    @Test
    fun displayChangelogAndLicensesThenHideThem() {

        // clicks on buttons and ensures that ScrollView containing TextView appears
        onView(withId(R.id.about_changelog_mb)).perform(click())
        onView(withId(R.id.about_changelog_sv)).check(matches(isDisplayed())).perform(swipeUp())
        onView(withId(R.id.about_changelog_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.about_ci_mb)).perform(scrollTo(), click())
        onView(withId(R.id.about_ci_sv))
            .perform(scrollTo()).check(matches(isDisplayed())).perform(swipeUp())
        onView(withId(R.id.about_ci_license_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.about_mpc_mb)).perform(scrollTo(), click())
        onView(withId(R.id.about_mpc_sv))
            .perform(scrollTo()).check(matches(isDisplayed())).perform(swipeUp())
        onView(withId(R.id.about_mpc_license_tv)).check(matches(isDisplayed()))

        // click on buttons again and ensure that ScrollViews and TextViews disappear
        onView(withId(R.id.about_mpc_mb)).perform(click())
        onView(withId(R.id.about_mpc_sv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_mpc_license_tv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_ci_mb)).perform(scrollTo(), click())
        onView(withId(R.id.about_ci_sv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_ci_license_tv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_changelog_mb)).perform(scrollTo(), click())
        onView(withId(R.id.about_changelog_sv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.about_changelog_tv)).check(matches(not(isDisplayed())))
    }
}