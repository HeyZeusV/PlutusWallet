package com.heyzeusv.plutuswallet.ui.transaction

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
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
    var newTran: Boolean = state["newTran"] ?: false
    private var tranId: Int = state["tranId"] ?: 0

    // string resource received from Fragment
    var emptyTitle = ""
    // highest Transaction.id in DB
    private var maxId: Int = 0
    // used to tell if date has been edited for re-repeating Transactions
    private var dateChanged = false

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

    private val _typeSelected = MutableStateFlow(TransactionType.EXPENSE)
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

    private val _frequencyFieldValue = MutableStateFlow(TextFieldValue())
    val frequencyFieldValue: StateFlow<TextFieldValue> get() = _frequencyFieldValue
    fun updateFrequencyFieldValue(newValue: String) {
        _frequencyFieldValue.value = TextFieldValue(newValue, TextRange(newValue.length))
    }


    // Lists used by Spinners
    private val _accountList = MutableStateFlow(mutableListOf(""))
    val accountList: StateFlow<MutableList<String>> get() = _accountList
    fun updateAccountList(newList: MutableList<String>) { _accountList.value = newList }

    private val _expenseCatList = MutableStateFlow(mutableListOf(""))
    val expenseCatList: StateFlow<MutableList<String>> get() = _expenseCatList
    fun updateExpenseCatList(newList: MutableList<String>) { _expenseCatList.value = newList }

    private val _incomeCatList = MutableStateFlow(mutableListOf(""))
    val incomeCatList: StateFlow<MutableList<String>> get() = _incomeCatList
    fun updateIncomeCatList(newList: MutableList<String>) { _incomeCatList.value = newList }

    private val _periodList = MutableStateFlow(mutableListOf(""))
    val periodList: StateFlow<MutableList<String>> get() = _periodList
    fun updatePeriodList(newList: MutableList<String>) { _periodList.value = newList }


    // determines when to show DatePicker
    private val _showDateDialog = MutableStateFlow(false)
    val showDateDialog: StateFlow<Boolean> get() = _showDateDialog
    fun updateDateDialog(newValue: Boolean) { _showDateDialog.value = newValue }

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

    /**
     *  Checks to see if a Transaction with tranId exists, if it does then it retrieves that data
     *  and places it in _tran to populate fields.
     *  If it doesn't exist, a new Transaction is used.
     */
    fun retrieveTransaction() {
        viewModelScope.launch {
            tranRepo.getTransactionAsync(tranId)?.let { _transaction.value = it }
            setTranData(transaction.value)
        }
    }

    /**
     *  Uses [transaction] to pass values to StateFlow to be displayed.
     */
    fun setTranData(transaction: Transaction) {
        updateTitle(transaction.title)
        updateDate(DateFormat.getDateInstance(setVals.dateFormat).format(transaction.date))
        updateAccount(transaction.account)
        updateTotalFieldValue(transaction.total.toString())
        if (transaction.type == "Expense") {
            updateTypeSelected(TransactionType.EXPENSE)
            updateExpenseCat(transaction.category)
        } else {
            updateTypeSelected(TransactionType.INCOME)
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
        updateFrequencyFieldValue(transaction.frequency.toString())
    }

    /**
     *  Reassigns LiveData values that couldn't be assigned directly
     *  from Transaction using DataBinding back to Transaction and saves or updates it.
     */
    fun saveTransaction() {
        transaction.value.let { tran: Transaction ->
            // assigns new id if new Transaction
            if (newTran) tran.id = maxId + 1
            tranId = tran.id

            // gives Transaction simple title if user doesn't enter any
            tran.title = title.value.ifBlank { emptyTitle + tran.id }

            // is empty if account hasn't been changed so defaults to first account
            tran.account = account.value.ifBlank { accountList.value[0] }

            val totalFromFieldValue = totalFieldValue.value.text
            tran.total = when {
                totalFromFieldValue.isEmpty() && setVals.decimalPlaces -> BigDecimal("0.00")
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
                TransactionType.EXPENSE -> expenseCat.value.ifBlank { expenseCatList.value[0] }
                TransactionType.INCOME -> incomeCat.value.ifBlank { incomeCatList.value[0] }
            }

            tran.memo = memo.value

            tran.repeating = repeat.value
            if (tran.repeating) tran.futureDate = createFutureDate()
            tran.period = periodList.value.indexOf(period.value)
            val frequencyFromFieldValue = frequencyFieldValue.value.text
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
        accountList.value.let {
            // create if doesn't exist
            if (!it.contains(name)) {
                viewModelScope.launch {
                    // creates and inserts new Account with name
                    val account = Account(0, name)
                    tranRepo.insertAccount(account)
                }
                updateAccountList(addNewToList(it, name, accCreate))
            }
            updateAccount(name)
        }
        updateAccountDialog(false)
    }

    /**
     *  Creates Category with [name] or selects it in Spinner if it exists already.
     *  [catCreate] ("Create New..." translated) is added after resorting list.
     */
    fun insertCategory(name: String, catCreate: String) {
        // checks which type is currently selected
        when (typeSelected.value) {
            TransactionType.EXPENSE -> {
                expenseCatList.value.let {
                    // create if doesn't exist
                    if (!it.contains(name)) {
                        viewModelScope.launch {
                            // creates and inserts new Category with name
                            val category = Category(0, name, typeSelected.value.type)
                            tranRepo.insertCategory(category)
                        }
                        updateExpenseCatList(addNewToList(it, name, catCreate))
                    }
                }
                updateExpenseCat(name)
                updateExpenseDialog(false)
            }
            TransactionType.INCOME -> {
                incomeCatList.value.let {
                    // create if doesn't exist
                    if (!it.contains(name)) {
                        viewModelScope.launch {
                            // creates and inserts new Category with name
                            val category = Category(0, name, typeSelected.value.type)
                            tranRepo.insertCategory(category)
                        }
                        updateIncomeCatList(addNewToList(it, name, catCreate))
                    }
                }
                updateIncomeCat(name)
                updateIncomeDialog(false)
            }
        }
    }

    /**
     *  Takes given [list], removes "Create New..", adds new entry with [name], sorts list, re-adds
     *  [create] ("Create New.." translated), and returns list.
     */
    private fun addNewToList(
        list: MutableList<String>,
        name: String,
        create: String
    ): MutableList<String> {
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
        dateChanged = _transaction.value.date != newDate
        _transaction.value.date = newDate
        // turns date selected into Date type
        updateDate(DateFormat.getDateInstance(setVals.dateFormat).format(newDate))
    }

    /**
     *  Retrieves list of Accounts/Categories from Database,
     *  adds [accCreate]/[catCreate] ("Create New..." translated),
     *  and retrieves highest ID from database, then refreshes tranIdLd
     */
    fun prepareLists(accCreate: String, catCreate: String) {
        viewModelScope.launch {
            updateAccountList(tranRepo.getAccountNamesAsync())
            accountList.value.add(accCreate)
            updateExpenseCatList(tranRepo.getCategoryNamesByTypeAsync("Expense"))
            expenseCatList.value.add(catCreate)
            updateIncomeCatList(tranRepo.getCategoryNamesByTypeAsync("Income"))
            incomeCatList.value.add(catCreate)
            maxId = tranRepo.getMaxIdAsync() ?: 0
        }
    }
}