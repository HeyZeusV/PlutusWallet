package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
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
     *  Account queries
     */
    suspend fun upsertAccounts(accounts : List<Account>) {

        transactionRepository.upsertAccounts(accounts)
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

    /**
     *  Transaction queries.
     */
    suspend fun getDistinctAccountsAsync() : Deferred<List<String>> {

        return transactionRepository.getDistinctAccountsAsync()
    }


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