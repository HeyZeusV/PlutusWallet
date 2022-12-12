package com.heyzeusv.plutuswallet.ui.cfl.filter

import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, TestCoroutineExtension::class)
internal class FilterViewModelTest {

    // test Fake
    private val repo = FakeRepository()

    // what is being tested
    private lateinit var filterVM: FilterViewModel

    @BeforeEach
    fun setUpViewModel() {

        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        filterVM = FilterViewModel(repo)
    }

    @AfterEach
    fun clearLists() {

        filterVM.accSelectedChips.clear()
        filterVM.exCatSelectedChips.clear()
        filterVM.inCatSelectedChips.clear()
    }

    @Test
    @DisplayName("Should retrieve data to be displayed in ChipGroups from Database")
    fun prepareChipData() {

        val expectedAccList: MutableList<String> = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused")
        val expectedExCatList: MutableList<String> = mutableListOf("All", "Entertainment", "Food", "Unused Expense")
        val expectedInCatList: MutableList<String> = mutableListOf("All", "Salary", "Unused Income", "Zelle")

        filterVM.prepareChipData()

        assertEquals(expectedAccList, filterVM.accList.value)
        assertEquals(expectedExCatList, filterVM.exCatList.value)
        assertEquals(expectedInCatList, filterVM.inCatList.value)
    }

    @Test
    @DisplayName("Should switch type visible when clicked")
    fun typeVisibleOC() {

        filterVM.typeVisibleOC()
        assertEquals(false, filterVM.typeVisible.value)
        filterVM.typeVisibleOC()
        assertEquals(true, filterVM.typeVisible.value)
    }

    @Test
    @DisplayName("Should create selectDate Event containing which date button was selected")
    fun selectDateOC() {

        filterVM.selectDateOC(0)
        val selectDateEvent: Event<Int> = filterVM.selectDateEvent.value!!

        assertEquals(0, selectDateEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should save date user selected after pressing Start button")
    fun startDateSelected() {

        filterVM.startDateSelected(Date(864000000))

        assertEquals(Date(864000000), filterVM.startDate)
        assertEquals("1/10/70", filterVM.startDateLD.value!!)
    }

    @Test
    @DisplayName("Should save date user selected after pressing End button")
    fun endDateSelected() {

        filterVM.endDateSelected(Date(864000000))

        assertEquals(Date(864000000 + 86399999), filterVM.endDate)
        assertEquals("1/11/70", filterVM.endDateLD.value!!)
    }

    @Test
    @DisplayName("Should apply filters selected and create cflChangeEvent")
    fun applyFilterOC() {

        filterVM.catFilter.value = true
        filterVM.accSelectedChips.add("Cash")
        filterVM.exCatSelectedChips.add("Food")
        filterVM.startDate = Date(0)
        filterVM.endDate = Date(1000)
        val expectedCFLtInfo = FilterInfo(
            account = false, category = true, date = false,
            "Expense", listOf("Cash"), listOf("Food"), Date(0), Date(1000)
        )

        filterVM.applyFilterOC()
        val cflChangeEvent: Event<Boolean> = filterVM.cflChange.value!!

        assertEquals(expectedCFLtInfo, filterVM.cflTInfo)
        assertEquals(true, cflChangeEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should reset filters")
    fun applyFilterOCReset() {

        filterVM.accSelectedChips.addAll(listOf("Test1", "Test2", "Test3"))
        filterVM.exCatSelectedChips.addAll(listOf("Test1", "Test2", "Test3"))
        filterVM.inCatSelectedChips.addAll(listOf("Test1", "Test2", "Test3"))
        val expectedStartDate: Date = DateUtils.startOfDay(Date())
        val expectedEndDate = Date(expectedStartDate.time + 86399999)
        val expectedCFLtInfo = FilterInfo(
            account = false, category = false, date = false, "Expense",
            listOf(), listOf(), expectedStartDate, Date(expectedStartDate.time + 86399999)
        )

        filterVM.applyFilterOC()
        val resetEvent: Event<Boolean> = filterVM.resetEvent.value!!
        val cflChangeEvent: Event<Boolean> = filterVM.cflChange.value!!

        assertEquals(listOf<String>(), filterVM.accSelectedChips)
        assertEquals(listOf<String>(), filterVM.exCatSelectedChips)
        assertEquals(listOf<String>(), filterVM.inCatSelectedChips)
        assertEquals(true, resetEvent.getContentIfNotHandled())
        assertEquals(expectedStartDate, filterVM.startDate)
        assertEquals(expectedEndDate, filterVM.endDate)
        assertEquals("", filterVM.startDateLD.value)
        assertEquals("", filterVM.endDateLD.value)
        assertEquals(expectedCFLtInfo, filterVM.cflTInfo)
        assertEquals(true, cflChangeEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should create dateErrorEvent when applying filter with endDate before startDate")
    fun applyFilterOCDateError() {

        filterVM.dateFilterOld.value = true
        filterVM.startDate = Date()
        filterVM.endDate = Date(0)

        filterVM.applyFilterOC()
        val dateErrorEvent: Event<Boolean> = filterVM.dateErrorEvent.value!!

        assertEquals(true, dateErrorEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should create noChipEvent when applying filter with no Chips selected")
    fun applyFilterOCNoChip() {

        /**
         *  Account error
         */
        filterVM.accFilter.value = true
        filterVM.catFilter.value = false
        filterVM.typeVisible.value = true

        filterVM.applyFilterOC()
        var noChipEvent: Event<Boolean> = filterVM.noChipEvent.value!!

        assertEquals(true, noChipEvent.getContentIfNotHandled())

        /**
         *  Expense category error
         */
        filterVM.accFilter.value = false
        filterVM.catFilter.value = true
        filterVM.typeVisible.value = true

        filterVM.applyFilterOC()
        noChipEvent = filterVM.noChipEvent.value!!

        assertEquals(false, noChipEvent.getContentIfNotHandled())

        /**
         *  Income category error
         */
        filterVM.accFilter.value = false
        filterVM.catFilter.value = true
        filterVM.typeVisible.value = false

        filterVM.applyFilterOC()
        noChipEvent = filterVM.noChipEvent.value!!

        assertEquals(false, noChipEvent.getContentIfNotHandled())
    }
}