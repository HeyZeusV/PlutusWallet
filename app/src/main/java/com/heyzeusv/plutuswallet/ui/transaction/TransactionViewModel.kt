package com.heyzeusv.plutuswallet.ui.transaction

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.TransactionType
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.prepareTotalText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.Date
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
    private val tranRepo: Repository
) : ViewModel() {

    var setVals = SettingsValues()

    // string resources received from MainActivity
    var emptyTitle = ""
    var accountCreate = "Create New Account"
    var categoryCreate = "Create New Category"
    // highest Transaction.id in DB
    private var maxId: Int = 0
    // used to tell if date has been edited for re-repeating Transactions
    private var dateChanged = false

    var retrieveTransaction = true

    private val _transaction = MutableStateFlow(Transaction())
    val transaction: StateFlow<Transaction> get() = _transaction

    // variables for fields appear in same order as they do on screen
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> get() = _title
    fun updateTitle(newValue: String) { _title.value = newValue }

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> get() = _date
    fun updateDate(newValue: String) { _date.value = newValue }

    private val _account = MutableStateFlow("")
    val account: StateFlow<String> get() = _account
    fun updateAccount(newValue: String) { _account.value = newValue }

    private val _total = MutableStateFlow(TextFieldValue())
    val total: StateFlow<TextFieldValue> get() = _total
    fun updateTotal(newValue: String) {
        val removedSymbols = BigDecimal(removeSymbols(newValue))
        val formattedTotal = removedSymbols.prepareTotalText(setVals)
        _total.value = TextFieldValue(formattedTotal, TextRange(formattedTotal.length))
    }

    private val _typeSelected = MutableStateFlow(EXPENSE)
    val typeSelected: StateFlow<TransactionType> get() = _typeSelected
    fun updateTypeSelected(newValue: TransactionType) { _typeSelected.value = newValue }

    private val _expenseCat = MutableStateFlow("")
    val expenseCat: StateFlow<String> get() = _expenseCat
    fun updateExpenseCat(newValue: String) { _expenseCat.value = newValue }

    private val _incomeCat = MutableStateFlow("")
    val incomeCat: StateFlow<String> get() = _incomeCat
    fun updateIncomeCat(newValue: String) { _incomeCat.value = newValue }

    private val _memo = MutableStateFlow("")
    val memo: StateFlow<String> get() = _memo
    fun updateMemo(newValue: String) { _memo.value = newValue }

    private val _repeat = MutableStateFlow(false)
    val repeat: StateFlow<Boolean> get() = _repeat
    fun updateRepeat(newValue: Boolean) { _repeat.value = newValue }

    private val _period = MutableStateFlow("")
    val period: StateFlow<String> get() = _period
    fun updatePeriod(newValue: String) { _period.value = newValue }

    private val _frequency = MutableStateFlow(TextFieldValue())
    val frequency: StateFlow<TextFieldValue> get() = _frequency
    fun updateFrequency(newValue: String) {
        _frequency.value = TextFieldValue(newValue, TextRange(newValue.length))
    }

    // Lists used by Spinners
    private val _accountList = MutableStateFlow(listOf(""))
    val accountList: StateFlow<List<String>> get() = _accountList
    fun updateAccountList(newList: List<String>) {
        _accountList.value = newList + listOf(accountCreate)
    }

    private val _expenseCatList = MutableStateFlow(listOf(""))
    val expenseCatList: StateFlow<List<String>> get() = _expenseCatList
    fun updateExpenseCatList(newList: List<String>) {
        _expenseCatList.value = newList + listOf(categoryCreate)
    }

    private val _incomeCatList = MutableStateFlow(listOf(""))
    val incomeCatList: StateFlow<List<String>> get() = _incomeCatList
    fun updateIncomeCatList(newList: List<String>) {
        _incomeCatList.value = newList + listOf(categoryCreate)
    }

    private val _periodList = MutableStateFlow(mutableListOf(""))
    val periodList: StateFlow<MutableList<String>> get() = _periodList
    fun updatePeriodList(newList: MutableList<String>) { _periodList.value = newList }


    // determines when to show AlertDialogs
    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog: StateFlow<Boolean> get() = _showAccountDialog
    fun updateAccountDialog(newValue: Boolean) { _showAccountDialog.value = newValue }

    private val _showExpenseDialog = MutableStateFlow(false)
    val showExpenseDialog: StateFlow<Boolean> get() = _showExpenseDialog
    fun updateExpenseDialog(newValue: Boolean) { _showExpenseDialog.value = newValue }

    private val _showIncomeDialog = MutableStateFlow(false)
    val showIncomeDialog: StateFlow<Boolean> get() = _showIncomeDialog
    fun updateIncomeDialog(newValue: Boolean) { _showIncomeDialog.value = newValue }

    private val _showFutureDialog = MutableStateFlow(false)
    val showFutureDialog: StateFlow<Boolean> get() = _showFutureDialog
    fun updateFutureDialog(newValue: Boolean) { _showFutureDialog.value = newValue }

    // determines when to show save SnackBar
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> get() = _saveSuccess
    fun updateSaveSuccess(newValue: Boolean) { _saveSuccess.value = newValue }

    init {
        viewModelScope.launch {
            tranRepo.getMaxId().collect { maxId = it ?: 0 }
        }
        viewModelScope.launch {
            tranRepo.getAccountNames().collect { updateAccountList(it) }
        }
        viewModelScope.launch {
            tranRepo.getCategoryNamesByType(EXPENSE.type).collect { updateExpenseCatList(it) }
        }
        viewModelScope.launch {
            tranRepo.getCategoryNamesByType(INCOME.type).collect { updateIncomeCatList(it) }
        }
    }
    /**
     *  Checks to see if a Transaction with tranId exists, if it does then it retrieves that data
     *  and places it in _tran to populate fields.
     *  If it doesn't exist, a new Transaction is used.
     */
    fun retrieveTransaction(tranId: Int) {
        viewModelScope.launch {
            tranRepo.getTransactionAsync(tranId).let {
                _transaction.value = it ?: Transaction()
            }
            setTranData(transaction.value)
            retrieveTransaction = false
        }
    }

    /**
     *  Uses [transaction] to pass values to StateFlow to be displayed.
     */
    fun setTranData(transaction: Transaction) {
        updateTitle(transaction.title)
        updateDate(setVals.dateFormatter.format(transaction.date))
        updateAccount(transaction.account)
        updateTotal(transaction.total.toString())
        if (transaction.type == EXPENSE.type) {
            updateTypeSelected(EXPENSE)
            updateExpenseCat(transaction.category)
        } else {
            updateTypeSelected(INCOME)
            updateIncomeCat(transaction.category)
        }
        updateMemo(transaction.memo)
        updateRepeat(transaction.repeating)
        periodList.value.let {
            // gets translated period value using periodArray
            updatePeriod(when (transaction.period) {
                0 -> it[0]
                1 -> it[1]
                2 -> it[2]
                else -> it[3]
            })
        }
        updateFrequency(transaction.frequency.toString())
    }

    /**
     *  Save values from composables to Transaction object.
     */
    fun saveTransaction() {
        transaction.value.let { tran: Transaction ->
            // assigns new id if new Transaction
            if (tran.id == 0) {
                tran.id = maxId + 1
            }

            // gives Transaction simple title if user doesn't enter any
            tran.title = title.value.ifBlank { emptyTitle + tran.id }

            // is empty if account hasn't been changed so defaults to first account
            tran.account = account.value.ifBlank { accountList.value[0] }

            val totalFromFieldValue = total.value.text
            tran.total = when {
                totalFromFieldValue.isEmpty() && setVals.decimalNumber == "yes" ->
                    BigDecimal("0.00")
                totalFromFieldValue.isEmpty() -> BigDecimal("0")
                else -> BigDecimal(
                    totalFromFieldValue
                        .replace(setVals.currencySymbol, "")
                        .replace("${setVals.thousandsSymbol}", "")
                        .replace("${setVals.decimalSymbol}", ".")
                )
            }

            tran.type = typeSelected.value.type
            // cat values are empty if they haven't been changed so defaults to first category
            tran.category = when (typeSelected.value) {
                EXPENSE -> expenseCat.value.ifBlank { expenseCatList.value[0] }
                INCOME -> incomeCat.value.ifBlank { incomeCatList.value[0] }
            }

            tran.memo = memo.value

            tran.repeating = repeat.value
            if (tran.repeating) tran.futureDate = createFutureDate()
            tran.period = periodList.value.indexOf(period.value)
            val frequencyFromFieldValue = frequency.value.text
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
                setTranData(transaction.value)
            }
        }
    }

    /**
     *  Confirm button function for futureAlertDialog.
     *  Transaction will be able to repeat again.
     */
    fun futureDialogConfirm() {
        transaction.value.let {
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
        transaction.value.let {
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
        transaction.value.let {
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

    /**
     *  Removes thousands symbols and replaces decimal symbol with '.'
     *  if decimalPlaces is true from [numString]
     */
    private fun removeSymbols(numString: String): String {
        var chars = ""
        // retrieves only numbers in numString
        for (c: Char in numString) { if (c.isDigit()) chars += c }

        return when {
            // doesn't allow string to be empty
            setVals.decimalNumber == "yes" && chars.isBlank() -> "0.00"
            // divides numbers by 100 in order to easily get decimal places
            setVals.decimalNumber == "yes" -> BigDecimal(chars)
                .divide(BigDecimal(100), 2, RoundingMode.HALF_UP).toString()
            // doesn't allow string to be empty
            chars.isBlank() -> "0"
            // returns just a string of numbers
            else -> chars
        }
    }

    /**
     *  Creates Account with [name] or selects it in Spinner if it exists already.
     */
    fun insertAccount(name: String) {
        accountList.value.let {
            // create if doesn't exist
            if (!it.contains(name)) {
                viewModelScope.launch {
                    // creates and inserts new Account with name
                    val account = Account(0, name)
                    tranRepo.insertAccount(account)
                }
            }
            updateAccount(name)
        }
        updateAccountDialog(false)
    }

    /**
     *  Creates Category with [name] or selects it in Spinner if it exists already.
     */
    fun insertCategory(name: String) {
        // checks which type is currently selected
        when (typeSelected.value) {
            EXPENSE -> {
                expenseCatList.value.let {
                    // create if doesn't exist
                    if (!it.contains(name)) {
                        viewModelScope.launch {
                            // creates and inserts new Category with name
                            val category = Category(0, name, typeSelected.value.type)
                            tranRepo.insertCategory(category)
                        }
                    }
                }
                updateExpenseCat(name)
                updateExpenseDialog(false)
            }
            INCOME -> {
                incomeCatList.value.let {
                    // create if doesn't exist
                    if (!it.contains(name)) {
                        viewModelScope.launch {
                            // creates and inserts new Category with name
                            val category = Category(0, name, typeSelected.value.type)
                            tranRepo.insertCategory(category)
                        }
                    }
                }
                updateIncomeCat(name)
                updateIncomeDialog(false)
            }
        }
    }

    /**
     *  Takes [newDate] user selects, changes Transaction date, and formats it to be displayed.
     */
    fun onDateSelected(newDate: Date) {
        // true if newDate is different from previous date
        dateChanged = _transaction.value.date != newDate
        _transaction.value.date = newDate
        // turns date selected into Date type
        updateDate(setVals.dateFormatter.format(newDate))
    }
}