package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Category

/**
 *  Data manager for CategoriesFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class CategoriesViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    val expenseCategoriesLiveData : LiveData<List<Category>> = transactionRepository.getLDCategoriesByType("Expense")
    val incomeCategoriesLiveData  : LiveData<List<Category>> = transactionRepository.getLDCategoriesByType("Income" )

    suspend fun updateCategory(category : Category) {

        transactionRepository.updateCategory(category)
    }
}