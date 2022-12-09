package com.heyzeusv.plutuswallet.ui.cfl.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ChartInformation
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 *  Data manager for GraphFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
@HiltViewModel
class ChartViewModel @Inject constructor(
    private val tranRepo: Repository,
    val setVals: SettingsValues
) : ViewModel() {

    private val _catTotalsList = MutableStateFlow(emptyList<CategoryTotals>())
    val catTotalsList: StateFlow<List<CategoryTotals>> get() = _catTotalsList
    fun updateCatTotalsList(filter: FilterInfo) {
        viewModelScope.launch {
            filteredCategoryTotals(filter).collect { list ->
                _catTotalsList.value = list
                prepareChartInformation()
            }
        }
    }

    private val _chartInfoList = MutableStateFlow(listOf(ChartInformation(), ChartInformation()))
    val chartInfoList: StateFlow<List<ChartInformation>> get() = _chartInfoList
    fun updateChartInfoList(newList: List<ChartInformation>) { _chartInfoList.value = newList }

    init {
        updateCatTotalsList(FilterInfo())
    }

    /**
     *  Creates ChartInformation for both Expense and Income type Categories.
     *  It first splits list of CategoryTotals into 2, one for Expense, other for Income.
     *  Then it calculates the total of each list separately.
     *  Lastly it creates ChartInformation using lists/total and updating StateFlow value.
     */
    private fun prepareChartInformation() {
        // split into type lists
        val exCTList = mutableListOf<CategoryTotals>()
        val inCTList = mutableListOf<CategoryTotals>()
        catTotalsList.value.forEach {
            if (it.type == "Expense") exCTList.add(it) else inCTList.add(it)
        }

        // calculate totals
        val exCTListTotal =
            exCTList.fold(BigDecimal.ZERO) { total: BigDecimal, next: CategoryTotals ->
                total + next.total
            }
        val inCTListTotal =
            inCTList.fold(BigDecimal.ZERO) { total: BigDecimal, next: CategoryTotals ->
                total + next.total
            }

        val expenseChartInfo = ChartInformation(
            ctList = exCTList,
            totalText = prepareTotalText(exCTListTotal)
        )
        val incomeChartInfo = ChartInformation(
            ctList = inCTList,
            totalText = prepareTotalText(inCTListTotal)
        )
        updateChartInfoList(listOf(expenseChartInfo, incomeChartInfo))
    }

    /**
     *  Takes [total] and turns it into a string which includes currency, thousands, and decimal
     *  symbols according to SettingsValues.
     */
    private fun prepareTotalText(total: BigDecimal): String {
        var totalText: String
        setVals.apply {
            totalText = when {
                decimalPlaces && symbolSide -> "$currencySymbol${decimalFormatter.format(total)}"
                decimalPlaces -> "${decimalFormatter.format(total)}$currencySymbol"
                symbolSide -> "$currencySymbol${integerFormatter.format(total)}"
                else -> "${integerFormatter.format(total)}$currencySymbol"
            }
        }

        return totalText
    }

    /**
     *  Returns StateFlow of list of CategoryTotals depending on [fi] arguments.
     */
    suspend fun filteredCategoryTotals(fi: FilterInfo): Flow<List<CategoryTotals>> {
        return when {
            fi.account && fi.date -> tranRepo.getCtAD(fi.accountNames, fi.start, fi.end)
            fi.account -> tranRepo.getCtA(fi.accountNames)
            fi.date -> tranRepo.getCtD(fi.start, fi.end)
            else -> tranRepo.getCt()
        }
    }
}