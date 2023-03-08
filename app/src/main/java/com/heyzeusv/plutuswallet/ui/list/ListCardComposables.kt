package com.heyzeusv.plutuswallet.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.ListDialog
import com.heyzeusv.plutuswallet.data.model.ListItemInterface
import com.heyzeusv.plutuswallet.ui.PreviewHelperCard
import com.heyzeusv.plutuswallet.ui.PWAlertDialog
import com.heyzeusv.plutuswallet.ui.PWInputAlertDialog
import com.heyzeusv.plutuswallet.util.AppBarActions
import com.heyzeusv.plutuswallet.util.ListItemAction.CREATE
import com.heyzeusv.plutuswallet.util.ListItemAction.DELETE
import com.heyzeusv.plutuswallet.util.ListItemAction.EDIT
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME

/**
 *  Composable that displays List card.
 *  Data that is displayed is retrieved from [viewModel]. [appBarActionSetup] determines what to do
 *  when an action item is pressed from the AppBar. [showSnackbar] is used to display Snackbar.
 *  [navigateUp] returns user back to OverviewScreen. [pagerState] is used to determine which page
 *  the user is currently viewing.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ListCard(
    viewModel: ListViewModel,
    appBarActionSetup: (AppBarActions) -> Unit,
    showSnackbar: suspend (String) -> Unit,
    navigateUp: () -> Unit,
    pagerState: PagerState
) {
    val itemExists by viewModel.itemExists.collectAsState()

    val firstItemList by viewModel.firstItemList.collectAsState()
    val secondItemList by viewModel.secondItemList.collectAsState()
    val firstUsedItemList by viewModel.firstUsedItemList.collectAsState()
    val secondUsedItemList by viewModel.secondUsedItemList.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()

    // set up AppBar actions
    appBarActionSetup(
        AppBarActions(
            onNavPressed = {
                viewModel.updateItemExists("")
                navigateUp()
            },
            onActionRightPressed = {
                val type = when (pagerState.currentPage) {
                    0 -> EXPENSE
                    else -> INCOME
                }
                viewModel.updateDialog(ListDialog(CREATE, 0, type))
            }
        )
    )
    ListCard(
        pagerState,
        dataLists = listOf(firstItemList, secondItemList),
        usedDataLists = listOf(firstUsedItemList, secondUsedItemList),
        listSubtitles = viewModel.listSubtitleStringIds,
        onClick = viewModel::updateDialog,
        showDialog,
        createDialogTitle = stringResource(viewModel.createItemStringId),
        createDialogOnConfirm = viewModel::insertItem,
        deleteDialogTitle = stringResource(viewModel.deleteItemStringId),
        deleteDialogOnConfirm = viewModel::deleteItem,
        editDialogTitle = stringResource(viewModel.editItemStringId),
        editDialogOnConfirm = viewModel::editItem,
        dialogOnDismiss = viewModel::updateDialog,
        itemExists,
        showSnackbar = { msg ->
            showSnackbar(msg)
            viewModel.updateItemExists("")
        }
    )
}

/**
 *  Composable that displays List card.
 *  All the data has been hoisted into above [ListCard] thus allowing for easier testing.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ListCard(
    pagerState: PagerState = rememberPagerState(),
    dataLists: List<List<ListItemInterface>> = emptyList(),
    usedDataLists: List<List<ListItemInterface>> = emptyList(),
    listSubtitles: List<Int> = emptyList(),
    onClick: (ListDialog) -> Unit = { },
    showDialog: ListDialog = ListDialog(EDIT, -1),
    createDialogTitle: String = "",
    createDialogOnConfirm: (String) -> Unit = { },
    deleteDialogTitle: String = "",
    deleteDialogOnConfirm: (ListItemInterface) -> Unit = { },
    editDialogTitle: String = "",
    editDialogOnConfirm: (ListItemInterface, String) -> Unit = { _, _ -> },
    dialogOnDismiss: (ListDialog) -> Unit = { },
    itemExists: String = "",
    showSnackbar: suspend (String) -> Unit = { }
) {
    val existsMessage = stringResource(R.string.snackbar_exists, itemExists)
    val dataListsSize = dataLists.size

    LaunchedEffect(key1 = itemExists) {
        if (itemExists.isNotBlank()) showSnackbar(existsMessage)
    }
    if (showDialog.action == CREATE) {
        PWInputAlertDialog(
            title = createDialogTitle,
            onDismiss = { dialogOnDismiss(ListDialog(EDIT, -1)) },
            onConfirm = createDialogOnConfirm
        )
    }
    Card(
        modifier = Modifier
            .padding(all = dimensionResource(R.dimen.cardFullPadding))
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                count = dataListsSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .testTag("List ViewPager"),
                state = pagerState
            ) { page ->
                Column {
                    if (listSubtitles.isNotEmpty()) {
                        val subtitle = stringResource(listSubtitles[page])
                        Text(
                            text = subtitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 12.dp)
                                .testTag("List Subtitle $subtitle"),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.h5
                        )
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(dataLists[page]) { data ->
                            DataItem(
                                data = data,
                                deletable = !usedDataLists[page].contains(data),
                                editOnClick = { onClick(ListDialog(EDIT, data.id)) },
                                deleteOnClick = { onClick(ListDialog(DELETE, data.id)) }
                            )
                            Divider(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )
                            if (showDialog.id == data.id) {
                                when (showDialog.action) {
                                    DELETE -> {
                                        PWAlertDialog(
                                            title = deleteDialogTitle,
                                            message = stringResource(
                                                R.string.alert_dialog_delete_warning,
                                                data.name
                                            ),
                                            onConfirmText = stringResource(R.string.alert_dialog_yes),
                                            onConfirm = { deleteDialogOnConfirm(data) },
                                            onDismissText = stringResource(R.string.alert_dialog_no),
                                            onDismiss = { dialogOnDismiss(ListDialog(DELETE, -1)) }
                                        )
                                    }
                                    EDIT -> {
                                        PWInputAlertDialog(
                                            title = editDialogTitle,
                                            onDismiss = { dialogOnDismiss(ListDialog(EDIT, -1)) },
                                            data = data,
                                            onConfirmData = editDialogOnConfirm
                                        )
                                    }
                                    CREATE -> {}
                                }
                            }
                        }
                    }
                }
            }
            if (listSubtitles.isNotEmpty()) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 *  Composable that displays [data]. If [deletable] is true, then user can press right button for
 *  [deleteOnClick]. [editOnClick], the left button, is always available for the user.
 */
@Composable
fun DataItem(
    data: ListItemInterface,
    deletable: Boolean,
    editOnClick: () -> Unit,
    deleteOnClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = data.name,
            modifier = Modifier.weight(0.8f)
        )
        Button(
            onClick = editOnClick,
            modifier = Modifier
                .size(49.dp)
                .testTag("${data.name} Edit"),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary,
                contentColor = MaterialTheme.colors.surface
            ),
            contentPadding = PaddingValues(all = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.data_edit)
            )
        }
        Button(
            onClick = deleteOnClick,
            modifier = Modifier
                .size(49.dp)
                .testTag("${data.name} Delete"),
            enabled = deletable,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary,
                contentColor = MaterialTheme.colors.surface
            ),
            contentPadding = PaddingValues(all = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.data_delete)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Preview
@Composable
fun ListCardPreview() {
    val exCat = Category(0, "Test EX Cat", "Expense")
    val inCat = Category(0, "Test IN Cat", "Income")
    PreviewHelperCard {
        ListCard(
            pagerState = rememberPagerState(),
            dataLists = listOf(listOf(exCat, exCat), listOf(inCat, inCat)),
            usedDataLists = listOf(listOf(exCat), emptyList()),
            listSubtitles = listOf(R.string.type_expense, R.string.type_income),
            onClick = { },
            showDialog = ListDialog(EDIT, -1, EXPENSE),
            createDialogTitle = "Create Title",
            createDialogOnConfirm = { },
            deleteDialogTitle = "Delete Title",
            deleteDialogOnConfirm = { },
            editDialogTitle = "Edit Title",
            editDialogOnConfirm = { _, _ -> },
            dialogOnDismiss = { }
        )
    }
}

@Preview
@Composable
fun PreviewDataItem() {
    PreviewHelperCard {
        DataItem(
            data = Account(0, "Preview"),
            deletable = false,
            editOnClick = { },
            deleteOnClick = { }
        )
    }
}