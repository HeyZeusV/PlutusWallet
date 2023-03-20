package com.heyzeusv.plutuswallet.ui

import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.ListDialog
import com.heyzeusv.plutuswallet.ui.list.CategoryViewModel
import com.heyzeusv.plutuswallet.util.ListItemAction.CREATE
import com.heyzeusv.plutuswallet.util.ListItemAction.EDIT
import com.heyzeusv.plutuswallet.util.ListItemAction.DELETE
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class CategoryViewModelTest {

    // test Fake
    private val repo = FakeRepository()

    // what is being tested
    private lateinit var catVM: CategoryViewModel

    // dummy data
    private val dd = DummyDataUtil()

    @BeforeEach
    fun setUpViewModel() = runTest {
        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        catVM = CategoryViewModel(repo)
        repo.expenseCatListEmit(dd.catList.filter { it.type == EXPENSE.type })
        repo.expenseCatUsedListEmit(
            dd.catList.filter { cat ->
                cat.type == EXPENSE.type && dd.tranList.any { it.category == cat.name }
            }.distinct()
        )
        repo.incomeCatListEmit(dd.catList.filter { it.type == INCOME.type })
        repo.incomeCatUsedListEmit(
            dd.catList.filter { cat ->
                cat.type == INCOME.type && dd.tranList.any { it.category == cat.name }
            }.distinct()
        )
    }

    @Test
    @DisplayName("Should have 4 lists at start up, 2 lists of all Expense/Income Categories " +
            "and 2 lists of Expense/Income Categories in use")
    fun viewModelInit() {
        val expectedExpenseList = listOf(dd.cat2, dd.cat1, dd.cat7, dd.cat5)
        val expectedExpenseUsedList = listOf(dd.cat1, dd.cat2, dd.cat7)
        val expectedIncomeList = listOf(dd.cat3, dd.cat6, dd.cat4)
        val expectedIncomeUsedList = listOf(dd.cat3)

        assertEquals(expectedExpenseList, catVM.firstItemList.value)
        assertEquals(expectedExpenseUsedList, catVM.firstUsedItemList.value)
        assertEquals(expectedIncomeList, catVM.secondItemList.value)
        assertEquals(expectedIncomeUsedList, catVM.secondUsedItemList.value)
    }


    @Test
    @DisplayName("Should delete Category from database and update showDialog")
    fun deleteCategory() = runTest {
        val expectedExpenseList = listOf(dd.cat2, dd.cat1, dd.cat7)
        val expectedIncomeList = listOf(dd.cat3, dd.cat4)

        catVM.updateDialog(ListDialog(DELETE, 5, EXPENSE))
        catVM.deleteItem(dd.cat5)
        catVM.updateDialog(ListDialog(DELETE, 6, INCOME))
        catVM.deleteItem(dd.cat6)

        assertEquals(expectedExpenseList, catVM.firstItemList.value)
        assertEquals(expectedIncomeList, catVM.secondItemList.value)
        assertEquals(ListDialog(DELETE, -1), catVM.showDialog.value)
    }

    @Test
    @DisplayName("Should edit Category with new name and update showDialog")
    fun editCategory() {
        val expenseCat = Category(1, "Test Expense", "Expense")
        val incomeCat = Category(6, "Test Income", "Income")
        val expectedExpenseList = listOf(dd.cat2, expenseCat, dd.cat7, dd.cat5)
        val expectedExpenseUsedList = listOf(expenseCat, dd.cat2, dd.cat7)
        val expectedIncomeList = listOf(dd.cat3, incomeCat, dd.cat4)
        val expectedIncomeUsedList = listOf(dd.cat3)

        catVM.updateDialog(ListDialog(EDIT, 1, EXPENSE))
        catVM.editItem(dd.cat1, expenseCat.name)
        catVM.updateDialog(ListDialog(EDIT, 6, INCOME))
        catVM.editItem(dd.cat6, incomeCat.name)

        assertEquals(expectedExpenseList, catVM.firstItemList.value)
        assertEquals(expectedExpenseUsedList, catVM.firstUsedItemList.value)
        assertEquals(expectedIncomeList, catVM.secondItemList.value)
        assertEquals(expectedIncomeUsedList, catVM.secondUsedItemList.value)
        assertEquals(ListDialog(EDIT, -1), catVM.showDialog.value)
    }

    @Test
    @DisplayName("Should edit Category with an existing name which updates accountExists")
    fun editCategoryExists() {
        catVM.editItem(dd.cat1, dd.cat5.name)

        assertEquals(dd.cat5.name, catVM.itemExists.value)

        catVM.editItem(dd.cat3, dd.cat6.name)

        assertEquals(dd.cat6.name, catVM.itemExists.value)
    }

    @Test
    @DisplayName("Should create a new unique Category")
    fun createNewCategory() {
        val newExpenseCat = Category(0, "Expense Test", "Expense")
        val newIncomeCat = Category(0, "Income Test", "Income")
        val expectedExpenseList = listOf(dd.cat2, newExpenseCat, dd.cat1, dd.cat7, dd.cat5)
        val expectedIncomeList = listOf(newIncomeCat, dd.cat3, dd.cat6, dd.cat4)

        catVM.updateDialog(ListDialog(CREATE, 0, EXPENSE))
        catVM.insertItem(newExpenseCat.name)
        catVM.updateDialog(ListDialog(CREATE, 0, INCOME))
        catVM.insertItem(newIncomeCat.name)

        assert(repo.catList.containsAll(listOf(newExpenseCat, newIncomeCat)))
        assertEquals(expectedExpenseList, catVM.firstItemList.value)
        assertEquals(expectedIncomeList, catVM.secondItemList.value)
        assertEquals(ListDialog(EDIT, -1, INCOME), catVM.showDialog.value)
    }

    @Test
    @DisplayName("Should insert a Category with an existing name which updates categoryExists")
    fun createNewCategoryExists() {
        catVM.updateDialog(ListDialog(CREATE, 0, EXPENSE))
        catVM.insertItem(dd.cat5.name)

        assertEquals(dd.cat5.name, catVM.itemExists.value)

        catVM.updateDialog(ListDialog(CREATE, 0, INCOME))
        catVM.insertItem(dd.cat6.name)

        assertEquals(dd.cat6.name, catVM.itemExists.value)
    }
}