package com.heyzeusv.plutuswallet.ui.transaction

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.TransactionRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 *  Data manager for TransactionFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class TransactionViewModel @ViewModelInject constructor(
    private val tranRepo: TransactionRepository
) : ViewModel() {

    // stores ID of Transaction displayed.
    private val tranIdLD = MutableLiveData<Int>()

    /**
     * Returns LiveData of a Transaction that gets updated every time a
     * new value gets set on the trigger LiveData instance.
     */
    var tranLD: MutableLiveData<Transaction?> =
        Transformations.switchMap(tranIdLD) { transactionId: Int ->
            tranRepo.getLDTransaction(transactionId)
        } as MutableLiveData<Transaction?>

    // used for various Transaction Fields since property changes don't cause LiveDate updates
    private val _date: MutableLiveData<String> = MutableLiveData("")
    val date: LiveData<String> = _date
    val account: MutableLiveData<String> = MutableLiveData("")
    val total: MutableLiveData<String> = MutableLiveData("")
    val checkedChip: MutableLiveData<Int> = MutableLiveData(R.id.tran_expense_chip)
    val expenseCat: MutableLiveData<String> = MutableLiveData("")
    val incomeCat: MutableLiveData<String> = MutableLiveData("")
    val repeatCheck: MutableLiveData<Boolean> = MutableLiveData(false)

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

    // SettingsValues will be retrieved from Fragment
    var setVals: SettingsValues = SettingsValues()

    private var maxId: Int = 0

    // argument from Navigation
    var newTran = false

    // used to tell if date has been edited for re-repeating Transactions
    private var dateChanged = false

    /**
     *  Uses [transaction] to pass values to LiveData to be displayed.
     */
    fun setTranData(transaction: Transaction) {
        // Date to String
        _date.value =
            DateFormat.getDateInstance(setVals.dateFormat).format(transaction.date)
        account.value = transaction.account
        // BigDecimal to String
        total.value = if (setVals.decimalPlaces) {
            when (transaction.total.toString()) {
                "0" -> ""
                "0.00" -> ""
                else -> formatDecimal(
                    transaction.total,
                    setVals.thousandsSymbol, setVals.decimalSymbol
                )
            }
        } else {
            if (transaction.total.toString() == "0") {
                ""
            } else {
                formatInteger(transaction.total, setVals.thousandsSymbol)
            }
        }
        if (transaction.type == "Expense") {
            checkedChip.value = R.id.tran_expense_chip
            expenseCat.value = transaction.category
        } else {
            checkedChip.value = R.id.tran_income_chip
            incomeCat.value = transaction.category
        }
        repeatCheck.value = transaction.repeating
    }

    /**
     *  Reassigns LiveData values that couldn't be assigned directly
     *  from Transaction using DataBinding back to Transaction and saves or updates it.
     *  [emptyTitle] is translated string to be used when user does not enter a title.
     */
    fun saveTransaction(emptyTitle: String) {

        tranLD.value!!.let { tran: Transaction ->
            // assigns new id if new Transaction
            if (newTran) tran.id = maxId + 1

            // gives Transaction simple title if user doesn't enter any
            if (tran.title.isBlank()) tran.title = emptyTitle + tran.id

            // is empty if account hasn't been changed so defaults to first account
            tran.account = if (account.value == "") accountList.value!![0] else account.value!!

            // converts the totalField from String into BigDecimal
            tran.total = when {
                total.value!!.isEmpty() && setVals.decimalPlaces -> BigDecimal("0.00")
                total.value!!.isEmpty() -> BigDecimal("0")
                else -> BigDecimal(
                    total.value!!
                        .replace(setVals.thousandsSymbol.toString(), "")
                        .replace(setVals.decimalSymbol.toString(), ".")
                )
            }

            // sets type depending on Chip selected
            // cat values are empty if they haven't been changed so defaults to first category
            if (checkedChip.value == R.id.tran_expense_chip) {
                tran.type = "Expense"
                tran.category = if (expenseCat.value == "") {
                    expenseCatList.value!![0]
                } else {
                    expenseCat.value!!
                }
            } else {
                tran.type = "Income"
                tran.category = if (incomeCat.value == "") {
                    incomeCatList.value!![0]
                } else {
                    incomeCat.value!!
                }
            }

            tran.repeating = repeatCheck.value!!
            if (tran.repeating) tran.futureDate = createFutureDate()
            // frequency must always be at least 1
            if (tran.frequency < 1) tran.frequency = 1

            // Coroutine that Save/Updates/warns user of FutureDate
            viewModelScope.launch {
                if (tran.futureTCreated && dateChanged && tran.repeating) {
                    _futureTranEvent.value = Event(tran)
                } else {
                    // upsert Transaction
                    upsertTransaction(tran)
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
        viewModelScope.launch { upsertTransaction(tran) }
        _saveTranEvent.value = Event(true)
    }

    /**
     *  Negative button function for futureTranDialog.
     *  Stops warning from appearing again, unless user changes Date again.
     *  Updates [tran] in database.
     */
    fun futureTranNegFun(tran: Transaction) {

        dateChanged = false
        viewModelScope.launch { upsertTransaction(tran) }
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

    /**
     *  Returns formatted [num] using [thousands] symbol.
     */
    private fun formatInteger(num: BigDecimal, thousands: Char): String {

        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.groupingSeparator = thousands
        // every three numbers, a thousands symbol will be added
        val formatter = DecimalFormat("#,###", customSymbols)
        formatter.roundingMode = RoundingMode.HALF_UP
        return formatter.format(num)
    }

    /**
     *  Returns formatted [num] using [thousands] and [decimal] symbols.
     */
    private fun formatDecimal(num: BigDecimal, thousands: Char, decimal: Char): String {

        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.groupingSeparator = thousands
        customSymbols.decimalSeparator = decimal
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
                    insertAccount(account)
                }
                accountList.value = addNewToList(it, name, accCreate)
            }
            account.value = name
        }
    }

    /**
     *  Creates Category with [name] or selects it in Spinner if it exists already.
     *  [catCreate] ("Create New..." translated) is added after resorting list.
     */
    fun insertCategory(name: String, catCreate: String) {

        // checks which type is currently selected
        if (checkedChip.value == R.id.tran_expense_chip) {
            expenseCatList.value?.let {
                // create if doesn't exist
                if (!it.contains(name)) {
                    viewModelScope.launch {
                        // creates and inserts new Category with name
                        val category = Category(0, name, "Expense")
                        insertCategory(category)
                    }
                    expenseCatList.value = addNewToList(it, name, catCreate)
                }
            }
            expenseCat.value = name
        } else {
            incomeCatList.value?.let {
                // create if doesn't exist
                if (!it.contains(name)) {
                    viewModelScope.launch {
                        // creates and inserts new Category with name
                        val category = Category(0, name, "Income")
                        insertCategory(category)
                    }
                    incomeCatList.value = addNewToList(it, name, catCreate)
                }
            }
            incomeCat.value = name
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
            accountList.value = getAccountsAsync()
            accountList.value!!.add(accCreate)
            expenseCatList.value = getCategoriesByTypeAsync("Expense")
            expenseCatList.value!!.add(catCreate)
            incomeCatList.value = getCategoriesByTypeAsync("Income")
            incomeCatList.value!!.add(catCreate)
            maxId = getMaxIdAsync().await() ?: 0
            refresh()
        }
    }

    // manually refresh on LiveData
    private fun refresh() {

        tranIdLD.postValue(tranIdLD.value)
    }


    /**
     *  Account queries
     */
    private suspend fun insertAccount(account: Account) {

        tranRepo.insertAccount(account)
    }

    /**
     *  Category queries
     */
    private suspend fun getCategoriesByTypeAsync(type: String): MutableList<String> {

        return tranRepo.getCategoryNamesByTypeAsync(type)
    }

    private suspend fun insertCategory(category: Category) {

        tranRepo.insertCategory(category)
    }

    /**
     *  Transaction queries.
     */
    /**
     *  Doesn't load Transaction directly from Database, but rather by updating the
     *  LiveData object holding ID with [transactionId] which in turn triggers
     *  mapping function above.
     */
    fun loadTransaction(transactionId: Int) {

        tranIdLD.value = transactionId
    }

    private suspend fun getAccountsAsync(): MutableList<String> {

        return tranRepo.getAccountNamesAsync()
    }

    private suspend fun getMaxIdAsync(): Deferred<Int?> {

        return tranRepo.getMaxIdAsync()
    }

    private suspend fun upsertTransaction(transaction: Transaction) {

        tranRepo.upsertTransaction(transaction)
    }
}