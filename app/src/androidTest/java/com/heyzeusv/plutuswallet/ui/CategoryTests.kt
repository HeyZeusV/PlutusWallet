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
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.ui.category.CategoryViewModel
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.INCOME
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CategoryTests : BaseTest() {

    @Test
    fun category_displayAllCategories() {
        navigateToCategoryScreen()

        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()

        checkCategoriesByTypeAndDeleteState(INCOME)
    }

    @Test
    fun category_createNewCategory() {
        val createNew = hasContentDescription(res.getString(R.string.category_new))
        navigateToCategoryScreen()

        val testCategory = "Test Category"
        dialogAction(createNew, testCategory, "AlertDialog confirm")

        // check that new Category was created then check all
        composeRule.onNodeWithText(testCategory).assertExists()
        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()

        dialogAction(createNew, testCategory, "AlertDialog confirm")

        // check that new Category was created then check all
        composeRule.onNodeWithText(testCategory).assertExists()
        checkCategoriesByTypeAndDeleteState(INCOME)
    }

    @Test
    fun category_createNewCategoryExists() {
        val createNew = hasContentDescription(res.getString(R.string.category_new))
        navigateToCategoryScreen()

        dialogAction(createNew, dd.cat1.name, "AlertDialog confirm")

        // check that snackbar with message appears
        composeRule.onNodeWithText(res.getString(R.string.snackbar_exists, dd.cat1.name))
            .assertIsDisplayed()
        // check that no repeating Category exists
        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()

        dialogAction(createNew, dd.cat3.name, "AlertDialog confirm")

        // check that snackbar with message appears
        composeRule.onNodeWithText(res.getString(R.string.snackbar_exists, dd.cat3.name))
            .assertIsDisplayed()
        // check that no repeating Category exists
        checkCategoriesByTypeAndDeleteState(INCOME)
    }

    @Test
    fun category_createNewCategoryDismiss() {
        val createNew = hasContentDescription(res.getString(R.string.category_new))
        navigateToCategoryScreen()

        val testCategory = "Test"
        dialogAction(createNew, testCategory, "AlertDialog dismiss")

        // check that new Category was not created then check all
        composeRule.onNodeWithText(testCategory).assertDoesNotExist()
        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()

        dialogAction(createNew, testCategory, "AlertDialog dismiss")

        // check that new Category was not created then check all
        composeRule.onNodeWithText(testCategory).assertDoesNotExist()
        checkCategoriesByTypeAndDeleteState(INCOME)
    }

    @Test
    fun category_editCategory() {
        navigateToCategoryScreen()

        val newName ="New Test Name"
        dialogAction(hasTestTag("${dd.cat1.name} Edit"), newName, "AlertDialog confirm")

        // check for updated name then check all
        composeRule.onNodeWithText(newName).assertExists()
        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()
        dialogAction(hasTestTag("${dd.cat3.name} Edit"), newName, "AlertDialog confirm")

        // check for updated name then check all
        composeRule.onNodeWithText(newName).assertExists()
        checkCategoriesByTypeAndDeleteState(INCOME)
    }

    @Test
    fun category_editCategoryExists() {
        navigateToCategoryScreen()

        dialogAction(hasTestTag("${dd.cat1.name} Edit"), dd.cat2.name, "AlertDialog confirm")

        // check that snackbar with message appears
        composeRule.onNodeWithText(res.getString(R.string.snackbar_exists, dd.cat2.name))
            .assertIsDisplayed()
        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()
        dialogAction(hasTestTag("${dd.cat3.name} Edit"), dd.cat4.name, "AlertDialog confirm")

        // check that snackbar with message appears
        composeRule.onNodeWithText(res.getString(R.string.snackbar_exists, dd.cat4.name))
            .assertIsDisplayed()
        checkCategoriesByTypeAndDeleteState(INCOME)
    }

    @Test
    fun category_editCategoryDismiss() {
        navigateToCategoryScreen()

        dialogAction(hasTestTag("${dd.cat1.name} Edit"), dd.cat2.name, "AlertDialog dismiss")

        // check that Category exists unedited then check all
        composeRule.onNodeWithText(dd.cat1.name).assertExists()
        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()
        dialogAction(hasTestTag("${dd.cat3.name} Edit"), dd.cat4.name, "AlertDialog dismiss")

        // check that Category exists unedited then check all
        composeRule.onNodeWithText(dd.cat3.name).assertExists()
        checkCategoriesByTypeAndDeleteState(INCOME)
    }

    @Test
    fun category_deleteCategory() {
        navigateToCategoryScreen()

        dialogAction(hasTestTag("${dd.cat5.name} Delete"), "", "AlertDialog confirm")

        // check that Category is deleted then check all
        composeRule.onNodeWithText(dd.cat5.name).assertDoesNotExist()
        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()
        dialogAction(hasTestTag("${dd.cat6.name} Delete"), "", "AlertDialog confirm")

        // check that Category is deleted then check all
        composeRule.onNodeWithText(dd.cat6.name).assertDoesNotExist()
        checkCategoriesByTypeAndDeleteState(INCOME)
    }

    @Test
    fun category_deleteCategoryDismiss() {
        navigateToCategoryScreen()

        dialogAction(hasTestTag("${dd.cat5.name} Delete"), "", "AlertDialog dismiss")

        // check that Category still exists then check all
        composeRule.onNodeWithText(dd.cat5.name).assertExists()
        checkCategoriesByTypeAndDeleteState(EXPENSE)

        navigateToIncomeCategories()
        dialogAction(hasTestTag("${dd.cat6.name} Delete"), "", "AlertDialog dismiss")

        // check that Category still exists then check all
        composeRule.onNodeWithText(dd.cat6.name).assertExists()
        checkCategoriesByTypeAndDeleteState(INCOME)
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

    private fun navigateToCategoryScreen() {
        // check that we start on Overview screen, open drawer, and navigate to Category screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_drawer_description)))
            .performClick()
        composeRule.onNode(hasTestTag("DrawerItem Categories")).performClick()

        // check that we navigate to Category screen and start on Expense screen
        composeRule.onNode(hasTestTag("AppBar Categories")).assertExists()
        composeRule.onNode(hasTestTag("List Subtitle Expense")).assertIsDisplayed()
    }

    /**
     *  Check that all Categories of [type] in Repo are being displayed with correct delete button state
     */
    private fun checkCategoriesByTypeAndDeleteState(type: TransactionType) {
        val viewModel =  composeRule.activity.viewModels<CategoryViewModel>().value
        val catList: List<Category>
        val catUsedList: List<Category>
        if (type == EXPENSE) {
            catList = viewModel.expenseCatList.value
            catUsedList = viewModel.expenseCatUsedList.value
        } else {
            catList = viewModel.incomeCatList.value
            catUsedList = viewModel.incomeCatUsedList.value
        }
        catList.forEach {
            composeRule.onNodeWithText(it.name).assertExists()
            if (catUsedList.contains(it)) {
                composeRule.onNode(hasTestTag("${it.name} Delete")).assertIsNotEnabled()
            } else {
                composeRule.onNode(hasTestTag("${it.name} Delete")).assertIsEnabled()
            }
        }
    }

    /**
     *  Swipes left on ViewPager and checks if Income Categories are displayed
     */
    private fun navigateToIncomeCategories() {
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("List Subtitle Income")).assertIsDisplayed()
    }
}