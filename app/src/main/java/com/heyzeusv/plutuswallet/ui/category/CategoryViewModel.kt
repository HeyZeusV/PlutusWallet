package com.heyzeusv.plutuswallet.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.DELETE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.Event
import com.heyzeusv.plutuswallet.util.replace
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

    // used to notify adapter of specific item change
    var expenseAdapter: CategoryAdapter? = null
    var incomeAdapter: CategoryAdapter? = null

    // list of Categories by type used to prevent 2 Categories from having same name
    // 0 = "Expense"; 1 = "Income"
    val catNames: MutableList<MutableList<String>> = mutableListOf(mutableListOf(), mutableListOf())
    // list of Categories by type unable to be deleted due to being used
    // 0 = "Expense"; 1 = "Income"
    val catsUsed: MutableList<MutableList<String>> = mutableListOf(mutableListOf(), mutableListOf())

    // list of Categories from Database
    val expenseCatsLD: LiveData<List<Category>> = tranRepo.getLDCategoriesByType("Expense")
    val incomeCatsLD: LiveData<List<Category>> = tranRepo.getLDCategoriesByType("Income")

    private val _editCategoryEvent = MutableLiveData<Event<Category>>()
    val editCategoryEvent: LiveData<Event<Category>> = _editCategoryEvent

    private val _existsCategoryEvent = MutableLiveData<Event<String>>()
    val existsCategoryEvent: LiveData<Event<String>> = _existsCategoryEvent

    private val _deleteCategoryEvent = MutableLiveData<Event<Category>>()
    val deleteCategoryEvent: LiveData<Event<Category>> = _deleteCategoryEvent

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
     *  Event to edit name of selected [category].
     */
    fun editCategoryOC(category: Category) {

        _editCategoryEvent.value = Event(category)
    }

    /**
     *  Event to delete selected [category].
     */
    fun deleteCategoryOC(category: Category) {

        _deleteCategoryEvent.value = Event(category)
    }

    /**
     *  Event to edit name of selected [category].
     */
    fun editCategory(category: DataInterface, newName: String) {

    }

    /**
     *  Event to delete selected [category].
     */
    fun deleteCategory(category: DataInterface) {


    }

    /**
     *  Initializes [type] lists containing all Category names
     *  and Categories being used into catNames/catUsed[pos].
     */
    suspend fun initNamesUsedLists(type: String, pos: Int) {

        catNames[pos] = tranRepo.getCategoryNamesByTypeAsync(type)
        catsUsed[pos] = tranRepo.getDistinctCatsByTypeAsync(type)
    }

    /**
     *  Positive button function for deleteCategoryDialog.
     *  Removes [category] name from [type] lists and deletes it from database.
     */
    fun deleteCategoryPosFun(category: Category, type: Int) {

        catNames[type].remove(category.name)
        catsUsed[type].remove(category.name)
        viewModelScope.launch {
            tranRepo.deleteCategory(category)
        }
    }

    /**
     *  If name exists, creates Snackbar event telling user so,
     *  else updates [category] in [type] list with [newName].
     */
    fun editCategoryName(category: Category, newName: String, type: Int) {

        if (catNames[type].contains(newName)) {
            _existsCategoryEvent.value = Event(newName)
        } else {
            // replaces previous name in lists with new value
            catNames[type].replace(category.name, newName)
            if (catsUsed[type].contains(category.name)) {
                catsUsed[type].replace(category.name, newName)
            }
            category.name = newName
            viewModelScope.launch {
                tranRepo.updateCategory(category)
            }
            // DiffUtil would not update the name change,
            // so notifying specific item change rather than entire list
            if (type == 0) {
                expenseAdapter?.notifyItemChanged(expenseCatsLD.value!!.indexOf(category))
            } else {
                incomeAdapter?.notifyItemChanged(incomeCatsLD.value!!.indexOf(category))
            }
        }
    }

    /**
     *  If Category exists, creates SnackBar event telling user so,
     *  else creates and inserts [category] in [type] list with [name].
     */
    fun insertNewCategory(category: Category, name: String, type: Int) {

        if (catNames[type].contains(name)) {
            _existsCategoryEvent.value = Event(name)
        } else {
            // adds new name to list to prevent new Category with same name
            catNames[type].add(name)
            category.name = name
            category.type = if (type == 0) "Expense" else "Income"
            viewModelScope.launch {
                tranRepo.insertCategory(category)
            }
        }
    }

    /**
     *  If Category exists, creates SnackBar event telling user so,
     *  else creates and inserts [category] in [type] list with [name].
     */
    fun createNewCategory(name: String /*, type: Int*/) {

//        if (catNames[type].contains(name)) {
//            _existsCategoryEvent.value = Event(name)
//        } else {
//            // adds new name to list to prevent new Category with same name
//            catNames[type].add(name)
//            category.name = name
//            category.type = if (type == 0) "Expense" else "Income"
//            viewModelScope.launch {
//                tranRepo.insertCategory(category)
//            }
//        }
    }
}