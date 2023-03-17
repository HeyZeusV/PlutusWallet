package com.heyzeusv.plutuswallet.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.util.FilterChipAction
import com.heyzeusv.plutuswallet.util.FilterChipAction.ADD
import com.heyzeusv.plutuswallet.util.FilterChipAction.REMOVE
import com.heyzeusv.plutuswallet.util.FilterState
import com.heyzeusv.plutuswallet.util.FilterState.INVALID_DATE_RANGE
import com.heyzeusv.plutuswallet.util.FilterState.NO_SELECTED_ACCOUNT
import com.heyzeusv.plutuswallet.util.FilterState.NO_SELECTED_CATEGORY
import com.heyzeusv.plutuswallet.util.FilterState.NO_SELECTED_DATE
import com.heyzeusv.plutuswallet.util.FilterState.VALID
import com.heyzeusv.plutuswallet.util.TransactionType
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.endOfDay
import com.heyzeusv.plutuswallet.util.formatDate
import com.heyzeusv.plutuswallet.util.startOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.time.format.FormatStyle.SHORT
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
@HiltViewModel
class FilterViewModel @Inject constructor(
    private val tranRepo: PWRepositoryInterface
) : ViewModel() {

    private val _showFilter = MutableStateFlow(false)
    val showFilter: StateFlow<Boolean> get() = _showFilter
    fun updateShowFilter(newValue: Boolean) { _showFilter.value = newValue }

    // chip lists
    private val _accountList = MutableStateFlow(listOf<String>())
    val accountList: StateFlow<List<String>> get() = _accountList
    private fun updateAccountList(newList: List<String>) { _accountList.value = newList }

    private var expenseCatList = listOf<String>()
    private fun updateExpenseCatList(newList: List<String>) {
        expenseCatList = newList
        updateCategoryList()
    }

    private var incomeCatList = listOf<String>()
    private fun updateIncomeCatList(newList: List<String>) {
        incomeCatList = newList
        updateCategoryList()
    }

    private val _categoryList = MutableStateFlow(listOf<String>())
    val categoryList: StateFlow<List<String>> get() = _categoryList
    private fun updateCategoryList() {
        _categoryList.value = if (typeSelected.value == EXPENSE) {
            expenseCatList
        } else {
            incomeCatList
        }
    }

    private val _typeSelected = MutableStateFlow(EXPENSE)
    val typeSelected: StateFlow<TransactionType> get() = _typeSelected
    fun updateTypeSelected(newValue: TransactionType) {
        _typeSelected.value = newValue
        updateCategoryList()
        updateCategorySelectedList()
    }

    // filter status
    private val _accountFilter = MutableStateFlow(false)
    val accountFilter: StateFlow<Boolean> get() = _accountFilter
    fun updateAccountFilter(newValue: Boolean) { _accountFilter.value = newValue }

    private val _categoryFilter = MutableStateFlow(false)
    val categoryFilter: StateFlow<Boolean> get() = _categoryFilter
    fun updateCategoryFilter(newValue: Boolean) { _categoryFilter.value = newValue }

    private val _dateFilter = MutableStateFlow(false)
    val dateFilter: StateFlow<Boolean> get() = _dateFilter
    fun updateDateFilter(newValue: Boolean) { _dateFilter.value = newValue }

    // selected items
    private val _accountSelected = MutableStateFlow(listOf<String>())
    val accountSelected: StateFlow<List<String>> get() = _accountSelected
    fun updateAccountSelected(value: String, action: FilterChipAction) {
        _accountSelected.value = when (action) {
            ADD -> _accountSelected.value + value
            REMOVE -> _accountSelected.value - value
        }
    }

    // selected chips
    private var expenseCatSelectedList = listOf<String>()
    private var incomeCatSelectedList = listOf<String>()
    private val _categorySelectedList = MutableStateFlow(listOf<String>())
    val categorySelectedList: StateFlow<List<String>> get() = _categorySelectedList
    private fun updateCategorySelectedList() {
        if (typeSelected.value == EXPENSE) {
            incomeCatSelectedList = categorySelectedList.value
            _categorySelectedList.value = expenseCatSelectedList
        } else {
            expenseCatSelectedList = categorySelectedList.value
            _categorySelectedList.value = incomeCatSelectedList
        }
    }
    fun updateCategorySelectedList(value: String, action: FilterChipAction) {
        _categorySelectedList.value = when (action) {
            ADD -> _categorySelectedList.value + value
            REMOVE -> _categorySelectedList.value - value
        }
    }

    private var startDate: ZonedDateTime = startOfDay(ZonedDateTime.now(systemDefault()))
    private var endDate: ZonedDateTime = endOfDay(startDate)

    // Date string values
    private val _startDateString = MutableStateFlow("")
    val startDateString: StateFlow<String> get() = _startDateString
    fun updateStartDateString(newDate: ZonedDateTime) {
        startDate = newDate
        _startDateString.value = formatDate(startDate, SHORT)
    }

    private val _endDateString = MutableStateFlow("")
    val endDateString: StateFlow<String> get() = _endDateString
    fun updateEndDateString(newDate: ZonedDateTime) {
        endDate = endOfDay(newDate)
        _endDateString.value = formatDate(endDate, SHORT)
    }

    private val _filterInfo = MutableStateFlow(FilterInfo())
    val filterInfo: StateFlow<FilterInfo> get() = _filterInfo

    private val _filterState = MutableStateFlow(VALID)
    val filterState: StateFlow<FilterState> get() = _filterState
    fun updateFilterState(newState: FilterState) { _filterState.value = newState }

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
     *  Applies filters or shows Snackbar warning if no Chips are selected or if end date is before
     *  start date.
     */
    fun applyFilter() {
        when {
            // users must select at least 1 Chip
            accountFilter.value && accountSelected.value.isEmpty() ->
                _filterState.value = NO_SELECTED_ACCOUNT
            categoryFilter.value && categorySelectedList.value.isEmpty() ->
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
                    accountNames = accountSelected.value,
                    category = categoryFilter.value,
                    type = typeSelected.value.type,
                    categoryNames = categorySelectedList.value,
                    date = dateFilter.value,
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
        expenseCatSelectedList = emptyList()
        incomeCatSelectedList = emptyList()
        _categorySelectedList.value = emptyList()

        // sets the startDate to very start of current day and endDate to right before the next day
        startDate = startOfDay(ZonedDateTime.now(systemDefault()))
        endDate = endOfDay(startDate)
        _startDateString.value = ""
        _endDateString.value = ""

        _filterInfo.value = FilterInfo()
    }
}