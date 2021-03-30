package com.heyzeusv.plutuswallet.ui.cfl.filter

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.TransactionInfo
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

private const val MIDNIGHT_MILLI = 86399999

/**
 *  Data manager for FilterFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class FilterViewModel @ViewModelInject constructor(
    private val tranRepo: Repository
) : ViewModel() {

    // translated "All"
    var all = "All"

    // current Account selected and Account list
    val account: MutableLiveData<String> = MutableLiveData("None")
    val accList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // type of Category selected and which is visible, true = "Expense" false = "Income"
    var typeVisible: MutableLiveData<Boolean> = MutableLiveData(true)

    // Category list by type
    val exCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val inCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // Date values
    var startDate: Date = DateUtils.startOfDay(Date())
    var endDate: Date = Date(startDate.time + MIDNIGHT_MILLI)

    // Date string values
    val startDateLD: MutableLiveData<String> = MutableLiveData("")
    val endDateLD: MutableLiveData<String> = MutableLiveData("")

    // Button status
    val accFilter: MutableLiveData<Boolean> = MutableLiveData(false)
    val catFilter: MutableLiveData<Boolean> = MutableLiveData(false)
    val dateFilter: MutableLiveData<Boolean> = MutableLiveData(false)

    // Chip status
    val accSelectedChips: MutableList<String> = mutableListOf()
    val exCatSelectedChips: MutableList<String> = mutableListOf()
    val inCatSelectedChips: MutableList<String> = mutableListOf()

    // Events
    private val _noChipEvent = MutableLiveData<Event<Boolean>>()
    val noChipEvent: LiveData<Event<Boolean>> = _noChipEvent

    private val _dateErrorEvent = MutableLiveData<Event<Boolean>>()
    val dateErrorEvent: LiveData<Event<Boolean>> = _dateErrorEvent

    private val _selectDateEvent = MutableLiveData<Event<Int>>()
    val selectDateEvent: LiveData<Event<Int>> = _selectDateEvent

    private val _resetEvent = MutableLiveData<Event<Boolean>>()
    val resetEvent: LiveData<Event<Boolean>> = _resetEvent

    // used to pass TransactionInfo to CFLViewModel
    private val _cflChange = MutableLiveData<Event<Boolean>>()
    val cflChange: LiveData<Event<Boolean>> = _cflChange

    var cflTInfo: TransactionInfo = TransactionInfo()

    /**
     *  Retrieves data that will be displayed in Spinners from Repository.
     */
    fun prepareChipData() {

        viewModelScope.launch {
            // Account data
            accList.value = tranRepo.getAccountNamesAsync()

            // Category by type data
            val mExCatList: MutableList<String> = tranRepo.getCategoryNamesByTypeAsync("Expense")
            val mInCatList: MutableList<String> = tranRepo.getCategoryNamesByTypeAsync("Income")
            mExCatList.add(0, all)
            mInCatList.add(0, all)
            exCatList.value = mExCatList
            inCatList.value = mInCatList
        }
    }

    /**
     *  Changes what Transaction type is visible.
     */
    fun typeVisibleOC() {

        typeVisible.value = !typeVisible.value!!
    }

    /**
     *  Event to show DatePickerDialog. Uses [viewId] to determine if Start or End was selected.
     */
    fun selectDateOC(viewId: Int) {

        _selectDateEvent.value = Event(viewId)
    }

    /**
     *  Takes [newDate] user selected on Start button and saves to be used in query.
     */
    fun startDateSelected(newDate: Date) {

        startDate = newDate
        startDateLD.value = DateFormat.getDateInstance(DateFormat.SHORT).format(startDate)
    }

    /**
     *  Takes [newDate] user selected on End button and saves to be used in query
     */
    fun endDateSelected(newDate: Date) {

        endDate = Date(newDate.time + MIDNIGHT_MILLI)
        endDateLD.value = DateFormat.getDateInstance(DateFormat.SHORT).format(endDate)
    }

    /**
     *  Applies filters or shows Snackbar warning if no Chips are selected or if end date is before
     *  start date.
     */
    fun applyFilterOC() {

        when {
            // users must select at least 1 Chip
            accFilter.value!! && accSelectedChips.isEmpty() -> _noChipEvent.value = Event(true)
            catFilter.value!! && typeVisible.value!! && exCatSelectedChips.isEmpty() ->
                _noChipEvent.value = Event(false)
            catFilter.value!! && !typeVisible.value!! && inCatSelectedChips.isEmpty() ->
                _noChipEvent.value = Event(false)
            // startDate must be before endDate else it displays warning and doesn't apply filters
            dateFilter.value!! && startDate > endDate -> _dateErrorEvent.value = Event(true)
            else -> {
                val cats: List<String>
                val type: String
                // sets type and category applied
                if (typeVisible.value!!) {
                    type = "Expense"
                    cats = exCatSelectedChips
                } else {
                    type = "Income"
                    cats = inCatSelectedChips
                }

                // translates "All"
                if (cats.contains(all)) cats[cats.indexOf(all)] = "All"

                // updating MutableLiveData value in ViewModel
                cflTInfo = TransactionInfo(
                    accFilter.value!!, catFilter.value!!, dateFilter.value!!,
                    type, accSelectedChips, cats,
                    startDate, endDate
                )
                // if all filters are unchecked
                if (!accFilter.value!!
                    && !catFilter.value!!
                    && !dateFilter.value!!
                ) {
                    resetFilter()
                }
                _cflChange.value = Event(true)
            }
        }
    }

    /**
     *  Resets all filters.
     */
    private fun resetFilter() {

        // clear Chip lists and launch resetEvent to clear Chips
        accSelectedChips.clear()
        exCatSelectedChips.clear()
        inCatSelectedChips.clear()
        _resetEvent.value = Event(true)

        // sets the startDate to very start of current day and endDate to right before the next day
        startDate = DateUtils.startOfDay(Date())
        endDate = Date(startDate.time + MIDNIGHT_MILLI)
        startDateLD.value = ""
        endDateLD.value = ""
    }
}