package com.heyzeusv.plutuswallet.ui

import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.overview.TransactionListViewModel
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class TransactionListViewModelTest {

    // test Fake
    private val repo = FakeRepository()

    // What is being tested
    private lateinit var tlVM: TransactionListViewModel

    // dummy data
    private val dd = DummyDataUtil()

    @BeforeEach
    fun setUpViewModel() {
        // reset fake repo with dummy data and pass it to ViewModel
        repo.clearAccCatLists()
        tlVM = TransactionListViewModel(repo)
    }

    @Test
    @DisplayName("Should correctly launch ViewModel with correct data required in its init block")
    fun viewModelInit() {
        assertEquals(4, tlVM.previousMaxId)
        assertEquals(dd.tlifList, tlVM.tranList.value)
        // initializeTables() has its own test
    }

    @Test
    @DisplayName("Should take given id and delete Transaction associated with it from Database")
    fun deleteTranPosFun() = runTest {
        tlVM.deleteTransaction(dd.tran1.id)

        assert(!repo.tranList.contains(dd.tran1))
    }

    @Test
    @DisplayName("Should create Transactions for repeating Transactions whose dates have" +
            "passed its futureDate")
    fun futureTransactions() = runTest {
        val expectedTranList = repo.tranList
        val repeatingTransaction = Transaction(
            title = "Title",
            date = ZonedDateTime.of(1980, 1, 9, 1, 0, 0, 0, systemDefault()),
            repeating = true,
            futureDate = ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault())
        )
        val repeatingTransaction2nd = Transaction(
            title = "Title x2",
            date = ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()),
            repeating = true,
            futureDate = ZonedDateTime.of(1980, 1, 11, 1, 0, 0, 0, systemDefault())
        )
        val repeatingTransaction3rd = Transaction(
            title = "Title x3",
            date = ZonedDateTime.of(1980, 1, 11, 1, 0, 0, 0, systemDefault()),
            repeating = true,
            futureDate = ZonedDateTime.of(1980, 1, 12, 1, 0, 0, 0, systemDefault())
        )
        expectedTranList.addAll(listOf(repeatingTransaction2nd, repeatingTransaction3rd))

        repo.tranList.add(repeatingTransaction)
        tlVM.futureTransactions()

        assertEquals(repo.tranList, expectedTranList)
    }

    @Test
    @DisplayName("Should create predetermined Categories and Account if Database is empty")
    fun initializeTables() {
        val expense = "Expense"
        val income = "Income"

        val education = Category(0, "Education", expense)
        val entertainment = Category(0, "Entertainment", expense)
        val food = Category(0, "Food", expense)
        val home = Category(0, "Home", expense)
        val transportation = Category(0, "Transportation", expense)
        val utilities = Category(0, "Utilities", expense)
        val cryptocurrency = Category(0, "Cryptocurrency", income)
        val investments = Category(0, "Investments", income)
        val salary = Category(0, "Salary", income)
        val savings = Category(0, "Savings", income)
        val stocks = Category(0, "Stocks", income)
        val wages = Category(0, "Wages", income)
        val initialCategories: List<Category> = listOf(
            education, entertainment, food, home, transportation, utilities,
            cryptocurrency, investments, salary, savings, stocks, wages
        )
        val none = Account(0, "None")

        assertEquals(initialCategories, repo.catList)
        assertEquals(mutableListOf(none), repo.accList)
    }

    @Test
    @DisplayName("Returns StateFlow containing list of ItemViewTransactions depending on filters applied")
    fun filteredTransactionList() = runTest {
        var collectedList = listOf<TranListItem>()

        val expectedATD: List<TranListItem> = listOf(dd.tli1, dd.tli2)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, listOf("Cash", "Savings"), category = true, "Expense",
                listOf("All"), date = true,
                ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()),
                ZonedDateTime.of(1980, 1, 12, 1, 0, 0, 0, systemDefault())

            )
        ).collect { collectedList = it }
        assertEquals(expectedATD, collectedList)

        val expectedATCD: List<TranListItem> = listOf(dd.tli3)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, listOf("Debit Card"), category = true, "Income",
                listOf("Salary"), date = true,
                ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()),
                ZonedDateTime.of(1980, 1, 14, 1, 0, 0, 0, systemDefault())
            )
        ).collect { collectedList = it }
        assertEquals(expectedATCD, collectedList)

        val expectedAT: List<TranListItem> = listOf(dd.tli4)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, listOf("Credit Card"), category = true, "Expense",
                listOf("All")
            )
        ).collect { collectedList = it }
        assertEquals(expectedAT, collectedList)

        val expectedATC: List<TranListItem> = listOf(dd.tli4)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, listOf("Credit Card"), category = true, "Expense",
                listOf("Entertainment")
            )
        ).collect { collectedList = it }
        assertEquals(expectedATC, collectedList)

        val expectedAD: List<TranListItem> = listOf()
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, listOf("None"), date = true,
                start = ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()),
                end = ZonedDateTime.of(1980, 1, 16, 1, 0, 0, 0, systemDefault())
            )
        ).collect { collectedList = it }
        assertEquals(expectedAD, collectedList)

        val expectedA: List<TranListItem> = listOf(dd.tli4)
        tlVM.filteredTransactionList(
            FilterInfo(account = true, listOf("Credit Card"))
        ).collect { collectedList = it }
        assertEquals(expectedA, collectedList)

        val expectedTD: List<TranListItem> = listOf()
        tlVM.filteredTransactionList(
            FilterInfo(
                category = true, type = "Income",
                categoryNames = listOf("All"), date = true,
                start = ZonedDateTime.of(1980, 1, 20, 1, 0, 0, 0, systemDefault()),
                end = ZonedDateTime.of(1990, 1, 12, 1, 0, 0, 0, systemDefault())
            )
        ).collect { collectedList = it }
        assertEquals(expectedTD, collectedList)

        val expectedTCD: List<TranListItem> = listOf(dd.tli2)
        tlVM.filteredTransactionList(
            FilterInfo(
                category = true, type = "Expense",
                categoryNames = listOf("Housing"), date = true,
                start = ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()),
                end = ZonedDateTime.of(1990, 1, 12, 1, 0, 0, 0, systemDefault())
            )
        ).collect { collectedList = it }
        assertEquals(expectedTCD, collectedList)

        val expectedT: List<TranListItem> = listOf(dd.tli1, dd.tli2, dd.tli4)
        tlVM.filteredTransactionList(
            FilterInfo(category = true, type = "Expense", categoryNames = listOf("All"))
        ).collect { collectedList = it }
        assertEquals(expectedT, collectedList)

        val expectedTC: List<TranListItem> = listOf(dd.tli4)
        tlVM.filteredTransactionList(
            FilterInfo(
                category = true, type = "Expense", categoryNames = listOf("Entertainment")
            )
        ).collect { collectedList = it }
        assertEquals(expectedTC, collectedList)

        val expectedD: List<TranListItem> = listOf(dd.tli2, dd.tli3)
        tlVM.filteredTransactionList(
            FilterInfo(
                date = true,
                start = ZonedDateTime.of(1980, 1, 11, 1, 0, 0, 0, systemDefault()),
                end = ZonedDateTime.of(1980, 1, 13, 1, 0, 0, 0, systemDefault())
            )
        ).collect { collectedList = it }
        assertEquals(expectedD, collectedList)

        val expected: List<TranListItem> = dd.tliList
        tlVM.filteredTransactionList(FilterInfo()).collect { collectedList = it }
        assertEquals(expected, collectedList)
    }
}