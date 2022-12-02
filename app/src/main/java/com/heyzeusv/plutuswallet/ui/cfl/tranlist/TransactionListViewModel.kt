package com.heyzeusv.plutuswallet.ui.cfl.tranlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val EXPENSE = "Expense"
private const val INCOME = "Income"

/**
 *  Data manager for TransactionListFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val tranRepo: Repository,
    val setVals: SettingsValues
) : ViewModel() {

    // ItemViewTransaction list to be displayed by RecyclerView
    private val _tranList = MutableStateFlow(emptyList<ItemViewTransaction>())
    val tranList: StateFlow<List<ItemViewTransaction>> get() = _tranList
    fun updateTranList(filter: FilterInfo) {
        viewModelScope.launch {
            filteredTransactionList(filter).collect { list ->
                _tranList.value = list
            }
        }
    }

    private val _showDeleteDialog = MutableStateFlow(-1)
    val showDeleteDialog: StateFlow<Int> get() = _showDeleteDialog
    fun updateDeleteDialog(newValue: Int) { _showDeleteDialog.value = newValue }

    // true if there are more Transactions that repeat with futureDate before Date()
    private var moreToCreate: Boolean = false
    var previousListSize = 0

    init {
        initializeTables()
        updateTranList(FilterInfo())
    }

    /**
     *  Removes Transaction with [id] from database.
     */
    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            tranRepo.getTransactionAsync(id)?.let {
                tranRepo.deleteTransaction(it)
            }
        }
    }

    /**
     *  Runs when user first starts app or wipes data, adds Categories of both types in table and
     *  creates "None" account.
     */
    private fun initializeTables() {
        viewModelScope.launch {
            val catSize: Int = tranRepo.getCategorySizeAsync()

            if (catSize == 0) {
                val education = Category(0, "Education", EXPENSE)
                val entertainment = Category(0, "Entertainment", EXPENSE)
                val food = Category(0, "Food", EXPENSE)
                val home = Category(0, "Home", EXPENSE)
                val transportation = Category(0, "Transportation", EXPENSE)
                val utilities = Category(0, "Utilities", EXPENSE)

                val cryptocurrency = Category(0, "Cryptocurrency", INCOME)
                val investments = Category(0, "Investments", INCOME)
                val salary = Category(0, "Salary", INCOME)
                val savings = Category(0, "Savings", INCOME)
                val stocks = Category(0, "Stocks", INCOME)
                val wages = Category(0, "Wages", INCOME)

                val initialCategories: List<Category> = listOf(
                    education, entertainment, food, home, transportation, utilities,
                    cryptocurrency, investments, salary, savings, stocks, wages
                )
                tranRepo.insertCategories(initialCategories)
            }

            val accSize: Int = tranRepo.getAccountSizeAsync()

            if (accSize == 0) {
                val none = Account(0, "None")
                tranRepo.insertAccount(none)
            }
        }
    }

    /**
     *  Returns FutureDate set at the beginning of the day by calculating
     *  ([frequency] * [period]) + [date].
     */
    private fun createFutureDate(date: Date, period: Int, frequency: Int): Date {
        val calendar: Calendar = Calendar.getInstance()
        // set to Transaction date
        calendar.time = date

        // 0 = Day, 1 = Week, 2 = Month, 3 = Year
        when (period) {
            0 -> calendar.add(Calendar.DAY_OF_MONTH, frequency)
            1 -> calendar.add(Calendar.WEEK_OF_YEAR, frequency)
            2 -> calendar.add(Calendar.MONTH, frequency)
            3 -> calendar.add(Calendar.YEAR, frequency)
        }

        return calendar.time
    }

    /**
     *  Adds new Transactions depending on existing Transactions futureDate.
     */
    fun futureTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            // used to tell if newly created Transaction's futureDate is before Date()
            moreToCreate = false
            // returns list of all Transactions whose futureDate is before current date
            val futureTranList: MutableList<Transaction> =
                tranRepo.getFutureTransactionsAsync(Date()).toMutableList()
            // return if empty
            if (futureTranList.isNotEmpty()) {
                val ready: MutableList<Transaction> = tranUpsertAsync(futureTranList).await()
                tranRepo.upsertTransactions(ready)
                // recursive call in order to create Transactions until all futureDates are past Date()
                if (moreToCreate) futureTransactions()
            }
        }
    }

    /**
     *  Coroutine that creates copies of Transactions from [futureTranList] whose futureDates
     *  are before current date.
     *  Copies will have new title, date, and futureDate. They will be upserted into Database.
     */
    private fun tranUpsertAsync(futureTranList: MutableList<Transaction>)
            : Deferred<MutableList<Transaction>> = viewModelScope.async(Dispatchers.IO) {
        // list that will be upserted into database
        val readyToUpsert: MutableList<Transaction> = mutableListOf()

        futureTranList.forEach { transaction: Transaction ->
            // gets copy of Transaction attached to this FutureTransaction
            val newTran: Transaction = transaction.copy()
            // changing new Transaction values to updated values
            newTran.id = 0
            newTran.date = transaction.futureDate
            newTran.title = incrementString(newTran.title)
            newTran.futureDate = createFutureDate(
                newTran.date,
                newTran.period,
                newTran.frequency
            )
            // if new futureDate is before current time,
            // then there are more Transactions to be added
            if (newTran.futureDate < Date()) moreToCreate = true

            // stops this Transaction from being repeated again if user switches its date
            transaction.futureTCreated = true
            // futureTran to be inserted
            readyToUpsert.add(newTran)
            // transaction to be updated
            readyToUpsert.add(transaction)
        }

        return@async readyToUpsert
    }

    /**
     *  Appends " x####" to the end of Transaction [title] that has been repeated.
     */
    private fun incrementString(title: String): String {
        // pattern: x######
        val regex = Regex("(x)\\d+")
        // will search for regex in title
        val match: MatchResult? = regex.find(title)

        // string that match found, if any
        val matchingString: String? = match?.value
        var newTitle: String = title

        if (matchingString != null) {
            // returns only the Int and increments it by one
            var noPrefixInt: Int = matchingString.replace("x", "").toInt()
            noPrefixInt += 1
            // removes "x###" as this will be updated
            newTitle = newTitle.replace(regex, "")
            // appends the prefix and updated int onto the end of title
            newTitle += "x$noPrefixInt"
        } else {
            newTitle += " x2" // first time being repeated
        }

        return newTitle
    }

    /**
     *  Returns StateFlow of list of Transactions depending on [fi] arguments.
     */
    suspend fun filteredTransactionList(fi: FilterInfo): Flow<List<ItemViewTransaction>> {
        return when {
            fi.account && fi.category && fi.date && fi.categoryNames.contains("All") ->
                tranRepo.getIvtATD(fi.accountNames, fi.type, fi.start, fi.end)
            fi.account && fi.category && fi.date ->
                tranRepo.getIvtATCD(fi.accountNames, fi.type, fi.categoryNames, fi.start, fi.end)
            fi.account && fi.category && fi.categoryNames.contains("All") -> tranRepo.getIvtAT(fi.accountNames, fi.type)
            fi.account && fi.category -> tranRepo.getIvtATC(fi.accountNames, fi.type, fi.categoryNames)
            fi.account && fi.date -> tranRepo.getIvtAD(fi.accountNames, fi.start, fi.end)
            fi.account -> tranRepo.getIvtA(fi.accountNames)
            fi.category && fi.date && fi.categoryNames.contains("All") -> tranRepo.getIvtTD(fi.type, fi.start, fi.end)
            fi.category && fi.date -> tranRepo.getIvtTCD(fi.type, fi.categoryNames, fi.start, fi.end)
            fi.category && fi.categoryNames.contains("All") -> tranRepo.getIvtT(fi.type)
            fi.category -> tranRepo.getIvtTC(fi.type, fi.categoryNames)
            fi.date -> tranRepo.getIvtD(fi.start, fi.end)
            else -> tranRepo.getIvt()
        }
    }
}