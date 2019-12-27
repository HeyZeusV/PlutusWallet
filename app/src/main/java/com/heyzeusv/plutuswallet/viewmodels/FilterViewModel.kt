package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Category
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
     *  Transaction queries.
     */
    suspend fun getAccountsAsync() : Deferred<List<String>> {

        return transactionRepository.getAccountsAsync()
    }

    /**
     *  Category queries.
     */
    suspend fun getCategoriesByTypeAsync(type : String) : Deferred<List<String>> {

        return transactionRepository.getCategoriesByTypeAsync(type)
    }

    suspend fun getCategorySizeAsync() : Deferred<Int?> {

        return transactionRepository.getCategorySizeAsync()
    }

    suspend fun insertCategories(categories : List<Category>) {

        transactionRepository.insertCategories(categories)
    }
}