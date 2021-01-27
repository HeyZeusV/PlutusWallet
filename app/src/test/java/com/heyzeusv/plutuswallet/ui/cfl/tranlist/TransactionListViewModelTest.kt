package com.heyzeusv.plutuswallet.ui.cfl.tranlist

import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class TransactionListViewModelTest {

    // test Fake
    private lateinit var repo: FakeRepository

    // What is being tested
    private lateinit var tlVM: TransactionListViewModel

    // dummy data
    private lateinit var dd: DummyDataUtil

    @BeforeEach
    fun setUpViewModel() {

        // some function add/remove data, so want same data at start of every test.
        dd = DummyDataUtil()

        // initialize fake repo with dummy data and pass it to ViewModel
        repo = FakeRepository(dd.accList, dd.catList, dd.tranList)
        tlVM = TransactionListViewModel(repo)
    }

    @Test
    @DisplayName("Should create openTran Event containing id of Transaction selected")
    fun openTranOC() {

        tlVM.openTranOC(100)
        val openTranEvent: Event<Int> = tlVM.openTranEvent.value!!

        assertEquals(100, openTranEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should create deleteTran Event containing ItemViewTransaction to be deleted")
    fun deleteTranOC() {

        val expectedIVT = ItemViewTransaction(
            0, "", Date(), BigDecimal("0"), "", "", ""
        )

        tlVM.deleteTranOC(expectedIVT)
        val deleteTranEvent: Event<ItemViewTransaction> = tlVM.deleteTranEvent.value!!

        assertEquals(expectedIVT, deleteTranEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should take given ItemViewTransaction and delete Transaction associated with it from Database")
    fun deleteTranPosFun() {

        // deletes using ID so only need correct ID to test deletion
        val deletedIVT = ItemViewTransaction(
            1, "", Date(), BigDecimal("0"), "", "", ""
        )

        runBlockingTest {
            tlVM.deleteTranPosFun(deletedIVT)
        }

        assert(!repo.tranList.contains(dd.tran1))
    }

    @Test
    @DisplayName("Should predetermined Categories and Account if Database is empty")
    fun initializeTables() {

        val expense = "Expense"
        val income = "Income"

        repo.catList.clear()
        repo.accList.clear()
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

        tlVM.initializeTables()

        assertEquals(initialCategories, repo.catList)
        assertEquals(mutableListOf(none), repo.accList)
    }

    @Test
    @DisplayName("Returns LiveData containing list of ItemViewTransactions depending on filters applied")
    fun filteredTransactionList() {

        val expectedATD: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(1, "Party", Date(86400000), BigDecimal("1000.10"),
                "Cash", "Expense", "Food"),
            ItemViewTransaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"),
                "Cash", "Expense", "Food")
        )
        assertEquals(expectedATD,
            tlVM.filteredTransactionList(
                account = true, category = true, date = true, "Expense",
                "Cash", "All", Date(0), Date(86400001 * 2)
            ).value
        )

        val expectedATCD: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"),
                "Debit Card", "Income", "Salary")
        )
        assertEquals(expectedATCD,
            tlVM.filteredTransactionList(
                account = true, category = true, date = true, "Income",
                "Debit Card", "Salary", Date(0), Date(86400001 * 6)
            ).value
        )

        val expectedAT: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"),
            "Credit Card", "Expense", "Entertainment")
        )
        assertEquals(expectedAT,
            tlVM.filteredTransactionList(
                account = true, category = true, date = false, "Expense",
                "Credit Card", "All", Date(), Date()
            ).value
        )

        val expectedATC: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"),
                "Credit Card", "Expense", "Entertainment")
        )
        assertEquals(expectedATC,
            tlVM.filteredTransactionList(
                account = true, category = true, date = false, "Expense",
                "Credit Card", "Entertainment", Date(0), Date()
            ).value
        )

        val expectedAD: List<ItemViewTransaction> = listOf()
        assertEquals(expectedAD,
            tlVM.filteredTransactionList(
                account = true, category = false, date = true, "",
                "None", "", Date(0), Date(86400001 * 6)
            ).value
        )

        val expectedA: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"),
                "Credit Card", "Expense", "Entertainment")
        )
        assertEquals(expectedA,
            tlVM.filteredTransactionList(
                account = true, category = false, date = false, "",
                "Credit Card", "", Date(), Date()
            ).value
        )

        val expectedTD: List<ItemViewTransaction> = listOf()
        assertEquals(expectedTD,
            tlVM.filteredTransactionList(
                account = false, category = true, date = true, "Income",
                "", "All", Date(86400001 * 15), Date()
            ).value
        )

        val expectedTCD: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"),
                "Cash", "Expense", "Food")
        )
        assertEquals(expectedTCD,
            tlVM.filteredTransactionList(
                account = false, category = true, date = true, "Expense",
                "", "Food", Date(86400000 * 2), Date()
            ).value
        )

        val expectedT: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(1, "Party", Date(86400000), BigDecimal("1000.10"),
            "Cash", "Expense", "Food"),
            ItemViewTransaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"),
                "Cash", "Expense", "Food"),
            ItemViewTransaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"),
                "Credit Card", "Expense", "Entertainment")
        )
        assertEquals(expectedT,
            tlVM.filteredTransactionList(
                account = false, category = true, date = false, "Expense",
                "", "All", Date(), Date()
            ).value
        )

        val expectedTC: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"),
                "Credit Card", "Expense", "Entertainment")
        )
        assertEquals(expectedTC,
            tlVM.filteredTransactionList(
                account = false, category = true, date = false, "Expense",
                "", "Entertainment", Date(), Date()
            ).value
        )

        val expectedD: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"),
                "Cash", "Expense", "Food"),
            ItemViewTransaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"),
                "Debit Card", "Income", "Salary")
        )
        assertEquals(expectedD,
            tlVM.filteredTransactionList(
                account = false, category = false, date = true, "",
                "", "", Date(86400000 * 2), Date(86400000 * 4)
            ).value
        )

        val expected: List<ItemViewTransaction> = listOf(
            ItemViewTransaction(1, "Party", Date(86400000), BigDecimal("1000.10"),
                "Cash", "Expense", "Food"),
            ItemViewTransaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"),
                "Cash", "Expense", "Food"),
            ItemViewTransaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"),
                "Debit Card", "Income", "Salary"),
            ItemViewTransaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"),
                "Credit Card", "Expense", "Entertainment")
        )
        assertEquals(expected,
            tlVM.filteredTransactionList(
                account = false, category = false, date = false, "",
                "", "", Date(), Date()
            ).value
        )
    }
}