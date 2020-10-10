package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */
    private val tranRepo: TransactionRepository = TransactionRepository.get()

    // list of all Account names used to prevent 2 Accounts from having same name
    var accountNames: List<String> = emptyList()
    // list of Accounts unable to be deleted due to being used
    var accountsUsed: List<String> = emptyList()

    /**
     *  Account Queries
     */
    val accountLD: LiveData<List<Account>> = tranRepo.getLDAccounts()

    suspend fun getAccountNamesAsync(): Deferred<MutableList<String>> {

        return tranRepo.getAccountNamesAsync()
    }

    fun deleteAccount(account: Account): Job = viewModelScope.launch {

        tranRepo.deleteAccount(account)
    }

    fun insertAccount(account: Account): Job = viewModelScope.launch {

        tranRepo.insertAccount(account)
    }

    fun updateAccount(account: Account): Job = viewModelScope.launch {

        tranRepo.updateAccount(account)
    }

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctAccountsAsync(): Deferred<List<String>> {

        return tranRepo.getDistinctAccountsAsync()
    }
}