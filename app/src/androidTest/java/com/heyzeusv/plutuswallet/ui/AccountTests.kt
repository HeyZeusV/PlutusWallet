package com.heyzeusv.plutuswallet.ui

import androidx.activity.viewModels
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.ui.account.AccountViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AccountTests : BaseTest() {

    @Test
    fun account_displayAllAccounts() {
        navigateToAccountScreen()

        checkAccountsAndDeleteState()
    }

    @Test
    fun account_createNewAccount() {
        navigateToAccountScreen()

        val testAccount = "Test Account"
        // create new Account
        composeRule.onNode(hasContentDescription(res.getString(R.string.account_new)))
            .performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(testAccount)
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // check that new Account was created then check all
        composeRule.onNodeWithText(testAccount).assertExists()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_createNewAccountExists() {
        navigateToAccountScreen()

        // create new Account with existing name
        composeRule.onNode(hasContentDescription(res.getString(R.string.account_new)))
            .performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(dd.acc1.name)
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // check that snackbar with message appears
        composeRule.onNodeWithText(res.getString(R.string.snackbar_exists, dd.acc1.name))
            .assertIsDisplayed()
        // check that no repeating Account exists
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_createNewAccountDismiss() {
        navigateToAccountScreen()

        val testAccount = "Test Account"
        // create new Account
        composeRule.onNode(hasContentDescription(res.getString(R.string.account_new)))
            .performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(testAccount)
        composeRule.onNode(
            hasTestTag("AlertDialog dismiss"),
            useUnmergedTree = true
        ).performClick()

        // check that new Account was not created then check all
        composeRule.onNodeWithText(testAccount).assertDoesNotExist()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_editAccount() {
        navigateToAccountScreen()

        val newName = "New Test Name"
        // edit Account
        composeRule.onNode(hasTestTag("${dd.acc5.name} Edit")).performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(newName)
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // check for updated name then check all
        composeRule.onNodeWithText(newName).assertExists()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_editAccountExists() {
        navigateToAccountScreen()

        // edit Account
        composeRule.onNode(hasTestTag("${dd.acc5.name} Edit")).performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(dd.acc2.name)
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // check that snackbar with message appears then check all
        composeRule.onNodeWithText(res.getString(R.string.snackbar_exists, dd.acc2.name))
            .assertIsDisplayed()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_editAccountDismiss() {
        navigateToAccountScreen()

        // edit Account
        composeRule.onNode(hasTestTag("${dd.acc5.name} Edit")).performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(dd.acc2.name)
        composeRule.onNode(
            hasTestTag("AlertDialog dismiss"),
            useUnmergedTree = true
        ).performClick()

        // check that Account exists unedited then check all
        composeRule.onNodeWithText(dd.acc5.name).assertExists()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_deleteAccount() {
        navigateToAccountScreen()

        // delete Account
        composeRule.onNode(hasTestTag("${dd.acc5.name} Delete")).performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // check that account is Deleted then check all
        composeRule.onNodeWithText(dd.acc5.name).assertDoesNotExist()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_deleteAccountDismiss() {
        navigateToAccountScreen()

        // open delete dialog but dismiss
        composeRule.onNode(hasTestTag("${dd.acc5.name} Delete")).performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        composeRule.onNode(
            hasTestTag("AlertDialog dismiss"),
            useUnmergedTree = true
        ).performClick()

        // check that Account still exists then check all
        composeRule.onNodeWithText(dd.acc5.name).assertExists()
        checkAccountsAndDeleteState()
    }

    private fun navigateToAccountScreen() {
        // check that we start on Overview screen, open drawer, and navigate to Account screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_drawer_description)))
            .performClick()
        composeRule.onNode(hasTestTag("DrawerItem Accounts")).performClick()

        // check that we navigate to Account screen
        composeRule.onNode(hasTestTag("AppBar Accounts")).assertExists()
    }

    /**
     *  Check that all Accounts in Repo are being displayed with correct delete button state
     */
    private fun checkAccountsAndDeleteState() {
        repo.accList.forEach {
            composeRule.onNodeWithText(it.name).assertExists()
            if (
                composeRule.activity.viewModels<AccountViewModel>()
                    .value.accountsUsedList.value.contains(it)
            ) {
                composeRule.onNode(hasTestTag("${it.name} Delete")).assertIsNotEnabled()
            } else {
                composeRule.onNode(hasTestTag("${it.name} Delete")).assertIsEnabled()
            }
        }
    }
}