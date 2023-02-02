package com.heyzeusv.plutuswallet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.heyzeusv.plutuswallet.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AboutTests : BaseTest() {

    @Test
    fun about_buttonContentHidden() {
        navigateToAboutScreen()

        composeRule.onNodeWithText(res.getString(R.string.about_changelog).uppercase())
            .assertIsEnabled()
        composeRule.onNode(hasTestTag("File Changelog.txt")).assertDoesNotExist()

        composeRule.onNodeWithText(res.getString(R.string.about_license).uppercase())
            .assertIsEnabled()
        composeRule.onNode(hasTestTag("File MPAndroidChartLicense.txt")).assertDoesNotExist()
    }

    @Test
    fun about_buttonContentShownThenHide() {
        navigateToAboutScreen()

        composeRule.onNodeWithText(res.getString(R.string.about_changelog).uppercase())
            .performClick()
        composeRule.onNode(hasTestTag("File Changelog.txt")).assertIsDisplayed()

        composeRule.onNodeWithText(res.getString(R.string.about_changelog).uppercase())
            .performClick()
        composeRule.onNode(hasTestTag("File Changelog.txt")).assertDoesNotExist()

        composeRule.onNodeWithText(res.getString(R.string.about_license).uppercase())
            .performClick()
        composeRule.onNode(hasTestTag("File MPAndroidChartLicense.txt")).assertIsDisplayed()

        composeRule.onNodeWithText(res.getString(R.string.about_license).uppercase())
            .performClick()
        composeRule.onNode(hasTestTag("File MPAndroidChartLicense.txt")).assertDoesNotExist()
    }

    private fun navigateToAboutScreen() {
        // check that we start on Overview screen, open drawer, and navigate to Account screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_drawer_description)))
            .performClick()
        composeRule.onNode(hasTestTag("DrawerItem About")).performClick()

        // check that we navigate to Account screen
        composeRule.onNode(hasTestTag("AppBar About")).assertExists()
    }
}