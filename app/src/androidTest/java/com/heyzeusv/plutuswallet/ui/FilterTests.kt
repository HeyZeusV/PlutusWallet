package com.heyzeusv.plutuswallet.ui

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.ui.overview.FilterCard
import com.heyzeusv.plutuswallet.util.FilterState
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterTests {

    @get:Rule
    val composeRule = createComposeRule()

    val dd = DummyAndroidDataUtil()

    @Test
    fun filter_accountError() {
        snackbarMessageTest(FilterState.NO_SELECTED_ACCOUNT, R.string.filter_no_selected_account)
    }

    @Test
    fun filter_categoryError() {
        snackbarMessageTest(FilterState.NO_SELECTED_CATEGORY, R.string.filter_no_selected_category)
    }

    @Test
    fun filter_dateError() {
        snackbarMessageTest(FilterState.NO_SELECTED_DATE, R.string.filter_no_selected_dates)
    }

    @Test
    fun filter_dateRangeError() {
        snackbarMessageTest(FilterState.INVALID_DATE_RANGE, R.string.filter_invalid_date_range)
    }

    @Test
    fun filter_checkAccountFilterHasAllChips() {
        composeRule.setContent {
            PlutusWalletTheme {
                FilterCard(
                    showFilter = true,
                    accountFilterSelected = true,
                    accountList = dd.accList.map { it.name }
                )
            }
        }

        // check that all Accounts are represented by a Chip
        dd.accList.forEach {
            composeRule.onNodeWithText(it.name).assertExists()
        }
    }

    @Test
    fun filter_checkCategoryFilterHasAllChips() {
        composeRule.setContent {
            PlutusWalletTheme {
                var typeSelected by remember { mutableStateOf(EXPENSE) }
                var catList by remember {
                    mutableStateOf(dd.catList.filter { it.type == EXPENSE.type }.map { it.name })
                }
                FilterCard(
                    showFilter = true,
                    categoryFilterSelected = true,
                    typeSelected = typeSelected,
                    updateTypeSelected = {
                        typeSelected = typeSelected.opposite()
                        catList = dd.catList.filter { it.type == INCOME.type }.map { it.name }
                    },
                    categoryList = catList
                )
            }
        }

        // check that all Categories of type Expense are represented by a Chip
        dd.catList.filter { it.type == EXPENSE.type }.forEach {
            composeRule.onNodeWithText(it.name).assertExists()
        }
        // switch to Income
        composeRule.onNode(hasTestTag("Expense Button")).performClick()
        // check that all Categories of type Income are represented by a Chip
        dd.catList.filter { it.type == INCOME.type }.forEach {
            composeRule.onNodeWithText(it.name).assertExists()
        }
    }

    /**
     *  Testing Snackbar message is the same, only [filterState] and [expectedMessageId] changes.
     *  This function sets up FilterCard while providing [filterState] and checking the actual
     *  Snackbar message is the same as [expectedMessageId].
     */
    private fun snackbarMessageTest(filterState: FilterState, expectedMessageId: Int) {
        val snackbarHostState = SnackbarHostState()
        composeRule.setContent {
            PlutusWalletTheme {
                FilterCard(
                    showFilter = true,
                    filterState = filterState,
                    showSnackbar = { msg -> snackbarHostState.showSnackbar(msg) }
                )
            }
        }

        runBlocking {
            val actualSnackbarText = snapshotFlow { snackbarHostState.currentSnackbarData }
                .filterNotNull().first().message
            val expectedSnackbarText = InstrumentationRegistry.getInstrumentation().targetContext
                .resources.getString(expectedMessageId)
            assertEquals(expectedSnackbarText, actualSnackbarText)
        }
    }
}