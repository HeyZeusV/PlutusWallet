package com.heyzeusv.plutuswallet.ui.cfl.filter

import com.heyzeusv.plutuswallet.DummyDataUtil
import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.TransactionInfo
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class FilterViewModelTest {

    // test Fake
    private lateinit var repo: FakeRepository

    // what is being tested
    private lateinit var filterVM: FilterViewModel

    // dummy data
    private lateinit var dd: DummyDataUtil

    @BeforeEach
    fun setUpViewModel() {

        // some function add/remove data, so want same data at start of every test.
        dd = DummyDataUtil()

        // initialize fake repo with dummy data and pass it to ViewModel
        repo = FakeRepository(dd.accList, dd.catList, dd.tranList)
        filterVM = FilterViewModel(repo)
    }

    @Test
    @DisplayName("Should retrieve data to be displayed in Spinners from Database")
    fun prepareSpinners() {

        val expectedAccList: MutableList<String> = mutableListOf("Cash", "Credit Card", "Debit Card")
        val expectedExCatList: MutableList<String> = mutableListOf("All", "Entertainment", "Food")
        val expectedInCatList: MutableList<String> = mutableListOf("All", "Salary")

        filterVM.prepareSpinners()

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

        filterVM.startDateSelected(Date(10000000))

        assertEquals(Date(10000000), filterVM.startDate.value)
    }

    @Test
    @DisplayName("Should save date user selected after pressing End button")
    fun endDateSelected() {

        filterVM.endDateSelected(Date(1000))

        assertEquals(Date(1000), filterVM.endDate.value)
    }

    @Test
    @DisplayName("Should apply filters selected and create cflChange event")
    fun applyFilterOC() {

        filterVM.catCheck.value = true
        filterVM.account.value = "Cash"
        filterVM.exCategory.value = "Food"
        filterVM.startDate.value = Date(0)
        filterVM.endDate.value = Date(1000)
        filterVM.exCategory.value = "Food"
        val expectedCFLtInfo = TransactionInfo(
            account = false, category = true, date = false,
            "Expense", "Cash", "Food", Date(0), Date(1000)
        )

        filterVM.applyFilterOC()
        val cflChangeEvent: Event<Boolean> = filterVM.cflChange.value!!

        assertEquals(expectedCFLtInfo, filterVM.cflTInfo)
        assertEquals(true, cflChangeEvent.getContentIfNotHandled())

        // check if filters reset properly
        filterVM.catCheck.value = false
        val expectedStartDate: Date = DateUtils.startOfDay(Date())
        val expectedEndDate = Date(expectedStartDate.time + 86399999)

        filterVM.applyFilterOC()

        assertEquals(expectedStartDate, filterVM.startDate.value)
        assertEquals(expectedEndDate, filterVM.endDate.value)
        assertEquals("All", filterVM.exCategory.value)
        assertEquals("All", filterVM.inCategory.value)

    }

    @Test
    @DisplayName("Should create dateError Event when applying filter with endDate before startDate")
    fun applyFilterOCDateError() {

        filterVM.dateCheck.value = true
        filterVM.startDate.value = Date()
        filterVM.endDate.value = Date(0)

        filterVM.applyFilterOC()
        val dateErrorEvent: Event<Boolean> = filterVM.dateErrorEvent.value!!

        assertEquals(true, dateErrorEvent.getContentIfNotHandled())
    }
}