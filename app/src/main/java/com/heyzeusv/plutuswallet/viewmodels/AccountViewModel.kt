package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import kotlinx.coroutines.Deferred

class AccountViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    /**
     *  Account Queries
     */
    val accountLiveData : LiveData<List<Account>> = transactionRepository.getLDAccounts()

    suspend fun deleteAccount(account : Account) {

        transactionRepository.deleteAccount(account)
    }

    suspend fun updateAccount(account : Account) {

        transactionRepository.updateAccount(account)
    }

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctAccountsAsync() : Deferred<List<String>> {

        return transactionRepository.getDistinctAccountsAsync()
    }
}