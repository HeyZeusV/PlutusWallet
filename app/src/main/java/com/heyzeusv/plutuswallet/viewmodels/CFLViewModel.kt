package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo

/**
 *  Data manager for shared data between Filter/Graph/TransactionList Fragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class CFLViewModel : ViewModel() {

    // stores TransactionInfo object
    var tInfoLiveData = MutableLiveData<TransactionInfo>()

    // will be used by TransactionListFragment to tell when a new filter is applied/reset in order
    // to scroll back to top of the list
    var filterChanged : Boolean = false

    /**
     *  Updates tInfoLiveData which in turn will set off any Observers attached.
     *
     *  @param newValue easier to send entirely new TransactionInfo object rather
     *         than update each property separately.
     */
    fun updateTInfo(newValue : TransactionInfo) {

        tInfoLiveData.value = newValue
    }

    init {

        // will only be used at app start up which will show all Transactions
        tInfoLiveData.value = TransactionInfo(null, null, null, null,
            null, null, null, null)
    }
}