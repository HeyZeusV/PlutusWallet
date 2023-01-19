package com.heyzeusv.plutuswallet.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.DELETE
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.EDIT
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.INCOME
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
) : ViewModel() {

    private val _expenseCatList = MutableStateFlow(listOf<Category>())
    val expenseCatList: StateFlow<List<Category>> get() = _expenseCatList

    private val _incomeCatList = MutableStateFlow(listOf<Category>())
    val incomeCatList: StateFlow<List<Category>> get() = _incomeCatList

    private val _expenseCatUsedList = MutableStateFlow(listOf<Category>())
    val expenseCatUsedList: StateFlow<List<Category>> get() = _expenseCatUsedList

    private val _incomeCatUsedList = MutableStateFlow(listOf<Category>())
    val incomeCatUsedList: StateFlow<List<Category>> get() = _incomeCatUsedList

    private val _showDialog = MutableStateFlow(DataDialog(DELETE, -1))
    val showDialog: StateFlow<DataDialog> get() = _showDialog
    fun updateDialog(newValue: DataDialog) { _showDialog.value = newValue }

    private val _categoryExists = MutableStateFlow("")
    val categoryExists: StateFlow<String> get() = _categoryExists
    fun updateCategoryExists(newValue: String) { _categoryExists.value = newValue }

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
     *  Removes [category] from database.
     */
    fun deleteCategory(category: DataInterface) {
        viewModelScope.launch {
            tranRepo.deleteCategory(category as Category)
        }
        updateDialog(DataDialog(DELETE, -1))
    }

    /**
     *  If name exists, creates Snackbar telling user so, else updates [category] with [newName]
     */
    fun editCategory(data: DataInterface, newName: String) {
        val category = (data as Category)
        val exists = if (category.type == EXPENSE.type) {
            _expenseCatList.value.find { it.name == newName }
        } else {
            _incomeCatList.value.find { it.name == newName }
        }
        if (exists != null) {
            updateCategoryExists(newName)
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
    fun createNewCategory(name: String) {
        // use type passed along with DataDialog to determine if Expense or Income Category
        val type = showDialog.value.type
        val exists = if (type == EXPENSE) {
            _expenseCatList.value.find { it.name == name }
        } else {
            _incomeCatList.value.find { it.name == name }
        }
        if (exists != null) {
            updateCategoryExists(name)
        } else {
            viewModelScope.launch {
                val newCategory = Category(0, name, type.type)
                tranRepo.insertCategory(newCategory)
            }
        }
        updateDialog(DataDialog(EDIT, -1, type))
    }
}