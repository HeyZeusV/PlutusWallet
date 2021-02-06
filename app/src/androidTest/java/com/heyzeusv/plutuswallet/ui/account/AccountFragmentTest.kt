package com.heyzeusv.plutuswallet.ui.account

import android.content.res.Resources
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.heyzeusv.plutuswallet.CustomActions.Companion.rvViewClick
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.rvSize
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.rvViewHolder
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
class AccountFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repo: Repository

    val dd = DummyDataUtil()

    // used to get string resource
    private val resource: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Before
    fun init() {

        // populate @Inject fields in test class
        hiltRule.inject()

        // display Account list
        launchFragmentInHiltContainer<AccountFragment>(Bundle(), R.style.AppTheme)
    }

    @Test
    fun displayAccounts() {

        // check that all Accounts were loaded in
        onView(withId(R.id.account_rv)).check(matches(rvSize(4)))
        // check that Accounts are sorted
        onView(withId(R.id.account_rv))
            .check(matches(rvViewHolder(0, withText(dd.acc3.account), R.id.iva_name)))
        onView(withId(R.id.account_rv))
            .check(matches(rvViewHolder(1, withText(dd.acc1.account), R.id.iva_name)))
        onView(withId(R.id.account_rv))
            .check(matches(rvViewHolder(2, withText(dd.acc2.account), R.id.iva_name)))
        onView(withId(R.id.account_rv))
            .check(matches(rvViewHolder(3, withText(dd.acc4.account), R.id.iva_name)))
    }

    @Test
    fun createNewAccount() {

        // name for newly created Account
        val testAcc = "New Test Account"

        // create new Account and save it
        onView(withId(R.id.account_new)).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(testAcc))
        onView(withId(android.R.id.button1)).perform(click())

        // check that new Account exists and in correct location
        onView(withId(R.id.account_rv)).check(matches(rvSize(5)))
        onView(withId(R.id.account_rv))
            .check(matches(rvViewHolder(3, withText(testAcc), R.id.iva_name)))
    }

    @Test
    fun createNewAccountExists() {

        // create new Account and save it
        onView(withId(R.id.account_new)).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(dd.acc1.account))
        onView(withId(android.R.id.button1)).perform(click())

        // check Snackbar appears warning user that Account exists
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(resource.getString(R.string.snackbar_exists, dd.acc1.account))))
    }

    @Test
    fun deleteAccount() {

        // click on delete Button in ViewHolder at position 3 in RecyclerView and confirm
        onView(withId(R.id.account_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(3, rvViewClick(R.id.iva_delete)))
        onView(withId(android.R.id.button1)).perform(click())

        // check Account was deleted
        onView(withId(R.id.account_rv)).check(matches(rvSize(3)))
        onView(withText("Unused")).check(doesNotExist())
    }

    @Test
    fun editAccount() {

        //  edited Account name
        val editedName = "Test Account"

        // click on edit Button in ViewHolder at position 1 in RecyclerView, enter new name, and confirm
        onView(withId(R.id.account_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(2, rvViewClick(R.id.iva_edit)))
        onView(withId(R.id.dialog_input)).perform(typeText(editedName))
        onView(withId(android.R.id.button1)).perform(click())

        // check Account name has been edited and in correct location
        onView(withId(R.id.account_rv))
            .check(matches(rvViewHolder(2, withText(editedName), R.id.iva_name)))
    }

    @Test
    fun editAccountExists() {

        // click on edit Button in ViewHolder at position 1 in RecyclerView, enter new name, and confirm
        onView(withId(R.id.account_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(2, rvViewClick(R.id.iva_edit)))
        onView(withId(R.id.dialog_input)).perform(typeText(dd.acc3.account))
        onView(withId(android.R.id.button1)).perform(click())

        // check Snackbar appears warning user that Account exists
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(resource.getString(R.string.snackbar_exists, dd.acc3.account))))
    }
}