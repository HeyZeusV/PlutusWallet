package com.heyzeusv.plutuswallet.ui

import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.ui.overview.ChartViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId.systemDefault
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class ChartViewModelTest {

    // test Fake
    private val repo = FakeRepository()
    private val clock = Clock.fixed(
        Instant.parse("1980-01-10T00:00:00Z"),
        ZoneOffset.systemDefault()
    )

    // what is being tested
    private lateinit var chartVM: ChartViewModel

    // dummy data
    private val dd = DummyDataUtil()

    @BeforeEach
    fun setUpViewModel() {
        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        chartVM = ChartViewModel(repo, clock)
    }

    @Test
    @DisplayName("Should have ChartInformation ready for Expense and Income types at start up")
    fun viewModelInit() {
        val expectedExpenseCatTotalList = listOf(
            CategoryTotals("Food", BigDecimal("1000.10"), "Expense"),
            CategoryTotals("Housing", BigDecimal("100.00"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense")
        )
        val expectedIncomeCatTotalList = listOf(
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        assertEquals(expectedExpenseCatTotalList, chartVM.chartInfoList.value[0].ctList)
        assertEquals(expectedIncomeCatTotalList, chartVM.chartInfoList.value[1].ctList)

        val expectedExpenseTotalText = "$1,155.55"
        val expectedIncomeTotalText = "$2,000.32"
        assertEquals(expectedExpenseTotalText, chartVM.chartInfoList.value[0].totalText)
        assertEquals(expectedIncomeTotalText, chartVM.chartInfoList.value[1].totalText)
    }

    @Test
    @DisplayName("Should have correct ChartInformation after adding a Transaction from an" +
            " existing Category.")
    fun checkAfterNewTransactionOfSameCategory() = runTest {
        // adding extra Transactions
        repo.tranList.add(dd.tran1)
        repo.tranList.add(dd.tran3)
        chartVM.updateCatTotalsList(FilterInfo())

        val expectedExpenseCatTotalList = listOf(
            CategoryTotals("Food", BigDecimal("2000.20"), "Expense"),
            CategoryTotals("Housing", BigDecimal("100.00"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense")
        )
        val expectedIncomeCatTotalList = listOf(
            CategoryTotals("Salary", BigDecimal("4000.64"), "Income")
        )
        assertEquals(expectedExpenseCatTotalList, chartVM.chartInfoList.value[0].ctList)
        assertEquals(expectedIncomeCatTotalList, chartVM.chartInfoList.value[1].ctList)

        val expectedExpenseTotalText = "$2,155.65"
        val expectedIncomeTotalText = "$4,000.64"
        assertEquals(expectedExpenseTotalText, chartVM.chartInfoList.value[0].totalText)
        assertEquals(expectedIncomeTotalText, chartVM.chartInfoList.value[1].totalText)
    }

    @Test
    @DisplayName("Should have correct ChartInformation after adding a Transaction from a new" +
            "Category.")
    fun checkAfterNewTransactionOfNewCategory() = runTest {
        val newExpenseCat = dd.tran1
        newExpenseCat.category = "Test Expense Category"
        val newIncomeCat = dd.tran3
        newIncomeCat.category = "Test Income Category"
        repo.tranList.add(newExpenseCat)
        repo.tranList.add(newIncomeCat)
        chartVM.updateCatTotalsList(FilterInfo())

        val expectedExpenseCatTotalList = listOf(
            CategoryTotals("Food", BigDecimal("1000.10"), "Expense"),
            CategoryTotals("Housing", BigDecimal("100.00"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
            CategoryTotals("Test Expense Category", BigDecimal("1000.10"), "Expense")
        )
        val expectedIncomeCatTotalList = listOf(
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income"),
            CategoryTotals("Test Income Category", BigDecimal("2000.32"), "Income")

        )
        assertEquals(expectedExpenseCatTotalList, chartVM.chartInfoList.value[0].ctList)
        assertEquals(expectedIncomeCatTotalList, chartVM.chartInfoList.value[1].ctList)

        val expectedExpenseTotalText = "$2,155.65"
        val expectedIncomeTotalText = "$4,000.64"
        assertEquals(expectedExpenseTotalText, chartVM.chartInfoList.value[0].totalText)
        assertEquals(expectedIncomeTotalText, chartVM.chartInfoList.value[1].totalText)
    }

    @Test
    @DisplayName("Should return CategoryTotal list from Database depending on filters applied")
    fun filterCategoryTotals() = runTest {
        var collectedList = listOf<CategoryTotals>()

        val expectedNoFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1000.10"), "Expense"),
            CategoryTotals("Housing", BigDecimal("100.00"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        chartVM.filteredCategoryTotals(FilterInfo()).collect { collectedList = it }
        assertEquals(expectedNoFilter, collectedList)

        val expectedAccFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1000.10"), "Expense")
        )
        chartVM.filteredCategoryTotals(
            FilterInfo(account = true, accountNames = listOf("Cash"))
        ).collect { collectedList = it }
        assertEquals(expectedAccFilter, collectedList)

        val expectedDateFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Housing", BigDecimal("100.00"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        chartVM.filteredCategoryTotals(
            FilterInfo(
                date = true,
                start = ZonedDateTime.of(1980, 1, 11, 1, 0, 0, 0, systemDefault()),
                end = ZonedDateTime.of(1980, 1, 15, 1, 0, 0, 0, systemDefault())
            )
        ).collect { collectedList = it }
        assertEquals(expectedDateFilter, collectedList)

        val expectedBothFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1000.10"), "Expense")
        )
        chartVM.filteredCategoryTotals(
            FilterInfo(
                account = true,
                accountNames = listOf("Cash"),
                date = true,
                start = ZonedDateTime.of(1980, 1, 9, 1, 0, 0, 0, systemDefault()),
                end = ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault())
            )
        ).collect { collectedList = it }
        assertEquals(expectedBothFilter, collectedList)
    }
}