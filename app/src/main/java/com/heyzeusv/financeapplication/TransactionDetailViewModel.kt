package com.heyzeusv.financeapplication

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TransactionDetailViewModel : ViewModel() {

    // stores handle to TransactionRepository
    private val transactionRepository = TransactionRepository.get()
    // stores ID of transaction displayed
    private val transactionIdLiveData = MutableLiveData<Int>()
    // retrieves max id of database
    val transactionMaxIdLiveData = transactionRepository.getMaxId()
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

    fun saveTransaction(transaction : Transaction) {

        viewModelScope.launch {

            transactionRepository.updateTransaction(transaction)
        }
    }

    fun insertTransaction(transaction : Transaction) {

        viewModelScope.launch {

            transactionRepository.insertTransaction(transaction)
        }
    }
}