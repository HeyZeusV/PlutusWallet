package com.heyzeusv.plutuswallet.ui

import android.content.res.Resources
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyzeusv.plutuswallet.chartEntry
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.data.FakeAndroidRepository
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.data.model.ChartInformation
import com.heyzeusv.plutuswallet.ui.overview.ChartCard
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChartTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeRule = createComposeRule()

    @Inject
    lateinit var fakeRepo: PWRepositoryInterface
    lateinit var repo: FakeAndroidRepository
    val dd = DummyAndroidDataUtil()
    lateinit var res: Resources

    @Before
    fun setUp() {
        hiltRule.inject()
        repo = (fakeRepo as FakeAndroidRepository)
        res = InstrumentationRegistry.getInstrumentation().targetContext.resources
    }

    @BeforeEach
    fun setUpBeforeEach() {
        repo.resetLists()
    }

    @Test
    fun chart_displayCharts() {
        composeRule.setContent {
            PlutusWalletTheme {
                ChartCard(chartInfoList = listOf(dd.expenseCi, dd.incomeCi))
            }
        }

        // check that Chart + total are displayed, while empty string is not for Expense
        composeRule.onNode(hasTestTag("Chart page 0")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 0")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertDoesNotExist()

        // swipe to Income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }

        // check that Chart + total are displayed, while empty string is not for Income
        composeRule.onNode(hasTestTag("Chart page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Chart Total for page 1")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertDoesNotExist()
    }

    @Test
    fun chart_noTransactions() {
        composeRule.setContent {
            PlutusWalletTheme {
                ChartCard()
            }
        }

        // check that Chart + total is not displayed, while empty string is for Expense
        composeRule.onNode(hasTestTag("Chart page 0")).assertDoesNotExist()
        composeRule.onNode(hasTestTag("Empty Chart for page 0")).assertIsDisplayed()

        // swipe to Income chart
        composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }

        // check that Chart + total is not displayed, while empty string is for Income
        composeRule.onNode(hasTestTag("Chart page 1")).assertDoesNotExist()
        composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()
    }

    @Test
    fun chart_transactionDeleted() {
        composeRule.setContent {
            PlutusWalletTheme {
                val chartInfoList by repo.chartInfoListFlow.collectAsState(
                    initial = listOf(ChartInformation(), ChartInformation())
                )
                ChartCard(chartInfoList = chartInfoList)
            }
        }

        runBlocking {
            repo.chartInfoListEmit(listOf(dd.expenseCi, dd.incomeCi))

            // check total is correctly displayed
            composeRule.onNode(hasTestTag("Chart Total for page 0"))
                .assertTextEquals("Total: $1,155.55")
            // check correct slices are shown
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Entertainment", 55.45F)))
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Food", 1000.10F)))
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Housing", 100.00F)))


            // emit ChartInformation with 'deleted' Transaction and check total updates correctly
            repo.chartInfoListEmit(listOf(dd.expenseCiNoCt1, dd.incomeCi))
            composeRule.onNode(hasTestTag("Chart Total for page 0"))
                .assertTextEquals("Total: $155.45")
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Entertainment", 55.45F)))
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Housing", 100.00F)))
            // check 'deleted' slice doesn't exist
            onView(withContentDescription("Chart 0"))
                .check(matches(not(chartEntry("Food", 1000.10F))))

            // swipe to Income chart
            composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }

            // check total is correctly displayed
            composeRule.onNode(hasTestTag("Chart Total for page 1"))
                .assertTextEquals("Total: $2,000.32")
            // check correct slice is shown
            onView(withContentDescription("Chart 1"))
                .check(matches(chartEntry("Salary", 2000.32F)))

            // emit ChartInformation with 'deleted' Transaction and check Chart gets replaced
            repo.chartInfoListEmit(listOf(dd.expenseCiNoCt1, ChartInformation()))
            composeRule.onNode(hasTestTag("Chart page 1")).assertDoesNotExist()
            composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()
        }
    }

    @Test
    fun chart_transactionInserted() {
        composeRule.setContent {
            PlutusWalletTheme {
                val chartInfoList by repo.chartInfoListFlow.collectAsState(
                    initial = listOf(ChartInformation(), ChartInformation())
                )
                ChartCard(chartInfoList = chartInfoList)
            }
        }

        runBlocking {
            repo.chartInfoListEmit(listOf(dd.expenseCiNoCt1, ChartInformation()))

            // check total is correctly displayed
            composeRule.onNode(hasTestTag("Chart Total for page 0"))
                .assertTextEquals("Total: $155.45")
            // check correct slices are shown
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Entertainment", 55.45F)))
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Housing", 100.00F)))

            // emit ChartInformation with 'inserted' Transaction and check total updates correctly
            repo.chartInfoListEmit(listOf(dd.expenseCi, ChartInformation()))
            composeRule.onNode(hasTestTag("Chart Total for page 0"))
                .assertTextEquals("Total: $1,155.55")
            // check correct slices are shown
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Entertainment", 55.45F)))
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Food", 1000.10F)))
            onView(withContentDescription("Chart 0"))
                .check(matches(chartEntry("Housing", 100.00F)))

            // swipe to Income chart
            composeRule.onNode(hasTestTag("Chart ViewPager")).performTouchInput { swipeLeft() }

            // check that empty string is displayed
            composeRule.onNode(hasTestTag("Chart page 1")).assertDoesNotExist()
            composeRule.onNode(hasTestTag("Empty Chart for page 1")).assertIsDisplayed()

            // emit ChartInformation with 'inserted' Transaction and check Chart is now shown
            repo.chartInfoListEmit(listOf(dd.expenseCi, dd.incomeCi))
            composeRule.onNode(hasTestTag("Chart Total for page 1"))
                .assertTextEquals("Total: $2,000.32")
            // check correct slice is shown
            onView(withContentDescription("Chart 1"))
                .check(matches(chartEntry("Salary", 2000.32F)))
        }
    }
}