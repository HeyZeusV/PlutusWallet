package com.heyzeusv.plutuswallet.ui.overview

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.ChartInformation
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.delay

@Composable
fun OverviewScreen(
    tranList: List<ItemViewTransaction>,
    tranListPreviousSize: Int,
    tranListUpdatePreviousSize: (Int) -> Unit,
    tranListItemOnLongClick: (Int) -> Unit,
    tranListItemOnClick: (Int) -> Unit,
    tranListShowDeleteDialog: Int,
    tranListDialogOnConfirm: (Int) -> Unit,
    tranListDialogOnDismiss: () -> Unit,
    chartInfoList: List<ChartInformation>
) {
    val fullPad = dimensionResource(R.dimen.cardFullPadding)
    val sharedPad = dimensionResource(R.dimen.cardSharedPadding)

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
            tranListPreviousSize = tranListPreviousSize,
            tranListUpdatePreviousSize = tranListUpdatePreviousSize,
            tranList = tranList,
            tranListItemOnLongClick = tranListItemOnLongClick,
            tranListItemOnClick = tranListItemOnClick,
            tranListShowDeleteDialog = tranListShowDeleteDialog,
            tranListDialogOnConfirm = tranListDialogOnConfirm,
            tranListDialogOnDismiss = tranListDialogOnDismiss,
            modifier = Modifier
                .weight(0.6f)
                .padding(start = fullPad, top = sharedPad, end = fullPad, bottom = fullPad)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ChartCard(
    chartInfoList: List<ChartInformation>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState()

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
                                .padding(vertical = 8.dp)
                                .weight(0.8f),
                            update = { pieChart: PieChart ->
                                // list of entries to be displayed in PieChart
                                val pieEntries: List<PieEntry> = chartInfo.ctList.map { catTotal ->
                                    PieEntry(catTotal.total.toFloat(), catTotal.category)
                                }

                                // PieDataSet set up
                                val dataSet = PieDataSet(pieEntries, "Transactions")
                                // distance between slices
                                dataSet.sliceSpace = 2.5f
                                // size of percent value
                                dataSet.valueTextSize = 13f
                                // color of percent value
                                dataSet.valueTextColor =
                                    ContextCompat.getColor(pieChart.context, R.color.colorChartText)
                                // colors used for slices
                                dataSet.colors = chartColorLists[page]
                                // size of highlighted area
                                // TODO: Check if statement is necessary
                                if (chartInfo.fCategory /* TODO: Check this: `&& !ivc.fCatName.contains("All")` */) {
                                    dataSet.selectionShift = 10f
                                } else {
                                    dataSet.selectionShift = 0.0f
                                }

                                // PieData set up
                                val pData = PieData(dataSet)
                                // makes values in form of percentages
                                pData.setValueFormatter(PercentFormatter(pieChart))
                                // PieChart set up
                                pieChart.data = pData

                                val highlights: MutableList<Highlight> = mutableListOf()
                                // highlights Category selected if it exists with current filters applied
                                if (chartInfo.fCategory /* TODO: Check this: `&& !ivc.fCatName.contains("All")` */) {
                                    for (cat: String in chartInfo.fCatName) {
                                        // finds position of Category selected in FilterFragment in ctList
                                        val position: Int =
                                            chartInfo.ctList.indexOfFirst { it.category == cat }
                                        // -1 = doesn't exist
                                        if (position != -1) highlights.add(
                                            Highlight(
                                                position.toFloat(),
                                                0,
                                                0
                                            )
                                        )
                                    }
                                }
                                pieChart.highlightValues(highlights.toTypedArray())

                            }
                        )
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
                                .testTag("Empty Chart for page $page")
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

@Composable
fun TransactionListCard(
    tranList: List<ItemViewTransaction>,
    tranListPreviousSize: Int,
    tranListUpdatePreviousSize: (Int) -> Unit,
    tranListItemOnLongClick: (Int) -> Unit,
    tranListItemOnClick: (Int) -> Unit,
    tranListShowDeleteDialog: Int,
    tranListDialogOnConfirm: (Int) -> Unit,
    tranListDialogOnDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tranListState = rememberLazyListState()

    LaunchedEffect(key1 = tranList) {
        if (tranList.size > tranListPreviousSize) {
            tranListState.animateScrollToItem(0)
            tranListUpdatePreviousSize(tranList.size)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()

    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = tranListState
        ) {
            items(tranList.reversed()) { ivTransaction ->
                Divider(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                TransactionListItem(
                    onLongClick = { tranListItemOnLongClick(ivTransaction.id) },
                    onClick = { tranListItemOnClick(ivTransaction.id) },
                    ivTransaction = ivTransaction,
                )
                if (tranListShowDeleteDialog == ivTransaction.id) {
                    PWAlertDialog(
                        onConfirmText = stringResource(R.string.alert_dialog_yes),
                        onConfirm = { tranListDialogOnConfirm(ivTransaction.id) },
                        onDismissText = stringResource(R.string.alert_dialog_no),
                        onDismiss = tranListDialogOnDismiss,
                        title = stringResource(R.string.alert_dialog_delete_transaction),
                        message = stringResource(
                            R.string.alert_dialog_delete_warning,
                            ivTransaction.title
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
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListItem(
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    ivTransaction: ItemViewTransaction,
) {
    Surface(
        modifier = Modifier
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .testTag("${ivTransaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 4.dp, end = 4.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MarqueeText(
                    text = ivTransaction.title,
                    style = MaterialTheme.typography.subtitle1
                )
                MarqueeText(
                    text = ivTransaction.account,
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                MarqueeText(
                    text = ivTransaction.formattedDate,
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
                    text = ivTransaction.formattedTotal,
                    style = MaterialTheme.typography.subtitle1,
                    color = when (ivTransaction.type) {
                        "Expense" -> LocalPWColors.current.expense
                        else -> LocalPWColors.current.income
                    },
                    textAlign = TextAlign.End
                )
                MarqueeText(
                    text = ivTransaction.category,
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Preview
@Composable
fun TransactionListItemPreview() {
    val ivTransaction = ItemViewTransaction(
        0, "This is a very long title to test marquee text", Date(),
        BigDecimal(1000000000000000000), "Account", "Expense", "Category"
    )
    PlutusWalletTheme {
        TransactionListItem(
            onLongClick = { },
            onClick = { },
            ivTransaction = ivTransaction,
        )
    }
}