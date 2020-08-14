package com.heyzeusv.plutuswallet.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.utilities.Utils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import java.util.Date

private const val MIDNIGHT_MILLI = 86399999

/**
 *  Data manager for FilterFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class FilterViewModel : ViewModel() {

    // stores handle to TransactionRepository
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    // localized Strings
    var all     : String = ""
    var apply   : String = ""
    var end     : String = ""
    var expense : String = ""
    var income  : String = ""
    var reset   : String = ""
    var start   : String = ""
    var type    : String = ""

    // current Account selected and Account list
    val account : MutableLiveData<String>              = MutableLiveData("None")
    val accList : MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // type of Category selected and which is visible, true = "Expense" false = "Income"
    var typeSelected : MutableLiveData<String>  = MutableLiveData("")
    var typeVisible  : MutableLiveData<Boolean> = MutableLiveData(true)

    // current Category selected and Category list, both by type
    val exCategory : MutableLiveData<String>              = MutableLiveData("")
    val inCategory : MutableLiveData<String>              = MutableLiveData("")
    val exCatList  : MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val inCatList  : MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // Date values
    val startDate : MutableLiveData<Date> = MutableLiveData(Utils.startOfDay(Date()))
    val endDate   : MutableLiveData<Date> = MutableLiveData(Date(startDate.value!!.time + MIDNIGHT_MILLI))

    // CheckBox status
    val accCheck  : MutableLiveData<Boolean> = MutableLiveData(false)
    val catCheck  : MutableLiveData<Boolean> = MutableLiveData(false)
    val dateCheck : MutableLiveData<Boolean> = MutableLiveData(false)

    // OnClickListeners for MaterialButtons
    val typeOnClick   : MutableLiveData<View.OnClickListener> = MutableLiveData()
    val startOnClick  : MutableLiveData<View.OnClickListener> = MutableLiveData()
    val endOnClick    : MutableLiveData<View.OnClickListener> = MutableLiveData()
    val actionOnClick : MutableLiveData<View.OnClickListener> = MutableLiveData()

    /**
     *  Retrieves data that will be displayed in Spinners from Repository.
     */
    fun prepareSpinners() {

        viewModelScope.launch {
            
            // Account data
            accList.value = transactionRepository.getAccountNamesAsync().await()

            // Category by type data
            val mExCatList : MutableList<String> = getCategoriesByTypeAsync("Expense").await()
            val mInCatList : MutableList<String> = getCategoriesByTypeAsync("Income" ).await()
            mExCatList.add(0, all)
            mInCatList.add(0, all)
            exCatList.value = mExCatList
            inCatList.value = mInCatList
            // sets Spinner to previous value
            exCategory.value = exCategory.value
            inCategory.value = inCategory.value
        }
    }

    /**
     *  Resets all filters.
     */
    fun resetFilter() {

        // sets the startDate to very start of current day and endDate to right before the next day
        startDate.value = Utils.startOfDay(Date())
        endDate  .value = startDate.value

        // resets type Button and Spinner selections
        exCategory  .value = all
        inCategory  .value = all
        typeSelected.value = expense
    }

    /**
     *  Category Queries
     */
    private suspend fun getCategoriesByTypeAsync(type : String) : Deferred<MutableList<String>> {

        return transactionRepository.getCategoriesByTypeAsync(type)
    }
}