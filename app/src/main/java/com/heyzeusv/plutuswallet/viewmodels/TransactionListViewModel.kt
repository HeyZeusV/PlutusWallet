package com.heyzeusv.plutuswallet.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction
import com.heyzeusv.plutuswallet.database.entities.SettingsValues
import com.heyzeusv.plutuswallet.database.entities.Transaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

private const val EXPENSE = "Expense"
private const val INCOME = "Income"

/**
 *  Data manager for TransactionListFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class TransactionListViewModel @ViewModelInject constructor(
    private val tranRepo: TransactionRepository
) : ViewModel() {

    // true if there are more Transactions that repeat with futureDate before Date()
    private var moreToCreate: Boolean = false

    // saves position of RecyclerView, MAX_VALUE so that list starts at top
    var rvPosition: Int = Int.MAX_VALUE

    // ItemViewTransaction list to be displayed by RecyclerView
    var ivtList: LiveData<List<ItemViewTransaction>> = MutableLiveData(emptyList())

    // tried using ivtList.empty in XML, but could not get it to work.. displays empty message
    val ivtEmpty: MutableLiveData<Boolean> = MutableLiveData(false)

    // onClick/onLongClick Events for ViewHolder
    val openTran: MutableLiveData<Int?> = MutableLiveData()
    val deleteTran: MutableLiveData<ItemViewTransaction?> = MutableLiveData()

    var setVals: SettingsValues = SettingsValues()

    /**
     *  Event that navigates user to Transaction with selected [tranId].
     */
    fun openTranOC(tranId: Int) {

        openTran.value = tranId
    }

    /**
     *  Event that deletes selected [ivt]. Must return Boolean.
     */
    fun deleteTranOC(ivt: ItemViewTransaction): Boolean {

        deleteTran.value = ivt
        return true
    }

    /**
     *  Runs when user first starts app or wipes data, adds Categories of both types in table and
     *  creates "None" account.
     */
    fun initializeTables() {

        viewModelScope.launch {
            val catSize: Int = getCategorySizeAsync().await()

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
                insertCategories(initialCategories)
            }

            val accSize: Int = getAccountSizeAsync().await()

            if (accSize == 0) {
                val none = Account(0, "None")
                insertAccount(none)
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
                getFutureTransactionsAsync(Date()).await().toMutableList()

            // return if empty
            if (futureTranList.isNotEmpty()) {

                val ready: MutableList<Transaction> = tranUpsertAsync(futureTranList).await()
                upsertTransactions(ready)
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
     *  Account queries
     */
    private suspend fun getAccountSizeAsync(): Deferred<Int> {

        return tranRepo.getAccountSizeAsync()
    }

    private suspend fun insertAccount(account: Account) {

        tranRepo.insertAccount(account)
    }

    /**
     *  Category queries
     */
    private suspend fun getCategorySizeAsync(): Deferred<Int> {

        return tranRepo.getCategorySizeAsync()
    }

    private suspend fun insertCategories(categories: List<Category>) {

        tranRepo.insertCategories(categories)
    }

    /**
     *  Transaction queries
     */
    /**
     *  Returns LiveData of list of Transactions depending on [account]/[category]/[date] filters,
     *  [type] selected, [accountName]/[categoryName] selected, and [start]/[end] dates selected.
     */
    fun filteredTransactionList(
        account: Boolean,
        category: Boolean,
        date: Boolean,
        type: String,
        accountName: String,
        categoryName: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {

        return when {
            account && category && date && categoryName == "All" ->
                tranRepo.getLdATD(accountName, type, start, end)
            account && category && date ->
                tranRepo.getLdATCD(accountName, type, categoryName, start, end)
            account && category && categoryName == "All" -> tranRepo.getLdAT(accountName, type)
            account && category -> tranRepo.getLdATC(accountName, type, categoryName)
            account && date -> tranRepo.getLdAD(accountName, start, end)
            account -> tranRepo.getLdA(accountName)
            category && date && categoryName == "All" -> tranRepo.getLdTD(type, start, end)
            category && date -> tranRepo.getLdTCD(type, categoryName, start, end)
            category && categoryName == "All" -> tranRepo.getLdT(type)
            category -> tranRepo.getLdTC(type, categoryName)
            date -> tranRepo.getLdD(start, end)
            else -> tranRepo.getLd()
        }
    }

    private suspend fun getFutureTransactionsAsync(currentDate: Date): Deferred<List<Transaction>> {

        return tranRepo.getFutureTransactionsAsync(currentDate)
    }

    suspend fun getTransactionAsync(id: Int): Deferred<Transaction> {

        return tranRepo.getTransactionAsync(id)
    }

    suspend fun deleteTransaction(transaction: Transaction) {

        tranRepo.deleteTransaction(transaction)
    }

    private suspend fun upsertTransactions(transactions: List<Transaction>) {

        tranRepo.upsertTransactions(transactions)
    }
}