package com.heyzeusv.plutuswallet.ui.cfl.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.ui.transaction.FilterState
import com.heyzeusv.plutuswallet.ui.transaction.FilterState.INVALID_DATE_RANGE
import com.heyzeusv.plutuswallet.ui.transaction.FilterState.NO_SELECTED_ACCOUNT
import com.heyzeusv.plutuswallet.ui.transaction.FilterState.NO_SELECTED_CATEGORY
import com.heyzeusv.plutuswallet.ui.transaction.FilterState.NO_SELECTED_DATE
import com.heyzeusv.plutuswallet.ui.transaction.FilterState.VALID
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val MIDNIGHT_MILLI = 86399999

/**
 *  Data manager for FilterFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
@HiltViewModel
class FilterViewModel @Inject constructor(
    private val tranRepo: Repository
) : ViewModel() {

    // translated "All"
    var all = "All"

    // Account list
    val accList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    private val _accountList = MutableStateFlow(listOf<String>())
    val accountList: StateFlow<List<String>> get() = _accountList
    private fun updateAccountList(newList: List<String>) { _accountList.value = newList }

    private val _expenseCatList = MutableStateFlow(listOf<String>())
    val expenseCatList: StateFlow<List<String>> get() = _expenseCatList
    private fun updateExpenseCatList(newList: List<String>) { _expenseCatList.value = newList }

    private val _incomeCatList = MutableStateFlow(listOf<String>())
    val incomeCatList: StateFlow<List<String>> get() = _incomeCatList
    private fun updateIncomeCatList(newList: List<String>) { _incomeCatList.value = newList }

    private val _typeSelected = MutableStateFlow(EXPENSE)
    val typeSelected: StateFlow<TransactionType> get() = _typeSelected
    fun updateTypeSelected(newValue: TransactionType) { _typeSelected.value = newValue }

    private val _filterState = MutableStateFlow(VALID)
    val filterState: StateFlow<FilterState> get() = _filterState
    fun updateFilterState(newState: FilterState) { _filterState.value = newState}

    private val _filterInfo = MutableStateFlow(FilterInfo())
    val filterInfo: StateFlow<FilterInfo> get() = _filterInfo

    // type of Category selected and which is visible, true = "Expense" false = "Income"
    var typeVisible: MutableLiveData<Boolean> = MutableLiveData(true)

    // Category list by type
    val exCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val inCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // Date values
    var startDateOld: Date = DateUtils.startOfDay(Date())
    var endDateOld: Date = Date(startDateOld.time + MIDNIGHT_MILLI)

    var startDate = DateUtils.startOfDay(Date())
    var endDate = Date(startDate.time + MIDNIGHT_MILLI)

    private val dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT)

    private val _startDateString = MutableStateFlow("")
    val startDateString: StateFlow<String> get() = _startDateString
    fun updateStartDateString(newDate: Date) {
        startDate = newDate
        _startDateString.value = dateFormatter.format(startDate)
    }

    private val _endDateString = MutableStateFlow("")
    val endDateString: StateFlow<String> get() = _endDateString
    fun updateEndDateString(newDate: Date) {
        endDate = Date(newDate.time + MIDNIGHT_MILLI)
        _endDateString.value = dateFormatter.format(endDate)
    }


    // Date string values
    val startDateLD: MutableLiveData<String> = MutableLiveData("")
    val endDateLD: MutableLiveData<String> = MutableLiveData("")

    // Button status
    val accFilter: MutableLiveData<Boolean> = MutableLiveData(false)
    val catFilter: MutableLiveData<Boolean> = MutableLiveData(false)
    val dateFilterOld: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _showFilter = MutableStateFlow(false)
    val showFilter: StateFlow<Boolean> get() = _showFilter
    fun updateShowFilter(newValue: Boolean) { _showFilter.value = newValue }

    private val _accountFilter = MutableStateFlow(false)
    val accountFilter: StateFlow<Boolean> get() = _accountFilter
    fun updateAccountFilter(newValue: Boolean) { _accountFilter.value = newValue }

    private val _categoryFilter = MutableStateFlow(false)
    val categoryFilter: StateFlow<Boolean> get() = _categoryFilter
    fun updateCategoryFilter(newValue: Boolean) { _categoryFilter.value = newValue }

    private val _dateFilter = MutableStateFlow(false)
    val dateFilter: StateFlow<Boolean> get() = _dateFilter
    fun updateDateFilter(newValue: Boolean) { _dateFilter.value = newValue }

    private val _accountSelected = MutableStateFlow(listOf<String>())
    val accountSelected: StateFlow<List<String>> get() = _accountSelected
    fun updateAccountSelected(value: String, action: Boolean) {
        _accountSelected.value = if (action) {
            _accountSelected.value + value
        } else {
            _accountSelected.value - value
        }
    }

    private val _expenseCatSelected = MutableStateFlow(listOf<String>())
    val expenseCatSelected: StateFlow<List<String>> get() = _expenseCatSelected
    fun updateExpenseCatSelected(value: String, action: Boolean) {
        _expenseCatSelected.value = if (action) {
            _expenseCatSelected.value + value
        } else {
            _expenseCatSelected.value - value
        }
    }

    private val _incomeCatSelected = MutableStateFlow(listOf<String>())
    val incomeCatSelected: StateFlow<List<String>> get() = _incomeCatSelected
    fun updateIncomeCatSelected(value: String, action: Boolean) {
        _incomeCatSelected.value = if (action) {
            _incomeCatSelected.value + value
        } else {
            _incomeCatSelected.value - value
        }
    }

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

    // used to pass FilterInfo to CFLViewModel
    private val _cflChange = MutableLiveData<Event<Boolean>>()
    val cflChange: LiveData<Event<Boolean>> = _cflChange

    var cflTInfo: FilterInfo = FilterInfo()

    init {
        viewModelScope.launch {
            tranRepo.getAccountNames().collect { list -> updateAccountList(list) }
        }
        viewModelScope.launch {
            tranRepo.getCategoryNamesByType(EXPENSE.type)
                .collect { list -> updateExpenseCatList(list) }
        }
        viewModelScope.launch {
            tranRepo.getCategoryNamesByType(INCOME.type)
                .collect { list -> updateIncomeCatList(list) }
        }
    }

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

        startDateOld = newDate
        startDateLD.value = DateFormat.getDateInstance(DateFormat.SHORT).format(startDateOld)
    }

    /**
     *  Takes [newDate] user selected on End button and saves to be used in query
     */
    fun endDateSelected(newDate: Date) {

        endDateOld = Date(newDate.time + MIDNIGHT_MILLI)
        endDateLD.value = DateFormat.getDateInstance(DateFormat.SHORT).format(endDateOld)
    }

    /**
     *  Applies filters or shows Snackbar warning if no Chips are selected or if end date is before
     *  start date.
     */
    fun applyFilter() {
        when {
            // users must select at least 1 Chip
            accountFilter.value && accountSelected.value.isEmpty() ->
                _filterState.value = NO_SELECTED_ACCOUNT
            categoryFilter.value &&
                    ((typeSelected.value == EXPENSE && expenseCatSelected.value.isEmpty()) ||
                    (typeSelected.value == INCOME && incomeCatSelected.value.isEmpty())) ->
                _filterState.value = NO_SELECTED_CATEGORY
            // user must select both start and end date
            dateFilter.value && startDateString.value.isEmpty() && endDateString.value.isEmpty() ->
                _filterState.value = NO_SELECTED_DATE
            // startDate must be before endDate else it displays warning and doesn't apply filters
            dateFilter.value && startDate > endDate -> _filterState.value = INVALID_DATE_RANGE
            !accountFilter.value && !categoryFilter.value && !dateFilter.value -> resetFilter()
            else -> {
                _filterInfo.value = FilterInfo(
                    account = accountFilter.value,
                    category = categoryFilter.value,
                    date = dateFilter.value,
                    type = typeSelected.value.type,
                    accountNames = accountSelected.value,
                    categoryNames = if (typeSelected.value == EXPENSE) {
                        expenseCatSelected.value
                    } else {
                        incomeCatSelected.value
                    },
                    start = startDate,
                    end = endDate
                )
                _showFilter.value = false
            }
        }
    }

    /**
     *  Resets all filters.
     */
    private fun resetFilter() {
        // clear Chip lists and launch resetEvent to clear Chips
        _accountSelected.value = emptyList()
        _expenseCatSelected.value = emptyList()
        _incomeCatSelected.value = emptyList()

        // sets the startDate to very start of current day and endDate to right before the next day
        startDate = DateUtils.startOfDay(Date())
        endDate = Date(startDate.time + MIDNIGHT_MILLI)
        _startDateString.value = ""
        _endDateString.value = ""

        _filterInfo.value = FilterInfo()
    }
}