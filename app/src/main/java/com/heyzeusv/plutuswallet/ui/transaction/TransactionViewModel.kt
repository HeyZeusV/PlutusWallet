package com.heyzeusv.plutuswallet.ui.transaction

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *  Data manager for TransactionFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    state: SavedStateHandle,
    private val tranRepo: Repository,
    val setVals: SettingsValues
) : ViewModel() {

    // arguments from Navigation
    var newTran: Boolean = state["newTran"]!!
    private var tranId: Int = state["tranId"]!!

    private val _tran = MutableStateFlow(Transaction())
    val tran: StateFlow<Transaction> get() = _tran

    // used for various Transaction Fields since property changes don't cause LiveDate updates
    private val _date: MutableLiveData<String> = MutableLiveData("")
    val date: LiveData<String> = _date
    val total: MutableLiveData<String> = MutableLiveData("")

    private val _totalFieldValue = MutableStateFlow(TextFieldValue())
    val totalFieldValue: StateFlow<TextFieldValue> get() = _totalFieldValue
    fun updateTotalFieldValue(newValue: String) {
        
        val removedSymbols = removeSymbols(newValue)
        var formattedTotal = when (setVals.decimalPlaces) {
            true -> formatDecimal(BigDecimal(removedSymbols))
            false -> formatInteger(BigDecimal(removedSymbols))
        }
        formattedTotal = when (setVals.symbolSide) {
            true -> "${setVals.currencySymbol}$formattedTotal"
            false -> "$formattedTotal${setVals.currencySymbol}"
        }
        _totalFieldValue.value = TextFieldValue(formattedTotal, TextRange(formattedTotal.length))
    }

    private val _frequencyFieldValue = MutableStateFlow(TextFieldValue())
    val frequencyFieldValue: StateFlow<TextFieldValue> get() = _frequencyFieldValue
    fun updateFrequencyFieldValue(newValue: String) {
        _frequencyFieldValue.value = TextFieldValue(newValue, TextRange(newValue.length))
    }

    // false = "Expense", true = "Income"
    private val _typeSelected = MutableStateFlow(false)
    val typeSelected: StateFlow<Boolean> get() = _typeSelected
    fun updateTypeSelected(newValue: Boolean) {
        _typeSelected.value = newValue
    }
    val repeatLD: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _repeat = MutableStateFlow(false)
    val repeat: StateFlow<Boolean> get() = _repeat
    fun updateRepeat(newValue: Boolean) { _repeat.value = newValue }

    private val _showInputDialog = MutableStateFlow(false)
    val showInputDialog: StateFlow<Boolean> get() = _showInputDialog
    fun updateInputDialog(newValue: Boolean) {
        _showInputDialog.value = newValue
    }

    private val _showFutureDialog = MutableStateFlow(false)
    val showFutureDialog: StateFlow<Boolean> get() = _showFutureDialog
    private fun updateFutureDialog(newValue: Boolean) {
        _showFutureDialog.value = newValue
    }

    var emptyTitle = ""

    // Lists used by Spinners
    val accountList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val expenseCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val incomeCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val periodArray: MutableLiveData<List<String>> = MutableLiveData(emptyList())

    private val _futureTranEvent: MutableLiveData<Event<Transaction>> =
        MutableLiveData<Event<Transaction>>()
    val futureTranEvent: LiveData<Event<Transaction>> = _futureTranEvent

    private val _saveTranEvent: MutableLiveData<Event<Boolean>> = MutableLiveData<Event<Boolean>>()
    val saveTranEvent: LiveData<Event<Boolean>> = _saveTranEvent

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> get() = _saveSuccess
    fun updateSaveSuccess(newValue: Boolean) { _saveSuccess.value = newValue }

    private val _selectDate = MutableStateFlow(false)
    val selectDate: StateFlow<Boolean> get() = _selectDate
    fun updateSelectDate(newValue: Boolean) {
        _selectDate.value = newValue
    }

    private var maxId: Int = 0

    // used to tell if date has been edited for re-repeating Transactions
    private var dateChanged = false

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> get() = _title
    fun updateTitle(newValue: String) { _title.value = newValue }
    private val _memo = MutableStateFlow("")
    val memo: StateFlow<String> get() = _memo
    fun updateMemo(newValue: String) { _memo.value = newValue }

    // currently selected Spinner item
    private val _account = MutableStateFlow("")
    val account: StateFlow<String> get() = _account
    fun updateAccount(newValue: String) { _account.value = newValue }
    private val _expenseCat = MutableStateFlow("")
    val expenseCat: StateFlow<String> get() = _expenseCat
    fun updateExpenseCat(newValue: String) { _expenseCat.value = newValue }
    private val _incomeCat = MutableStateFlow("")
    val incomeCat: StateFlow<String> get() = _incomeCat
    fun updateIncomeCat(newValue: String) { _incomeCat.value = newValue }
    private val _period = MutableStateFlow("")
    val period: StateFlow<String> get() = _period
    fun updatePeriod(newValue: String) { _period.value = newValue }

    fun retrieveTransaction() {
        viewModelScope.launch {
            tranRepo.getTransactionAsync(tranId)?.let { _tran.value = it }
            setTranData(tran.value)
        }
    }

    /**
     *  Uses [transaction] to pass values to LiveData to be displayed.
     */
    fun setTranData(transaction: Transaction) {
        updateTitle(transaction.title)
        // Date to String
        _date.value =
            DateFormat.getDateInstance(setVals.dateFormat).format(transaction.date)
        updateAccount(transaction.account)
        // BigDecimal to String
        total.value = when {
            setVals.decimalPlaces && transaction.total > BigDecimal.ZERO ->
                formatDecimal(transaction.total)
            setVals.decimalPlaces -> "0${setVals.decimalSymbol}00"
            transaction.total > BigDecimal.ZERO -> formatInteger(transaction.total)
            else -> "0"
        }
        updateTotalFieldValue(transaction.total.toString())
        if (transaction.type == "Expense") {
            updateTypeSelected(false)
            updateExpenseCat(transaction.category)
        } else {
            updateTypeSelected(true)
            updateIncomeCat(transaction.category)
        }
        updateMemo(transaction.memo)
        updateRepeat(transaction.repeating)
        repeatLD.value = transaction.repeating
        periodArray.value?.let {
            // gets translated period value using periodArray
            updatePeriod(when (transaction.period) {
                0 -> it[0]
                1 -> it[1]
                2 -> it[2]
                else -> it[3]
            })
        }
        updateFrequencyFieldValue(transaction.frequency.toString())
    }

    /**
     *  Reassigns LiveData values that couldn't be assigned directly
     *  from Transaction using DataBinding back to Transaction and saves or updates it.
     */
    fun saveTransaction() {
        _tran.value.let { tran: Transaction ->
            // assigns new id if new Transaction
            if (newTran) tran.id = maxId + 1
            tranId = tran.id

            // gives Transaction simple title if user doesn't enter any
            tran.title = title.value.ifBlank { emptyTitle + tran.id }

            // is empty if account hasn't been changed so defaults to first account
            tran.account = if (account.value == "") accountList.value!![0] else account.value

            val totalFromFieldValue = _totalFieldValue.value.text
            tran.total = when {
                totalFromFieldValue.isEmpty() && setVals.decimalPlaces -> BigDecimal("0.00")
                totalFromFieldValue.isEmpty() -> BigDecimal("0")
                else -> BigDecimal(
                    totalFromFieldValue
                        .replace(setVals.currencySymbol, "")
                        .replace("${setVals.thousandsSymbol}", "")
                        .replace("${setVals.decimalPlaces}", ".")
                )
            }

            // sets type depending on Chip selected
            // cat values are empty if they haven't been changed so defaults to first category
            if (!typeSelected.value) {
                tran.type = "Expense"
                tran.category = if (expenseCat.value == "") {
                    expenseCatList.value!![0]
                } else {
                    expenseCat.value
                }
            } else {
                tran.type = "Income"
                tran.category = if (incomeCat.value == "") {
                    incomeCatList.value!![0]
                } else {
                    incomeCat.value
                }
            }

            tran.memo = memo.value

            tran.repeating = repeat.value
            if (tran.repeating) tran.futureDate = createFutureDate()
            tran.period = periodArray.value!!.indexOf(period.value)
            val frequencyFromFieldValue = _frequencyFieldValue.value.text
            // frequency must always be at least 1
            tran.frequency = when {
                frequencyFromFieldValue.isBlank() || frequencyFromFieldValue.toInt() < 1 -> 1
                else -> frequencyFromFieldValue.toInt()
            }

            // Coroutine that Save/Updates/warns user of FutureDate
            viewModelScope.launch {
                withContext(viewModelScope.coroutineContext) {
                    if (tran.futureTCreated && dateChanged && tran.repeating) {
                        updateFutureDialog(true)
                    } else {
                        // upsert Transaction
                        tranRepo.upsertTransaction(tran)
                        updateSaveSuccess(true)
                    }
                }
                setTranData(_tran.value)
            }
        }
    }

    /**
     *  Confirm button function for futureAlertDialog.
     *  Transaction will be able to repeat again.
     */
    fun futureDialogConfirm() {
        _tran.value.let {
            it.futureTCreated = false
            viewModelScope.launch {
                tranRepo.upsertTransaction(it)
            }
        }
        updateSaveSuccess(true)
        updateFutureDialog(false)
    }

    /**
     *  Dismiss button function for futureAlertDialog.
     *  Transaction will no longer repeat in the future.
     *  Stops warning from appearing again, unless user changes Date again.
     */
    fun futureDialogDismiss() {
        dateChanged = false
        _tran.value.let {
            viewModelScope.launch {
                tranRepo.upsertTransaction(it)
            }
        }
        updateSaveSuccess(true)
        updateFutureDialog(false)
    }

    /**
     *  Returns date from Transaction after adding frequency * period.
     */
    private fun createFutureDate(): Date {

        val calendar: Calendar = Calendar.getInstance()
        _tran.value.let {
            // set to Transaction date rather than current time due to Users being able
            // to select a Date in the past or future
            calendar.time = it.date

            // 0 = Day, 1 = Week, 2 = Month, 3 = Year
            calendar.add(
                when (it.period) {
                    0 -> Calendar.DAY_OF_MONTH
                    1 -> Calendar.WEEK_OF_YEAR
                    2 -> Calendar.MONTH
                    else -> Calendar.YEAR
                },
                it.frequency
            )
        }

        return calendar.time
    }

    private fun removeSymbols(numString: String): String {

        var chars = ""
        // retrieves only numbers in numString
        for (c: Char in numString) { if (c.isDigit()) chars += c }

        return when {
            // doesn't allow string to be empty
            setVals.decimalPlaces && chars.isBlank() -> "0.00"
            // divides numbers by 100 in order to easily get decimal places
            setVals.decimalPlaces -> BigDecimal(chars)
                .divide(BigDecimal(100), 2, RoundingMode.HALF_UP).toString()
            // doesn't allow string to be empty
            chars.isBlank() -> "0"
            // returns just a string of numbers
            else -> chars
        }
    }

    /**
     *  Returns formatted [num] in integer form.
     */
    private fun formatInteger(num: BigDecimal): String {

        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.groupingSeparator = setVals.thousandsSymbol
        // every three numbers, a thousands symbol will be added
        val formatter = DecimalFormat("#,###", customSymbols)
        formatter.roundingMode = RoundingMode.HALF_UP
        return formatter.format(num)
    }

    /**
     *  Returns formatted [num] in decimal form.
     */
    private fun formatDecimal(num: BigDecimal): String {

        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.groupingSeparator = setVals.thousandsSymbol
        customSymbols.decimalSeparator = setVals.decimalSymbol
        // every three numbers, a thousands symbol will be added
        val formatter = DecimalFormat("#,##0.00", customSymbols)
        formatter.roundingMode = RoundingMode.HALF_UP
        return formatter.format(num)
    }

    /**
     *  Creates Account with [name] or selects it in Spinner if it exists already.
     *  [accCreate] ("Create New..." translated) is added after resorting list.
     */
    fun insertAccount(name: String, accCreate: String) {
        accountList.value?.let {
            // create if doesn't exist
            if (!it.contains(name)) {
                viewModelScope.launch {
                    // creates and inserts new Account with name
                    val account = Account(0, name)
                    tranRepo.insertAccount(account)
                }
                accountList.value = addNewToList(it, name, accCreate)
            }
            updateAccount(name)
        }
    }

    /**
     *  Creates Category with [name] or selects it in Spinner if it exists already.
     *  [catCreate] ("Create New..." translated) is added after resorting list.
     */
    fun insertCategory(name: String, catCreate: String) {
        // checks which type is currently selected
        if (!typeSelected.value) {
            expenseCatList.value?.let {
                // create if doesn't exist
                if (!it.contains(name)) {
                    viewModelScope.launch {
                        // creates and inserts new Category with name
                        val category = Category(0, name, "Expense")
                        tranRepo.insertCategory(category)
                    }
                    expenseCatList.value = addNewToList(it, name, catCreate)
                }
            }
            updateExpenseCat(name)
        } else {
            incomeCatList.value?.let {
                // create if doesn't exist
                if (!it.contains(name)) {
                    viewModelScope.launch {
                        // creates and inserts new Category with name
                        val category = Category(0, name, "Income")
                        tranRepo.insertCategory(category)
                    }
                    incomeCatList.value = addNewToList(it, name, catCreate)
                }
            }
            updateIncomeCat(name)
        }
    }

    /**
     *  Takes given [list], removes "Create New..", adds new entry with [name], sorts list, re-adds
     *  [create] ("Create New.." translated), and returns list.
     */
    private fun addNewToList(list: MutableList<String>, name: String, create: String)
            : MutableList<String> {

        list.remove(create)
        list.add(name)
        list.sort()
        list.add(create)
        return list
    }

    /**
     *  Takes [newDate] user selects, changes Transaction date, and formats it to be displayed.
     */
    fun onDateSelected(newDate: Date) {

        // true if newDate is different from previous date
        dateChanged = _tran.value.date != newDate
        _tran.value.date = newDate
        // turns date selected into Date type
        _date.value = DateFormat.getDateInstance(setVals.dateFormat).format(newDate)
    }

    /**
     *  Retrieves list of Accounts/Categories from Database,
     *  adds [accCreate]/[catCreate] ("Create New..." translated),
     *  and retrieves highest ID from database, then refreshes tranIdLd
     */
    fun prepareLists(accCreate: String, catCreate: String) {

        viewModelScope.launch {
            accountList.value = tranRepo.getAccountNamesAsync()
            accountList.value!!.add(accCreate)
            expenseCatList.value = tranRepo.getCategoryNamesByTypeAsync("Expense")
            expenseCatList.value!!.add(catCreate)
            incomeCatList.value = tranRepo.getCategoryNamesByTypeAsync("Income")
            incomeCatList.value!!.add(catCreate)
            maxId = tranRepo.getMaxIdAsync() ?: 0
        }
    }

    /**
     *  onClick for type Button. Switches value of typeSelected Boolean.
     */
    fun typeButtonOC() {
        updateTypeSelected(!typeSelected.value)
    }
//
//    init {
//        Timber.v(tranId.toString())
//        Timber.v("_tran ${_tran.value}")
//
////            _tran.value = viewModelScope.launch {
////                tranRepo.getTransactionAsync(tranId)
////            }
////        }
////         viewModelScope.launch {
////            val test = withContext(Dispatchers.IO) {
////                tranRepo.getTransactionAsync(tranId)
////            }
////            Timber.v(test.toString())
////        }
//    }
}