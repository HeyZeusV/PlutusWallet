package com.heyzeusv.plutuswallet.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.util.DataListSelectedAction.DELETE
import com.heyzeusv.plutuswallet.util.DataListSelectedAction.EDIT
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
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
class CategoryViewModel @Inject constructor(
    private val tranRepo: Repository
) : ViewModel(), ListViewModel {

    // string ids used by Composables
    override val createItemStringId: Int = R.string.alert_dialog_create_category
    override val deleteItemStringId: Int = R.string.alert_dialog_delete_category
    override val editItemStringId: Int = R.string.alert_dialog_edit_category
    override val listSubtitleStringIds: List<Int> =
        listOf(R.string.type_expense, R.string.type_income)

    // Categories to be displayed
    private val _expenseCatList = MutableStateFlow(listOf<Category>())
    override val firstItemList: StateFlow<List<Category>> get() = _expenseCatList

    private val _incomeCatList = MutableStateFlow(listOf<Category>())
    override val secondItemList: StateFlow<List<Category>> get() = _incomeCatList

    // Categories that are tied to Transactions
    private val _expenseCatUsedList = MutableStateFlow(listOf<Category>())
    override val firstUsedItemList: StateFlow<List<Category>> get() = _expenseCatUsedList

    private val _incomeCatUsedList = MutableStateFlow(listOf<Category>())
    override val secondUsedItemList: StateFlow<List<Category>> get() = _incomeCatUsedList

    // name of Category that already exists
    private val _categoryExists = MutableStateFlow("")
    override val itemExists: StateFlow<String> get() = _categoryExists
    override fun updateItemExists(value: String) { _categoryExists.value = value }

    // display different dialog depending on Action
    private val _showDialog = MutableStateFlow(DataDialog(DELETE, -1))
    override val showDialog: StateFlow<DataDialog> get() = _showDialog
    override fun updateDialog(newValue: DataDialog) { _showDialog.value = newValue }

    init {
        viewModelScope.launch {
            tranRepo.getCategoriesByType(EXPENSE.type).collect { _expenseCatList.value = it }
        }
        viewModelScope.launch {
            tranRepo.getCategoriesByType(INCOME.type).collect { _incomeCatList.value = it }
        }
        viewModelScope.launch {
            tranRepo.getCategoriesUsedByType(EXPENSE.type).collect { _expenseCatUsedList.value = it }
        }
        viewModelScope.launch {
            tranRepo.getCategoriesUsedByType(INCOME.type).collect { _incomeCatUsedList.value = it }
        }
    }

    /**
     *  Removes [item] from database.
     */
    override fun deleteItem(item: DataInterface) {
        viewModelScope.launch {
            tranRepo.deleteCategory(item as Category)
        }
        updateDialog(DataDialog(DELETE, -1))
    }

    /**
     *  If name exists, creates Snackbar telling user so, else updates [item] with [newName]
     */
    override fun editItem(item: DataInterface, newName: String) {
        val category = (item as Category)
        val exists = if (category.type == EXPENSE.type) {
            _expenseCatList.value.find { it.name == newName }
        } else {
            _incomeCatList.value.find { it.name == newName }
        }
        if (exists != null) {
            updateItemExists(newName)
        } else {
            viewModelScope.launch {
                category.name = newName
                tranRepo.updateCategory(category)
            }
        }
        updateDialog(DataDialog(EDIT, -1))
    }

    /**
     *  If Category exists, creates SnackBar telling user so, else creates Category with [name].
     */
    override fun insertItem(name: String) {
        // use type passed along with DataDialog to determine if Expense or Income Category
        val type = showDialog.value.type
        val exists = if (type == EXPENSE) {
            _expenseCatList.value.find { it.name == name }
        } else {
            _incomeCatList.value.find { it.name == name }
        }
        if (exists != null) {
            updateItemExists(name)
        } else {
            viewModelScope.launch {
                val newCategory = Category(0, name, type.type)
                tranRepo.insertCategory(newCategory)
            }
        }
        updateDialog(DataDialog(EDIT, -1, type))
    }
}