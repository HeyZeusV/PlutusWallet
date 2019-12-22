package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.ExpenseCategory
import com.heyzeusv.plutuswallet.database.entities.IncomeCategory
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

    val expenseNamesLiveData : LiveData<List<String>> = transactionRepository.getLDExpenseCategoryNames()
    val incomeNamesLiveData  : LiveData<List<String>> = transactionRepository.getLDIncomeCategoryNames ()

    suspend fun updateExpenseCategory(expenseCategory : ExpenseCategory) {

        transactionRepository.updateExpenseCategory(expenseCategory)
    }

    suspend fun updateIncomeCategory(incomeCategory : IncomeCategory) {

        transactionRepository.updateIncomeCategory(incomeCategory)
    }
}