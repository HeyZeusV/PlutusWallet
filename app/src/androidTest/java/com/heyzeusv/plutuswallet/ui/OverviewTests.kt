package com.heyzeusv.plutuswallet.ui

import android.content.res.Resources
import android.widget.DatePicker
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.heyzeusv.plutuswallet.chartEntry
import com.heyzeusv.plutuswallet.chartText
import com.heyzeusv.plutuswallet.checkTlifIsDisplayed
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.data.FakeAndroidRepository
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.onNodeWithTTStrId
import com.heyzeusv.plutuswallet.onNodeWithTextIdUp
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

@HiltAndroidTest
class OverviewTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var fakeRepo: PWRepositoryInterface
    lateinit var repo: FakeAndroidRepository
    val dd = DummyAndroidDataUtil()
    lateinit var res: Resources

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.activity.setContent {
            PlutusWalletTheme {
                PlutusWalletApp()
            }
        }
        repo = (fakeRepo as FakeAndroidRepository)
        res = InstrumentationRegistry.getInstrumentation().targetContext.resources
    }

    @BeforeEach
    fun setUpBeforeEach() {
        repo.resetLists()
    }

    @Test
    fun overview_startUp() {
        val expense = res.getString(R.string.type_expense)
        val income = res.getString(R.string.type_income)

        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        dd.tlifList.forEach { item -> composeRule.checkTlifIsDisplayed(item) }

        // should start with Expense chart
        onView(withContentDescription("Chart 0")).check(matches(chartText(expense)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0)
            .assertTextContains("Total: $1,155.55")
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1).assertDoesNotExist()
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Housing", 100.00F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertDoesNotExist()

        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }

        // after scrolling to Income chart
        onView(withContentDescription("Chart 1")).check(matches(chartText(income)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0).assertIsNotDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1)
            .assertTextEquals("Total: $2,000.32")
        onView(withContentDescription("Chart 1"))
            .check(matches(chartEntry("Salary", 2000.32F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertDoesNotExist()

        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertDoesNotExist()
        // should start with no filters selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_account).assertIsNotSelected()
        composeRule.onNodeWithTextIdUp(R.string.filter_category).assertIsNotSelected()
        composeRule.onNodeWithTextIdUp(R.string.filter_date).assertIsNotSelected()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act)
            .assertTextEquals(res.getString(R.string.filter_reset).uppercase())
    }

    @Test
    fun overview_applyAccountFilter() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Account filter with 2 chips selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_account).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Cash").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Credit Card").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act).performClick()

        // check expense chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0)
            .assertTextEquals("Total: $1,055.55")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertDoesNotExist()

        // check income chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        composeRule.checkTlifIsDisplayed(dd.tlif1)
        composeRule.checkTlifIsDisplayed(dd.tlif4)
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_empty).assertDoesNotExist()
    }

    @Test
    fun overview_applyExpenseCategoryFilter() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Category filter with 2 chips selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_category).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_type, "Expense").assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Entertainment").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Housing").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act).performClick()

        // check expense chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0)
            .assertTextEquals("Total: $155.45")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Housing", 100.00F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertDoesNotExist()

        // check income chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        composeRule.checkTlifIsDisplayed(dd.tlif2)
        composeRule.checkTlifIsDisplayed(dd.tlif4)
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_empty).assertDoesNotExist()
    }

    @Test
    fun overview_applyIncomeCategoryFilter() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Category filter with 2 chips selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_category).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_type, "Expense").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_type, "Income").assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Salary").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act).performClick()

        // check expense chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0).assertDoesNotExist()
        onView(withContentDescription("Chart 0")).check(doesNotExist())

        // check income chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1)
            .assertTextEquals("Total: $2,000.32")
        onView(withContentDescription("Chart 1"))
            .check(matches(chartEntry("Salary", 2000.32F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertDoesNotExist()

        // check TranList
        composeRule.checkTlifIsDisplayed(dd.tlif3)
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_empty).assertDoesNotExist()
    }

    @Test
    fun overview_applyDateFilter() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply date filter
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_date).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 20))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).assertTextEquals("1/20/21")
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 21))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).assertTextEquals("1/21/21")
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act).performClick()

        // check expense chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0).assertDoesNotExist()
        onView(withContentDescription("Chart 0")).check(doesNotExist())

        // check income chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_empty).assertIsDisplayed()
    }

    @Test
    fun overview_applyAccountCategoryFilters() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Account filter with 1 chip selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_account).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Debit Card").performClick()

        // apply Category filter with 2 chip selected
        composeRule.onNodeWithTextIdUp(R.string.filter_category).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_type, "Expense").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_type, "Income").assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Salary").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Unused Income").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act).performClick()

        // check expense chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0).assertDoesNotExist()
        onView(withContentDescription("Chart 0")).check(doesNotExist())

        // check income chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1)
            .assertTextEquals("Total: $2,000.32")
        onView(withContentDescription("Chart 1"))
            .check(matches(chartEntry("Salary", 2000.32F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertDoesNotExist()

        // check TranList
        composeRule.checkTlifIsDisplayed(dd.tlif3)
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_empty).assertDoesNotExist()
    }

    @Test
    fun overview_applyAccountDateFilters() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Account filter with 2 chips selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_account).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Cash").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Credit Card").performClick()

        // apply Date filter
        composeRule.onNodeWithTextIdUp(R.string.filter_date).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1980, 1, 13))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).assertTextEquals("1/13/80")
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1980, 1, 15))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).assertTextEquals("1/15/80")
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act).performClick()

        // check expense chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0)
            .assertTextEquals("Total: $55.45")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertDoesNotExist()

        // check income chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        composeRule.checkTlifIsDisplayed(dd.tlif4)
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_empty).assertDoesNotExist()
    }

    @Test
    fun overview_applyCategoryDateFilters() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Category filter with 1 chip selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_category).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_type, "Expense").assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Food").performClick()

        // apply Date filter
        composeRule.onNodeWithTextIdUp(R.string.filter_date).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1980, 1, 10))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).assertTextEquals("1/10/80")
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1980, 1, 11))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).assertTextEquals("1/11/80")
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act).performClick()

        // check expense chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0)
            .assertTextEquals("Total: $1,000.10")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertDoesNotExist()

        // check income chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        composeRule.checkTlifIsDisplayed(dd.tlif1)
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_empty).assertDoesNotExist()
    }

    @Test
    fun overview_applyAllFilters() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Account filter with 1 chip selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_card).assertIsDisplayed()
        composeRule.onNodeWithTextIdUp(R.string.filter_account).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Cash").performClick()

        // open filter and apply Category filter with 1 chip selected
        composeRule.onNodeWithTextIdUp(R.string.filter_category).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_type, "Expense").assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_chip, "Food").performClick()

        // apply Date filter
        composeRule.onNodeWithTextIdUp(R.string.filter_date).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1980, 1, 10))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNodeWithTTStrId(R.string.tt_filter_start).assertTextEquals("1/10/80")
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1980, 1, 11))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNodeWithTTStrId(R.string.tt_filter_end).assertTextEquals("1/11/80")
        composeRule.onNodeWithTTStrId(R.string.tt_filter_act).performClick()

        // check expense chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 0)
            .assertTextEquals("Total: $1,000.10")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 0).assertDoesNotExist()

        // check income chart
        composeRule.onNodeWithTTStrId(R.string.tt_chart_vp).performTouchInput { swipeLeft() }
        composeRule.onNodeWithTTStrId(R.string.tt_chart_empty, 1).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_chart_total, 1).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        composeRule.checkTlifIsDisplayed(dd.tlif1)
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_empty).assertDoesNotExist()
    }
}