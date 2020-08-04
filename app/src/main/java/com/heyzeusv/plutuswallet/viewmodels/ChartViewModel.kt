package com.heyzeusv.plutuswallet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.database.TransactionRepository
import com.heyzeusv.plutuswallet.database.entities.CategoryTotals
import com.heyzeusv.plutuswallet.database.entities.ItemViewChart
import com.heyzeusv.plutuswallet.utilities.adapters.ChartAdapter
import java.math.BigDecimal
import java.util.Date

private const val TAG = "PWChartViewModel"

/**
 *  Data manager for GraphFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class ChartViewModel : ViewModel() {

    // stores handle to TransactionRepository
    private val transactionRepository : TransactionRepository = TransactionRepository.get()

    // used to make list of 2 ItemViewChart objects to initialize ChartAdapter
    private val emptyIvc = ItemViewChart(
        emptyList(), "", "", emptyList(), null, null, null)
    private var ivcList : MutableList<ItemViewChart> = mutableListOf(emptyIvc, emptyIvc)
    var adapter = ChartAdapter()

    // list of CategoryTotals after filter is applied
    private var exCatTotals : List<CategoryTotals> = emptyList()
    private var inCatTotals : List<CategoryTotals> = emptyList()

    // list of names from list of CategoryTotals
    private var exNames : List<String> = emptyList()
    private var inNames : List<String> = emptyList()

    // total from each list of CategoryTotals
    var exTotal : BigDecimal = BigDecimal("0.0")
    var inTotal : BigDecimal = BigDecimal("0.0")

    // formatted text that displays total
    var exTotText : String = ""
    var inTotText : String = ""

    // translated versions
    var expense : String = "Expense"
    var income  : String = "Income"

    // list of Int that represent colors resources to be used for charts
    var exColors : List<Int> = emptyList()
    var inColors : List<Int> = emptyList()

    /**
     *  Splits ctList into 2 lists according to type and retrieves Category names.
     *
     *  @param ctList list containing all CategoryTotals that pass filters applied.
     */
    fun prepareLists(ctList : List<CategoryTotals>) {

        // list by type
        val eCTs : MutableList<CategoryTotals> = mutableListOf()
        val iCTs : MutableList<CategoryTotals> = mutableListOf()

        // splits ctList into 2 lists according to type
        ctList.forEach {

            when (it.type) {

                "Expense" -> eCTs.add(it)
                else      -> iCTs.add(it)
            }
        }
        exCatTotals = eCTs
        inCatTotals = iCTs

        // map transformation to retrieve Category names
        exNames = exCatTotals.map { it.category }
        inNames = inCatTotals.map { it.category }
    }

    /**
     *  Calculates totals for each list.
     *
     *  @param fCat     true if Category filter is applied.
     *  @param fCatName Category selected when Category filter is applied.
     *  @param fType    type of Category selected from filter.
     */
    fun prepareTotals(fCat : Boolean?, fCatName : String?, fType : String?) {

        // if fCatName = "All", add up all totals of given type
        // else total is total of Category selected in filter if there exists entries else 0
        // sets opposite type total to 0
        when {
            fCat == true && fType == "Expense" && fCatName == "All" -> {

                exTotal = exCatTotals.fold(BigDecimal.ZERO) {
                        total : BigDecimal, next : CategoryTotals -> total + next.total }
                inTotal = BigDecimal.ZERO
            }
            fCat == true && fType == "Expense" -> {

                exTotal = exCatTotals.find{it.category == fCatName}?.total ?: BigDecimal.ZERO
                inTotal = BigDecimal.ZERO
            }
            fCat == true && fType == "Income" && fCatName == "All" -> {

                inTotal = inCatTotals.fold(BigDecimal.ZERO) {
                        total : BigDecimal, next : CategoryTotals -> total + next.total }
                exTotal = BigDecimal.ZERO
            }
            fCat == true && fType == "Income" -> {

                inTotal = inCatTotals.find{it.category == fCatName}?.total ?: BigDecimal.ZERO
                exTotal = BigDecimal.ZERO
            }
            else -> {

                // Category filter is not applied, so add up all the totals
                exTotal = exCatTotals.fold(BigDecimal.ZERO) {
                        total : BigDecimal, next : CategoryTotals -> total + next.total }
                inTotal = inCatTotals.fold(BigDecimal.ZERO) {
                        total : BigDecimal, next : CategoryTotals -> total + next.total }
            }
        }
    }

    /**
     *  Creates ItemViewChart objects using data calculated using above functions, passes them
     *  to adapter, and notifies of changes.
     *
     *  @param fCat     true if Category filter is applied.
     *  @param fCatName Category selected when Category filter is applied.
     *  @param fType    type of Category selected from filter.
     */
    fun prepareIvgAdapter(fCat : Boolean?, fCatName : String?, fType : String?) {

        val exIvc = ItemViewChart(exCatTotals, expense, exTotText, exColors, fCat, fCatName, fType)
        val inIvc = ItemViewChart(inCatTotals, income , inTotText, inColors, fCat, fCatName, fType)

        ivcList = mutableListOf(exIvc, inIvc)
        adapter.submitList(ivcList)
    }

    /**
     *  Transaction queries
     */
    /**
     *  Tells Repository which CategoryTotals list to return.
     *
     *  @param  fAccount     boolean for account filter
     *  @param  fDate        boolean for date filter.
     *  @param  fAccountName Account name for account filter.
     *  @param  fStart       starting Date for date filter.
     *  @param  fEnd         ending Date for date filter.
     *  @return LiveData object holding list of Transactions.
     */
    fun filteredCategoryTotals(fAccount : Boolean?, fDate : Boolean?, fAccountName : String?,
                               fStart : Date?, fEnd : Date?) : LiveData<List<CategoryTotals>> {

        return when {

            fAccount == true && fDate == true ->
                transactionRepository.getLdCtAD(fAccountName, fStart, fEnd)
            fAccount == true ->
                transactionRepository.getLdCtA(fAccountName)
            fDate == true ->
                transactionRepository.getLdCtD(fStart, fEnd)
            else ->
                transactionRepository.getLdCt()
        }
    }
}