package com.heyzeusv.financeapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class FilterViewModel : ViewModel() {

    // stores handle to TransactionRepository
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    val expenseCategoryNamesLiveData : LiveData<List<String>> = transactionRepository.getExpenseCategoryNames()
    val incomeCategoryNamesLiveData  : LiveData<List<String>> = transactionRepository.getIncomeCategoryNames ()
}