package com.heyzeusv.plutuswallet.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.Transaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 *  Data manager for TransactionFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class TransactionViewModel : ViewModel() {

    // stores handle to TransactionRepository
    private val tranRepo : TransactionRepository = TransactionRepository.get()

    // stores ID of Transaction displayed.
    private val tranIdLD = MutableLiveData<Int>()

    // manually refresh on LiveData
    private fun refresh() { tranIdLD.postValue(tranIdLD.value) }

    /**
     *  Sets up a trigger-response relationship.
     *
     *  LiveData object used as trigger and mapping function that must return LiveData object.
     *
     *  @return LiveData object holding a Transaction that gets updated every time a
     *          new value gets set on the trigger LiveData instance.
     */
    var tranLD : MutableLiveData<Transaction?> =
        Transformations.switchMap(tranIdLD) { transactionId : Int ->
            tranRepo.getLDTransaction(transactionId)
        } as MutableLiveData<Transaction?>

    // used for various Transaction Fields since property changes don't cause LiveDate updates
    val date        : MutableLiveData<String>  = MutableLiveData("")
    val account     : MutableLiveData<String>  = MutableLiveData("")
    val total       : MutableLiveData<String>  = MutableLiveData("")
    val checkedChip : MutableLiveData<Int>     = MutableLiveData(R.id.tran_expense_chip)
    val expenseCat  : MutableLiveData<String>  = MutableLiveData("")
    val incomeCat   : MutableLiveData<String>  = MutableLiveData("")
    val repeatCheck : MutableLiveData<Boolean> = MutableLiveData(false)

    // Lists used by Spinners
    val accountList    : MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val expenseCatList : MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val incomeCatList  : MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val periodArray    : MutableLiveData<List<String>>        = MutableLiveData(emptyList())

    // OnClick defined in Fragment
    val dateOnClick : MutableLiveData<View.OnClickListener> = MutableLiveData()
    val saveOnClick : MutableLiveData<View.OnClickListener> = MutableLiveData()

    var maxId : Int = 0
    // used to tell if date has been edited for re-repeating Transactions
    var dateChanged = false

    /**
     *  Takes given list, removes "Create New..", adds new entry, sorts list, re-adds
     *  "Create New..", and returns list.
     *
     *  @param list   to be edited.
     *  @param name   of new entry.
     *  @param create translated "Create New.."
     */
    private fun addNewToList(list : MutableList<String>, name : String, create : String)
            : MutableList<String> {

        list.remove(create)
        list.add(name)
        list.sort()
        list.add(create)
        return list
    }

    /**
     *  Adds frequency * period to the date on Transaction.
     *
     *  @return Date object with updated date.
     */
    fun createFutureDate() : Date {

        val calendar : Calendar = Calendar.getInstance()
        tranLD.value?.let {

            // set to Transaction date rather than current time due to Users being able
            // to select a Date in the past or future
            calendar.time = it.date

            // 0 = Day, 1 = Week, 2 = Month, 3 = Year
            calendar.add(when (it.period) {
                0 -> Calendar.DAY_OF_MONTH
                1 -> Calendar.WEEK_OF_YEAR
                2 -> Calendar.MONTH
                else -> Calendar.YEAR
            }, it.frequency)
        }

        return calendar.time
    }

    /**
     *  Inserts new Account into list or selects it in Spinner if it exists already.
     *
     *  @param name      the Account to be inserted/selected.
     *  @param accCreate translated "Create New Account."
     */
    @ExperimentalStdlibApi
    fun insertAccount(name : String, accCreate : String) {

        accountList.value?.let {

            // create if doesn't exist
            if (!it.contains(name)) {

                viewModelScope.launch {

                    // creates and inserts new Account with name
                    val account = Account(0, name)
                    insertAccount(account)
                }

                accountList.value = addNewToList(it, name, accCreate)
            }
            account.value = name
        }
    }

    /**
     *  Inserts new Category into database or selects it in Spinner if it exists already.
     *
     *  @param name      the category to be inserted/selected.
     *  @param catCreate translated "Create New Category."
     */
    @ExperimentalStdlibApi
    fun insertCategory(name : String, catCreate : String) {

        when (checkedChip.value) {
            R.id.tran_expense_chip -> {
                expenseCatList.value?.let {

                    // create if doesn't exist
                    if (!it.contains(name)) {

                        viewModelScope.launch {

                            // creates and inserts new Category with name
                            val category = Category(0, name, "Expense")
                            insertCategory(category)
                        }

                        expenseCatList.value = addNewToList(it, name, catCreate)
                    }
                }
                expenseCat.value = name
            }
            else -> {
                incomeCatList.value?.let {

                    // create if doesn't exist
                    if (!it.contains(name)) {

                        viewModelScope.launch {

                            // creates and inserts new Category with name
                            val category = Category(0, name, "Income")
                            insertCategory(category)
                        }

                        incomeCatList.value = addNewToList(it, name, catCreate)
                    }
                }
                incomeCat.value = name
            }
        }
    }

    /**
     *  Retrieves list of Accounts/Categories and highest ID from database, then refreshes tranIdLd
     *  in order to refresh account/category values.
     *
     *  @param accCreate translated "Create New Account."
     *  @param catCreate translated "Create New Category."
     */
    fun prepareLists(accCreate : String, catCreate : String) {

        viewModelScope.launch {

            accountList.value = getAccountsAsync().await()
            accountList.value!!.add(accCreate)
            expenseCatList.value = getCategoriesByTypeAsync("Expense").await()
            expenseCatList.value!!.add(catCreate)
            incomeCatList.value = getCategoriesByTypeAsync("Income").await()
            incomeCatList.value!!.add(catCreate)
            maxId = getMaxIdAsync().await() ?: 0
            refresh()
        }
    }

    /**
     *  Account queries
     */
    private suspend fun insertAccount(account : Account) {

        tranRepo.insertAccount(account)
    }

    /**
     *  Category queries
     */
    private suspend fun getCategoriesByTypeAsync(type : String) : Deferred<MutableList<String>> {

        return tranRepo.getCategoriesByTypeAsync(type)
    }

    private suspend fun insertCategory(category : Category) {

        tranRepo.insertCategory(category)
    }

    /**
     *  Transaction queries.
     */
    /**
     *  'Loads' Transaction.
     *
     *  Doesn't load Transaction directly from Database, but rather by updating the
     *  LiveData object holding ID which in turn triggers mapping function above.
     *
     *  @param transactionId Id of Transaction to be loaded.
     */
    fun loadTransaction(transactionId : Int) {

        tranIdLD.value = transactionId
    }

    private suspend fun getAccountsAsync() : Deferred<MutableList<String>> {

        return tranRepo.getAccountsAsync()
    }

    private suspend fun getMaxIdAsync() : Deferred<Int?> {

        return tranRepo.getMaxIdAsync()
    }

    suspend fun upsertTransaction(transaction : Transaction) {

        tranRepo.upsertTransaction(transaction)
    }
}