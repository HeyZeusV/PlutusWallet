package com.heyzeusv.financeapplication

import androidx.lifecycle.*
import kotlinx.coroutines.Deferred

class TransactionDetailViewModel : ViewModel() {

    // stores handle to TransactionRepository
    private val transactionRepository = TransactionRepository.get()
    // stores ID of transaction displayed
    private val transactionIdLiveData = MutableLiveData<Int>()
    // LiveData<List<String>>
    val categoryNamesLiveData = transactionRepository.getCategoryNames()

    // value gets updated every time a new value gets set on the trigger LiveData instance
    var transactionLiveData : LiveData<Transaction?> =
    // sets up a trigger-response relationship
    // LiveData obj used as trigger and mapping function that must return LiveData obj
    Transformations.switchMap(transactionIdLiveData) { transactionId ->
        transactionRepository.getTransaction(transactionId)
    }

    fun loadTransaction(transactionId : Int) {

        transactionIdLiveData.value = transactionId
    }

    suspend fun saveTransaction(transaction : Transaction) {

        transactionRepository.updateTransaction(transaction)
    }

    suspend fun insertTransaction(transaction : Transaction) {

        transactionRepository.insertTransaction(transaction)
    }

    suspend fun getMaxIdAsync() : Deferred<Int?> {

        return transactionRepository.getMaxIdAsync()
    }

}