package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction
import com.heyzeusv.plutuswallet.database.entities.Transaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

private const val EXPENSE = "Expense"
private const val INCOME  = "Income"

/**
 *  Data manager for TransactionListFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class TransactionListViewModel : ViewModel() {

    // stores handle to TransactionRepository
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    // true if there are more Transactions that repeat with futureDate before Date()
    private var moreToCreate : Boolean = false

    // saves position of RecyclerView, MAX_VALUE so that list starts at top
    var rvPosition : Int = Int.MAX_VALUE

    // ItemViewTransaction list to be displayed by RecyclerView
    var ivtList : LiveData<List<ItemViewTransaction>> = MutableLiveData(emptyList())

    // tried using ivtList.empty in XML, but could not get it to work.. displays empty message
    val ivtEmpty : MutableLiveData<Boolean> = MutableLiveData(false)

    /**
     *  Runs when user first starts app or wipes data, adds Categories of both types in table and
     *  creates "None" account.
     */
    fun initializeTables() {

        viewModelScope.launch {

            val catSize : Int = getCategorySizeAsync().await() ?: 0

            if (catSize == 0) {

                val education      = Category(0, "Education"     , EXPENSE)
                val entertainment  = Category(0, "Entertainment" , EXPENSE)
                val food           = Category(0, "Food"          , EXPENSE)
                val home           = Category(0, "Home"          , EXPENSE)
                val transportation = Category(0, "Transportation", EXPENSE)
                val utilities      = Category(0, "Utilities"     , EXPENSE)
                val cryptocurrency = Category(0, "Cryptocurrency", INCOME)
                val investments    = Category(0, "Investments"   , INCOME)
                val salary         = Category(0, "Salary"        , INCOME)
                val savings        = Category(0, "Savings"       , INCOME)
                val stocks         = Category(0, "Stocks"        , INCOME)
                val wages          = Category(0, "Wages"         , INCOME)
                val initialCategories : List<Category> = listOf(education, entertainment, food,
                    home, transportation, utilities, cryptocurrency, investments, salary, savings,
                    stocks, wages)
                insertCategories(initialCategories)
            }

            val accSize : Int = getAccountSizeAsync().await() ?: 0

            if (accSize == 0) {

                val none = Account(0, "None")
                insertAccount(none)
            }
        }
    }

    /**
     *  Adds frequency * period to the date on Transaction.
     *
     *  @param  date      the date of Transaction.
     *  @param  period    how often Transaction repeats.
     *  @param  frequency how often Transaction repeats.
     *  @return the FutureDate set at the beginning of day.
     */
    private fun createFutureDate(date : Date, period : Int, frequency : Int) : Date {

        val calendar : Calendar = Calendar.getInstance()
        // set to Transaction date
        calendar.time = date

        // 0 = Day, 1 = Week, 2 = Month, 3 = Year
        when (period) {

            0 -> calendar.add(Calendar.DAY_OF_MONTH, frequency)
            1 -> calendar.add(Calendar.WEEK_OF_YEAR, frequency)
            2 -> calendar.add(Calendar.MONTH       , frequency)
            3 -> calendar.add(Calendar.YEAR        , frequency)
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
            val futureTranList : MutableList<Transaction> =
                getFutureTransactionsAsync(Date()).await().toMutableList()

            // return if empty
            if (futureTranList.isNotEmpty()) {

                val ready : MutableList<Transaction> = tranUpsertAsync(futureTranList).await()
                upsertTransactions(ready)
                // recursive call in order to create Transactions until all futureDates are past Date()
                if (moreToCreate) {

                    futureTransactions()
                }
            }
        }
    }

    /**
     *  Coroutine that creates copies of Transactions whose futureDates are before current date.
     *  Copies will have new title, date, and futureDate. They will be upserted into Database.
     *
     *  @param futureTranList list of Transactions to be copied, updated, and upserted
     */
    private fun tranUpsertAsync(futureTranList : MutableList<Transaction>)
            : Deferred<MutableList<Transaction>> = viewModelScope.async(Dispatchers.IO) {

            // list that will be upserted into database
            val readyToUpsert: MutableList<Transaction> = mutableListOf()

            futureTranList.forEach { transaction: Transaction ->

                // gets copy of Transaction attached to this FutureTransaction
                val newTran: Transaction = transaction.copy()
                // changing new Transaction values to updated values
                newTran.id         = 0
                newTran.date       = transaction.futureDate
                newTran.title      = incrementString(newTran.title)
                newTran.futureDate = createFutureDate(
                    newTran.date,
                    newTran.period,
                    newTran.frequency
                )
                // if new futureDate is before current time,
                // then there are more Transactions to be added
                if (newTran.futureDate < Date()) {

                    moreToCreate = true
                }
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
     *  Appends " x####" to the end of Transaction title that has been repeated.
     *
     *  @param title the title of Transaction.
     */
    private fun incrementString(title : String) : String {

        // pattern: x######
        val regex = Regex("(x)\\d+")

        // will search for regex in title
        val match : MatchResult? = regex.find(title)

        // string that match found, if any
        val matchingString : String? = match?.value
        var newTitle       : String  = title

        when {

            matchingString != null -> {
                // returns only the Int and increments it by one
                var noPrefixInt : Int = matchingString.replace("x", "").toInt()
                noPrefixInt += 1
                // removes "x###" as this will be updated
                newTitle = newTitle.replace(regex , "")
                // appends the prefix and updated int onto the end of title
                newTitle += "x$noPrefixInt"
            }
            else -> newTitle += " x2" // first time being repeated
        }

        return newTitle
    }

    /**
     *  Account queries
     */
    private suspend fun getAccountSizeAsync() : Deferred<Int?> {

        return  transactionRepository.getAccountSizeAsync()
    }

    private suspend fun insertAccount(account : Account) {

        transactionRepository.insertAccount(account)
    }

    /**
     *  Category queries
     */
    private suspend fun getCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getCategorySizeAsync()
    }

    private suspend fun insertCategories(categories : List<Category>) {

        transactionRepository.insertCategories(categories)
    }

    /**
     *  Transaction queries
     */
    /**
     *  Tells Repository which Transaction list to return.
     *
     *  Uses the values of category and date in order to determine which Transaction list is needed.
     *
     *  @param  account      boolean for account filter
     *  @param  category     boolean for category filter.
     *  @param  date         boolean for date filter.
     *  @param  type         either "Expense" or "Income".
     *  @param  accountName  account name to be searched
     *  @param  categoryName category name to be searched in table of type.
     *  @param  start        starting Date for date filter.
     *  @param  end          ending Date for date filter.
     *  @return LiveData object holding list of Transactions.
     */
    fun filteredTransactionList(account : Boolean?, category : Boolean?, date : Boolean?,
                                type : String?, accountName : String?, categoryName : String?,
                                start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>> {

        return when {

            account == true && category == true && date == true && categoryName == "All" ->
                transactionRepository.getLdATD(accountName, type, start, end)
            account == true && category == true && date == true ->
                transactionRepository.getLdATCD(accountName, type, categoryName, start, end)
            account == true && category == true && categoryName == "All" ->
                transactionRepository.getLdAT(accountName, type)
            account == true && category == true ->
                transactionRepository.getLdATC(accountName, type, categoryName)
            account == true && date == true ->
                transactionRepository.getLdAD(accountName, start, end)
            account == true ->
                transactionRepository.getLdA(accountName)
            category == true && date == true && categoryName == "All" ->
                transactionRepository.getLdTD(type, start, end)
            category == true && date == true ->
                transactionRepository.getLdTCD(type, categoryName, start, end)
            category == true && categoryName == "All" ->
                transactionRepository.getLdT(type)
            category == true ->
                transactionRepository.getLdTC(type, categoryName)
            date == true ->
                transactionRepository.getLdD(start, end)
            else ->
                transactionRepository.getLd()
        }
    }

    private suspend fun getFutureTransactionsAsync(currentDate : Date) : Deferred<List<Transaction>> {

        return transactionRepository.getFutureTransactionsAsync(currentDate)
    }

    suspend fun getTransactionAsync(id : Int) : Deferred<Transaction> {

        return transactionRepository.getTransactionAsync(id)
    }

    suspend fun deleteTransaction(transaction : Transaction) {

        transactionRepository.deleteTransaction(transaction)
    }

    private suspend fun upsertTransactions(transactions : List<Transaction>) {

        transactionRepository.upsertTransactions(transactions)
    }
}