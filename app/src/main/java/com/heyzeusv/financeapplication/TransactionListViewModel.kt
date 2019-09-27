package com.heyzeusv.financeapplication

import androidx.lifecycle.ViewModel

class TransactionListViewModel : ViewModel() {

    /*
    USED FOR TESTING PURPOSES
    val transactions = mutableListOf<Transaction>()

    init {

        for (i in 0 until 100) {

            val transaction = Transaction()
            transaction.id = i
            transaction.title = "Transaction #$i"
            transaction.total = BigDecimal("100.00")
            transaction.repeating = i % 2 == 0
            transactions += transaction
        }
    }
    */

    // gets instance of TransactionRepository and
    // retrieves list of Transactions from database
    private val transactionRepository = TransactionRepository.get()
    val transactionsListLiveData = transactionRepository.getTransactions()

    fun insert(transaction : Transaction) {

        transactionRepository.insert(transaction)
    }
}