package com.heyzeusv.plutuswallet.ui

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.assertCurrentRouteName
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.onNodeWithContDiscId
import com.heyzeusv.plutuswallet.onNodeWithTTStrId
import com.heyzeusv.plutuswallet.onNodeWithTextId
import com.heyzeusv.plutuswallet.util.AboutDestination
import com.heyzeusv.plutuswallet.util.AccountsDestination
import com.heyzeusv.plutuswallet.util.Blank
import com.heyzeusv.plutuswallet.util.CategoriesDestination
import com.heyzeusv.plutuswallet.util.OverviewDestination
import com.heyzeusv.plutuswallet.util.PWDestination
import com.heyzeusv.plutuswallet.util.SettingsDestination
import com.heyzeusv.plutuswallet.util.TransactionDestination
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeRule = createAndroidComposeRule<MainActivity>()

    lateinit var navController: TestNavHostController
    val dd = DummyAndroidDataUtil()

    @Before
    fun setUp() {
        navController = TestNavHostController(composeRule.activity.applicationContext).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
        }

        composeRule.activity.setContent {
            PlutusWalletTheme {
                PlutusWalletApp(navController)
            }
        }
    }

    @Test
    fun navigation_verifyStartDestinationAndAppBar() { checkRouteAndAppBar(OverviewDestination) }

    @Test
    fun navigation_navigateToNewTransaction() {
        // navigate to new Transaction screen by pressing "New Transaction" action button on AppBar
        composeRule.onNodeWithContDiscId(OverviewDestination.actionRightDescription).performClick()

        checkRouteAndAppBar(TransactionDestination, "/{${TransactionDestination.id_arg}}")
        val actualId =
            navController.currentBackStackEntry?.arguments?.getInt(TransactionDestination.id_arg)
        // new Transaction should pass 0 as id argument
        assertEquals(0, actualId)
    }

    @Test
    fun navigation_navigateToExistingTransaction() {
        composeRule.onNodeWithText(dd.tran4.title).performClick()

        checkRouteAndAppBar(TransactionDestination, "/{${TransactionDestination.id_arg}}")
        val actualId =
            navController.currentBackStackEntry?.arguments?.getInt(TransactionDestination.id_arg)
        // existing Transaction should pass its id as id argument
        assertEquals(dd.tran4.id, actualId)
    }

    @Test
    fun navigation_navigateToAccountScreen() {
        navigateToDrawerScreen(AccountsDestination)
        checkRouteAndAppBar(AccountsDestination)
    }

    @Test
    fun navigation_navigateToCategoriesScreen() {
        navigateToDrawerScreen(CategoriesDestination)
        checkRouteAndAppBar(CategoriesDestination)
    }

    @Test
    fun navigation_navigateToSettingsScreen() {
        navigateToDrawerScreen(SettingsDestination)
        checkRouteAndAppBar(SettingsDestination)
    }

    @Test
    fun navigation_navigateToAboutScreen() {
        navigateToDrawerScreen(AboutDestination)
        checkRouteAndAppBar(AboutDestination)
    }

    /**
     *  Checks that AppBar title and nav/action icons match values from [dest].
     */
    private fun checkRouteAndAppBar(dest: PWDestination, navArgs: String = "") {
        // seems like test moves too fast and route name assertion fails due to being null if there
        // isn't a separate assertion beforehand
        composeRule.onNodeWithTTStrId(R.string.tt_app_scaffold).assertIsDisplayed()
        navController.assertCurrentRouteName("${dest.route}$navArgs")
        // check app bar title
        composeRule.onNodeWithTTStrId(
            R.string.tt_app_barTitle, dest.route.replaceFirstChar { it.uppercase() }
        ).assertIsDisplayed()
        // check nav icon
        composeRule.onNodeWithContDiscId(dest.navDescription).assertIsDisplayed()
        // check action buttons
        if (dest.actionRightIcon != Blank) {
            composeRule.onNodeWithContDiscId(dest.actionRightDescription).assertIsDisplayed()
        }
        if (dest.actionLeftIcon != Blank) {
            composeRule.onNodeWithContDiscId(dest.actionLeftDescription).assertIsDisplayed()
        }
    }

    /**
     *  Navigates to given [dest] which is only accessible through Drawer.
     */
    private fun navigateToDrawerScreen(dest: PWDestination) {
        composeRule.onNodeWithContDiscId(R.string.cfl_drawer_description).performClick()
        composeRule.onNodeWithTextId(dest.title).performClick()
    }
}