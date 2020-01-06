package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction
import com.heyzeusv.plutuswallet.database.entities.Transaction
import kotlinx.coroutines.Deferred
import java.util.Date

/**
 *  Data manager for TransactionListFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class TransactionListViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    /**
     *  Account queries
     */
    suspend fun getAccountSizeAsync() : Deferred<Int?> {

        return  transactionRepository.getAccountSizeAsync()
    }

    suspend fun upsertAccount(account : Account) {

        transactionRepository.upsertAccount(account)
    }

    /**
     *  Category queries
     */
    suspend fun getCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getCategorySizeAsync()
    }

    suspend fun insertCategories(categories : List<Category>) {

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

        return if (account == true && category == true && date == true) {

            if (accountName == "All" && categoryName == "All") {

                transactionRepository.getLdTD(type, start, end)
            } else if (accountName == "All") {

                transactionRepository.getLdTCD(type, categoryName, start, end)
            } else if (categoryName == "All") {

                transactionRepository.getLdATD(accountName, type, start, end)
            } else {

                transactionRepository.getLdATCD(accountName, type, categoryName, start, end)
            }
        } else if (account == true && category == true) {

            if (accountName == "All" && categoryName == "All") {

                transactionRepository.getLdT(type)
            } else if (accountName == "All") {

                transactionRepository.getLdTC(type, categoryName)
            } else if (categoryName == "All") {

                transactionRepository.getLdAT(accountName, type)
            } else {

                transactionRepository.getLdATC(accountName, type, categoryName)
            }
        } else if (account == true && date == true) {

            if (accountName == "All") {

                transactionRepository.getLdD(start, end)
            } else {

                transactionRepository.getLdAD(accountName, start, end)
            }
        } else if (account == true) {

            if (accountName == "All") {

                transactionRepository.getLd()
            } else {

                transactionRepository.getLdA(accountName)
            }
        } else if (category == true && date == true) {

            if (categoryName == "All") {

                transactionRepository.getLdTD(type, start, end)
            } else {

                transactionRepository.getLdTCD(type, categoryName, start, end)
            }
        } else if (category == true) {

            if (categoryName == "All") {

                transactionRepository.getLdT(type)
            } else {

                transactionRepository.getLdTC(type, categoryName)
            }
        } else if (date == true) {

            transactionRepository.getLdD(start, end)
        } else {

            transactionRepository.getLd()
        }
    }

    suspend fun getFutureTransactionsAsync(currentDate : Date) : Deferred<List<Transaction>> {

        return transactionRepository.getFutureTransactionsAsync(currentDate)
    }

    suspend fun getTransactionAsync(id : Int) : Deferred<Transaction> {

        return transactionRepository.getTransactionAsync(id)
    }

    suspend fun deleteTransaction(transaction : Transaction) {

        transactionRepository.deleteTransaction(transaction)
    }

    suspend fun upsertTransactions(transactions : List<Transaction>) {

        transactionRepository.upsertTransactions(transactions)
    }

    /**
     *  ExpenseCategory queries
     */
    suspend fun getExpenseCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getExpenseCategorySizeAsync()
    }

    /**
     *  IncomeCategory queries
     */
    suspend fun getIncomeCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getIncomeCategorySizeAsync()
    }
}