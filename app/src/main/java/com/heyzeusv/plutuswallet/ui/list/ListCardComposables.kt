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
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.ui.AppBarActions
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.DataListSelectedAction.CREATE
import com.heyzeusv.plutuswallet.util.DataListSelectedAction.DELETE
import com.heyzeusv.plutuswallet.util.DataListSelectedAction.EDIT
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import com.heyzeusv.plutuswallet.util.PWInputAlertDialog
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
    val existsMessage = stringResource(R.string.snackbar_exists, itemExists)

    val firstItemList by viewModel.firstItemList.collectAsState()
    val secondItemList by viewModel.secondItemList.collectAsState()
    val firstUsedItemList by viewModel.firstUsedItemList.collectAsState()
    val secondUsedItemList by viewModel.secondUsedItemList.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()

    LaunchedEffect(key1 = itemExists) {
        if (itemExists.isNotBlank()) showSnackbar(existsMessage)
    }
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
                viewModel.updateDialog(DataDialog(CREATE, 0, type))
            }
        )
    )
    ListCard(
        pagerState = pagerState,
        dataLists = listOf(firstItemList, secondItemList),
        usedDataLists = listOf(firstUsedItemList, secondUsedItemList),
        listSubtitles = viewModel.listSubtitleStringIds,
        onClick = viewModel::updateDialog,
        showDialog = showDialog,
        createDialogTitle = stringResource(viewModel.createItemStringId),
        createDialogOnConfirm = viewModel::insertItem,
        deleteDialogTitle = stringResource(viewModel.deleteItemStringId),
        deleteDialogOnConfirm = viewModel::deleteItem,
        editDialogTitle = stringResource(viewModel.editItemStringId),
        editDialogOnConfirm = viewModel::editItem,
        dialogOnDismiss = viewModel::updateDialog
    )
}

/**
 *  Composable that displays List card.
 *  All the data has been hoisted into above [ListCard] thus allowing for easier testing.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ListCard(
    pagerState: PagerState,
    dataLists: List<List<DataInterface>>,
    usedDataLists: List<List<DataInterface>>,
    listSubtitles: List<Int>,
    onClick: (DataDialog) -> Unit,
    showDialog: DataDialog,
    createDialogTitle: String,
    createDialogOnConfirm: (String) -> Unit,
    deleteDialogTitle: String,
    deleteDialogOnConfirm: (DataInterface) -> Unit,
    editDialogTitle: String,
    editDialogOnConfirm: (DataInterface, String) -> Unit,
    dialogOnDismiss: (DataDialog) -> Unit,
) {
    val dataListsSize = dataLists.size

    if (showDialog.action == CREATE) {
        PWInputAlertDialog(
            title = createDialogTitle,
            onDismiss = { dialogOnDismiss(DataDialog(EDIT, -1)) },
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
                        Text(
                            text = stringResource(listSubtitles[page]),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 12.dp)
                                .testTag("List Subtitle ${listSubtitles[page]}"),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.h5
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(dataLists[page]) { data ->
                            DataItem(
                                data = data,
                                deletable = !usedDataLists[page].contains(data),
                                editOnClick = { onClick(DataDialog(EDIT, data.id)) },
                                deleteOnClick = { onClick(DataDialog(DELETE, data.id)) }
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
                                            onDismiss = { dialogOnDismiss(DataDialog(DELETE, -1)) }
                                        )
                                    }
                                    EDIT -> {
                                        PWInputAlertDialog(
                                            title = editDialogTitle,
                                            onDismiss = { dialogOnDismiss(DataDialog(EDIT, -1)) },
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
            if (dataListsSize > 1) {
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

@Composable
fun DataItem(
    data: DataInterface,
    deletable: Boolean,
    editOnClick: () -> Unit,
    deleteOnClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ,
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

@Preview
@Composable
fun PreviewDataItem() {
    PlutusWalletTheme {
        DataItem(
            data = Account(0, "Preview"),
            deletable = false,
            editOnClick = { /*TODO*/ },
            deleteOnClick = { }
        )
    }
}