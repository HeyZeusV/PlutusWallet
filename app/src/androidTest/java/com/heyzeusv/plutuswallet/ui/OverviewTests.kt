package com.heyzeusv.plutuswallet.ui

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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.chartEntry
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.chartText
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.FilterInfo
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

        dd.tranList.forEach { item ->
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