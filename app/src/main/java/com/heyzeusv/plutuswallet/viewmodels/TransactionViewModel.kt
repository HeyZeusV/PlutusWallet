package com.heyzeusv.plutuswallet.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.SettingsValues
import com.heyzeusv.plutuswallet.database.entities.Transaction
import com.heyzeusv.plutuswallet.utilities.Event
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

    // manually refresh on LiveData
    private fun refresh() {
        tranIdLD.postValue(tranIdLD.value)
    }

    /**
     * Returns LiveData of a Transaction that gets updated every time a
     * new value gets set on the trigger LiveData instance.
     */
    var tranLD: MutableLiveData<Transaction?> =
        Transformations.switchMap(tranIdLD) { transactionId: Int ->
            tranRepo.getLDTransaction(transactionId)
        } as MutableLiveData<Transaction?>

    // used for various Transaction Fields since property changes don't cause LiveDate updates
    val date: MutableLiveData<String> = MutableLiveData("")
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

    private val _selectDateEvent = MutableLiveData<Event<Date>>()
    val selectDateEvent: LiveData<Event<Date>> = _selectDateEvent

    // SettingsValues will be retrieved from Fragment
    var setVals: SettingsValues = SettingsValues()

    var maxId: Int = 0

    // used to tell if date has been edited for re-repeating Transactions
    var dateChanged = false

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
     *  Returns date from Transaction after adding frequency * period.
     */
    fun createFutureDate(): Date {

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
    fun formatInteger(num: BigDecimal, thousands: Char): String {

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
    fun formatDecimal(num: BigDecimal, thousands: Char, decimal: Char): String {

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
        date.value = DateFormat.getDateInstance(setVals.dateFormat).format(newDate)
    }

    /**
     *  Retrieves list of Accounts/Categories from Database,
     *  adds [accCreate]/[catCreate] ("Create New..." translated),
     *  and retrieves highest ID from database, then refreshes tranIdLd
     */
    fun prepareLists(accCreate: String, catCreate: String) {

        viewModelScope.launch {
            accountList.value = getAccountsAsync().await()
            accountList.value!!.add(accCreate)
            expenseCatList.value = getCategoriesByTypeAsync("Expense").await()
            expenseCatList.value!!.add(catCreate)
            incomeCatList.value = getCategoriesByTypeAsync("Income").await()
            incomeCatList.value!!.add(catCreate)
            maxId = getMaxIdAsync().await() ?: 0
            refresh()
        }
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
    private suspend fun getCategoriesByTypeAsync(type: String): Deferred<MutableList<String>> {

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

    private suspend fun getAccountsAsync(): Deferred<MutableList<String>> {

        return tranRepo.getAccountNamesAsync()
    }

    private suspend fun getMaxIdAsync(): Deferred<Int?> {

        return tranRepo.getMaxIdAsync()
    }

    suspend fun upsertTransaction(transaction: Transaction) {

        tranRepo.upsertTransaction(transaction)
    }
}