package com.heyzeusv.plutuswallet.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.DELETE
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.EDIT
import com.heyzeusv.plutuswallet.util.Event
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

    // used to notify adapter of specific item change
    var accountAdapter: AccountAdapter? = null

    // list of all Account names used to prevent 2 Accounts from having same name
    var accountNames: MutableList<String> = mutableListOf()
    // list of Accounts unable to be deleted due to being used
    var accountsUsed: MutableList<String> = mutableListOf()

    // list of Accounts from Database
    val accountLD: LiveData<List<Account>> = tranRepo.getLDAccounts()

    private val _editAccountEvent = MutableLiveData<Event<Account>>()
    val editAccountEvent: LiveData<Event<Account>> = _editAccountEvent

    private val _existsAccountEvent = MutableLiveData<Event<String>>()
    val existsAccountEvent: LiveData<Event<String>> = _existsAccountEvent

    private val _deleteAccountEvent = MutableLiveData<Event<Account>>()
    val deleteAccountEvent: LiveData<Event<Account>> = _deleteAccountEvent

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
     *  Removes [account] from database
     */
    fun deleteAccount(account: DataInterface) {
        viewModelScope.launch {
            tranRepo.deleteAccount(account as Account)
        }
        updateDialog(DataDialog(DELETE, -1))
    }
    /**
     *  Event to edit name of selected [account].
     */
    fun editAccountOC(account: Account) {

        _editAccountEvent.value = Event(account)
    }

    /**
     *  Event to delete selected [account].
     */
    fun deleteAccountOC(account: Account) {

        _deleteAccountEvent.value = Event(account)
    }

    /**
     *  Initializes lists containing all Account names and Accounts being used.
     */
    suspend fun initNamesUsedLists() {
        accountNames = tranRepo.getAccountNamesAsync()
        accountsUsed = tranRepo.getDistinctAccountsAsync()
    }

    /**
     *  Positive button function for deleteAccountDialog.
     *  Removes [account] name from lists and deletes it from database.
     */
    fun deleteAccountPosFun(account: Account) {

        accountNames.remove(account.name)
        accountsUsed.remove(account.name)
        viewModelScope.launch {
            tranRepo.deleteAccount(account)
        }
    }

    /**
     *  If name exists, creates Snackbar telling user so, else updates [account] with [newName].
     */
    fun editAccount(account: DataInterface, newName: String) {
        val exists = _accountList.value.find { it.name == newName }
        if (exists != null) {
            updateAccountExists(newName)
        } else {
            account.name = newName
            viewModelScope.launch {
                tranRepo.updateAccount(account as Account)
            }
        }
        updateDialog(DataDialog(EDIT, -1))
    }

    /**
     *  If Account exists, creates SnackBar event telling user so,
     *  else creates and inserts [account] with [name].
     */
    fun insertNewAccount(account: Account, name: String) {

        if (accountNames.contains(name)) {
            _existsAccountEvent.value = Event(name)
        } else {
            // adds new name to list to prevent new Account with same name
            accountNames.add(name)
            account.name = name
            viewModelScope.launch {
                tranRepo.insertAccount(account)
            }
        }
    }
}