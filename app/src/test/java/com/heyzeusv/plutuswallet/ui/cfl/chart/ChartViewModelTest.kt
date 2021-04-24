package com.heyzeusv.plutuswallet.ui.cfl.chart

import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
internal class ChartViewModelTest {

    // test Fake
    private val repo = FakeRepository()

    // what is being tested
    private lateinit var chartVM: ChartViewModel

    @BeforeEach
    fun setUpViewModel() {

        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        chartVM = ChartViewModel(repo)
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
            fAccount = false, fDate = false, listOf(),
            ZonedDateTime.now(ZoneId.systemDefault()), ZonedDateTime.now(ZoneId.systemDefault())
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
            fAccount = false, fDate = false, listOf(),
            ZonedDateTime.now(ZoneId.systemDefault()), ZonedDateTime.now(ZoneId.systemDefault())
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
            fAccount = false, fDate = false, listOf(),
            ZonedDateTime.now(ZoneId.systemDefault()), ZonedDateTime.now(ZoneId.systemDefault())
        )
        val accFilter: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = true, fDate = false, listOf("Cash"),
            ZonedDateTime.now(ZoneId.systemDefault()), ZonedDateTime.now(ZoneId.systemDefault())
        )
        val dateFilter: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = false, fDate = true, listOf(),
            ZonedDateTime.of(2018, 8, 11, 0, 0, 0, 0, ZoneId.systemDefault()),
            ZonedDateTime.of(2018, 8, 20, 0, 0, 0, 0, ZoneId.systemDefault())
        )
        val bothFilter: LiveData<List<CategoryTotals>> = chartVM.filteredCategoryTotals(
            fAccount = true, fDate = true, listOf("Cash"),
            ZonedDateTime.of(2018, 8, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
            ZonedDateTime.of(2018, 8, 10, 0, 0, 0, 0, ZoneId.systemDefault())
        )

        assertEquals(expectedNoFilter, noFilter.value)
        assertEquals(expectedAccFilter, accFilter.value)
        assertEquals(expectedDateFilter, dateFilter.value)
        assertEquals(expectedBothFilter, bothFilter.value)
    }
}