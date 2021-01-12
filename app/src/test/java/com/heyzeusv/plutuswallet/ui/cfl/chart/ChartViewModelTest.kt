package com.heyzeusv.plutuswallet.ui.cfl.chart

import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class ChartViewModelTest {

    // test Fake
    private lateinit var repo: FakeRepository

    // what is being tested
    private lateinit var chartVM: ChartViewModel

    // dummy data
    private val acc1 = Account(1, "Credit Card")
    private val acc2 = Account(2, "Debit Card")
    private val acc3 = Account(3, "Cash")
    private var accList : MutableList<Account> = mutableListOf()

    private val cat1 = Category(1, "Food", "Expense")
    private val cat2 = Category(2, "Entertainment", "Expense")
    private val cat3 = Category(3, "Salary", "Income")
    private var catList : MutableList<Category> = mutableListOf()

    private val tran1 = Transaction(1, "Party", Date(86400000), BigDecimal("1000.10"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 2), true)
    private val tran2 = Transaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 3), false)
    private val tran3 = Transaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"), "Debit Card", "Income", "Salary", "", true, 2, 1, Date(86400000 * 11), false)
    private val tran4 = Transaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment")
    private var tranList: MutableList<Transaction> = mutableListOf()

    @BeforeEach
    fun setUpViewModel() {

        // some function add/remove data, so want same data at start of every test.
        accList = mutableListOf(acc1, acc2, acc3)
        catList = mutableListOf(cat1, cat2, cat3)
        tranList = mutableListOf(tran1, tran2, tran3, tran4)

        // initialize fake repo with dummy data and pass it to ViewModel
        repo = FakeRepository(accList, catList, tranList)
        chartVM = ChartViewModel(repo)
    }

    @Test
    @DisplayName("Should split CategoryTotal list into 2 by type and retrieve Category Names")
    fun prepareLists() {

        val expectedExCt: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1100.10"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense")
        )
        val expectedInCt: List<CategoryTotals> = listOf(
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        val expectedExNames: List<String> = listOf("Food", "Entertainment")
        val expectedInNames: List<String> = listOf("Salary")

        // no filter
        val ctList: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = false, fDate = false, "", Date(0), Date(0)
        )
        chartVM.prepareLists(ctList.value!!)

        assertEquals(expectedExCt, chartVM.exCatTotals)
        assertEquals(expectedInCt, chartVM.inCatTotals)
        assertEquals(expectedExNames, chartVM.exNames)
        assertEquals(expectedInNames, chartVM.inNames)
    }

    @Test
    @DisplayName("Should return total added up from CategoryTotal lists depending on filters applied")
    fun prepareTotals() {

        val expectedTotalZero: BigDecimal = BigDecimal.ZERO
        val expectedAllExTotal = BigDecimal("1155.55")
        val expectedFoodExTotal = BigDecimal("1100.10")
        val expectedAllInTotal = BigDecimal("2000.32")
        val expectedTestInTotal: BigDecimal = BigDecimal.ZERO

        // no filter
        val ctList: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = false, fDate = false, "", Date(0), Date(0)
        )
        chartVM.prepareLists(ctList.value!!)

        // All expense categories
        chartVM.prepareTotals(true, "All", "Expense")
        assertEquals(expectedAllExTotal, chartVM.exTotal)
        assertEquals(expectedTotalZero, chartVM.inTotal)
        // Food expense category
        chartVM.prepareTotals(true, "Food", "Expense")
        assertEquals(expectedFoodExTotal, chartVM.exTotal)
        assertEquals(expectedTotalZero, chartVM.inTotal)
        // All income categories
        chartVM.prepareTotals(true, "All", "Income")
        assertEquals(expectedTotalZero, chartVM.exTotal)
        assertEquals(expectedAllInTotal, chartVM.inTotal)
        // Test income category
        chartVM.prepareTotals(true, "Test", "Income")
        assertEquals(expectedTotalZero, chartVM.exTotal)
        assertEquals(expectedTestInTotal, chartVM.inTotal)
        // All categories
        chartVM.prepareTotals(null, null, null)
        assertEquals(expectedAllExTotal, chartVM.exTotal)
        assertEquals(expectedAllInTotal, chartVM.inTotal)
    }

    @Test
    @DisplayName("Should return CategoryTotal list from Database depending on filters applied")
    fun filterCategoryTotals() {

        val expectedNoFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1100.10"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        val expectedAccFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1100.10"), "Expense")
        )
        val expectedDateFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("100.00"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        val expectedBothFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1000.10"), "Expense")
        )

        // testing all filter options
        val noFilter: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = false, fDate = false, "", Date(0), Date(0)
        )
        val accFilter: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = true, fDate = false, "Cash", Date(0), Date(0)
        )
        val dateFilter: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = false, fDate = true, "", Date(86400000 * 2), Date(86400000 * 5)
        )
        val bothFilter: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = true, fDate = true, "Cash", Date(0), Date(86400000)
        )

        assertEquals(expectedNoFilter, noFilter.value)
        assertEquals(expectedAccFilter, accFilter.value)
        assertEquals(expectedDateFilter, dateFilter.value)
        assertEquals(expectedBothFilter, bothFilter.value)
    }
}