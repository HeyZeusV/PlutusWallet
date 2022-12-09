package com.heyzeusv.plutuswallet.ui.cfl.chart

import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, TestCoroutineExtension::class)
internal class ChartViewModelTest {

    // test Fake
    private val repo = FakeRepository()

    // what is being tested
    private lateinit var chartVM: ChartViewModel

    // dummy data
    private val dd = DummyDataUtil()

    @BeforeEach
    fun setUpViewModel() {

        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        chartVM = ChartViewModel(repo, SettingsValues())
    }

    @Test
    @DisplayName("Should have ChartInformation ready for Expense and Income types at start up")
    fun checkSetUp() {
        val expectedExpenseCatTotalList = listOf(
            CategoryTotals("Food", BigDecimal("1100.10"), "Expense"),
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
        repo.tranList.add(dd.tran1)
        repo.tranList.add(dd.tran3)
        chartVM.updateCatTotalsList(FilterInfo())


        val expectedExpenseCatTotalList = listOf(
            CategoryTotals("Food", BigDecimal("2100.20"), "Expense"),
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
            CategoryTotals("Food", BigDecimal("1100.10"), "Expense"),
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
    @DisplayName("Should split CategoryTotal list into 2 by type and retrieve Category Names")
    fun prepareLists() {

        val emptyCtList: List<CategoryTotals> = emptyList()
        val emptyNameList: List<String> = emptyList()

        val expectedExCt: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1100.10"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense")
        )
        val expectedInCt: List<CategoryTotals> = listOf(
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        val expectedExNames: List<String> = listOf("Food", "Entertainment")
        val expectedInNames: List<String> = listOf("Salary")

        // retrieve all
        val ctList: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = false, fDate = false, listOf(), Date(0), Date(0)
        )
        // no filter
        chartVM.prepareLists(ctList.value!!, false, "")

        assertEquals(expectedExCt, chartVM.exCatTotals)
        assertEquals(expectedInCt, chartVM.inCatTotals)
        assertEquals(expectedExNames, chartVM.exNames)
        assertEquals(expectedInNames, chartVM.inNames)

        // expense filter
        chartVM.prepareLists(ctList.value!!, true, "Expense")

        assertEquals(expectedExCt, chartVM.exCatTotals)
        assertEquals(emptyCtList, chartVM.inCatTotals)
        assertEquals(expectedExNames, chartVM.exNames)
        assertEquals(emptyNameList, chartVM.inNames)

        // income filter
        chartVM.prepareLists(ctList.value!!, true, "Income")

        assertEquals(emptyCtList, chartVM.exCatTotals)
        assertEquals(expectedInCt, chartVM.inCatTotals)
        assertEquals(emptyNameList, chartVM.exNames)
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
            fAccount = false, fDate = false, listOf(), Date(0), Date(0)
        )
        chartVM.prepareLists(ctList.value!!, false, "")

        // All expense categories
        chartVM.prepareTotals(true, listOf("All"), "Expense")
        assertEquals(expectedAllExTotal, chartVM.exTotal)
        assertEquals(expectedTotalZero, chartVM.inTotal)
        // Food expense category
        chartVM.prepareTotals(true, listOf("Food"), "Expense")
        assertEquals(expectedFoodExTotal, chartVM.exTotal)
        assertEquals(expectedTotalZero, chartVM.inTotal)
        // All income categories
        chartVM.prepareTotals(true, listOf("All"), "Income")
        assertEquals(expectedTotalZero, chartVM.exTotal)
        assertEquals(expectedAllInTotal, chartVM.inTotal)
        // Test income category
        chartVM.prepareTotals(true, listOf("Test"), "Income")
        assertEquals(expectedTotalZero, chartVM.exTotal)
        assertEquals(expectedTestInTotal, chartVM.inTotal)
        // All categories
        chartVM.prepareTotals(false, listOf(), "Expense")
        assertEquals(expectedAllExTotal, chartVM.exTotal)
        assertEquals(expectedAllInTotal, chartVM.inTotal)
    }

    @Test
    @DisplayName("Should return CategoryTotal list from Database depending on filters applied")
    fun filterCategoryTotals() = runTest {
        var collectedList = listOf<CategoryTotals>()

        val expectedNoFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1100.10"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        chartVM.filteredCategoryTotals(FilterInfo()).collect { collectedList = it }
        assertEquals(expectedNoFilter, collectedList)

        val expectedAccFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1100.10"), "Expense")
        )
        chartVM.filteredCategoryTotals(
            FilterInfo(account = true, accountNames = listOf("Cash"))
        ).collect { collectedList = it }
        assertEquals(expectedAccFilter, collectedList)

        val expectedDateFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("100.00"), "Expense"),
            CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
            CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
        )
        chartVM.filteredCategoryTotals(
            FilterInfo(date = true, start = Date(86400000 * 2), end = Date(86400000 * 5))
        ).collect { collectedList = it }
        assertEquals(expectedDateFilter, collectedList)

        val expectedBothFilter: List<CategoryTotals> = listOf(
            CategoryTotals("Food", BigDecimal("1000.10"), "Expense")
        )
        chartVM.filteredCategoryTotals(
            FilterInfo(
                account = true, date = true,
                accountNames = listOf("Cash"), start = Date(0), end = Date(86400000)
            )
        ).collect { collectedList = it }
        assertEquals(expectedBothFilter, collectedList)
    }
}