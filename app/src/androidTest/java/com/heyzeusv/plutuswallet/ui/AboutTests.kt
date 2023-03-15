package com.heyzeusv.plutuswallet.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.onNodeWithTTStrId
import com.heyzeusv.plutuswallet.onNodeWithTextIdUp
import com.heyzeusv.plutuswallet.ui.about.AboutScreen
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AboutTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeRule = createAndroidComposeRule<ComponentActivity>()

    val cl = "Changelog.txt"
    val mpacl = "MPAndroidChartLicense.txt"

    @Before
    fun setUp() {
        composeRule.setContent {
            PlutusWalletTheme {
                AboutScreen(
                    appBarActionSetup = { },
                    navigateUp = { }
                )
            }
        }
    }

    @Test
    fun about_buttonContentHidden() {
        composeRule.onNodeWithTextIdUp(R.string.about_changelog).assertIsEnabled()
        composeRule.onNodeWithTTStrId(R.string.tt_about_file, cl).assertDoesNotExist()

        composeRule.onNodeWithTextIdUp(R.string.about_license).assertIsEnabled()
        composeRule.onNodeWithTTStrId(R.string.tt_about_file, mpacl).assertDoesNotExist()
    }

    @Test
    fun about_buttonContentShownThenHide() {
        composeRule.onNodeWithTextIdUp(R.string.about_changelog).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_about_file, cl).assertIsDisplayed()

        composeRule.onNodeWithTextIdUp(R.string.about_changelog).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_about_file, cl).assertDoesNotExist()

        composeRule.onNodeWithTextIdUp(R.string.about_license).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_about_file, mpacl).assertIsDisplayed()

        composeRule.onNodeWithTextIdUp(R.string.about_license).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_about_file, mpacl).assertDoesNotExist()
    }
}