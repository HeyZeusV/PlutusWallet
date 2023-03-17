package com.heyzeusv.plutuswallet.ui.transaction

import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.TransactionType
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.createFutureDate
import com.heyzeusv.plutuswallet.util.formatDate
import com.heyzeusv.plutuswallet.util.prepareTotalText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZonedDateTime
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
    private val tranRepo: PWRepositoryInterface
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
    fun updateTypeSelected(newValue: TransactionType) {
        _typeSelected.value = newValue
        updateSelectedCat()
        updateSelectedCatList()
    }

    private var expenseCat = ""
    private var incomeCat = ""
    private val _selectedCat = MutableStateFlow("")
    val selectedCat: StateFlow<String> get() = _selectedCat
    private fun updateSelectedCat() {
        _selectedCat.value = if (typeSelected.value == EXPENSE) expenseCat else incomeCat
    }
    fun updateSelectedCat(newValue: String) {
        _selectedCat.value = newValue
        if (_typeSelected.value == EXPENSE) {
            expenseCat = newValue
        } else {
            incomeCat = newValue
        }
    }

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

    // Lists used by DropDownMenus
    private val _accountList = MutableStateFlow(listOf(""))
    val accountList: StateFlow<List<String>> get() = _accountList
    fun updateAccountList(newList: List<String>) {
        _accountList.value = newList + listOf(accountCreate)
    }

    private var expenseCatList = listOf("")
    private fun updateExpenseCatList(newList: List<String>) {
        expenseCatList = newList
        updateSelectedCatList()
    }

    private var incomeCatList = listOf("")
    private fun updateIncomeCatList(newList: List<String>) {
        incomeCatList = newList
        updateSelectedCatList()
    }

    private val _selectedCatList = MutableStateFlow(listOf(""))
    val selectedCatList: StateFlow<List<String>> get() = _selectedCatList
    private fun updateSelectedCatList() {
        _selectedCatList.value = if (typeSelected.value == EXPENSE) {
            expenseCatList
        } else {
            incomeCatList
        } + listOf(categoryCreate)
    }

    private val _periodList = MutableStateFlow(mutableListOf(""))
    val periodList: StateFlow<MutableList<String>> get() = _periodList
    fun updatePeriodList(newList: MutableList<String>) { _periodList.value = newList }


    // determines when to show AlertDialogs
    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog: StateFlow<Boolean> get() = _showAccountDialog
    fun updateAccountDialog(newValue: Boolean) { _showAccountDialog.value = newValue }

    private val _showCategoryDialog = MutableStateFlow(false)
    val showCategoryDialog: StateFlow<Boolean> get() = _showCategoryDialog
    fun updateCategoryDialog(newValue: Boolean) { _showCategoryDialog.value = newValue }

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
        updateDate(formatDate(transaction.date, setVals.dateFormat))
        updateAccount(transaction.account)
        updateTotal(transaction.total.toString())
        updateTypeSelected(if (transaction.type == EXPENSE.type) EXPENSE else INCOME)
        updateSelectedCat(transaction.category)
        updateMemo(transaction.memo)
        updateRepeat(transaction.repeating)
        updatePeriod(periodList.value[transaction.period])
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
            tran.category = selectedCat.value.ifBlank { selectedCatList.value[0] }

            tran.memo = memo.value

            tran.repeating = repeat.value
            if (tran.repeating) {
                tran.futureDate = createFutureDate(tran.date, tran.period, tran.frequency)
            }
            tran.period = periodList.value.indexOf(period.value)
            val frequencyValue = frequency.value.text
            // frequency must always be at least 1
            tran.frequency = when {
                frequencyValue.isBlank() || frequencyValue.toInt() < 1 -> 1
                else -> frequencyValue.toInt()
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
                expenseCatList.let {
                    // create if doesn't exist
                    if (!it.contains(name)) {
                        viewModelScope.launch {
                            // creates and inserts new Category with name
                            val category = Category(0, name, typeSelected.value.type)
                            tranRepo.insertCategory(category)
                        }
                    }
                }
            }
            INCOME -> {
                incomeCatList.let {
                    // create if doesn't exist
                    if (!it.contains(name)) {
                        viewModelScope.launch {
                            // creates and inserts new Category with name
                            val category = Category(0, name, typeSelected.value.type)
                            tranRepo.insertCategory(category)
                        }
                    }
                }
            }
        }
        updateSelectedCat(name)
        updateCategoryDialog(false)
    }

    /**
     *  Takes [newDate] user selects, changes Transaction date, and formats it to be displayed.
     */
    fun onDateSelected(newDate: ZonedDateTime) {
        // true if newDate is different from previous date
        dateChanged = _transaction.value.date.isEqual(newDate).not()
        _transaction.value.date = newDate
        // turns date selected into Date type
        updateDate(formatDate(newDate, setVals.dateFormat))
    }
}

/**
 *  Update SettingsValues in TransactionViewModel with updated [sv].
 *
 *  TransactionViewModel requires several translated strings, but I don't want to have it hold
 *  context in order to get string resources. This extension function retrieves all strings
 *  required using the provided [context].
 */
fun TransactionViewModel.tranVMSetup(sv: SettingsValues, context: Context) {
    this.apply {
        setVals = sv

        emptyTitle = context.getString(R.string.transaction_empty_title)
        accountCreate = context.getString(R.string.account_create)
        categoryCreate = context.getString(R.string.category_create)
        // array used by Period DropDownMenu
        updatePeriodList(
            mutableListOf(
                context.getString(R.string.period_days), context.getString(R.string.period_weeks),
                context.getString(R.string.period_months), context.getString(R.string.period_years)
            )
        )
    }
}