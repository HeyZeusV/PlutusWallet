package com.heyzeusv.plutuswallet.ui

import android.content.res.Resources
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.checkTlifIsDisplayed
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.data.FakeAndroidRepository
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.ui.overview.TransactionListCard
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TransactionListTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeRule = createComposeRule()

    @Inject lateinit var fakeRepo: PWRepositoryInterface
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
    fun tranList_displayTransactions() {
        composeRule.setContent {
            PlutusWalletTheme {
                TransactionListCard(
                    tranList = repo.tlifList
                )
            }
        }

        // check that each Transaction is displayed correctly
        repo.tlifList.forEach {
            checkTlifIsDisplayed(composeRule, it)
        }
        composeRule.onNodeWithText(res.getString(R.string.cfl_no_transactions)).assertDoesNotExist()
    }

    @Test
    fun tranList_noTransactions() {
        composeRule.setContent {
            PlutusWalletTheme {
                TransactionListCard()
            }
        }

        // check that "No Transactions exist" string is displayed
        composeRule.onNodeWithText(res.getString(R.string.cfl_no_transactions)).assertIsDisplayed()
    }

    @Test
    fun tranList_deleteTransaction_confirm() {
        composeRule.setContent {
            PlutusWalletTheme {
                val tlifList by repo.tlifFlow.collectAsState(emptyList())
                var showDeleteDialog by remember { mutableStateOf(0) }
                TransactionListCard(
                    tranList = tlifList,
                    itemOnLongClick = { showDeleteDialog = it },
                    showDeleteDialog = showDeleteDialog,
                    deleteDialogOnConfirm = {
                        runBlocking {
                            repo.tlifList.removeAt(it - 1)
                            repo.tlifEmit(repo.tlifList)
                            showDeleteDialog = 0
                        }
                    },
                )
            }
        }

        runBlocking {
            repo.tlifEmit(repo.tlifList)

            // long press Transaction to bring up delete AlertDialog
            composeRule.onNode(hasTestTag("${dd.tlif2.tli.id}")).performTouchInput { longClick() }
            // checks that AlertDialog is being displayed and press confirm button
            composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
            composeRule.onNode(
                hasTestTag("AlertDialog confirm"),
                useUnmergedTree = true
            ).performClick()

            // check that correct item was deleted
            composeRule.onNode(hasTestTag("${dd.tlif2.tli.id}")).assertDoesNotExist()
            // check that no other item was deleted
            val expectedTlifList = listOf(dd.tlif1, dd.tlif3, dd.tlif4)
            expectedTlifList.forEach {
                checkTlifIsDisplayed(composeRule, it)
            }
        }
    }

    @Test
    fun tranList_deleteTransaction_dismiss() {
        composeRule.setContent {
            PlutusWalletTheme {
                val tlifList by repo.tlifFlow.collectAsState(emptyList())
                var showDeleteDialog by remember { mutableStateOf(0) }
                TransactionListCard(
                    tranList = tlifList,
                    itemOnLongClick = { showDeleteDialog = it },
                    showDeleteDialog = showDeleteDialog,
                    deleteDialogOnDismiss = { showDeleteDialog = 0 },
                )
            }
        }

        runBlocking {
            repo.tlifEmit(repo.tlifList)

            // long press Transaction to bring up delete AlertDialog
            composeRule.onNode(hasTestTag("${dd.tlif2.tli.id}")).performTouchInput { longClick() }
            // checks that AlertDialog is being displayed and press dismiss button
            composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
            composeRule.onNode(
                hasTestTag("AlertDialog dismiss"),
                useUnmergedTree = true
            ).performClick()
            composeRule.onNode(hasTestTag("AlertDialog")).assertDoesNotExist()

            // check that no item was deleted
            val expectedTlifList = listOf(dd.tlif1, dd.tlif2, dd.tlif3, dd.tlif4)
            expectedTlifList.forEach {
                checkTlifIsDisplayed(composeRule, it)
            }
        }
    }
}