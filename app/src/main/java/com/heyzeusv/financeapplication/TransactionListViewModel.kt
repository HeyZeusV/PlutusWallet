package com.heyzeusv.financeapplication

import androidx.lifecycle.ViewModel

class TransactionListViewModel : ViewModel() {

    // gets instance of TransactionRepository and
    // retrieves list of Transactions from database
    private val transactionRepository = TransactionRepository.get()
    val transactionsListLiveData = transactionRepository.getTransactions()
    val categorySizeLiveData = transactionRepository.getCategorySize()

    fun insertTransaction(transaction : Transaction) {

        transactionRepository.insertTransaction(transaction)
    }

    fun deleteTransaction(transaction : Transaction) {

        transactionRepository.deleteTransaction(transaction)
    }

    fun insertCategories(categories : Array<Category>) {

        transactionRepository.insertCategories(categories)
    }
}