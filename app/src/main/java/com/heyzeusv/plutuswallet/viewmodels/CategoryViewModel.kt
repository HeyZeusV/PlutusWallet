package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Category
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 *  Data manager for CategoriesFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class CategoryViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */
    private val tranRepo : TransactionRepository = TransactionRepository.get()

    // combined lists of LiveData values from below
    val catLists : MutableList<List<Category>> = mutableListOf(emptyList(), emptyList())

    // list shown by ViewPager, 0 = "Expense"; 1 = "Income"
    var listShown = 0

    // list of Categories by type used to prevent 2 Categories from having same name
    // 0 = "Expense"; 1 = "Income"
    val catNames : MutableList<List<String>> = mutableListOf(emptyList(), emptyList())

    // list of Categories by type unable to be deleted due to being used
    // 0 = "Expense"; 1 = "Income"
    val catsUsed : MutableList<List<String>> = mutableListOf(emptyList(), emptyList())

    /**
     *  Category Queries
     */
    val expenseCatsLD : LiveData<List<Category>> = tranRepo.getLDCategoriesByType("Expense")
    val incomeCatsLD  : LiveData<List<Category>> = tranRepo.getLDCategoriesByType("Income" )

    suspend fun getCatsByTypeAsync(type : String) : Deferred<MutableList<String>> {

        return tranRepo.getCategoryNamesByTypeAsync(type)
    }

    fun deleteCategory(category : Category) : Job = viewModelScope.launch {

        tranRepo.deleteCategory(category)
    }

    fun insertCategory(category : Category) : Job = viewModelScope.launch {

        tranRepo.insertCategory(category)
    }

    fun updateCategory(category : Category) : Job = viewModelScope.launch {

        tranRepo.updateCategory(category)
    }

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctCatsByTypeAsync(type : String) : Deferred<List<String>> {

        return tranRepo.getDistinctCatsByTypeAsync(type)
    }
}