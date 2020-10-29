package com.heyzeusv.plutuswallet.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.utilities.Event
import com.heyzeusv.plutuswallet.utilities.adapters.CategoryAdapter
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
class CategoryViewModel @ViewModelInject constructor(
    private val tranRepo: TransactionRepository
) : ViewModel() {

    // used to notify adapter of specific item change
    lateinit var expenseAdapter: CategoryAdapter
    lateinit var incomeAdapter: CategoryAdapter

    // list of Categories by type used to prevent 2 Categories from having same name
    // 0 = "Expense"; 1 = "Income"
    val catNames: MutableList<MutableList<String>> = mutableListOf(mutableListOf(), mutableListOf())
    // list of Categories by type unable to be deleted due to being used
    // 0 = "Expense"; 1 = "Income"
    val catsUsed: MutableList<MutableList<String>> = mutableListOf(mutableListOf(), mutableListOf())

    private val _editCategoryEvent = MutableLiveData<Event<Category>>()
    val editCategoryEvent: LiveData<Event<Category>> = _editCategoryEvent

    private val _existsCategoryEvent = MutableLiveData<Event<String>>()
    val existsCategoryEvent: LiveData<Event<String>> = _existsCategoryEvent

    private val _deleteCategoryEvent = MutableLiveData<Event<Category>>()
    val deleteCategoryEvent: LiveData<Event<Category>> = _deleteCategoryEvent

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
     *  Initializes [type] lists containing all Category names
     *  and Categories being used into catNames/catUsed[pos].
     */
    suspend fun initNamesUsedLists(type: String, pos: Int) {

        catNames[pos] = getCatsByTypeAsync(type).await()
        catsUsed[pos] = getDistinctCatsByTypeAsync(type).await()
    }

    /**
     *  Positive button function for deleteCategoryDialog.
     *  Removes [category] name from [type] lists and deletes it from database.
     */
    fun deleteCategoryPosFun(category: Category, type: Int) {

        catNames[type].remove(category.category)
        catsUsed[type].remove(category.category)
        deleteCategory(category)
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
            catNames[type].replace(category.category, newName)
            if (catsUsed[type].contains(category.category)) {
                catsUsed[type].replace(category.category, newName)
            }
            category.category = newName
            updateCategory(category)
            // DiffUtil would not update the name change,
            // so notifying specific item change rather than entire list
            if (type == 0) {
                expenseAdapter.notifyItemChanged(expenseCatsLD.value!!.indexOf(category))
            } else {
                incomeAdapter.notifyItemChanged(incomeCatsLD.value!!.indexOf(category))
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
            category.category = name
            category.type = if (type == 0) "Expense" else "Income"
            insertCategory(category)
        }
    }

    /**
     *  Category Queries
     */
    val expenseCatsLD: LiveData<List<Category>> = tranRepo.getLDCategoriesByType("Expense")
    val incomeCatsLD: LiveData<List<Category>> = tranRepo.getLDCategoriesByType("Income")

    private suspend fun getCatsByTypeAsync(type: String): Deferred<MutableList<String>> {

        return tranRepo.getCategoryNamesByTypeAsync(type)
    }

    private fun deleteCategory(category: Category): Job = viewModelScope.launch {

        tranRepo.deleteCategory(category)
    }

    private fun insertCategory(category: Category): Job = viewModelScope.launch {

        tranRepo.insertCategory(category)
    }

    private fun updateCategory(category: Category): Job = viewModelScope.launch {

        tranRepo.updateCategory(category)
    }

    /**
     *  Transaction Queries
     */
    private suspend fun getDistinctCatsByTypeAsync(type: String): Deferred<MutableList<String>> {

        return tranRepo.getDistinctCatsByTypeAsync(type)
    }
}