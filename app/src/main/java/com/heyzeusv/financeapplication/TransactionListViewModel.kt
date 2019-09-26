package com.heyzeusv.financeapplication

import androidx.lifecycle.ViewModel
import java.math.BigDecimal

class TransactionListViewModel : ViewModel() {

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
}