package com.heyzeusv.plutuswallet.ui.transaction

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

/**
 *  Data manager for TransactionFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val tranRepo: Repository,
    val setVals: SettingsValues
) : ViewModel() {

    // stores ID of Transaction displayed.
    private val tranIdLD = MutableLiveData<Int>()

    /**
     * Returns LiveData of a Transaction that gets updated every time a
     * new value gets set on the trigger LiveData instance.
     */
    var tranLD: MutableLiveData<Transaction?> =
        Transformations.switchMap(tranIdLD) { transactionId: Int ->
            tranRepo.getLdTransaction(transactionId)
        } as MutableLiveData<Transaction?>

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

    val showDialog = MutableStateFlow(false)

    fun updateShowDialog(newValue: Boolean) {
        showDialog.value = newValue
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

    private val _selectDateEvent = MutableLiveData<Event<Date>>()
    val selectDateEvent: LiveData<Event<Date>> = _selectDateEvent

    private var maxId: Int = 0

    // argument from Navigation
    var newTran = false

    // used to tell if date has been edited for re-repeating Transactions
    private var dateChanged = false

    // Int is type: 0 = Account, 1 = Expense, 2 = Income
    // String is name of newly created entity
    private val _createEvent = MutableLiveData<Event<Pair<Int, String>>>()
    val createEvent: LiveData<Event<Pair<Int, String>>> = _createEvent

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

        tranLD.value!!.let { tran: Transaction ->
            // assigns new id if new Transaction
            if (newTran) tran.id = maxId + 1

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
                if (tran.futureTCreated && dateChanged && tran.repeating) {
                    _futureTranEvent.value = Event(tran)
                } else {
                    // upsert Transaction
                    tranRepo.upsertTransaction(tran)
                    loadTransaction(tran.id)
                    _saveTranEvent.value = Event(true)
                }
            }
        }
    }

    /**
     *  Positive button function for futureTranDialog.
     *  Changes [tran] to recreate its future date and updates it in database.
     */
    fun futureTranPosFun(tran: Transaction) {

        tran.futureTCreated = false
        viewModelScope.launch {
            tranRepo.upsertTransaction(tran)
        }
        _saveTranEvent.value = Event(true)
    }

    /**
     *  Negative button function for futureTranDialog.
     *  Stops warning from appearing again, unless user changes Date again.
     *  Updates [tran] in database.
     */
    fun futureTranNegFun(tran: Transaction) {

        dateChanged = false
        viewModelScope.launch {
            tranRepo.upsertTransaction(tran)
        }
        _saveTranEvent.value = Event(true)
    }

    /**
     *  Returns date from Transaction after adding frequency * period.
     */
    private fun createFutureDate(): Date {

        val calendar: Calendar = Calendar.getInstance()
        tranLD.value?.let {
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
            _createEvent.value = Event(Pair(0, name))
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
            _createEvent.value = Event(Pair(1, name))
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
            _createEvent.value = Event(Pair(2, name))
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
     *  Event to show DatePickerDialog starting at [date].
     */
    fun selectDateOC(date: Date) {

        _selectDateEvent.value = Event(date)
    }

    /**
     *  Takes [newDate] user selects, changes Transaction date, and formats it to be displayed.
     */
    fun onDateSelected(newDate: Date) {

        // true if newDate is different from previous date
        dateChanged = tranLD.value!!.date != newDate
        tranLD.value!!.date = newDate
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
            refresh()
        }
    }

    /**
     *  onClick for type Button. Switches value of typeSelected Boolean.
     */
    fun typeButtonOC() {
        updateTypeSelected(!typeSelected.value)
    }

    // manually refresh on LiveData
    private fun refresh() {

        tranIdLD.postValue(tranIdLD.value)
    }

    /**
     *  Doesn't load Transaction directly from Database, but rather by updating the
     *  LiveData object holding ID with [transactionId] which in turn triggers
     *  mapping function above.
     */
    fun loadTransaction(transactionId: Int) {

        tranIdLD.value = transactionId
    }
}