package com.heyzeusv.plutuswallet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.DataInterface

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DataScreen(
    dataLists: List<List<DataInterface>>,
    editOnClick: () -> Unit,
    deleteOnClick: () -> Unit
) {
    val pagerState = rememberPagerState()

    val dataListsSize = dataLists.size

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
                if (dataListsSize > 1) {
                    Text(
                        text = if (page == 0) {
                            stringResource(R.string.type_expense)
                        } else {
                            stringResource(R.string.type_income)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(dataLists[page]) { data ->
                        DataItem(data, editOnClick, deleteOnClick)
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
fun DataItem(
    data: DataInterface,
    editOnClick: () -> Unit,
    deleteOnClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = data.name)
        Button(onClick = editOnClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.data_edit)
            )
        }
        Button(onClick = deleteOnClick) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.data_delete)
            )
        }
    }
}