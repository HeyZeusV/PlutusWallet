package com.heyzeusv.financeapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.util.*

class TransactionListViewModel : ViewModel() {

    // gets instance of TransactionRepository and
    // retrieves list of Transactions from database
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    val categorySizeLiveData : LiveData<Int?> = transactionRepository.getCategorySize()

    suspend fun deleteTransaction(transaction : Transaction) {

        transactionRepository.deleteTransaction(transaction)
    }

    fun insertCategories(categories : Array<Category>) {

        transactionRepository.insertCategories(categories)
    }

    // tells repository which query to run on Transaction and passes any arguments needed
    fun filteredTransactionList(category : Boolean?, date : Boolean?, categoryName : String?,
                                start : Date?, end : Date?) : LiveData<List<Transaction>> {

        return if (category == true && date == true) {

            transactionRepository.getTransactions(categoryName, start, end)
        } else if (category == true) {

            transactionRepository.getTransactions(categoryName)
        } else if (date == true) {

            transactionRepository.getTransactions(start, end)
        } else {

            transactionRepository.getTransactions()
        }
    }
}