package com.heyzeusv.financeapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

/**
 *  Data manager for TransactionListFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class FilterViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    /**
     *  ExpenseCategory queries.
     */
    val expenseCategoryNamesLiveData : LiveData<List<String>> = transactionRepository.getExpenseCategoryNames()

    /**
     *  IncomeCategory queries.
     */
    val incomeCategoryNamesLiveData  : LiveData<List<String>> = transactionRepository.getIncomeCategoryNames ()
}