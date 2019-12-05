package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.ExpenseCategory
import com.heyzeusv.plutuswallet.database.entities.IncomeCategory
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
     *  Transaction queries
     */
    /**
     *  Tells Repository which Transaction list to return.
     *
     *  Uses the values of category and date in order to determine which Transaction list is needed.
     *
     *  @param  category     boolean for category filter.
     *  @param  date         boolean for date filter.
     *  @param  type         either "Expense" or "Income".
     *  @param  categoryName category name to be searched in table of type.
     *  @param  start        starting Date for date filter.
     *  @param  end          ending Date for date filter.
     *  @return LiveData object holding list of Transactions.
     */
    fun filteredTransactionList(category : Boolean?, date : Boolean?, type : String?, categoryName : String?,
                                start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>> {

        return if (category == true && date == true && categoryName == "All") {

            transactionRepository.getLDTransactions(type, start, end)
        } else if (category == true && date == true) {

            transactionRepository.getLDTransactions(type, categoryName, start, end)
        } else if (category == true && categoryName == "All") {

            transactionRepository.getLDTransactions(type)
        } else if (category == true) {

            transactionRepository.getLDTransactions(type, categoryName)
        } else if (date == true) {

            transactionRepository.getLDTransactions(start, end)
        }  else {

            transactionRepository.getLDTransactions()
        }
    }

    suspend fun getFutureTransactionsAsync(currentDate : Date) : Deferred<List<Transaction>> {

        return transactionRepository.getFutureTransactionsAsync(currentDate)
    }

    suspend fun getMaxIdAsync() : Deferred<Int?> {

        return transactionRepository.getMaxIdAsync()
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

    suspend fun insertExpenseCategories(expenseCategories : List<ExpenseCategory>) {

        transactionRepository.insertExpenseCategories(expenseCategories)
    }

    /**
     *  IncomeCategory queries
     */
    suspend fun getIncomeCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getIncomeCategorySizeAsync()
    }

    suspend fun insertIncomeCategories(incomeCategories : List<IncomeCategory>) {

        transactionRepository.insertIncomeCategories(incomeCategories)
    }
}