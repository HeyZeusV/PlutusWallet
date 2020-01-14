package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.CategoryTotals
import java.math.BigDecimal
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

    // graph being displayed
    var selectedGraph : Int = 0

    var expenseCatTotals : List<CategoryTotals> = emptyList()
    var incomeCatTotals  : List<CategoryTotals> = emptyList()

    var expenseNames : List<String> = emptyList()
    var incomeNames  : List<String> = emptyList()

    var expenseTotal : BigDecimal = BigDecimal("0.0")
    var incomeTotal  : BigDecimal = BigDecimal("0.0")

    /**
     *  Transaction queries
     */
    /**
     *  Tells Repository which CategoryTotals list to return.
     *
     *  Uses the values of date and type in order to determine which Transaction list is needed.
     *
     *  @param  account     boolean for account filter
     *  @param  date        boolean for date filter.
     *  @param  type        either "Expense" or "Income".
     *  @param  accountName Account name for account filter
     *  @param  start       starting Date for date filter.
     *  @param  end         ending Date for date filter.
     *  @return LiveData object holding list of Transactions.
     */
    fun filteredCategoryTotals(account : Boolean?, date : Boolean?, type : String?, accountName : String?,
                               start : Date?, end : Date?) : LiveData<List<CategoryTotals>> {

        return if (account == true && date == true) {

            transactionRepository.getLdCtTAD(type, accountName, start, end)
        } else if (account == true) {

            transactionRepository.getLdCtTA(type, accountName)
        } else if (date == true) {

            transactionRepository.getLdCtTD(type, start, end)
        } else {

            transactionRepository.getLdCtT(type)
        }
    }
}