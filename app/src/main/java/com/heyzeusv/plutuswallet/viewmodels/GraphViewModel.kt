package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.CategoryTotals
import java.util.Date

/**
 *  Data manager for GraphFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class GraphViewModel : ViewModel() {

    /**
     *  Stores handle to TransactionRepository.
     */
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    /**
     *  Transaction queries
     */
    /**
     *  Tells Repository which CategoryTotals list to return.
     *
     *  Uses the values of date and type in order to determine which Transaction list is needed.
     *
     *  @param  date         boolean for date filter.
     *  @param  type         either "Expense" or "Income".
     *  @param  start        starting Date for date filter.
     *  @param  end          ending Date for date filter.
     *  @return LiveData object holding list of Transactions.
     */
    fun filteredCategoryTotals(date : Boolean?, type : String?, start : Date?, end : Date?)
            : LiveData<List<CategoryTotals>> {

        return if (date == true) {

            transactionRepository.getLDCategoryTotals(type, start, end)
        } else {

            transactionRepository.getLDCategoryTotals(type)
        }
    }
}