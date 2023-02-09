package com.heyzeusv.plutuswallet.ui.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ChipDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.ChartInformation
import com.heyzeusv.plutuswallet.data.model.FilterInfo
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.TranListItemFull
import com.heyzeusv.plutuswallet.ui.AppBarActions
import com.heyzeusv.plutuswallet.ui.PWButtonChip
import com.heyzeusv.plutuswallet.ui.navigateToTransactionWithId
import com.heyzeusv.plutuswallet.util.theme.LocalPWColors
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.theme.chipTextStyle
import com.heyzeusv.plutuswallet.util.FilterSelectedAction
import com.heyzeusv.plutuswallet.util.FilterSelectedAction.ADD
import com.heyzeusv.plutuswallet.util.FilterSelectedAction.REMOVE
import com.heyzeusv.plutuswallet.util.TransactionType
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.FilterState
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay

/**
 *  Composable that displays Overview Card.
 *  Data that is displayed is retrieved from [tranListVM], [chartVM], and [filterVM].
 *  [appBarActionSetup] determines what to do when an action item is pressed from the AppBar.
 *  [scaffoldState] is used to display SnackBar and open Drawer.
 *  [navController] is used to navigate to Transaction screen with id argument.
 */
@Composable
fun OverviewScreen(
    tranListVM: TransactionListViewModel,
    chartVM: ChartViewModel,
    filterVM: FilterViewModel,
    appBarActionSetup: (AppBarActions) -> Unit,
    scaffoldState: ScaffoldState,
    navController: NavHostController
) {

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
    val fExpenseCatNameList by filterVM.expenseCatList.collectAsState()
    val fExpenseCatSelected by filterVM.expenseCatSelected.collectAsState()
    val fIncomeCatNameList by filterVM.incomeCatList.collectAsState()
    val fIncomeCatSelected by filterVM.incomeCatSelected.collectAsState()
    val fDateFilterSelected by filterVM.dateFilter.collectAsState()
    val fStartDateString by filterVM.startDateString.collectAsState()
    val fEndDateString by filterVM.endDateString.collectAsState()
    val filterState by filterVM.filterState.collectAsState()
    val filterStateMessage = stringResource(filterState.stringId)

    LaunchedEffect(key1 = filterState) {
        if (filterState != FilterState.VALID) {
            scaffoldState.snackbarHostState.showSnackbar(filterStateMessage)
            filterVM.updateFilterState(FilterState.VALID)
        }
    }

    // set up AppBar actions
    appBarActionSetup(
        AppBarActions(
            onNavPressed = { scaffoldState.drawerState.open() },
            onActionLeftPressed = { filterVM.updateShowFilter(!fShowFilter) },
            onActionRightPressed = { navController.navigateToTransactionWithId(0) }
        )
    )

    OverviewScreen(
        filterInfo,
        tlPreviousMaxId = tranListVM.previousMaxId,
        tlUpdatePreviousMaxId = tranListVM::updatePreviousMaxId,
        tlTranList,
        tlUpdateTranList = tranListVM::updateTranList,
        tlItemOnLongClick = tranListVM::updateDeleteDialog,
        tlItemOnClick = { tranId -> navController.navigateToTransactionWithId(tranId) },
        tlShowDeleteDialog = tranListShowDeleteDialog,
        tlDialogOnConfirm = { tranId -> tranListVM.deleteTransaction(tranId) },
        tlDialogOnDismiss = { tranListVM.updateDeleteDialog(-1) },
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
        fCategoryList = if (fTypeSelected == EXPENSE) fExpenseCatNameList else fIncomeCatNameList,
        fCategorySelected = if (fTypeSelected == EXPENSE) fExpenseCatSelected else fIncomeCatSelected,
        fCategoryChipOnClick = if (fTypeSelected == EXPENSE)
            filterVM::updateExpenseCatSelected else filterVM::updateIncomeCatSelected,
        fDateFilterSelected,
        fDateFilterOnClick = filterVM::updateDateFilter,
        fStartDateString,
        fStartDateOnClick = filterVM::updateStartDateString,
        fEndDateString,
        fEndDateOnClick = filterVM::updateEndDateString,
        fApplyOnClick = filterVM::applyFilter
    )
}

/**
 *  Composable that displays OverviewScreen.
 *  All the data has been hoisted into above [OverviewScreen] thus allowing for easier testing.
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
    tlDialogOnConfirm: (Int) -> Unit,
    tlDialogOnDismiss: () -> Unit,
    chartInfoList: List<ChartInformation>,
    cUpdateCatTotalsList: suspend (FilterInfo) -> Unit,
    fShowFilter: Boolean,
    fUpdateShowFilter: (Boolean) -> Unit,
    fAccountFilterSelected: Boolean,
    fAccountFilterOnClick: (Boolean) -> Unit,
    fAccountNameList: List<String>,
    fAccountSelected: List<String>,
    fAccountChipOnClick: (String, FilterSelectedAction) -> Unit,
    fCategoryFilterSelected: Boolean,
    fCategoryFilterOnClick: (Boolean) -> Unit,
    fTypeSelected: TransactionType,
    fUpdateTypeSelected: (TransactionType) -> Unit,
    fCategoryList: List<String>,
    fCategorySelected: List<String>,
    fCategoryChipOnClick: (String, FilterSelectedAction) -> Unit,
    fDateFilterSelected: Boolean,
    fDateFilterOnClick: (Boolean) -> Unit,
    fStartDateString: String,
    fStartDateOnClick: (Date) -> Unit,
    fEndDateString: String,
    fEndDateOnClick: (Date) -> Unit,
    fApplyOnClick: () -> Unit
) {

    val fullPad = dimensionResource(R.dimen.cardFullPadding)
    val sharedPad = dimensionResource(R.dimen.cardSharedPadding)

    LaunchedEffect(key1 = filterInfo) { tlUpdateTranList(filterInfo) }
    LaunchedEffect(key1 = filterInfo) { cUpdateCatTotalsList(filterInfo) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ChartCard(
            chartInfoList = chartInfoList,
            modifier = Modifier
                .weight(0.4f)
                .padding(start = fullPad, top = fullPad, end = fullPad, bottom = sharedPad)
        )
        TransactionListCard(
            tranListPreviousMaxId = tlPreviousMaxId,
            tranListUpdatePreviousMaxId = tlUpdatePreviousMaxId,
            tranList = tlTranList,
            tranListItemOnLongClick = tlItemOnLongClick,
            tranListItemOnClick = tlItemOnClick,
            tranListShowDeleteDialog = tlShowDeleteDialog,
            tranListDialogOnConfirm = tlDialogOnConfirm,
            tranListDialogOnDismiss = tlDialogOnDismiss,
            modifier = Modifier
                .weight(0.6f)
                .padding(start = fullPad, top = sharedPad, end = fullPad, bottom = fullPad)
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
        fStartDateString,
        fStartDateOnClick,
        fEndDateString,
        fEndDateOnClick,
        fApplyOnClick
    )
}

/**
 *  Composable that displays Charts and totals using data from [chartInfoList]
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ChartCard(
    chartInfoList: List<ChartInformation>,
    modifier: Modifier = Modifier
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

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // TODO: Look into item scroll effects https://google.github.io/accompanist/pager/#item-scroll-effects
            HorizontalPager(
                count = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .testTag("Chart ViewPager"),
                state = pagerState
            ) { page ->
                val chartInfo = chartInfoList[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (chartInfo.ctList.isNotEmpty()) {
                        Surface(modifier = Modifier.weight(0.8f)) {
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
                        MarqueeText(
                            text = "$totalPrefix${chartInfo.totalText}",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(horizontal = dimensionResource(R.dimen.chartMarginStartEnd))
                                .testTag("Chart Total for page $page")
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.cfl_no_transactions),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .testTag("Empty Chart for page $page"),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 *  Composable that displays [tranList], list of Transactions, in [TranListItem] form.
 *  [tranListPreviousMaxId] is used to determine if a new Transaction was created in order to
 *  scroll to the top of the list automatically which is updated by [tranListUpdatePreviousMaxId].
 *  [tranListItemOnLongClick] and [tranListItemOnClick] are used for deletion and selection
 *  respectively. [tranListShowDeleteDialog] determines when to show AlertDialog, while
 *  [tranListDialogOnConfirm] and [tranListDialogOnDismiss] are used to confirm deletion or deny it
 *  respectively.
 */
@Composable
fun TransactionListCard(
    tranList: List<TranListItemFull>,
    tranListPreviousMaxId: Int,
    tranListUpdatePreviousMaxId: (Int) -> Unit,
    tranListItemOnLongClick: (Int) -> Unit,
    tranListItemOnClick: (Int) -> Unit,
    tranListShowDeleteDialog: Int,
    tranListDialogOnConfirm: (Int) -> Unit,
    tranListDialogOnDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tranListState = rememberLazyListState()

    // scrolls to top of the list when new Transaction is added
    LaunchedEffect(key1 = tranList) {
        if (tranList.isNotEmpty()
            && tranList[tranList.size - 1].transactionItem.id > tranListPreviousMaxId
        ) {
            tranListState.animateScrollToItem(0)
            tranListUpdatePreviousMaxId(tranList[tranList.size - 1].transactionItem.id)
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = tranListState
        ) {
            items(tranList.reversed()) { transactionItemFormatted ->
                val transactionItem = transactionItemFormatted.transactionItem
                Divider(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                TransactionListItem(
                    transactionItemFormatted,
                    onLongClick = { tranListItemOnLongClick(transactionItem.id) },
                    onClick = { tranListItemOnClick(transactionItem.id) },
                )
                if (tranListShowDeleteDialog == transactionItem.id) {
                    PWAlertDialog(
                        onConfirmText = stringResource(R.string.alert_dialog_yes),
                        onConfirm = { tranListDialogOnConfirm(transactionItem.id) },
                        onDismissText = stringResource(R.string.alert_dialog_no),
                        onDismiss = tranListDialogOnDismiss,
                        title = stringResource(R.string.alert_dialog_delete_transaction),
                        message = stringResource(
                            R.string.alert_dialog_delete_warning,
                            transactionItem.title
                        )
                    )
                }
            }
        }
        if (tranList.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.cfl_no_transactions),
                    modifier = Modifier.testTag("Empty Transaction List"),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 *  Composable for scrolling Text. Text will scroll indefinitely when it does not fit in given area.
 *  [text] is to be displayed using [style], [color], and [textAlign].
 */
@Composable
fun MarqueeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface,
    textAlign: TextAlign? = null
) {
    val scrollState = rememberScrollState()
    var animate by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = animate) {
        scrollState.animateScrollTo(
            value = scrollState.maxValue,
            animationSpec = tween(
                durationMillis = 4000,
                delayMillis = 1000,
                easing = CubicBezierEasing(0f, 0f, 0f, 0f)
            )
        )
        delay(1000)
        scrollState.scrollTo(0)
        animate = !animate
    }

    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState, false),
        color = color,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = style
    )
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
    val transactionItem = transactionItemFormatted.transactionItem
    Surface(
        modifier = Modifier
            .combinedClickable(
                onLongClick = onLongClick, onClick = onClick
            )
            .testTag("${transactionItem.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 4.dp, end = 4.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MarqueeText(
                    text = transactionItem.title,
                    style = MaterialTheme.typography.subtitle1
                )
                MarqueeText(
                    text = transactionItem.account,
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                MarqueeText(
                    text = transactionItemFormatted.formattedDate,
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MarqueeText(
                    text = transactionItemFormatted.formattedTotal,
                    style = MaterialTheme.typography.subtitle1,
                    color = when (transactionItem.type) {
                        "Expense" -> LocalPWColors.current.expense
                        else -> LocalPWColors.current.income
                    },
                    textAlign = TextAlign.End
                )
                MarqueeText(
                    text = transactionItem.category,
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FilterCard(
    showFilter: Boolean,
    updateShowFilter: (Boolean) -> Unit,
    accountFilterSelected: Boolean,
    accountFilterOnClick: (Boolean) -> Unit,
    accountList: List<String>,
    accountSelected: List<String>,
    accountChipOnClick: (String, FilterSelectedAction) -> Unit,
    categoryFilterSelected: Boolean,
    categoryFilterOnClick: (Boolean) -> Unit,
    filterTypeSelected: TransactionType,
    filterUpdateTypeSelected: (TransactionType) -> Unit,
    categoryList: List<String>,
    categorySelected: List<String>,
    categoryChipOnClick: (String, FilterSelectedAction) -> Unit,
    dateFilterSelected: Boolean,
    dateFilterOnClick: (Boolean) -> Unit,
    startDateString: String,
    startDateOnClick: (Date) -> Unit,
    endDateString: String,
    endDateOnClick: (Date) -> Unit,
    applyOnClick: () -> Unit
) {
    // used by animation to determine Y offset
    var filterComposeSize by remember { mutableStateOf(Size.Zero) }
    var accountComposeSize by remember { mutableStateOf(Size.Zero) }
    var categoryComposeSize by remember { mutableStateOf(Size.Zero) }
    var dateComposeSize by remember { mutableStateOf(Size.Zero) }

    val view = LocalView.current
    val noFilters = !accountFilterSelected && !categoryFilterSelected && !dateFilterSelected

    val typeSelectedLabel = stringResource(filterTypeSelected.stringId)

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
                PWButtonChip(
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
                                PlutusWalletChip(
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
                PWButtonChip(
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
                        PWButtonChip(
                            selected = true,
                            onClick = { filterUpdateTypeSelected(filterTypeSelected.opposite()) },
                            label = typeSelectedLabel,
                            showIcon = false,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.f_button_chip_height))
                                .testTag("$typeSelectedLabel Button"),
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
                                    PlutusWalletChip(
                                        selected = categorySelected.contains(category),
                                        onClick = {
                                            categoryChipOnClick(
                                                category,
                                                if (categorySelected.contains(category)) REMOVE else ADD
                                            )
                                        },
                                        label = category
                                    )
                                }
                            }
                        }
                    }
                }
                PWButtonChip(
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
                        PWButtonChip(
                            selected = true,
                            onClick = {
                                DateUtils.datePickerDialog(
                                    view,
                                    initDate = if (startDateString.isNotBlank()) {
                                        DateFormat.getDateInstance().parse(startDateString)
                                    } else {
                                        Date()
                                    },
                                    onDateSelected = startDateOnClick
                                ).show()
                            },
                            label = startDateString.ifBlank { stringResource(R.string.filter_start) },
                            showIcon = false,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.f_button_chip_height))
                                .testTag("Filter Start Date"),
                        )
                        PWButtonChip(
                            selected = true,
                            onClick = {
                                DateUtils.datePickerDialog(
                                    view,
                                    initDate = if (endDateString.isNotBlank()) {
                                        DateFormat.getDateInstance().parse(endDateString)
                                    } else {
                                        Date()
                                    },
                                    onDateSelected = endDateOnClick
                                ).show()
                            },
                            label = endDateString.ifBlank { stringResource(R.string.filter_end) },
                            showIcon = false,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.f_button_chip_height))
                                .padding(bottom = 6.dp)
                                .testTag("Filter End Date"),
                        )
                    }
                }
                PWButtonChip(
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
                        .testTag("Filter action"),
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlutusWalletChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected,
        onClick,
        modifier = Modifier.testTag("Chip: $label"),
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
fun FilterCardPreview() {
    var accountFilterSelected = true
    var categoryFilterSelected = false
    var dateFilterSelected = true
    PlutusWalletTheme {
        FilterCard(
            showFilter = true,
            updateShowFilter = { },
            accountFilterSelected = accountFilterSelected,
            accountFilterOnClick = { accountFilterSelected = !accountFilterSelected },
            accountList = listOf("Preview"),
            accountSelected = listOf(),
            accountChipOnClick = { _, _ -> },
            categoryFilterSelected = categoryFilterSelected,
            categoryFilterOnClick = { categoryFilterSelected = !categoryFilterSelected},
            filterTypeSelected = EXPENSE,
            filterUpdateTypeSelected = { },
            categoryList = listOf("Preview"),
            categorySelected = listOf(),
            categoryChipOnClick = { _, _ -> },
            dateFilterSelected = dateFilterSelected,
            dateFilterOnClick = { dateFilterSelected = !dateFilterSelected},
            startDateString = "Start",
            startDateOnClick = { },
            endDateString = "End",
            endDateOnClick = { },
            applyOnClick = { }
        )
    }
}

@Preview
@Composable
fun TransactionListItemPreview() {
    val ivTransaction = TranListItem(
        0, "This is a very long title to test marquee text", Date(),
        BigDecimal(1000000000000000000), "Account", "Expense", "Category"
    )
    val transactionItemFormatted = TranListItemFull(
        ivTransaction, "$123.45", "Jan 1, 2000"
    )
    PlutusWalletTheme {
        TransactionListItem(
            transactionItemFormatted,
            onLongClick = { },
            onClick = { },
        )
    }
}