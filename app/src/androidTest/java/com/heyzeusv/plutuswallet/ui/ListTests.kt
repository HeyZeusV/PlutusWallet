package com.heyzeusv.plutuswallet.ui

import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.assertDisplayedMessage
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.ListDialog
import com.heyzeusv.plutuswallet.data.model.ListItemInterface
import com.heyzeusv.plutuswallet.ui.list.ListCard
import com.heyzeusv.plutuswallet.util.ListItemAction.CREATE
import com.heyzeusv.plutuswallet.util.ListItemAction.EDIT
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.replace
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagerApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ListTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeRule = createAndroidComposeRule<ComponentActivity>()

    val dd = DummyAndroidDataUtil()
    val usedList = listOf(dd.acc2, dd.acc5)
    val expenseCats = dd.catList.filter { it.type == EXPENSE.type }
    val incomeCats = dd.catList.filter { it.type == INCOME.type }
    val usedListExpense = listOf(dd.cat1, dd.cat2)
    val usedListIncome = listOf(dd.cat3, dd.cat4)

    lateinit var res: Resources

    @Before
    fun setUp() {
        res = InstrumentationRegistry.getInstrumentation().targetContext.resources
    }

    @Test
    fun list_oneList_display() {
        composeRule.setContent {
            PlutusWalletTheme {
                ListCard(
                    dataLists = listOf(dd.accList),
                    usedDataLists = listOf(usedList)
                )
            }
        }

        checkItemsExistsAndDeleteState(dd.accList, usedList)
    }

    @Test
    fun list_twoLists_display() {
        composeRule.setContent {
            PlutusWalletTheme {
                ListCard(
                    dataLists = listOf(expenseCats, incomeCats),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    listSubtitles = listOf(R.string.type_expense, R.string.type_income)
                )
            }
        }

        // check correct subtitle is displayed
        composeRule.onNode(hasTestTag("List Subtitle Expense")).assertIsDisplayed()
        checkItemsExistsAndDeleteState(expenseCats, usedListExpense)

        // navigate to second page and check correct subtitle is displayed
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }
        composeRule.onNode(hasTestTag("List Subtitle Income")).assertIsDisplayed()

        checkItemsExistsAndDeleteState(incomeCats, usedListIncome)
    }

    @Test
    fun list_oneList_create() {
        composeRule.setContent {
            PlutusWalletTheme {
                var list by remember { mutableStateOf(dd.accList.toList()) }
                var showDialog by remember { mutableStateOf(ListDialog(CREATE, 0)) }
                ListCard(
                    dataLists = listOf(list),
                    usedDataLists = listOf(usedList),
                    showDialog = showDialog,
                    createDialogTitle = "Create",
                    createDialogOnConfirm = {
                        val newItem = Account(0, it)
                        list = list + newItem
                        showDialog = ListDialog(EDIT, -1)
                    }
                )
            }
        }

        val testItem = "Test Account"
        val expectedItem = Account(0, testItem)
        dialogAction(null, testItem, "AlertDialog confirm")

        checkItemsExistsAndDeleteState(dd.accList + expectedItem, usedList)
    }

    @Test
    fun list_twoLists_create() {
        composeRule.setContent {
            PlutusWalletTheme {
                val pagerState = rememberPagerState()
                var pagerChange by remember { mutableStateOf(true) }
                var expenseList by remember { mutableStateOf(expenseCats) }
                var incomeList by remember { mutableStateOf(incomeCats) }
                var showDialog by remember { mutableStateOf(ListDialog(CREATE, 0, EXPENSE)) }

                if (pagerState.currentPage == 1 && pagerChange) {
                    showDialog = ListDialog(CREATE, 0, INCOME)
                    // do not remove, shows it isn't used, but it stops AlertDialog from staying
                    // open due to multiple calls of state change
                    pagerChange = false
                }
                ListCard(
                    pagerState = pagerState,
                    dataLists = listOf(expenseList, incomeList),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    showDialog = showDialog,
                    createDialogTitle = "Create",
                    createDialogOnConfirm = {
                        val newItem: Category
                        when (pagerState.currentPage) {
                            0 -> {
                                newItem = Category(0, it, EXPENSE.type)
                                expenseList = expenseList + newItem
                            }
                            else -> {
                                newItem = Category(0, it, INCOME.type)
                                incomeList = incomeList + newItem
                            }
                        }
                        showDialog = ListDialog(EDIT, -1)
                    }
                )
            }
        }

        val testExpenseItem = "Test Expense Category"
        val expectedExpenseItem = Category(0, testExpenseItem, EXPENSE.type)
        dialogAction(null, testExpenseItem, "AlertDialog confirm")

        checkItemsExistsAndDeleteState(expenseCats + expectedExpenseItem, usedListExpense)

        // navigate to second page
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }

        val testIncomeItem = "Test Income Category"
        val expectedIncomeItem = Category(0, testIncomeItem, INCOME.type)
        dialogAction(null, testIncomeItem, "AlertDialog confirm")

        checkItemsExistsAndDeleteState(incomeCats + expectedIncomeItem, usedListIncome)
    }

    @Test
    fun list_oneList_createExists() {
        val snackbarHostState = SnackbarHostState()
        composeRule.setContent {
            PlutusWalletTheme {
                var showDialog by remember { mutableStateOf(ListDialog(CREATE, 0)) }
                var itemExists by remember { mutableStateOf("") }
                ListCard(
                    dataLists = listOf(dd.accList),
                    usedDataLists = listOf(usedList),
                    showDialog = showDialog,
                    createDialogTitle = "Create",
                    createDialogOnConfirm = { name ->
                        if (dd.accList.find { it.name == name } != null) {
                            itemExists = name
                        }
                        showDialog = ListDialog(EDIT, -1)
                    },
                    itemExists = itemExists,
                    showSnackbar = { msg ->
                        snackbarHostState.showSnackbar(msg)
                        itemExists = ""
                    }
                )
            }
        }
        dialogAction(null, dd.acc1.name, "AlertDialog confirm")

        // check no item repeats and Snackbar message
        checkItemsExistsAndDeleteState(dd.accList, usedList)
        snackbarHostState.assertDisplayedMessage(R.string.snackbar_exists, dd.acc1.name)
    }

    @Test
    fun list_twoLists_createExists() {
        // extension function only collects first value, so will use individual states for each
        // page, this is handled by on SnackbarHostState in prod
        val expenseSnackbarHost = SnackbarHostState()
        val incomeSnackbarHost = SnackbarHostState()

        composeRule.setContent {
            PlutusWalletTheme {
                val pagerState = rememberPagerState()
                var pagerChange by remember { mutableStateOf(true) }
                var showDialog by remember { mutableStateOf(ListDialog(CREATE, 0, EXPENSE)) }
                var itemExists by remember { mutableStateOf("") }

                if (pagerState.currentPage == 1 && pagerChange) {
                    showDialog = ListDialog(CREATE, 0, INCOME)
                    // do not remove, shows it isn't used, but it stops AlertDialog from staying
                    // open due to multiple calls of state change
                    pagerChange = false
                }
                ListCard(
                    pagerState = pagerState,
                    dataLists = listOf(expenseCats, incomeCats),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    showDialog = showDialog,
                    createDialogTitle = "Create",
                    createDialogOnConfirm = { name ->
                        when (pagerState.currentPage) {
                            0 -> if (expenseCats.find { it.name == name } != null) itemExists = name
                            else ->
                                if (incomeCats.find { it.name == name } != null) itemExists = name
                        }
                        showDialog = ListDialog(EDIT, -1)
                    },
                    itemExists = itemExists,
                    showSnackbar = { msg ->
                        when (pagerState.currentPage) {
                            0 -> expenseSnackbarHost.showSnackbar(msg)
                            else -> incomeSnackbarHost.showSnackbar(msg)
                        }
                        itemExists = ""
                    }
                )
            }
        }

        dialogAction(null, dd.cat1.name, "AlertDialog confirm")

        // check no item repeats and Snackbar message
        checkItemsExistsAndDeleteState(expenseCats, usedListExpense)
        expenseSnackbarHost.assertDisplayedMessage(R.string.snackbar_exists, dd.cat1.name)

        // navigate to second page
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }

        dialogAction(null, dd.cat3.name, "AlertDialog confirm")

        // check no item repeats and Snackbar message
        checkItemsExistsAndDeleteState(incomeCats, usedListIncome)
        incomeSnackbarHost.assertDisplayedMessage(R.string.snackbar_exists, dd.cat3.name)
    }

    @Test
    fun list_oneList_createDismiss() {
        composeRule.setContent {
            PlutusWalletTheme {
                var showDialog by remember { mutableStateOf(ListDialog(CREATE, 0)) }
                ListCard(
                    dataLists = listOf(dd.accList),
                    usedDataLists = listOf(usedList),
                    showDialog = showDialog,
                    createDialogTitle = "Create",
                    dialogOnDismiss = { showDialog = ListDialog(EDIT, -1) }
                )
            }
        }

        val testItem = "Test Account"
        dialogAction(null, testItem, "AlertDialog dismiss")

        // check that new item was not created then check all
        composeRule.onNodeWithText(testItem).assertDoesNotExist()
        checkItemsExistsAndDeleteState(dd.accList, usedList)
    }

    @Test
    fun list_twoLists_createDismiss() {
        composeRule.setContent {
            PlutusWalletTheme {
                val pagerState = rememberPagerState()
                var pagerChange by remember { mutableStateOf(true) }
                var showDialog by remember { mutableStateOf(ListDialog(CREATE, 0, EXPENSE)) }

                if (pagerState.currentPage == 1 && pagerChange) {
                    showDialog = ListDialog(CREATE, 0, INCOME)
                    // do not remove, shows it isn't used, but it stops AlertDialog from staying
                    // open due to multiple calls of state change
                    pagerChange = false
                }
                ListCard(
                    pagerState = pagerState,
                    dataLists = listOf(expenseCats, incomeCats),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    showDialog = showDialog,
                    createDialogTitle = "Create",
                    dialogOnDismiss = { showDialog = ListDialog(EDIT, -1) }
                )
            }
        }

        val testExpenseItem = "Test Expense Category"
        dialogAction(null, testExpenseItem, "AlertDialog dismiss")

        // check that new item was not created then check all
        composeRule.onNodeWithText(testExpenseItem).assertDoesNotExist()
        checkItemsExistsAndDeleteState(expenseCats, usedListExpense)

        // navigate to second page
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }

        val testIncomeItem = "Test Income Category"
        dialogAction(null, testIncomeItem, "AlertDialog dismiss")

        // check that new item was not created then check all
        composeRule.onNodeWithText(testIncomeItem).assertDoesNotExist()
        checkItemsExistsAndDeleteState(incomeCats, usedListIncome)
    }

    @Test
    fun list_oneList_edit() {
        composeRule.setContent {
            PlutusWalletTheme {
                var list by remember { mutableStateOf(dd.accList.toList()) }
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, -1)) }
                ListCard(
                    dataLists = listOf(list),
                    usedDataLists = listOf(usedList),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    editDialogTitle = "Edit",
                    editDialogOnConfirm = { item, name ->
                        val editableList = list.toMutableList()
                        editableList.replace(item as Account, Account(item.id, name))
                        list = editableList
                        showDialog = ListDialog(EDIT, -1)
                    }
                )
            }
        }

        val updatedName = "New Test Name"
        dialogAction(hasTestTag("${dd.acc5.name} Edit"), updatedName, "AlertDialog confirm")

        // check for updated name
        composeRule.onNodeWithText(updatedName).assertIsDisplayed()
    }

    @Test
    fun list_twoLists_edit() {
        composeRule.setContent {
            PlutusWalletTheme {
                val pagerState = rememberPagerState()
                var expenseList by remember { mutableStateOf(expenseCats) }
                var incomeList by remember { mutableStateOf(incomeCats) }
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, 0, EXPENSE)) }

                ListCard(
                    pagerState = pagerState,
                    dataLists = listOf(expenseList, incomeList),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    editDialogTitle = "Edit",
                    editDialogOnConfirm = { item, name ->
                        when (pagerState.currentPage) {
                            0 -> {
                                val editableList = expenseList.toMutableList()
                                editableList.replace(
                                    item as Category, Category(item.id, name, EXPENSE.type)
                                )
                                expenseList = editableList
                            }
                            else -> {
                                val editableList = incomeList.toMutableList()
                                editableList.replace(
                                    item as Category, Category(item.id, name, INCOME.type)
                                )
                                incomeList = editableList
                            }
                        }
                        showDialog = ListDialog(EDIT, -1)
                    }
                )
            }
        }

        val updatedName = "New Test Name"
        dialogAction(hasTestTag("${dd.cat1.name} Edit"), updatedName, "AlertDialog confirm")

        // check for updated name
        composeRule.onNodeWithText(updatedName).assertIsDisplayed()

        // navigate to second page
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }

        dialogAction(hasTestTag("${dd.cat3.name} Edit"), updatedName, "AlertDialog confirm")

        // check for updated name
        composeRule.onNodeWithText(updatedName).assertIsDisplayed()
    }

    @Test
    fun list_oneList_editExists() {
        val snackbarHostState = SnackbarHostState()
        composeRule.setContent {
            PlutusWalletTheme {
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, -1)) }
                var itemExists by remember { mutableStateOf("") }
                ListCard(
                    dataLists = listOf(dd.accList),
                    usedDataLists = listOf(usedList),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    editDialogTitle = "Edit",
                    editDialogOnConfirm = { _, name ->
                        if (dd.accList.find { it.name == name } != null) {
                            itemExists = name
                        }
                        showDialog = ListDialog(EDIT, -1)
                    },
                    itemExists = itemExists,
                    showSnackbar = { msg ->
                        snackbarHostState.showSnackbar(msg)
                        itemExists = ""
                    }
                )
            }
        }

        dialogAction(hasTestTag("${dd.acc5.name} Edit"), dd.acc1.name, "AlertDialog confirm")

        // check no item repeats and Snackbar message
        checkItemsExistsAndDeleteState(dd.accList, usedList)
        snackbarHostState.assertDisplayedMessage(R.string.snackbar_exists, dd.acc1.name)
    }

    @Test
    fun list_twoLists_editExists() {
        // extension function only collects first value, so will use individual states for each
        // page, this is handled by on SnackbarHostState in prod
        val expenseSnackbarHost = SnackbarHostState()
        val incomeSnackbarHost = SnackbarHostState()

        composeRule.setContent {
            PlutusWalletTheme {
                val pagerState = rememberPagerState()
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, 0, EXPENSE)) }
                var itemExists by remember { mutableStateOf("") }

                ListCard(
                    pagerState = pagerState,
                    dataLists = listOf(expenseCats, incomeCats),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    editDialogTitle = "Edit",
                    editDialogOnConfirm = { _, name ->
                        when (pagerState.currentPage) {
                            0 -> if (expenseCats.find { it.name == name } != null) itemExists = name
                            else ->
                                if (incomeCats.find { it.name == name } != null) itemExists = name
                        }
                        showDialog = ListDialog(EDIT, -1)
                    },
                    itemExists = itemExists,
                    showSnackbar = { msg ->
                        when (pagerState.currentPage) {
                            0 -> expenseSnackbarHost.showSnackbar(msg)
                            else -> incomeSnackbarHost.showSnackbar(msg)
                        }
                        itemExists = ""
                    }
                )
            }
        }

        dialogAction(hasTestTag("${dd.cat1.name} Edit"), dd.cat2.name, "AlertDialog confirm")

        // check no item repeats and Snackbar message
        checkItemsExistsAndDeleteState(expenseCats, usedListExpense)
        expenseSnackbarHost.assertDisplayedMessage(R.string.snackbar_exists, dd.cat2.name)

        // navigate to second page
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }

        dialogAction(hasTestTag("${dd.cat3.name} Edit"), dd.cat4.name, "AlertDialog confirm")

        // check no item repeats and Snackbar message
        checkItemsExistsAndDeleteState(incomeCats, usedListIncome)
        incomeSnackbarHost.assertDisplayedMessage(R.string.snackbar_exists, dd.cat4.name)
    }

    @Test
    fun list_oneList_editDismiss() {
        composeRule.setContent {
            PlutusWalletTheme {
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, -1)) }
                ListCard(
                    dataLists = listOf(dd.accList),
                    usedDataLists = listOf(usedList),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    editDialogTitle = "Edit",
                    dialogOnDismiss = { showDialog = ListDialog(EDIT, -1) }
                )
            }
        }

        val testItem = "Test Account"
        dialogAction(hasTestTag("${dd.acc5.name} Edit"), testItem, "AlertDialog dismiss")

        // check that item was not edited then check all
        composeRule.onNodeWithText(testItem).assertDoesNotExist()
        checkItemsExistsAndDeleteState(dd.accList, usedList)
    }

    @Test
    fun list_twoLists_editDismiss() {
        composeRule.setContent {
            PlutusWalletTheme {
                val pagerState = rememberPagerState()
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, 0, EXPENSE)) }

                ListCard(
                    pagerState = pagerState,
                    dataLists = listOf(expenseCats, incomeCats),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    editDialogTitle = "Edit",
                    dialogOnDismiss = { showDialog = ListDialog(EDIT, -1) }
                )
            }
        }

        val testItem = "Test Expense Category"
        dialogAction(hasTestTag("${dd.cat1.name} Edit"), testItem, "AlertDialog dismiss")

        // check that new item was not edited then check all
        composeRule.onNodeWithText(testItem).assertDoesNotExist()
        checkItemsExistsAndDeleteState(expenseCats, usedListExpense)

        // navigate to second page
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }

        dialogAction(hasTestTag("${dd.cat3.name} Edit"), testItem, "AlertDialog dismiss")

        // check that new item was not edited then check all
        composeRule.onNodeWithText(testItem).assertDoesNotExist()
        checkItemsExistsAndDeleteState(incomeCats, usedListIncome)
    }

    @Test
    fun list_oneList_delete() {
        composeRule.setContent {
            PlutusWalletTheme {
                var list by remember { mutableStateOf(dd.accList.toList()) }
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, -1)) }
                ListCard(
                    dataLists = listOf(list),
                    usedDataLists = listOf(usedList),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    deleteDialogTitle = "Delete",
                    deleteDialogOnConfirm = {
                        list = list - (it as Account)
                        showDialog = ListDialog(EDIT, -1)
                    }
                )
            }
        }

        dialogAction(hasTestTag("${dd.acc4.name} Delete"), "", "AlertDialog confirm")

        // check item was deleted
        composeRule.onNodeWithText(dd.acc4.name).assertDoesNotExist()
    }

    @Test
    fun list_twoLists_delete() {
        composeRule.setContent {
            PlutusWalletTheme {
                val pagerState = rememberPagerState()
                var expenseList by remember { mutableStateOf(expenseCats) }
                var incomeList by remember { mutableStateOf(incomeCats) }
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, 0, EXPENSE)) }

                ListCard(
                    pagerState = pagerState,
                    dataLists = listOf(expenseList, incomeList),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    deleteDialogTitle = "Delete",
                    deleteDialogOnConfirm = {
                        when (pagerState.currentPage) {
                            0 -> expenseList = expenseList - (it as Category)
                            else -> incomeList = incomeList - (it as Category)
                        }
                        showDialog = ListDialog(EDIT, -1)
                    }
                )
            }
        }

        dialogAction(hasTestTag("${dd.cat5.name} Delete"), "", "AlertDialog confirm")

        // check item was deleted
        composeRule.onNodeWithText(dd.cat5.name).assertDoesNotExist()

        // navigate to second page
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }

        dialogAction(hasTestTag("${dd.cat6.name} Delete"), "", "AlertDialog confirm")

        // check item was deleted
        composeRule.onNodeWithText(dd.cat6.name).assertDoesNotExist()
    }

    @Test
    fun list_oneList_deleteDismiss() {
        composeRule.setContent {
            PlutusWalletTheme {
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, -1)) }
                ListCard(
                    dataLists = listOf(dd.accList),
                    usedDataLists = listOf(usedList),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    deleteDialogTitle = "Delete",
                    dialogOnDismiss = { showDialog = ListDialog(EDIT, -1) }
                )
            }
        }

        dialogAction(hasTestTag("${dd.acc4.name} Delete"), "", "AlertDialog dismiss")

        // check that item was not deleted then check all
        composeRule.onNodeWithText(dd.acc4.name).assertIsDisplayed()
        checkItemsExistsAndDeleteState(dd.accList, usedList)
    }

    @Test
    fun list_twoLists_deleteDismiss() {
        composeRule.setContent {
            PlutusWalletTheme {
                val pagerState = rememberPagerState()
                var showDialog by remember { mutableStateOf(ListDialog(EDIT, 0, EXPENSE)) }

                ListCard(
                    pagerState = pagerState,
                    dataLists = listOf(expenseCats, incomeCats),
                    usedDataLists = listOf(usedListExpense, usedListIncome),
                    onClick = { showDialog = it },
                    showDialog = showDialog,
                    deleteDialogTitle = "Delete",
                    dialogOnDismiss = { showDialog = ListDialog(EDIT, -1) }
                )
            }
        }

        dialogAction(hasTestTag("${dd.cat5.name} Delete"), "", "AlertDialog dismiss")

        // check that new item was not deleted then check all
        composeRule.onNodeWithText(dd.cat5.name).assertIsDisplayed()
        checkItemsExistsAndDeleteState(expenseCats, usedListExpense)

        // navigate to second page
        composeRule.onNode(hasTestTag("List ViewPager")).performTouchInput { swipeLeft() }

        dialogAction(hasTestTag("${dd.cat6.name} Delete"), "", "AlertDialog dismiss")

        // check that new item was not deleted then check all
        composeRule.onNodeWithText(dd.cat6.name).assertIsDisplayed()
        checkItemsExistsAndDeleteState(incomeCats, usedListIncome)
    }

    /**
     *  Clicks on [node] then types in [name] into input field if it is not empty and
     *  performs [action].
     */
    private fun dialogAction(node: SemanticsMatcher?, name: String, action: String) {
        node?.let {
            composeRule.onNode(it).performClick()
        }
        composeRule.onNode(hasTestTag("AlertDialog")).assertIsDisplayed()
        if (name.isNotBlank()) {
            composeRule.onNode(hasTestTag("AlertDialog input")).performTextInput(name)
        }
        composeRule.onNode(
            hasTestTag(action),
            useUnmergedTree = true
        ).performClick()
    }

    /**
     *  Check that all Accounts in Repo are being displayed with correct delete button state
     */
    private fun checkItemsExistsAndDeleteState(itemList: List<ListItemInterface>, usedList: List<ListItemInterface>) {
        itemList.forEach {
            composeRule.onNodeWithText(it.name).assertExists()
            if (usedList.contains(it)) {
                composeRule.onNode(hasTestTag("${it.name} Delete")).assertIsNotEnabled()
            } else {
                composeRule.onNode(hasTestTag("${it.name} Delete")).assertIsEnabled()
            }
        }
    }
}