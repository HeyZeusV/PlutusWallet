package com.heyzeusv.plutuswallet.ui.cfl.tranlist

import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
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
import java.time.ZoneId
import java.time.ZonedDateTime

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, TestCoroutineExtension::class)
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
        repo.resetLists()
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
            0, "", ZonedDateTime.now(ZoneId.systemDefault()),
            BigDecimal("0"), "", "", ""
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
            1, "", ZonedDateTime.now(ZoneId.systemDefault()),
            BigDecimal("0"), "", "", ""
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

        // ATD
        assertEquals(
            listOf(dd.ivt1, dd.ivt2),
            tlVM.filteredTransactionList(
                account = true, category = true, date = true, "Expense",
                listOf("Cash"), listOf("All"),
                ZonedDateTime.of(2018, 8, 9, 0, 0, 0, 0, ZoneId.systemDefault()),
                ZonedDateTime.of(2018, 8, 12, 0, 0, 0, 0, ZoneId.systemDefault())
            ).value
        )

        // ATCD
        assertEquals(
            listOf(dd.ivt3),
            tlVM.filteredTransactionList(
                account = true, category = true, date = true, "Income",
                listOf("Debit Card"), listOf("Salary"),
                ZonedDateTime.of(2018, 8, 5, 0, 0, 0, 0, ZoneId.systemDefault()),
                ZonedDateTime.of(2018, 8, 16, 0, 0, 0, 0, ZoneId.systemDefault())
            ).value
        )

        // AT
        assertEquals(
            listOf(dd.ivt4),
            tlVM.filteredTransactionList(
                account = true, category = true, date = false, "Expense",
                listOf("Credit Card"), listOf("All"),
                ZonedDateTime.now(ZoneId.systemDefault()),
                ZonedDateTime.now(ZoneId.systemDefault())
            ).value
        )

        // ATC
        assertEquals(
            listOf(dd.ivt4),
            tlVM.filteredTransactionList(
                account = true, category = true, date = false, "Expense",
                listOf("Credit Card"), listOf("Entertainment"),
                ZonedDateTime.now(ZoneId.systemDefault()),
                ZonedDateTime.now(ZoneId.systemDefault())
            ).value
        )

        // AD
        assertEquals(
            listOf<ItemViewTransaction>(),
            tlVM.filteredTransactionList(
                account = true, category = false, date = true, "",
                listOf("None"), listOf(),
                ZonedDateTime.of(2018, 8, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                ZonedDateTime.of(2018, 8, 25, 0, 0, 0, 0, ZoneId.systemDefault())
            ).value
        )

        // A
        assertEquals(
            listOf(dd.ivt4),
            tlVM.filteredTransactionList(
                account = true, category = false, date = false, "",
                listOf("Credit Card"), listOf(),
                ZonedDateTime.now(ZoneId.systemDefault()),
                ZonedDateTime.now(ZoneId.systemDefault())
            ).value
        )

        assertEquals(
            listOf<ItemViewTransaction>(),
            tlVM.filteredTransactionList(
                account = false, category = true, date = true, "Income",
                listOf(), listOf("All"),
                ZonedDateTime.of(2018, 8, 25, 0, 0, 0, 0, ZoneId.systemDefault()),
                ZonedDateTime.now(ZoneId.systemDefault())
            ).value
        )

        // TCD
        assertEquals(
            listOf(dd.ivt2),
            tlVM.filteredTransactionList(
                account = false, category = true, date = true, "Expense",
                listOf(), listOf("Food"),
                ZonedDateTime.of(2018, 8, 11, 0, 0, 0, 0, ZoneId.systemDefault()),
                ZonedDateTime.now(ZoneId.systemDefault())
            ).value
        )

        // T
        assertEquals(
            listOf(dd.ivt1, dd.ivt2, dd.ivt4),
            tlVM.filteredTransactionList(
                account = false, category = true, date = false, "Expense",
                listOf(), listOf("All"),
                ZonedDateTime.now(ZoneId.systemDefault()),
                ZonedDateTime.now(ZoneId.systemDefault())
            ).value
        )

        // TC
        assertEquals(
            listOf(dd.ivt4),
            tlVM.filteredTransactionList(
                account = false, category = true, date = false, "Expense",
                listOf(), listOf("Entertainment"),
                ZonedDateTime.now(ZoneId.systemDefault()),
                ZonedDateTime.now(ZoneId.systemDefault())
            ).value
        )

        // D
        assertEquals(
            listOf(dd.ivt2, dd.ivt3),
            tlVM.filteredTransactionList(
                account = false, category = false, date = true, "",
                listOf(), listOf(),
                ZonedDateTime.of(2018, 8, 11, 0, 0, 0, 0, ZoneId.systemDefault()),
                ZonedDateTime.of(2018, 8, 14, 0, 0, 0, 0, ZoneId.systemDefault())
            ).value
        )

        // No filter
        assertEquals(
            listOf(dd.ivt1, dd.ivt2, dd.ivt3, dd.ivt4),
            tlVM.filteredTransactionList(
                account = false, category = false, date = false, "",
                listOf(), listOf(),
                ZonedDateTime.now(ZoneId.systemDefault()),
                ZonedDateTime.now(ZoneId.systemDefault())
            ).value
        )
    }
}