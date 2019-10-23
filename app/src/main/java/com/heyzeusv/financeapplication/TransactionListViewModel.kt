package com.heyzeusv.financeapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Deferred
import java.util.*

class TransactionListViewModel : ViewModel() {

    // gets instance of TransactionRepository and
    // retrieves list of Transactions from database
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    /*
        Transaction queries
     */
    val transactionMaxIdLiveData : LiveData<Int?> = transactionRepository.getMaxLDId()

    // tells repository which query to run on Transaction and passes any arguments needed
    fun filteredTransactionList(category : Boolean?, date : Boolean?, type : String?, categoryName : String?,
                                start : Date?, end : Date?) : LiveData<List<Transaction>> {

        return if (category == true && date == true) {

            transactionRepository.getLDTransactions(type, categoryName, start, end)
        } else if (category == true) {

            transactionRepository.getLDTransactions(type, categoryName)
        } else if (date == true) {

            transactionRepository.getLDTransactions(start, end)
        } else {

            transactionRepository.getLDTransactions()
        }
    }

    suspend fun getFutureTransactionsAsync(currentDate : Date) : Deferred<List<Transaction>> {

        return transactionRepository.getFutureTransactionsAsync(currentDate)
    }

    suspend fun getMaxIdAsync() : Deferred<Int?> {

        return transactionRepository.getMaxIdAsync()
    }

    suspend fun deleteTransaction(transaction : Transaction) {

        transactionRepository.deleteTransaction(transaction)
    }

    suspend fun insertTransaction(transaction : Transaction) {

        transactionRepository.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction : Transaction) {

        transactionRepository.updateTransaction(transaction)
    }

    suspend fun insertTransactions(transactions : Array<Transaction>) {

        transactionRepository.insertTransactions(transactions)
    }

    suspend fun updateTransactions(transactions : Array<Transaction>) {

        transactionRepository.updateTransactions(transactions)
    }

    /*
        ExpenseCategory queries
     */
    suspend fun getExpenseCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getExpenseCategorySizeAsync()
    }

    suspend fun insertExpenseCategories(expenseCategories : Array<ExpenseCategory>) {

        transactionRepository.insertExpenseCategories(expenseCategories)
    }

    /*
        IncomeCategory queries
     */
    suspend fun getIncomeCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getIncomeCategorySizeAsync()
    }

    suspend fun insertIncomeCategories(incomeCategories : Array<IncomeCategory>) {

        transactionRepository.insertIncomeCategories(incomeCategories)
    }
}