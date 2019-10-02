package com.heyzeusv.financeapplication

import androidx.lifecycle.ViewModel

class TransactionListViewModel : ViewModel() {

    // gets instance of TransactionRepository and
    // retrieves list of Transactions from database
    private val transactionRepository = TransactionRepository.get()
    val transactionsListLiveData = transactionRepository.getTransactions()

    fun insert(transaction : Transaction) {

        transactionRepository.insert(transaction)
    }

    fun deleteTransaction(transaction : Transaction) {

        transactionRepository.delete(transaction)
    }
}