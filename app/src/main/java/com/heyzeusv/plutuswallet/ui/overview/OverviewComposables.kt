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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay

@Composable
fun OverviewScreen(
    tranListVM: TransactionListViewModel,
    tranList: List<ItemViewTransaction>,
    tranListItemOnLongClick: (Int) -> Unit,
    tranListItemOnClick: (Int) -> Unit,
    tranListShowDeleteDialog: Int,
    tranListDialogOnConfirm: (Int) -> Unit,
    tranListDialogOnDismiss: () -> Unit
) {
    val tranListState = rememberLazyListState()

    LaunchedEffect(key1 = tranList) {
        if (tranList.size > tranListVM.previousListSize) {
            tranListState.animateScrollToItem(0)
            tranListVM.previousListSize = tranList.size
        }
    }
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.cardFullPadding))
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
                        message = stringResource(R.string.alert_dialog_delete_warning, ivTransaction.title)
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
        modifier = Modifier.combinedClickable(
            onLongClick = onLongClick,
            onClick = onClick
        )
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