package com.heyzeusv.plutuswallet.ui

import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.assertDisplayedMessage
import com.heyzeusv.plutuswallet.assertEditTextEquals
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.onNodeWithTTStrId
import com.heyzeusv.plutuswallet.onNodeWithTextId
import com.heyzeusv.plutuswallet.onNodeWithTextIdUp
import com.heyzeusv.plutuswallet.ui.transaction.TransactionCard
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.formatDate
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import java.math.RoundingMode.HALF_UP
import java.text.DecimalFormat
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.time.format.FormatStyle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionTests {

    @get:Rule
    var composeRule = createAndroidComposeRule<ComponentActivity>()

    val dd = DummyAndroidDataUtil()

    val totalFormatter = DecimalFormat("#,##0.00").apply { roundingMode = HALF_UP }

    @Test
    fun transaction_displayNewTransaction() {
        composeRule.setContent {
            PlutusWalletTheme {
                TransactionCard()
            }
        }

        // check that fields display default values
        composeRule.onNodeWithTextId(R.string.transaction_title).assertEditTextEquals("")
        composeRule.onNodeWithTextId(R.string.transaction_date)
            .assertEditTextEquals(formatDate(ZonedDateTime.now(systemDefault()), FormatStyle.LONG))
        composeRule.onNodeWithTextId(R.string.transaction_account).assertEditTextEquals("")
        composeRule.onNodeWithTextId(R.string.transaction_total).assertEditTextEquals("$0.00")
        composeRule.onNodeWithTextIdUp(R.string.type_expense).assertIsSelected()
        composeRule.onNodeWithTextIdUp(R.string.type_income).assertIsNotSelected()
        composeRule.onNodeWithTextId(R.string.transaction_category).assertEditTextEquals("")
        composeRule.onNodeWithTextId(R.string.transaction_memo).assertEditTextEquals("")
        composeRule.onNodeWithTextIdUp(R.string.transaction_repeat).assertIsNotSelected()
        // checks that nodes are not being shown/exist
        composeRule.onNodeWithTextId(R.string.transaction_period).assertDoesNotExist()
        composeRule.onNodeWithTextId(R.string.transaction_frequency).assertDoesNotExist()
    }

    @Test
    fun transaction_displayExistingTransaction() {
        composeRule.setContent {
            PlutusWalletTheme {
                TransactionCard(
                    transaction = dd.tran1,
                    title = dd.tran1.title,
                    date = formatDate(dd.tran1.date, FormatStyle.LONG),
                    account = dd.tran1.account,
                    total = TextFieldValue("\$${totalFormatter.format(dd.tran1.total)}"),
                    selectedCat = dd.tran1.category,
                    memo = dd.tran1.memo,
                    repeat = dd.tran1.repeating,
                    period = "Days",
                    frequency = TextFieldValue("${dd.tran1.frequency}"),
                )
            }
        }

        // check that fields display correct values
        composeRule.onNodeWithTextId(R.string.transaction_title)
            .assertEditTextEquals(dd.tran1.title)
        composeRule.onNodeWithTextId(R.string.transaction_date)
           .assertEditTextEquals(formatDate(dd.tran1.date, FormatStyle.LONG))
        composeRule.onNodeWithTextId(R.string.transaction_account)
            .assertEditTextEquals(dd.tran1.account)
        composeRule.onNodeWithTextId(R.string.transaction_total)
            .assertEditTextEquals("\$${totalFormatter.format(dd.tran1.total)}")
        composeRule.onNodeWithTextIdUp(R.string.type_expense).assertIsSelected()
        composeRule.onNodeWithTextIdUp(R.string.type_income).assertIsNotSelected()
        composeRule.onNodeWithTextId(R.string.transaction_category)
            .assertEditTextEquals(dd.tran1.category)
        composeRule.onNodeWithTextId(R.string.transaction_memo).assertEditTextEquals(dd.tran1.memo)
        composeRule.onNodeWithTextIdUp(R.string.transaction_repeat).assertIsSelected()
        composeRule.onNodeWithTextId(R.string.transaction_period)
            .assertEditTextEquals(composeRule.activity.getString(R.string.period_days))
        composeRule.onNodeWithTextId(R.string.transaction_frequency)
            .assertEditTextEquals("${dd.tran1.frequency}")
    }

    @Test
    fun transaction_createNewAccountAndCategories() {
        composeRule.setContent {
            PlutusWalletTheme {
                var account by remember { mutableStateOf("") }
                var typeSelected by remember { mutableStateOf(EXPENSE) }
                var selectedCat by remember { mutableStateOf("") }
                var accountList by remember { mutableStateOf(listOf("Create New Account")) }
                val expenseCatList = listOf("Create New Category")
                val incomeCatList = listOf("Create New Category")
                var categoryList by remember { mutableStateOf(expenseCatList) }
                var showAccountDialog by remember { mutableStateOf(false) }
                var showCategoryDialog by remember { mutableStateOf(false) }
                TransactionCard(
                    account = account,
                    typeSelected = typeSelected,
                    updateTypeSelected = {
                        typeSelected = typeSelected.opposite()
                        categoryList =
                            if (typeSelected == EXPENSE) expenseCatList else incomeCatList
                    },
                    selectedCat = selectedCat,
                    accountList = accountList,
                    categoryList = categoryList,
                    showAccountDialog = showAccountDialog,
                    updateAccountDialog = { showAccountDialog = it },
                    accountDialogOnConfirm = {
                        account = it
                        accountList = listOf(it) + accountList
                        showAccountDialog = false
                    },
                    showCategoryDialog = showCategoryDialog,
                    updateCategoryDialog = { showCategoryDialog = it },
                    categoryDialogOnConfirm = {
                        selectedCat = it
                        categoryList = listOf(it) + categoryList
                        showCategoryDialog = false
                    }
                )
            }
        }
        // names for newly created items
        val testAcc = "Test Account"
        val testExCat = "Test Expense Category"
        val testInCat = "Test Income Category"

        // Account
        createNew(R.string.transaction_account, R.string.account_create, testAcc)
        // Expense Category
        createNew(R.string.transaction_category, R.string.category_create, testExCat)
        // switch to Income Categories
        composeRule.onNodeWithTextIdUp(R.string.type_income).performClick()
        // Income Category
        createNew(R.string.transaction_category, R.string.category_create, testInCat)
    }

    @Test
    fun transaction_cancelNewAccountAndCategories() {
        composeRule.setContent {
            PlutusWalletTheme {
                val accountList by remember {
                    mutableStateOf(listOf("Test Account", "Create New Account"))
                }
                val expenseCatList = listOf("Test Expense Category", "Create New Category")
                val incomeCatList = listOf("Test Income Category", "Create New Category")
                var account by remember { mutableStateOf(accountList[0]) }
                var typeSelected by remember { mutableStateOf(EXPENSE) }
                var selectedCat by remember { mutableStateOf(expenseCatList[0]) }
                var categoryList by remember { mutableStateOf(expenseCatList) }
                var showAccountDialog by remember { mutableStateOf(false) }
                var showCategoryDialog by remember { mutableStateOf(false) }
                TransactionCard(
                    account = account,
                    updateAccount = { account = it },
                    typeSelected = typeSelected,
                    updateTypeSelected = {
                        typeSelected = typeSelected.opposite()
                        if (typeSelected == EXPENSE) {
                            categoryList = expenseCatList
                            selectedCat = categoryList[0]

                        } else {
                            categoryList = incomeCatList
                            selectedCat = categoryList[0]
                        }
                    },
                    selectedCat = selectedCat,
                    updateSelectedCat = { selectedCat = it },
                    accountList = accountList,
                    categoryList = categoryList,
                    showAccountDialog = showAccountDialog,
                    updateAccountDialog = { showAccountDialog = it },
                    showCategoryDialog = showCategoryDialog,
                    updateCategoryDialog = { showCategoryDialog = it },
                )
            }
        }
        // Account
        cancelNew(R.string.transaction_account, R.string.account_create, "Test Account")
        // Expense Category
        cancelNew(R.string.transaction_category, R.string.category_create, "Test Expense Category")
        // switch to Income Categories
        composeRule.onNodeWithTextIdUp(R.string.type_income).performClick()
        // Income Category
        cancelNew(R.string.transaction_category, R.string.category_create, "Test Income Category")
    }

    @Test
    fun transaction_setNewDate() {
        val expectedDate = "January 18, 2021"
        composeRule.setContent {
            PlutusWalletTheme {
                var date by remember { mutableStateOf("") }
                TransactionCard(
                    date = date,
                    onDateSelected = { date = formatDate(it, FormatStyle.LONG) }
                )
            }
        }

        // open DatePickerDialog
        composeRule.onNodeWithTextId(R.string.transaction_date).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())
        // check that date is formatted and displayed correctly
        composeRule.onNodeWithTextId(R.string.transaction_date).assertEditTextEquals(expectedDate)
    }

    @Test
    fun transaction_cancelNewDate() {
        val expectedDate = "Thursday, January 1, 1970"
        composeRule.setContent {
            PlutusWalletTheme {
                var date by remember { mutableStateOf(expectedDate) }
                TransactionCard(
                    date = date,
                    onDateSelected = { date = formatDate(it, FormatStyle.LONG) }
                )
            }
        }

        // open DatePickerDialog
        composeRule.onNodeWithTextId(R.string.transaction_date).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button2)).perform(click())
        // check that previous date is formatted and displayed correctly
        composeRule.onNodeWithTextId(R.string.transaction_date).assertEditTextEquals(expectedDate)
    }

    @Test
    fun transaction_displaySaveSnackbar() {
        val snackbarHostState = SnackbarHostState()
        composeRule.setContent {
            PlutusWalletTheme {
                TransactionCard(
                    saveSuccess = true,
                    onSaveSuccess = { msg -> snackbarHostState.showSnackbar(msg) }
                )
            }
        }

        snackbarHostState.assertDisplayedMessage(R.string.snackbar_saved)
    }

    @Test
    fun transaction_acceptFutureDialogWarning() {
        composeRule.setContent {
            PlutusWalletTheme {
                val tran = dd.tran1
                var date by remember { mutableStateOf(formatDate(dd.tran1.date, FormatStyle.LONG)) }
                var showFutureDialog by remember { mutableStateOf(false) }
                var saveSuccess by remember { mutableStateOf(false) }
                TransactionCard(
                    transaction = tran,
                    date = date,
                    onDateSelected = {
                        date = formatDate(it, FormatStyle.LONG)
                        // simulate the user clicking save after changing date
                        saveSuccess = true
                    },
                    showFutureDialog = showFutureDialog,
                    futureDialogOnConfirm = {
                        tran.futureTCreated = false
                        showFutureDialog = false
                    },
                    saveSuccess = saveSuccess,
                    onSaveSuccess = {
                        // this is normally down by AppBar save button, but we are simulating it
                        // using saveSuccess which normally displays Snackbar
                        if (date != formatDate(tran.date, FormatStyle.LONG) && tran.futureTCreated) {
                            showFutureDialog = true
                        }
                        saveSuccess = false
                    }
                )
            }
        }

        // change date of Transaction that has already been repeated and save
        composeRule.onNodeWithTextId(R.string.transaction_date).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())

        // accept warning
        composeRule.onNodeWithTTStrId(R.string.tt_ad).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_ad_confirm, useUnmergedTree = true).performClick()

        // change the date of Transaction again and save
        composeRule.onNodeWithTextId(R.string.transaction_date).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 17))
        onView(withId(android.R.id.button1)).perform(click())

        // check that the AlertDialog does not appear again
        composeRule.onNodeWithTTStrId(R.string.tt_ad).assertDoesNotExist()
    }

    @Test
    fun transaction_declineFutureDialogWarning() {
        composeRule.setContent {
            PlutusWalletTheme {
                val tran = dd.tran1
                var date by remember { mutableStateOf(formatDate(tran.date, FormatStyle.LONG)) }
                var showFutureDialog by remember { mutableStateOf(false) }
                var saveSuccess by remember { mutableStateOf(false) }
                TransactionCard(
                    transaction = tran,
                    date = date,
                    onDateSelected = {
                        date = formatDate(it, FormatStyle.LONG)
                        // simulate the user clicking save after changing date
                        saveSuccess = true
                    },
                    showFutureDialog = showFutureDialog,
                    futureDialogOnDismiss = { showFutureDialog = false },
                    saveSuccess = saveSuccess,
                    onSaveSuccess = {
                        // this is normally down by AppBar save button, but we are simulating it
                        // using saveSuccess which normally displays Snackbar
                        if (date != formatDate(tran.date, FormatStyle.LONG) && tran.futureTCreated) {
                            showFutureDialog = true
                        }
                        saveSuccess = false
                    }
                )
            }
        }

        // change date of Transaction that has already been repeated and save
        composeRule.onNodeWithTextId(R.string.transaction_date).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())

        // decline warning
        composeRule.onNodeWithTTStrId(R.string.tt_ad).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_ad_dismiss, useUnmergedTree = true).performClick()

        // change the date of Transaction again and save
        composeRule.onNodeWithTextId(R.string.transaction_date).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 17))
        onView(withId(android.R.id.button1)).perform(click())

        // check that the AlertDialog does appear again
        composeRule.onNodeWithTTStrId(R.string.tt_ad).assertIsDisplayed()
    }

    /**
     *  Helper function for [transaction_createNewAccountAndCategories] test.
     *  Checks the creation of new item on node with [nodeId].
     *  This is done by clicking on the node then selecting [createId] from drop down menu
     *  to open up AlertDialog creation. Confirm button is pressed after entering name of item and
     *  then check if item with [newName] exists as text for node and in drop down menu.
     */
    private fun createNew(nodeId: Int, createId: Int, newName: String) {
        // create new item
        composeRule.onNodeWithTextId(nodeId).performClick()
        composeRule.onNodeWithTextId(createId).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_ad).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_ad_input).performTextInput(newName)
        composeRule.onNodeWithTTStrId(R.string.tt_ad_confirm,
            useUnmergedTree = true
        ).performClick()

        // check that newly created item is selected
        composeRule.onNodeWithTextId(nodeId).assertEditTextEquals(newName)

        // check that newly created item exists in dropdown menu
        composeRule.onNodeWithTextId(nodeId).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_tran_ddmItem, newName).assertIsDisplayed()
    }

    /**
     *  Helper function for [transaction_cancelNewAccountAndCategories] test.
     *  Checks the cancellation of new item on node with [nodeId].
     *  This is done by clicking on the node then selecting [createId] from drop down menu
     *  to open up AlertDialog creation. The dismiss button is then pressed and checks that
     *  [previousName] is displayed on node
     */
    private fun cancelNew(nodeId: Int, createId: Int, previousName: String) {
        composeRule.onNodeWithTextId(nodeId).performClick()
        composeRule.onNodeWithTextId(createId).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_ad).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(R.string.tt_ad_dismiss, useUnmergedTree = true).performClick()

        composeRule.onNodeWithTextId(nodeId).assertEditTextEquals(previousName)
    }
}