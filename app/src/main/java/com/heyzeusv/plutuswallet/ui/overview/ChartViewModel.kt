package com.heyzeusv.plutuswallet.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ChartInformation
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.prepareTotalText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 *  Data manager for GraphFragments.
 *
 *  Stores and manages UI-related data in a lifecycle conscious way.
 *  Data can survive configuration changes.
 */
@HiltViewModel
class ChartViewModel @Inject constructor(
    private val tranRepo: PWRepositoryInterface
) : ViewModel() {

    var setVals = SettingsValues()

    private val _catTotalsList = MutableStateFlow(emptyList<CategoryTotals>())
    val catTotalsList: StateFlow<List<CategoryTotals>> get() = _catTotalsList
    suspend fun updateCatTotalsList(filterInfo: FilterInfo) {
        filteredCategoryTotals(filterInfo).collect { list ->
            _catTotalsList.value = list
            prepareChartInformation()
        }
    }

    private val _chartInfoList = MutableStateFlow(listOf(ChartInformation(), ChartInformation()))
    val chartInfoList: StateFlow<List<ChartInformation>> get() = _chartInfoList
    fun updateChartInfoList(newList: List<ChartInformation>) { _chartInfoList.value = newList }

    init {
        viewModelScope.launch {
            val ctList = filteredCategoryTotals(FilterInfo()).first()
            _catTotalsList.value = ctList
            prepareChartInformation()
        }
    }

    /**
     *  Creates ChartInformation for both Expense and Income type Categories.
     *  It first splits list of CategoryTotals into 2, one for Expense, other for Income.
     *  Then it calculates the total of each list separately.
     *  Lastly it creates ChartInformation using lists and creating the total String using setVals
     *  and updating StateFlow value.
     */
    private fun prepareChartInformation() {
        // split into type lists
        val exCTList = mutableListOf<CategoryTotals>()
        val inCTList = mutableListOf<CategoryTotals>()
        catTotalsList.value.forEach {
            if (it.type == EXPENSE.type) exCTList.add(it) else inCTList.add(it)
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
            totalText = exCTListTotal.prepareTotalText(setVals)
        )
        val incomeChartInfo = ChartInformation(
            ctList = inCTList,
            totalText = inCTListTotal.prepareTotalText(setVals)
        )
        updateChartInfoList(listOf(expenseChartInfo, incomeChartInfo))
    }

    /**
     *  Returns StateFlow of list of CategoryTotals depending on [fi] arguments.
     */
    suspend fun filteredCategoryTotals(fi: FilterInfo): Flow<List<CategoryTotals>> {
        return when {
            fi.account && fi.category && fi.date ->
                tranRepo.getCtACD(fi.accountNames, fi.type, fi.categoryNames, fi.start, fi.end)
            fi.account && fi.category -> tranRepo.getCtAC(fi.accountNames, fi.type, fi.categoryNames)
            fi.account && fi.date -> tranRepo.getCtAD(fi.accountNames, fi.start, fi.end)
            fi.category && fi.date -> tranRepo.getCtCD(fi.type, fi.categoryNames, fi.start, fi.end)
            fi.account -> tranRepo.getCtA(fi.accountNames)
            fi.category -> tranRepo.getCtC(fi.type, fi.categoryNames)
            fi.date -> tranRepo.getCtD(fi.start, fi.end)
            else -> tranRepo.getCt()
        }
    }
}