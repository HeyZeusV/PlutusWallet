package com.heyzeusv.financeapplication

import androidx.lifecycle.*
import kotlinx.coroutines.Deferred

/**
 *  Data manager for TransactionFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class TransactionDetailViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    /**
     *  Transaction queries.
     */
    /**
     *  stores ID of Transaction displayed.
     */
    private val transactionIdLiveData = MutableLiveData<Int>()

    /**
     *  Sets up a trigger-response relationship.
     *
     *  LiveData object used as trigger and mapping function that must return LiveData object.
     *
     *  @return LiveData object holding a Transaction that gets updated every time a
     *          new value gets set on the trigger LiveData instance.
     */
    var transactionLiveData : LiveData<Transaction?> =
        Transformations.switchMap(transactionIdLiveData) { transactionId ->
            transactionRepository.getLDTransaction(transactionId)
        }

    /**
     *  'Loads' Transaction.
     *
     *  Doesn't load Transaction directly from Database, but rather by updating the
     *  LiveData object holding ID which in turn triggers mapping function above.
     *
     *  @param transactionId Id of Transaction to be loaded.
     */
    fun loadTransaction(transactionId : Int) {

        transactionIdLiveData.value = transactionId
    }

    suspend fun insertTransaction(transaction : Transaction) {

        transactionRepository.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction : Transaction) {

        transactionRepository.updateTransaction(transaction)
    }

    /**
     *  ExpenseCategory queries.
     */
    val expenseCategoryNamesLiveData : LiveData<List<String>> = transactionRepository.getExpenseCategoryNames()

    suspend fun insertExpenseCategory(expenseCategory : ExpenseCategory) {

        transactionRepository.insertExpenseCategory(expenseCategory)
    }

    /**
     *  IncomeCategory queries.
     */
    val incomeCategoryNamesLiveData  : LiveData<List<String>> = transactionRepository.getIncomeCategoryNames ()

    suspend fun insertIncomeCategory(incomeCategory : IncomeCategory) {

        transactionRepository.insertIncomeCategory(incomeCategory)
    }
}