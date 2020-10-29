package com.heyzeusv.plutuswallet.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo
import com.heyzeusv.plutuswallet.utilities.DateUtils
import com.heyzeusv.plutuswallet.utilities.Event
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import java.util.Date

private const val MIDNIGHT_MILLI = 86399999

/**
 *  Data manager for FilterFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class FilterViewModel @ViewModelInject constructor(
    private val tranRepo: TransactionRepository
) : ViewModel() {

    // translated "All"
    var all = ""

    // current Account selected and Account list
    val account: MutableLiveData<String> = MutableLiveData("None")
    val accList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // type of Category selected and which is visible, true = "Expense" false = "Income"
    var typeVisible: MutableLiveData<Boolean> = MutableLiveData(true)

    // current Category selected and Category list, both by type
    val exCategory: MutableLiveData<String> = MutableLiveData("")
    val inCategory: MutableLiveData<String> = MutableLiveData("")
    val exCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val inCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // Date values
    val startDate: MutableLiveData<Date> = MutableLiveData(DateUtils.startOfDay(Date()))
    val endDate: MutableLiveData<Date> =
        MutableLiveData(Date(startDate.value!!.time + MIDNIGHT_MILLI))

    // CheckBox status
    val accCheck: MutableLiveData<Boolean> = MutableLiveData(false)
    val catCheck: MutableLiveData<Boolean> = MutableLiveData(false)
    val dateCheck: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _dateErrorEvent = MutableLiveData<Event<Boolean>>()
    val dateErrorEvent: LiveData<Event<Boolean>> = _dateErrorEvent

    private val _selectDateEvent = MutableLiveData<Event<Int>>()
    val selectDateEvent: LiveData<Event<Int>> = _selectDateEvent

    // used to pass TransactionInfo to CFLViewModel
    private val _cflChange = MutableLiveData<Event<Boolean>>()
    val cflChange: LiveData<Event<Boolean>> = _cflChange
    var cflTInfo: TransactionInfo = TransactionInfo()

    /**
     *  Retrieves data that will be displayed in Spinners from Repository.
     */
    fun prepareSpinners() {

        viewModelScope.launch {
            // Account data
            accList.value = tranRepo.getAccountNamesAsync().await()

            // Category by type data
            val mExCatList: MutableList<String> = getCategoriesByTypeAsync("Expense").await()
            val mInCatList: MutableList<String> = getCategoriesByTypeAsync("Income").await()
            mExCatList.add(0, all)
            mInCatList.add(0, all)
            exCatList.value = mExCatList
            inCatList.value = mInCatList

            // sets Spinner to previous value since it might have moved position in list
            exCategory.value = exCategory.value
            inCategory.value = inCategory.value
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

        startDate.value = newDate
    }

    /**
     *  Takes [newDate] user selected on End button and saves to be used in query
     */
    fun endDateSelected(newDate: Date) {

        endDate.value = newDate
    }

    /**
     *  Applies filters or shows Snackbar warning if end date is before start date.
     */
    fun applyFilterOC() {

        // startDate must be before endDate else it displays warning and doesn't apply filters
        if (startDate.value!! > endDate.value!!
            && dateCheck.value!!
        ) {
            _dateErrorEvent.value = Event(true)
        } else {
            var cat: String
            val type: String
            // sets type and category applied
            if (typeVisible.value!!) {
                type = "Expense"
                cat = exCategory.value!!
            } else {
                type = "Income"
                cat = inCategory.value!!
            }

            // translates "All"
            if (cat == all) cat = "All"

            // updating MutableLiveData value in ViewModel
            cflTInfo = TransactionInfo(
                accCheck.value!!, catCheck.value!!, dateCheck.value!!,
                type, account.value!!, cat,
                startDate.value!!, endDate.value!!
            )
            // if all filters are unchecked
            if (!accCheck.value!!
                && !catCheck.value!!
                && !dateCheck.value!!
            ) {
                resetFilter()
            }
            _cflChange.value = Event(true)
        }
    }

    /**
     *  Resets all filters.
     */
    private fun resetFilter() {

        // sets the startDate to very start of current day and endDate to right before the next day
        startDate.value = DateUtils.startOfDay(Date())
        endDate.value = startDate.value

        // resets type Button and Spinner selections
        exCategory.value = all
        inCategory.value = all
    }

    /**
     *  Category Queries
     */
    private suspend fun getCategoriesByTypeAsync(type: String): Deferred<MutableList<String>> {

        return tranRepo.getCategoryNamesByTypeAsync(type)
    }
}