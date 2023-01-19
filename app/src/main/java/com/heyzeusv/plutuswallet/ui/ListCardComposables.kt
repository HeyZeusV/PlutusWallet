package com.heyzeusv.plutuswallet.ui

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
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.CREATE
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.DELETE
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.EDIT
import com.heyzeusv.plutuswallet.ui.transaction.InputAlertDialog
import com.heyzeusv.plutuswallet.util.PWAlertDialog

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ListCard(
    snackbarHostState: SnackbarHostState,
    dataLists: List<List<DataInterface>>,
    usedDataLists: List<List<DataInterface>>,
    listSubtitles: List<String> = listOf(""),
    onClick: (DataDialog) -> Unit,
    showDialog: DataDialog,
    createDialogTitle: String,
    createDialogOnConfirm: (String) -> Unit,
    deleteDialogTitle: String,
    deleteDialogOnConfirm: (DataInterface) -> Unit,
    editDialogTitle: String,
    editDialogOnConfirm: (DataInterface, String) -> Unit,
    dialogOnDismiss: (DataDialog) -> Unit,
    existsName: String
) {
    val pagerState = rememberPagerState()

    val dataListsSize = dataLists.size
    val existsMessage = stringResource(R.string.snackbar_exists, existsName)

    LaunchedEffect(key1 = existsName) {
        if (existsName.isNotBlank()) {
            snackbarHostState.showSnackbar(existsMessage)
        }
    }
    if (showDialog.action == CREATE) {
        InputAlertDialog(
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
                    .testTag("Data ViewPager"),
                state = pagerState
            ) { page ->
                Column {
                    if (dataLists.isNotEmpty()) {
                        Text(
                            text = listSubtitles[page],
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 12.dp),
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
                                            onConfirmText = stringResource(R.string.alert_dialog_yes),
                                            onConfirm = { deleteDialogOnConfirm(data) },
                                            onDismissText = stringResource(R.string.alert_dialog_no),
                                            onDismiss = { dialogOnDismiss(DataDialog(DELETE, -1)) },
                                            title = deleteDialogTitle,
                                            message = stringResource(
                                                R.string.alert_dialog_delete_warning,
                                                data.name
                                            )
                                        )
                                    }
                                    EDIT -> {
                                        InputAlertDialog(
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
                contentColor = Color.White
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
                contentColor = Color.White
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