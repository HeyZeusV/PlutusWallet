package com.heyzeusv.financeapplication

import androidx.lifecycle.*
import kotlinx.coroutines.Deferred

class TransactionDetailViewModel : ViewModel() {

    // stores handle to TransactionRepository
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    // stores ID of transaction displayed
    private val transactionIdLiveData = MutableLiveData<Int>()

    val expenseCategoryNamesLiveData : LiveData<List<String>> = transactionRepository.getExpenseCategoryNames()
    val incomeCategoryNamesLiveData  : LiveData<List<String>> = transactionRepository.getIncomeCategoryNames ()

    // value gets updated every time a new value gets set on the trigger LiveData instance
    var transactionLiveData : LiveData<Transaction?> =
    // sets up a trigger-response relationship
    // LiveData obj used as trigger and mapping function that must return LiveData obj
        Transformations.switchMap(transactionIdLiveData) { transactionId ->
            transactionRepository.getLDTransaction(transactionId)
        }

    fun loadTransaction(transactionId : Int) {

        transactionIdLiveData.value = transactionId
    }

    /*
        Transaction queries
     */
    suspend fun getMaxIdAsync() : Deferred<Int?> {

        return transactionRepository.getMaxIdAsync()
    }

    suspend fun insertTransaction(transaction : Transaction) {

        transactionRepository.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction : Transaction) {

        transactionRepository.updateTransaction(transaction)
    }

    /*
        FutureTransaction queries
     */
    suspend fun getFutureTransactionAsync(transactionId : Int) : Deferred<FutureTransaction?> {

        return transactionRepository.getFutureTransactionAsync(transactionId)
    }

    suspend fun deleteFutureTransaction(futureTransaction : FutureTransaction) {

        transactionRepository.deleteFutureTransaction(futureTransaction)
    }

    suspend fun insertFutureTransaction(futureTransaction : FutureTransaction) {

        transactionRepository.insertFutureTransaction(futureTransaction)
    }

    suspend fun updateFutureTransaction(futureTransaction : FutureTransaction) {

         transactionRepository.updateFutureTransaction(futureTransaction)
    }

    /*
        ExpenseCategory queries
     */
    suspend fun insertExpenseCategory(expenseCategory : ExpenseCategory) {

        transactionRepository.insertExpenseCategory(expenseCategory)
    }

    /*
        IncomeCategory queries
     */
    suspend fun insertIncomeCategory(incomeCategory : IncomeCategory) {

        transactionRepository.insertIncomeCategory(incomeCategory)
    }
}