package com.heyzeusv.plutuswallet.ui.cfl.chart

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.data.TransactionRepository
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ItemViewChart
import java.math.BigDecimal
import java.util.Date

/**
 *  Data manager for GraphFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
class ChartViewModel @ViewModelInject constructor(
    private val tranRepo: TransactionRepository
) : ViewModel() {

    // used to make list of 2 ItemViewChart objects to initialize ChartAdapter
    private val emptyIvc = ItemViewChart(
        emptyList(), "", "", emptyList(), null, null, null
    )
    private var ivcList: MutableList<ItemViewChart> = mutableListOf(emptyIvc, emptyIvc)
    var adapter: ChartAdapter = ChartAdapter().apply { submitList(ivcList) }

    // list of CategoryTotals after filter is applied
    private var exCatTotals: List<CategoryTotals> = emptyList()
    private var inCatTotals: List<CategoryTotals> = emptyList()

    // list of names from list of CategoryTotals
    private var exNames: List<String> = emptyList()
    private var inNames: List<String> = emptyList()

    // total from each list of CategoryTotals
    var exTotal: BigDecimal = BigDecimal("0.0")
    var inTotal: BigDecimal = BigDecimal("0.0")

    // formatted text that displays total
    var exTotText: String = ""
    var inTotText: String = ""

    /**
     *  Splits [ctList] into 2 lists according to type and retrieves Category names.
     */
    fun prepareLists(ctList: List<CategoryTotals>) {

        // CategoryTotals list by type
        val eCTs: MutableList<CategoryTotals> = mutableListOf()
        val iCTs: MutableList<CategoryTotals> = mutableListOf()

        // splits ctList into 2 lists according to type
        ctList.forEach { if (it.type == "Expense") eCTs.add(it) else iCTs.add(it) }
        exCatTotals = eCTs
        inCatTotals = iCTs

        // map transformation to retrieve Category names
        exNames = exCatTotals.map { it.category }
        inNames = inCatTotals.map { it.category }
    }

    /**
     *  Calculates totals for each list depending on category filter [fCat],
     *  category selected filter [fCatName], and type of category filter [fType].
     */
    fun prepareTotals(fCat: Boolean?, fCatName: String?, fType: String?) {

        // if fCatName = "All", add up all totals of given type
        // else total is total of Category selected in filter if there exists entries else 0
        // sets opposite type total to 0
        when {
            fCat == true && fType == "Expense" && fCatName == "All" -> {
                exTotal =
                    exCatTotals.fold(BigDecimal.ZERO) { total: BigDecimal, next: CategoryTotals ->
                        total + next.total
                    }
                inTotal = BigDecimal.ZERO
            }
            fCat == true && fType == "Expense" -> {
                exTotal = exCatTotals.find { it.category == fCatName }?.total ?: BigDecimal.ZERO
                inTotal = BigDecimal.ZERO
            }
            fCat == true && fType == "Income" && fCatName == "All" -> {
                exTotal = BigDecimal.ZERO
                inTotal =
                    inCatTotals.fold(BigDecimal.ZERO) { total: BigDecimal, next: CategoryTotals ->
                        total + next.total
                    }
            }
            fCat == true && fType == "Income" -> {
                exTotal = BigDecimal.ZERO
                inTotal = inCatTotals.find { it.category == fCatName }?.total ?: BigDecimal.ZERO
            }
            else -> {
                // Category filter is not applied, so add up all the totals
                exTotal =
                    exCatTotals.fold(BigDecimal.ZERO) { total: BigDecimal, next: CategoryTotals ->
                        total + next.total
                    }
                inTotal =
                    inCatTotals.fold(BigDecimal.ZERO) { total: BigDecimal, next: CategoryTotals ->
                        total + next.total
                    }
            }
        }
    }

    /**
     *  Creates ItemViewChart objects using data calculated using above functions and
     *  depending on category filter [fCat], category selected filter [fCatName],
     *  and type of category filter [fType], passes them translated strings [expense]/[income],
     *  colors used for Chart [exColors]/[inColors], and passes them to adapter, and notifies changes.
     */
    fun prepareIvgAdapter(
        fCat: Boolean?,
        fCatName: String?,
        fType: String?,
        expense: String,
        income: String,
        exColors: List<Int>,
        inColors: List<Int>
    ) {

        val exIvc = ItemViewChart(exCatTotals, expense, exTotText, exColors, fCat, fCatName, fType)
        val inIvc = ItemViewChart(inCatTotals, income, inTotText, inColors, fCat, fCatName, fType)

        ivcList = mutableListOf(exIvc, inIvc)
        adapter.submitList(ivcList)
    }

    /**
     *  Transaction queries
     */
    /**
     *  Tells Repository which CategoryTotals list to return depending on
     *  [fAccount] and [fDate] filters, account name [fAccountName],
     *  and the start/end dates [fStart]/[fEnd]
     */
    fun filteredCategoryTotals(
        fAccount: Boolean,
        fDate: Boolean,
        fAccountName: String,
        fStart: Date,
        fEnd: Date
    ): LiveData<List<CategoryTotals>> {

        return when {
            fAccount && fDate -> tranRepo.getLdCtAD(fAccountName, fStart, fEnd)
            fAccount -> tranRepo.getLdCtA(fAccountName)
            fDate -> tranRepo.getLdCtD(fStart, fEnd)
            else -> tranRepo.getLdCt()
        }
    }
}