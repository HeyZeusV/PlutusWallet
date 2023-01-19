package com.heyzeusv.plutuswallet.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.CREATE
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.DELETE
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.EDIT
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 *  Data manager for CategoryFragment.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val tranRepo: Repository
) : ViewModel() {

    private val _accountList = MutableStateFlow(listOf<Account>())
    val accountList: StateFlow<List<Account>> get() = _accountList

    private val _accountsUsedList = MutableStateFlow(listOf<Account>())
    val accountsUsedList: StateFlow<List<Account>> get() = _accountsUsedList

    private val _showDialog = MutableStateFlow(DataDialog(DELETE, -1))
    val showDialog: StateFlow<DataDialog> get() = _showDialog
    fun updateDialog(newValue: DataDialog) { _showDialog.value = newValue }

    private val _accountExists = MutableStateFlow("")
    val accountExists: StateFlow<String> get() = _accountExists
    fun updateAccountExists(newValue: String) { _accountExists.value = newValue }


    init {
        viewModelScope.launch {
            tranRepo.getAccounts().collect { _accountList.value = it }
        }
        viewModelScope.launch {
            tranRepo.getAccountsUsed().collect { _accountsUsedList.value = it }
        }
    }

    /**
     *  Removes [account] from database.
     */
    fun deleteAccount(account: DataInterface) {
        viewModelScope.launch {
            tranRepo.deleteAccount(account as Account)
        }
        updateDialog(DataDialog(DELETE, -1))
    }

    /**
     *  If name exists, creates Snackbar telling user so, else updates [account] with [newName].
     */
    fun editAccount(account: DataInterface, newName: String) {
        val exists = _accountList.value.find { it.name == newName }
        if (exists != null) {
            updateAccountExists(newName)
        } else {
            viewModelScope.launch {
                account.name = newName
                tranRepo.updateAccount(account as Account)
            }
        }
        updateDialog(DataDialog(EDIT, -1))
    }

    /**
     *  If Account exists, creates SnackBar telling user so, else creates Account with [name].
     */
    fun createNewAccount(name: String) {
        val exists = _accountList.value.find { it.name == name }
        if (exists != null) {
            updateAccountExists(name)
        } else {
            viewModelScope.launch {
                tranRepo.insertAccount(Account(0, name))
            }
        }
        updateDialog(DataDialog(CREATE, -1))
    }
}