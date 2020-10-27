package com.heyzeusv.plutuswallet.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Category
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

    val editCategory: MutableLiveData<Category?> = MutableLiveData()
    val deleteCategory: MutableLiveData<Category?> = MutableLiveData()
    val existsCategory: MutableLiveData<String?> = MutableLiveData()

    fun editCategoryOC(category: Category) {

        editCategory.value = category
    }

    fun deleteCategoryOC(category: Category) {

        deleteCategory.value = category
    }

    fun editCategoryName(newName: String, type: Int) {

        if (catNames[type].contains(newName)) {
            existsCategory.value = newName
        } else {
            val category: Category = editCategory.value!!
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

    fun insertNewCategory(name: String, type: Int) {

        if (catNames[type].contains(name)) {
            existsCategory.value = name
        } else {
            // adds new name to list to prevent new Category with same name
            catNames[type].add(name)
            val category = Category(0, name, if (type == 0) "Expense" else "Income")
            insertCategory(category)
        }
    }

    /**
     *  Category Queries
     */
    val expenseCatsLD: LiveData<List<Category>> = tranRepo.getLDCategoriesByType("Expense")
    val incomeCatsLD: LiveData<List<Category>> = tranRepo.getLDCategoriesByType("Income")

    suspend fun getCatsByTypeAsync(type: String): Deferred<MutableList<String>> {

        return tranRepo.getCategoryNamesByTypeAsync(type)
    }

    fun deleteCategory(category: Category): Job = viewModelScope.launch {

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
    suspend fun getDistinctCatsByTypeAsync(type: String): Deferred<MutableList<String>> {

        return tranRepo.getDistinctCatsByTypeAsync(type)
    }
}