package com.heyzeusv.plutuswallet.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.utilities.adapters.AccountAdapter
import com.heyzeusv.plutuswallet.utilities.replace
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 *  Data manager for CategoryFragment.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class AccountViewModel @ViewModelInject constructor(
    private val tranRepo: TransactionRepository
) : ViewModel() {

    // used to notify adapter of specific item change
    lateinit var accountAdapter: AccountAdapter

    // list of all Account names used to prevent 2 Accounts from having same name
    var accountNames: MutableList<String> = mutableListOf()
    // list of Accounts unable to be deleted due to being used
    var accountsUsed: MutableList<String> = mutableListOf()

    // used for various Events
    val editAccount: MutableLiveData<Account?> = MutableLiveData()
    val deleteAccount: MutableLiveData<Account?> = MutableLiveData()
    val existsAccount: MutableLiveData<String?> = MutableLiveData()

    /**
     *  Event to edit name of selected [account].
     */
    fun editAccountOC(account: Account) {

        editAccount.value = account
    }

    /**
     *  Event to delete selected [account].
     */
    fun deleteAccountOC(account: Account) {

        deleteAccount.value = account
    }

    /**
     *  If name exists, creates Snackbar event telling user so,
     *  else updates Account with [newName].
     */
    fun editAccountName(newName: String) {

        if (accountNames.contains(newName)) {
            existsAccount.value = newName
        } else {
            val account: Account = editAccount.value!!
            // replaces previous name in lists with new value
            accountNames.replace(account.account, newName)
            if (accountsUsed.contains(account.account)) {
                accountsUsed.replace(account.account, newName)
            }
            account.account = newName
            updateAccount(account)
            // DiffUtil would not update the name change,
            // so notifying specific item change rather than entire list
            accountAdapter.notifyItemChanged(accountLD.value!!.indexOf(account))
        }
    }

    /**
     *  If Account exists, creates SnackBar event telling user so,
     *  else creates and inserts new Account with [name].
     */
    fun insertNewAccount(name: String) {

        if (accountNames.contains(name)) {
            existsAccount.value = name
        } else {
            // adds new name to list to prevent new Account with same name
            accountNames.add(name)
            val account = Account(0, name)
            insertAccount(account)
        }
    }

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

    private fun insertAccount(account: Account): Job = viewModelScope.launch {

        tranRepo.insertAccount(account)
    }

    private fun updateAccount(account: Account): Job = viewModelScope.launch {

        tranRepo.updateAccount(account)
    }

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctAccountsAsync(): Deferred<MutableList<String>> {

        return tranRepo.getDistinctAccountsAsync()
    }
}