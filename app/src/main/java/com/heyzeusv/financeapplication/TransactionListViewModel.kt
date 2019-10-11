package com.heyzeusv.financeapplication

import androidx.lifecycle.ViewModel

class TransactionListViewModel : ViewModel() {

    // gets instance of TransactionRepository and
    // retrieves list of Transactions from database
    private val transactionRepository = TransactionRepository.get()
    // LiveData<List<Transaction>>
    val transactionsListLiveData = transactionRepository.getTransactions()
    // LiveData<Int?>
    val categorySizeLiveData = transactionRepository.getCategorySize()

    suspend fun deleteTransaction(transaction : Transaction) {

        transactionRepository.deleteTransaction(transaction)
    }

    fun insertCategories(categories : Array<Category>) {

        transactionRepository.insertCategories(categories)
    }
}