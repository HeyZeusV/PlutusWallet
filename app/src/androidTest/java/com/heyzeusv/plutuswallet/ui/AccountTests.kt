package com.heyzeusv.plutuswallet.ui

import androidx.activity.viewModels
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.ui.list.AccountViewModel
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
        val createNew = hasContentDescription(res.getString(R.string.account_new))
        navigateToAccountScreen()

        val testAccount = "Test Account"
        dialogAction(createNew, testAccount, "AlertDialog confirm")

        // check that new Account was created then check all
        composeRule.onNodeWithText(testAccount).assertExists()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_createNewAccountExists() {
        val createNew = hasContentDescription(res.getString(R.string.account_new))
        navigateToAccountScreen()

        dialogAction(createNew, dd.acc1.name, "AlertDialog confirm")

        // check that snackbar with message appears
        composeRule.onNodeWithText(res.getString(R.string.snackbar_exists, dd.acc1.name))
            .assertIsDisplayed()
        // check that no repeating Account exists
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_createNewAccountDismiss() {
        val createNew = hasContentDescription(res.getString(R.string.account_new))
        navigateToAccountScreen()

        val testAccount = "Test Account"
        dialogAction(createNew, testAccount, "AlertDialog dismiss")

        // check that new Account was not created then check all
        composeRule.onNodeWithText(testAccount).assertDoesNotExist()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_editAccount() {
        navigateToAccountScreen()

        val newName = "New Test Name"
        dialogAction(hasTestTag("${dd.acc5.name} Edit"), newName, "AlertDialog confirm")

        // check for updated name then check all
        composeRule.onNodeWithText(newName).assertExists()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_editAccountExists() {
        navigateToAccountScreen()

        dialogAction(hasTestTag("${dd.acc5.name} Edit"), dd.acc2.name, "AlertDialog confirm")

        // check that snackbar with message appears then check all
        composeRule.onNodeWithText(res.getString(R.string.snackbar_exists, dd.acc2.name))
            .assertIsDisplayed()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_editAccountDismiss() {
        navigateToAccountScreen()

        dialogAction(hasTestTag("${dd.acc5.name} Edit"), dd.acc2.name, "AlertDialog dismiss")

        // check that Account exists unedited then check all
        composeRule.onNodeWithText(dd.acc5.name).assertExists()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_deleteAccount() {
        navigateToAccountScreen()

        dialogAction(hasTestTag("${dd.acc5.name} Delete"), "", "AlertDialog confirm")

        // check that account is Deleted then check all
        composeRule.onNodeWithText(dd.acc5.name).assertDoesNotExist()
        checkAccountsAndDeleteState()
    }

    @Test
    fun account_deleteAccountDismiss() {
        navigateToAccountScreen()

        dialogAction(hasTestTag("${dd.acc5.name} Delete"), "", "AlertDialog dismiss")

        // check that Account still exists then check all
        composeRule.onNodeWithText(dd.acc5.name).assertExists()
        checkAccountsAndDeleteState()
    }

    /**
     *  Clicks on [node] then types in [name] into input field if it is not empty and
     *  performs [action].
     */
    private fun dialogAction(node: SemanticsMatcher, name: String, action: String) {
        composeRule.onNode(node).performClick()
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        if (name.isNotBlank()) {
            composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(name)
        }
        composeRule.onNode(
            hasTestTag(action),
            useUnmergedTree = true
        ).performClick()
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