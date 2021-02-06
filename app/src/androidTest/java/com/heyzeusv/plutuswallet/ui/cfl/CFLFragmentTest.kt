package com.heyzeusv.plutuswallet.ui.cfl

import android.content.res.Resources
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.chartEntry
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.chartText
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.rvSize
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.rvViewHolder
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withIndex
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withTextColor
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.launchFragmentInHiltContainer
import com.heyzeusv.plutuswallet.util.ViewPager2IdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
class CFLFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repo: Repository

    val dd = DummyDataUtil()

    private var totalFormatter: DecimalFormat = initTotalFormatter()

    // used to get string resource
    private val resource: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    private lateinit var viewPager2IdlingResource: ViewPager2IdlingResource

    @Before
    fun init() {

        // populate @Inject fields in test class
        hiltRule.inject()

        // display Category lists and register IdlingResource
        launchFragmentInHiltContainer<CFLFragment>(Bundle(), R.style.AppTheme) {
            registerVP2IdlingResource(this)
        }
    }

    /**
     *   Unregister Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {

        IdlingRegistry.getInstance().unregister(viewPager2IdlingResource)
    }

    @Test
    fun displayFragments() {

        // Chart totals
        val expenseTotal = resource.getString(R.string.chart_total, "$", "1,155.55")
        val incomeTotal = resource.getString(R.string.chart_total, "$", "2,000.32")

        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(chartText("Expense")))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(withText(expenseTotal)))
        onView(withIndex(withId(R.id.ivchart_pie), 0))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        onView(withIndex(withId(R.id.ivchart_pie), 0))
            .check(matches(chartEntry("Food", 1100.10F)))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(not(isDisplayed())))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(chartText("Income")))
        onView(withId(R.id.ivchart_total)).check(matches(withText(incomeTotal)))
        onView(withId(R.id.ivchart_pie))
            .check(matches(chartEntry("Salary", 2000.32F)))
        onView(withId(R.id.emptyTextView)).check(matches(not(isDisplayed())))

        // check that nothing in filter is selected
        onView(withId(R.id.filter_account_check)).check(matches(isNotChecked()))
        onView(withId(R.id.filter_account)).check(matches(not(isEnabled())))
        onView(withId(R.id.filter_account)).check(matches(withSpinnerText(dd.acc3.account)))
        onView(withId(R.id.filter_cat_check)).check(matches(isNotChecked()))
        onView(withId(R.id.filter_type)).check(matches(not(isEnabled())))
        onView(withId(R.id.filter_type)).check(matches(withText(R.string.filter_type)))
        onView(withId(R.id.filter_expense_cat)).check(matches(not(isEnabled())))
        onView(withId(R.id.filter_expense_cat)).check(matches(withSpinnerText(R.string.category_all)))
        onView(withId(R.id.filter_income_cat)).check(matches(not(isDisplayed())))
        onView(withId(R.id.filter_scroll)).perform(swipeUp())
        onView(withId(R.id.filter_date_check)).check(matches(isNotChecked()))
        onView(withId(R.id.filter_start_date)).check(matches(not(isEnabled())))
        onView(withId(R.id.filter_start_date)).check(matches(withText(R.string.filter_start)))
        onView(withId(R.id.filter_end_date)).check(matches(not(isEnabled())))
        onView(withId(R.id.filter_end_date)).check(matches(withText(R.string.filter_end)))
        onView(withId(R.id.filter_action)).check(matches(withText(R.string.filter_reset)))

        // check that all Transactions loaded
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(4)))
        onView(withId(R.id.emptyListTextView)).check(matches(not(isDisplayed())))
        // check that all data for Transactions is loaded correctly and in correct position
        checkTranViewHolder(0, dd.tran1, android.R.color.holo_red_dark)
        checkTranViewHolder(1, dd.tran2, android.R.color.holo_red_dark)
        checkTranViewHolder(2, dd.tran3, android.R.color.holo_green_dark)
        checkTranViewHolder(3, dd.tran4, android.R.color.holo_red_dark)
    }

    @Test
    fun applyAccountFilter() {

        // Chart total
        val expenseTotal = resource.getString(R.string.chart_total, "$", "1,100.10")

        // apply Account filter
        onView(withId(R.id.filter_account_check)).perform(click())
        onView(withId(R.id.filter_action)).perform(click())

        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(chartText("Expense")))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(withText(expenseTotal)))
        onView(withIndex(withId(R.id.ivchart_pie), 0))
            .check(matches(chartEntry("Food", 1100.10F)))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(not(isDisplayed())))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivchart_total)).check(matches(not(isDisplayed())))
        onView(withId(R.id.emptyTextView)).check(matches(isDisplayed()))

        // check that all Transactions loaded
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(2)))
        onView(withId(R.id.emptyListTextView)).check(matches(not(isDisplayed())))
        // check that all data for Transactions is loaded correctly and in correct position
        checkTranViewHolder(0, dd.tran1, android.R.color.holo_red_dark)
        checkTranViewHolder(1, dd.tran2, android.R.color.holo_red_dark)
    }

    @Test
    fun applyExpenseCategoryFilter() {

        // Chart total
        val expenseTotal = resource.getString(R.string.chart_total, "$", "55.45")

        // apply expense Category filter
        onView(withId(R.id.filter_cat_check)).perform(click())
        onView(withId(R.id.filter_expense_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(dd.cat2.category))).perform(click())
        onView(withId(R.id.filter_action)).perform(click())

        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(chartText("Expense")))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(withText(expenseTotal)))
        onView(withIndex(withId(R.id.ivchart_pie), 0))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(not(isDisplayed())))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivchart_total)).check(matches(not(isDisplayed())))
        onView(withId(R.id.emptyTextView)).check(matches(isDisplayed()))

        // check that all Transactions loaded
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(1)))
        onView(withId(R.id.emptyListTextView)).check(matches(not(isDisplayed())))
        // check that all data for Transactions is loaded correctly and in correct position
        checkTranViewHolder(0, dd.tran4, android.R.color.holo_red_dark)
    }

    @Test
    fun applyIncomeCategoryFilter() {

        // Chart total
        val incomeTotal = resource.getString(R.string.chart_total, "$", "0.00")

        // apply income Category filter
        onView(withId(R.id.filter_cat_check)).perform(click())
        onView(withId(R.id.filter_type)).perform(click())
        onView(withId(R.id.filter_expense_cat)).check(matches(not(isDisplayed())))
        onView(withId(R.id.filter_income_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(dd.cat4.category))).perform(click())
        onView(withId(R.id.filter_action)).perform(click())

        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(not(isDisplayed())))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(not(isDisplayed())))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(isDisplayed()))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(chartText("Income")))
        onView(withId(R.id.ivchart_total)).check(matches(withText(incomeTotal)))
        onView(withId(R.id.emptyTextView)).check(matches(not(isDisplayed())))

        // check that no Transactions loaded
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(0)))
        onView(withId(R.id.emptyListTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun applyDateFilter() {

        // apply Date filter
        onView(withId(R.id.filter_scroll)).perform(swipeUp())
        onView(withId(R.id.filter_date_check)).perform(click())
        onView(withId(R.id.filter_start_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 20))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_start_date)).check(matches(withText("1/20/21")))
        onView(withId(R.id.filter_end_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 21))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_end_date)).check(matches(withText("1/21/21")))
        onView(withId(R.id.filter_action)).perform(click())

        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(not(isDisplayed())))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(not(isDisplayed())))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(isDisplayed()))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivchart_total)).check(matches(not(isDisplayed())))
        onView(withId(R.id.emptyTextView)).check(matches(isDisplayed()))

        // check that no Transactions loaded
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(0)))
        onView(withId(R.id.emptyListTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun applyAccountCategoryFilters() {

        // Chart total
        val incomeTotal = resource.getString(R.string.chart_total, "$", "4,000.64")

        // apply Account and Category filter
        onView(withId(R.id.filter_account_check)).perform(click())
        onView(withId(R.id.filter_account)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(dd.acc2.account))).perform(click())
        onView(withId(R.id.filter_cat_check)).perform(click())
        onView(withId(R.id.filter_type)).perform(click())
        onView(withId(R.id.filter_action)).perform(click())

        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(not(isDisplayed())))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(not(isDisplayed())))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(isDisplayed()))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(chartText("Income")))
        onView(withId(R.id.ivchart_pie))
            .check(matches(chartEntry("Salary", 4000.64F)))
        onView(withId(R.id.ivchart_total)).check(matches(withText(incomeTotal)))
        onView(withId(R.id.emptyTextView)).check(matches(not(isDisplayed())))

        // check that all Transactions loaded
        // size increases because refresh triggers creation of repeating Transaction
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(2)))
        onView(withId(R.id.emptyListTextView)).check(matches(not(isDisplayed())))
        // check that all data for Transactions is loaded correctly and in correct position
        checkTranViewHolder(0, dd.tran3, android.R.color.holo_green_dark)
    }

    @Test
    fun applyAccountDateFilters() {

        // Chart total
        val expenseTotal = resource.getString(R.string.chart_total, "$", "55.45")

        // apply Account and Date filter
        onView(withId(R.id.filter_account_check)).perform(click())
        onView(withId(R.id.filter_account)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(dd.acc1.account))).perform(click())
        onView(withId(R.id.filter_scroll)).perform(swipeUp())
        onView(withId(R.id.filter_date_check)).perform(click())
        onView(withId(R.id.filter_start_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 4))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_start_date)).check(matches(withText("1/4/70")))
        onView(withId(R.id.filter_end_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 6))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_end_date)).check(matches(withText("1/6/70")))
        onView(withId(R.id.filter_action)).perform(click())


        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(chartText("Expense")))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(withText(expenseTotal)))
        onView(withIndex(withId(R.id.ivchart_pie), 0))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(not(isDisplayed())))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivchart_total)).check(matches(not(isDisplayed())))
        onView(withId(R.id.emptyTextView)).check(matches(isDisplayed()))

        // check that all Transactions loaded
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(1)))
        onView(withId(R.id.emptyListTextView)).check(matches(not(isDisplayed())))
        // check that all data for Transactions is loaded correctly and in correct position
        checkTranViewHolder(0, dd.tran4, android.R.color.holo_red_dark)
    }

    @Test
    fun applyCategoryDateFilters() {

        // Chart total
        val expenseTotal = resource.getString(R.string.chart_total, "$", "100.00")

        // apply Category and Date filter
        onView(withId(R.id.filter_cat_check)).perform(click())
        onView(withId(R.id.filter_expense_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(dd.cat1.category))).perform(click())
        onView(withId(R.id.filter_scroll)).perform(swipeUp())
        onView(withId(R.id.filter_date_check)).perform(click())
        onView(withId(R.id.filter_start_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 2))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_start_date)).check(matches(withText("1/2/70")))
        onView(withId(R.id.filter_end_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 3))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_end_date)).check(matches(withText("1/3/70")))
        onView(withId(R.id.filter_action)).perform(click())

        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(chartText("Expense")))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(withText(expenseTotal)))
        onView(withIndex(withId(R.id.ivchart_pie), 0))
            .check(matches(chartEntry("Food", 100.00F)))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(not(isDisplayed())))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivchart_total)).check(matches(not(isDisplayed())))
        onView(withId(R.id.emptyTextView)).check(matches(isDisplayed()))

        // check that all Transactions loaded
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(1)))
        onView(withId(R.id.emptyListTextView)).check(matches(not(isDisplayed())))
        // check that all data for Transactions is loaded correctly and in correct position
        checkTranViewHolder(0, dd.tran2, android.R.color.holo_red_dark)
    }

    @Test
    fun applyAllFilters() {

        // Chart total
        val expenseTotal = resource.getString(R.string.chart_total, "$", "1,000.10")

        // apply all filters
        onView(withId(R.id.filter_account_check)).perform(click())
        onView(withId(R.id.filter_account)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(dd.acc3.account))).perform(click())
        onView(withId(R.id.filter_cat_check)).perform(click())
        onView(withId(R.id.filter_expense_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(dd.cat1.category))).perform(click())
        onView(withId(R.id.filter_scroll)).perform(swipeUp())
        onView(withId(R.id.filter_date_check)).perform(click())
        onView(withId(R.id.filter_start_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 1))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_start_date)).check(matches(withText("1/1/70")))
        onView(withId(R.id.filter_end_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 1))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_end_date)).check(matches(withText("1/1/70")))
        onView(withId(R.id.filter_action)).perform(click())

        // check expense Chart values
        onView(withIndex(withId(R.id.ivchart_pie), 0)).check(matches(chartText("Expense")))
        onView(withIndex(withId(R.id.ivchart_total), 0)).check(matches(withText(expenseTotal)))
        onView(withIndex(withId(R.id.ivchart_pie), 0))
            .check(matches(chartEntry("Food", 1000.10F)))
        onView(withIndex(withId(R.id.emptyTextView), 0)).check(matches(not(isDisplayed())))

        // swipe to income Chart
        onView(withId(R.id.chart_vp)).perform(swipeLeft())

        // check income Chart values
        onView(withId(R.id.ivchart_pie)).check(matches(not(isDisplayed())))
        onView(withId(R.id.ivchart_total)).check(matches(not(isDisplayed())))
        onView(withId(R.id.emptyTextView)).check(matches(isDisplayed()))

        // check that all Transactions loaded
        // size increases because refresh triggers creation of repeating Transaction
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(1)))
        onView(withId(R.id.emptyListTextView)).check(matches(not(isDisplayed())))
        // check that all data for Transactions is loaded correctly and in correct position
        checkTranViewHolder(0, dd.tran1, android.R.color.holo_red_dark)
    }

    @Test
    fun deleteTransaction() {

        // delete Transaction in 4th position
        onView(withIndex(withId(R.id.ivt_layout), 3)).perform(longClick())
        onView(withId(android.R.id.button1)).perform(click())

        // check that 1 Transaction was deleted
        onView(withId(R.id.tranlist_rv)).check(matches(rvSize(3)))
        onView(withText(dd.tran4.title)).check(doesNotExist())
    }

    /**
     *  Registers IdlingResource for Viewpager2 by using [frag] View to obtain ViewPager2.
     */
    private fun registerVP2IdlingResource(frag: Fragment) {

        val vp2: ViewPager2 = frag.view?.findViewById(R.id.chart_vp)!!
        viewPager2IdlingResource = ViewPager2IdlingResource(vp2, "vpIdlingResource")
        IdlingRegistry.getInstance().register(viewPager2IdlingResource)
    }

    /**
     *  Initializes formatter to be used to correctly display Total.
     */
    private fun initTotalFormatter(): DecimalFormat {

        val decimal = '.'
        val thousands = ','
        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.decimalSeparator = decimal
        customSymbols.groupingSeparator = thousands
        val formatter = DecimalFormat("#,##0.00", customSymbols)
        formatter.roundingMode = RoundingMode.HALF_UP

        return formatter
    }

    /**
     *  Checks that ViewHolder at [pos] in RecyclerView is displaying the correct [tran] details,
     *  as well as the [color] of Total text.
     */
    private fun checkTranViewHolder(pos: Int, tran: Transaction, color: Int) {

        // Date to be shown in ItemView
        val formattedDate: String = DateFormat.getDateInstance(0).format(tran.date)
        val formattedTotal = "\$${totalFormatter.format(tran.total)}"

        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(tran.title), R.id.ivt_title)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(tran.account), R.id.ivt_account)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(formattedDate), R.id.ivt_date)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(formattedTotal), R.id.ivt_total)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withTextColor(color), R.id.ivt_total)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(tran.category), R.id.ivt_category)))
    }
}