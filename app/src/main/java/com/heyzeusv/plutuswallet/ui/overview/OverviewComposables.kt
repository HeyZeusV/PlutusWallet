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
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.ui.cfl.chart.ChartViewModel
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OverviewScreen(
    tranListVM: TransactionListViewModel,
    tranList: List<ItemViewTransaction>,
    tranListItemOnLongClick: (Int) -> Unit,
    tranListItemOnClick: (Int) -> Unit,
    tranListShowDeleteDialog: Int,
    tranListDialogOnConfirm: (Int) -> Unit,
    tranListDialogOnDismiss: () -> Unit,
    chartVM: ChartViewModel
) {
    val tranListState = rememberLazyListState()
    val pagerState = rememberPagerState()
    val fullPad = dimensionResource(R.dimen.cardFullPadding)
    val sharedPad = dimensionResource(R.dimen.cardSharedPadding)

    LaunchedEffect(key1 = tranList) {
        if (tranList.size > tranListVM.previousListSize) {
            tranListState.animateScrollToItem(0)
            tranListVM.previousListSize = tranList.size
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .padding(start = fullPad, top = fullPad, end = fullPad, bottom = sharedPad)
        ) {
            HorizontalPager(
                count = 2,
                modifier = Modifier.fillMaxSize(),
                state = pagerState
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(factory = { context ->
                        PieChart(context).apply {
                            val ivc = chartVM.ivcList[page]
                            // no chart to create if ctList is empty
                            if (ivc.ctList.isNotEmpty()) {
                                // either "Expense" or "Income"
                                val type: String = ivc.ctList[0].type

                                // list of values to be displayed in PieChart
                                val pieEntries: List<PieEntry> = ivc.ctList.map { PieEntry(it.total.toFloat(), it.category) }

                                // PieDataSet set up
                                val dataSet = PieDataSet(pieEntries, "Transactions")
                                // distance between slices
                                dataSet.sliceSpace = 2.5f
                                // size of percent value
                                dataSet.valueTextSize = 13f
                                // color of percent value
                                dataSet.valueTextColor = ContextCompat.getColor(context, R.color.colorChartText)
                                // colors used for slices
                                dataSet.colors = ivc.colorArray
                                // size of highlighted area
                                if (ivc.fCategory && ivc.fType == type && !ivc.fCatName.contains("All")) {
                                    dataSet.selectionShift = 10f
                                } else {
                                    dataSet.selectionShift = 0.0f
                                }

                                // PieData set up
                                val pData = PieData(dataSet)
                                // makes values in form of percentages
                                pData.setValueFormatter(PercentFormatter(this))
                                // PieChart set up
                                data = pData
                                // displays translated type in center of chart
                                centerText = ivc.typeTrans
                                // don't want a description so make it blank
                                description.text = ""
                                // don't want legend so disable it
                                legend.isEnabled = false
                                // true = doughnut chart
                                isDrawHoleEnabled = true
                                // color of labels
                                setEntryLabelColor(ContextCompat.getColor(this.context, R.color.colorChartText))
                                // size of Category labels
                                setEntryLabelTextSize(14.5f)
                                // color of center hole
                                setHoleColor(ContextCompat.getColor(this.context, R.color.colorChartHole))
                                // size of center text
                                setCenterTextSize(15f)
                                // color of center text
                                setCenterTextColor(ContextCompat.getColor(this.context, R.color.textColorPrimary))
                                // true = display center text
                                setDrawCenterText(true)
                                // true = use percent values
                                setUsePercentValues(true)
                                val highlights: MutableList<Highlight> = mutableListOf()
                                // highlights Category selected if it exists with current filters applied
                                if (ivc.fCategory && ivc.fType == type && !ivc.fCatName.contains("All")) {
                                    for (cat: String in ivc.fCatName) {
                                        // finds position of Category selected in FilterFragment in ctList
                                        val position: Int = ivc.ctList.indexOfFirst { it.category == cat }
                                        // -1 = doesn't exist
                                        if (position != -1) highlights.add(Highlight(position.toFloat(), 0, 0))
                                    }
                                }
                                highlightValues(highlights.toTypedArray())
                                invalidate()
                            }
                        }
                    },
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "$page",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            HorizontalPagerIndicator(pagerState = pagerState)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .padding(start = fullPad, top = sharedPad, end = fullPad, bottom = fullPad)
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
                        setVals = tranListVM.setVals
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
}

@Composable
fun MarqueeText(
    text: String,
    color: Color = MaterialTheme.colors.onSurface,
    textAlign: TextAlign? = null,
    style: TextStyle
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
        modifier = Modifier
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
    setVals: SettingsValues
) {
    val formattedDate = DateFormat.getDateInstance(setVals.dateFormat).format(ivTransaction.date)
    val total = when {
        // currency symbol on left with decimal places
        setVals.decimalPlaces && setVals.symbolSide ->
            "${setVals.currencySymbol}${setVals.decimalFormatter.format(ivTransaction.total)}"
        // currency symbol on right with decimal places
        setVals.decimalPlaces ->
            "${setVals.decimalFormatter.format(ivTransaction.total)}${setVals.currencySymbol}"
        // currency symbol on left without decimal places
        setVals.symbolSide ->
            "${setVals.currencySymbol}${setVals.integerFormatter.format(ivTransaction.total)}"
        // currency symbol on right without decimal places
        else -> "${setVals.integerFormatter.format(ivTransaction.total)}${setVals.currencySymbol}"
    }

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
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.subtitle2
                )
                MarqueeText(
                    text = formattedDate,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.subtitle2
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MarqueeText(
                    text = total,
                    color = when (ivTransaction.type) {
                        "Expense" -> LocalPWColors.current.expense
                        else -> LocalPWColors.current.income
                    },
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.subtitle1
                )
                MarqueeText(
                    text = ivTransaction.category,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.subtitle2
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
            setVals = SettingsValues()
        )
    }
}