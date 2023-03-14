package com.heyzeusv.plutuswallet.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.ListDialog
import com.heyzeusv.plutuswallet.data.model.ListItemInterface
import com.heyzeusv.plutuswallet.util.ListItemAction.DELETE
import com.heyzeusv.plutuswallet.util.ListItemAction.EDIT
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
    private val tranRepo: PWRepositoryInterface
) : ViewModel(), ListViewModel {

    // string ids used by Composables
    override val createItemStringId: Int = R.string.alert_dialog_create_account
    override val deleteItemStringId: Int = R.string.alert_dialog_delete_account
    override val editItemStringId: Int = R.string.alert_dialog_edit_account
    override val listSubtitleStringIds: List<Int> = emptyList()

    // Accounts to be displayed
    private val _accountList = MutableStateFlow(listOf<Account>())
    override val firstItemList: StateFlow<List<ListItemInterface>> get() = _accountList

    // used by Categories, so can be empty here
    private val _emptyList = MutableStateFlow(emptyList<Account>())
    override val secondItemList: StateFlow<List<ListItemInterface>> get() = _emptyList

    // Accounts that are tied to Transactions
    private val _accountsUsedList = MutableStateFlow(listOf<Account>())
    override val firstUsedItemList: StateFlow<List<ListItemInterface>> get() = _accountsUsedList

    // used by Categories, so can be empty here
    override val secondUsedItemList: StateFlow<List<ListItemInterface>> get() = _emptyList

    // name of Account that already exists
    private val _accountExists = MutableStateFlow("")
    override val itemExists: StateFlow<String> get() = _accountExists
    override fun updateItemExists(value: String) { _accountExists.value = value }

    // display different dialog depending on Action
    private val _showDialog = MutableStateFlow(ListDialog(DELETE, -1))
    override val showDialog: StateFlow<ListDialog> get() = _showDialog
    override fun updateDialog(newValue: ListDialog) { _showDialog.value = newValue }

    init {
        viewModelScope.launch {
            tranRepo.getAccounts().collect {
                _accountList.value = it }
        }
        viewModelScope.launch {
            tranRepo.getAccountsUsed().collect { _accountsUsedList.value = it }
        }
    }

    /**
     *  Removes [item] from database.
     */
    override fun deleteItem(item: ListItemInterface) {
        viewModelScope.launch {
            tranRepo.deleteAccount(item as Account)
        }
        updateDialog(ListDialog(DELETE, -1))
    }

    /**
     *  If name exists, creates Snackbar telling user so, else updates [item] with [newName].
     */
    override fun editItem(item: ListItemInterface, newName: String) {
        val exists = _accountList.value.find { it.name == newName }
        if (exists != null) {
            updateItemExists(newName)
        } else {
            viewModelScope.launch {
                item.name = newName
                tranRepo.updateAccount(item as Account)
            }
        }
        updateDialog(ListDialog(EDIT, -1))
    }

    /**
     *  If Account exists, creates SnackBar telling user so, else creates Account with [name].
     */
    override fun insertItem(name: String) {
        val exists = _accountList.value.find { it.name == name }
        if (exists != null) {
            updateItemExists(name)
        } else {
            viewModelScope.launch {
                tranRepo.insertAccount(Account(0, name))
            }
        }
        updateDialog(ListDialog(EDIT, -1))
    }
}