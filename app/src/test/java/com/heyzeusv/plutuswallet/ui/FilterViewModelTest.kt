package com.heyzeusv.plutuswallet.ui

import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.ui.overview.FilterViewModel
import com.heyzeusv.plutuswallet.util.FilterChipAction.ADD
import com.heyzeusv.plutuswallet.util.FilterState.INVALID_DATE_RANGE
import com.heyzeusv.plutuswallet.util.FilterState.NO_SELECTED_ACCOUNT
import com.heyzeusv.plutuswallet.util.FilterState.NO_SELECTED_CATEGORY
import com.heyzeusv.plutuswallet.util.FilterState.NO_SELECTED_DATE
import com.heyzeusv.plutuswallet.util.FilterState.VALID
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.endOfDay
import java.time.Clock
import java.time.Instant
import java.time.ZoneId.systemDefault
import java.time.ZoneOffset
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
internal class FilterViewModelTest {

    // test Fake
    private val repo = FakeRepository()
    private val clock = Clock.fixed(
        Instant.parse("1980-01-10T00:00:00Z"),
        ZoneOffset.systemDefault()
    )

    // what is being tested
    private lateinit var filterVM: FilterViewModel

    // dummy data
    private val dd = DummyDataUtil()

    @BeforeEach
    fun setUpViewModel() = runTest {
        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        filterVM = FilterViewModel(repo, clock)
        repo.accountNameListEmit(dd.accList.map { it.name })
        repo.expenseCatNameListEmit(dd.catList.filter { it.type == EXPENSE.type }.map { it.name })
        repo.incomeCatNameListEmit(dd.catList.filter { it.type == INCOME.type }.map { it.name })
    }

    @Test
    @DisplayName("Should retrieve data to be displayed in ChipGroups from Database at startup")
    fun viewModelInit() {
        val expectedAccList =
            mutableListOf("Cash", "Credit Card", "Debit Card", "Savings", "Unused")
        val expectedExCatList = mutableListOf("Entertainment", "Food", "Housing", "Unused Expense")
        val expectedInCatList = mutableListOf("Salary", "Unused Income", "Zelle")

        assertEquals(expectedAccList, filterVM.accountList.value)
        assertEquals(expectedExCatList, filterVM.categoryList.value)

        filterVM.updateTypeSelected(INCOME)
        assertEquals(expectedInCatList, filterVM.categoryList.value)
    }

    @Test
    @DisplayName("Should save new start date selected and update String that displays formatted date")
    fun updateStartDateString() {
        filterVM.updateStartDateString(ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()))

        assertEquals("1/10/80", filterVM.startDateString.value)
    }

    @Test
    @DisplayName("Should save new end date selected and update String that displays formatted date")
    fun updateEndDateString() {
        filterVM.updateEndDateString(ZonedDateTime.of(1980, 1, 11, 1, 0, 0, 0, systemDefault()))

        assertEquals("1/11/80", filterVM.endDateString.value)
    }

    @Test
    @DisplayName("Should update filterState to NO_SELECTED_ACCOUNT when selecting account filter," +
            "but not selecting any accounts")
    fun applyFilter_noSelectedAccount() {
        filterVM.updateAccountFilter(true)

        filterVM.applyFilter()

        assertEquals(NO_SELECTED_ACCOUNT, filterVM.filterState.value)
    }

    @Test
    @DisplayName("Should update filterState to NO_SELECTED_CATEGORY when selecting category filter," +
            "but not selecting any categories")
    fun applyFilter_noSelectedCategory() {
        filterVM.updateCategoryFilter(true)

        filterVM.applyFilter()

        assertEquals(NO_SELECTED_CATEGORY, filterVM.filterState.value)

        // switch to income type
        filterVM.updateTypeSelected(INCOME)

        filterVM.applyFilter()

        assertEquals(NO_SELECTED_CATEGORY, filterVM.filterState.value)
    }

    @Test
    @DisplayName("Should update filterState to NO_SELECTED_DATE when selecting date filter," +
            "but not selecting start and/or end dates")
    fun applyFilter_noSelectedDate() {
        // no dates selected
        filterVM.updateDateFilter(true)

        filterVM.applyFilter()

        assertEquals(NO_SELECTED_DATE, filterVM.filterState.value)

        // select only start date
        filterVM.updateStartDateString(ZonedDateTime.now(clock))

        filterVM.applyFilter()

        assertEquals(NO_SELECTED_DATE, filterVM.filterState.value)

        // reset
        filterVM.updateDateFilter(false)

        filterVM.applyFilter()

        // select only end date
        filterVM.updateDateFilter(true)
        filterVM.updateEndDateString(ZonedDateTime.now(clock))

        filterVM.applyFilter()

        assertEquals(NO_SELECTED_DATE, filterVM.filterState.value)
    }

    @Test
    @DisplayName("Should update filterState to INVALID_DATE_RANGE when selecting date filter, but" +
            "selecting a start date that is after the end date")
    fun applyFilter_invalidDateRange() {
        filterVM.updateDateFilter(true)
        filterVM.updateStartDateString(ZonedDateTime.now(clock))
        filterVM.updateEndDateString(ZonedDateTime.of(1980, 1, 7, 1, 0, 0, 0, systemDefault()))

        filterVM.applyFilter()

        assertEquals(INVALID_DATE_RANGE, filterVM.filterState.value)
    }

    @Test
    @DisplayName("Should reset filters")
    fun applyFilter_reset() {
        filterVM.updateAccountFilter(true)
        filterVM.updateCategoryFilter(true)
        filterVM.updateDateFilter(true)
        filterVM.updateAccountSelected("Cash", ADD)
        filterVM.updateCategorySelectedList("Food", ADD)
        filterVM.updateStartDateString(ZonedDateTime.of(1980, 1, 9, 1, 0, 0, 0, systemDefault()))
        filterVM.updateEndDateString(ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()))
        val expectedFilterInfo = FilterInfo(
            account = true, listOf("Cash"), category = true,
            EXPENSE.type, listOf("Food"), date = true,
            ZonedDateTime.of(1980, 1, 9, 1, 0, 0, 0, systemDefault()),
            endOfDay(ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()))
        )

        // first fill filter with data
        filterVM.applyFilter()
        assertEquals(expectedFilterInfo, filterVM.filterInfo.value)

        filterVM.updateAccountFilter(false)
        filterVM.updateCategoryFilter(false)
        filterVM.updateDateFilter(false)

        // this second call should reset filter data
        filterVM.applyFilter()

        assertEquals(listOf<String>(), filterVM.accountSelected.value)
        assertEquals(listOf<String>(), filterVM.categorySelectedList.value)
        filterVM.updateTypeSelected(INCOME)
        assertEquals(listOf<String>(), filterVM.categorySelectedList.value)
        assertEquals("", filterVM.startDateString.value)
        assertEquals("", filterVM.endDateString.value)
        assertEquals(FilterInfo(), filterVM.filterInfo.value)
        assertEquals(VALID, filterVM.filterState.value)
    }

    @Test
    @DisplayName("Should create FilterInfo object with all filter options")
    fun applyFilter_valid() {
        filterVM.updateAccountFilter(true)
        filterVM.updateCategoryFilter(true)
        filterVM.updateDateFilter(true)
        filterVM.updateAccountSelected("Cash", ADD)
        filterVM.updateCategorySelectedList("Food", ADD)
        filterVM.updateStartDateString(ZonedDateTime.of(1980, 1, 9, 1, 0, 0, 0, systemDefault()))
        filterVM.updateEndDateString(ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()))
        val expectedFilterInfo = FilterInfo(
            account = true, listOf("Cash"), category = true,
            EXPENSE.type, listOf("Food"), date = true,
            ZonedDateTime.of(1980, 1, 9, 1, 0, 0, 0, systemDefault()),
            endOfDay(ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()))
        )

        filterVM.applyFilter()

        assertEquals(expectedFilterInfo, filterVM.filterInfo.value)
        assertEquals(VALID, filterVM.filterState.value)
    }
}