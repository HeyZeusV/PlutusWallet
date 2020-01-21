package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.Transaction
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
     *  Account queries
     */
    suspend fun insertAccount(account : Account) {

        transactionRepository.insertAccount(account)
    }

    /**
     *  Category queries
     */
    suspend fun getCategoriesByTypeAsync(type : String) : Deferred<List<String>> {

        return transactionRepository.getCategoriesByTypeAsync(type)
    }

    suspend fun insertCategory(category : Category) {

        transactionRepository.insertCategory(category)
    }

    /**
     *  Transaction queries.
     */
    // stores ID of Transaction displayed.
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
        Transformations.switchMap(transactionIdLiveData) { transactionId : Int ->
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

    suspend fun getAccountsAsync() : Deferred<MutableList<String>> {

        return transactionRepository.getAccountsAsync()
    }

    suspend fun getMaxIdAsync() : Deferred<Int?> {

        return transactionRepository.getMaxIdAsync()
    }

    suspend fun insertTransaction(transaction : Transaction) {

        transactionRepository.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction : Transaction) {

        transactionRepository.updateTransaction(transaction)
    }
}