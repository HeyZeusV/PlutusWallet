package com.heyzeusv.plutuswallet.ui.cfl

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.data.model.FilterInfo

/**
 *  Data manager for shared data between Filter/Graph/TransactionList Fragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class CFLViewModel : ViewModel() {

    // stores FilterInfo object
    var tInfoLiveData = MutableLiveData<FilterInfo>()

    // will be used by TransactionListFragment to tell when a new filter is applied/reset in order
    // to scroll back to top of the list
    var filterChanged: Boolean = false

    init {
        // will only be used at app start up which will show all Transactions
        tInfoLiveData.value = FilterInfo()
    }

    /**
     *  Updates tInfoLiveData with [newValue] which in turn will set off any Observers attached.
     */
    fun updateTInfo(newValue: FilterInfo) {

        tInfoLiveData.value = newValue
    }
}