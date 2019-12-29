package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Category
import kotlinx.coroutines.Deferred

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

    /**
     *  Category Queries
     */
    val expenseCategoriesLiveData : LiveData<List<Category>> = transactionRepository.getLDCategoriesByType("Expense")
    val incomeCategoriesLiveData  : LiveData<List<Category>> = transactionRepository.getLDCategoriesByType("Income" )

    suspend fun deleteCategory(category : Category) {

        transactionRepository.deleteCategory(category)
    }

    suspend fun updateCategory(category : Category) {

        transactionRepository.updateCategory(category)
    }

    /**
     *  Transaction Queries
     */
    val uniqueExpenseLiveData : LiveData<List<String>> = transactionRepository.getLDUniqueCategories("Expense")
    val uniqueIncomeLiveData  : LiveData<List<String>> = transactionRepository.getLDUniqueCategories("Income" )
}