package com.heyzeusv.financeapplication.viewmodels

import androidx.lifecycle.ViewModel
import com.heyzeusv.financeapplication.database.TransactionRepository
import kotlinx.coroutines.Deferred

/**
 *  Data manager for FilterFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class FilterViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    /**
     *  ExpenseCategory queries.
     */
    suspend fun getExpenseCategoryNamesAsync() : Deferred<List<String>> {

        return transactionRepository.getExpenseCategoryNamesAsync()
    }
    /**
     *  IncomeCategory queries.
     */
    suspend fun getIncomeCategoryNamesAsync() : Deferred<List<String>> {

        return transactionRepository.getIncomeCategoryNamesAsync()
    }
}