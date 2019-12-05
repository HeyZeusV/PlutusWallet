package com.heyzeusv.plutuswallet.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.ExpenseCategory
import com.heyzeusv.plutuswallet.database.entities.IncomeCategory
import com.heyzeusv.plutuswallet.database.entities.Transaction
import kotlinx.coroutines.Deferred

/**
 *  Data manager for TransactionFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class TransactionDetailViewModel : ViewModel() {

    // true = Expense, false = Income
    val categorySpinVisibility : MutableLiveData<Boolean> = MutableLiveData(true)
    val frequencyVisibility : MutableLiveData<Boolean> = MutableLiveData(false)
    val title : MutableLiveData<String> = MutableLiveData("")
    val memo  : MutableLiveData<String> = MutableLiveData("")
    val frequency : MutableLiveData<String> = MutableLiveData("1")
    val type : MutableLiveData<String> = MutableLiveData("Expense")
    val chipSelected : MutableLiveData<Int> = MutableLiveData()

    init {

        Log.d("TEST", "HERE")

    }

    fun setTitle(newTitle : String) {

        title.value = newTitle
    }
    fun setCategorySpinVisibility(state : Boolean) {

        categorySpinVisibility.value = state
    }

    /**
     *  Repository/Queries
     */
    /**
     *  Stores handle to TransactionRepository.
     */
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    /**
     *  Transaction queries.
     */
    // stores ID of Transaction displayed.
    private val transactionIdLiveData = MutableLiveData<Int>()

    /**
     *  Sets up a trigger-response relationship.
     *
     *  LiveData object used as trigger and mapping function that must return LiveData object.
     *
     *  @return LiveData object holding a Transaction that gets updated every time a
     *          new value gets set on the trigger LiveData instance.
     */
    var transactionLiveData : LiveData<Transaction> =
        Transformations.switchMap(transactionIdLiveData) { transactionId : Int ->
            transactionRepository.getLDTransaction(transactionId)
        }


    /**
     *  'Loads' Transaction.
     *
     *  Doesn't load Transaction directly from Database, but rather by updating the
     *  LiveData object holding ID which in turn triggers mapping function above.
     *
     *  @param transactionId Id of Transaction to be loaded.
     */
    fun loadTransaction(transactionId : Int) {

        transactionIdLiveData.value = transactionId
    }

    suspend fun getMaxIdAsync() : Deferred<Int?> {

        return transactionRepository.getMaxIdAsync()
    }

    suspend fun insertTransaction(transaction : Transaction) {

        transactionRepository.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction : Transaction) {

        transactionRepository.updateTransaction(transaction)
    }

    /**
     *  ExpenseCategory queries.
     */
    suspend fun getExpenseCategoryNamesAsync() : Deferred<List<String>> {

        return transactionRepository.getExpenseCategoryNamesAsync()
    }

    suspend fun insertExpenseCategory(expenseCategory : ExpenseCategory) {

        transactionRepository.insertExpenseCategory(expenseCategory)
    }

    /**
     *  IncomeCategory queries.
     */
    suspend fun getIncomeCategoryNamesAsync() : Deferred<List<String>> {

        return transactionRepository.getIncomeCategoryNamesAsync()
    }

    suspend fun insertIncomeCategory(incomeCategory : IncomeCategory) {

        transactionRepository.insertIncomeCategory(incomeCategory)
    }
}