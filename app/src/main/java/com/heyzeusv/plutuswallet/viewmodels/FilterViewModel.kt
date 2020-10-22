package com.heyzeusv.plutuswallet.viewmodels

import android.app.DatePickerDialog
import android.view.View
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.utilities.DateUtils
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
class FilterViewModel @ViewModelInject constructor(
    private val tranRepo: TransactionRepository
) : ViewModel() {

    // current Account selected and Account list
    val account: MutableLiveData<String> = MutableLiveData("None")
    val accList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // type of Category selected and which is visible, true = "Expense" false = "Income"
    var typeVisible: MutableLiveData<Boolean> = MutableLiveData(true)

    // current Category selected and Category list, both by type
    val exCategory: MutableLiveData<String> = MutableLiveData("")
    val inCategory: MutableLiveData<String> = MutableLiveData("")
    val exCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val inCatList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    // Date values
    val startDate: MutableLiveData<Date> = MutableLiveData(DateUtils.startOfDay(Date()))
    val endDate: MutableLiveData<Date> =
        MutableLiveData(Date(startDate.value!!.time + MIDNIGHT_MILLI))

    // CheckBox status
    val accCheck: MutableLiveData<Boolean> = MutableLiveData(false)
    val catCheck: MutableLiveData<Boolean> = MutableLiveData(false)
    val dateCheck: MutableLiveData<Boolean> = MutableLiveData(false)

    // OnClickListeners for MaterialButtons
    val typeOnClick: MutableLiveData<View.OnClickListener> = MutableLiveData()
    val actionOnClick: MutableLiveData<View.OnClickListener> = MutableLiveData()

    /**
     *  Runs on Date button [view] on click. Creates DatePickerDialog and shows it.
     *  Uses different arguments depending on start/end button selected.
     */
    fun onDateClicked(view: View) {

        val dateDialog: DatePickerDialog = if (view.id == R.id.filter_start_date) {
            DateUtils.datePickerDialog(view, startDate.value!!, this::onDateSelectedStart)
        } else {
            DateUtils.datePickerDialog(view, endDate.value!!, this::onDateSelectedEnd)
        }
        dateDialog.show()
    }

    /**
     *  Takes [newDate] user selected on Start button and saves to be used in query
     */
    private fun onDateSelectedStart(newDate: Date) {

        startDate.value = newDate
    }

    /**
     *  Takes [newDate] user selected on End button and saves to be used in query
     */
    private fun onDateSelectedEnd(newDate: Date) {

        endDate.value = newDate
    }

    /**
     *  Retrieves data that will be displayed in Spinners from Repository.
     */
    fun prepareSpinners(all: String) {

        viewModelScope.launch {
            // Account data
            accList.value = tranRepo.getAccountNamesAsync().await()

            // Category by type data
            val mExCatList: MutableList<String> = getCategoriesByTypeAsync("Expense").await()
            val mInCatList: MutableList<String> = getCategoriesByTypeAsync("Income").await()
            mExCatList.add(0, all)
            mInCatList.add(0, all)
            exCatList.value = mExCatList
            inCatList.value = mInCatList

            // sets Spinner to previous value since it might have moved position in list
            exCategory.value = exCategory.value
            inCategory.value = inCategory.value
        }
    }

    /**
     *  Resets all filters.
     */
    fun resetFilter(all: String) {

        // sets the startDate to very start of current day and endDate to right before the next day
        startDate.value = DateUtils.startOfDay(Date())
        endDate.value = startDate.value

        // resets type Button and Spinner selections
        exCategory.value = all
        inCategory.value = all
    }

    /**
     *  Category Queries
     */
    private suspend fun getCategoriesByTypeAsync(type: String): Deferred<MutableList<String>> {

        return tranRepo.getCategoryNamesByTypeAsync(type)
    }
}