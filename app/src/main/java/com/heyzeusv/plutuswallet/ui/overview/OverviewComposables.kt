package com.heyzeusv.plutuswallet.ui.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ChipDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DrawerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ChartInformation
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.TranListItemFull
import com.heyzeusv.plutuswallet.ui.BackPressHandler
import com.heyzeusv.plutuswallet.ui.PWButton
import com.heyzeusv.plutuswallet.ui.PreviewHelper
import com.heyzeusv.plutuswallet.ui.PreviewHelperCard
import com.heyzeusv.plutuswallet.ui.PWAlertDialog
import com.heyzeusv.plutuswallet.util.theme.LocalPWColors
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.theme.chipTextStyle
import com.heyzeusv.plutuswallet.util.AppBarActions
import com.heyzeusv.plutuswallet.util.FilterChipAction
import com.heyzeusv.plutuswallet.util.FilterChipAction.ADD
import com.heyzeusv.plutuswallet.util.FilterChipAction.REMOVE
import com.heyzeusv.plutuswallet.util.TransactionType
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.FilterState
import com.heyzeusv.plutuswallet.util.datePickerDialog
import java.math.BigDecimal
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import kotlinx.coroutines.launch

/**
 *  Composable that displays Overview screen.
 *  Data that is displayed is retrieved from [tranListVM], [chartVM], and [filterVM].
 *  [appBarActionSetup] determines what to do when an action item is pressed from the AppBar.
 *  [showSnackbar] is used to display Snackbar. [drawerState] is to open/close drawer.
 *  [navigateToTransaction] navigates to Transaction screen with id argument.
 */
@Composable
fun OverviewScreen(
    tranListVM: TransactionListViewModel,
    chartVM: ChartViewModel,
    filterVM: FilterViewModel,
    appBarActionSetup: (AppBarActions) -> Unit,
    showSnackbar: suspend (String) -> Unit,
    drawerState: DrawerState,
    navigateToTransaction: (Int) -> Unit,
    activityFinish: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var closeApp by remember { mutableStateOf(false) }

    val tlTranList by tranListVM.tranList.collectAsState()
    val tranListShowDeleteDialog by tranListVM.showDeleteDialog.collectAsState()

    val cChartInfoList by chartVM.chartInfoList.collectAsState()

    val filterInfo by filterVM.filterInfo.collectAsState()
    val fShowFilter by filterVM.showFilter.collectAsState()
    val fAccountFilterSelected by filterVM.accountFilter.collectAsState()
    val fAccountNameList by filterVM.accountList.collectAsState()
    val fAccountSelected by filterVM.accountSelected.collectAsState()
    val fCategoryFilterSelected by filterVM.categoryFilter.collectAsState()
    val fTypeSelected by filterVM.typeSelected.collectAsState()
    val fCategoryList by filterVM.categoryList.collectAsState()
    val fCategorySelectedList by filterVM.categorySelectedList.collectAsState()
    val fDateFilterSelected by filterVM.dateFilter.collectAsState()
    val fStartDate by filterVM.startDate.collectAsState()
    val fStartDateString by filterVM.startDateString.collectAsState()
    val fEndDate by filterVM.endDate.collectAsState()
    val fEndDateString by filterVM.endDateString.collectAsState()
    val fFilterState by filterVM.filterState.collectAsState()

    // set up AppBar actions
    appBarActionSetup(
        AppBarActions(
            onNavPressed = { drawerState.open() },
            onActionLeftPressed = { filterVM.updateShowFilter(!fShowFilter) },
            onActionRightPressed = { navigateToTransaction(0) }
        )
    )
    BackPressHandler {
        when {
            drawerState.isOpen -> coroutineScope.launch { drawerState.close() }
            fShowFilter -> filterVM.updateShowFilter(false)
            else -> closeApp = true
        }
    }
    if (closeApp) {
        PWAlertDialog(
            title = stringResource(R.string.alert_dialog_closeapp),
            message = stringResource(R.string.alert_dialog_closeapp_message),
            onConfirmText = stringResource(R.string.alert_dialog_yes),
            onConfirm = { activityFinish() },
            onDismissText = stringResource(R.string.alert_dialog_no),
            onDismiss = { closeApp = false }
        )
    }
    OverviewScreen(
        filterInfo,
        tlPreviousMaxId = tranListVM.previousMaxId,
        tlUpdatePreviousMaxId = tranListVM::updatePreviousMaxId,
        tlTranList,
        tlUpdateTranList = tranListVM::updateTranList,
        tlItemOnLongClick = tranListVM::updateDeleteDialog,
        tlItemOnClick = { tranId -> navigateToTransaction(tranId) },
        tlShowDeleteDialog = tranListShowDeleteDialog,
        tlDeleteDialogOnConfirm = { tranId -> tranListVM.deleteTransaction(tranId) },
        tlDeleteDialogOnDismiss = { tranListVM.updateDeleteDialog(-1) },
        cChartInfoList,
        cUpdateCatTotalsList = chartVM::updateCatTotalsList,
        fShowFilter,
        fUpdateShowFilter = filterVM::updateShowFilter,
        fAccountFilterSelected,
        fAccountFilterOnClick = filterVM::updateAccountFilter,
        fAccountNameList,
        fAccountSelected,
        fAccountChipOnClick = filterVM::updateAccountSelected,
        fCategoryFilterSelected,
        fCategoryFilterOnClick = filterVM::updateCategoryFilter,
        fTypeSelected,
        fUpdateTypeSelected = filterVM::updateTypeSelected,
        fCategoryList,
        fCategorySelectedList,
        fCategoryChipOnClick = filterVM::updateCategorySelectedList,
        fDateFilterSelected,
        fDateFilterOnClick = filterVM::updateDateFilter,
        fStartDate,
        fStartDateString,
        fStartDateOnClick = filterVM::updateStartDateString,
        fEndDate,
        fEndDateString,
        fEndDateOnClick = filterVM::updateEndDateString,
        fApplyOnClick = filterVM::applyFilter,
        fFilterState,
        fShowSnackbar = { msg ->
            showSnackbar(msg)
            filterVM.updateFilterState(FilterState.VALID)
        }
    )
}

/**
 *  Composable that displays Overview screen.
 *  All the data has been hoisted into above [OverviewScreen] thus allowing for easier testing.
 *  See individual Composables for parameter info.
 */
@Composable
fun OverviewScreen(
    filterInfo: FilterInfo,
    tlPreviousMaxId: Int,
    tlUpdatePreviousMaxId: (Int) -> Unit,
    tlTranList: List<TranListItemFull>,
    tlUpdateTranList: suspend (FilterInfo) -> Unit,
    tlItemOnLongClick: (Int) -> Unit,
    tlItemOnClick: (Int) -> Unit,
    tlShowDeleteDialog: Int,
    tlDeleteDialogOnConfirm: (Int) -> Unit,
    tlDeleteDialogOnDismiss: () -> Unit,
    chartInfoList: List<ChartInformation>,
    cUpdateCatTotalsList: suspend (FilterInfo) -> Unit,
    fShowFilter: Boolean,
    fUpdateShowFilter: (Boolean) -> Unit,
    fAccountFilterSelected: Boolean,
    fAccountFilterOnClick: (Boolean) -> Unit,
    fAccountNameList: List<String>,
    fAccountSelected: List<String>,
    fAccountChipOnClick: (String, FilterChipAction) -> Unit,
    fCategoryFilterSelected: Boolean,
    fCategoryFilterOnClick: (Boolean) -> Unit,
    fTypeSelected: TransactionType,
    fUpdateTypeSelected: (TransactionType) -> Unit,
    fCategoryList: List<String>,
    fCategorySelected: List<String>,
    fCategoryChipOnClick: (String, FilterChipAction) -> Unit,
    fDateFilterSelected: Boolean,
    fDateFilterOnClick: (Boolean) -> Unit,
    fStartDate: ZonedDateTime,
    fStartDateString: String,
    fStartDateOnClick: (ZonedDateTime) -> Unit,
    fEndDate: ZonedDateTime,
    fEndDateString: String,
    fEndDateOnClick: (ZonedDateTime) -> Unit,
    fApplyOnClick: () -> Unit,
    fFilterState: FilterState,
    fShowSnackbar: suspend (String) -> Unit
) {

    val fullPad = dimensionResource(R.dimen.cardFullPadding)
    val sharedPad = dimensionResource(R.dimen.cardSharedPadding)

    LaunchedEffect(key1 = filterInfo) { tlUpdateTranList(filterInfo) }
    LaunchedEffect(key1 = filterInfo) { cUpdateCatTotalsList(filterInfo) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ChartCard(
            modifier = Modifier
                .weight(0.4f)
                .padding(start = fullPad, top = fullPad, end = fullPad, bottom = sharedPad),
            chartInfoList = chartInfoList
        )
        TransactionListCard(
            modifier = Modifier
                .weight(0.6f)
                .padding(start = fullPad, top = sharedPad, end = fullPad, bottom = fullPad),
            tlTranList,
            tlPreviousMaxId,
            tlUpdatePreviousMaxId,
            tlItemOnLongClick,
            tlItemOnClick,
            tlShowDeleteDialog,
            tlDeleteDialogOnConfirm,
            tlDeleteDialogOnDismiss
        )
    }
    FilterCard(
        fShowFilter,
        fUpdateShowFilter,
        fAccountFilterSelected,
        fAccountFilterOnClick,
        fAccountNameList,
        fAccountSelected,
        fAccountChipOnClick,
        fCategoryFilterSelected,
        fCategoryFilterOnClick,
        fTypeSelected,
        fUpdateTypeSelected,
        fCategoryList,
        fCategorySelected,
        fCategoryChipOnClick,
        fDateFilterSelected,
        fDateFilterOnClick,
        fStartDate,
        fStartDateString,
        fStartDateOnClick,
        fEndDate,
        fEndDateString,
        fEndDateOnClick,
        fApplyOnClick,
        fFilterState,
        fShowSnackbar
    )
}

/**
 *  Composable that displays Charts and totals using data from [chartInfoList]
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@Composable
fun ChartCard(
    modifier: Modifier = Modifier,
    chartInfoList: List<ChartInformation> = listOf(ChartInformation(), ChartInformation())
) {
    val pagerState = rememberPagerState()

    // colors used by charts
    val chartLabelColor = MaterialTheme.colors.onSurface.toArgb()
    val chartCenterHoleColor = LocalPWColors.current.chartCenterHole.toArgb()
    val chartColorLists: List<List<Int>> = listOf(
        listOf(
            LocalPWColors.current.expenseChartPrimary.toArgb(),
            LocalPWColors.current.expenseChartSecondary.toArgb(),
            LocalPWColors.current.expenseChartTertiary.toArgb(),
            LocalPWColors.current.expenseChartQuaternary.toArgb()
        ),
        listOf(
            LocalPWColors.current.incomeChartPrimary.toArgb(),
            LocalPWColors.current.incomeChartSecondary.toArgb(),
            LocalPWColors.current.incomeChartTertiary.toArgb(),
            LocalPWColors.current.incomeChartQuaternary.toArgb()
        )
    )

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                pageCount = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .testTag(stringResource(R.string.tt_chart_vp)),
                state = pagerState
            ) { page ->
                val chartInfo = chartInfoList[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (chartInfo.ctList.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag(stringResource(R.string.tt_chart_page, page))
                        ) {
                            /**
                             *  Library used for PieChart is most likely never going to be updated to
                             *  be Composable. Will be looking for new library or possibly make own.
                             */
                            AndroidView(
                                factory = { context ->
                                    PieChart(context).apply {
                                        // displays translated type in center of chart
                                        centerText = if (page == 0) {
                                            context.resources.getString(R.string.type_expense)
                                        } else {
                                            context.resources.getString(R.string.type_income)
                                        }
                                        // don't want a description so make it blank
                                        description.text = ""
                                        // don't want legend so disable it
                                        legend.isEnabled = false
                                        // true = doughnut chart
                                        isDrawHoleEnabled = true
                                        // color of labels
                                        setEntryLabelColor(chartLabelColor)
                                        // size of Category labels
                                        setEntryLabelTextSize(14.5f)
                                        // color of center hole
                                        setHoleColor(chartCenterHoleColor)
                                        // size of center text
                                        setCenterTextSize(15f)
                                        // color of center text
                                        setCenterTextColor(chartLabelColor)
                                        // true = display center text
                                        setDrawCenterText(true)
                                        // true = use percent values
                                        setUsePercentValues(true)
                                        contentDescription = "Chart $page"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                update = { pieChart: PieChart ->
                                    // list of entries to be displayed in PieChart
                                    val pieEntries: List<PieEntry> =
                                        chartInfo.ctList.map { catTotal ->
                                            PieEntry(catTotal.total.toFloat(), catTotal.category)
                                        }

                                    // PieDataSet set up
                                    val dataSet = PieDataSet(pieEntries, "Transactions")
                                    // distance between slices
                                    dataSet.sliceSpace = 2.5f
                                    // size of percent value
                                    dataSet.valueTextSize = 13f
                                    // color of percent value
                                    dataSet.valueTextColor = chartLabelColor
                                    // colors used for slices
                                    dataSet.colors = chartColorLists[page]
                                    // no highlights so no shift needed
                                    dataSet.selectionShift = 0f

                                    // PieData set up
                                    val pData = PieData(dataSet)
                                    // makes values in form of percentages
                                    pData.setValueFormatter(PercentFormatter(pieChart))
                                    // PieChart set up
                                    pieChart.data = pData

                                    val highlights: MutableList<Highlight> = mutableListOf()
                                    chartInfo.ctList.forEachIndexed { i, _ ->
                                        highlights.add(Highlight(i.toFloat(), 0, 0))
                                    }
                                    pieChart.highlightValues(highlights.toTypedArray())
                                }
                            )
                        }
                        val totalPrefix = stringResource(R.string.chart_total)
                        Text(
                            text = "$totalPrefix${chartInfo.totalText}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Start)
                                .padding(horizontal = dimensionResource(R.dimen.chartMarginStartEnd))
                                .testTag(stringResource(R.string.tt_chart_total, page))
                                .basicMarquee(),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.subtitle1
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.cfl_no_transactions),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .testTag(stringResource(R.string.tt_chart_empty, page)),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = 2,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 *  Composable that displays [tranList], list of Transactions, in [TranListItem] form.
 *  [previousMaxId] is used to determine if a new Transaction was created in order to
 *  scroll to the top of the list automatically which is updated by [updatePreviousMaxId].
 *  [itemOnLongClick] and [itemOnClick] are used for deletion and selection
 *  respectively. [showDeleteDialog] determines when to show AlertDialog, while
 *  [deleteDialogOnConfirm] and [deleteDialogOnDismiss] are used to confirm deletion or deny it
 *  respectively.
 */
@Composable
fun TransactionListCard(
    modifier: Modifier = Modifier,
    tranList: List<TranListItemFull> = emptyList(),
    previousMaxId: Int = 0,
    updatePreviousMaxId: (Int) -> Unit = { },
    itemOnLongClick: (Int) -> Unit = { },
    itemOnClick: (Int) -> Unit = { },
    showDeleteDialog: Int = 0,
    deleteDialogOnConfirm: (Int) -> Unit = { },
    deleteDialogOnDismiss: () -> Unit = { }
) {
    val tranListState = rememberLazyListState()

    // scrolls to top of the list when new Transaction is added
    LaunchedEffect(key1 = tranList) {
        if (tranList.isNotEmpty() && tranList[tranList.size - 1].tli.id > previousMaxId) {
            tranListState.animateScrollToItem(0)
            updatePreviousMaxId(tranList[tranList.size - 1].tli.id)
        }
    }
    Card(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = tranListState
        ) {
            items(tranList.reversed()) { transactionItemFormatted ->
                val transactionItem = transactionItemFormatted.tli
                Divider(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                TransactionListItem(
                    transactionItemFormatted,
                    onLongClick = { itemOnLongClick(transactionItem.id) },
                    onClick = { itemOnClick(transactionItem.id) },
                )
                if (showDeleteDialog == transactionItem.id) {
                    PWAlertDialog(
                        title = stringResource(R.string.alert_dialog_delete_transaction),
                        message = stringResource(
                            R.string.alert_dialog_delete_warning,
                            transactionItem.title
                        ),
                        onConfirmText = stringResource(R.string.alert_dialog_yes),
                        onConfirm = { deleteDialogOnConfirm(transactionItem.id) },
                        onDismissText = stringResource(R.string.alert_dialog_no),
                        onDismiss = deleteDialogOnDismiss
                    )
                }
            }
        }
        if (tranList.isEmpty()) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.cfl_no_transactions),
                    modifier = Modifier.testTag(stringResource(R.string.tt_tranL_empty)),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 *  Composable that displays a Transaction in [TranListItem] form. [transactionItemFormatted]
 *  contains the data to be displayed. [onLongClick] and [onClick] are used for deletion and
 *  selection respectively.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListItem(
    transactionItemFormatted: TranListItemFull,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
) {
    val transactionItem = transactionItemFormatted.tli

    Surface(
        modifier = Modifier
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .testTag(stringResource(R.string.tt_tranL_item, transactionItem.id))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 4.dp, end = 4.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = transactionItem.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = transactionItem.account,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle2,
                )
                Text(
                    text = transactionItemFormatted.formattedDate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle2
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = transactionItemFormatted.formattedTotal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    color = when (transactionItem.type) {
                        EXPENSE.type -> LocalPWColors.current.expense
                        else -> LocalPWColors.current.income
                    },
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = transactionItem.category,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    }
}

/**
 *  Composable that displays filter. [showFilter] is used to determine if filter should be displayed.
 *  [updateShowFilter] is used to show/hide filter. [accountFilterSelected], [categoryFilterSelected],
 *  and [dateFilterSelected] are used to determine which filters should be selected.
 *  [accountFilterOnClick], [categoryFilterOnClick], [dateFilterOnClick] are used to select/deselect
 *  filters. [accountList] are all accounts available while [accountSelected] is the list of accounts
 *  which have been selected. [accountChipOnClick] determines action when individual account chip is
 *  selected. [typeSelected] determines if Expense/Income categories should be displayed.
 *  [updateTypeSelected] switches between Expense/Income lists. [categoryList] are all
 *  categories available of type while [categorySelectedList] is the list of categories which have been
 *  selected. [categoryChipOnClick] determines action when individual category chip is selected.
 *  [startDate] is the actual date object while [startDateString] is displayed on start button which
 *  performs [startDateOnClick] when clicked. [endDate] is the actual date object while
 *  [endDateString] is displayed on end button which performs [endDateOnClick] when clicked.
 *  [applyOnClick] runs when apply/reset button is pressed. [filterState] is used to determine
 *  which message to display if there is an error. [showSnackbar] is suspend function which takes
 *  a String that is meant to be displayed by Snackbar.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FilterCard(
    showFilter: Boolean = false,
    updateShowFilter: (Boolean) -> Unit = { },
    accountFilterSelected: Boolean = false,
    accountFilterOnClick: (Boolean) -> Unit = { },
    accountList: List<String> = emptyList(),
    accountSelected: List<String> = emptyList(),
    accountChipOnClick: (String, FilterChipAction) -> Unit = { _, _ -> },
    categoryFilterSelected: Boolean = false,
    categoryFilterOnClick: (Boolean) -> Unit = { },
    typeSelected: TransactionType = EXPENSE,
    updateTypeSelected: (TransactionType) -> Unit = { },
    categoryList: List<String> = emptyList(),
    categorySelectedList: List<String> = emptyList(),
    categoryChipOnClick: (String, FilterChipAction) -> Unit = { _, _ -> },
    dateFilterSelected: Boolean = false,
    dateFilterOnClick: (Boolean) -> Unit = { },
    startDate: ZonedDateTime = ZonedDateTime.now(systemDefault()),
    startDateString: String = "",
    startDateOnClick: (ZonedDateTime) -> Unit = { },
    endDate: ZonedDateTime = ZonedDateTime.now(systemDefault()),
    endDateString: String = "",
    endDateOnClick: (ZonedDateTime) -> Unit = { },
    applyOnClick: () -> Unit = { },
    filterState: FilterState = FilterState.VALID,
    showSnackbar: suspend (String) -> Unit = { }
) {
    // used by animation to determine Y offset
    var filterComposeSize by remember { mutableStateOf(Size.Zero) }
    var accountComposeSize by remember { mutableStateOf(Size.Zero) }
    var categoryComposeSize by remember { mutableStateOf(Size.Zero) }
    var dateComposeSize by remember { mutableStateOf(Size.Zero) }

    val view = LocalView.current
    val noFilters = !accountFilterSelected && !categoryFilterSelected && !dateFilterSelected

    val typeSelectedLabel = stringResource(typeSelected.stringId)

    val filterStateMessage = stringResource(filterState.stringId)

    LaunchedEffect(key1 = filterState) {
        if (filterState != FilterState.VALID) { showSnackbar(filterStateMessage) }
    }
    AnimatedVisibility(
        visible = showFilter,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable { updateShowFilter(!showFilter) }
                .animateEnterExit(
                    enter = fadeIn(),
                    exit = fadeOut()
                ),
            color = LocalPWColors.current.backgroundOverlay
        ) {}
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .onGloballyPositioned { filterComposeSize = it.size.toSize() }
                .animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { -filterComposeSize.height.toInt() - 50 }
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { -filterComposeSize.height.toInt() - 50 }
                    )
                )
                .testTag("Filter Card"),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(all = 8.dp)
            ) {
                PWButton(
                    selected = accountFilterSelected,
                    onClick = { accountFilterOnClick(!accountFilterSelected) },
                    label = stringResource(R.string.filter_account),
                    showIcon = true,
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.f_button_chip_height))
                        .padding(bottom = 6.dp)
                )
                AnimatedVisibility(
                    visible = accountFilterSelected,
                    enter = expandVertically(
                        animationSpec = tween(easing = LinearEasing),
                        initialHeight = { -accountComposeSize.height.toInt() - 50 }
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(easing = LinearEasing),
                        targetHeight = { -accountComposeSize.height.toInt() - 50 }
                    )
                ) {
                    Surface(
                        modifier = Modifier
                            .heightIn(max = dimensionResource(R.dimen.f_chipGr_max_height))
                            .padding(start = 8.dp, end = 6.dp, bottom = 6.dp)
                            .fillMaxWidth()
                            .onGloballyPositioned { accountComposeSize = it.size.toSize() },
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(
                            width = dimensionResource(R.dimen.f_surface_border_width),
                            color = MaterialTheme.colors.secondary
                        )
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .verticalScroll(rememberScrollState()),
                            mainAxisSpacing = dimensionResource(R.dimen.f_chipGr_inHoriPad),
                            crossAxisSpacing = dimensionResource(R.dimen.f_chipGr_inVertPad)
                        ) {
                            for (account in accountList) {
                                PWChip(
                                    selected = accountSelected.contains(account),
                                    onClick = {
                                        accountChipOnClick(
                                            account,
                                            if (accountSelected.contains(account)) REMOVE else ADD
                                        )
                                    },
                                    label = account
                                )
                            }
                        }
                    }
                }
                PWButton(
                    selected = categoryFilterSelected,
                    onClick = { categoryFilterOnClick(!categoryFilterSelected) },
                    label = stringResource(R.string.filter_category),
                    showIcon = true,
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.f_button_chip_height))
                        .padding(bottom = 6.dp)
                )
                AnimatedVisibility(
                    visible = categoryFilterSelected,
                    enter = expandVertically(
                        animationSpec = tween(easing = LinearEasing),
                        initialHeight = { -categoryComposeSize.height.toInt() - 50 }
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(easing = LinearEasing),
                        targetHeight = { -categoryComposeSize.height.toInt() - 50 }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .onGloballyPositioned { categoryComposeSize = it.size.toSize() },
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PWButton(
                            selected = true,
                            onClick = { updateTypeSelected(typeSelected.opposite()) },
                            label = typeSelectedLabel,
                            showIcon = false,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.f_button_chip_height))
                                .testTag(stringResource(R.string.tt_filter_type, typeSelectedLabel)),
                        )
                        Surface(
                            modifier = Modifier
                                .heightIn(max = dimensionResource(R.dimen.f_chipGr_max_height))
                                .padding(bottom = 6.dp)
                                .fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(
                                width = dimensionResource(R.dimen.f_surface_border_width),
                                color = MaterialTheme.colors.secondary
                            )
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .verticalScroll(rememberScrollState()),
                                mainAxisSpacing = dimensionResource(R.dimen.f_chipGr_inHoriPad),
                                crossAxisSpacing = dimensionResource(R.dimen.f_chipGr_inVertPad)
                            ) {
                                categoryList.map { category ->
                                    PWChip(
                                        selected = categorySelectedList.contains(category),
                                        onClick = {
                                            categoryChipOnClick(
                                                category,
                                                if (categorySelectedList.contains(category)) REMOVE else ADD
                                            )
                                        },
                                        label = category
                                    )
                                }
                            }
                        }
                    }
                }
                PWButton(
                    selected = dateFilterSelected,
                    onClick = { dateFilterOnClick(!dateFilterSelected) },
                    label = stringResource(R.string.filter_date),
                    showIcon = true,
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.f_button_chip_height))
                        .padding(bottom = 6.dp)
                )
                AnimatedVisibility(
                    visible = dateFilterSelected,
                    enter = expandVertically(
                        animationSpec = tween(easing = LinearEasing),
                        initialHeight = { -dateComposeSize.height.toInt() - 50 }
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(easing = LinearEasing),
                        targetHeight = { -dateComposeSize.height.toInt() - 50 }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .onGloballyPositioned { dateComposeSize = it.size.toSize() },
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PWButton(
                            selected = true,
                            onClick = {
                                datePickerDialog(
                                    view,
                                    initDate = startDate,
                                    onDateSelected = startDateOnClick
                                ).show()
                            },
                            label = startDateString.ifBlank { stringResource(R.string.filter_start) },
                            showIcon = false,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.f_button_chip_height))
                                .testTag(stringResource(R.string.tt_filter_start)),
                        )
                        PWButton(
                            selected = true,
                            onClick = {
                                datePickerDialog(
                                    view,
                                    initDate = endDate,
                                    onDateSelected = endDateOnClick
                                ).show()
                            },
                            label = endDateString.ifBlank { stringResource(R.string.filter_end) },
                            showIcon = false,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.f_button_chip_height))
                                .padding(bottom = 6.dp)
                                .testTag(stringResource(R.string.tt_filter_end)),
                        )
                    }
                }
                PWButton(
                    selected = true,
                    onClick = applyOnClick,
                    label = if (noFilters) {
                        stringResource(R.string.filter_reset)
                    } else {
                        stringResource(R.string.filter_apply)
                    },
                    showIcon = false,
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.f_button_chip_height))
                        .testTag(stringResource(R.string.tt_filter_act)),
                    selectedBackgroundColor = MaterialTheme.colors.secondary,
                    selectedTextColor = LocalElevationOverlay.current?.apply(
                        color = MaterialTheme.colors.surface,
                        elevation = 8.dp
                    ) ?: MaterialTheme.colors.surface
                )
            }
        }
    }
}

/**
 *  Composable for a text only Chip. [selected] determines if Chip has be selected. [onClick] is
 *  performed when Chip is clicked. [label] is the text to be displayed in Chip.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PWChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag(stringResource(R.string.tt_filter_chip, label)),
        border = BorderStroke(
            width = dimensionResource(R.dimen.f_chip_border_width),
            color = if (selected) {
                MaterialTheme.colors.secondary
            } else {
                LocalPWColors.current.unselected
            }
        ),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = LocalPWColors.current.chipUnselectedBackground,
            selectedBackgroundColor = LocalPWColors.current.chipSelectedBackground
        ),
        content = {
            Text(
                text = label,
                modifier = Modifier,
                color = if (selected) {
                    MaterialTheme.colors.onPrimary
                } else {
                    LocalPWColors.current.unselected
                },
                style = chipTextStyle
            )
        }
    )
}

@Preview
@Composable
fun OverviewScreenPreview() {
    val tlItem = TranListItemFull(
        TranListItem(
            10,
            "Test Title",
            ZonedDateTime.now(systemDefault()),
            BigDecimal("100.00"),
            "Test Account",
            "Expense",
            "Test Category"
        ),
        formattedDate = "Today O' Clock",
        formattedTotal = "$100.00"
    )
    val chartInfo = ChartInformation(
        ctList = listOf(
            CategoryTotals(
                "Test Category",
                BigDecimal("300.00"),
                "Expense"
            )
        ),
        totalText = "$300.00"
    )
    PreviewHelper {
        OverviewScreen(
            filterInfo = FilterInfo(),
            tlPreviousMaxId = 1,
            tlUpdatePreviousMaxId = { },
            tlTranList = listOf(tlItem, tlItem, tlItem),
            tlUpdateTranList = { },
            tlItemOnLongClick = { },
            tlItemOnClick = { },
            tlShowDeleteDialog = 0,
            tlDeleteDialogOnConfirm = { },
            tlDeleteDialogOnDismiss = { },
            chartInfoList = listOf(chartInfo),
            cUpdateCatTotalsList = { },
            fShowFilter = false,
            fUpdateShowFilter = { },
            fAccountFilterSelected = false,
            fAccountFilterOnClick = { },
            fAccountNameList = listOf(),
            fAccountSelected = listOf(),
            fAccountChipOnClick = { _, _ -> },
            fCategoryFilterSelected = false,
            fCategoryFilterOnClick = { },
            fTypeSelected = EXPENSE,
            fUpdateTypeSelected = { },
            fCategoryList = listOf(),
            fCategorySelected = listOf(),
            fCategoryChipOnClick = { _, _ -> },
            fDateFilterSelected = false,
            fDateFilterOnClick = { },
            fStartDate = ZonedDateTime.now(systemDefault()),
            fStartDateString = "",
            fStartDateOnClick = { },
            fEndDate = ZonedDateTime.now(systemDefault()),
            fEndDateString = "",
            fEndDateOnClick = { },
            fApplyOnClick = { },
            fFilterState = FilterState.VALID,
            fShowSnackbar = { }
        )
    }
}

@Preview
@Composable
fun ChartCardPreview() {
    val chartInfo = ChartInformation(
        ctList = listOf(
            CategoryTotals(
                "Test Category",
                BigDecimal("300.00"),
                "Expense"
            )
        ),
        totalText = "$300.00"
    )
    PreviewHelperCard {
        ChartCard(chartInfoList = listOf(chartInfo))
    }
}

@Preview
@Composable
fun TransactionListCardPreview() {
    val tlItem = TranListItemFull(
        TranListItem(
            10,
            "Test Title",
            ZonedDateTime.now(systemDefault()),
            BigDecimal("100.00"),
            "Test Account",
            "Expense",
            "Test Category"
        ),
        formattedDate = "Today O' Clock",
        formattedTotal = "$100.00"
    )
    PreviewHelperCard {
        TransactionListCard(
            tranList = listOf(tlItem, tlItem, tlItem),
            previousMaxId = 1,
            updatePreviousMaxId = { },
            itemOnLongClick = { },
            itemOnClick = { },
            showDeleteDialog = 0,
            deleteDialogOnConfirm = { }
        ) { }
    }
}

@Preview
@Composable
fun TransactionListItemPreview() {
    val tlItem = TranListItemFull(
        TranListItem(
            10,
            "Test Title",
            ZonedDateTime.now(systemDefault()),
            BigDecimal("100.00"),
            "Test Account",
            "Expense",
            "Test Category"
        ),
        formattedDate = "Today O' Clock",
        formattedTotal = "$100.00"
    )
    PlutusWalletTheme {
        TransactionListItem(
            tlItem,
            onLongClick = { },
            onClick = { },
        )
    }
}

@Preview
@Composable
fun FilterCardPreview() {
    PreviewHelperCard {
        FilterCard(
            showFilter = true,
            accountFilterSelected = true,
            accountList = listOf("Preview"),
            categoryFilterSelected = true,
            typeSelected = EXPENSE,
            categoryList = listOf("Preview"),
            dateFilterSelected = true,
            startDateString = "Start",
            endDateString = "End",
        )
    }
}

@Preview
@Composable
fun PWChipPreview() {
    PreviewHelperCard {
        Column {
            PWChip(selected = true, onClick = { }, label = "Selected")
            PWChip(selected = false, onClick = { }, label = "Unselected")
        }
    }
}