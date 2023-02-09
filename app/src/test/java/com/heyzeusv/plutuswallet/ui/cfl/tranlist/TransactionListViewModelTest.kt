package com.heyzeusv.plutuswallet.ui.cfl.tranlist

import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.overview.TransactionListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date
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
            date = Date(System.currentTimeMillis() - 259200000),
            repeating = true,
            futureDate = Date(System.currentTimeMillis() - 172800000)
        )
        val repeatingTransaction2nd = Transaction(
            title = "Title x2",
            date = Date(System.currentTimeMillis() - 172800000),
            repeating = true,
            futureDate = Date(System.currentTimeMillis() - 86400000)
        )
        val repeatingTransaction3rd = Transaction(
            title = "Title x3",
            date = Date(System.currentTimeMillis() - 86400000),
            repeating = true,
            futureDate = Date(System.currentTimeMillis())
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

        val expectedATD: List<TranListItem> = listOf(dd.ivt1, dd.ivt2)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, category = true, date = true, "Expense",
                listOf("Cash"), listOf("All"), Date(0), Date(86400001 * 2)
            )
        ).collect { collectedList = it }
        assertEquals(expectedATD, collectedList)

        val expectedATCD: List<TranListItem> = listOf(dd.ivt3)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, category = true, date = true, "Income",
                listOf("Debit Card"), listOf("Salary"), Date(0), Date(86400001 * 6)
            )
        ).collect { collectedList = it }
        assertEquals(expectedATCD, collectedList)

        val expectedAT: List<TranListItem> = listOf(dd.ivt4)
        tlVM.filteredTransactionList(
            FilterInfo(
                 account = true, category = true, date = false, "Expense",
                listOf("Credit Card"), listOf("All"), Date(), Date()
            )
        ).collect { collectedList = it }
        assertEquals(expectedAT, collectedList)

        val expectedATC: List<TranListItem> = listOf(dd.ivt4)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, category = true, date = false, "Expense",
                listOf("Credit Card"), listOf("Entertainment"), Date(0), Date()
            )
        ).collect { collectedList = it }
        assertEquals(expectedATC, collectedList)

        val expectedAD: List<TranListItem> = listOf()
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, category = false, date = true, "",
                listOf("None"), listOf(), Date(0), Date(86400001 * 6)
            )
        ).collect { collectedList = it }
        assertEquals(expectedAD, collectedList)

        val expectedA: List<TranListItem> = listOf(dd.ivt4)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = true, category = false, date = false, "",
                listOf("Credit Card"), listOf(), Date(), Date()
            )
        ).collect { collectedList = it }
        assertEquals(expectedA, collectedList)

        val expectedTD: List<TranListItem> = listOf()
        tlVM.filteredTransactionList(
            FilterInfo(
                account = false, category = true, date = true, "Income",
                listOf(), listOf("All"), Date(86400001 * 15), Date()
            )
        ).collect { collectedList = it }
        assertEquals(expectedTD, collectedList)

        val expectedTCD: List<TranListItem> = listOf(dd.ivt2)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = false, category = true, date = true, "Expense",
                listOf(), listOf("Food"), Date(86400000 * 2), Date()
            )
        ).collect { collectedList = it }
        assertEquals(expectedTCD, collectedList)

        val expectedT: List<TranListItem> = listOf(dd.ivt1, dd.ivt2, dd.ivt4)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = false, category = true, date = false, "Expense",
                listOf(), listOf("All"), Date(), Date()
            )
        ).collect { collectedList = it }
        assertEquals(expectedT, collectedList)

        val expectedTC: List<TranListItem> = listOf(dd.ivt4)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = false, category = true, date = false, "Expense",
                listOf(), listOf("Entertainment"), Date(), Date()
            )
        ).collect { collectedList = it }
        assertEquals(expectedTC, collectedList)

        val expectedD: List<TranListItem> = listOf(dd.ivt2, dd.ivt3)
        tlVM.filteredTransactionList(
            FilterInfo(
                account = false, category = false, date = true, "",
                listOf(), listOf(), Date(86400000 * 2), Date(86400000 * 4)
            )
        ).collect { collectedList = it }
        assertEquals(expectedD, collectedList)

        val expected: List<TranListItem> = dd.ivtList
        tlVM.filteredTransactionList(
            FilterInfo(
                account = false, category = false, date = false, "",
                listOf(), listOf(), Date(), Date()
            )
        ).collect { collectedList = it }
        assertEquals(expected, collectedList)
    }
}