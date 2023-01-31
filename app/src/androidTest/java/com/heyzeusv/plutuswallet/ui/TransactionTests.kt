package com.heyzeusv.plutuswallet.ui

import android.widget.DatePicker
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.heyzeusv.plutuswallet.R
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@HiltAndroidTest
class TransactionTests : BaseTest() {

    @Test
    fun transaction_displayNewTransaction() {
        // check that we start on Overview screen and click on 'New Transaction'
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_transaction)))
            .performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // check that fields display default values
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_title)))
            .assertEditTextEquals("")
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date)))
            .assertEditTextEquals(dateFormatter.format(Date()))
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_account)))
            .assertEditTextEquals("")
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_total)))
            .assertEditTextEquals("$0.00")
        composeRule.onNode(hasTestTag(res.getString(R.string.type_expense))).assertIsSelected()
        composeRule.onNode(hasTestTag(res.getString(R.string.type_income))).assertIsNotSelected()
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_category)))
            .assertEditTextEquals("")
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_memo)))
            .assertEditTextEquals("")
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_repeat)))
            .assertIsNotSelected()
        // checks that nodes are not being shown/exist
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_period)))
            .assertDoesNotExist()
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_frequency)))
            .assertDoesNotExist()
    }

    @Test
    fun transaction_displayExistingTransaction() {
        // check that we start on Overview screen and click on existing Transaction
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasTestTag("${dd.tran1.id}")).performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // check that fields display correct values
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_title)))
            .assertEditTextEquals(dd.tran1.title)
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date)))
            .assertEditTextEquals(dateFormatter.format(dd.tran1.date))
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_account)))
            .assertEditTextEquals(dd.tran1.account)
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_total)))
            .assertEditTextEquals("\$${totalFormatter.format(dd.tran1.total)}")
        composeRule.onNode(hasTestTag(res.getString(R.string.type_expense))).assertIsSelected()
        composeRule.onNode(hasTestTag(res.getString(R.string.type_income))).assertIsNotSelected()
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_category)))
            .assertEditTextEquals(dd.tran1.category)
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_memo)))
            .assertEditTextEquals(dd.tran1.memo)
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_repeat)))
            .assertIsSelected()
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_period)))
            .assertEditTextEquals(res.getString(R.string.period_days))
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_frequency)))
            .assertEditTextEquals("${dd.tran1.frequency}")
    }

    @Test
    fun transaction_createNewAccountAndCategories() {
        // names for newly created items
        val testAcc = "Test Account"
        val testExCat = "Test Expense Category"
        val testInCat = "Test Income Category"

        // check that we start on Overview screen and click on 'New Transaction'
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_transaction)))
            .performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // Account
        createNew(R.string.transaction_account, R.string.account_create, testAcc)
        // Expense Category
        createNew(R.string.transaction_category, R.string.category_create, testExCat)
        // switch to Income Categories
        composeRule.onNode(hasTestTag(res.getString(R.string.type_income))).performClick()
        // Income Category
        createNew(R.string.transaction_category, R.string.category_create, testInCat)
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
        composeRule.onNode(hasTestTag(res.getString(nodeId))).performClick()
        composeRule.onNodeWithText(res.getString(createId)).performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(newName)
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // check that newly created item is selected
        composeRule.onNode(hasTestTag(res.getString(nodeId))).assertEditTextEquals(newName)

        // check that newly created item exists in dropdown menu
        composeRule.onNode(hasTestTag(res.getString(nodeId))).performClick()
        composeRule.onNodeWithText(newName).assertExists()
    }

    @Test
    fun transaction_cancelNewAccountAndCategories() {
        // check that we start on Overview screen and click on existing Transaction
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasTestTag("${dd.tran1.id}")).performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // Account
        cancelNew(R.string.transaction_account, R.string.account_create, dd.tran1.account)
        // Expense Category
        cancelNew(R.string.transaction_category, R.string.category_create, dd.tran1.category)
        // switch to Income Categories
        composeRule.onNode(hasTestTag(res.getString(R.string.type_income))).performClick()
        // Income Category
        cancelNew(R.string.transaction_category, R.string.category_create, "")
    }

    /**
     *  Helper function for [transaction_cancelNewAccountAndCategories] test.
     *  Checks the cancellation of new item on node with [nodeId].
     *  This is done by clicking on the node then selecting [createId] from drop down menu
     *  to open up AlertDialog creation. The dismiss button is then pressed and checks that
     *  [previousName] is displayed on node
     */
    private fun cancelNew(nodeId: Int, createId: Int, previousName: String) {
        composeRule.onNode(hasTestTag(res.getString(nodeId))).performClick()
        composeRule.onNodeWithText(res.getString(createId)).performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(
            hasTestTag("AlertDialog dismiss"),
            useUnmergedTree = true
        ).performClick()

        composeRule.onNode(hasTestTag(res.getString(nodeId)))
            .assertEditTextEquals(previousName)
    }

    @Test
    fun transaction_setNewDate() {
        val expectedDate = "Monday, January 18, 2021"

        // check that we start on Overview screen and click on 'New Transaction'
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_transaction)))
            .performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // open DatePickerDialog
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date))).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())
        // check that date is formatted and displayed correctly
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date)))
            .assertEditTextEquals(expectedDate)
    }

    @Test
    fun transaction_cancelNewDate() {
        val expectedDate = "Thursday, January 1, 1970"

        // check that we start on Overview screen and click on existing Transaction
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasTestTag("${dd.tran1.id}")).performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // open DatePickerDialog
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date))).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button2)).perform(click())
        // check that previous date is formatted and displayed correctly
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date)))
            .assertEditTextEquals(expectedDate)
    }

    @Test
    fun transaction_displaySaveSnackbar() {
        // check that we start on Overview screen and click on 'New Transaction'
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_transaction)))
            .performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // check that Snackbar with save message is displayed after Save button is pressed
        composeRule.onNode(hasContentDescription(res.getString(R.string.transaction_save)))
            .performClick()
        composeRule.onNodeWithText(res.getString(R.string.snackbar_saved)).assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun transaction_saveTransactionWithNoTitle() = runTest {
        // retrieve highest id in FakeAndroidRepository and add 1
        var expectedId = 0
        repo.getMaxId().collect { id -> expectedId = id?.plus(1) ?: 0}
        val title = res.getString(R.string.transaction_empty_title)

        // check that we start on Overview screen and click on 'New Transaction'
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_menu_transaction)))
            .performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // check that title is correctly displayed after saving
        composeRule.onNode(hasContentDescription(res.getString(R.string.transaction_save)))
            .performClick()
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_title)))
            .assertEditTextEquals("$title$expectedId")
    }

    @Test
    fun transaction_acceptFutureDialogWarning() {
        // check that we start on Overview screen and click on repeatedTransaction
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasTestTag("${dd.tran1.id}")).performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // change date of Transaction that has already been repeated and save
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date))).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasContentDescription(res.getString(R.string.transaction_save)))
            .performClick()
        // accept warning
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // change the date of Transaction again and save
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date))).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 17))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasContentDescription(res.getString(R.string.transaction_save)))
            .performClick()

        // check that the AlertDialog does not appear again
        composeRule.onNode(hasTestTag("AlertDialog")).assertDoesNotExist()
    }

    @Test
    fun transaction_declineFutureDialogWarning() {
        // check that we start on Overview screen and click on repeatedTransaction
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasTestTag("${dd.tran1.id}")).performClick()

        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()

        // change date of Transaction that has already been repeated and save
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date))).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasContentDescription(res.getString(R.string.transaction_save)))
            .performClick()
        // decline warning
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(
            hasTestTag("AlertDialog dismiss"),
            useUnmergedTree = true
        ).performClick()

        // change the date of Transaction again and save
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date))).performClick()
        // using non-Compose DatePicker so using Espresso here to select the date and confirm
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 17))
        onView(withId(android.R.id.button1)).perform(click())
        composeRule.onNode(hasContentDescription(res.getString(R.string.transaction_save)))
            .performClick()

        // check that the AlertDialog does appear again
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
    }
}

fun SemanticsNodeInteraction.assertEditTextEquals(value: String) : SemanticsNodeInteraction =
    assert(hasEditTextExactly(value))

fun hasEditTextExactly(value: String): SemanticsMatcher =
    SemanticsMatcher("${SemanticsProperties.EditableText.name} is $value") { node ->
        var actual = ""
        node.config.getOrNull(SemanticsProperties.EditableText)?.let { actual = it.text }
        return@SemanticsMatcher actual == value
    }