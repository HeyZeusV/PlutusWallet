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

    suspend fun deleteTransaction(transaction : Transaction) {

        transactionRepository.deleteTransaction(transaction)
    }

    suspend fun upsertTransactions(transactions : List<Transaction>) {

        transactionRepository.upsertTransactions(transactions)
    }

    /*
        ExpenseCategory queries
     */
    suspend fun getExpenseCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getExpenseCategorySizeAsync()
    }

    suspend fun insertExpenseCategories(expenseCategories : List<ExpenseCategory>) {

        transactionRepository.insertExpenseCategories(expenseCategories)
    }

    /*
        IncomeCategory queries
     */
    suspend fun getIncomeCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getIncomeCategorySizeAsync()
    }

    suspend fun insertIncomeCategories(incomeCategories : List<IncomeCategory>) {

        transactionRepository.insertIncomeCategories(incomeCategories)
    }
}