package com.heyzeusv.plutuswallet.ui.category

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.heyzeusv.plutuswallet.CustomActions.Companion.rvViewClick
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.rvSize
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.rvViewHolder
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.launchFragmentInHiltContainer
import com.heyzeusv.plutuswallet.util.ViewPager2IdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
class CategoryFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repo: Repository

    val dd = DummyDataUtil()

    // used to get string resource
    private val resource: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    private lateinit var viewPager2IdlingResource: ViewPager2IdlingResource

    @Before
    fun init() {

        // populate @Inject fields in test class
        hiltRule.inject()

        // display Category lists and register IdlingResource
        launchFragmentInHiltContainer<CategoryFragment>(Bundle(), R.style.AppTheme) {
            registerVP2IdlingResource(this)
        }
    }

    /**
     *  Unregister Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {

        IdlingRegistry.getInstance().unregister(viewPager2IdlingResource)
    }

    @Test
    fun displayCategories() {

        // check that expense list of Categories loaded in
        onView(withId(R.id.ivcl_rv)).check(matches(rvSize(2)))
        // check that expense Categories are sorted
        onView(withId(R.id.ivcl_rv))
            .check(matches(rvViewHolder(0, withText(dd.cat2.category), R.id.ivcat_name)))
        onView(withId(R.id.ivcl_rv))
            .check(matches(rvViewHolder(1, withText(dd.cat1.category), R.id.ivcat_name)))

        // swipe to income Categories
        onView(withId(R.id.category_vp)).perform(swipeLeft())

        // check that income list of Categories loaded in
        onView(withId(R.id.ivcl_rv)).check(matches(rvSize(2)))
    }

    @Test
    fun createNewCategories() {

        // names for newly created Categories
        val testExCat = "New Test Expense Category"
        val testInCat = "New Test Income Category"

        // create new expense Category and save it
        onView(withId(R.id.category_new)).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(testExCat))
        onView(withId((android.R.id.button1))).perform(click())

        // check that new Category exists and in correct location
        onView(withId(R.id.ivcl_rv)).check(matches(rvSize(3)))
        onView(withId(R.id.ivcl_rv))
            .check(matches(rvViewHolder(2, withText(testExCat), R.id.ivcat_name)))

        // swipe to income Categories
        onView(withId(R.id.category_vp)).perform(swipeLeft())

        // create new income Category and save it
        onView(withId(R.id.category_new)).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(testInCat))
        onView(withId((android.R.id.button1))).perform(click())

        // check that new Category exists and in correct location
        onView(withId(R.id.ivcl_rv)).check(matches(rvSize(3)))
        onView(withId(R.id.ivcl_rv))
            .check(matches(rvViewHolder(0, withText(testInCat), R.id.ivcat_name)))
    }

    @Test
    fun createNewCategoriesExist() {

        // create new expense Category and save it
        onView(withId(R.id.category_new)).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(dd.cat1.category))
        onView(withId((android.R.id.button1))).perform(click())

        // check Snackbar appears warning user that Category exists and list remains same size
        onView(withId(R.id.ivcl_rv)).check(matches(rvSize(2)))
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(resource.getString(R.string.snackbar_exists, dd.cat1.category))))

        // swipe to income Categories
        onView(withId(R.id.category_vp)).perform(swipeLeft())

        // create new income Category and save it
        onView(withId(R.id.category_new)).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(dd.cat3.category))
        onView(withId((android.R.id.button1))).perform(click())

        // check Snackbar appears warning user that Category exists and list remains same size
        onView(withId(R.id.ivcl_rv)).check(matches(rvSize(2)))
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(resource.getString(R.string.snackbar_exists, dd.cat3.category))))
    }

    @Test
    fun deleteCategories() {

        // cannot delete Categories in use so need theses dummy Categories
        val deleteExCat = Category(0, "Delete Me", "Expense")
        val deleteInCat = Category(0, "Delete Me", "Income")

        // insert dummy Categories into Database
        runBlocking {
            repo.insertCategories(listOf(deleteExCat, deleteInCat))
        }

        // click on delete Button in ViewHolder at position 0 in RecyclerView and confirm
        onView(withId(R.id.ivcl_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(0, rvViewClick(R.id.ivcat_delete)))
        onView(withId(android.R.id.button1)).perform(click())

        // check expense Category was deleted
        onView(withId(R.id.ivcl_rv)).check(matches(rvSize(2)))
        onView(withText("Delete Me")).check(doesNotExist())

        // swipe to income Categories
        onView(withId(R.id.category_vp)).perform(swipeLeft())

        // click on delete Button in ViewHolder at position 0 in RecyclerView and confirm
        onView(withId(R.id.ivcl_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(0, rvViewClick(R.id.ivcat_delete)))
        onView(withId(android.R.id.button1)).perform(click())

        // check income Category was deleted
        onView(withId(R.id.ivcl_rv)).check(matches(rvSize(2)))
        onView(withText("Delete Me")).check(doesNotExist())
    }

    @Test
    fun editCategories() {

        // edited Category names
        val editedExName = "Test Expense Category"
        val editedInName = "Test Income Category"

        // click on edit Button in ViewHolder at position 1 in RecyclerView, enter new name, and confirm
        onView(withId(R.id.ivcl_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(1, rvViewClick(R.id.ivcat_edit)))
        onView(withId(R.id.dialog_input)).perform(typeText(editedExName))
        onView(withId(android.R.id.button1)).perform(click())

        // check Account name has been edited and in correct location
        onView(withId(R.id.ivcl_rv))
            .check(matches(rvViewHolder(1, withText(editedExName), R.id.ivcat_name)))

        // swipe to income Categories
        onView(withId(R.id.category_vp)).perform(swipeLeft())

        // click on edit Button in ViewHolder at position 1 in RecyclerView, enter new name, and confirm
        onView(withId(R.id.ivcl_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(1, rvViewClick(R.id.ivcat_edit)))
        onView(withId(R.id.dialog_input)).perform(typeText(editedInName))
        onView(withId(android.R.id.button1)).perform(click())

        // check Account name has been edited and in correct location
        onView(withId(R.id.ivcl_rv))
            .check(matches(rvViewHolder(1, withText(editedInName), R.id.ivcat_name)))
    }

    @Test
    fun editCategoriesExists() {

        // click on edit Button in ViewHolder at position 1 in RecyclerView, enter new name, and confirm
        onView(withId(R.id.ivcl_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(1, rvViewClick(R.id.ivcat_edit)))
        onView(withId(R.id.dialog_input)).perform(typeText(dd.cat2.category))
        onView(withId(android.R.id.button1)).perform(click())

        // check Snackbar appears warning user that Category exists
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(resource.getString(R.string.snackbar_exists, dd.cat2.category))))

        // swipe to income Categories
        onView(withId(R.id.category_vp)).perform(swipeLeft())

        // click on edit Button in ViewHolder at position 0 in RecyclerView, enter new name, and confirm
        onView(withId(R.id.ivcl_rv)).perform(RecyclerViewActions
            .actionOnItemAtPosition<RecyclerView.ViewHolder>(0, rvViewClick(R.id.ivcat_edit)))
        onView(withId(R.id.dialog_input)).perform(typeText(dd.cat4.category))
        onView(withId(android.R.id.button1)).perform(click())

        // check Snackbar appears warning user that Category exists
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(resource.getString(R.string.snackbar_exists, dd.cat4.category))))
    }

    /**
     *  Registers IdlingResource for Viewpager2 by using [frag] View to obtain ViewPager2.
     */
    private fun registerVP2IdlingResource(frag: Fragment) {

        val vp2: ViewPager2 = frag.view?.findViewById(R.id.category_vp)!!
        viewPager2IdlingResource = ViewPager2IdlingResource(vp2, "vpIdlingResource")
        IdlingRegistry.getInstance().register(viewPager2IdlingResource)
    }
}