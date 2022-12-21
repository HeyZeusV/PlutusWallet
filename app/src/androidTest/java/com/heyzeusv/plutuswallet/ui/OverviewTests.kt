package com.heyzeusv.plutuswallet.ui

import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.chartEntry
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.chartText
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.cfl.chart.ChartViewModel
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@HiltAndroidTest
class OverviewTests : BaseTest() {

    @Test
    fun overview_startUp() {
        val expense = composeRule.activity.baseContext.resources.getString(R.string.type_expense)
        val income = composeRule.activity.baseContext.resources.getString(R.string.type_income)

        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        dd.tranList.forEach { item -> tranListItemCheck(item) }

        // should start with Expense chart
        onView(withContentDescription("Chart 0")).check(matches(chartText(expense)))
        composeRule.onNode(hasTestTag("Chart Total for page 0")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 0"))
            .assertTextContains("Total: $1,155.55")
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertIsNotDisplayed()
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Unused Expense", 100.00F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertDoesNotExist()

        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }

        // after scrolling to Income chart
        onView(withContentDescription("Chart 1")).check(matches(chartText(income)))
        composeRule.onNode(hasTestTag("Chart Total for page 0")).assertIsNotDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1"))
            .assertTextEquals("Total: $2,000.32")
        onView(withContentDescription("Chart 1"))
            .check(matches(chartEntry("Salary", 2000.32F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertDoesNotExist()

        composeRule.onNode(hasTestTag("Filter Card")).assertDoesNotExist()
        // should start with no filters selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_account))).assertIsNotSelected()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_category))).assertIsNotSelected()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_date))).assertIsNotSelected()
        composeRule.onNode(hasTestTag("Filter action"))
            .assertTextEquals(res.getString(R.string.filter_reset).uppercase())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun overview_deleteTransaction() = runTest {
        val expense = composeRule.activity.baseContext.resources.getString(R.string.type_expense)
        val income = composeRule.activity.baseContext.resources.getString(R.string.type_income)

        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        composeRule.onNode(hasTestTag("${dd.tran2.id}")).performTouchInput { longClick() }
        // checks that AlertDialog is being displayed and press confirm button
        composeRule.onNode(hasTestTag("AlertDialog")).assertExists()
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // rerun calls to get updated lists
        composeRule.activity.viewModels<TransactionListViewModel>().value.updateTranList(FilterInfo())
        composeRule.activity.viewModels<ChartViewModel>().value.updateCatTotalsList(FilterInfo())

        // check that correct Transaction has been deleted
        composeRule.onNode(hasTestTag("${dd.tran2.id}")).assertDoesNotExist()

        // check expense chart total and slices shown
        onView(withContentDescription("Chart 0")).check(matches(chartText(expense)))
        composeRule.onNode(hasTestTag("Chart Total for page 0")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 0"))
            .assertTextContains("Total: $1,055.55")
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertIsNotDisplayed()
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertDoesNotExist()

        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }

        // income chart should remain the same
        onView(withContentDescription("Chart 1")).check(matches(chartText(income)))
        composeRule.onNode(hasTestTag("Chart Total for page 0")).assertIsNotDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1"))
            .assertTextEquals("Total: $2,000.32")
        onView(withContentDescription("Chart 1"))
            .check(matches(chartEntry("Salary", 2000.32F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertDoesNotExist()
    }

    @Test
    fun overview_applyAccountFilter() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Account filter with 2 chips selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_account))).performClick()
        composeRule.onNode(hasTestTag("Chip: Cash")).performClick()
        composeRule.onNode(hasTestTag("Chip: Credit Card")).performClick()
        composeRule.onNode(hasTestTag("Filter action")).performClick()

        // check expense chart
        composeRule.onNode(hasTestTag("Chart Total for page 0"))
            .assertTextEquals("Total: $1,055.55")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertDoesNotExist()

        // check income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        tranListItemCheck(dd.tran1)
        tranListItemCheck(dd.tran4)
        composeRule.onNode(hasTestTag("Empty Transaction List")).assertDoesNotExist()
    }

    @Test
    fun overview_applyExpenseCategoryFilter() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Category filter with 2 chips selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_category))).performClick()
        composeRule.onNode(hasTestTag("Expense Button")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chip: Entertainment")).performClick()
        composeRule.onNode(hasTestTag("Chip: Unused Expense")).performClick()
        composeRule.onNode(hasTestTag("Filter action")).performClick()

        // check expense chart
        composeRule.onNode(hasTestTag("Chart Total for page 0"))
            .assertTextEquals("Total: $155.45")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Unused Expense", 100.00F)))
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertDoesNotExist()

        // check income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        tranListItemCheck(dd.tran2)
        tranListItemCheck(dd.tran4)
        composeRule.onNode(hasTestTag("Empty Transaction List")).assertDoesNotExist()
    }

    @Test
    fun overview_applyIncomeCategoryFilter() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Category filter with 2 chips selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_category))).performClick()
        composeRule.onNode(hasTestTag("Expense Button")).performClick()
        composeRule.onNode(hasTestTag("Income Button")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chip: Salary")).performClick()
        composeRule.onNode(hasTestTag("Filter action")).performClick()

        // check expense chart
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 0")).assertDoesNotExist()
        onView(withContentDescription("Chart 0")).check(doesNotExist())

        // check income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("Chart Total for page 1"))
            .assertTextEquals("Total: $2,000.32")
        onView(withContentDescription("Chart 1"))
            .check(matches(chartEntry("Salary", 2000.32F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertDoesNotExist()

        // check TranList
        tranListItemCheck(dd.tran3)
        composeRule.onNode(hasTestTag("Empty Transaction List")).assertDoesNotExist()
    }

    @Test
    fun overview_applyDateFilter() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply date filter
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_date))).performClick()
        composeRule.onNode(hasTestTag("Filter Start Date")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Filter End Date")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Filter Start Date")).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 20))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasTestTag("Filter Start Date")).assertTextEquals("1/20/21")
        composeRule.onNode(hasTestTag("Filter End Date")).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 21))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasTestTag("Filter End Date")).assertTextEquals("1/21/21")
        composeRule.onNode(hasTestTag("Filter action")).performClick()

        // check expense chart
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 0")).assertDoesNotExist()
        onView(withContentDescription("Chart 0")).check(doesNotExist())

        // check income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        composeRule.onNode(hasTestTag("Empty Transaction List")).assertIsDisplayed()
    }

    @Test
    fun overview_applyAccountCategoryFilters() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Account filter with 1 chip selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_account))).performClick()
        composeRule.onNode(hasTestTag("Chip: Debit Card")).performClick()

        // apply Category filter with 2 chip selected
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_category))).performClick()
        composeRule.onNode(hasTestTag("Expense Button")).performClick()
        composeRule.onNode(hasTestTag("Income Button")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chip: Salary")).performClick()
        composeRule.onNode(hasTestTag("Chip: Unused Income")).performClick()
        composeRule.onNode(hasTestTag("Filter action")).performClick()

        // check expense chart
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 0")).assertDoesNotExist()
        onView(withContentDescription("Chart 0")).check(doesNotExist())

        // check income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("Chart Total for page 1"))
            .assertTextEquals("Total: $2,000.32")
        onView(withContentDescription("Chart 1"))
            .check(matches(chartEntry("Salary", 2000.32F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertDoesNotExist()

        // check TranList
        tranListItemCheck(dd.tran3)
        composeRule.onNode(hasTestTag("Empty Transaction List")).assertDoesNotExist()
    }

    @Test
    fun overview_applyAccountDateFilters() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Account filter with 2 chips selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_account))).performClick()
        composeRule.onNode(hasTestTag("Chip: Cash")).performClick()
        composeRule.onNode(hasTestTag("Chip: Credit Card")).performClick()

        // apply Date filter
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_date))).performClick()
        composeRule.onNode(hasTestTag("Filter Start Date")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Filter End Date")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Filter Start Date")).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 4))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasTestTag("Filter Start Date")).assertTextEquals("1/4/70")
        composeRule.onNode(hasTestTag("Filter End Date")).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 6))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasTestTag("Filter End Date")).assertTextEquals("1/6/70")
        composeRule.onNode(hasTestTag("Filter action")).performClick()

        // check expense chart
        composeRule.onNode(hasTestTag("Chart Total for page 0"))
            .assertTextEquals("Total: $55.45")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Entertainment", 55.45F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertDoesNotExist()

        // check income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        tranListItemCheck(dd.tran4)
        composeRule.onNode(hasTestTag("Empty Transaction List")).assertDoesNotExist()
    }

    @Test
    fun overview_applyCategoryDateFilters() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Category filter with 1 chip selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_category))).performClick()
        composeRule.onNode(hasTestTag("Expense Button")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chip: Food")).performClick()

        // apply Date filter
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_date))).performClick()
        composeRule.onNode(hasTestTag("Filter Start Date")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Filter End Date")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Filter Start Date")).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 1))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasTestTag("Filter Start Date")).assertTextEquals("1/1/70")
        composeRule.onNode(hasTestTag("Filter End Date")).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 3))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasTestTag("Filter End Date")).assertTextEquals("1/3/70")
        composeRule.onNode(hasTestTag("Filter action")).performClick()

        // check expense chart
        composeRule.onNode(hasTestTag("Chart Total for page 0"))
            .assertTextEquals("Total: $1,000.10")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertDoesNotExist()

        // check income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        tranListItemCheck(dd.tran1)
        composeRule.onNode(hasTestTag("Empty Transaction List")).assertDoesNotExist()
    }

    @Test
    fun overview_applyAllFilters() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        // open filter and apply Account filter with 1 chip selected
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_filter)))
            .performClick()
        composeRule.onNode(hasTestTag("Filter Card")).assertIsDisplayed()
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_account))).performClick()
        composeRule.onNode(hasTestTag("Chip: Cash")).performClick()

        // open filter and apply Category filter with 1 chip selected
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_category))).performClick()
        composeRule.onNode(hasTestTag("Expense Button")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chip: Food")).performClick()

        // apply Date filter
        composeRule.onNode(hasTestTag(res.getString(R.string.filter_date))).performClick()
        composeRule.onNode(hasTestTag("Filter Start Date")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Filter End Date")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Filter Start Date")).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 1))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasTestTag("Filter Start Date")).assertTextEquals("1/1/70")
        composeRule.onNode(hasTestTag("Filter End Date")).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(1970, 1, 3))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasTestTag("Filter End Date")).assertTextEquals("1/3/70")
        composeRule.onNode(hasTestTag("Filter action")).performClick()

        // check expense chart
        composeRule.onNode(hasTestTag("Chart Total for page 0"))
            .assertTextEquals("Total: $1,000.10")
        onView(withContentDescription("Chart 0"))
            .check(matches(chartEntry("Food", 1000.10F)))
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertDoesNotExist()

        // check income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertDoesNotExist()
        onView(withContentDescription("Chart 1")).check(doesNotExist())

        // check TranList
        tranListItemCheck(dd.tran1)
        composeRule.onNode(hasTestTag("Empty Transaction List")).assertDoesNotExist()
    }

    private fun tranListItemCheck(item: Transaction) {
        // checks that all required information is being displayed
        composeRule.onNodeWithText(item.title).assertExists()
        composeRule.onNodeWithText(item.account).assertExists()
        composeRule.onNodeWithText(dateFormatter.format(item.date)).assertExists()
        // extra check to make sure text is correct color
        composeRule.onNodeWithText(
            text = "\$${totalFormatter.format(item.total)}",
            useUnmergedTree = true
        )
            .assertExists()
            .assertTextColor(if (item.type == "Expense") pwColors.expense else pwColors.income)
        composeRule.onNodeWithText(item.category).assertExists()
    }

    /**
     *  Assertion that looks at text [color]
     */
    fun SemanticsNodeInteraction.assertTextColor(color: Color): SemanticsNodeInteraction =
        assert(isOfColor(color))

    /**
     *  Matcher that checks if text color matches [color] by checking node's TextLayoutResult.
     *  Found on StackOverflow [here](https://stackoverflow.com/a/71077459)
     *  Google does have a section explaining how to make custom semantics properties for testing
     *  [here](https://developer.android.com/jetpack/compose/testing#custom-semantics-properties),
     *  but they have a warning that it shouldn't be used for visual properties like colors...
     */
    private fun isOfColor(color: Color): SemanticsMatcher = SemanticsMatcher(
        "${SemanticsProperties.Text.name} is of color '$color'"
    ) {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        it.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(textLayoutResults)
        return@SemanticsMatcher if (textLayoutResults.isEmpty()) {
            false
        } else {
            textLayoutResults.first().layoutInput.style.color == color
        }
    }
}